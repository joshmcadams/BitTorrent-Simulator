package edu.ualr.bittorrent.interfaces.messages;

import edu.ualr.bittorrent.interfaces.HasReceivingPeer;
import edu.ualr.bittorrent.interfaces.HasSendingPeer;
import edu.ualr.bittorrent.interfaces.Message;
import edu.ualr.bittorrent.interfaces.Peer;

/**
 * In order to keep the line of communication open between two {@link Peer}s
 * there should be a continual flow of data. If a {@link Peer} has nothing
 * meaningful to say to another {@link Peer}, it should at least send a
 * {@link KeepAlive} to let the other {@link Peer} know that it is still around.
 *
 * Objects that serve as {@link KeepAlive}s should implement this interface.
 */
public interface KeepAlive extends HasSendingPeer, HasReceivingPeer, Message {

}
