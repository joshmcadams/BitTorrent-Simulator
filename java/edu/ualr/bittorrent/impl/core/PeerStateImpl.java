package edu.ualr.bittorrent.impl.core;

import java.util.List;

import org.joda.time.Instant;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.sun.tools.javac.util.Pair;

import edu.ualr.bittorrent.interfaces.PeerState;

public class PeerStateImpl implements PeerState {
  Instant lastLocalHandshakeSentAt;
  Instant lastRemoteHandshakeSentAt;
  Instant lastLocalKeepAliveSentAt;
  Instant lastRemoteKeepAliveSentAt;
  int localPort;
  int remotePort;
  List<PieceRequest> piecesRequestedByRemote = Lists.newLinkedList();
  List<PieceRequest> piecesRequestedByLocal = Lists.newLinkedList();
  Pair<InterestLevel, Instant> localInterestLevelInRemote;
  Pair<InterestLevel, Instant> remoteInterestLevelInLocal;
  Pair<ChokeStatus, Instant> localChokeStatus;
  Pair<ChokeStatus, Instant> remoteChokeStatus;
  List<PieceDownload> piecesDownloaded = Lists.newArrayList();
  List<PieceUpload> piecesUploaded = Lists.newArrayList();
  List<PieceDeclaration> remoteDeclaredPieces = Lists.newArrayList();
  List<PieceDeclaration> localDeclaredPieces = Lists.newArrayList();

  public static class PieceTransferImpl implements PieceTransfer {
    Instant completionTime;
    Integer pieceIndex;
    Instant startTime;
    Integer blockOffset;
    Integer blockSize;

    public Instant getCompletionTime() {
      return completionTime;
    }

    public Integer getPieceIndex() {
      return pieceIndex;
    }

    public Instant getStartTime() {
      return startTime;
    }

    public void setCompletionTime(Instant time) {
      this.completionTime = Preconditions.checkNotNull(time);
    }

    public void setPieceIndex(Integer index) {
      this.pieceIndex = Preconditions.checkNotNull(index);
    }

    public void setStartTime(Instant time) {
      this.startTime = Preconditions.checkNotNull(time);
    }

    public Integer getBlockOffset() {
      return blockOffset;
    }

    public Integer getBlockSize() {
      return blockSize;
    }

    public void setBlockOffset(Integer offset) {
      this.blockOffset = Preconditions.checkNotNull(offset);
    }

    public void setBlockSize(Integer size) {
      this.blockSize = Preconditions.checkNotNull(size);
    }
  }

  public static class PieceUploadImpl extends PieceTransferImpl implements PieceUpload { }

  public static class PieceDownloadImpl extends PieceTransferImpl implements PieceDownload {
    boolean valid;

    public void setValidPiece(boolean valid) {
      this.valid = Preconditions.checkNotNull(valid);
    }

    public boolean wasValidPiece() {
      return valid;
    }
  }

  public static class PieceRequestImpl implements PieceRequest {
    Integer pieceIndex;
    Instant requestTime;
    Integer blockOffset;
    Integer blockSize;

    public Integer getPieceIndex() {
      return pieceIndex;
    }

    public Instant getRequestTime() {
      return requestTime;
    }

    public void setPieceIndex(Integer index) {
      this.pieceIndex = Preconditions.checkNotNull(index);
    }

    public void setRequestTime(Instant time) {
      this.requestTime = Preconditions.checkNotNull(time);
    }

    public Integer getBlockOffset() {
      return blockOffset;
    }

    public Integer getBlockSize() {
      return blockSize;
    }

    public void setBlockOffset(Integer offset) {
      this.blockOffset = Preconditions.checkNotNull(offset);
    }

    public void setBlockSize(Integer size) {
      this.blockSize = Preconditions.checkNotNull(size);
    }
  }

  public static class PieceDeclarationImpl implements PieceDeclaration {
    Instant declarationTime;
    Integer pieceIndex;

    public Instant getDeclarationTime() {
      return declarationTime;
    }

    public Integer getPieceIndex() {
      return pieceIndex;
    }

    public void setDeclarationTime(Instant time) {
      this.declarationTime = Preconditions.checkNotNull(time);
    }

    public void setPieceIndex(Integer index) {
      this.pieceIndex = Preconditions.checkNotNull(index);
    }
  }

  public void cancelLocalRequestedPiece(PieceRequest request) {
    while (piecesRequestedByLocal.remove(Preconditions.checkNotNull(request))) { }
  }

  public void cancelRemoteRequestedPiece(PieceRequest request) {
    while (piecesRequestedByRemote.remove(Preconditions.checkNotNull(request))) { }
  }

