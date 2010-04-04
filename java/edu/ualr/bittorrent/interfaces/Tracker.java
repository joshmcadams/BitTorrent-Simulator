package edu.ualr.bittorrent.interfaces;

public interface Tracker extends Runnable {
  public TrackerResponse get(TrackerRequest request);
  public void registerPeer(Peer peer);
};

