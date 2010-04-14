package edu.ualr.bittorrent.interfaces;

import com.google.common.collect.ImmutableList;

public interface TrackerResponse {
  public String getFailureReason();

  public String getWarningMessage();

  public int getInterval();

  public Integer getMinInterval();

  public byte[] getTrackerId();

  public int getComplete();

  public int getIncomplete();

  public ImmutableList<Peer> getPeers();
}
