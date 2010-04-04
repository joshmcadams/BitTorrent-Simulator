package edu.ualr.bittorrent.interfaces;

public interface Tracker extends Runnable {
  public byte[] getId();
  public TrackerResponse get(TrackerRequest request);
};
