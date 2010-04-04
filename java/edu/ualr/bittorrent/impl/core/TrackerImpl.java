package edu.ualr.bittorrent.impl.core;

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

  public TrackerImpl() {
    this(UUID.randomUUID().toString().getBytes());
  }

  public TrackerImpl(byte[] id) {
    this.id = Preconditions.checkNotNull(id);
  }

  public synchronized TrackerResponse get(TrackerRequest request) {
    Preconditions.checkNotNull(request);
    Peer peer = request.getPeer();
    rememberPeer(peer);
    return buildResponse(request);
  }

  private class SwarmInfo {
    private final Map<byte[], Instant> leeches = new ConcurrentHashMap<byte[], Instant>();
    private final Map<byte[], Instant> seeders = new ConcurrentHashMap<byte[], Instant>();

    void logRequest(TrackerRequest request) {
      if (request.getLeft() == 0) {
        leeches.remove(request.getPeer());
        seeders.put(request.getPeer().getId(), new Instant());
      } else {
        seeders.remove(request.getPeer());
        leeches.put(request.getPeer().getId(), new Instant());
      }
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
        ImmutableList.copyOf(peers),
        swarmInfo.getSeederCount(),
        swarmInfo.getLeechCount(),
        0   // TODO: interval
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
