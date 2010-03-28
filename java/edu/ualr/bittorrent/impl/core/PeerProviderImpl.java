package edu.ualr.bittorrent.impl.core;

import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import edu.ualr.bittorrent.interfaces.Metainfo;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.PeerProvider;
import edu.ualr.bittorrent.interfaces.Tracker;

public class PeerProviderImpl implements PeerProvider {
  private static Logger logger = Logger.getLogger(PeerProviderImpl.class);
  private static boolean alreadyCalled = false;
  public ImmutableList<Peer> addPeers(Tracker tracker, Metainfo metainfo) {
    if (alreadyCalled) {
      return null;
    }
    alreadyCalled = true;
    List<Peer> peers = Lists.newArrayList();
    logger.info("adding peers");
    for (int i = 0; i < 10; i++) {
      Peer peer = new PeerImpl();
      peer.setTracker(tracker);
      peer.setId(Integer.toString(i).getBytes());
      peer.setMetainfo(metainfo);
      peers.add(peer);
    }
    return ImmutableList.copyOf(peers);
  }

}
