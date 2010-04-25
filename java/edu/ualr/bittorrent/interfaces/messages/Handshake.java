package edu.ualr.bittorrent.interfaces.messages;

import edu.ualr.bittorrent.interfaces.HasReceivingPeer;
import edu.ualr.bittorrent.interfaces.HasSendingPeer;
import edu.ualr.bittorrent.interfaces.Message;
import edu.ualr.bittorrent.interfaces.Metainfo;
import edu.ualr.bittorrent.interfaces.Peer;

/**
 * When two {@link Peer}s open a line of communication, they should each start
 * by sending a {@link Handshake}. No other communication should occur between
 * the {@link Peer}s until both have sent a {@link Handshake}.
 *
 * Objects that will act as a {@link Handshake} should implement this interface.
 */
public interface Handshake extends HasSendingPeer, HasReceivingPeer, Message {
  /**
   * Return an identifier that specifies the protocol being spoken. Typically
   * this is just a hard-coded indicator of the current version of the
   * BitTorrent protocol.
   *
   * @return
   */
  public String getProtocolIdentifier();

  /**
   * Return any custom payload in the reserved bytes.
   *
   * @return
   */
  public byte[] getReservedBytes();

  /**
   * Return the info hash of the {@link Metainfo} that this torrent swarm is
   * based on.
   *
   * @return
   */
  public byte[] getInfoHash();
}
