package edu.ualr.bittorrent;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.ualr.bittorrent.impl.core.MetainfoImpl;
import edu.ualr.bittorrent.impl.core.PeerProviderImpl;
import edu.ualr.bittorrent.impl.core.TrackerImpl;
import edu.ualr.bittorrent.interfaces.Metainfo;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.PeerProvider;
import edu.ualr.bittorrent.interfaces.Tracker;

/**
 * Driver class that defines the experiment. The {@link Simulator} binds all
 * components of a swarm together. This includes the {@link PeerProvider} that
 * provides the peers to the simulated swarm and the {@link Metainfo} that the
 * swarm is using to calculate its downloads.
 */
public class Simulator {
  private final PeerProvider peerProvider;
  private final Metainfo metainfo;
  private final List<Peer> peers = Lists.newArrayList();
  private final ExecutorService executor;

  private final static Logger logger = Logger.getLogger(Simulator.class);

  /**
   * Create a new {@link Simulator}.
   *
   * @param peerProvider
   *          class to provide {@link Peer}s to the experiment.
   * @param metainfo
   *          metadata information about the file(s) to be downloaded in the
   *          experiment.
   * @param threadCount
   *          number of threads that the experiment should use to execute.
   */
  public Simulator(PeerProvider peerProvider, Metainfo metainfo,
      Integer threadCount) {
    this.peerProvider = Preconditions.checkNotNull(peerProvider);
    this.metainfo = Preconditions.checkNotNull(metainfo);
    this.executor = Executors.newFixedThreadPool(Preconditions
        .checkNotNull(threadCount));
  }

  /**
   * Private subclass used to terminate the experiment after a fixed period of
   * time.
   */
  private class ExperimentTerminator implements Runnable {
    private final Long millisecondsToRun;

    /**
     * Create a new thread that will terminate the experiment after a provided
     * period of time.
     *
     * @param millisecondsToRun
     */
    public ExperimentTerminator(Long millisecondsToRun) {
      this.millisecondsToRun = Preconditions.checkNotNull(millisecondsToRun);
    }

    /**
     * Run a thread, waiting on a timeout that is used to shutdown the
     * experiment.
     */
    public void run() {
      try {
        logger.info(String.format("Experiment limited to %d microseconds",
            millisecondsToRun));
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

  /**
   * Start a given experiment for a limited amount of time.
   *
   * @param millisecondsToRun
   */
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

  /**
   * Set the max time for the experiment.
   *
   * @param millisecondsToRun
   */
  private void setTimeout(Long millisecondsToRun) {
    if (millisecondsToRun != null) {
      executor.execute(new ExperimentTerminator(millisecondsToRun));
    }
  }

  /**
   * Torrents can have one or more {@link Tracker}s. In our experiments, each of
   * these {@link Tracker}s will have its own thread.
   */
  private void spawnTrackers() {
    for (Tracker tracker : metainfo.getTrackers()) {
      if (executor.isShutdown()) {
        return;
      }
      executor.execute(tracker);
    }
  }

  /**
   * The {@link PeerProvider} is called upon throughout the experiment to
   * provide new peers to the swarm. This allows for an experiment to more
   * closely relate to a traditional swarm where all of the peers join over a
   * period of time. This method repeatedly calls upon the {@link PeerProvider}
   * to provide new {@link Peers},starting each in a new thread.
   */
  private void spawnPeers() {
    ImmutableList<Peer> newPeers;
    while ((newPeers = peerProvider.addPeers()) != null) {
      logger.info(String.format("Provided with %d new peers", newPeers.size()));
      synchronized (peers) {
        peers.addAll(newPeers);
      }
      for (Peer peer : newPeers) {
        if (executor.isShutdown()) {
          logger.info("Executor is shutdown");
          return;
        }
        logger.info(String.format("Requesting execution of peer %s",
            new String(peer.getId())));
        executor.execute(peer);
      }
    }
  }

  /**
   * Run an experiment.
   *
   * @param args
   * @throws NoSuchAlgorithmException
   */
  public static void main(String[] args) throws NoSuchAlgorithmException {
    BasicConfigurator.configure();

    final Integer trackerRequestInterval = 5000;
    Tracker tracker = new TrackerImpl(trackerRequestInterval);
    ImmutableList<Tracker> trackers = ImmutableList.of(tracker);

    final Integer pieceLength = 1000;

    Map<Integer, byte[]> data = Maps.newHashMap();
    List<String> pieces = Lists.newArrayList();

    for (int i = 0; i < 10; i++) {
      StringBuilder stringBuilder = new StringBuilder(pieceLength);
      for (int j = 0; j < pieceLength; j++) {
        stringBuilder.append('A');
      }
      String dataPiece = stringBuilder.toString();
      data.put(i, dataPiece.getBytes());
      pieces.add(new String(MessageDigest.getInstance("SHA").digest(
          dataPiece.getBytes())));
    }

    Metainfo.File file = new MetainfoImpl.FileImpl(new Long(pieceLength * 10),
        ImmutableList.of("x.txt"));
    ImmutableList<Metainfo.File> files = ImmutableList.of(file);

    Metainfo metainfo = new MetainfoImpl(trackers,
        ImmutableList.copyOf(pieces), pieceLength, files);

    new Simulator(new PeerProviderImpl(metainfo, data), metainfo, 100)
        .runExperiment(null);
  }
}
