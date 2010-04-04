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
  private final PeerProvider peerProvider;
  private final Metainfo metainfo;
  private final List<Peer> peers = Lists.newArrayList();
  private final static Logger logger = Logger.getLogger(Simulator.class);

  public Simulator(PeerProvider peerProvider, Metainfo metainfo) {
    this.peerProvider = Preconditions.checkNotNull(peerProvider);
    this.metainfo = Preconditions.checkNotNull(metainfo);
  }

  public void runExperiment(long millisecondsToRun) {
    logger.info("Simulator Running");

    long startTime = System.currentTimeMillis();

    Executor executor = Executors.newFixedThreadPool(100);

    while (System.currentTimeMillis() - startTime < millisecondsToRun) {
      ImmutableList<Peer> newPeers = peerProvider.addPeers();
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
    Tracker tracker = new TrackerImpl();
    ImmutableList<Tracker> trackers = ImmutableList.of(tracker);
    ImmutableList<String> pieces = ImmutableList.of("12345678901234567890");
    Metainfo.File file = new MetainfoImpl.FileImpl(new Long(10L), ImmutableList.of("x.txt"));
    ImmutableList<Metainfo.File> files = ImmutableList.of(file);
    Metainfo metainfo = new MetainfoImpl(trackers, pieces, new Long(10L), files);

    new Simulator(
        new PeerProviderImpl(metainfo),
        metainfo
        ).runExperiment(1000);
  }
}
