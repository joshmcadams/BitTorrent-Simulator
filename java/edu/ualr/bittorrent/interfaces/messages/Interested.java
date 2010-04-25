package edu.ualr.bittorrent.interfaces.messages;

import edu.ualr.bittorrent.interfaces.HasReceivingPeer;
import edu.ualr.bittorrent.interfaces.HasSendingPeer;
import edu.ualr.bittorrent.interfaces.Message;
import edu.ualr.bittorrent.interfaces.Peer;

/**
 * When a {@link Peer} is choked by another, but needs data from the
 * {@link Peer} who applied the {@link Choke}, the choked {@link Peer} can
 * express interest in the choker, letting that {@link Peer} know that it would
 * like to be {@link UnChoke}d.
 *
 * Objects that will represent {@link Interested} messages should implement this
 * interface.
 */
public interface Interested extends HasSendingPeer, HasReceivingPeer, Message {

}
