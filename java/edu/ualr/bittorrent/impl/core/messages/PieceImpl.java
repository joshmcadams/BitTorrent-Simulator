package edu.ualr.bittorrent.impl.core.messages;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.Piece;

public class PieceImpl extends PeerMessage<Piece> implements Piece {
  public PieceImpl(Peer peer) {
    super(peer, PeerMessage.Type.PIECE);
  }

  public long getBeginningOffset() {
    // TODO Auto-generated method stub
    return 0;
  }

  public byte[] getBlock() {
    // TODO Auto-generated method stub
    return null;
  }

  public long getPieceIndex() {
    // TODO Auto-generated method stub
    return 0;
  }
}
