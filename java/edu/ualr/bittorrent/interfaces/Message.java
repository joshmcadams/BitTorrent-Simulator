package edu.ualr.bittorrent.interfaces;

public interface Message extends HasPeer {
  public enum Type {
    BIT_FIELD, CANCEL, CHOKE, HANDSHAKE, HAVE, INTERESTED, KEEP_ALIVE, NOT_INTERESTED, PIECE, PORT, REQUEST, UNCHOKE
  };

  public Type getType();
}
