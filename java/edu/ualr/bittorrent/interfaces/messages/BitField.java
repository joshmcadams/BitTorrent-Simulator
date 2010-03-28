package edu.ualr.bittorrent.interfaces.messages;

import edu.ualr.bittorrent.interfaces.HasPeer;

public interface BitField extends HasPeer {
  public byte[] getBitField();
}
