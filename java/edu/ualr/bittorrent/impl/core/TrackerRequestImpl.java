package edu.ualr.bittorrent.impl.core;

import com.google.common.base.Preconditions;

import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.TrackerRequest;

public class TrackerRequestImpl implements TrackerRequest{
  private final Peer peer;

  TrackerRequestImpl(Peer peer) {
    this.peer = Preconditions.checkNotNull(peer);
  }

  public Peer getPeer() {
    return peer;
  }
}
