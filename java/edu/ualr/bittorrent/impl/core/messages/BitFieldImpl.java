package edu.ualr.bittorrent.impl.core.messages;

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

  /**
   * Create a new bit field message.
   *
   * @param peer
   * @param bitfield
   */
  @Inject
  public BitFieldImpl(@Assisted Peer peer, @Assisted byte[] bitfield) {
    super(peer, PeerMessage.Type.BIT_FIELD);
    this.bitfield = Preconditions.checkNotNull(bitfield);
  }

  /**
   * {@inheritDoc}
   */
  public byte[] getBitField() {
    return bitfield;
  }
}
