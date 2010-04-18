package edu.ualr.bittorrent.impl.core.messages;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

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
  @Inject
  public UnchokeImpl(@Assisted Peer peer) {
    super(peer, PeerMessage.Type.UNCHOKE);
  }
}
