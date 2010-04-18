package edu.ualr.bittorrent.interfaces.messages;

import edu.ualr.bittorrent.interfaces.Peer;

public interface BitFieldFactory {
  public BitField create(Peer localPeer, byte[] bitfield);
}
