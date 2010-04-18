package edu.ualr.bittorrent.impl.core;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.internal.Nullable;

import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.TrackerResponse;

/**
 * Default implementation of the {@link TrackerResponse} interface.
 */
public class TrackerResponseImpl implements TrackerResponse {
  final byte[] trackerId;
  final ImmutableList<Peer> peers;
  final int complete;
  final int incomplete;
  final int interval;
  final Integer minInterval;
  final String failureReason;
  final String warningMessage;

  /**
   * Create a new tracker response.
   *
   * @param trackerId
   * @param peers
   * @param complete
   * @param incomplete
   * @param interval
   */
  public TrackerResponseImpl(byte[] trackerId, ImmutableList<Peer> peers,
      int complete, int incomplete, int interval) {
    this(trackerId, peers, complete, incomplete, interval, null, null, null);
  }

  /**
   * Create a new tracker response.
   *
   * @param trackerId
   * @param peers
   * @param complete
   * @param incomplete
   * @param interval
   * @param minInterval
   * @param failureReason
   * @param warningMessage
   */
  @Inject
  public TrackerResponseImpl(@Assisted("trackerId") byte[] trackerId,
      @Assisted("peers") ImmutableList<Peer> peers,
      @Assisted("seederCount") int complete,
      @Assisted("leecherCount") int incomplete,
      @Assisted("interval") int interval,
      @Nullable @Assisted("minInterval") Integer minInterval,
      @Nullable @Assisted("failureMessage") String failureReason,
      @Nullable @Assisted("warningMessage") String warningMessage) {
    this.trackerId = Preconditions.checkNotNull(trackerId);
    this.peers = Preconditions.checkNotNull(peers);
    this.complete = Preconditions.checkNotNull(complete);
    this.incomplete = Preconditions.checkNotNull(incomplete);
    this.interval = Preconditions.checkNotNull(interval);
    this.minInterval = minInterval;
    this.failureReason = failureReason;
    this.warningMessage = warningMessage;
  }

  /**
   * {@inheritDoc}
   */
  public ImmutableList<Peer> getPeers() {
    return peers;
  }

  /**
   * {@inheritDoc}
   */
  public int getComplete() {
    return complete;
  }

  /**
   * {@inheritDoc}
   */
  public String getFailureReason() {
    return failureReason;
  }

  /**
   * {@inheritDoc}
   */
  public int getIncomplete() {
    return incomplete;
  }

  /**
   * {@inheritDoc}
   */
  public int getInterval() {
    return interval;
  }

  /**
   * {@inheritDoc}
   */
  public Integer getMinInterval() {
    return minInterval;
  }

  /**
   * {@inheritDoc}
   */
  public byte[] getTrackerId() {
    return trackerId;
  }

  /**
   * {@inheritDoc}
   */
  public String getWarningMessage() {
    return warningMessage;
  }
}
