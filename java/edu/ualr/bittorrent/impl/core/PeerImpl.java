package edu.ualr.bittorrent.impl.core;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.joda.time.Instant;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.impl.core.messages.BitFieldImpl;
import edu.ualr.bittorrent.impl.core.messages.CancelImpl;
import edu.ualr.bittorrent.impl.core.messages.ChokeImpl;
import edu.ualr.bittorrent.impl.core.messages.HandshakeImpl;
import edu.ualr.bittorrent.impl.core.messages.HaveImpl;
import edu.ualr.bittorrent.impl.core.messages.InterestedImpl;
import edu.ualr.bittorrent.impl.core.messages.KeepAliveImpl;
import edu.ualr.bittorrent.impl.core.messages.NotInterestedImpl;
import edu.ualr.bittorrent.impl.core.messages.PieceImpl;
import edu.ualr.bittorrent.impl.core.messages.PortImpl;
import edu.ualr.bittorrent.impl.core.messages.RequestImpl;
import edu.ualr.bittorrent.impl.core.messages.UnchokeImpl;
import edu.ualr.bittorrent.interfaces.Metainfo;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.PeerState;
import edu.ualr.bittorrent.interfaces.Tracker;
import edu.ualr.bittorrent.interfaces.TrackerResponse;
import edu.ualr.bittorrent.interfaces.PeerState.ChokeStatus;
import edu.ualr.bittorrent.interfaces.PeerState.InterestLevel;
import edu.ualr.bittorrent.interfaces.PeerState.PieceDeclaration;
import edu.ualr.bittorrent.interfaces.PeerState.PieceDownload;
import edu.ualr.bittorrent.interfaces.PeerState.PieceRequest;
import edu.ualr.bittorrent.interfaces.PeerState.PieceUpload;
import edu.ualr.bittorrent.interfaces.messages.BitField;
import edu.ualr.bittorrent.interfaces.messages.Cancel;
import edu.ualr.bittorrent.interfaces.messages.Choke;
import edu.ualr.bittorrent.interfaces.messages.Handshake;
import edu.ualr.bittorrent.interfaces.messages.Have;
import edu.ualr.bittorrent.interfaces.messages.Interested;
import edu.ualr.bittorrent.interfaces.messages.KeepAlive;
import edu.ualr.bittorrent.interfaces.messages.NotInterested;
import edu.ualr.bittorrent.interfaces.messages.Piece;
import edu.ualr.bittorrent.interfaces.messages.Port;
import edu.ualr.bittorrent.interfaces.messages.Request;
import edu.ualr.bittorrent.interfaces.messages.Unchoke;

