package edu.ualr.bittorrent.interfaces.messages;

import edu.ualr.bittorrent.interfaces.Peer;

public interface NotInterestedFactory {
  public NotInterested create(Peer localPeer);
}
