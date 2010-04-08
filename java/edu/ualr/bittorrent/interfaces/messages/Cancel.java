package edu.ualr.bittorrent.interfaces.messages;

import edu.ualr.bittorrent.interfaces.HasPeer;

public interface Cancel extends HasPeer {
  public int getPieceIndex();
  public int getBeginningOffset();
  public int getBlockLength();
}
