package edu.ualr.bittorrent.interfaces.messages;

import edu.ualr.bittorrent.interfaces.HasPeer;
import edu.ualr.bittorrent.interfaces.Message;
import edu.ualr.bittorrent.interfaces.Peer;

/**
 * When a {@link Peer} is choked by another and does not need data from the
 * {@link Peer} who applied the {@link Choke}, the choked {@link Peer} can
 * express lack of interest in the choker, letting that {@link Peer} know that
 * it would not like to be {@link UnChoke}d.
 *
 * Objects that will represent {@link NotInterested} messages should implement
 * this interface.
 */
public interface NotInterested extends HasPeer, Message {

}
