package edu.ualr.bittorrent.impl.core;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

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
import edu.ualr.bittorrent.interfaces.Metainfo.File;
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

/**
 * Default peer implementation.
 */
public class PeerImpl implements Peer {
  private Tracker tracker;
  private final byte[] id;
  private Metainfo metainfo;
  private final PeerBrains brains;
  private final List<Message> messageQueue = Lists.newArrayList();
  private final AtomicInteger downloaded = new AtomicInteger();
  private final AtomicInteger uploaded = new AtomicInteger();
  private final AtomicInteger remaining = new AtomicInteger();
  private final Map<Peer, PeerState> activePeers = new ConcurrentHashMap<Peer, PeerState>();
  private final ConcurrentLinkedQueue<Peer> newlyReportedPeers = new ConcurrentLinkedQueue<Peer>();
  private final Map<Integer, byte[]> data;
  private int lastPieceIndex;
  private int lastPieceSize;
  private int totalDownloadSize;
  private int pieceLength;

  /**
   * Create a new PeerImpl object, providing a unique ID, the brains of the
   * peer, and some initial data related to the torrent.
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
   * Create a new PeerImpl object, providing the brains of the peer and some
   * initial data related to the torrent.
   *
   * @param brains
   * @param initialData
   */
  public PeerImpl(PeerBrains brains, Map<Integer, byte[]> initialData) {
    this(UUID.randomUUID().toString().getBytes(), brains, initialData);
  }

  /**
   * Create a new PeerImpl object, providing some initial data. The default
   * {@link PeerBrainsImpl} will be used to control decision making by the peer.
   *
   * @param initialData
   */
  public PeerImpl(Map<Integer, byte[]> initialData) {
    this(UUID.randomUUID().toString().getBytes(), new PeerBrainsImpl(),
        initialData);
  }

  /**
   * Create a new leeching PeerImpl object accepting the default
   * {@link PeerBrainsImpl} for peer decision making.
   */
  public PeerImpl() {
    this(UUID.randomUUID().toString().getBytes(), new PeerBrainsImpl(), null);
  }

  /**
   * {@inheritDoc}
   */
  public void setMetainfo(Metainfo metainfo) {
    this.metainfo = Preconditions.checkNotNull(metainfo);
    this.tracker = Preconditions.checkNotNull(metainfo.getTrackers().get(0));
    lastPieceIndex = metainfo.getPieces().size() - 1;
    pieceLength = metainfo.getPieceLength();

    totalDownloadSize = 0;
    for (File file : metainfo.getFiles()) {
      totalDownloadSize += file.getLength();
    }
    lastPieceSize = totalDownloadSize % metainfo.getPieceLength();
    if (lastPieceSize == 0) {
      lastPieceSize = pieceLength;
    }
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

    brains.setActivePeers(activePeers);
    brains.setLocalPeer(this);
    brains.setMetainfo(metainfo);
    brains.setData(data);

    synchronized (remaining) {
      remaining.set(howMuchIsLeftToDownload());
    }

    ExecutorService executor = Executors.newFixedThreadPool(2);

    // line of communication with the tracker
    executor.execute(new TrackerTalker(this, this.metainfo.getInfoHash()));

    // outbound peer communication
    executor.execute(new PeerTalkerManager(this));

    // inbound peer communication
    while (true) {
      Message message = null;
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

    synchronized (remaining) {
      remaining.set(howMuchIsLeftToDownload());
    }

    synchronized (downloaded) {
      downloaded.addAndGet(piece.getBlock().length);
    }
  }

  private int howMuchIsLeftToDownload() {
    int downloadedPieceCount = 0;
    boolean downloadedLastPiece = false;

    synchronized (data) {
      downloadedPieceCount = data.size();
      downloadedLastPiece = data.containsKey(lastPieceIndex);
    }

    int downloadedAmount = downloadedPieceCount * metainfo.getPieceLength();

    if (downloadedLastPiece) {
      downloadedAmount -= (metainfo.getPieceLength() - lastPieceSize);
    }

    return totalDownloadSize - downloadedAmount;
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
            infoHash, downloaded.get(), uploaded.get(), remaining.get()));
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

        // let our brain determining what messages we should send, then loop
        // through the messages,
        // logging and dispatching each one
        for (Pair<Peer, Message> peerAndMessage : brains
            .getMessagesToDispatch(null)) {
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

    synchronized (uploaded) {
      uploaded.addAndGet(piece.getBlock().length);
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
}
