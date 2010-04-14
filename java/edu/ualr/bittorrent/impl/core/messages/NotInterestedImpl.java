package edu.ualr.bittorrent.impl.core.messages;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.NotInterested;

public class NotInterestedImpl extends PeerMessage<NotInterested> implements
    NotInterested {
  public NotInterestedImpl(Peer peer) {
    super(peer, PeerMessage.Type.NOT_INTERESTED);
  }
}
