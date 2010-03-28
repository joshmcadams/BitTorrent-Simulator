package edu.ualr.bittorrent.interfaces;



public interface Tracker {
  public TrackerResponse get(TrackerRequest request);
  public void registerPeer(Peer peer);
};

