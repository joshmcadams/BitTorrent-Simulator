package edu.ualr.bittorrent.impl.core;

import com.google.common.base.Preconditions;

import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.TrackerRequest;

/**
 * Default implementation of the {@link TrackerRequest} interface.
 */
public class TrackerRequestImpl implements TrackerRequest {
  private final Peer peer;
  private final Boolean acceptsCompactResponse;
  private final int downloaded;
  private final int uploaded;
  private final Event event;
  private final byte[] infoHash;
  private final String ip;
  private final Integer port;
  private final String key;
  private final int left;
  private final Integer numWant;
  private final byte[] trackerId;
  private final Boolean omitPeerId;

  /**
   * Create a new tracker request.
   *
   * @param peer
   * @param infoHash
   * @param downloaded
   * @param uploaded
   * @param left
   */
  public TrackerRequestImpl(Peer peer, byte[] infoHash, int downloaded,
      int uploaded, int left) {
    this(peer, infoHash, downloaded, uploaded, left, null, null, null, null,
        null, null, null, null);
  }

  /**
   * Create a new tracker request.
   *
   * @param peer
   * @param infoHash
   * @param downloaded
   * @param uploaded
   * @param left
   * @param acceptsCompactResponse
   * @param event
   * @param ip
   * @param port
   * @param key
   * @param numWant
   * @param trackerId
   * @param omitPeerId
   */
  public TrackerRequestImpl(Peer peer, byte[] infoHash, int downloaded,
      int uploaded, int left, Boolean acceptsCompactResponse, Event event,
      String ip, Integer port, String key, Integer numWant, byte[] trackerId,
      Boolean omitPeerId) {
    this.peer = Preconditions.checkNotNull(peer);
    this.infoHash = Preconditions.checkNotNull(infoHash);
    this.downloaded = Preconditions.checkNotNull(downloaded);
    this.uploaded = Preconditions.checkNotNull(uploaded);
    this.left = Preconditions.checkNotNull(left);
    this.acceptsCompactResponse = acceptsCompactResponse == null ? false
        : acceptsCompactResponse;
    this.event = event;
    this.ip = ip;
    this.port = port;
    this.key = key;
    this.numWant = numWant;
    this.trackerId = trackerId;
    this.omitPeerId = omitPeerId;
  }

  /**
   * {@inheritDoc}
   */
  public Peer getPeer() {
    return peer;
  }

  /**
   * {@inheritDoc}
   */
  public Boolean acceptsCompactResponses() {
    return acceptsCompactResponse;
  }

  /**
   * {@inheritDoc}
   */
  public int getDownloaded() {
    return downloaded;
  }

  /**
   * {@inheritDoc}
   */
  public Event getEvent() {
    return event;
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
  public String getIp() {
    return ip;
  }

  /**
   * {@inheritDoc}
   */
  public String getKey() {
    return key;
  }

  /**
   * {@inheritDoc}
   */
  public int getLeft() {
    return left;
  }

  /**
   * {@inheritDoc}
   */
  public Integer getNumWant() {
    return numWant;
  }

  /**
   * {@inheritDoc}
   */
  public Integer getPort() {
    return port;
  }

  /**
   * {@inheritDoc}
   */
  public byte[] getTrackerId() {
    return trackerId;
  }

  /**
   * {@inheritDoc}
   */
  public int getUploaded() {
    return uploaded;
  }

  /**
   * {@inheritDoc}
   */
  public Boolean omitPeerId() {
    return omitPeerId;
  }
}
