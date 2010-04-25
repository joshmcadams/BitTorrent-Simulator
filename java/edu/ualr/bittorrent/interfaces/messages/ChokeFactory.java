package edu.ualr.bittorrent.interfaces.messages;

import com.google.inject.assistedinject.Assisted;

import edu.ualr.bittorrent.interfaces.Peer;

public interface ChokeFactory {
  public Choke create(@Assisted("sendingPeer") Peer sendingPeer,
      @Assisted("receivingPeer") Peer receivingPeer);
}
