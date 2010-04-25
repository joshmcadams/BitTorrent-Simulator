package edu.ualr.bittorrent.impl.core.messages;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

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
  @Inject
  public RequestImpl(@Assisted("sendingPeer") Peer sendingPeer,
      @Assisted("receivingPeer") Peer receivingPeer,
      @Assisted("pieceIndex") int index,
      @Assisted("beginningOffset") int begin,
      @Assisted("blockLength") int length) {
    super(sendingPeer, receivingPeer, PeerMessage.Type.REQUEST);
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
