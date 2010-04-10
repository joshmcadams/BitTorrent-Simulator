package edu.ualr.bittorrent.interfaces.messages;

import edu.ualr.bittorrent.interfaces.HasPeer;
import edu.ualr.bittorrent.interfaces.Message;

public interface Have extends HasPeer, Message {
  public long getPieceIndex();
}
