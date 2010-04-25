package edu.ualr.bittorrent.impl.core.messages;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.KeepAlive;

/**
 * Default implementation of the {@link KeepAlive} interface.
 */
public class KeepAliveImpl extends PeerMessage<KeepAlive> implements KeepAlive {
  /**
   * Create a new keep alive message.
   *
   * @param peer
   */
  @Inject
  public KeepAliveImpl(@Assisted("sendingPeer") Peer sendingPeer,
      @Assisted("receivingPeer") Peer receivingPeer) {
    super(sendingPeer, receivingPeer, PeerMessage.Type.KEEP_ALIVE);
  }
}
