package edu.ualr.bittorrent.impl.core.messages;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.Cancel;

public class CancelImpl extends PeerMessage<Cancel> implements Cancel {
  public CancelImpl(Peer peer) {
    super(peer, PeerMessage.Type.CANCEL);
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
