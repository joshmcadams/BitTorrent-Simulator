package edu.ualr.bittorrent.interfaces.messages;

import edu.ualr.bittorrent.interfaces.HasPeer;
import edu.ualr.bittorrent.interfaces.Message;

public interface Piece extends HasPeer, Message {
  public int getPieceIndex();

  public int getBeginningOffset();

  public byte[] getBlock();
}
