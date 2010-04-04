package edu.ualr.bittorrent.interfaces;

import com.google.common.collect.ImmutableList;

public interface TrackerResponse {
  public String getFailureReason();
  public String getWarningMessage();
  public Integer getInterval();
  public Integer getMinInterval();
  public byte[] getTrackerId();
  public Long getComplete();
  public Long getIncomplete();
  public ImmutableList<Peer> getPeers();
}
