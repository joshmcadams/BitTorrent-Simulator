package edu.ualr.bittorrent.impl.core.messages;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.Request;

public class RequestImpl extends PeerMessage<Request> implements Request {
  private final int index;
  private final int begin;
  private final int length;

  public RequestImpl(Peer peer, int index, int begin, int length) {
    super(peer, PeerMessage.Type.REQUEST);
    this.index = index;
    this.begin = begin;
    this.length = length;
  }

  public int getBeginningOffset() {
    return begin;
  }

  public int getBlockLength() {
    return length;
  }

  public int getPieceIndex() {
    return index;
  }
}
