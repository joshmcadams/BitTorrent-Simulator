package edu.ualr.bittorrent.interfaces.messages;

import edu.ualr.bittorrent.interfaces.HasPeer;
import edu.ualr.bittorrent.interfaces.Message;

public interface Request extends HasPeer, Message {
  public int getPieceIndex();
  public int getBeginningOffset();
  public int getBlockLength();
}
