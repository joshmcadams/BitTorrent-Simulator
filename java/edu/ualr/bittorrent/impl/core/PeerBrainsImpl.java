package edu.ualr.bittorrent.impl.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.Instant;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.tools.javac.util.Pair;

import edu.ualr.bittorrent.impl.core.messages.HandshakeImpl;
import edu.ualr.bittorrent.impl.core.messages.MessagesModule;
import edu.ualr.bittorrent.interfaces.Message;
import edu.ualr.bittorrent.interfaces.Metainfo;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.PeerBrains;
import edu.ualr.bittorrent.interfaces.PeerState;
import edu.ualr.bittorrent.interfaces.PeerState.ChokeStatus;
import edu.ualr.bittorrent.interfaces.PeerState.InterestLevel;
import edu.ualr.bittorrent.interfaces.PeerState.PieceDeclaration;
import edu.ualr.bittorrent.interfaces.PeerState.PieceRequest;
import edu.ualr.bittorrent.interfaces.messages.CancelFactory;
import edu.ualr.bittorrent.interfaces.messages.ChokeFactory;
import edu.ualr.bittorrent.interfaces.messages.HandshakeFactory;
import edu.ualr.bittorrent.interfaces.messages.HaveFactory;
import edu.ualr.bittorrent.interfaces.messages.InterestedFactory;
import edu.ualr.bittorrent.interfaces.messages.KeepAliveFactory;
import edu.ualr.bittorrent.interfaces.messages.NotInterestedFactory;
import edu.ualr.bittorrent.interfaces.messages.PieceFactory;
import edu.ualr.bittorrent.interfaces.messages.RequestFactory;
import edu.ualr.bittorrent.interfaces.messages.UnchokeFactory;

/**
 * Default implementation of the decision making portion of a peer, the
 * {@link PeerBrains}.
 */
public class PeerBrainsImpl implements PeerBrains {
  private Map<Peer, PeerState> activePeers;
  private Peer localPeer;
  private Metainfo metainfo;
  private Map<Integer, byte[]> data;
  private static final Logger logger = Logger.getLogger(PeerBrainsImpl.class);
  private static final Integer UNCHOKED_PEER_LIMIT = 100;
  private final Injector injector;

  public PeerBrainsImpl() {
    this.injector = Guice.createInjector(new MessagesModule());
  }

  /**
   * {@inheritDoc}
   */
  public void setLocalPeer(Peer local) {
    this.localPeer = Preconditions.checkNotNull(local);
  }

  /**
   * {@inheritDoc}
   */
  public void setActivePeers(Map<Peer, PeerState> activePeers) {
    this.activePeers = Preconditions.checkNotNull(activePeers);
  }

  /**
   * {@inheritDoc}
   */
  public void setMetainfo(Metainfo metainfo) {
    this.metainfo = Preconditions.checkNotNull(metainfo);
  }

  /**
   * {@inheritDoc}
   */
  public void setData(Map<Integer, byte[]> data) {
    this.data = Preconditions.checkNotNull(data);
  }

  private PeerState returnStateIfAvailable(Peer p) {
    if (p.equals(localPeer)) {
      return null;
    }

    PeerState state = null;
    synchronized (activePeers) {
      if (activePeers.containsKey(p)) {
        state = activePeers.get(p);
      }
    }

    return state;
  }

  private boolean handleCommunicationInitalization(Peer p, PeerState state,
      List<Pair<Peer, Message>> messages) {
    /* If the local client hasn't sent a handshake to this peer yet, send one */
    if (sendHandshake(p, state, messages)) {
      return true;
    }

    /* If the remote peer hasn't sent a handshake yet, send them another */
    if (!remoteHasSentHandshake(p, state, messages)) {
      return true;
    }

    /*
     * If we haven't started communicating with this peer yet, then the choke
     * status will be null. If this is the case, go ahead and send a choke
     * message to make the initial choke state official.
     */
    if (sendInitialChoke(p, state, messages)) {
      return true;
    }

    return false;
  }

  /**
   * {@inheritDoc}
   */
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
      PeerState state = returnStateIfAvailable(p);
      if (state == null) {
        continue;
      }

      if(handleCommunicationInitalization(p, state, messages)) {
        continue;
      }

      /* Let the remote peer know about any new pieces that we might have */
      letPeerKnowAboutNewPieces(p, state, messages);

