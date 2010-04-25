package edu.ualr.bittorrent.impl.core.messages;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

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
  private static final Logger logger = Logger.getLogger(PieceImpl.class);

  /**
   * Create a new piece message.
   *
   * @param peer
   * @param pieceIndex
   * @param beginningOffset
   * @param block
   */
  @Inject
  public PieceImpl(@Assisted("sendingPeer") Peer sendingPeer,
      @Assisted("receivingPeer") Peer receivingPeer,
      @Assisted("pieceIndex") int pieceIndex,
      @Assisted("beginningOffset") int beginningOffset, @Assisted byte[] block) {
    super(sendingPeer, receivingPeer, PeerMessage.Type.PIECE);
    this.pieceIndex = pieceIndex;
    this.beginningOffset = beginningOffset;
    this.block = Preconditions.checkNotNull(block);
    logger.debug(String.format("[message: %s][from: %s][to: %s][piece: %d]",
        PeerMessage.Type.PIECE, sendingPeer, receivingPeer, pieceIndex));
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
