package edu.ualr.bittorrent.interfaces;

import com.google.common.collect.ImmutableList;

/**
 * When an experiment is running, it is necessary to simulate the joining and
 * leaving of {@link Peer}s to the swarm. The {@link PeerProvider} interface
 * should be implemented by the object responsible for controlling the addition
 * of {@link Peer}s to the swarm.
 */
public interface PeerProvider {
  ImmutableList<Peer> addPeers();
}
