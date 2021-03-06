package edu.ualr.bittorrent.impl.core.messages;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.Port;

/**
 * Default implementation of the {@link Port} message.
 */
public class PortImpl extends PeerMessage<Port> implements Port {
  final int port;
  private static final Logger logger = Logger.getLogger(PortImpl.class);

  /**
   * Create a new port message.
   *
   * @param peer
   * @param port
   */
  @Inject
  public PortImpl(@Assisted("sendingPeer") Peer sendingPeer,
      @Assisted("receivingPeer") Peer receivingPeer, @Assisted int port) {
    super(sendingPeer, receivingPeer, PeerMessage.Type.PORT);
    this.port = port;
    logger.debug(String.format("[message: %s][from: %s][to: %s]",
        PeerMessage.Type.PORT, sendingPeer, receivingPeer));
  }

  /**
   * {@inheritDoc}
   */
  public int getPort() {
    return port;
  }
}
