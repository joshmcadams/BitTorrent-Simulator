package edu.ualr.bittorrent.impl.core;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.Tracker;
import edu.ualr.bittorrent.interfaces.TrackerRequest;
import edu.ualr.bittorrent.interfaces.TrackerResponse;

public class TrackerImpl implements Tracker {
  List<Peer> peers = Lists.newArrayList();

  public synchronized TrackerResponse get(TrackerRequest request) {
    return new TrackerResponseImpl(ImmutableList.copyOf(peers));
  }

  public synchronized void registerPeer(Peer peer) {
    peers.add(Preconditions.checkNotNull(peer));
  }
}
