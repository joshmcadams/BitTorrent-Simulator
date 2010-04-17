package edu.ualr.bittorrent.interfaces.messages;

import edu.ualr.bittorrent.interfaces.HasPeer;
import edu.ualr.bittorrent.interfaces.Message;
import edu.ualr.bittorrent.interfaces.Peer;

/**
 * Once a {@link Peer} receives a new and valid {@link Piece}, the {@link Peer}
 * should announce this to all of its peers via a {@link Have} message.
 *
 * Objects that will serve as the {@link Have} message should implement this
 * interface.
 */
public interface Have extends HasPeer, Message {
  /**
   * Return the index (zero-based) of the new piece.
   *
   * @return
   */
  public int getPieceIndex();
}
