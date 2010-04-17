package edu.ualr.bittorrent.impl.core.messages;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.Port;

/**
 * Default implementation of the {@link Port} message.
 */
public class PortImpl extends PeerMessage<Port> implements Port {
  final int port;

  /**
   * Create a new port message.
   *
   * @param peer
   * @param port
   */
  public PortImpl(Peer peer, int port) {
    super(peer, PeerMessage.Type.PORT);
    this.port = port;
  }

  /**
   * {@inheritDoc}
   */
  public int getPort() {
    return port;
  }
}
