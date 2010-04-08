package edu.ualr.bittorrent.interfaces.messages;

import edu.ualr.bittorrent.interfaces.HasPeer;

public interface Piece extends HasPeer {
  public int getPieceIndex();
  public int getBeginningOffset();
  public byte[] getBlock();
}
