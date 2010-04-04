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
import com.google.common.collect.Lists;

import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.Tracker;
import edu.ualr.bittorrent.interfaces.TrackerRequest;
import edu.ualr.bittorrent.interfaces.TrackerResponse;

public class TrackerImpl implements Tracker {
  List<Peer> peers = Lists.newArrayList();
  private static final Logger logger = Logger.getLogger(PeerImpl.class);
  private final byte[] id;
  private final int interval;
  private static final int PEER_REQUEST_LIMIT = 50;
  private static final double SEED_PROBABILITY = 0.05;

  public TrackerImpl(int interval) {
    this(UUID.randomUUID().toString().getBytes(), interval);
  }

  public TrackerImpl(byte[] id, int interval) {
    this.id = Preconditions.checkNotNull(id);
    this.interval = interval;
  }

  public synchronized TrackerResponse get(TrackerRequest request) {
    Preconditions.checkNotNull(request);
    Peer peer = request.getPeer();
    logger.info(String.format("Peer %s made a request to the tracker", peer.getId()));
    rememberPeer(peer);
    return buildResponse(request);
  }

  private class SwarmInfo {
    private final Map<Peer, Instant> leeches = new ConcurrentHashMap<Peer, Instant>();
    private final Map<Peer, Instant> seeders = new ConcurrentHashMap<Peer, Instant>();

    void logRequest(TrackerRequest request) {
      if (request.getLeft() == 0) {
        leeches.remove(request.getPeer());
        seeders.put(request.getPeer(), new Instant());
      } else {
        seeders.remove(request.getPeer());
        leeches.put(request.getPeer(), new Instant());
      }
    }

    List<Peer> getListOfPeers(TrackerRequest request) {
      final int listSize =
        request.getNumWant() != null && PEER_REQUEST_LIMIT > request.getNumWant() ?
        request.getNumWant() : PEER_REQUEST_LIMIT;

      Map<Peer, Integer> peers = new HashMap<Peer, Integer>(listSize);

      List<Peer> seederKeys = new ArrayList<Peer>(seeders.size());
      for (Peer key : seeders.keySet()) {
        seederKeys.add(key);
      }

      List<Peer> leechKeys = new ArrayList<Peer>(leeches.size());
      for (Peer key : leeches.keySet()) {
        leechKeys.add(key);
      }

      while(peers.size() < PEER_REQUEST_LIMIT) {
        Peer peer;
        if (Math.random() < SEED_PROBABILITY && seederKeys.size() > 0) {
          peer = seederKeys.get((int) (Math.random() * (seederKeys.size() - 1)));
        } else {
          peer = seederKeys.get((int) (Math.random() * (seederKeys.size() - 1)));
        }

        if (!peer.equals(request.getPeer())) {
          peers.put(peer, 1);
        }
      }

      logger.info(String.format("Returning a list of %d peers", peers.keySet().size()));
      return ImmutableList.copyOf(peers.keySet());
    }

    int getSeederCount() {
      return seeders.size();
    }

    int getLeechCount() {
      return leeches.size();
    }
  }

  private final Map<byte[], SwarmInfo> swarmInfoMap = new ConcurrentHashMap<byte[], SwarmInfo>();

  private SwarmInfo getSwarmInfo(byte[] infoHash) {
    if (swarmInfoMap.containsKey(infoHash)) {
      return swarmInfoMap.get(infoHash);
    }
    SwarmInfo swarmInfo = new SwarmInfo();
    swarmInfoMap.put(infoHash, swarmInfo);
    return swarmInfo;
  }

  private TrackerResponse buildResponse(TrackerRequest request) {
    SwarmInfo swarmInfo = getSwarmInfo(request.getInfoHash());
    swarmInfo.logRequest(request);
    TrackerResponse response = new TrackerResponseImpl(
        id,
        ImmutableList.copyOf(swarmInfo.getListOfPeers(request)),
        swarmInfo.getSeederCount(),
        swarmInfo.getLeechCount(),
        interval
    );
    return response;
  }

  private void rememberPeer(Peer peer) {
    if (!peers.contains(peer)) {
      logger.info(String.format("Adding peer %s", peer.getId()));
      peers.add(peer);
    }
  }

  public void run() {
    /*
     * Default tracker doesn't do anything in its thread and instead relies on synchronized access
     * by the peers. If the experiment calls for the tracker to actively do something in the
     * background, this is your hook.
     */
  }
}
