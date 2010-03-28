package edu.ualr.bittorrent.impl.core.messages;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.Interested;

public class InterestedImpl extends PeerMessage<Interested> implements Interested {
  public InterestedImpl(Peer peer) {
    super(peer, PeerMessage.Type.INTERESTED);
  }
}
