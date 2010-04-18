package edu.ualr.bittorrent.impl.core;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import edu.ualr.bittorrent.interfaces.Metainfo;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.PeerProvider;

/**
 * Default implementation of the {@link PeerProvider} interface.
 */
public class PeerProviderImpl implements PeerProvider {
  private static Logger logger = Logger.getLogger(PeerProviderImpl.class);

  private boolean alreadyCalled = false;
  private final Metainfo metainfo;
  private final Map<Integer, byte[]> pieces;

  /**
   * Create a new peer provider.
   *
   * @param metainfo
   * @param pieces
   */
  @Inject
  public PeerProviderImpl(Metainfo metainfo, Map<Integer, byte[]> pieces) {
    this.metainfo = Preconditions.checkNotNull(metainfo);
    this.pieces = Preconditions.checkNotNull(pieces);
  }

  /**
   * {@inheritDoc}
   */
  public ImmutableList<Peer> addPeers() {
    if (alreadyCalled) {
      return null;
    }
    alreadyCalled = true;
    List<Peer> peers = Lists.newArrayList();
    logger.info("adding seeds");
    for (int i = 0; i < 1; i++) {
      Peer peer = new PeerImpl(pieces);
      logger.info(String.format("Adding seed %s", new String(peer.getId())));
      peer.setMetainfo(metainfo);
      peers.add(peer);
    }
    logger.info("adding peers");
    for (int i = 0; i < 1; i++) {
      Peer peer = new PeerImpl();
      logger.info(String.format("Adding peer %s", new String(peer.getId())));
      peer.setMetainfo(metainfo);
      peers.add(peer);
    }
    return ImmutableList.copyOf(peers);
  }

}
