package edu.ualr.bittorrent.interfaces.messages;

import edu.ualr.bittorrent.interfaces.HasReceivingPeer;
import edu.ualr.bittorrent.interfaces.HasSendingPeer;
import edu.ualr.bittorrent.interfaces.Message;
import edu.ualr.bittorrent.interfaces.Peer;

/**
 * In BitTorrent, a {@link BitField} message is used to tell a {@link Peer} all
 * of the pieces of data that another {@link Peer} has in a single message.
 * Basically, it is a compact way of sending multiple {@link Have} messages.
 *
 * Objects that will represent a {@link BitField} should implement this
 * interface.
 */
public interface BitField extends HasSendingPeer, HasReceivingPeer, Message {
  /**
   * Return an array of bytes, where each bit in the list represents a piece of
   * data in the torrent. If the bit is 'on', the {@link Peer} who sent the
   * message has the given piece. If the bit is 'off', the {@link Peer} who sent
   * the message does not have the given piece.
   *
   * @return
   */
  public byte[] getBitField();
}
