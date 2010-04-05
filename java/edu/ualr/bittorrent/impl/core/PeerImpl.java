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
import edu.ualr.bittorrent.impl.core.messages.CancelImpl;
import edu.ualr.bittorrent.impl.core.messages.ChokeImpl;
import edu.ualr.bittorrent.impl.core.messages.RequestImpl;
import edu.ualr.bittorrent.impl.core.messages.UnchokeImpl;
import edu.ualr.bittorrent.interfaces.Metainfo;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.Tracker;
import edu.ualr.bittorrent.interfaces.TrackerResponse;
import edu.ualr.bittorrent.interfaces.messages.Cancel;
import edu.ualr.bittorrent.interfaces.messages.Choke;
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

    private void cancel() {
      remote.message(new CancelImpl(local, 0, 0, 100));
    }

    private void choke() {
      remote.message(new ChokeImpl(local));
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
       // TODO: peer.message(new BitFieldImpl(this));
        cancel();
        choke();
       // TODO: peer.message(new HandshakeImpl("12345678901234567890".getBytes(), this));
       // TODO: peer.message(new HaveImpl(this));
       // TODO: peer.message(new InterestedImpl(this));
       // TODO: peer.message(new KeepAliveImpl(this));
       // TODO: peer.message(new NotInterestedImpl(this));
       // TODO: peer.message(new PieceImpl(this));
       // TODO: peer.message(new PortImpl(this));
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

      if (message == null) {
        continue;
      }

      if (message instanceof Cancel) {
        logger.info(String.format("Peer %s canceled by peer %s", new String(id),
            new String(message.getPeer().getId())));
      }
      else if (message instanceof Choke) {
        logger.info(String.format("Peer %s choked by peer %s", new String(id),
            new String(message.getPeer().getId())));
      }
      else if (message instanceof Unchoke) {
          logger.info(String.format("Peer %s unchoked by peer %s", new String(id),
              new String(message.getPeer().getId())));
      } else {
        logger.info(String.format(
            "Peer %s sent message %s", message.getPeer().getId(), message.getType()));
      }
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
