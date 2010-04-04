package edu.ualr.bittorrent.interfaces;


public interface TrackerRequest extends HasPeer {
  public enum Event {
    STARTED,
    STOPPED,
    COMPLETED
  };
  public byte[] getInfoHash();
  public Integer getPort();
  public Long getUploaded();
  public Long getDownloaded();
  public Long getLeft();
  public Boolean acceptsCompactResponses();
  public Boolean omitPeerId();
  public Event getEvent();
  public String getIp();
  public Integer getNumWant();
  public String getKey();
  public byte[] getTrackerId();
}
