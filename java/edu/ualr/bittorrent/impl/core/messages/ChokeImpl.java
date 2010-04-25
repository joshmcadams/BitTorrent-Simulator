package edu.ualr.bittorrent.impl.core.messages;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.Choke;

/**
 * Default implementation of the {@link Choke} interface.
 */
public class ChokeImpl extends PeerMessage<Choke> implements Choke {
  private static final Logger logger = Logger.getLogger(ChokeImpl.class);

  /**
   * Create a new choke message.
   *
   * @param peer
   */
  @Inject
  public ChokeImpl(@Assisted("sendingPeer") Peer sendingPeer,
      @Assisted("receivingPeer") Peer receivingPeer) {
    super(sendingPeer, receivingPeer, PeerMessage.Type.CHOKE);
    logger.debug(String.format("[message: %s][from: %s][to: %s]",
        PeerMessage.Type.CHOKE, sendingPeer, receivingPeer));
  }
}
