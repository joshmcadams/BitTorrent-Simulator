package edu.ualr.bittorrent.impl.core.messages;

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
  public KeepAliveImpl(Peer peer) {
    super(peer, PeerMessage.Type.KEEP_ALIVE);
  }
}
