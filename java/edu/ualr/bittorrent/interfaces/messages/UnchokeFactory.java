package edu.ualr.bittorrent.interfaces.messages;

import edu.ualr.bittorrent.interfaces.Peer;

public interface UnchokeFactory {
  public Unchoke create(Peer localPeer);
}
