package edu.ualr.bittorrent.impl.core.messages;

import com.google.common.base.Preconditions;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.Piece;

/**
 * Default implementation of the {@link Piece} interface.
 */
public class PieceImpl extends PeerMessage<Piece> implements Piece {
  final int beginningOffset;
  final byte[] block;
  final int pieceIndex;

  /**
   * Create a new piece message.
   *
   * @param peer
   * @param pieceIndex
   * @param beginningOffset
   * @param block
   */
  public PieceImpl(Peer peer, int pieceIndex, int beginningOffset, byte[] block) {
    super(peer, PeerMessage.Type.PIECE);
    this.pieceIndex = pieceIndex;
    this.beginningOffset = beginningOffset;
    this.block = Preconditions.checkNotNull(block);
  }

  /**
   * {@inheritDoc}
   */
  public int getBeginningOffset() {
    return beginningOffset;
  }

  /**
   * {@inheritDoc}
   */
  public byte[] getBlock() {
    return block;
  }

  /**
   * {@inheritDoc}
   */
  public int getPieceIndex() {
    return pieceIndex;
  }
}
