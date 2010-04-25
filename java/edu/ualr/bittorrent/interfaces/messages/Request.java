package edu.ualr.bittorrent.interfaces.messages;

import edu.ualr.bittorrent.interfaces.HasReceivingPeer;
import edu.ualr.bittorrent.interfaces.HasSendingPeer;
import edu.ualr.bittorrent.interfaces.Message;
import edu.ualr.bittorrent.interfaces.Peer;

/**
 * When a {@link Peer} is unchoked and one of its {@link Peer}s has data that
 * the local {@link Peer} needs, the local {@link Peer} can request the data via
 * a {@link Request} message.
 *
 * Objects that serve as {@link Request} messages should implment this
 * interface.
 */
public interface Request extends HasSendingPeer, HasReceivingPeer, Message {
  /**
   * Return the index (zero-based) of the requested piece.
   *
   * @return
   */
  public int getPieceIndex();

  /**
   * Return the offset (zero-based) within the piece that the requested data
   * should start from.
   *
   * @return
   */
  public int getBeginningOffset();

  /**
   * Return the length of data within the piece that is requested.
   *
   * @return
   */
  public int getBlockLength();
}
