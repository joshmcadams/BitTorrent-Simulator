package edu.ualr.bittorrent.impl.core;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.TrackerResponse;

public class TrackerResponseImpl implements TrackerResponse {
  ImmutableList<Peer> peers;

  public TrackerResponseImpl(ImmutableList<Peer> peers) {
    this.peers = Preconditions.checkNotNull(peers);
  }

  public ImmutableList<Peer> getPeers() {
    return peers;
  }
}
