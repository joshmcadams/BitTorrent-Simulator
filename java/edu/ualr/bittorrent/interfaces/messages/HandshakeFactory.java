package edu.ualr.bittorrent.interfaces.messages;

import com.google.inject.assistedinject.Assisted;

import edu.ualr.bittorrent.interfaces.Peer;

public interface HandshakeFactory {
  public Handshake create(@Assisted("sendingPeer") Peer sendingPeer,
      @Assisted("receivingPeer") Peer receivingPeer, String protocolIdentifier,
      @Assisted("reservedBytes") byte[] reservedBytes,
      @Assisted("infoHash") byte[] infoHash);
}
