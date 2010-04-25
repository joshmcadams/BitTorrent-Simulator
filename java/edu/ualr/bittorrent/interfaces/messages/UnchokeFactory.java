package edu.ualr.bittorrent.interfaces.messages;

import com.google.inject.assistedinject.Assisted;

import edu.ualr.bittorrent.interfaces.Peer;

public interface UnchokeFactory {
  public Unchoke create(@Assisted("sendingPeer") Peer sendingPeer,
      @Assisted("receivingPeer") Peer receivingPeer);
}
