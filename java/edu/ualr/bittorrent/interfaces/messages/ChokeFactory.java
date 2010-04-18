package edu.ualr.bittorrent.interfaces.messages;

import edu.ualr.bittorrent.interfaces.Peer;

public interface ChokeFactory {
  public Choke create(Peer localPeer);
}
