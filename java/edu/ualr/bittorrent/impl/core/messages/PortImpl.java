package edu.ualr.bittorrent.impl.core.messages;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.Port;

public class PortImpl extends PeerMessage<Port> implements Port {
  public PortImpl(Peer peer) {
    super(peer, PeerMessage.Type.PORT);
  }

  public int getPort() {
    // TODO Auto-generated method stub
    return 0;
  }
}
