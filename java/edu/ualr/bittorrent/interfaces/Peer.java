package edu.ualr.bittorrent.interfaces;

/**
 * A peer is a participant in a BitTorrent swarm that either seeds or leeches
 * from the swarm. Objects that will be peers in the swarm need to implement
 * this interface, including a threadable {@code run} method.
 */
public interface Peer extends Runnable {
  /**
   * Return the unique ID of this peer.
   *
   * @return
   */
  public byte[] getId();

  /**
   * Give the peer the {@link Metainfo} that it is working with.
   *
   * @param metainfo
   */
  public void setMetainfo(Metainfo metainfo);

  /**
   * Send the peer a {@link Message}.
   *
   * @param message
   */
  public void message(Message message);
}
