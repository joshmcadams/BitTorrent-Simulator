package edu.ualr.bittorrent.impl.core;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

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
      logger.info(String.format("Peer %s contacting tracker", new String(id)));
      TrackerResponse response = tracker.get(
          new TrackerRequestImpl(
              this,
              this.metainfo.getInfoHash(),
              0L, // TODO: downloaded
              0L, // TODO: uploaded
              0L  // TODO: left
          ));
      logger.info(String.format("Peer %s received response from tracker", new String(id)));
      for (Peer peer : response.getPeers()) {
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
