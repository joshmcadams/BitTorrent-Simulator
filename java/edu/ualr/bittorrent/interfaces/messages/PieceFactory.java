package edu.ualr.bittorrent.interfaces.messages;

import com.google.inject.assistedinject.Assisted;

import edu.ualr.bittorrent.interfaces.Peer;

public interface PieceFactory {
  public Piece create(@Assisted("sendingPeer") Peer sendingPeer,
      @Assisted("receivingPeer") Peer receivingPeer,
      @Assisted("pieceIndex") int pieceIndex,
      @Assisted("beginningOffset") int beginningOffset, byte[] block);
}
