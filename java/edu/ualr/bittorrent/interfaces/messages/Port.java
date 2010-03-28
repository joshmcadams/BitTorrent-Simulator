package edu.ualr.bittorrent.interfaces.messages;

import edu.ualr.bittorrent.interfaces.HasPeer;

public interface Port extends HasPeer {
  public int getPort();
}
