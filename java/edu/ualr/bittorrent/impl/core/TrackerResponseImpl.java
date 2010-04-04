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

  public Long getComplete() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getFailureReason() {
    // TODO Auto-generated method stub
    return null;
  }

  public Long getIncomplete() {
    // TODO Auto-generated method stub
    return null;
  }

  public Integer getInterval() {
    // TODO Auto-generated method stub
    return null;
  }

  public Integer getMinInterval() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getTrackerId() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getWarningMessage() {
    // TODO Auto-generated method stub
    return null;
  }
}
