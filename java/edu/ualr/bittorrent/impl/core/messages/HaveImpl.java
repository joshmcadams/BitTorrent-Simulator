package edu.ualr.bittorrent.impl.core.messages;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.Have;

public class HaveImpl extends PeerMessage<Have> implements Have {
  final int pieceIndex;

  public HaveImpl(Peer peer, int pieceIndex) {
    super(peer, PeerMessage.Type.HAVE);
    this.pieceIndex = pieceIndex;
  }

  public long getPieceIndex() {
    return pieceIndex;
  }
}
