package edu.ualr.bittorrent.interfaces.messages;

import edu.ualr.bittorrent.interfaces.Peer;

public interface InterestedFactory {
  public Interested create(Peer localPeer);
}
