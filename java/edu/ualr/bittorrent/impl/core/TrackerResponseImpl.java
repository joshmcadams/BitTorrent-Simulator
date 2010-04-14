package edu.ualr.bittorrent.impl.core;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.TrackerResponse;

public class TrackerResponseImpl implements TrackerResponse {
  final byte[] trackerId;
  final ImmutableList<Peer> peers;
  final int complete;
  final int incomplete;
  final int interval;
  final Integer minInterval;
  final String failureReason;
  final String warningMessage;

  public TrackerResponseImpl(byte[] trackerId, ImmutableList<Peer> peers,
      int complete, int incomplete, int interval) {
    this(trackerId, peers, complete, incomplete, interval, null, null, null);
  }

  public TrackerResponseImpl(byte[] trackerId, ImmutableList<Peer> peers,
      int complete, int incomplete, int interval, Integer minInterval,
      String failureReason, String warningMessage) {
    this.trackerId = Preconditions.checkNotNull(trackerId);
    this.peers = Preconditions.checkNotNull(peers);
    this.complete = Preconditions.checkNotNull(complete);
    this.incomplete = Preconditions.checkNotNull(incomplete);
    this.interval = Preconditions.checkNotNull(interval);
    this.minInterval = minInterval;
    this.failureReason = failureReason;
    this.warningMessage = warningMessage;
  }

  public ImmutableList<Peer> getPeers() {
    return peers;
  }

  public int getComplete() {
    return complete;
  }

  public String getFailureReason() {
    return failureReason;
  }

  public int getIncomplete() {
    return incomplete;
  }

  public int getInterval() {
    return interval;
  }

  public Integer getMinInterval() {
    return minInterval;
  }

  public byte[] getTrackerId() {
    return trackerId;
  }

  public String getWarningMessage() {
    return warningMessage;
  }
}
