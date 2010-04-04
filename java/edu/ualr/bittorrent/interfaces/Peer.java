package edu.ualr.bittorrent.interfaces;

import edu.ualr.bittorrent.PeerMessage;


public interface Peer extends Runnable {
  public byte[] getId();

  public void setTracker(Tracker tracker);

  public void setMetainfo(Metainfo metainfo);

  public void message(PeerMessage<?> message);
}
