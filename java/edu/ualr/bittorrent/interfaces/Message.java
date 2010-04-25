package edu.ualr.bittorrent.interfaces;

import org.joda.time.Instant;

import edu.ualr.bittorrent.interfaces.messages.BitField;
import edu.ualr.bittorrent.interfaces.messages.Cancel;
import edu.ualr.bittorrent.interfaces.messages.Choke;
import edu.ualr.bittorrent.interfaces.messages.Handshake;
import edu.ualr.bittorrent.interfaces.messages.Interested;
import edu.ualr.bittorrent.interfaces.messages.KeepAlive;
import edu.ualr.bittorrent.interfaces.messages.NotInterested;
import edu.ualr.bittorrent.interfaces.messages.Piece;

/**
 * Interface that should be implemented by any object that contains a message
 * that will be shared between {@link Peer} objects. This interface references
 * the messages specified in the original BitTorrent specification:
 *
 * <ul>
 * <li>BIT_FIELD: {@link BitField}</li>
 * <li>CANCEL: {@link Cancel}</li>
 * <li>CHOKE: {@link Choke}</li>
 * <li>HANDSHAKE: {@link Handshake}</li>
 * <li>INTERESTED: {@link Interested}</li>
 * <li>KEEP_ALIVE: {@link KeepAlive}</li>
 * <li>NOT_INTERESTED: {@link NotInterested}</li>
 * <li>PIECE: {@link Piece}</li>
 * <li>PORT: {@link Port}</li>
 * <li>REQUEST: {@link Request}</li>
 * <li>UNCHOKE: {@link UnChoke}</li>
 * </ul>
 */
public interface Message extends HasSendingPeer, HasReceivingPeer {
  /**
   * Types of messages detailed in the BitTorrent specification.
   */
  public enum Type {
    BIT_FIELD, CANCEL, CHOKE, HANDSHAKE, HAVE, INTERESTED, KEEP_ALIVE, NOT_INTERESTED, PIECE, PORT, REQUEST, UNCHOKE
  };

  /**
   * Return the {@link Type} of the current message.
   *
   * @return
   */
  public Type getType();

  /**
   * Return the {@link Instant} that the message was sent.
   *
   * @return
   */
  public Instant getSentTime();
}
