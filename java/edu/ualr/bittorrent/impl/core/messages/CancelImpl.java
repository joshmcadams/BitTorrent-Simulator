package edu.ualr.bittorrent.impl.core.messages;

import edu.ualr.bittorrent.PeerMessage;
import edu.ualr.bittorrent.interfaces.Peer;
import edu.ualr.bittorrent.interfaces.messages.Cancel;

public class CancelImpl extends PeerMessage<Cancel> implements Cancel {
  private final int index;
  private final int begin;
  private final int length;

  public CancelImpl(Peer peer, int index, int begin, int length) {
    super(peer, PeerMessage.Type.CANCEL);
    this.index = index;
    this.begin = begin;
    this.length = length;
  }

  public long getBeginningOffset() {
    return begin;
  }

  public long getBlockLength() {
    return length;
  }

  public long getPieceIndex() {
    return index;
  }
}
