package edu.ualr.bittorrent.interfaces;

import org.joda.time.Instant;

import com.google.common.collect.ImmutableList;
import com.sun.tools.javac.util.Pair;

public interface PeerState {
  void setRemoteIsChoked(boolean choked, Instant when);
  boolean isRemoteChoked();

  void setLocalIsChoked(boolean choked, Instant when);
  boolean isLocalChoked();

  void setRemoteSentHandshake(Instant when);
  boolean didRemoteSendHandshake();

  void setLocalSentHandshake(Instant when);
  boolean didLocalSendHandshake();

  void setRemoteIsInteresting(boolean interesting, Instant when);
  boolean isRemoteInteresting();

  void setLocalIsInteresting(boolean interesting, Instant when);
  boolean isLocalInteresting();

  void setRemoteSentKeepAlive(Instant when);
  Instant remoteSentKeepAliveAt();

  void setLocalSentKeepAlive(Instant when);
  Instant sentLastKeepAliveAt();

  void setRemoteHasPiece(int pieceIndex, Instant when);
  boolean remoteHasPiece(int pieceIndex);
  ImmutableList<Integer> remoteHasPieces();

  void setRemoteRequestedPiece(int pieceIndex, Instant when);
  Instant remoteRequestedPiece(int pieceIndex);
  ImmutableList<Pair<Integer, Instant>> remoteRequestedPieces();

  void setLocalSentPiece(int pieceIndex, Instant when);
  ImmutableList<Pair<Integer, Instant>> piecesSent();

  void setRemoteSentPiece(int pieceIndex, Instant when);
  ImmutableList<Integer> piecesReceived();

  void setRemoteRequestedPort(int port);
  int requestedPort();
}
