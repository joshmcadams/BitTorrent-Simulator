package edu.ualr.bittorrent.interfaces;

import com.google.common.collect.ImmutableList;

public interface TrackerResponse {
  public ImmutableList<Peer> getPeers();
}
