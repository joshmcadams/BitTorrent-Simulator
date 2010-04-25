package edu.ualr.bittorrent.impl.core.messages;

import org.apache.log4j.Logger;

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
  public static final byte[] DEFAULT_RESERVED_BYTES = { 0x00, 0x00, 0x00, 0x00,
      0x00, 0x00, 0x00, 0x00 };
  private static final Logger logger = Logger.getLogger(HandshakeImpl.class);

  /**
   * Create a new handshake message.
   *
   * @param infoHash
   * @param peer
   */
  public HandshakeImpl(Peer sendingPeer, Peer receivingPeer, byte[] infoHash) {
    this(sendingPeer, receivingPeer, infoHash, DEFAULT_PROTOCOL_IDENTIFIER,
        DEFAULT_RESERVED_BYTES);
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
  HandshakeImpl(@Assisted("sendingPeer") Peer sendingPeer,
      @Assisted("receivingPeer") Peer receivingPeer,
      @Assisted("infoHash") byte[] infoHash,
      @Assisted String protocolIdentifier,
      @Assisted("reservedBytes") byte[] reservedBytes) {
    super(sendingPeer, receivingPeer, PeerMessage.Type.HANDSHAKE);
    this.infoHash = Preconditions.checkNotNull(infoHash);
    this.protocolIdentifier = Preconditions.checkNotNull(protocolIdentifier);
    this.reservedBytes = Preconditions.checkNotNull(reservedBytes);
    logger.debug(String.format("[message: %s][from: %s][to: %s]",
        PeerMessage.Type.HANDSHAKE, sendingPeer, receivingPeer));
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
