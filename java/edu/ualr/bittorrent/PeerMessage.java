package edu.ualr.bittorrent;

import org.joda.time.Instant;

import com.google.common.base.Preconditions;

import edu.ualr.bittorrent.interfaces.HasPeer;
import edu.ualr.bittorrent.interfaces.HasReceivingPeer;
import edu.ualr.bittorrent.interfaces.HasSendingPeer;
import edu.ualr.bittorrent.interfaces.Message;
import edu.ualr.bittorrent.interfaces.Peer;

/**
 * BitTorrent peers exchange messages. Most are documented, some are
 * experimental and simply ignored by standard clients. Regardless, all should
 * inherit from this class if they can. If not, they should implement the
 * {@link Message} and {@link HasPeer} interface so that they are compatible
 * with this simulator.
 *
 * @param <T>
 *          The type of message.
 */
public class PeerMessage<T> implements HasSendingPeer, HasReceivingPeer, Message {
  private final Peer sendingPeer;
  private final Peer receivingPeer;
  private final Type type;
  private final Instant sentTime;

  /**
   * Create a new {@link PeerMessage}.
   *
   * @param peer
   *          The {@link Peer} sending the message.
   * @param type
   *          The {@link Type} of message being sent.
   */
  public PeerMessage(Peer sendingPeer, Peer receivingPeer, Type type) {
    this(sendingPeer, receivingPeer, type, new Instant());
  }

  public PeerMessage(Peer sendingPeer, Peer receivingPeer, Type type, Instant sentTime) {
    this.sendingPeer = Preconditions.checkNotNull(sendingPeer);
    this.receivingPeer = Preconditions.checkNotNull(receivingPeer);
    this.type = Preconditions.checkNotNull(type);
    this.sentTime = Preconditions.checkNotNull(sentTime);
  }

  /**
   * {@inheritDoc}
   */
  public Peer getSendingPeer() {
    return sendingPeer;
  }

  /**
   * {@inheritDoc}
   */
  public Peer getReceivingPeer() {
    return receivingPeer;
  }

  /**
   * {@inheritDoc}
   */
  public Type getType() {
    return type;
  }

  public Instant getSentTime() {
    return sentTime;
  }
}
