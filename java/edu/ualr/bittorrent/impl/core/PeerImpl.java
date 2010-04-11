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
import com.sun.tools.javac.util.Pair;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Message;
import edu.ualr.bittorrent.interfaces.Metainfo;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.PeerBrains;
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
  private final byte[] id;
  private Metainfo metainfo;
  private final PeerBrains brains;
  private static final Logger logger = Logger.getLogger(PeerImpl.class);
  private final List<Message> messageQueue = Lists.newArrayList();
  private final AtomicInteger downloaded = new AtomicInteger();
  private final AtomicInteger uploaded = new AtomicInteger();
  private final AtomicInteger remaining = new AtomicInteger();
  private final Map<Peer, PeerState> activePeers = new ConcurrentHashMap<Peer, PeerState>();
  private final ConcurrentLinkedQueue<Peer> newlyReportedPeers = new ConcurrentLinkedQueue<Peer>();
  private final Map<Integer, byte[]> data;

  /**
   * Create a new PeerImpl object, providing a unique ID, the brains of the peer, and some initial
   * data related to the torrent.
   *
   * @param id
   * @param brains
   * @param initialData
   */
  public PeerImpl(byte[] id, PeerBrains brains, Map<Integer, byte[]> initialData) {
    this.id = Preconditions.checkNotNull(id);
    this.brains = Preconditions.checkNotNull(brains);

    if (initialData == null) {
      this.data = Maps.newHashMap();
    } else {
      this.data = initialData;
    }
  }

  /**
   * Create a new PeerImpl object, providing the brains of the peer and some initial data related to
   * the torrent.
   *
   * @param brains
   * @param initialData
   */
  public PeerImpl(PeerBrains brains, Map<Integer, byte[]> initialData) {
    this(UUID.randomUUID().toString().getBytes(), brains, initialData);
  }

  /**
   * Create a new PeerImpl object, providing some initial data. The default {@link PeerBrainsImpl}
   * will be used to control decision making by the peer.
   *
   * @param initialData
   */
  public PeerImpl(Map<Integer, byte[]> initialData) {
    this(UUID.randomUUID().toString().getBytes(), new PeerBrainsImpl(), initialData);
  }

  /**
   * Create a new leeching PeerImpl object accepting the default {@link PeerBrainsImpl} for peer
   * decision making.
   */
  public PeerImpl() {
    this(UUID.randomUUID().toString().getBytes(), new PeerBrainsImpl(), null);
  }

  /**
   * {@inheritDoc}
   */
  public void setTracker(Tracker tracker) {
    this.tracker = Preconditions.checkNotNull(tracker);
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
  public byte[] getId() {
    return id;
  }

  /**
   * {@inheritDoc}
   */
  public void message(Message message) {
    synchronized (messageQueue) {
      messageQueue.add(message);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object object) {
    if (!(object instanceof PeerImpl)) {
      return false;
    }
    PeerImpl peer = (PeerImpl) object;
    return Objects.equal(id, peer.id) && Objects.equal(metainfo, peer.metainfo);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return Objects.hashCode(this.id, this.metainfo);
  }

  /**
   * The PeerImpl is intended to run as a thread of a higher-level {@link ExecutorService}. The
   * peer itself creates a new {@link ExecutorService} that it uses to spawn threads for parallel
   * communication with the tracker and with other peers in the swarm.
   *
   * This thread loops infinitely, pulling messages off of a message queue. The message queue is
   * populated with messages received from other peers in the swarm. The messages are processed in
   * the sense that they are used to populate a state object. This object is used by the
   * {@link PeerBrains} to make decisions about how this peer should act.
   */
  public void run() {
    Preconditions.checkNotNull(tracker);
    Preconditions.checkNotNull(id);
    Preconditions.checkNotNull(metainfo);

    brains.setActivePeers(activePeers);
    brains.setLocalPeer(this);
    brains.setMetainfo(metainfo);
    brains.setData(data);

    logger.info(String.format("Peer %s running", new String(id)));

    ExecutorService executor = Executors.newFixedThreadPool(2);

    // line of communication with the tracker
    executor.execute(new TrackerTalker(this, this.metainfo.getInfoHash()));

    // outbound peer communication
    executor.execute(new PeerTalkerManager(this));

    int piecesToDownload = metainfo.getPieces().size();
    int piecesDownloaded = 0;
    // inbound peer communication
    while (true) {
      Message message = null;
      synchronized (messageQueue) {
        if (messageQueue.size() > 0) {
          logger.info(String.format("Local peer %s message queue size: %d",
              new String(this.getId()), messageQueue.size()));
          message = messageQueue.remove(0);
        }
      }
      if (message != null) {
        processMessage(message);
      }

      int newPiecesDownloaded = 0;
      synchronized (data) {
        newPiecesDownloaded = data.size();
      }
      if (newPiecesDownloaded != piecesDownloaded) {
        piecesDownloaded = newPiecesDownloaded;
        logger.info(String.format("Peer %s has downloaded %d of %d pieces", new String(id),
            piecesDownloaded, piecesToDownload));
      }
    }
  }

  /**
   * A {@link BitField} message is really just the remote peer letting the local peer know all of
   * the pieces that it has via a single message instead of via multiple {@link Have} messages. This
   * method parses the {@link BitField} and stores a {@link PieceDeclaration} for each piece that
   * the remote claims to possess.
   *
   * @param bitfield
   */
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

  /**
   * After a remote peer requests a piece, it can later cancel that request by sending a cancel
   * message. This method processes the given cancel message and notes the cancel in the peer state.
   *
   * @param cancel
   */
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

  /**
   * When choked by a remote peer, the local client should send any requests. This method notes the
   * choke in shared state.
   *
   * @param choke
   */
  private void processChokeMessage(Choke choke) {
    PeerState state = getStateForPeer(choke.getPeer());

    synchronized (state) {
      state.setLocalIsChoked(ChokeStatus.CHOKED, new Instant());
    }

    logger.info(String.format("Local peer %s received choke from remote peer %s",
        new String(id), new String(choke.getPeer().getId())));
  }

  /**
   * In the real-world of BitTorrent, a remote peer might request communication on a specific port.
   * This method takes note of the port request; however, since this simulator doesn't make actual
   * network calls, the port operation does little more than update state.
   *
   * @param port
   */
  private void processPortMessage(Port port) {
    PeerState state = getStateForPeer(port.getPeer());

    synchronized (state) {
      state.setRemoteRequestedPort(port.getPort());
    }

    logger.info(String.format("Local peer %s received port from remote peer %s",
        new String(id), new String(port.getPeer().getId())));
  }

  /**
   * A request message is sent by the remote peer to request a specific piece from the local client.
   * This method takes note of the specific request.
   *
   * @param request
   */
  private void processRequestMessage(Request request) {
    PeerState state = activePeers.get(request.getPeer());

    PieceRequest pieceRequest = new PeerStateImpl.PieceRequestImpl();
    pieceRequest.setPieceIndex(request.getPieceIndex());
    pieceRequest.setBlockOffset(request.getBeginningOffset());
    pieceRequest.setBlockSize(request.getBlockLength());
    pieceRequest.setRequestTime(new Instant());

    synchronized (state) {
      state.setRemoteRequestedPiece(pieceRequest);
    }

    logger.info(String.format("Peer %s received request from peer %s", new String(id),
        new String(request.getPeer().getId())));
  }

  /**
   * A handshake message is sent between peers to initiate communication. This method notes that
   * the remote peer sent a handshake.
   *
   * @param handshake
   */
  private void processHandshakeMessage(Handshake handshake) {
    PeerState state = getStateForPeer(handshake.getPeer());

    synchronized (state) {
      state.setRemoteSentHandshakeAt(new Instant());
    }

    logger.info(String.format("Local peer %s received handshake from remote peer %s",
        new String(id), new String(handshake.getPeer().getId())));
  }

  /**
   * As a peer collects pieces, it announces the newly collected pieces to its peers. This method
   * processes a piece announcement from a remote peer.
   *
   * @param have
   */
  private void processHaveMessage(Have have) {
    PeerState state = getStateForPeer(have.getPeer());

    PieceDeclaration declaration = new PeerStateImpl.PieceDeclarationImpl();
    declaration.setPieceIndex(have.getPieceIndex());
    declaration.setDeclarationTime(new Instant());

    synchronized (state) {
      state.setRemoteHasPiece(declaration);
    }

    logger.info(String.format("Peer %s received have from peer %s", new String(id),
        new String(have.getPeer().getId())));
  }

  /**
   * When a remote peer, probably a choked one, is interested in a piece that the local client has,
   * it sends an {@link Interested} message to let the local client know that the remote is
   * interested in what the local client has to offer. This method notes that interest.
   *
   * @param interested
   */
  private void processInterestedMessage(Interested interested) {
    PeerState state = getStateForPeer(interested.getPeer());

    synchronized (state) {
      state.setRemoteInterestLevelInLocal(InterestLevel.INTERESTED, new Instant());
    }

    logger.info(String.format("Local peer %s received interested from remote peer %s",
        new String(id), new String(interested.getPeer().getId())));
  }

  /**
   * Periodically, clients should send a {@link KeepAlive} message to peers in which they are
   * connected, just to let the peers know that the client is here and communicating... it just
   * might not have had anything interesting to say for a while.
   *
   * @param keepAlive
   */
  private void processKeepAliveMessage(KeepAlive keepAlive) {
    PeerState state = getStateForPeer(keepAlive.getPeer());

    synchronized (state) {
      state.setRemoteSentKeepAliveAt(new Instant());
    }

    logger.info(String.format("Peer %s received keep-alive from peer %s", new String(id),
        new String(keepAlive.getPeer().getId())));
  }

  /**
   * When a peer isn't interested in anything that the local client has to offer, it can tell them
   * so using the {@link NotInterested} message. This lets the local client know that there really
   * isn't any reason to unchoke the remote peer.
   *
   * @param notInterested
   */
  private void processNotInterestedMessage(NotInterested notInterested) {
    PeerState state = getStateForPeer(notInterested.getPeer());

    synchronized (state) {
      state.setRemoteInterestLevelInLocal(InterestLevel.NOT_INTERESTED, new Instant());
    }

    logger.info(String.format("Local peer %s received not-interested from remote peer %s",
        new String(id), new String(notInterested.getPeer().getId())));
  }

  /**
   * When a remote peer sends a piece of data to the local client, it does so via a {@link Piece}
   * message. This method collects that piece.
   *
   * @param piece
   */
  private void processPieceMessage(Piece piece) {
    PeerState state = activePeers.get(piece.getPeer());

    PieceDownload download = new PeerStateImpl.PieceDownloadImpl();
    download.setPieceIndex(piece.getPieceIndex());
    download.setBlockOffset(piece.getBeginningOffset());
    download.setBlockSize(piece.getBlock().length);
    download.setStartTime(new Instant()); // TODO: pull the original number from Message
    download.setCompletionTime(new Instant());

    synchronized (state) {
      state.setRemoteSentPiece(download);
    }

    synchronized (data) {
      data.put(piece.getPieceIndex(), piece.getBlock());

      for (Integer key : data.keySet()) {
        logger.info(String.format("Peer %s now has piece %d", new String(this.getId()), key));
      }
    }

    logger.info(String.format("Peer %s received piece %d from peer %s", new String(id),
        piece.getPieceIndex(), new String(piece.getPeer().getId())));
  }

  /**
   * A remote peer sends an {@link Unchoke} message to the local client to let the local client
   * know that it is okay to start sending requests for data. This method notes the unchoked state.
   *
   * @param unchoke
   */
  private void processUnchokeMessage(Unchoke unchoke) {
    PeerState state = getStateForPeer(unchoke.getPeer());

    synchronized (state) {
      state.setLocalIsChoked(ChokeStatus.UNCHOKED, new Instant());
    }

    logger.info(String.format("Local peer %s received unchoke from remote peer %s",
        new String(id), new String(unchoke.getPeer().getId())));
  }

  /**
   * Messages are received as somewhat generic {@link PeerMessage} objects. This method determines
   * the type of {@link PeerMessage} and dispatches the message to the appropriate handler.
   *
   * @param message
   */
  private void processMessage(Message message) {
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

  /**
   * The TrackerTalker object is used by {@link PeerImpl} to keep a constant thread of communication
   * open with the given {@link Tracker}. The allows for the {@link PeerImpl} to keep the
   * {@link Tracker} updated with the status of the {@link PeerImpl} and allows for the
   * {@link PeerImpl} to keep a fresh list of {@link Peer}s in the swarm.
   */
  private class TrackerTalker implements Runnable {
    private final Peer parent;
    private final byte[] infoHash;

    /**
     * Creates a new {@link TrackerTalker} that can be used to keep communication flowing between
     * the given client and a {@link Tracker}.
     *
     * @param parent
     * @param infoHash
     */
    TrackerTalker(Peer parent, byte[] infoHash) {
      this.parent = Preconditions.checkNotNull(parent);
      this.infoHash = Preconditions.checkNotNull(infoHash);
    }

    /**
     * Thread of execution that builds a request to the {@link Tracker}, sends the request, waits
     * for the response, and then pulls the list of {@link Peers} reported by the {@link Tracker}
     * from the response.
     */
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
          if (!parent.equals(peer)) {
            newlyReportedPeers.add(peer);
          }
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
   * TODO: decide if this object should hang around or if simply having PeerBrains is enough.
   */
  private class PeerTalkerManager implements Runnable {
    private final Peer local;

    PeerTalkerManager(Peer local) {
      this.local = Preconditions.checkNotNull(local);
    }

    public void run() {
      logger.info("Peer talker manager started");

      while (true) {
        // add new peers that have been provided to us by the tracker
        for (Peer peer : newlyReportedPeers) {
          if (!activePeers.containsKey(peer)) {
            logger.info(String.format("Local peer %s adding remote peer %s",
                new String(local.getId()),
                new String(peer.getId())));
            synchronized (activePeers) {
              activePeers.put(peer, new PeerStateImpl());
            }
          }
        }

        // let our brain determining what messages we should send, then loop through the messages,
        // logging and dispatching each one
        for (Pair<Peer, Message> peerAndMessage : brains.getMessagesToDispatch()) {
          sendMessage(peerAndMessage.fst, peerAndMessage.snd);
        }

        try {
          Thread.sleep(1000L);
        } catch (InterruptedException e) {
          /* chomp */
        }
      }
    }
  }

  private void sendMessage(Peer remotePeer, Message message) {
    if (message instanceof BitField) {
    //TODO: processBitFieldMessage((BitField) message);
    } else if (message instanceof Cancel) {
    //TODO: processCancelMessage((Cancel) message);
    } else if (message instanceof Choke) {
      sendChokeMessage(remotePeer, (Choke) message);
    } else if (message instanceof Port) {
      sendPortMessage(remotePeer, (Port) message);
    } else if (message instanceof Request) {
      sendRequestMessage(remotePeer, (Request) message);
    } else if (message instanceof Handshake) {
      sendHandshakeMessage(remotePeer, (Handshake) message);
    } else if (message instanceof Have) {
      sendHaveMessage(remotePeer, (Have) message);
    } else if (message instanceof Interested) {
      sendInterestedMessage(remotePeer, (Interested) message);
    } else if (message instanceof KeepAlive) {
      sendKeepAliveMessage(remotePeer, (KeepAlive) message);
    } else if (message instanceof NotInterested) {
      sendNotInterestedMessage(remotePeer, (NotInterested) message);
    } else if (message instanceof Piece) {
      sendPieceMessage(remotePeer, (Piece) message);
    } else if (message instanceof Unchoke) {
      sendUnchokeMessage(remotePeer, (Unchoke) message);
    } else {
      throw new IllegalArgumentException(String.format(
          "Local client %s attempting to send an unsupported message of type %s",
          new String(getId()),
          message.getType()));
    }
  }

  /*
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
  */

    private void sendChokeMessage(Peer remotePeer, Choke choke) {
      logger.info(String.format("Local peer %s sending choke message to peer %s",
          new String(getId()), new String(remotePeer.getId())));

      PeerState state = getStateForPeer(remotePeer);

      synchronized(state) {
        state.setRemoteIsChoked(ChokeStatus.CHOKED, new Instant());
      }
      remotePeer.message(choke);
    }

    private void sendHandshakeMessage(Peer remotePeer, Handshake handshake) {
      logger.info(String.format("Local peer %s sending handshake message to peer %s",
          new String(getId()), new String(remotePeer.getId())));

      PeerState state = getStateForPeer(remotePeer);

      synchronized(state) {
        state.setLocalSentHandshakeAt(new Instant());
      }
      remotePeer.message(handshake);
    }

    private void sendHaveMessage(Peer remotePeer, Have have) {
      logger.info(String.format("Local peer %s sending handshake message to peer %s",
          new String(getId()), new String(remotePeer.getId())));

      PeerState state = getStateForPeer(remotePeer);

      PeerState.PieceDeclaration declaration = new PeerStateImpl.PieceDeclarationImpl();
      declaration.setPieceIndex(have.getPieceIndex());
      declaration.setDeclarationTime(new Instant());

      synchronized(state) {
        state.setLocalHasPiece(declaration);
      }

      remotePeer.message(have);
    }

    private void sendInterestedMessage(Peer remotePeer, Interested interested) {
      logger.info(String.format("Local peer %s sending interested message to peer %s",
          new String(getId()), new String(remotePeer.getId())));

      PeerState state = getStateForPeer(remotePeer);

      synchronized(state) {
        state.setLocalInterestLevelInRemote(InterestLevel.INTERESTED, new Instant());
      }
      remotePeer.message(interested);
    }

    private void sendKeepAliveMessage(Peer remotePeer, KeepAlive keepAlive) {
      logger.info(String.format("Local peer %s sending keep-alive message to peer %s",
          new String(getId()), new String(remotePeer.getId())));

      PeerState state = getStateForPeer(remotePeer);

      synchronized(state) {
        state.setLocalSentKeepAliveAt(new Instant());
      }
      remotePeer.message(keepAlive);
    }


    private void sendNotInterestedMessage(Peer remotePeer, NotInterested notInterested) {
      logger.info(String.format("Local peer %s sending not-interested message to peer %s",
          new String(getId()), new String(remotePeer.getId())));

      PeerState state = getStateForPeer(remotePeer);

      synchronized(state) {
        state.setLocalInterestLevelInRemote(InterestLevel.NOT_INTERESTED, new Instant());
      }
      remotePeer.message(notInterested);
    }

    private void sendPieceMessage(Peer remotePeer, Piece piece) {
      logger.info(String.format("Local peer %s sending piece message to peer %s",
          new String(getId()), new String(remotePeer.getId())));

      PeerState state = getStateForPeer(remotePeer);

      PieceUpload pieceUpload = new PeerStateImpl.PieceUploadImpl();
      pieceUpload.setPieceIndex(piece.getPieceIndex());
      pieceUpload.setBlockOffset(piece.getBeginningOffset());
      pieceUpload.setBlockSize(piece.getBlock().length);
      pieceUpload.setStartTime(new Instant());

      synchronized(state) {
        state.setLocalSentPiece(pieceUpload);
      }

      remotePeer.message(piece);
    }

    private void sendPortMessage(Peer remotePeer, Port port) {
      logger.info(String.format("Local peer %s sending port message to peer %s",
          new String(getId()), new String(remotePeer.getId())));

      PeerState state = getStateForPeer(remotePeer);

      synchronized(state) {
        state.setLocalRequestedPort(port.getPort());
      }
      remotePeer.message(port);
    }

    private void sendRequestMessage(Peer remotePeer, Request request) {
      logger.info(String.format("Local peer %s sending request message to peer %s",
          new String(getId()), new String(remotePeer.getId())));

      PeerState state = getStateForPeer(remotePeer);

      PieceRequest pieceRequest = new PeerStateImpl.PieceRequestImpl();
      pieceRequest.setPieceIndex(request.getPieceIndex());
      pieceRequest.setBlockOffset(request.getBeginningOffset());
      pieceRequest.setBlockSize(request.getBlockLength());
      pieceRequest.setRequestTime(new Instant());

      synchronized(state) {
        state.setLocalRequestedPiece(pieceRequest);
      }
      remotePeer.message(request);
    }

    private void sendUnchokeMessage(Peer remotePeer, Unchoke unchoke) {
      logger.info(String.format("Local peer %s sending unchoke message to peer %s",
          new String(getId()), new String(remotePeer.getId())));

      PeerState state = getStateForPeer(remotePeer);

      synchronized(state) {
        state.setRemoteIsChoked(ChokeStatus.UNCHOKED, new Instant());
      }

      remotePeer.message(unchoke);
    }

    private PeerState getStateForPeer(Peer peer) {
      PeerState state = null;
      synchronized (activePeers) {
        if (activePeers.containsKey(peer)) {
          state = activePeers.get(peer);
        } else {
          logger.info(String.format("Local peer %s adding remote peer %s",
              new String(getId()), new String(peer.getId())));
          state = new PeerStateImpl();
          activePeers.put(peer, state);
        }
      }
      return state;
    }
}
