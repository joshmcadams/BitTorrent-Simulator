package edu.ualr.bittorrent.interfaces.messages;

import edu.ualr.bittorrent.interfaces.HasPeer;
import edu.ualr.bittorrent.interfaces.Message;

public interface Handshake extends HasPeer, Message {
  public String getProtocolIdentifier();
  public byte[] getReservedBytes();
  public byte[] getInfoHash();
}
