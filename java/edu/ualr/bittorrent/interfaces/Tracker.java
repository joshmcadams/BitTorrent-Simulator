package edu.ualr.bittorrent.interfaces;

/**
 * In a BitTorrent swarm, the {@link Tracker} or {@link Tracker}s are
 * responsible for keeping {@link Peer}s visible to one-another. The
 * {@link Tracker} repeatedly receives requests from {@link Peer}s letting the
 * {@link Tracker} know their progress and asking the {@link Tracker} for more
 * {@link Peer}s that they can share with.
 *
 * Objects that will serve at {@link Tracker}s should implement this interface.
 */
public interface Tracker extends Runnable {
  /**
   * Respond to a request from a {@link Peer}.
   *
   * @param request
   * @return
   */
  public TrackerResponse get(TrackerRequest request);
};
