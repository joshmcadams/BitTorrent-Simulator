package edu.ualr.bittorrent.interfaces.messages;

import edu.ualr.bittorrent.interfaces.Peer;

public interface PortFactory {
  public Port create(Peer localPeer, int port);
}
