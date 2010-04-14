package edu.ualr.bittorrent.impl.core;

import com.google.common.base.Preconditions;

import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.TrackerRequest;

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

  public TrackerRequestImpl(Peer peer, byte[] infoHash, int downloaded,
      int uploaded, int left) {
    this(peer, infoHash, downloaded, uploaded, left, null, null, null, null,
        null, null, null, null);
  }

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

  public Peer getPeer() {
    return peer;
  }

  public Boolean acceptsCompactResponses() {
    return acceptsCompactResponse;
  }

  public int getDownloaded() {
    return downloaded;
  }

  public Event getEvent() {
    return event;
  }

  public byte[] getInfoHash() {
    return infoHash;
  }

  public String getIp() {
    return ip;
  }

  public String getKey() {
    return key;
  }

  public int getLeft() {
    return left;
  }

  public Integer getNumWant() {
    return numWant;
  }

  public Integer getPort() {
    return port;
  }

  public byte[] getTrackerId() {
    return trackerId;
  }

  public int getUploaded() {
    return uploaded;
  }

  public Boolean omitPeerId() {
    return omitPeerId;
  }
}
