package edu.ualr.bittorrent.impl.core;

import com.google.common.base.Preconditions;

import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.TrackerRequest;

public class TrackerRequestImpl implements TrackerRequest{
  private final Peer peer;

  TrackerRequestImpl(Peer peer) {
    this.peer = Preconditions.checkNotNull(peer);
  }

  public Peer getPeer() {
    return peer;
  }

  public Boolean acceptsCompactResponses() {
    // TODO Auto-generated method stub
    return null;
  }

  public Long getDownloaded() {
    // TODO Auto-generated method stub
    return null;
  }

  public Event getEvent() {
    // TODO Auto-generated method stub
    return null;
  }

  public byte[] getInfoHash() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getIp() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getKey() {
    // TODO Auto-generated method stub
    return null;
  }

  public Long getLeft() {
    // TODO Auto-generated method stub
    return null;
  }

  public Integer getNumWant() {
    // TODO Auto-generated method stub
    return null;
  }

  public byte[] getPeerId() {
    // TODO Auto-generated method stub
    return null;
  }

  public Integer getPort() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getTrackerId() {
    // TODO Auto-generated method stub
    return null;
  }

  public Long getUploaded() {
    // TODO Auto-generated method stub
    return null;
  }

  public Boolean omitPeerId() {
    // TODO Auto-generated method stub
    return null;
  }
}
