package edu.ualr.bittorrent.impl.core.messages;

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

  /**
   * Create a new have message.
   *
   * @param peer
   * @param pieceIndex
   */
  @Inject
  public HaveImpl(@Assisted Peer peer, @Assisted int pieceIndex) {
    super(peer, PeerMessage.Type.HAVE);
    this.pieceIndex = pieceIndex;
  }

  /**
   * {@inheritDoc}
   */
  public int getPieceIndex() {
    return pieceIndex;
  }
}
