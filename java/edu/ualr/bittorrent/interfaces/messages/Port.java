package edu.ualr.bittorrent.interfaces.messages;

import edu.ualr.bittorrent.interfaces.HasPeer;
import edu.ualr.bittorrent.interfaces.Message;
import edu.ualr.bittorrent.interfaces.Peer;

/**
 * {@link Peer}s can request communication on a specific port via a {@link Port}
 * message.
 *
 * Objects that wish to serve as {@link Port} messages should implement this
 * interface.
 */
public interface Port extends HasPeer, Message {
  /**
   * Return the desired port of communication.
   *
   * @return
   */
  public int getPort();
}
