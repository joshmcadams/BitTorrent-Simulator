package edu.ualr.bittorrent.impl.core.messages;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.Interested;

/**
 * Default implementation of the {@link Interested} interface.
 */
public class InterestedImpl extends PeerMessage<Interested> implements
    Interested {
  /**
   * Create a new interested message.
   *
   * @param peer
   */
  @Inject
  public InterestedImpl(@Assisted Peer peer) {
    super(peer, PeerMessage.Type.INTERESTED);
  }
}
