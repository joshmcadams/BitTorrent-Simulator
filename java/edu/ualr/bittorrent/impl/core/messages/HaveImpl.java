package edu.ualr.bittorrent.impl.core.messages;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.Have;

/**
 * Default implementation of the {@link Have} interface.
 */
public class HaveImpl extends PeerMessage<Have> implements Have {
  final int pieceIndex;
  private static final Logger logger = Logger.getLogger(HaveImpl.class);

  /**
   * Create a new have message.
   *
   * @param peer
   * @param pieceIndex
   */
  @Inject
  public HaveImpl(@Assisted("sendingPeer") Peer sendingPeer,
      @Assisted("receivingPeer") Peer receivingPeer, @Assisted int pieceIndex) {
    super(sendingPeer, receivingPeer, PeerMessage.Type.HAVE);
    this.pieceIndex = pieceIndex;
    logger.debug(String.format("[message: %s][from: %s][to: %s][piece: %d]",
        PeerMessage.Type.HAVE, sendingPeer, receivingPeer, pieceIndex));
  }

  /**
   * {@inheritDoc}
   */
  public int getPieceIndex() {
    return pieceIndex;
  }
}
