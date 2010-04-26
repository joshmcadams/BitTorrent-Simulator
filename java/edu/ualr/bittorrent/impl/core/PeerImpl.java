package edu.ualr.bittorrent.impl.core;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.joda.time.Instant;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.internal.Nullable;
import com.sun.tools.javac.util.Pair;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.impl.core.messages.HandshakeImpl;
import edu.ualr.bittorrent.impl.core.messages.MessagesModule;
import edu.ualr.bittorrent.interfaces.Message;
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
import edu.ualr.bittorrent.interfaces.messages.CancelFactory;
import edu.ualr.bittorrent.interfaces.messages.Choke;
import edu.ualr.bittorrent.interfaces.messages.ChokeFactory;
import edu.ualr.bittorrent.interfaces.messages.Handshake;
import edu.ualr.bittorrent.interfaces.messages.HandshakeFactory;
import edu.ualr.bittorrent.interfaces.messages.Have;
import edu.ualr.bittorrent.interfaces.messages.HaveFactory;
import edu.ualr.bittorrent.interfaces.messages.Interested;
import edu.ualr.bittorrent.interfaces.messages.InterestedFactory;
import edu.ualr.bittorrent.interfaces.messages.KeepAlive;
import edu.ualr.bittorrent.interfaces.messages.KeepAliveFactory;
import edu.ualr.bittorrent.interfaces.messages.NotInterested;
import edu.ualr.bittorrent.interfaces.messages.NotInterestedFactory;
import edu.ualr.bittorrent.interfaces.messages.Piece;
import edu.ualr.bittorrent.interfaces.messages.PieceFactory;
import edu.ualr.bittorrent.interfaces.messages.Port;
import edu.ualr.bittorrent.interfaces.messages.Request;
import edu.ualr.bittorrent.interfaces.messages.RequestFactory;
import edu.ualr.bittorrent.interfaces.messages.Unchoke;
import edu.ualr.bittorrent.interfaces.messages.UnchokeFactory;

/**
 * Default peer implementation.
 */
public class PeerImpl implements Peer {
  /*
   * Tracker that we will be using to learn about other peers and that we will
   * be reporting our status to
   */
  private Tracker tracker;

  /* Unique ID of this peer */
  private final byte[] id;

  /*
   * Metainfo that we are using to navigate this swarm
   */
  private Metainfo metainfo;

  /*
   * Messages that have been sent to this peer that are pending processing
   */
  private final List<Message> inboundMessageQueue = Lists.newArrayList();

  /*
   * Number of bytes that this peer has downloaded
   */
  private final AtomicInteger bytesDownloaded = new AtomicInteger();

  /* Number of bytes that this peer has uploaded */
  private final AtomicInteger bytesUploaded = new AtomicInteger();

  /*
   * Number of bytes that this peer still needs to download
   */
  private final AtomicInteger bytesRemaining = new AtomicInteger();

  /*
   * Map of active peers and the state of those peers
   */
  private final Map<Peer, PeerState> activePeers = new ConcurrentHashMap<Peer, PeerState>();

  /*
   * List of peers that the tracker has informed us about, but that we have not began communicating with
   */
  private final ConcurrentLinkedQueue<Peer> newlyReportedPeers = new ConcurrentLinkedQueue<Peer>();

  /*
   * The data that is being downloaded
   */
  private final Map<Integer, byte[]> data;

  /*
   *
   */
  private final Injector injector;
  private static final Integer UNCHOKED_PEER_LIMIT = 100;

  /**
   * Create a new PeerImpl object, providing a unique ID, the brains of the
   * peer, and some initial data related to the torrent.
   *
   * @param id
   * @param brains
   * @param initialData
   */
  public PeerImpl(byte[] id, Map<Integer, byte[]> initialData) {
    this.id = Preconditions.checkNotNull(id);
    this.injector = Guice.createInjector(new MessagesModule());

    if (initialData == null) {
      this.data = Maps.newHashMap();
    } else {
      this.data = initialData;
    }
  }

