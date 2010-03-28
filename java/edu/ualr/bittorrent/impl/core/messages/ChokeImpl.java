package edu.ualr.bittorrent.impl.core.messages;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.Choke;

public class ChokeImpl extends PeerMessage<Choke> implements Choke {
  public ChokeImpl(Peer peer) {
    super(peer, PeerMessage.Type.CHOKE);
  }
}