public class PeerImpl implements Peer {
  private Tracker tracker;
  private byte[] id;
  private Metainfo metainfo;
  private static final Logger logger = Logger.getLogger(PeerImpl.class);
  private final List<PeerMessage<?>> messageQueue = Lists.newArrayList();
  private final AtomicInteger downloaded = new AtomicInteger();
  private final AtomicInteger uploaded = new AtomicInteger();
  private final AtomicInteger remaining = new AtomicInteger();
  private final Map<Peer, PeerState> activePeers = new ConcurrentHashMap<Peer, PeerState>();
  private final ConcurrentLinkedQueue<Peer> newlyReportedPeers = new ConcurrentLinkedQueue<Peer>();
  private final Map<Integer, byte[]> data;

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof PeerImpl)) {
      return false;
    }
    PeerImpl peer = (PeerImpl) object;
    return Objects.equal(id, peer.id) && Objects.equal(metainfo, peer.metainfo);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.id, this.metainfo);
  }

  public PeerImpl(byte[] id, Map<Integer, byte[]> initialData) {
    this.id = Preconditions.checkNotNull(id);

    if (initialData == null) {
      this.data = Maps.newHashMap();
    } else {
      this.data = initialData;
    }
  }

  public PeerImpl(Map<Integer, byte[]> initialData) {
    this(UUID.randomUUID().toString().getBytes(), initialData);
  }

  public PeerImpl() {
    this(UUID.randomUUID().toString().getBytes(), null);
  }

  public void setTracker(Tracker tracker) {
    this.tracker = Preconditions.checkNotNull(tracker);
  }

  public void setMetainfo(Metainfo metainfo) {
    this.metainfo = Preconditions.checkNotNull(metainfo);
  }

  /**
   * TrackerTalker
   */
  private class TrackerTalker implements Runnable {
    private final Peer parent;
    private final byte[] infoHash;

    TrackerTalker(Peer parent, byte[] infoHash) {
      this.parent = Preconditions.checkNotNull(parent);
      this.infoHash = Preconditions.checkNotNull(infoHash);
    }

    public void run() {
      while (true) {
        logger.info(String.format("Peer %s contacting tracker", new String(id)));
        TrackerResponse response = tracker.get(
            new TrackerRequestImpl(
              parent,
              infoHash,
              downloaded.get(),
              uploaded.get(),
              remaining.get()
            ));
        logger.info(String.format("Peer %s received response from tracker", new String(id)));
        for (Peer peer : response.getPeers()) {
          newlyReportedPeers.add(peer);
        }
        try {
          Thread.sleep(response.getInterval());
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  /**
   * PeerTalkerManager
   */
  private class PeerTalkerManager implements Runnable {
    private final Peer local;
    private final ExecutorService executor;

    PeerTalkerManager(Peer local, ExecutorService executor) {
      this.local = Preconditions.checkNotNull(local);
      this.executor = Preconditions.checkNotNull(executor);
    }

    public void run() {
      logger.info("Peer talker manager started");
      while (true) {
        for (Peer peer : newlyReportedPeers) {
          // TODO: add culling of dead peers
          if (!activePeers.containsKey(peer)) {
            logger.info(String.format("Local peer %s adding remote peer %s",
                new String(local.getId()),
                new String(peer.getId())));
            PeerState state = new PeerStateImpl();
            activePeers.put(peer, state);
            executor.execute(new PeerTalker(local, peer, state));
          }
        }
        try {
          Thread.sleep(1000L);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  public static class PeerStatusImpl {

  }

  /**
   * PeerTalker
   */
  private class PeerTalker implements Runnable {
    private final Peer local;
    private final Peer remote;
    private final PeerState state;

    PeerTalker(Peer local, Peer remote, PeerState state) {
      this.local = Preconditions.checkNotNull(local);
      this.remote = Preconditions.checkNotNull(remote);
      this.state = Preconditions.checkNotNull(state);
    }

    private void bitfield(byte [] bitfield) {
      logger.info(String.format("Local peer %s sending bitfield message to peer %s",
          new String(local.getId()), new String(remote.getId())));

      Instant now = new Instant();
      for (int i = 0; i < bitfield.length; i++) {
        if (bitfield[i] != 0x0) {
          PeerState.PieceDeclaration declaration = new PeerStateImpl.PieceDeclarationImpl();
          declaration.setDeclarationTime(now);
          declaration.setPieceIndex(i);
          state.setLocalHasPiece(declaration);
          logger.info(String.format(
              "Local peer %s sent bitfield message declaring piece %d to peer %s",
              new String(local.getId()), i, new String(remote.getId())));
        }
      }
      remote.message(new BitFieldImpl(local, bitfield));
    }

    private void cancel(PieceRequest request) {
      logger.info(String.format("Local peer %s sending cancel message to peer %s",
          new String(local.getId()), new String(remote.getId())));
      state.cancelLocalRequestedPiece(request);
      remote.message(new CancelImpl(local, 0, 0, 100));
    }

    private void choke() {
      logger.info(String.format("Local peer %s sending choke message to peer %s",
          new String(local.getId()), new String(remote.getId())));
      state.setRemoteIsChoked(ChokeStatus.CHOKED, new Instant());
      remote.message(new ChokeImpl(local));
    }

    private void handshake() {
      logger.info(String.format("Local peer %s sending handshake message to peer %s",
          new String(local.getId()), new String(remote.getId())));
      state.setRemoteSentHandshakeAt(new Instant());
      remote.message(new HandshakeImpl(local.getId(), local));
    }

    private void have(PieceDeclaration declaration) {
      logger.info(String.format("Local peer %s sending have message to peer %s",
          new String(local.getId()), new String(remote.getId())));
      state.setLocalHasPiece(declaration);
      remote.message(new HaveImpl(local, 0));
    }

    private void interested() {
      logger.info(String.format("Local peer %s sending interested message to peer %s",
          new String(local.getId()), new String(remote.getId())));
      state.setLocalInterestLevelInRemote(InterestLevel.INTERESTED, new Instant());
      remote.message(new InterestedImpl(local));
    }

    private void keepAlive() {
      logger.info(String.format("Local peer %s sending keep alive message to peer %s",
          new String(local.getId()), new String(remote.getId())));
      state.setLocalSentKeepAliveAt(new Instant());
      remote.message(new KeepAliveImpl(local));
    }

    private void notInterested() {
      logger.info(String.format("Local peer %s sending not interested message to peer %s",
          new String(local.getId()), new String(remote.getId())));
      state.setLocalInterestLevelInRemote(InterestLevel.NOT_INTERESTED, new Instant());
      remote.message(new NotInterestedImpl(local));
    }

    private void piece(PieceUpload piece) {
      logger.info(String.format("Local peer %s sending piece message to peer %s",
          new String(local.getId()), new String(remote.getId())));
      state.setLocalSentPiece(piece);
      remote.message(new PieceImpl(local, 0, 0, "TODO".getBytes()));
    }

    private void port(int port) {
      logger.info(String.format("Local peer %s sending port message to peer %s",
          new String(local.getId()), new String(remote.getId())));
      remote.message(new PortImpl(local, port));
    }

    private void request(PieceRequest request) {
      logger.info(String.format("Local peer %s sending request message to peer %s",
          new String(local.getId()), new String(remote.getId())));
      state.setLocalRequestedPiece(request);
      remote.message(new RequestImpl(local, 0, 0, 100));
    }

    private void unchoke() {
      state.setRemoteIsChoked(ChokeStatus.CHOKED, new Instant());
      logger.info(String.format("Local peer %s sending unchoke message to peer %s",
          new String(local.getId()), new String(remote.getId())));
      remote.message(new UnchokeImpl(local));
    }

    private void sendHandshake() {
      handshake();
    }

    public void run() {
      logger.info("Peer talker started");
      sendHandshake();
      while (true) {
        if (state.whenDidRemoteSendHandshake() != null) {
          PieceRequest request = new PeerStateImpl.PieceRequestImpl();
          request.setPieceIndex(0);
          request.setBlockOffset(0);
          request.setBlockSize(100);
          request.setRequestTime(new Instant());

          PieceDeclaration declaration = new PeerStateImpl.PieceDeclarationImpl();
          declaration.setPieceIndex(0);
          declaration.setDeclarationTime(new Instant());

          PieceUpload upload = new PeerStateImpl.PieceUploadImpl();
          upload.setPieceIndex(0);
          upload.setBlockOffset(0);
          upload.setBlockSize(1000);
          upload.setStartTime(new Instant());
          upload.setCompletionTime(upload.getStartTime().plus(1000L));

          PieceDownload download = new PeerStateImpl.PieceDownloadImpl();
          download.setPieceIndex(0);
          download.setBlockOffset(0);
          download.setBlockSize(1000);
          download.setStartTime(new Instant());
          download.setCompletionTime(upload.getStartTime().plus(1000L));

          bitfield("123".getBytes());
          cancel(request);
          choke();
          have(declaration);
          interested();
          keepAlive();
          notInterested();
          piece(upload);
          port(1234);
          request(request);
          unchoke();
        }
        try {
          Thread.sleep(10000);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  public void run() {
    Preconditions.checkNotNull(tracker);
    Preconditions.checkNotNull(id);
    Preconditions.checkNotNull(metainfo);

    logger.info(String.format("Peer %s running", new String(id)));

    ExecutorService executor = Executors.newFixedThreadPool(10);
    executor.execute(new TrackerTalker(this, this.metainfo.getInfoHash()));
    executor.execute(new PeerTalkerManager(this, executor));

    while (true) {
      PeerMessage<?> message = null;
      synchronized (messageQueue) {
        if (messageQueue.size() > 0) {
          message = messageQueue.remove(0);
        }
      }
      if (message != null) {
        processMessage(message);
      }
    }
  }

  private void processBitFieldMessage(BitField bitfield) {
    PeerState state = activePeers.get(bitfield.getPeer());

    logger.info(String.format("Remote peer %s sending bitfield message to local peer %s",
        new String(bitfield.getPeer().getId()), new String(id)));

    Instant now = new Instant();
    for (int i = 0; i < bitfield.getBitField().length; i++) {
      if (bitfield.getBitField()[i] != 0x0) {
        PeerState.PieceDeclaration declaration = new PeerStateImpl.PieceDeclarationImpl();
        declaration.setDeclarationTime(now);
        declaration.setPieceIndex(i);
        state.setRemoteHasPiece(declaration);
        logger.info(String.format(
            "Remote peer %s sent bitfield message declaring piece %d to peer %s",
            new String(bitfield.getPeer().getId()), i, new String(id)));
      }
    }
  }

  private void processCancelMessage(Cancel cancel) {
    PeerState state = activePeers.get(cancel.getPeer());

    PieceRequest request = new PeerStateImpl.PieceRequestImpl();
    request.setPieceIndex(cancel.getPieceIndex());
    request.setBlockOffset(cancel.getBeginningOffset());
    request.setBlockSize(cancel.getBlockLength());
    request.setRequestTime(new Instant());

    state.cancelRemoteRequestedPiece(request);

    logger.info(String.format("Peer %s canceled by peer %s", new String(id),
        new String(cancel.getPeer().getId())));
  }

  private void processChokeMessage(Choke choke) {
    PeerState state = activePeers.get(choke.getPeer());

    state.setLocalIsChoked(ChokeStatus.CHOKED, new Instant());

    logger.info(String.format("Peer %s choked by peer %s", new String(id),
        new String(choke.getPeer().getId())));
  }

  private void processPortMessage(Port port) {
    PeerState state = activePeers.get(port.getPeer());

    state.setRemoteRequestedPort(port.getPort());

    logger.info(String.format("Peer %s received port request from peer %s", new String(id),
        new String(port.getPeer().getId())));
  }

  private void processRequestMessage(Request request) {
    PeerState state = activePeers.get(request.getPeer());

    PieceRequest pieceRequest = new PeerStateImpl.PieceRequestImpl();
    pieceRequest.setPieceIndex(request.getPieceIndex());
    pieceRequest.setBlockOffset(request.getBeginningOffset());
    pieceRequest.setBlockSize(request.getBlockLength());
    pieceRequest.setRequestTime(new Instant());

    state.setRemoteRequestedPiece(pieceRequest);

    logger.info(String.format("Peer %s received request from peer %s", new String(id),
        new String(request.getPeer().getId())));
  }

  private void processHandshakeMessage(Handshake handshake) {
    PeerState state = activePeers.get(handshake.getPeer());

    state.setRemoteSentHandshakeAt(new Instant());

    logger.info(String.format("Peer %s received handshake from peer %s", new String(id),
        new String(handshake.getPeer().getId())));
  }

  private void processHaveMessage(Have have) {
    PeerState state = activePeers.get(have.getPeer());

    PieceDeclaration declaration = new PeerStateImpl.PieceDeclarationImpl();
    declaration.setPieceIndex(0);
    declaration.setDeclarationTime(new Instant());

    state.setRemoteHasPiece(declaration);

    logger.info(String.format("Peer %s received have message from peer %s", new String(id),
        new String(have.getPeer().getId())));
  }

  private void processInterestedMessage(Interested interested) {
    PeerState state = activePeers.get(interested.getPeer());

    state.setRemoteInterestLevelInLocal(InterestLevel.INTERESTED, new Instant());

    logger.info(String.format(
        "Peer %s received interested message from peer %s", new String(id),
        new String(interested.getPeer().getId())));
  }

  private void processKeepAliveMessage(KeepAlive keepAlive) {
    PeerState state = activePeers.get(keepAlive.getPeer());

    state.setRemoteSentKeepAliveAt(new Instant());

    logger.info(String.format("Peer %s received keep alive from peer %s", new String(id),
        new String(keepAlive.getPeer().getId())));
  }

  private void processNotInterestedMessage(NotInterested notInterested) {
    PeerState state = activePeers.get(notInterested.getPeer());

    state.setRemoteInterestLevelInLocal(InterestLevel.NOT_INTERESTED, new Instant());

    logger.info(String.format("Peer %s received not interested from peer %s", new String(id),
        new String(notInterested.getPeer().getId())));
  }

  private void processPieceMessage(Piece piece) {
    PeerState state = activePeers.get(piece.getPeer());

    PieceDownload download = new PeerStateImpl.PieceDownloadImpl();
    download.setPieceIndex(piece.getPieceIndex());
    download.setBlockOffset(piece.getBeginningOffset());
    download.setBlockSize(piece.getBlock().length);
    download.setStartTime(new Instant());
    download.setCompletionTime(new Instant());

    state.setRemoteSentPiece(download);

    logger.info(String.format("Peer %s received piece from peer %s", new String(id),
        new String(piece.getPeer().getId())));
  }

  private void processUnchokeMessage(Unchoke unchoke) {
    PeerState state = activePeers.get(unchoke.getPeer());

    state.setLocalIsChoked(ChokeStatus.CHOKED, new Instant());

    logger.info(String.format("Peer %s unchoked by peer %s", new String(id),
        new String(unchoke.getPeer().getId())));
  }

  private void processMessage(PeerMessage<?> message) {
    if (message instanceof BitField) {
      processBitFieldMessage((BitField) message);
    } else if (message instanceof Cancel) {
      processCancelMessage((Cancel) message);
    } else if (message instanceof Choke) {
      processChokeMessage((Choke) message);
    } else if (message instanceof Port) {
      processPortMessage((Port) message);
    } else if (message instanceof Request) {
      processRequestMessage((Request) message);
    } else if (message instanceof Handshake) {
      processHandshakeMessage((Handshake) message);
    } else if (message instanceof Have) {
      processHaveMessage((Have) message);
    } else if (message instanceof Interested) {
      processInterestedMessage((Interested) message);
    } else if (message instanceof KeepAlive) {
      processKeepAliveMessage((KeepAlive) message);
    } else if (message instanceof NotInterested) {
      processNotInterestedMessage((NotInterested) message);
    } else if (message instanceof Piece) {
      processPieceMessage((Piece) message);
    } else if (message instanceof Unchoke) {
      processUnchokeMessage((Unchoke) message);
    } else {
      throw new IllegalArgumentException(String.format(
          "Peer %s sent an unsupported message of type %s", new String(message.getPeer().getId()),
          message.getType()));
    }
  }

  public void setId(byte[] id) {
    this.id = Preconditions.checkNotNull(id);
  }

  public byte[] getId() {
    return id;
  }

  public void message(PeerMessage<?> message) {
    synchronized (messageQueue) {
      messageQueue.add(message);
    }
  }
}