  /**
   * Create a new PeerImpl object, providing the brains of the peer and some
   * initial data related to the torrent.
   *
   * @param brains
   * @param initialData
   */
  public PeerImpl(Map<Integer, byte[]> initialData) {
    this(UUID.randomUUID().toString().getBytes(), initialData);
  }

  /**
   * Create a new leeching PeerImpl object accepting the default
   * {@link PeerBrainsImpl} for peer decision making.
   */
  public PeerImpl() {
    this(UUID.randomUUID().toString().getBytes(), null);
  }

  /**
   * {@inheritDoc}
   */
  public void setMetainfo(Metainfo metainfo) {
    this.metainfo = Preconditions.checkNotNull(metainfo);
    this.tracker = Preconditions.checkNotNull(metainfo.getTrackers().get(0));
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
    synchronized (inboundMessageQueue) {
      inboundMessageQueue.add(message);
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
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return new String(id);
  }

  /**
   * The PeerImpl is intended to run as a thread of a higher-level
   * {@link ExecutorService}. The peer itself creates a new
   * {@link ExecutorService} that it uses to spawn threads for parallel
   * communication with the tracker and with other peers in the swarm.
   *
   * This thread loops infinitely, pulling messages off of a message queue. The
   * message queue is populated with messages received from other peers in the
   * swarm. The messages are processed in the sense that they are used to
   * populate a state object. This object is used by the {@link PeerBrains} to
   * make decisions about how this peer should act.
   */
  public void run() {
    Preconditions.checkNotNull(tracker);
    Preconditions.checkNotNull(id);
    Preconditions.checkNotNull(metainfo);

    synchronized (bytesRemaining) {
      bytesRemaining.set(howMuchIsLeftToDownload());
    }

    ExecutorService executor = Executors.newFixedThreadPool(2);

    // line of communication with the tracker
    executor.execute(new TrackerTalker(this, this.metainfo.getInfoHash()));

    // outbound peer communication
    executor.execute(new PeerTalkerManager(this));

    // inbound peer communication
    while (true) {
      Message message = null;
      synchronized (inboundMessageQueue) {
        if (inboundMessageQueue.size() > 0) {
          message = inboundMessageQueue.remove(0);
        }
      }
      if (message != null) {
        processMessage(message);
      }
    }
  }

  /**
   * A {@link BitField} message is really just the remote peer letting the local
   * peer know all of the pieces that it has via a single message instead of via
   * multiple {@link Have} messages. This method parses the {@link BitField} and
   * stores a {@link PieceDeclaration} for each piece that the remote claims to
   * possess.
   *
   * @param bitfield
   */
  private void processBitFieldMessage(BitField bitfield) {
    PeerState state = activePeers.get(bitfield.getSendingPeer());
    Instant now = new Instant();
    for (int i = 0; i < bitfield.getBitField().length; i++) {
      if (bitfield.getBitField()[i] != 0x0) {
        PeerState.PieceDeclaration declaration = new PeerStateImpl.PieceDeclarationImpl();
        declaration.setDeclarationTime(now);
        declaration.setPieceIndex(i);
        synchronized (state) {
          state.setRemoteHasPiece(declaration);
        }
      }
    }
  }

  /**
   * After a remote peer requests a piece, it can later cancel that request by
   * sending a cancel message. This method processes the given cancel message
   * and notes the cancel in the peer state.
   *
   * @param cancel
   */
  private void processCancelMessage(Cancel cancel) {
    PeerState state = activePeers.get(cancel.getSendingPeer());

    PieceRequest request = new PeerStateImpl.PieceRequestImpl();
    request.setPieceIndex(cancel.getPieceIndex());
    request.setBlockOffset(cancel.getBeginningOffset());
    request.setBlockSize(cancel.getBlockLength());
    request.setRequestTime(new Instant());

    synchronized (state) {
      state.cancelRemoteRequestedPiece(request);
    }
  }

  /**
   * When choked by a remote peer, the local client should send any requests.
   * This method notes the choke in shared state.
   *
   * @param choke
   */
  private void processChokeMessage(Choke choke) {
    PeerState state = getStateForPeer(choke.getSendingPeer());

    synchronized (state) {
      state.setLocalIsChoked(ChokeStatus.CHOKED, new Instant());
    }
  }

  /**
   * In the real-world of BitTorrent, a remote peer might request communication
   * on a specific port. This method takes note of the port request; however,
   * since this simulator doesn't make actual network calls, the port operation
   * does little more than update state.
   *
   * @param port
   */
  private void processPortMessage(Port port) {
    PeerState state = getStateForPeer(port.getSendingPeer());

    synchronized (state) {
      state.setRemoteRequestedPort(port.getPort());
    }
  }

  /**
   * A request message is sent by the remote peer to request a specific piece
   * from the local client. This method takes note of the specific request.
   *
   * @param request
   */
  private void processRequestMessage(Request request) {
    PeerState state = activePeers.get(request.getSendingPeer());

    PieceRequest pieceRequest = new PeerStateImpl.PieceRequestImpl();
    pieceRequest.setPieceIndex(request.getPieceIndex());
    pieceRequest.setBlockOffset(request.getBeginningOffset());
    pieceRequest.setBlockSize(request.getBlockLength());
    pieceRequest.setRequestTime(new Instant());

    synchronized (state) {
      state.setRemoteRequestedPiece(pieceRequest);
    }
  }

  /**
   * A handshake message is sent between peers to initiate communication. This
   * method notes that the remote peer sent a handshake.
   *
   * @param handshake
   */
  private void processHandshakeMessage(Handshake handshake) {
    PeerState state = getStateForPeer(handshake.getSendingPeer());

    synchronized (state) {
      state.setRemoteSentHandshakeAt(new Instant());
    }
  }

  /**
   * As a peer collects pieces, it announces the newly collected pieces to its
   * peers. This method processes a piece announcement from a remote peer.
   *
   * @param have
   */
  private void processHaveMessage(Have have) {
    PeerState state = getStateForPeer(have.getSendingPeer());

    PieceDeclaration declaration = new PeerStateImpl.PieceDeclarationImpl();
    declaration.setPieceIndex(have.getPieceIndex());
    declaration.setDeclarationTime(new Instant());

    synchronized (state) {
      state.setRemoteHasPiece(declaration);
    }
  }

  /**
   * When a remote peer, probably a choked one, is interested in a piece that
   * the local client has, it sends an {@link Interested} message to let the
   * local client know that the remote is interested in what the local client
   * has to offer. This method notes that interest.
   *
   * @param interested
   */
  private void processInterestedMessage(Interested interested) {
    PeerState state = getStateForPeer(interested.getSendingPeer());

    synchronized (state) {
      state.setRemoteInterestLevelInLocal(InterestLevel.INTERESTED,
          new Instant());
    }
  }

  /**
   * Periodically, clients should send a {@link KeepAlive} message to peers in
   * which they are connected, just to let the peers know that the client is
   * here and communicating... it just might not have had anything interesting
   * to say for a while.
   *
   * @param keepAlive
   */
  private void processKeepAliveMessage(KeepAlive keepAlive) {
    PeerState state = getStateForPeer(keepAlive.getSendingPeer());

    synchronized (state) {
      state.setRemoteSentKeepAliveAt(new Instant());
    }
  }

  /**
   * When a peer isn't interested in anything that the local client has to
   * offer, it can tell them so using the {@link NotInterested} message. This
   * lets the local client know that there really isn't any reason to unchoke
   * the remote peer.
   *
   * @param notInterested
   */
  private void processNotInterestedMessage(NotInterested notInterested) {
    PeerState state = getStateForPeer(notInterested.getSendingPeer());

    synchronized (state) {
      state.setRemoteInterestLevelInLocal(InterestLevel.NOT_INTERESTED,
          new Instant());
    }
  }

  /**
   * When a remote peer sends a piece of data to the local client, it does so
   * via a {@link Piece} message. This method collects that piece.
   *
   * @param piece
   */
  private void processPieceMessage(Piece piece) {
    PeerState state = activePeers.get(piece.getSendingPeer());

    PieceDownload download = new PeerStateImpl.PieceDownloadImpl();
    download.setPieceIndex(piece.getPieceIndex());
    download.setBlockOffset(piece.getBeginningOffset());
    download.setBlockSize(piece.getBlock().length);
    download.setStartTime(piece.getSentTime());
    download.setCompletionTime(new Instant());

    synchronized (state) {
      state.setRemoteSentPiece(download);
    }

    synchronized (data) {
      data.put(piece.getPieceIndex(), piece.getBlock());
    }

    synchronized (bytesRemaining) {
      bytesRemaining.set(howMuchIsLeftToDownload());
    }

    synchronized (bytesDownloaded) {
      bytesDownloaded.addAndGet(piece.getBlock().length);
    }
  }

  private int howMuchIsLeftToDownload() {
    int downloadedPieceCount = 0;
    boolean downloadedLastPiece = false;

    synchronized (data) {
      downloadedPieceCount = data.size();
      downloadedLastPiece = data.containsKey(metainfo.getLastPieceIndex());
    }

    int downloadedAmount = downloadedPieceCount * metainfo.getPieceLength();

    if (downloadedLastPiece) {
      downloadedAmount -= (metainfo.getPieceLength() - metainfo.getLastPieceSize());
    }

    return metainfo.getTotalDownloadSize() - downloadedAmount;
  }

  /**
   * A remote peer sends an {@link Unchoke} message to the local client to let
   * the local client know that it is okay to start sending requests for data.
   * This method notes the unchoked state.
   *
   * @param unchoke
   */
  private void processUnchokeMessage(Unchoke unchoke) {
    PeerState state = getStateForPeer(unchoke.getSendingPeer());

    synchronized (state) {
      state.setLocalIsChoked(ChokeStatus.UNCHOKED, new Instant());
    }
  }

  /**
   * Messages are received as somewhat generic {@link PeerMessage} objects. This
   * method determines the type of {@link PeerMessage} and dispatches the
   * message to the appropriate handler.
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
          "Peer %s sent an unsupported message of type %s", new String(message
              .getSendingPeer().getId()), message.getType()));
    }
    for (Pair<Peer, Message> peerAndMessage : getMessagesToDispatch(message)) {
      sendMessage(peerAndMessage.fst, peerAndMessage.snd);
    }
  }

  /**
   * The TrackerTalker object is used by {@link PeerImpl} to keep a constant
   * thread of communication open with the given {@link Tracker}. The allows for
   * the {@link PeerImpl} to keep the {@link Tracker} updated with the status of
   * the {@link PeerImpl} and allows for the {@link PeerImpl} to keep a fresh
   * list of {@link Peer}s in the swarm.
   */
  private class TrackerTalker implements Runnable {
    private final Peer parent;
    private final byte[] infoHash;

    /**
     * Creates a new {@link TrackerTalker} that can be used to keep
     * communication flowing between the given client and a {@link Tracker}.
     *
     * @param parent
     * @param infoHash
     */
    TrackerTalker(Peer parent, byte[] infoHash) {
      this.parent = Preconditions.checkNotNull(parent);
      this.infoHash = Preconditions.checkNotNull(infoHash);
    }

    /**
     * Thread of execution that builds a request to the {@link Tracker}, sends
     * the request, waits for the response, and then pulls the list of
     * {@link Peers} reported by the {@link Tracker} from the response.
     */
    public void run() {
      while (true) {
        TrackerResponse response = tracker.get(new TrackerRequestImpl(parent,
            infoHash, bytesDownloaded.get(), bytesUploaded.get(),
            bytesRemaining.get()));
        for (Peer peer : response.getPeers()) {
          if (!parent.equals(peer)) {
            newlyReportedPeers.add(peer);
          }
        }
        try {
          Thread.sleep(response.getInterval() * 1000);
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

    /**
     * Create a new talker manager object.
     *
     * @param local
     */
    PeerTalkerManager(Peer local) {
      this.local = Preconditions.checkNotNull(local);
    }

    /**
     * As new peers are discovered, set up a communication channel with them.
     */
    public void run() {
      while (true) {
        // add new peers that have been provided to us by the tracker
        for (Peer peer : newlyReportedPeers) {
          if (!activePeers.containsKey(peer)) {
            synchronized (activePeers) {
              activePeers.put(peer, new PeerStateImpl());
            }
          }
        }

        for (Pair<Peer, Message> peerAndMessage : getMessagesToDispatch(null)) {
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

  /**
   * Dispatch a message to a peer.
   *
   * @param remotePeer
   * @param message
   */
  private void sendMessage(Peer remotePeer, Message message) {
    if (message instanceof BitField) {
      sendBitFieldMessage(remotePeer, (BitField) message);
    } else if (message instanceof Cancel) {
      sendCancelMessage(remotePeer, (Cancel) message);
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
      throw new IllegalArgumentException(
          String
              .format(
                  "Local client %s attempting to send an unsupported message of type %s",
                  new String(getId()), message.getType()));
    }
  }

  /**
   * Package and send a bitfield message.
   *
   * @param remotePeer
   * @param bitfield
   */
  private void sendBitFieldMessage(Peer remotePeer, BitField bitfield) {
    PeerState state = getStateForPeer(remotePeer);

    Instant now = new Instant();
    for (int i = 0; i < bitfield.getBitField().length; i++) {
      if (bitfield.getBitField()[i] != 0x0) {
        PeerState.PieceDeclaration declaration = new PeerStateImpl.PieceDeclarationImpl();
        declaration.setDeclarationTime(now);
        declaration.setPieceIndex(i);
        synchronized (state) {
          state.setRemoteHasPiece(declaration);
        }
      }
    }

    remotePeer.message(bitfield);
  }

  /**
   * Package and send a cancel message.
   *
   * @param remotePeer
   * @param cancel
   */
  private void sendCancelMessage(Peer remotePeer, Cancel cancel) {
    PeerState state = getStateForPeer(remotePeer);

    PieceRequest pieceRequest = new PeerStateImpl.PieceRequestImpl();
    pieceRequest.setPieceIndex(cancel.getPieceIndex());
    pieceRequest.setBlockOffset(cancel.getBeginningOffset());
    pieceRequest.setBlockSize(cancel.getBlockLength());

    synchronized (state) {
      state.cancelLocalRequestedPiece(pieceRequest);
    }
    remotePeer.message(cancel);
  }

  /**
   * Package and send a choke message.
   *
   * @param remotePeer
   * @param choke
   */
  private void sendChokeMessage(Peer remotePeer, Choke choke) {
    PeerState state = getStateForPeer(remotePeer);

    synchronized (state) {
      state.setRemoteIsChoked(ChokeStatus.CHOKED, new Instant());
    }
    remotePeer.message(choke);
  }

  /**
   * Package and send a handshake message.
   *
   * @param remotePeer
   * @param handshake
   */
  private void sendHandshakeMessage(Peer remotePeer, Handshake handshake) {
    PeerState state = getStateForPeer(remotePeer);

    synchronized (state) {
      state.setLocalSentHandshakeAt(new Instant());
    }
    remotePeer.message(handshake);
  }

  /**
   * Package and send a have message.
   *
   * @param remotePeer
   * @param have
   */
  private void sendHaveMessage(Peer remotePeer, Have have) {
    PeerState state = getStateForPeer(remotePeer);

    PeerState.PieceDeclaration declaration = new PeerStateImpl.PieceDeclarationImpl();
    declaration.setPieceIndex(have.getPieceIndex());
    declaration.setDeclarationTime(new Instant());

    synchronized (state) {
      state.setLocalHasPiece(declaration);
    }

    remotePeer.message(have);
  }

  /**
   * Package and send an interested message.
   *
   * @param remotePeer
   * @param interested
   */
  private void sendInterestedMessage(Peer remotePeer, Interested interested) {
    PeerState state = getStateForPeer(remotePeer);

    synchronized (state) {
      state.setLocalInterestLevelInRemote(InterestLevel.INTERESTED,
          new Instant());
    }
    remotePeer.message(interested);
  }

  /**
   * Package and send a keepalive message.
   *
   * @param remotePeer
   * @param keepAlive
   */
  private void sendKeepAliveMessage(Peer remotePeer, KeepAlive keepAlive) {
    PeerState state = getStateForPeer(remotePeer);

    synchronized (state) {
      state.setLocalSentKeepAliveAt(new Instant());
    }
    remotePeer.message(keepAlive);
  }

  /**
   * Package and sent a not-interested message.
   *
   * @param remotePeer
   * @param notInterested
   */
  private void sendNotInterestedMessage(Peer remotePeer,
      NotInterested notInterested) {
    PeerState state = getStateForPeer(remotePeer);

    synchronized (state) {
      state.setLocalInterestLevelInRemote(InterestLevel.NOT_INTERESTED,
          new Instant());
    }
    remotePeer.message(notInterested);
  }

  /**
   * Package and send a piece message.
   *
   * @param remotePeer
   * @param piece
   */
  private void sendPieceMessage(Peer remotePeer, Piece piece) {
    PeerState state = getStateForPeer(remotePeer);

    PieceUpload pieceUpload = new PeerStateImpl.PieceUploadImpl();
    pieceUpload.setPieceIndex(piece.getPieceIndex());
    pieceUpload.setBlockOffset(piece.getBeginningOffset());
    pieceUpload.setBlockSize(piece.getBlock().length);
    pieceUpload.setStartTime(new Instant());

    synchronized (state) {
      state.setLocalSentPiece(pieceUpload);
    }

    synchronized (bytesUploaded) {
      bytesUploaded.addAndGet(piece.getBlock().length);
    }

    remotePeer.message(piece);
  }

  /**
   * Package and send a port message.
   *
   * @param remotePeer
   * @param port
   */
  private void sendPortMessage(Peer remotePeer, Port port) {
    PeerState state = getStateForPeer(remotePeer);

    synchronized (state) {
      state.setLocalRequestedPort(port.getPort());
    }
    remotePeer.message(port);
  }

  /**
   * Package and send a request message.
   *
   * @param remotePeer
   * @param request
   */
  private void sendRequestMessage(Peer remotePeer, Request request) {
    PeerState state = getStateForPeer(remotePeer);

    PieceRequest pieceRequest = new PeerStateImpl.PieceRequestImpl();
    pieceRequest.setPieceIndex(request.getPieceIndex());
    pieceRequest.setBlockOffset(request.getBeginningOffset());
    pieceRequest.setBlockSize(request.getBlockLength());
    pieceRequest.setRequestTime(new Instant());

    synchronized (state) {
      state.setLocalRequestedPiece(pieceRequest);
    }
    remotePeer.message(request);
  }

  /**
   * Package and send an unchoke message.
   *
   * @param remotePeer
   * @param unchoke
   */
  private void sendUnchokeMessage(Peer remotePeer, Unchoke unchoke) {
    PeerState state = getStateForPeer(remotePeer);

    synchronized (state) {
      state.setRemoteIsChoked(ChokeStatus.UNCHOKED, new Instant());
    }

    remotePeer.message(unchoke);
  }

  /**
   * Package and send a state message.
   *
   * @param peer
   * @return
   */
  private PeerState getStateForPeer(Peer peer) {
    PeerState state = null;
    synchronized (activePeers) {
      if (activePeers.containsKey(peer)) {
        state = activePeers.get(peer);
      } else {
        state = new PeerStateImpl();
        activePeers.put(peer, state);
      }
    }
    return state;
  }

  private PeerState returnStateIfAvailable(Peer p) {
    if (p.equals(this)) {
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

  private boolean askForSomethingFromPeer(Peer p, PeerState state,
      List<Pair<Peer, Message>> messages) {
    /*
     * Let peers that are choking and that have pieces that we want know that we
     * are interested
     */
    if (expressInterest(p, state, messages)) {
      return true;
    } else if (makeRequests(p, state, messages)) {
      return true;
    }

    return false;
  }

  private boolean respondToPeerRequests(Peer p, PeerState state,
      List<Pair<Peer, Message>> messages) {
    /* Unchoke some peers if there are any that seem worthy */
    if (unchoke(p, state, messages)) {
      return true;
    } else if (sendPieces(p, state, messages)
        || cancelPieceRequests(p, state, messages)) {
      return true;
    }

    return false;
  }

  /**
   * After communication between peers is initialized, they can actually start
   * sending meaningful messages back-and-fourth. This method sees if any
   * messages need to be sent, and if so, returns true. Otherwise, it returns
   * false.
   *
   * @param p
   * @param state
   * @param messages
   * @return
   */
  private boolean sendMeaningfuleMessageToPeer(Peer p, PeerState state,
      List<Pair<Peer, Message>> messages) {
    boolean messageSent = false;
    messageSent = askForSomethingFromPeer(p, state, messages);
    messageSent &= respondToPeerRequests(p, state, messages);
    return messageSent;
  }

  public List<Pair<Peer, Message>> initiateCommunication() {
    return null;
  }

  public List<Pair<Peer, Message>> respondToASpecificMessage(
      @Nullable Message message) {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public List<Pair<Peer, Message>> getMessagesToDispatch(
      @Nullable Message message) {
    Preconditions.checkNotNull(activePeers);
    Preconditions.checkNotNull(metainfo);
    Preconditions.checkNotNull(data);

    if (message == null) {
      return initiateCommunication();
    } else {
      return respondToASpecificMessage(message);
    }
    /*
     *
     * List<Pair<Peer, Message>> messages = Lists.newArrayList();
     *
     * BitField messages will not be processed in this experiment. Port messages
     * will not be processed in this experiment.
     *
     * Handshake Any peer that I have not sent a handshake too will be sent a
     * handshake.
     *
     * Any peer that I have sent a handshake too, but who has not shaken back
     * over the last x period of time will receive another handshake. After n
     * attempts, the peer will be ignored.
     *
     * If a receive a handshake, i will accept it. If i recieve repeated
     * handshakes, I will respond with my own handshake.
     *
     * Choke Any peer that I have shaken hands with, but have not choked will be
     * choked.
     *
     * Periodically, I will choke peers that I feel are currently unworthy.
     *
     * Have All peers should get an updated have list from me.
     *
     * Cancel After I receive a piece from a peer, I will tell other peers that
     * are sending that piece that I no longer need it.
     *
     * Interested If a peer sends an interested message and it choked, I will
     * consider unchoking them.
     *
     * If I am choked by a peer, but would like to get data from them, I will
     * express interest.
     *
     * Not Interested If I am choked by a peer, I will let that peer know that I
     * don't care if I'm unchoked by expressing a lack of interest.
     *
     * If a peer expresses disinterest in me, I will choke that peer if they are
     * unchoked.
     *
     * Piece If an unchoked peer makes a request to me, I will do my best to
     * honor that request.
     *
     * Request If I am unchoked and a peer has data that I need, I will request
     * it from that peer. If the peer does not respond in a reasonable amount of
     * time, I will request the data from another peer.
     *
     * Unchoke If a peer is choked and has expressed interest and if I have a
     * slot open, I will unchoke the peer for a limited period of time to give
     * them a chance to request data from me.
     *
     * KeepAlive If I have no other message to send a peer, I will send it a
     * keep alive. Set<Peer> peers;
     *
     * synchronized (activePeers) { peers = activePeers.keySet(); }
     *
     * for (Peer p : peers) { messages.addAll(makePeerLevelDecisions());
     * PeerState state = returnStateIfAvailable(p); if (state == null) {
     * continue; }
     *
     * if (handleCommunicationInitalization(p, state, messages)) { continue; }
     *
     * Let the remote peer know about any new pieces that we might have if
     * (letPeerKnowAboutNewPieces(p, state, messages)) { continue; }
     *
     * if (sendMeaningfuleMessageToPeer(p, state, messages)) { continue; }
     *
     * If no other messages are necessary, just send a keep alive
     * sendKeepAlive(p, state, messages); }
     *
     * return messages;
     */
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
      messages.add(new Pair<Peer, Message>(remotePeer, injector.getInstance(
          HandshakeFactory.class).create(this, remotePeer,
          HandshakeImpl.DEFAULT_PROTOCOL_IDENTIFIER, metainfo.getInfoHash(),
          HandshakeImpl.DEFAULT_RESERVED_BYTES)));

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
      // shake again just to be sure that the remote got ours
      /*
       * messages.add(new Pair<Peer, Message>(remotePeer, injector.getInstance(
       * HandshakeFactory.class).create(this, remotePeer,
       * HandshakeImpl.DEFAULT_PROTOCOL_IDENTIFIER, metainfo.getInfoHash(),
       * HandshakeImpl.DEFAULT_RESERVED_BYTES)));
       */
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
      messages.add(new Pair<Peer, Message>(remotePeer, injector.getInstance(
          ChokeFactory.class).create(this, remotePeer)));

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
        messages.add(new Pair<Peer, Message>(remotePeer, injector.getInstance(
            HaveFactory.class).create(this, remotePeer, pieceIndex)));
      }
    }

    return pieceDeclared;
  }

  /**
   * If a neighbor has a piece that we are interested in and if we are choked,
   * then we should let the neighbor know that we'd like to request a piece of
   * data from it, hoping that we will soon be unchoked. If the we are choked
   * and our neighbor isn't interesting, let the know. Otherwise, don't send any
   * messages about our interest.
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
          messages.add(new Pair<Peer, Message>(remotePeer, injector
              .getInstance(InterestedFactory.class).create(this, remotePeer)));
          return true;
        }
      }
    }

    messages.add(new Pair<Peer, Message>(remotePeer, injector.getInstance(
        NotInterestedFactory.class).create(this, remotePeer)));

    return true;
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
      // TODO: add some element of optimistic unchoking here... now this is too
      // optimistic
      if (peerState.isRemoteChoked() != null
          && ChokeStatus.UNCHOKED.equals(peerState.isRemoteChoked().fst)) {
        unchokedPeerCount++;
      }
    }

    if (unchokedPeerCount < UNCHOKED_PEER_LIMIT) {
      messages.add(new Pair<Peer, Message>(remotePeer, injector.getInstance(
          UnchokeFactory.class).create(this, remotePeer)));
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
    // TODO: be more intelligent about requests (by intelligent, i really be
    // true to spec)

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

    ImmutableList<PieceRequest> alreadyRequestedPieces = null;
    synchronized (state) {
      alreadyRequestedPieces = state.getLocalRequestedPieces();
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
          messages.add(new Pair<Peer, Message>(remotePeer, injector
              .getInstance(RequestFactory.class).create(this, remotePeer,
                  piece.getPieceIndex(), 0, metainfo.getPieceLength())));
        }

        return true;
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

    if (requestedPieces == null || requestedPieces.size() == 0) {
      return false;
    }

    if (choked == null || ChokeStatus.CHOKED.equals(choked.fst)) {
      return false; // remote is choked
    }

    byte[] bytes = null;
    int requestedIndex = requestedPieces.get(0).getPieceIndex();
    int requestedOffset = requestedPieces.get(0).getBlockOffset();
    int requestedSize = requestedPieces.get(0).getBlockSize();

    synchronized (data) {
      if (data.containsKey(requestedIndex)) {
        bytes = data.get(requestedIndex);
      }
    }

    if (bytes == null) {
      return false;
    }

    if (requestedOffset > 0) {
      if (requestedOffset + requestedSize <= bytes.length) {
        byte[] newBytes = new byte[requestedSize];
        System.arraycopy(bytes, requestedOffset, newBytes, 0, requestedSize);
        bytes = newBytes;
      } else {
        return false;
      }
    }

    messages.add(new Pair<Peer, Message>(remotePeer, injector.getInstance(
        PieceFactory.class).create(this, remotePeer, requestedIndex,
        requestedOffset, bytes)));

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
        messages.add(new Pair<Peer, Message>(remotePeer, injector.getInstance(
            CancelFactory.class).create(this, remotePeer,
            request.getPieceIndex(), request.getBlockOffset(),
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
    messages.add(new Pair<Peer, Message>(remotePeer, injector.getInstance(
        KeepAliveFactory.class).create(this, remotePeer)));
    return true;
  }

}
