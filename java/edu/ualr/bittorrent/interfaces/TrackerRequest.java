package edu.ualr.bittorrent.interfaces;

/**
 * When a {@link Peer} communicates with a {@link Tracker}, it sends the
 * {@link Tracker} an object that implements the {@link TrackerRequest}
 * interface.
 */
public interface TrackerRequest extends HasPeer {
  /**
   * Enumeration of the lifecycle of a {@link Peer}. All requests made until the
   * full file in the torrent is downloaded are {@code STARTED}. After that, the
   * {@link Peer} can announce that it is done and begin to seed by sending a
   * {@code COMPLETED} request. When the {@link Peer} is ready to leave the
   * swarm, it can send a {@code STOPPED} request to let the {@link Tracker}
   * know that it is done.
   */
  public enum Event {
    STARTED, STOPPED, COMPLETED
  };

  /**
   * Return the info hash from the {@link Metainfo}. This lets the
   * {@link Tracker} know that you are in a particular swarm.
   *
   * @return
   */
  public byte[] getInfoHash();

  /**
   * Return the port that the {@link Peer} would like to communicate on. For the
   * most part, this is a useless operation in this simulator; however, it is
   * useful in the real torrent environment and might prove to be useful in an
   * eventual experiment.
   *
   * @return
   */
  public Integer getPort();

  /**
   * Return the number of bytes that the {@link Peer} has uploaded to the swarm.
   *
   * @return
   */
  public int getUploaded();

  /**
   * Return the number of bytes that the {@link Peer} has downloaded from the
   * swarm.
   *
   * @return
   */
  public int getDownloaded();

  /**
   * Return the number of bytes that the {@link Peer} still needs to download
   * before the entire torrent is downloaded.
   *
   * @return
   */
  public int getLeft();

  /**
   * Return true if the {@link Peer} will accept a compact response from the
   * {@link Tracker}. In this simulator, compact responses are not really used
   * since a raw bencoded response isn't returned from the {@link Tracker};
   * however, this can be useful for experiments where you want to measure
   * potential network traffic.
   *
   * @return
   */
  public Boolean acceptsCompactResponses();

  /**
   * Return true if the {@link Tracker} can omit {@link Peer} IDs from the
   * response. Since responses will typically come as {@link TrackerResponse}s
   * in this simulator, this method typically isn't useful; however, it might
   * have some application when measuring potential network traffic.
   *
   * @return
   */
  public Boolean omitPeerId();

  /**
   * Return the {@link Event} that the {@link Peer} claims to be in.
   *
   * @return
   */
  public Event getEvent();

  /**
   * Return the IP address for the {@link Peer}. This is a field that has little
   * application within this simulator, but might be used for measuring some
   * network effect.
   *
   * @return
   */
  public String getIp();

  /**
   * Return the number of {@link Peer}s that the requesting {@link Peer} would
   * like in the {@link TrackerResponse}.
   *
   * @return
   */
  public Integer getNumWant();

  /**
   * Return the key.
   *
   * @return
   */
  public String getKey();

  /**
   * Return the ID of the {@link Tracker} as provided by the {@link Tracker} in
   * a previous {@link TrackerResponse}.
   *
   * @return
   */
  public byte[] getTrackerId();
}
