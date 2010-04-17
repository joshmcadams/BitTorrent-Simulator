package edu.ualr.bittorrent.interfaces;

import com.google.common.collect.ImmutableList;

/**
 * When a {@link Tracker} receives a {@link TrackerRequest} from a {@link Peer},
 * the {@link Tracker} should respond to that {@link TrackerRequest} with a
 * {@link TrackerResponse}. Objects that will serve as a response should
 * implement this interface.
 */
public interface TrackerResponse {
  /**
   * If the {@link TrackerRequest} triggered some sort of failure, the reason
   * for that failure should be returned via this method.
   *
   * @return
   */
  public String getFailureReason();

  /**
   * If the {@link TrackerRequest} triggered some sort of warning, the reason
   * for that warning should be returned via this method.
   *
   * @return
   */
  public String getWarningMessage();

  /**
   * The {@link Tracker} should let the {@link Peer} know how often to contact
   * the {@link Tracker}. This method returns that interval in XXX.
   *
   * TODO: what are the units of the interval.
   *
   * @return
   */
  public int getInterval();

  /**
   * The minimum interval that a {@link Peer} should wait in between requests to
   * the {@link Tracker}.
   *
   * TODO: what are the units of the interval
   *
   * @return
   */
  public Integer getMinInterval();

  /**
   * Return the unique ID of the {@link Tracker}.
   *
   * @return
   */
  public byte[] getTrackerId();

  /**
   * Return a count of the number of seeders in the swarm.
   *
   * @return
   */
  public int getComplete();

  /**
   * Return a count of the number of leechers in the swarm.
   *
   * @return
   */
  public int getIncomplete();

  /**
   * Return a list of {@link Peer}s that the requesting {@link Peer} can begin
   * to communicate with.
   *
   * @return
   */
  public ImmutableList<Peer> getPeers();
}
