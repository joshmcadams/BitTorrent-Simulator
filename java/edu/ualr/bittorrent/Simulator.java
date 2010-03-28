package edu.ualr.bittorrent;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import edu.ualr.bittorrent.impl.core.MetainfoImpl;
import edu.ualr.bittorrent.impl.core.PeerProviderImpl;
import edu.ualr.bittorrent.impl.core.TrackerImpl;
import edu.ualr.bittorrent.interfaces.Metainfo;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.PeerProvider;
import edu.ualr.bittorrent.interfaces.Tracker;

public class Simulator {
  private final Tracker tracker;
  private final PeerProvider peerProvider;
  private final Metainfo metainfo;
  private final List<Peer> peers = Lists.newArrayList();
  private final static Logger logger = Logger.getLogger(Simulator.class);

  public Simulator(Tracker tracker, PeerProvider peerProvider, Metainfo metainfo) {
    this.tracker = Preconditions.checkNotNull(tracker);
    this.peerProvider = Preconditions.checkNotNull(peerProvider);
    this.metainfo = Preconditions.checkNotNull(metainfo);
  }

  public void runExperiment(long millisecondsToRun) {
    logger.info("Simulator Running");

    long startTime = System.currentTimeMillis();

    Executor executor = Executors.newFixedThreadPool(100);

    while (System.currentTimeMillis() - startTime < millisecondsToRun) {
      ImmutableList<Peer> newPeers = peerProvider.addPeers(tracker, metainfo);
      if (newPeers == null || newPeers.size() == 0) {
        continue;
      }
      peers.addAll(newPeers);
      for (Peer peer : newPeers) {
        executor.execute(peer);
      }
    }

    logger.info("Simulator Stopping");
  }

  public static void main(String[] args) {
    BasicConfigurator.configure();
    new Simulator(
        new TrackerImpl(),
        new PeerProviderImpl(),
        new MetainfoImpl()
        ).runExperiment(1000);
  }
}
