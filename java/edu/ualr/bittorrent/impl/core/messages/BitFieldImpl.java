package edu.ualr.bittorrent.impl.core.messages;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.BitField;

public class BitFieldImpl extends PeerMessage<BitField> implements BitField {
  public BitFieldImpl(Peer peer) {
    super(peer, PeerMessage.Type.BIT_FIELD);
  }

  public byte[] getBitField() {
    // TODO Auto-generated method stub
    return null;
  }
}
