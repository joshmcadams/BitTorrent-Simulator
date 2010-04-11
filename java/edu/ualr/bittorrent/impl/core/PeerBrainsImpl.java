package edu.ualr.bittorrent.impl.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.Instant;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.sun.tools.javac.util.Pair;

import edu.ualr.bittorrent.impl.core.messages.ChokeImpl;
import edu.ualr.bittorrent.impl.core.messages.HandshakeImpl;
import edu.ualr.bittorrent.impl.core.messages.HaveImpl;
import edu.ualr.bittorrent.impl.core.messages.KeepAliveImpl;
import edu.ualr.bittorrent.interfaces.Message;
import edu.ualr.bittorrent.interfaces.Metainfo;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.PeerBrains;
import edu.ualr.bittorrent.interfaces.PeerState;
import edu.ualr.bittorrent.interfaces.PeerState.ChokeStatus;
import edu.ualr.bittorrent.interfaces.PeerState.PieceDeclaration;

public class PeerBrainsImpl implements PeerBrains {
  private Map<Peer, PeerState> activePeers;
  private Peer localPeer;
  private Metainfo metainfo;
  private Map<Integer, byte[]> data;
  private static final Logger logger = Logger.getLogger(PeerBrainsImpl.class);

  public void setLocalPeer(Peer local) {
    this.localPeer = Preconditions.checkNotNull(local);
  }

  public void setActivePeers(Map<Peer, PeerState> activePeers) {
    this.activePeers = Preconditions.checkNotNull(activePeers);
  }

  public void setMetainfo(Metainfo metainfo) {
    this.metainfo = Preconditions.checkNotNull(metainfo);
  }

  public void setData(Map<Integer, byte[]> data) {
    this.data = Preconditions.checkNotNull(data);
  }

  public List<Pair<Peer, Message>> getMessagesToDispatch() {
    Preconditions.checkNotNull(localPeer);
    Preconditions.checkNotNull(activePeers);
    Preconditions.checkNotNull(metainfo);
    Preconditions.checkNotNull(data);

    List<Pair<Peer, Message>> messages = Lists.newArrayList();

    Set<Peer> peers;

    synchronized (activePeers) {
      peers = activePeers.keySet();
    }

    for (Peer p : peers) {
      if (p.equals(localPeer)) {
        continue;
      }

      PeerState state = null;
      synchronized (activePeers) {
        if (activePeers.containsKey(p)) {
          state = activePeers.get(p);
        }
      }

      if (state == null) {
        continue;
      }

      /* If the local client hasn't sent a handshake to this peer yet, send one */
      if (sendHandshake(p, state, messages)) {
        continue;
      }

      /* If the remote peer hasn't sent a handshake yet, send them another */

      if (!remoteHasSentHandshake(p, state, messages)) {
        continue;
      }

      /* If we haven't started communicating with this peer yet, then the choke status will be null.
       * If this is the case, go ahead and send a choke message to make the initial choke state
       * official.
       */

      if (sendInitialChoke(p, state, messages)) {
        continue;
      }

      /* Let the remote peer know about any new pieces that we might have */
      letPeerKnowAboutNewPieces(p, state, messages);

      /* If no other messages are necessary, just send a keep alive */
      sendKeepAlive(p, state, messages);
    }

    return messages;
  }

  private boolean sendHandshake(
      Peer remotePeer, PeerState state, List<Pair<Peer, Message>> messages) {
    Instant localSentHandshakeAt = null;
    synchronized(state) {
      localSentHandshakeAt = state.whenDidLocalSendHandshake();
    }
    if (localSentHandshakeAt == null) {
      logger.info(String.format("Queueing local peer %s to send handshake to remote peer %s",
          new String(localPeer.getId()), new String(remotePeer.getId())));

      messages.add(new Pair<Peer, Message> (remotePeer,
          new HandshakeImpl(metainfo.getInfoHash(), localPeer)));

      return true;
    }
    return false;
  }

  private boolean remoteHasSentHandshake(
      Peer remotePeer, PeerState state, List<Pair<Peer, Message>> messages) {
    Instant remoteSentHandshakeAt = null;
    synchronized(state) {
      remoteSentHandshakeAt = state.whenDidRemoteSendHandshake();
    }

    if (remoteSentHandshakeAt == null) {
      logger.info(String.format("Local peer %s has not received handshake from remote peer %s",
          new String(localPeer.getId()), new String(remotePeer.getId())));

      // shake again just to be sure that the remote got ours
      logger.info(String.format("Queueing local peer %s to send handshake to remote peer %s",
          new String(localPeer.getId()), new String(remotePeer.getId())));

      messages.add(new Pair<Peer, Message> (remotePeer,
          new HandshakeImpl(metainfo.getInfoHash(), localPeer)));

      return false;
    }
    return true;
  }

  private boolean sendInitialChoke(
      Peer remotePeer, PeerState state, List<Pair<Peer, Message>> messages) {
    Pair<ChokeStatus, Instant> choked = null;

    synchronized (state) {
      choked = state.isRemoteChoked();
    }

    if (choked == null) {
      logger.info(String.format("Queueing local peer %s to send choke to remote peer %s",
          new String(localPeer.getId()), new String(remotePeer.getId())));

      messages.add(new Pair<Peer, Message> (remotePeer, new ChokeImpl(localPeer)));

      return true;
    }
    return false;
  }

  private boolean letPeerKnowAboutNewPieces(
      Peer remotePeer, PeerState state, List<Pair<Peer, Message>> messages) {

    Set<Integer> downloadedPieces = null;

    synchronized (data) {
      downloadedPieces = data.keySet();
    }

    if (downloadedPieces == null) {
      return false;
    }

    List<PieceDeclaration> declaredPieces = null;
    synchronized (state) {
      declaredPieces = state.localHasPieces();
    }

    if (declaredPieces == null) {
      return false;
    }

    boolean pieceDeclared = false;

    for (Integer pieceIndex : downloadedPieces) {
      boolean declared = false;
      for (PieceDeclaration piece : declaredPieces) {
        if (pieceIndex.equals(piece.getPieceIndex())) {
          declared = true;
          break;
        }
      }
      if (!declared) {
        pieceDeclared = true;
        messages.add(new Pair<Peer, Message> (remotePeer, new HaveImpl(localPeer, pieceIndex)));
      }
    }

    return pieceDeclared;
  }

  private boolean sendKeepAlive(
    Peer remotePeer, PeerState state, List<Pair<Peer, Message>> messages) {
    messages.add(new Pair<Peer, Message> (remotePeer, new KeepAliveImpl(localPeer)));
    return true;
  }
}
