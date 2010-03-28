package edu.ualr.bittorrent.impl.core.messages;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.KeepAlive;

public class KeepAliveImpl extends PeerMessage<KeepAlive> implements KeepAlive {
  public KeepAliveImpl(Peer peer) {
    super(peer, PeerMessage.Type.KEEP_ALIVE);
  }
}
