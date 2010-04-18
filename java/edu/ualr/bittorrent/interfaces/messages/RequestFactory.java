package edu.ualr.bittorrent.interfaces.messages;

import com.google.inject.assistedinject.Assisted;

import edu.ualr.bittorrent.interfaces.Peer;

public interface RequestFactory {
  public Request create(Peer localPeer, @Assisted("pieceIndex") int pieceIndex,
      @Assisted("beginningOffset") int beginningOffset,
      @Assisted("blockLength") int blockLength);
}
