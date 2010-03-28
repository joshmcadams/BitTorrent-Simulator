package edu.ualr.bittorrent.interfaces.messages;

import edu.ualr.bittorrent.interfaces.HasPeer;

public interface Piece extends HasPeer {
  public long getPieceIndex();
  public long getBeginningOffset();
  public byte[] getBlock();
}
