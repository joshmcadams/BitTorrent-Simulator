package edu.ualr.bittorrent.interfaces.messages;

import edu.ualr.bittorrent.interfaces.HasPeer;
import edu.ualr.bittorrent.interfaces.Message;
import edu.ualr.bittorrent.interfaces.Peer;

/**
 * When a {@link Peer} receives a {@link Piece} that contains valid data, it
 * should send a {@link Cancel} to any other {@link Peer}s that it sent
 * {@link Request}s to for the same {@link Piece}.
 *
 * Objects that act as the {@link Cancel} message should implement this
 * interface.
 */
public interface Cancel extends HasPeer, Message {
  /**
   * Return the index of the piece (zero-offset) that was requested.
   *
   * @return
   */
  public int getPieceIndex();

  /**
   * Since portions of a piece of data can be requested, it is necessary to
   * specify the offset within the piece that the data should come from. This
   * returns that offset.
   *
   * @return
   */
  public int getBeginningOffset();

  /**
   * Since portions of a piece of data can be requested, it is necessary to
   * specify the length of data that should be returned. This returns that
   * length.
   *
   * @return
   */
  public int getBlockLength();
}