  public Instant whenDidLocalSendHandshake() {
    return this.lastLocalHandshakeSentAt;
  }

  public Pair<InterestLevel, Instant> getLocalInterestLevelInRemote() {
    return localInterestLevelInRemote;
  }

  public ImmutableList<PieceRequest> getLocalRequestedPieces() {
    return ImmutableList.copyOf(piecesRequestedByLocal);
  }

  public Pair<InterestLevel, Instant> getRemoteInterestLevelInLocal() {
    return remoteInterestLevelInLocal;
  }

  public ImmutableList<PieceRequest> getRemoteRequestedPieces() {
    return ImmutableList.copyOf(piecesRequestedByRemote);
  }

  public Instant getRemoteSentKeepAliveAt() {
    return this.lastRemoteKeepAliveSentAt;
  }

  public Instant getLocalSentKeepAliveAt() {
    return this.lastLocalKeepAliveSentAt;
  }

  public Pair<ChokeStatus, Instant> isLocalChoked() {
    return localChokeStatus;
  }

  public Pair<ChokeStatus, Instant> isRemoteChoked() {
    return remoteChokeStatus;
  }

  public ImmutableList<PieceDownload> piecesReceived() {
    return ImmutableList.copyOf(piecesDownloaded);
  }

  public ImmutableList<PieceUpload> piecesSent() {
    return ImmutableList.copyOf(piecesUploaded);
  }

  public ImmutableList<PieceDeclaration> remoteHasPieces() {
    return ImmutableList.copyOf(remoteDeclaredPieces);
  }

  public int getRemotePort() {
    return remotePort;
  }

  public int getLocalPort() {
    return localPort;
  }

  public void setLocalInterestLevelInRemote(InterestLevel interest, Instant when) {
    localInterestLevelInRemote = new Pair<InterestLevel, Instant>(
        Preconditions.checkNotNull(interest),
        Preconditions.checkNotNull(when)
    );
  }

  public void setLocalIsChoked(ChokeStatus choked, Instant when) {
    localChokeStatus = new Pair<ChokeStatus, Instant>(
        Preconditions.checkNotNull(choked),
        Preconditions.checkNotNull(when)
    );
  }

  public void setLocalRequestedPiece(PieceRequest request) {
    piecesRequestedByLocal.add(Preconditions.checkNotNull(request));
  }

  public void setLocalSentHandshakeAt(Instant when) {
    this.lastLocalHandshakeSentAt = Preconditions.checkNotNull(when);
  }

  public void setLocalSentKeepAliveAt(Instant when) {
    this.lastLocalKeepAliveSentAt = Preconditions.checkNotNull(when);
  }

  public void setLocalSentPiece(PieceUpload piece) {
    piecesUploaded.add(Preconditions.checkNotNull(piece));
  }

  public void setRemoteHasPiece(PieceDeclaration declaration) {
    remoteDeclaredPieces.add(Preconditions.checkNotNull(declaration));
  }

  public void setRemoteInterestLevelInLocal(InterestLevel interest, Instant when) {
    remoteInterestLevelInLocal = new Pair<InterestLevel, Instant>(
        Preconditions.checkNotNull(interest),
        Preconditions.checkNotNull(when)
    );
  }

  public void setRemoteIsChoked(ChokeStatus choked, Instant when) {
    remoteChokeStatus = new Pair<ChokeStatus, Instant>(
        Preconditions.checkNotNull(choked),
        Preconditions.checkNotNull(when)
    );
  }

  public void setRemoteRequestedPiece(PieceRequest request) {
    piecesRequestedByRemote.add(Preconditions.checkNotNull(request));
  }

  public void setRemoteRequestedPort(int port) {
    this.remotePort = Preconditions.checkNotNull(port);
  }

  public void setLocalRequestedPort(int port) {
    this.localPort = Preconditions.checkNotNull(port);
  }

  public void setRemoteSentHandshakeAt(Instant when) {
    this.lastRemoteHandshakeSentAt = Preconditions.checkNotNull(when);
  }

  public void setRemoteSentKeepAliveAt(Instant when) {
    this.lastRemoteKeepAliveSentAt = Preconditions.checkNotNull(when);
  }

  public void setRemoteSentPiece(PieceDownload piece) {
    piecesDownloaded.add(Preconditions.checkNotNull(piece));
  }

  public Instant whenDidRemoteSendHandshake() {
    return lastRemoteHandshakeSentAt;
  }

  public ImmutableList<PieceDeclaration> localHasPieces() {
    return ImmutableList.copyOf(localDeclaredPieces);
  }

  public void setLocalHasPiece(PieceDeclaration declaration) {
    localDeclaredPieces.add(Preconditions.checkNotNull(declaration));
  }
}
