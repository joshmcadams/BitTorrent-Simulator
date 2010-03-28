package edu.ualr.bittorrent.interfaces;

import com.google.common.collect.ImmutableList;


public interface PeerProvider {
  ImmutableList<Peer> addPeers(Tracker tracker, Metainfo metainfo);
}
