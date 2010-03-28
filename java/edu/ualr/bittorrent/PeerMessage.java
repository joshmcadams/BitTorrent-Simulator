package edu.ualr.bittorrent;

import com.google.common.base.Preconditions;

import edu.ualr.bittorrent.interfaces.HasPeer;
import edu.ualr.bittorrent.interfaces.Peer;

public class PeerMessage<T> implements HasPeer {
  public enum Type {
    BIT_FIELD,
    CANCEL,
    CHOKE,
    HANDSHAKE,
    HAVE,
    INTERESTED,
    KEEP_ALIVE,
    NOT_INTERESTED,
    PIECE,
    PORT,
    REQUEST,
    UNCHOKE
  };

  private final Peer peer;
  private final Type type;

  public PeerMessage(Peer peer, Type type) {
    this.peer = Preconditions.checkNotNull(peer);
    this.type = Preconditions.checkNotNull(type);
  }

  public Peer getPeer() {
    return peer;
  }

  public Type getType() {
    return type;
  }
}
