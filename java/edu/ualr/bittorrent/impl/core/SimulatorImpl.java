package edu.ualr.bittorrent.impl.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.internal.Nullable;

import edu.ualr.bittorrent.interfaces.Metainfo;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.PeerProvider;
import edu.ualr.bittorrent.interfaces.Simulator;
import edu.ualr.bittorrent.interfaces.Tracker;

/**
 * Driver class that defines the experiment. The {@link SimulatorImpl} binds all
 * components of a swarm together. This includes the {@link PeerProvider} that
 * provides the peers to the simulated swarm and the {@link Metainfo} that the
 * swarm is using to calculate its downloads.
 */
public class SimulatorImpl implements Simulator {
  private final PeerProvider peerProvider;
  private final Metainfo metainfo;
  private final ExecutorService executor;

  /**
   * Create a new {@link SimulatorImpl}.
   *
   * @param peerProvider
   *          class to provide {@link Peer}s to the experiment.
   * @param metainfo
   *          metadata information about the file(s) to be downloaded in the
   *          experiment.
   * @param threadCount
   *          number of threads that the experiment should use to execute.
   */
  @Inject
  public SimulatorImpl(PeerProvider peerProvider, Metainfo metainfo,
      ExecutorService executor) {
    this.peerProvider = Preconditions.checkNotNull(peerProvider);
    this.metainfo = Preconditions.checkNotNull(metainfo);
    this.executor = Preconditions.checkNotNull(executor);
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
        executor.awaitTermination(millisecondsToRun, TimeUnit.MILLISECONDS);
        if (!executor.isShutdown()) {
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
  public void runExperiment(@Nullable Long millisecondsToRun) {
    setTimeout(millisecondsToRun);

    try {
      spawnTrackers();
      spawnPeers();
    } finally {
      if (!executor.isShutdown()) {
        executor.shutdown();
      }
    }
  }

  /**
   * Set the max time for the experiment.
   *
   * @param millisecondsToRun
   */
  private void setTimeout(@Nullable Long millisecondsToRun) {
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
    while (true) {
      ImmutableList<Peer> newPeers = peerProvider.addPeers();
      if (newPeers != null) {
        for (Peer peer : newPeers) {
          if (executor.isShutdown()) {
            return;
          }
          executor.execute(peer);
        }
      }
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) { /* chomp */
      }
    }
  }
}
