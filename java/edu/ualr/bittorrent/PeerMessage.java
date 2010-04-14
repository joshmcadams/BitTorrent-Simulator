package edu.ualr.bittorrent;

import com.google.common.base.Preconditions;

import edu.ualr.bittorrent.interfaces.HasPeer;
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
public class PeerMessage<T> implements HasPeer, Message {
  private final Peer peer;
  private final Type type;

  /**
   * Create a new {@link PeerMessage}.
   *
   * @param peer
   *          The {@link Peer} sending the message.
   * @param type
   *          The {@link Type} of message being sent.
   */
  public PeerMessage(Peer peer, Type type) {
    this.peer = Preconditions.checkNotNull(peer);
    this.type = Preconditions.checkNotNull(type);
  }

  /**
   * {@inheritDoc}
   */
  public Peer getPeer() {
    return peer;
  }

  /**
   * {@inheritDoc}
   */
  public Type getType() {
    return type;
  }
}