      /*
       * Let peers that are choking and that have peices that we want know that
       * we are interested
       */
      expressInterest(p, state, messages);

      /* Unchoke some peers if there are any that seem worthy */
      unchoke(p, state, messages);

      /* request pieces from peers */
      makeRequests(p, state, messages);

      sendPieces(p, state, messages);

      cancelPieceRequests(p, state, messages);

      /* If no other messages are necessary, just send a keep alive */
      sendKeepAlive(p, state, messages);
    }

    return messages;
  }

  /**
   * Determine if a handshake message should be sent and if so, add it to the
   * message queue.
   *
   * @param remotePeer
   * @param state
   * @param messages
   * @return
   */
  private boolean sendHandshake(Peer remotePeer, PeerState state,
      List<Pair<Peer, Message>> messages) {
    Instant localSentHandshakeAt = null;
    synchronized (state) {
      localSentHandshakeAt = state.whenDidLocalSendHandshake();
    }
    if (localSentHandshakeAt == null) {
      logger.info(String.format(
          "Queueing local peer %s to send handshake to remote peer %s",
          new String(localPeer.getId()), new String(remotePeer.getId())));

      messages.add(new Pair<Peer, Message>(remotePeer,
          injector.getInstance(HandshakeFactory.class).create(localPeer,
              HandshakeImpl.DEFAULT_PROTOCOL_IDENTIFIER,
              metainfo.getInfoHash(), HandshakeImpl.DEFAULT_RESERVED_BYTES)));

      return true;
    }
    return false;
  }

  /**
   * Determine if the remote has sent a handshake, if not, trying sending
   * another to the remote.
   *
   * @param remotePeer
   * @param state
   * @param messages
   * @return
   */
  private boolean remoteHasSentHandshake(Peer remotePeer, PeerState state,
      List<Pair<Peer, Message>> messages) {
    Instant remoteSentHandshakeAt = null;
    synchronized (state) {
      remoteSentHandshakeAt = state.whenDidRemoteSendHandshake();
    }

    if (remoteSentHandshakeAt == null) {
      logger.info(String.format(
          "Local peer %s has not received handshake from remote peer %s",
          new String(localPeer.getId()), new String(remotePeer.getId())));

      // shake again just to be sure that the remote got ours
      logger.info(String.format(
          "Queueing local peer %s to send handshake to remote peer %s",
          new String(localPeer.getId()), new String(remotePeer.getId())));

      messages.add(new Pair<Peer, Message>(remotePeer,
          injector.getInstance(HandshakeFactory.class).create(localPeer,
              HandshakeImpl.DEFAULT_PROTOCOL_IDENTIFIER, metainfo.getInfoHash(),
              HandshakeImpl.DEFAULT_RESERVED_BYTES)));

      return false;
    }
    return true;
  }

  /**
   * Initially the remote should be choked according to the specification.
   *
   * @param remotePeer
   * @param state
   * @param messages
   * @return
   */
  private boolean sendInitialChoke(Peer remotePeer, PeerState state,
      List<Pair<Peer, Message>> messages) {
    Pair<ChokeStatus, Instant> choked = null;

    synchronized (state) {
      choked = state.isRemoteChoked();
    }

    if (choked == null) {
      logger.info(String.format(
          "Queueing local peer %s to send choke to remote peer %s", new String(
              localPeer.getId()), new String(remotePeer.getId())));

      messages.add(new Pair<Peer, Message>(remotePeer, injector.getInstance(
          ChokeFactory.class).create(localPeer)));

      return true;
    }
    return false;
  }

  /**
   * As the local peer collects pieces, it should let its neighbors know.
   *
   * @param remotePeer
   * @param state
   * @param messages
   * @return
   */
  private boolean letPeerKnowAboutNewPieces(Peer remotePeer, PeerState state,
      List<Pair<Peer, Message>> messages) {

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
        messages.add(new Pair<Peer, Message>(remotePeer,
            injector.getInstance(HaveFactory.class).create(localPeer, pieceIndex)));
      }
    }

    return pieceDeclared;
  }

  /**
   * If a neighbor has a piece that we are interested in and if we are choked,
   * then we should let the neighbor know that we'd like to request a piece of
   * data from it, hoping that we will soon be unchoked.
   *
   * @param remotePeer
   * @param state
   * @param messages
   * @return
   */
  private boolean expressInterest(Peer remotePeer, PeerState state,
      List<Pair<Peer, Message>> messages) {

    Set<Integer> downloadedPieces = null;

    synchronized (data) {
      downloadedPieces = data.keySet();
    }

    List<PieceDeclaration> remotePieces = null;
    Pair<ChokeStatus, Instant> choked = null;

    synchronized (state) {
      choked = state.isLocalChoked();
      remotePieces = state.remoteHasPieces();
    }

    if (choked != null && ChokeStatus.UNCHOKED.equals(choked.fst)) {
      return false; // if we are already unchoked, there is no reason to express
      // interest
    }

    if (remotePieces != null) {
      for (PieceDeclaration pieceDeclaration : remotePieces) {
        if (!downloadedPieces.contains(pieceDeclaration.getPieceIndex())) {
          messages.add(new Pair<Peer, Message>(remotePeer,
              injector.getInstance(InterestedFactory.class).create(localPeer)));
          return true;
        }
      }
    }

    messages.add(new Pair<Peer, Message>(remotePeer,
        injector.getInstance(NotInterestedFactory.class).create(localPeer)));

    return false;
  }

  /**
   * Unchoke peers that are interested.
   *
   * @param remotePeer
   * @param state
   * @param messages
   * @return
   */
  private boolean unchoke(Peer remotePeer, PeerState state,
      List<Pair<Peer, Message>> messages) {

    Pair<ChokeStatus, Instant> choked = null;
    Pair<InterestLevel, Instant> interest = null;

    synchronized (state) {
      choked = state.isRemoteChoked();
      interest = state.getRemoteInterestLevelInLocal();
    }

    if (interest == null || InterestLevel.NOT_INTERESTED.equals(interest.fst)) {
      return false; // the peer has no desire to be unchoked
    }

    if (choked != null && ChokeStatus.UNCHOKED.equals(choked.fst)) {
      return false; // already unchoked
    }

    Set<Peer> peers = null;

    synchronized (activePeers) {
      peers = activePeers.keySet();
    }

    int unchokedPeerCount = 0;
    for (Peer peer : peers) {
      PeerState peerState = null;
      synchronized (activePeers) {
        peerState = activePeers.get(peer);
      }
      if (peerState.isRemoteChoked() != null
          && ChokeStatus.UNCHOKED.equals(peerState.isRemoteChoked().fst)) {
        unchokedPeerCount++;
      }
    }

    logger.info(String.format("Unchoked peer count is %d", unchokedPeerCount));
    if (unchokedPeerCount < UNCHOKED_PEER_LIMIT) {
      messages.add(new Pair<Peer, Message>(remotePeer,
          injector.getInstance(UnchokeFactory.class).create(localPeer)));
    }

    return true;
  }

  /**
   * Make data requests to peers.
   *
   * @param remotePeer
   * @param state
   * @param messages
   * @return
   */
  private boolean makeRequests(Peer remotePeer, PeerState state,
      List<Pair<Peer, Message>> messages) {

    Pair<ChokeStatus, Instant> choked = null;
    List<PieceDeclaration> remotePieces = null;

    synchronized (state) {
      choked = state.isLocalChoked();
      remotePieces = state.remoteHasPieces();
    }

    if (choked == null || ChokeStatus.CHOKED.equals(choked.fst)) {
      return false; // bummer, choked
    }

    Set<Integer> downloadedPieces = null;

    synchronized (data) {
      downloadedPieces = data.keySet();
    }

    logger.info(String.format("Remote piece count for remote peer %s is %d",
        new String(remotePeer.getId()), remotePieces.size()));

    ImmutableList<PieceRequest> alreadyRequestedPieces = null;
    synchronized (state) {
      alreadyRequestedPieces = state.getLocalRequestedPieces();
    }

    logger.info(String.format("%s has downloaded %d pieces", new String(
        localPeer.getId()), downloadedPieces.size()));

    for (Integer i : downloadedPieces) {
      logger.info(String.format("%s has downloaded piece %d", new String(
          localPeer.getId()), i));
    }

    for (PieceDeclaration piece : remotePieces) {
      if (!downloadedPieces.contains(piece.getPieceIndex())) {
        boolean okayToRequest = true;
        if (alreadyRequestedPieces != null) {
          for (PieceRequest request : alreadyRequestedPieces) {
            if (request.getPieceIndex().equals(piece.getPieceIndex())
                && request.getRequestTime().isAfter(
                    new Instant().minus(100000L))) {
              okayToRequest = false;
              break;
            }
          }
        }

        if (okayToRequest) {
          messages.add(new Pair<Peer, Message>(remotePeer,
              injector.getInstance(RequestFactory.class).create(
              localPeer, piece.getPieceIndex(), 0, metainfo.getPieceLength())));
        }

        return true;
      } else {
        logger.info(String.format("Peer %s already has piece %d", new String(
            localPeer.getId()), piece.getPieceIndex()));
      }
    }

    return false;
  }

  /**
   * Send pieces to peers.
   *
   * @param remotePeer
   * @param state
   * @param messages
   * @return
   */
  private boolean sendPieces(Peer remotePeer, PeerState state,
      List<Pair<Peer, Message>> messages) {

    Pair<ChokeStatus, Instant> choked = null;
    List<PieceRequest> requestedPieces = null;

    synchronized (state) {
      choked = state.isRemoteChoked();
      requestedPieces = state.getRemoteRequestedPieces();
    }

    logger.info(String.format(
        "Remote peer %s has requested %d pieces from local peer %s",
        new String(remotePeer.getId()), requestedPieces.size(), new String(
            localPeer.getId())));

    if (requestedPieces == null || requestedPieces.size() == 0) {
      return false;
    }

    if (choked == null || ChokeStatus.CHOKED.equals(choked.fst)) {
      return false; // remote is choked
    }

    for (PieceRequest request : requestedPieces) {
      logger.info(String.format(
          "Remote peer %s has requested piece %s of local peer %s", new String(
              remotePeer.getId()), request, new String(localPeer.getId())));
    }

    byte[] bytes = null;
    int requestedIndex = requestedPieces.get(0).getPieceIndex();
    int requestedOffset = requestedPieces.get(0).getBlockOffset();

    logger
        .info(String.format(
            "Local peer %s is pulling piece %d for remote peer %s", new String(
                localPeer.getId()), requestedIndex, new String(remotePeer
                .getId())));

    synchronized (data) {
      if (data.containsKey(requestedIndex)) {
        bytes = data.get(requestedIndex);
      }
    }

    if (bytes == null) {
      logger.info(String.format(
          "Local peer %s could not find data for piece %d for remote peer %s",
          new String(localPeer.getId()), requestedIndex, new String(remotePeer
              .getId())));
      return false;
    }

    // TODO: make the bytes match the requested size

    logger.info(String.format(
        "Local peer %s is sending data for piece %d for remote peer %s",
        new String(localPeer.getId()), requestedIndex, new String(remotePeer
            .getId())));

    messages.add(new Pair<Peer, Message>(remotePeer,
        injector.getInstance(PieceFactory.class).create(localPeer,
        requestedIndex, requestedOffset, bytes)));

    return true;
  }

  /**
   * Cancel any requests that have been made, but fulfilled by other peers.
   *
   * @param remotePeer
   * @param state
   * @param messages
   * @return
   */
  private boolean cancelPieceRequests(Peer remotePeer, PeerState state,
      List<Pair<Peer, Message>> messages) {

    Set<Integer> downloadedPieces;
    ImmutableList<PieceRequest> requestedPieces;

    synchronized (data) {
      downloadedPieces = data.keySet();
    }

    synchronized (state) {
      requestedPieces = state.getLocalRequestedPieces();
    }

    for (PieceRequest request : requestedPieces) {
      if (downloadedPieces.contains(request.getPieceIndex())) {
        messages.add(new Pair<Peer, Message>(remotePeer,
            injector.getInstance(CancelFactory.class).create(
            localPeer, request.getPieceIndex(), request.getBlockOffset(),
            request.getBlockSize())));
      }
    }

    return true;
  }

  /**
   * Let the peer know we are here.
   *
   * @param remotePeer
   * @param state
   * @param messages
   * @return
   */
  private boolean sendKeepAlive(Peer remotePeer, PeerState state,
      List<Pair<Peer, Message>> messages) {
    messages.add(new Pair<Peer, Message>(remotePeer,
        injector.getInstance(KeepAliveFactory.class).create(localPeer)));
    return true;
  }
}
