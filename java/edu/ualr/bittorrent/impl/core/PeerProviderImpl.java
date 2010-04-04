package edu.ualr.bittorrent.impl.core;

import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import edu.ualr.bittorrent.interfaces.Metainfo;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.PeerProvider;

public class PeerProviderImpl implements PeerProvider {
  private static Logger logger = Logger.getLogger(PeerProviderImpl.class);

  private boolean alreadyCalled = false;
  private final Metainfo metainfo;

  public PeerProviderImpl(Metainfo metainfo) {
    this.metainfo = Preconditions.checkNotNull(metainfo);
  }

  public ImmutableList<Peer> addPeers() {
    if (alreadyCalled) {
      return null;
    }
    alreadyCalled = true;
    List<Peer> peers = Lists.newArrayList();
    logger.info("adding peers");
    for (int i = 0; i < 10; i++) {
      Peer peer = new PeerImpl();
      peer.setTracker(metainfo.getTrackers().get(0));
      peer.setMetainfo(metainfo);
      peers.add(peer);
    }
    return ImmutableList.copyOf(peers);
  }

}
