package edu.ualr.bittorrent.impl.core.messages;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.Handshake;

/**
 * Default implementation of the {@link Handshake} interface.
 */
public class HandshakeImpl extends PeerMessage<Handshake> implements Handshake {
  private final byte[] infoHash;
  private final String protocolIdentifier; /*
                                            * 'BitTorrent protocol' is the
                                            * default
                                            */
  private final byte[] reservedBytes; /* 0x00 x 8 is the default */
  public static final String DEFAULT_PROTOCOL_IDENTIFIER = "BitTorrent protocol";
  public static final byte[] DEFAULT_RESERVED_BYTES = { 0x00, 0x00, 0x00,
      0x00, 0x00, 0x00, 0x00, 0x00 };

  /**
   * Create a new handshake message.
   *
   * @param infoHash
   * @param peer
   */
  public HandshakeImpl(Peer peer, byte[] infoHash) {
    this(peer, infoHash, DEFAULT_PROTOCOL_IDENTIFIER, DEFAULT_RESERVED_BYTES);
  }

  /**
   * Create a new handshake message.
   *
   * @param infoHash
   * @param peer
   * @param protocolIdentifier
   * @param reservedBytes
   */
  @Inject
  HandshakeImpl(@Assisted Peer peer, @Assisted("infoHash") byte[] infoHash,
      @Assisted String protocolIdentifier,
      @Assisted("reservedBytes") byte[] reservedBytes) {
    super(peer, PeerMessage.Type.HANDSHAKE);
    this.infoHash = Preconditions.checkNotNull(infoHash);
    this.protocolIdentifier = Preconditions.checkNotNull(protocolIdentifier);
    this.reservedBytes = Preconditions.checkNotNull(reservedBytes);
  }

  /**
   * {@inheritDoc}
   */
  public byte[] getInfoHash() {
    return infoHash;
  }

  /**
   * {@inheritDoc}
   */
  public String getProtocolIdentifier() {
    return protocolIdentifier;
  }

  /**
   * {@inheritDoc}
   */
  public byte[] getReservedBytes() {
    return reservedBytes;
  }
}
