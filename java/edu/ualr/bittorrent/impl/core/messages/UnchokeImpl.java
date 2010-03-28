package edu.ualr.bittorrent.impl.core.messages;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.Unchoke;

public class UnchokeImpl extends PeerMessage<Unchoke> implements Unchoke {
  public UnchokeImpl(Peer peer) {
    super(peer, PeerMessage.Type.UNCHOKE);
  }
}
