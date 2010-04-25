package edu.ualr.bittorrent.impl.core.messages;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.NotInterested;

/**
 * Default implementation of the {@link NotInterested} interface.
 */
public class NotInterestedImpl extends PeerMessage<NotInterested> implements
    NotInterested {
  private static final Logger logger = Logger.getLogger(NotInterestedImpl.class);

  /**
   * Create a new not interested message.
   *
   * @param peer
   */
  @Inject
  public NotInterestedImpl(@Assisted("sendingPeer") Peer sendingPeer,
      @Assisted("receivingPeer") Peer receivingPeer) {
    super(sendingPeer, receivingPeer, PeerMessage.Type.NOT_INTERESTED);
    logger.debug(String.format("[message: %s][from: %s][to: %s]",
        PeerMessage.Type.NOT_INTERESTED, sendingPeer, receivingPeer));
  }
}
