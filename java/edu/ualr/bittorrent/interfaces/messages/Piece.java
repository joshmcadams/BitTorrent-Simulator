package edu.ualr.bittorrent.interfaces.messages;

import edu.ualr.bittorrent.interfaces.HasPeer;
import edu.ualr.bittorrent.interfaces.Message;
import edu.ualr.bittorrent.interfaces.Peer;

/**
 * Once a {@link Peer} receives a {@link Request} message, it can elect to honor
 * the {@link Request} and send the data. If this happens, that data is sent via
 * a {@link Piece} message.
 *
 * Objects that act as {@link Piece} messages should implement this interface.
 */
public interface Piece extends HasPeer, Message {
  /**
   * Return the index (zero-based) of the piece that was requested.
   *
   * @return
   */
  public int getPieceIndex();

  /**
   * Return the offset (zero-based) within the piece that the data begins.
   *
   * @return
   */
  public int getBeginningOffset();

  /**
   * Return the data.
   *
   * @return
   */
  public byte[] getBlock();
}
