package edu.ualr.bittorrent.impl.core.messages;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.Request;

public class RequestImpl extends PeerMessage<Request> implements Request {
  public RequestImpl(Peer peer) {
    super(peer, PeerMessage.Type.REQUEST);
  }

  public long getBeginningOffset() {
    // TODO Auto-generated method stub
    return 0;
  }

  public long getBlockLength() {
    // TODO Auto-generated method stub
    return 0;
  }

  public long getPieceIndex() {
    // TODO Auto-generated method stub
    return 0;
  }
}
