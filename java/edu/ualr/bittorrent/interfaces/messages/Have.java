package edu.ualr.bittorrent.interfaces.messages;

import edu.ualr.bittorrent.interfaces.HasPeer;

public interface Have extends HasPeer {
  public long getPieceIndex();
}
