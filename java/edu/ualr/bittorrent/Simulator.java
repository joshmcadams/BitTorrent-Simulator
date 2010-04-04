package edu.ualr.bittorrent;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
  private final ExecutorService executor;

  public Simulator(PeerProvider peerProvider, Metainfo metainfo, Integer threadCount) {
    this.peerProvider = Preconditions.checkNotNull(peerProvider);
    this.metainfo = Preconditions.checkNotNull(metainfo);
    this.executor = Executors.newFixedThreadPool(Preconditions.checkNotNull(threadCount));
  }

  private class ExperimentTerminator implements Runnable {
    private final Long millisecondsToRun;

    public ExperimentTerminator(Long millisecondsToRun) {
      this.millisecondsToRun = Preconditions.checkNotNull(millisecondsToRun);
    }

    public void run() {
      try {
        logger.info(String.format("Experiment limited to %d microseconds", millisecondsToRun));
        executor.awaitTermination(millisecondsToRun, TimeUnit.MILLISECONDS);
        if (!executor.isShutdown()) {
          logger.info("Stopping experiment before all threads are complete");
          executor.shutdown();
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public void runExperiment(Long millisecondsToRun) {
    logger.info("Experiment starting");

    setTimeout(millisecondsToRun);

    try {
      spawnTrackers();
      spawnPeers();
    } finally {
      executor.shutdown();
    }
  }

  private void setTimeout(Long millisecondsToRun) {
    if (millisecondsToRun != null) {
      executor.execute(new ExperimentTerminator(millisecondsToRun));
    }
  }

  private void spawnTrackers() {
    for (Tracker tracker : metainfo.getTrackers()) {
      if (executor.isShutdown()) {
        return;
      }
      executor.execute(tracker);
    }
  }

  private void spawnPeers() {
    ImmutableList<Peer> newPeers;
    while ((newPeers = peerProvider.addPeers()) != null) {
      peers.addAll(newPeers);
      for (Peer peer : newPeers) {
        if (executor.isShutdown()) {
          return;
        }
        executor.execute(peer);
      }
    }
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
        metainfo, 100
        ).runExperiment(null);
  }
}
