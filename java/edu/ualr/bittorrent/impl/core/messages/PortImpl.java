package edu.ualr.bittorrent.impl.core.messages;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.Port;

public class PortImpl extends PeerMessage<Port> implements Port {
  final int port;

  public PortImpl(Peer peer, int port) {
    super(peer, PeerMessage.Type.PORT);
    this.port = port;
  }

  public int getPort() {
    return port;
  }
}
