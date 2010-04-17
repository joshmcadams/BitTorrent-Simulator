package edu.ualr.bittorrent.impl.core.messages;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.Interested;

/**
 * Default implementation of the {@link Interested} interface.
 */
public class InterestedImpl extends PeerMessage<Interested> implements
    Interested {
  /**
   * Create a new interested message.
   *
   * @param peer
   */
  public InterestedImpl(Peer peer) {
    super(peer, PeerMessage.Type.INTERESTED);
  }
}
