package edu.ualr.bittorrent.impl.core.messages;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.Request;

/**
 * Default implementation of the {@link Request} interface.
 */
public class RequestImpl extends PeerMessage<Request> implements Request {
  private final int index;
  private final int begin;
  private final int length;

  /**
   * Create a new request message.
   *
   * @param peer
   * @param index
   * @param begin
   * @param length
   */
  public RequestImpl(Peer peer, int index, int begin, int length) {
    super(peer, PeerMessage.Type.REQUEST);
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
