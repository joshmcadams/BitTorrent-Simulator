package edu.ualr.bittorrent.interfaces.messages;

import edu.ualr.bittorrent.interfaces.HasPeer;

public interface Cancel extends HasPeer {
  public long getPieceIndex();
  public long getBeginningOffset();
  public long getBlockLength();
}
