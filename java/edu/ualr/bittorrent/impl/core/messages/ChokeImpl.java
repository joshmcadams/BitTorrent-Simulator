package edu.ualr.bittorrent.impl.core.messages;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

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
  @Inject
  public ChokeImpl(@Assisted("sendingPeer") Peer sendingPeer,
      @Assisted("receivingPeer") Peer receivingPeer) {
    super(sendingPeer, receivingPeer, PeerMessage.Type.CHOKE);
  }
}
