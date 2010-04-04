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
import edu.ualr.bittorrent.interfaces.Metainfo;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.Tracker;
import edu.ualr.bittorrent.interfaces.TrackerResponse;

public class PeerImpl implements Peer {
  private Tracker tracker;
  private byte[] id;
  private Metainfo metainfo;
  private static final Logger logger = Logger.getLogger(PeerImpl.class);
  private final List<PeerMessage<?>> messageQueue = Lists.newArrayList();
  private final AtomicInteger downloaded = new AtomicInteger();
  private final AtomicInteger uploaded = new AtomicInteger();
  private final AtomicInteger remaining = new AtomicInteger();

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

  private class TrackerTalker implements Runnable {
    private final Peer parent;
    private final byte[] infoHash;

    TrackerTalker(Peer parent, byte[] infoHash) {
      this.parent = Preconditions.checkNotNull(parent);
      this.infoHash = Preconditions.checkNotNull(infoHash);
    }

    public void run() {
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
        logger.info("Sleeping for " + response.getInterval());
        Thread.sleep(response.getInterval());
        logger.info("Awake from " + response.getInterval());
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private final Map<Peer, Boolean> peerMap = new ConcurrentHashMap<Peer, Boolean>();

  private class PeerTalkerManager implements Runnable {
    private final Peer local;
    private final ExecutorService executor;

    PeerTalkerManager(Peer local, ExecutorService executor) {
      this.local = Preconditions.checkNotNull(local);
      this.executor = Preconditions.checkNotNull(executor);
    }

    public void run() {
      logger.info("Peer talker manager started");
      for (Peer peer : peerMap.keySet()) {
        // TODO: add culling of dead peers
        if (!peerMap.get(peer)) {
          executor.execute(new PeerTalker(local, peer));
          peerMap.put(peer, true);
        }
      }
    }
  }

  private class PeerTalker implements Runnable {
    private Peer local;
    private Peer remote;

    PeerTalker(Peer local, Peer remote) {
      local = Preconditions.checkNotNull(local);
      remote = Preconditions.checkNotNull(remote);
    }

    public void run() {
      logger.info("Peer talker started");
      while (true) {
       // TODO: peer.message(new BitFieldImpl(this));
       // TODO: peer.message(new CancelImpl(this));
       // TODO: peer.message(new ChokeImpl(this));
       // TODO: peer.message(new HandshakeImpl("12345678901234567890".getBytes(), this));
       // TODO: peer.message(new HaveImpl(this));
       // TODO: peer.message(new InterestedImpl(this));
       // TODO: peer.message(new KeepAliveImpl(this));
       // TODO: peer.message(new NotInterestedImpl(this));
       // TODO: peer.message(new PieceImpl(this));
       // TODO: peer.message(new PortImpl(this));
       // TODO: peer.message(new RequestImpl(this));
       // TODO: peer.message(new UnchokeImpl(this));
        try {
          logger.info("Sleeping");
          Thread.sleep(100);
          logger.info("Awake");
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

    PeerMessage<?> message = null;
    synchronized (messageQueue) {
     if (messageQueue.size() > 0) {
       message = messageQueue.get(0);
       messageQueue.remove(0);
     }
    }

    if (message != null) {
      logger.info(String.format(
          "Peer %s sent message %s", message.getPeer().getId(), message.getType()));
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
