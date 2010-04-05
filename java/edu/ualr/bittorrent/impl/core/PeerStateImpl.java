package edu.ualr.bittorrent.impl.core;

import java.util.List;

import org.joda.time.Instant;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.sun.tools.javac.util.Pair;

import edu.ualr.bittorrent.interfaces.PeerState;

public class PeerStateImpl implements PeerState {
  private Instant localSentHandshakeAt;
  private Instant remoteSentHandshakeAt;
  private final boolean localChoked = false;
  private final boolean remoteChoked = false;
  private final boolean localInteresting = false;
  private final boolean remoteInteresting = false;
  private final List<Integer> piecesReceived = Lists.newArrayList();
  private final List<Integer> piecesSent = Lists.newArrayList();
  Instant lastLocalKeepAliveSent;
  Instant lastRemoteKeepAliveReceived;
  Instant localChokedAt;
  Instant remoteChokedAt;

  public boolean didLocalSendHandshake() {
    return localSentHandshakeAt != null;
  }

  public boolean didRemoteSendHandshake() {
    return remoteSentHandshakeAt != null;
  }

  public boolean isLocalChoked() {
    return localChoked;
  }

  public boolean isLocalInteresting() {
    return localInteresting;
  }

  public boolean isRemoteChoked() {
    return remoteChoked;
  }

  public boolean isRemoteInteresting() {
    return remoteInteresting;
  }

  public ImmutableList<Integer> piecesReceived() {
    // TODO Auto-generated method stub
    return null;
  }

  public ImmutableList<Pair<Integer, Instant>> piecesSent() {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean remoteHasPiece(int pieceIndex) {
    // TODO Auto-generated method stub
    return false;
  }

  public ImmutableList<Integer> remoteHasPieces() {
    // TODO Auto-generated method stub
    return null;
  }

  public Instant remoteRequestedPiece(int pieceIndex) {
    // TODO Auto-generated method stub
    return null;
  }

  public ImmutableList<Pair<Integer, Instant>> remoteRequestedPieces() {
    // TODO Auto-generated method stub
    return null;
  }

  public Instant remoteSentKeepAliveAt() {
    return lastRemoteKeepAliveReceived;
  }

  public int requestedPort() {
    // TODO Auto-generated method stub
    return 0;
  }

  public Instant sentLastKeepAliveAt() {
    return lastLocalKeepAliveSent;
  }

  public void setLocalIsChoked(boolean choked, Instant when) {
    // TODO Auto-generated method stub

  }

  public void setLocalIsInteresting(boolean interesting, Instant when) {
    // TODO Auto-generated method stub

  }

  public void setLocalSentHandshake(Instant when) {
    // TODO Auto-generated method stub

  }

  public void setLocalSentKeepAlive(Instant when) {
    // TODO Auto-generated method stub

  }

  public void setLocalSentPiece(int pieceIndex, Instant when) {
    // TODO Auto-generated method stub

  }

  public void setRemoteHasPiece(int pieceIndex, Instant when) {
    // TODO Auto-generated method stub

  }

  public void setRemoteIsChoked(boolean choked, Instant when) {
    // TODO Auto-generated method stub

  }

  public void setRemoteIsInteresting(boolean interesting, Instant when) {
    // TODO Auto-generated method stub

  }

  public void setRemoteRequestedPiece(int pieceIndex, Instant when) {
    // TODO Auto-generated method stub

  }

  public void setRemoteRequestedPort(int port) {
    // TODO Auto-generated method stub

  }

  public void setRemoteSentHandshake(Instant when) {
    remoteSentHandshakeAt = when;
  }

  public void setRemoteSentKeepAlive(Instant when) {
    lastRemoteKeepAliveReceived = when;
  }

  public void setRemoteSentPiece(int pieceIndex, Instant when) {
    // TODO Auto-generated method stub

  }
}
