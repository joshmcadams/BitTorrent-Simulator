package edu.ualr.bittorrent.impl.core.messages;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.Have;

public class HaveImpl extends PeerMessage<Have> implements Have {
  public HaveImpl(Peer peer) {
    super(peer, PeerMessage.Type.HAVE);
  }

  public long getPieceIndex() {
    // TODO Auto-generated method stub
    return 0;
  }
}
