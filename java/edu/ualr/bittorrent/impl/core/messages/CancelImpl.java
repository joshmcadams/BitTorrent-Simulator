package edu.ualr.bittorrent.impl.core.messages;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.Cancel;

/**
 * Default implementation of the {@link Cancel} interface.
 */
public class CancelImpl extends PeerMessage<Cancel> implements Cancel {
  private final int index;
  private final int begin;
  private final int length;

  /**
   * Create a new cancel message.
   *
   * @param peer
   * @param index
   * @param begin
   * @param length
   */
  public CancelImpl(Peer peer, int index, int begin, int length) {
    super(peer, PeerMessage.Type.CANCEL);
    this.index = index;
    this.begin = begin;
    this.length = length;
  }

  /**
   * {@inheritDoc}
   */
  public int getBeginningOffset() {
    return begin;
  }

  /**
   * {@inheritDoc}
   */
  public int getBlockLength() {
    return length;
  }

  /**
   * {@inheritDoc}
   */
  public int getPieceIndex() {
    return index;
  }
}
