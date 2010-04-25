package edu.ualr.bittorrent.impl.core.messages;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.BitField;

/**
 * Default implementation of the {@link BitField} interface.
 */
public class BitFieldImpl extends PeerMessage<BitField> implements BitField {
  final byte[] bitfield;
  private static final Logger logger = Logger.getLogger(BitFieldImpl.class);

  /**
   * Create a new bit field message.
   *
   * @param peer
   * @param bitfield
   */
  @Inject
  public BitFieldImpl(@Assisted("sendingPeer") Peer sendingPeer,
      @Assisted("receivingPeer") Peer receivingPeer, @Assisted byte[] bitfield) {
    super(sendingPeer, receivingPeer, PeerMessage.Type.BIT_FIELD);
    this.bitfield = Preconditions.checkNotNull(bitfield);
  }

  /**
   * {@inheritDoc}
   */
  public byte[] getBitField() {
    return bitfield;
  }
}
