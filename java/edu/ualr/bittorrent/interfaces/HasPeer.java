package edu.ualr.bittorrent.interfaces;

/**
 * When an object contains another object that implements the {@link Peer}
 * interface, the containing object should implement this interface in order to
 * expose the contained {@link Peer}.
 */
public interface HasPeer {
  /**
   * Return the referenced {@link Peer}.
   *
   * @return
   */
  public Peer getPeer();
}
