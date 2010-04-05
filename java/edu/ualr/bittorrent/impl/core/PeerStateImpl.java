package edu.ualr.bittorrent.impl.core;

import org.joda.time.Instant;

import com.google.common.collect.ImmutableList;
import com.sun.tools.javac.util.Pair;

import edu.ualr.bittorrent.interfaces.PeerState;

public class PeerStateImpl implements PeerState {

  public boolean didLocalSendHandshake() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean didRemoteSendHandshake() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isLocalChoked() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isLocalInteresting() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isRemoteChoked() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isRemoteInteresting() {
    // TODO Auto-generated method stub
    return false;
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
    // TODO Auto-generated method stub
    return null;
  }

  public int requestedPort() {
    // TODO Auto-generated method stub
    return 0;
  }

  public Instant sentLastKeepAliveAt() {
    // TODO Auto-generated method stub
    return null;
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
    // TODO Auto-generated method stub

  }

  public void setRemoteSentKeepAlive(Instant when) {
    // TODO Auto-generated method stub

  }

  public void setRemoteSentPiece(int pieceIndex, Instant when) {
    // TODO Auto-generated method stub

  }

}
