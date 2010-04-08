package edu.ualr.bittorrent.impl.core.messages;

import com.google.common.base.Preconditions;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.Piece;

public class PieceImpl extends PeerMessage<Piece> implements Piece {
  final int beginningOffset;
  final byte[] block;
  final int pieceIndex;

  public PieceImpl(Peer peer, int pieceIndex, int beginningOffset, byte[] block) {
    super(peer, PeerMessage.Type.PIECE);
    this.pieceIndex = pieceIndex;
    this.beginningOffset = beginningOffset;
    this.block = Preconditions.checkNotNull(block);
  }

  public int getBeginningOffset() {
    return beginningOffset;
  }

  public byte[] getBlock() {
    return block;
  }

  public int getPieceIndex() {
    return pieceIndex;
  }
}
