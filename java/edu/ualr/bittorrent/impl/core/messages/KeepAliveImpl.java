package edu.ualr.bittorrent.impl.core.messages;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.KeepAlive;

/**
 * Default implementation of the {@link KeepAlive} interface.
 */
public class KeepAliveImpl extends PeerMessage<KeepAlive> implements KeepAlive {
  private static final Logger logger = Logger.getLogger(KeepAliveImpl.class);

  /**
   * Create a new keep alive message.
   *
   * @param peer
   */
  @Inject
  public KeepAliveImpl(@Assisted("sendingPeer") Peer sendingPeer,
      @Assisted("receivingPeer") Peer receivingPeer) {
    super(sendingPeer, receivingPeer, PeerMessage.Type.KEEP_ALIVE);
    logger.debug(String.format("[message: %s][from: %s][to: %s]",
        PeerMessage.Type.KEEP_ALIVE, sendingPeer, receivingPeer));
  }
}
