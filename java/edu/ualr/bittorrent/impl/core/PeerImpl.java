package edu.ualr.bittorrent.impl.core;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

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
import edu.ualr.bittorrent.interfaces.Tracker;
import edu.ualr.bittorrent.interfaces.TrackerResponse;
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
  private final Map<Integer, byte[]> pieces = new ConcurrentHashMap<Integer, byte[]>();
  private final Map<Peer, Boolean> peerMap = new ConcurrentHashMap<Peer, Boolean>();

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

  public PeerImpl(byte[] id) {
    this.id = Preconditions.checkNotNull(id);
  }

  public PeerImpl() {
    this(UUID.randomUUID().toString().getBytes());
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
          if (!peerMap.containsKey(peer)) {
            peerMap.put(peer, false);
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
        for (Peer peer : peerMap.keySet()) {
          // TODO: add culling of dead peers
          if (!peerMap.get(peer)) {
            logger.info(String.format("Local peer %s adding remote peer %s",
                new String(local.getId()),
                new String(peer.getId())));
            executor.execute(new PeerTalker(local, peer));
            peerMap.put(peer, true);
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

  /**
   * PeerTalker
   */
  private class PeerTalker implements Runnable {
    private final Peer local;
    private final Peer remote;

    PeerTalker(Peer local, Peer remote) {
      this.local = Preconditions.checkNotNull(local);
      this.remote = Preconditions.checkNotNull(remote);
    }

    private void bitfield() {
      remote.message(new BitFieldImpl(local, "123".getBytes()));
    }

    private void cancel() {
      remote.message(new CancelImpl(local, 0, 0, 100));
    }

    private void choke() {
      remote.message(new ChokeImpl(local));
    }

    private void handshake() {
      remote.message(new HandshakeImpl(local.getId(), local));
    }

    private void have() {
      remote.message(new HaveImpl(local, 0));
    }

    private void interested() {
      remote.message(new InterestedImpl(local));
    }

    private void keepAlive() {
      remote.message(new KeepAliveImpl(local));
    }

    private void notInterested() {
      remote.message(new NotInterestedImpl(local));
    }

    private void piece() {
      remote.message(new PieceImpl(local, 0, 0, "TODO".getBytes()));
    }

    private void port() {
      remote.message(new PortImpl(local, 12345));
    }

    private void request() {
      remote.message(new RequestImpl(local, 0, 0, 100));
    }

    private void unchoke() {
      remote.message(new UnchokeImpl(local));
    }

    public void run() {
      logger.info("Peer talker started");
      while (true) {
        bitfield();
        cancel();
        choke();
        handshake();
        have();
        interested();
        keepAlive();
        notInterested();
        piece();
        port();
        request();
        unchoke();
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
    logger.info(String.format("Peer %s bit field by peer %s", new String(id),
        new String(bitfield.getPeer().getId())));
  }

  private void processCancelMessage(Cancel cancel) {
    logger.info(String.format("Peer %s canceled by peer %s", new String(id),
        new String(cancel.getPeer().getId())));
  }

  private void processChokeMessage(Choke choke) {
    logger.info(String.format("Peer %s choked by peer %s", new String(id),
        new String(choke.getPeer().getId())));
  }

  private void processPortMessage(Port port) {
    logger.info(String.format("Peer %s received port request from peer %s", new String(id),
        new String(port.getPeer().getId())));
  }

  private void processRequestMessage(Request request) {
    logger.info(String.format("Peer %s received request from peer %s", new String(id),
        new String(request.getPeer().getId())));
  }

  private void processHandshakeMessage(Handshake handshake) {
    logger.info(String.format("Peer %s received handshake from peer %s", new String(id),
        new String(handshake.getPeer().getId())));
  }

  private void processHaveMessage(Have have) {
    logger.info(String.format("Peer %s received have message from peer %s", new String(id),
        new String(have.getPeer().getId())));
  }

  private void processInterestedMessage(Interested interested) {
    logger.info(String.format(
        "Peer %s received interested message from peer %s", new String(id),
        new String(interested.getPeer().getId())));
  }

  private void processKeepAliveMessage(KeepAlive keepAlive) {
    logger.info(String.format("Peer %s received keep alive from peer %s", new String(id),
        new String(keepAlive.getPeer().getId())));
  }

  private void processNotInterestedMessage(NotInterested notInterested) {
    logger.info(String.format("Peer %s received not interested from peer %s", new String(id),
        new String(notInterested.getPeer().getId())));
  }

  private void processPieceMessage(Piece piece) {
    logger.info(String.format("Peer %s received piece from peer %s", new String(id),
        new String(piece.getPeer().getId())));
  }

  private void processUnchokeMessage(Unchoke unchoke) {
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
