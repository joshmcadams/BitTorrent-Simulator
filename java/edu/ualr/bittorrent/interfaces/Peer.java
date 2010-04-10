package edu.ualr.bittorrent.interfaces;

public interface Peer extends Runnable {
  public byte[] getId();

  public void setTracker(Tracker tracker);

  public void setMetainfo(Metainfo metainfo);

  public void message(Message message);
}
