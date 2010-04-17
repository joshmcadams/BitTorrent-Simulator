package edu.ualr.bittorrent.impl.core.messages;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.Unchoke;

/**
 * Default implementation of the {@link Unchoke} interface.
 */
public class UnchokeImpl extends PeerMessage<Unchoke> implements Unchoke {
  /**
   * Create a new unchoke message.
   *
   * @param peer
   */
  public UnchokeImpl(Peer peer) {
    super(peer, PeerMessage.Type.UNCHOKE);
  }
}
