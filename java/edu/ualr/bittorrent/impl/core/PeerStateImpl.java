package edu.ualr.bittorrent.impl.core;

import java.util.List;

import org.joda.time.Instant;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.sun.tools.javac.util.Pair;

import edu.ualr.bittorrent.interfaces.PeerState;

/**
 * Default implementation of the {@link PeerState} interface.
 */
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

  /**
   * Default implementation of the {@link PieceTransfer} interface.
   */
  public static class PieceTransferImpl implements PieceTransfer {
    Instant completionTime;
    Integer pieceIndex;
    Instant startTime;
    Integer blockOffset;
    Integer blockSize;

    /**
     * {@inheritDoc}
     */
    public Instant getCompletionTime() {
      return completionTime;
    }

    /**
     * {@inheritDoc}
     */
    public Integer getPieceIndex() {
      return pieceIndex;
    }

    /**
     * {@inheritDoc}
     */
    public Instant getStartTime() {
      return startTime;
    }

    /**
     * {@inheritDoc}
     */
    public void setCompletionTime(Instant time) {
      this.completionTime = Preconditions.checkNotNull(time);
    }

    /**
     * {@inheritDoc}
     */
    public void setPieceIndex(Integer index) {
      this.pieceIndex = Preconditions.checkNotNull(index);
    }

    /**
     * {@inheritDoc}
     */
    public void setStartTime(Instant time) {
      this.startTime = Preconditions.checkNotNull(time);
    }

    /**
     * {@inheritDoc}
     */
    public Integer getBlockOffset() {
      return blockOffset;
    }

    /**
     * {@inheritDoc}
     */
    public Integer getBlockSize() {
      return blockSize;
    }

    /**
     * {@inheritDoc}
     */
    public void setBlockOffset(Integer offset) {
      this.blockOffset = Preconditions.checkNotNull(offset);
    }

    /**
     * {@inheritDoc}
     */
    public void setBlockSize(Integer size) {
      this.blockSize = Preconditions.checkNotNull(size);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(pieceIndex, blockOffset, blockSize);
    }

    @Override
    public boolean equals(Object object) {
      if (!(object instanceof PieceTransferImpl)) {
        return false;
      }

      PieceTransferImpl other = (PieceTransferImpl) object;

      return Objects.equal(this.pieceIndex, other.pieceIndex)
          && Objects.equal(this.blockOffset, other.blockOffset)
          && Objects.equal(this.blockSize, other.blockSize);
    }

    @Override
    public String toString() {
      return String.format("PieceTransferImpl [%d][%d][%d][%s][%s]",
          pieceIndex, blockOffset, blockSize, startTime, completionTime);
    }
  }

  /**
   * Default implementation of the {@link PieceUpload} interface.
   */
  public static class PieceUploadImpl extends PieceTransferImpl implements
      PieceUpload {
    @Override
    public String toString() {
      return String.format("PieceUploadImpl [%d][%d][%d][%s][%s]", pieceIndex,
          blockOffset, blockSize, startTime, completionTime);
    }
  }

  /**
   * Default implementation of the {@link PieceDownload} interface.
   */
  public static class PieceDownloadImpl extends PieceTransferImpl implements
      PieceDownload {
    boolean valid;

    /**
     * {@inheritDoc}
     */
    public void setValidPiece(boolean valid) {
      this.valid = Preconditions.checkNotNull(valid);
    }

    /**
     * {@inheritDoc}
     */
    public boolean wasValidPiece() {
      return valid;
    }

    @Override
    public String toString() {
      return String.format("PieceDownloadImpl [%d][%d][%d][%s][%s][%s]",
          pieceIndex, blockOffset, blockSize, startTime, completionTime, valid);
    }
  }

  /**
   * Default implementation of the {@PieceRequest} interface.
   */
  public static class PieceRequestImpl implements PieceRequest {
    Integer pieceIndex;
    Instant requestTime;
    Integer blockOffset;
    Integer blockSize;

    /**
     * {@inheritDoc}
     */
    public Integer getPieceIndex() {
      return pieceIndex;
    }

    /**
     * {@inheritDoc}
     */
    public Instant getRequestTime() {
      return requestTime;
    }

    /**
     * {@inheritDoc}
     */
    public void setPieceIndex(Integer index) {
      this.pieceIndex = Preconditions.checkNotNull(index);
    }

    /**
     * {@inheritDoc}
     */
    public void setRequestTime(Instant time) {
      this.requestTime = Preconditions.checkNotNull(time);
    }

    /**
     * {@inheritDoc}
     */
    public Integer getBlockOffset() {
      return blockOffset;
    }

    /**
     * {@inheritDoc}
     */
    public Integer getBlockSize() {
      return blockSize;
    }

    /**
     * {@inheritDoc}
     */
    public void setBlockOffset(Integer offset) {
      this.blockOffset = Preconditions.checkNotNull(offset);
    }

    /**
     * {@inheritDoc}
     */
    public void setBlockSize(Integer size) {
      this.blockSize = Preconditions.checkNotNull(size);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(pieceIndex, blockOffset, blockSize);
    }

    @Override
    public boolean equals(Object object) {
      if (!(object instanceof PieceRequestImpl)) {
        return false;
      }

      PieceRequestImpl other = (PieceRequestImpl) object;

      return Objects.equal(this.pieceIndex, other.pieceIndex)
          && Objects.equal(this.blockOffset, other.blockOffset)
          && Objects.equal(this.blockSize, other.blockSize);
    }

    @Override
    public String toString() {
      return String.format("PieceRequestImpl [%d][%d][%d][%s]", pieceIndex,
          blockOffset, blockSize, requestTime);
    }
  }

  /**
   * Default implementation of the {@link PieceDeclaration} interface.
   */
  public static class PieceDeclarationImpl implements PieceDeclaration {
    Instant declarationTime;
    Integer pieceIndex;

    /**
     * {@inheritDoc}
     */
    public Instant getDeclarationTime() {
      return declarationTime;
    }

    /**
     * {@inheritDoc}
     */
    public Integer getPieceIndex() {
      return pieceIndex;
    }

    /**
     * {@inheritDoc}
     */
    public void setDeclarationTime(Instant time) {
      this.declarationTime = Preconditions.checkNotNull(time);
    }

    /**
     * {@inheritDoc}
     */
    public void setPieceIndex(Integer index) {
      this.pieceIndex = Preconditions.checkNotNull(index);
    }

    @Override
    public boolean equals(Object object) {
      if (!(object instanceof PieceDeclarationImpl)) {
        return false;
      }

      PieceDeclarationImpl other = (PieceDeclarationImpl) object;

      return Objects.equal(this.pieceIndex, other.pieceIndex);
    }

    @Override
    public String toString() {
      return String.format("PieceDeclarationImpl [%d][%s]", pieceIndex,
          declarationTime);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void cancelLocalRequestedPiece(PieceRequest request) {
    synchronized (piecesRequestedByLocal) {
      while (piecesRequestedByLocal.remove(Preconditions.checkNotNull(request))) {
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void cancelRemoteRequestedPiece(PieceRequest request) {
    synchronized (piecesRequestedByRemote) {
      while (piecesRequestedByRemote
          .remove(Preconditions.checkNotNull(request))) {
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public Instant whenDidLocalSendHandshake() {
    return this.lastLocalHandshakeSentAt;
  }

  /**
   * {@inheritDoc}
   */
  public Pair<InterestLevel, Instant> getLocalInterestLevelInRemote() {
    return localInterestLevelInRemote;
  }

  /**
   * {@inheritDoc}
   */
  public ImmutableList<PieceRequest> getLocalRequestedPieces() {
    ImmutableList<PieceRequest> requested = null;
    synchronized (piecesRequestedByLocal) {
      requested = ImmutableList.copyOf(piecesRequestedByLocal);
    }
    return requested;
  }

  /**
   * {@inheritDoc}
   */
  public Pair<InterestLevel, Instant> getRemoteInterestLevelInLocal() {
    return remoteInterestLevelInLocal;
  }

  /**
   * {@inheritDoc}
   */
  public ImmutableList<PieceRequest> getRemoteRequestedPieces() {
    return ImmutableList.copyOf(piecesRequestedByRemote);
  }

  /**
   * {@inheritDoc}
   */
  public Instant getRemoteSentKeepAliveAt() {
    return this.lastRemoteKeepAliveSentAt;
  }

  /**
   * {@inheritDoc}
   */
  public Instant getLocalSentKeepAliveAt() {
    return this.lastLocalKeepAliveSentAt;
  }

  /**
   * {@inheritDoc}
   */
  public Pair<ChokeStatus, Instant> isLocalChoked() {
    return localChokeStatus;
  }

  /**
   * {@inheritDoc}
   */
  public Pair<ChokeStatus, Instant> isRemoteChoked() {
    return remoteChokeStatus;
  }

  /**
   * {@inheritDoc}
   */
  public ImmutableList<PieceDownload> piecesReceived() {
    return ImmutableList.copyOf(piecesDownloaded);
  }

  /**
   * {@inheritDoc}
   */
  public ImmutableList<PieceUpload> piecesSent() {
    return ImmutableList.copyOf(piecesUploaded);
  }

  /**
   * {@inheritDoc}
   */
  public ImmutableList<PieceDeclaration> remoteHasPieces() {
    return ImmutableList.copyOf(remoteDeclaredPieces);
  }

  /**
   * {@inheritDoc}
   */
  public int getRemotePort() {
    return remotePort;
  }

  /**
   * {@inheritDoc}
   */
  public int getLocalPort() {
    return localPort;
  }

  /**
   * {@inheritDoc}
   */
  public void setLocalInterestLevelInRemote(InterestLevel interest, Instant when) {
    localInterestLevelInRemote = new Pair<InterestLevel, Instant>(Preconditions
        .checkNotNull(interest), Preconditions.checkNotNull(when));
  }

  /**
   * {@inheritDoc}
   */
  public void setLocalIsChoked(ChokeStatus choked, Instant when) {
    localChokeStatus = new Pair<ChokeStatus, Instant>(Preconditions
        .checkNotNull(choked), Preconditions.checkNotNull(when));
  }

  /**
   * {@inheritDoc}
   */
  public void setLocalRequestedPiece(PieceRequest request) {
    synchronized (piecesRequestedByLocal) {
      piecesRequestedByLocal.add(Preconditions.checkNotNull(request));
    }
  }

  /**
   * {@inheritDoc}
   */
  public void setLocalSentHandshakeAt(Instant when) {
    this.lastLocalHandshakeSentAt = Preconditions.checkNotNull(when);
  }

  /**
   * {@inheritDoc}
   */
  public void setLocalSentKeepAliveAt(Instant when) {
    this.lastLocalKeepAliveSentAt = Preconditions.checkNotNull(when);
  }

  /**
   * {@inheritDoc}
   */
  public void setLocalSentPiece(PieceUpload piece) {
    piecesUploaded.add(Preconditions.checkNotNull(piece));

    PieceRequest request = new PieceRequestImpl();
    request.setPieceIndex(piece.getPieceIndex());
    request.setBlockOffset(piece.getBlockOffset());
    request.setBlockSize(piece.getBlockSize());

    synchronized (piecesRequestedByRemote) {
      while (piecesRequestedByRemote.remove(request)) {
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void setRemoteHasPiece(PieceDeclaration declaration) {
    remoteDeclaredPieces.add(Preconditions.checkNotNull(declaration));
  }

  /**
   * {@inheritDoc}
   */
  public void setRemoteInterestLevelInLocal(InterestLevel interest, Instant when) {
    remoteInterestLevelInLocal = new Pair<InterestLevel, Instant>(Preconditions
        .checkNotNull(interest), Preconditions.checkNotNull(when));
  }

  /**
   * {@inheritDoc}
   */
  public void setRemoteIsChoked(ChokeStatus choked, Instant when) {
    remoteChokeStatus = new Pair<ChokeStatus, Instant>(Preconditions
        .checkNotNull(choked), Preconditions.checkNotNull(when));
  }

  /**
   * {@inheritDoc}
   */
  public void setRemoteRequestedPiece(PieceRequest request) {
    piecesRequestedByRemote.add(Preconditions.checkNotNull(request));
  }

  /**
   * {@inheritDoc}
   */
  public void setRemoteRequestedPort(int port) {
    this.remotePort = Preconditions.checkNotNull(port);
  }

  /**
   * {@inheritDoc}
   */
  public void setLocalRequestedPort(int port) {
    this.localPort = Preconditions.checkNotNull(port);
  }

  /**
   * {@inheritDoc}
   */
  public void setRemoteSentHandshakeAt(Instant when) {
    this.lastRemoteHandshakeSentAt = Preconditions.checkNotNull(when);
  }

  /**
   * {@inheritDoc}
   */
  public void setRemoteSentKeepAliveAt(Instant when) {
    this.lastRemoteKeepAliveSentAt = Preconditions.checkNotNull(when);
  }

  /**
   * {@inheritDoc}
   */
  public void setRemoteSentPiece(PieceDownload piece) {
    piecesDownloaded.add(Preconditions.checkNotNull(piece));
  }

  /**
   * {@inheritDoc}
   */
  public Instant whenDidRemoteSendHandshake() {
    return lastRemoteHandshakeSentAt;
  }

  /**
   * {@inheritDoc}
   */
  public ImmutableList<PieceDeclaration> localHasPieces() {
    return ImmutableList.copyOf(localDeclaredPieces);
  }

  /**
   * {@inheritDoc}
   */
  public void setLocalHasPiece(PieceDeclaration declaration) {
    localDeclaredPieces.add(Preconditions.checkNotNull(declaration));
  }
}
