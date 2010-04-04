package edu.ualr.bittorrent.impl.core;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

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

public class PeerImpl implements Peer {
  private Tracker tracker;
  private byte[] id;
  private Metainfo metainfo;
  private static final Logger logger = Logger.getLogger(PeerImpl.class);
  private final List<PeerMessage<?>> messageQueue = Lists.newArrayList();

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

  public void run() {
    Preconditions.checkNotNull(tracker);
    Preconditions.checkNotNull(id);
    Preconditions.checkNotNull(metainfo);
    logger.info(String.format("Peer %s running", new String(id)));
    while (true) {
      TrackerResponse response = tracker.get(new TrackerRequestImpl(this));
      for (Peer peer : response.getPeers()) {
        peer.message(new BitFieldImpl(this));
        peer.message(new CancelImpl(this));
        peer.message(new ChokeImpl(this));
        peer.message(
            new HandshakeImpl("12345678901234567890".getBytes(), this));
        peer.message(new HaveImpl(this));
        peer.message(new InterestedImpl(this));
        peer.message(new KeepAliveImpl(this));
        peer.message(new NotInterestedImpl(this));
        peer.message(new PieceImpl(this));
        peer.message(new PortImpl(this));
        peer.message(new RequestImpl(this));
        peer.message(new UnchokeImpl(this));
      }
      break;
    }

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
