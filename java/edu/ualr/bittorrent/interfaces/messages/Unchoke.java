package edu.ualr.bittorrent.interfaces.messages;

import edu.ualr.bittorrent.interfaces.HasPeer;
import edu.ualr.bittorrent.interfaces.Message;
import edu.ualr.bittorrent.interfaces.Peer;

/**
 * In a BitTorrent swarm, {@link Peer}s can {@link Choke} and {@link UnChoke}
 * each other in order to disallow or allow {@link Request}s to flow between
 * one-another.
 *
 * Objects that will serve as {@link UnChoke} messages should implement this
 * interface.
 */
public interface Unchoke extends HasPeer, Message {

}
