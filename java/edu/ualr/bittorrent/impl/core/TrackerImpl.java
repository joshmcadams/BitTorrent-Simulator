package edu.ualr.bittorrent.impl.core;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

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

    return buildResponse();
  }

  private TrackerResponse buildResponse() {
    TrackerResponse response = new TrackerResponseImpl(
        id,
        ImmutableList.copyOf(peers),
        0L, // TODO: complete
        0L, // TODO: incomplete
        0,  // TODO: interval
        null,
        null,
        null
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
