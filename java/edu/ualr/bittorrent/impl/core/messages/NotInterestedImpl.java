package edu.ualr.bittorrent.impl.core.messages;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.NotInterested;

/**
 * Default implementation of the {@link NotInterested} interface.
 */
public class NotInterestedImpl extends PeerMessage<NotInterested> implements
    NotInterested {
  /**
   * Create a new not interested message.
   *
   * @param peer
   */
  public NotInterestedImpl(Peer peer) {
    super(peer, PeerMessage.Type.NOT_INTERESTED);
  }
}
