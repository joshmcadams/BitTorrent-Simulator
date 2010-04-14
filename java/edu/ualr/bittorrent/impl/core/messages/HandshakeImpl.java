package edu.ualr.bittorrent.impl.core.messages;

import com.google.common.base.Preconditions;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.Handshake;

public class HandshakeImpl extends PeerMessage<Handshake> implements Handshake {
  private final byte[] infoHash;
  private final String protocolIdentifier; /*
                                            * 'BitTorrent protocol' is the
                                            * default
                                            */
  private final byte[] reservedBytes; /* 0x00 x 8 is the default */
  private static final String DEFAULT_PROTOCOL_IDENTIFIER = "BitTorrent protocol";
  private static final byte[] DEFAULT_RESERVED_BYTES = { 0x00, 0x00, 0x00,
      0x00, 0x00, 0x00, 0x00, 0x00 };

  public HandshakeImpl(byte[] infoHash, Peer peer) {
    this(infoHash, peer, DEFAULT_PROTOCOL_IDENTIFIER, DEFAULT_RESERVED_BYTES);
  }

  HandshakeImpl(byte[] infoHash, Peer peer, String protocolIdentifier,
      byte[] reservedBytes) {
    super(peer, PeerMessage.Type.HANDSHAKE);
    this.infoHash = Preconditions.checkNotNull(infoHash);
    this.protocolIdentifier = Preconditions.checkNotNull(protocolIdentifier);
    this.reservedBytes = Preconditions.checkNotNull(reservedBytes);
  }

  public byte[] getInfoHash() {
    return infoHash;
  }

  public String getProtocolIdentifier() {
    return protocolIdentifier;
  }

  public byte[] getReservedBytes() {
    return reservedBytes;
  }
}
