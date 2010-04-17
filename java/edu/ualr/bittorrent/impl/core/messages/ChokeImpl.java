package edu.ualr.bittorrent.impl.core.messages;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.Choke;

/**
 * Default implementation of the {@link Choke} interface.
 */
public class ChokeImpl extends PeerMessage<Choke> implements Choke {
  /**
   * Create a new choke message.
   *
   * @param peer
   */
  public ChokeImpl(Peer peer) {
    super(peer, PeerMessage.Type.CHOKE);
  }
}
