package edu.ualr.bittorrent.interfaces.messages;

import edu.ualr.bittorrent.interfaces.Peer;

public interface HaveFactory {
  public Have create(Peer localPeer, int pieceIndex);
}