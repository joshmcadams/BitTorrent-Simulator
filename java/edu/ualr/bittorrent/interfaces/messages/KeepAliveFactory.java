package edu.ualr.bittorrent.interfaces.messages;

import edu.ualr.bittorrent.interfaces.Peer;

public interface KeepAliveFactory {
  public KeepAlive create(Peer localPeer);
}
