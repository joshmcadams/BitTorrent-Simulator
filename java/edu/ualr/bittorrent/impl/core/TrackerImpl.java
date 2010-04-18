package edu.ualr.bittorrent.impl.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.joda.time.Instant;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import edu.ualr.bittorrent.impl.core.TrackerModule.TrackerRequestInterval;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.Tracker;
import edu.ualr.bittorrent.interfaces.TrackerRequest;
import edu.ualr.bittorrent.interfaces.TrackerResponse;

/**
 * Default implementation of the {@link Tracker} interface.
 */
public class TrackerImpl implements Tracker {
  private static final Logger logger = Logger.getLogger(TrackerImpl.class);
  private static final int PEER_REQUEST_LIMIT = 50;
  private static final double SEED_PROBABILITY = 0.05;

  private final byte[] id;
  private final int interval;
  private final Map<byte[], SwarmInfo> swarmInfoMap = new ConcurrentHashMap<byte[], SwarmInfo>();

  /**
   * Create a new tracker.
   *
   * @param interval
   */
  @Inject
  public TrackerImpl(@TrackerRequestInterval int interval) {
    this(UUID.randomUUID().toString().getBytes(), interval);
  }

  /**
   * Create a new tracker.
   *
   * @param id
   * @param interval
   */
  public TrackerImpl(byte[] id, int interval) {
    this.id = Preconditions.checkNotNull(id);
    this.interval = interval;
  }

  /**
   * {@inheritDoc}
   */
  public TrackerResponse get(TrackerRequest request) {
    Preconditions.checkNotNull(request);
    Peer peer = request.getPeer();
    logger.info(String.format("Peer %s made a request to the tracker",
        new String(peer.getId())));
    return buildResponse(request);
  }

  /**
   * {@inheritDoc}
   */
  private TrackerResponse buildResponse(TrackerRequest request) {
    SwarmInfo swarmInfo = getSwarmInfo(request.getInfoHash());
    swarmInfo.logRequest(request);
    TrackerResponse response = new TrackerResponseImpl(id, ImmutableList
        .copyOf(swarmInfo.getListOfPeers(request)), swarmInfo.getSeederCount(),
        swarmInfo.getLeechCount(), interval);
    return response;
  }

  /**
   * {@inheritDoc}
   */
  private SwarmInfo getSwarmInfo(byte[] infoHash) {
    if (swarmInfoMap.containsKey(infoHash)) {
      return swarmInfoMap.get(infoHash);
    }
    SwarmInfo swarmInfo = new SwarmInfo();
    synchronized (swarmInfoMap) {
      swarmInfoMap.put(infoHash, swarmInfo);
    }
    return swarmInfo;
  }

  /**
   * No thread needed for this implementation of the tracker, so the run method
   * does no work.
   */
  public void run() {
    /*
     * Default tracker doesn't do anything in its thread and instead relies on
     * synchronized access by the peers. If the experiment calls for the tracker
     * to actively do something in the background, this is your hook.
     */
  }

  /**
   * Object to keep the state of the swarm.
   */
  private class SwarmInfo {
    private final Map<Peer, Instant> leeches = new ConcurrentHashMap<Peer, Instant>();
    private final Map<Peer, Instant> seeders = new ConcurrentHashMap<Peer, Instant>();

    /**
     * Know when a request has been made.
     *
     * @param request
     */
    void logRequest(TrackerRequest request) {
      if (request.getLeft() == 0) {
        leeches.remove(request.getPeer());
        seeders.put(request.getPeer(), new Instant());
      } else {
        seeders.remove(request.getPeer());
        leeches.put(request.getPeer(), new Instant());
      }
    }

    /**
     * Get a list of peers to return to the requestor.
     *
     * @param request
     * @return
     */
    List<Peer> getListOfPeers(TrackerRequest request) {
      final int listSize = request.getNumWant() != null
          && PEER_REQUEST_LIMIT > request.getNumWant() ? request.getNumWant()
          : PEER_REQUEST_LIMIT;

      Map<Peer, Integer> peerMap = new HashMap<Peer, Integer>(listSize);

      List<Peer> seederKeys = new ArrayList<Peer>(seeders.size());
      for (Peer key : seeders.keySet()) {
        seederKeys.add(key);
      }

      List<Peer> leechKeys = new ArrayList<Peer>(leeches.size());
      for (Peer key : leeches.keySet()) {
        leechKeys.add(key);
      }

      if (seeders.size() + leeches.size() < PEER_REQUEST_LIMIT) {
        return new ImmutableList.Builder<Peer>().addAll(leechKeys).addAll(
            seederKeys).build();
      }

      while (peerMap.size() < PEER_REQUEST_LIMIT) {
        Peer peer;
        if (Math.random() < SEED_PROBABILITY && seederKeys.size() > 0) {
          peer = seederKeys
              .get((int) (Math.random() * (seederKeys.size() - 1)));
        } else {
          peer = leechKeys.get((int) (Math.random() * (seederKeys.size() - 1)));
        }

        if (!peer.equals(request.getPeer())) {
          peerMap.put(peer, 1);
        }
      }

      logger.info(String.format("Returning a list of %d peers", peerMap
          .keySet().size()));
      return ImmutableList.copyOf(peerMap.keySet());
    }

    /**
     * Get the number of seeders in the swarm.
     *
     * @return
     */
    int getSeederCount() {
      return seeders.size();
    }

    /**
     * Get the number of leechers in the swarm.
     *
     * @return
     */
    int getLeechCount() {
      return leeches.size();
    }
  }

}
