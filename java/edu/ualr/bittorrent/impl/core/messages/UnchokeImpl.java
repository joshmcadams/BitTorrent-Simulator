package edu.ualr.bittorrent.impl.core.messages;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.Unchoke;

/**
 * Default implementation of the {@link Unchoke} interface.
 */
public class UnchokeImpl extends PeerMessage<Unchoke> implements Unchoke {
  private static final Logger logger = Logger.getLogger(UnchokeImpl.class);

  /**
   * Create a new unchoke message.
   *
   * @param peer
   */
  @Inject
  public UnchokeImpl(@Assisted("sendingPeer") Peer sendingPeer,
      @Assisted("receivingPeer") Peer receivingPeer) {
    super(sendingPeer, receivingPeer, PeerMessage.Type.UNCHOKE);
    logger.debug(String.format("[message: %s][from: %s][to: %s]",
        PeerMessage.Type.UNCHOKE, sendingPeer, receivingPeer));
  }
}
