package edu.ualr.bittorrent.interfaces.messages;

import edu.ualr.bittorrent.interfaces.HasPeer;

public interface Request extends HasPeer {
  public int getPieceIndex();
  public int getBeginningOffset();
  public int getBlockLength();
}
