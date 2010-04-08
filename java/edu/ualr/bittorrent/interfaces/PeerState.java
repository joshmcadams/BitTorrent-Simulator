package edu.ualr.bittorrent.interfaces;

import org.joda.time.Instant;

import com.google.common.collect.ImmutableList;
import com.sun.tools.javac.util.Pair;

/**
 * Object used by "local" clients to encapsulates the state held between a local peer and a remote
 * peer. The interface is built and named from the perspective of the local peer/client.
 *
 * @author jmcadams
 */
public interface PeerState {
  /**
   * Pieces sent between peers are noted by a piece transfer object that logs relevant information
   * about the transfer. This interface is primarily used as a base for {@link PieceUpload} and
   * {@link PieceDownload} interfaces.
   */
  interface PieceTransfer {
    /**
     * Sets the index for the piece that was transferred.
     *
     * @param index
     */
    void setPieceIndex(Integer index);

    /**
     * Returns the index of the transferred piece.
     *
     * @return
     */
    Integer getPieceIndex();

    /**
     * Sets the time that the transfer began.
     *
     * @param time
     */
    void setStartTime(Instant time);

    /**
     * Returns the time that the transfer began.
     *
     * @return
     */
    Instant getStartTime();

    /**
     * Sets the time that the transfer completed.
     *
     * @param time
     */
    void setCompletionTime(Instant time);

    /**
     * Returns the time that the transfer completed.
     *
     * @return
     */
    Instant getCompletionTime();

    /**
     * Sets the starting position of the bytes requested in the piece.
     *
     * @param offset
     */
    void setBlockOffset(Integer offset);

    /**
     * Returns the starting position of the bytes requested in the piece.
     *
     * @return
     */
    Integer getBlockOffset();

    /**
     * Sets the size of the data block requested.
     *
     * @param size
     */
    void setBlockSize(Integer size);

    /**
     * Returns the size of the data block requested.
     *
     * @return
     */
    Integer getBlockSize();
  }

  public interface PieceUpload extends PieceTransfer {}

  public interface PieceDownload extends PieceTransfer {
    /**
     * Set this value to true if the piece that was transferred was a valid piece. If the piece
     * failed checksumming or was invalid for some other reason, set the value to false.
     *
     * @param wasValid
     */
    void setValidPiece(boolean valid);

    /**
     * Returns true if the piece was valid.
     *
     * @return
     */
    boolean wasValidPiece();
  }

  /**
   * When either the local or remote peer makes a request for a piece, that request is logged in a
   * piece request object.
   */
  public interface PieceRequest {
    /**
     * Set the index of the requested piece.
     *
     * @param index
     */
    void setPieceIndex(Integer index);

    /**
     * Get the index of the requested piece.
     *
     * @return
     */
    Integer getPieceIndex();

    /**
     * Set the time of the requst.
     *
     * @param time
     */
    void setRequestTime(Instant time);

    /**
     * Get the time of the request.
     *
     * @return
     */
    Instant getRequestTime();

    /**
     * Sets the starting position of the bytes requested in the piece.
     *
     * @param offset
     */
    void setBlockOffset(Integer offset);

    /**
     * Returns the starting position of the bytes requested in the piece.
     *
     * @return
     */
    Integer getBlockOffset();

    /**
     * Sets the size of the data block requested.
     *
     * @param size
     */
    void setBlockSize(Integer size);

    /**
     * Returns the size of the data block requested.
     *
     * @return
     */
    Integer getBlockSize();
  }

  /**
   * Remote peers declare which pieces they have. These declarations are packaged in a piece
   * declaration object.
   */
  public interface PieceDeclaration {
    /**
     * Set the declared piece index.
     *
     * @param index
     */
    void setPieceIndex(Integer index);

    /**
     * Get the declared piece index.
     *
     * @return
     */
    Integer getPieceIndex();

    /**
     * Set the time of declaration.
     *
     * @param time
     */
    void setDeclarationTime(Instant time);

    /**
     * Get the time of declaration.
     *
     * @return
     */
    Instant getDeclarationTime();
  }

  public enum ChokeStatus {
    CHOKED,
    UNCHOKED
  };

  /**
   * Set a true/false value for when the remote is choked.
   *
   * @param choked
   * @param when
   */
  void setRemoteIsChoked(ChokeStatus choked, Instant when);

  /**
   * Return a boolean indicating whether or not the remote is choked or unchoked an when it
   * happened.
   *
   * @return
   */
  Pair<ChokeStatus, Instant> isRemoteChoked();

  /**
   * Set a true/false value for when the local is choked.
   *
   * @param choked
   * @param when
   */
  void setLocalIsChoked(ChokeStatus choked, Instant when);

  /**
   * Return a boolean indicating whether or not the local is choked or unchoked an when it happened.
   *
   * @return
   */
  Pair<ChokeStatus, Instant> isLocalChoked();

  /**
   * Indicate when the remote sent a handshake.
   *
   * @param when
   */
  void setRemoteSentHandshakeAt(Instant when);

  /**
   * Return when the remote sent a handshake.
   *
   * @return
   */
  Instant whenDidRemoteSendHandshake();

  /**
   * Indicate when the local sent a handshake.
   *
   * @param when
   */
  void setLocalSentHandshakeAt(Instant when);

  /**
   * Return when the local sent a handshake.
   *
   * @return
   */
  Instant whenDidLocalSendHandshake();

  public enum InterestLevel {
    INTERESTED,
    NOT_INTERESTED
  };

  /**
   * When the remote says that it is interested or uninterested in the local, log it here.
   *
   * @param interest
   * @param when
   */
  void setRemoteInterestLevelInLocal(InterestLevel interest, Instant when);

  /**
   * Get the interest level of the remote in the local.
   *
   * @return
   */
  Pair<InterestLevel, Instant> getRemoteInterestLevelInLocal();

  /**
   * When the local tells the remote it is interested or uninterested in the remote, log it here.
   *
   * @param interest
   * @param when
   */
  void setLocalInterestLevelInRemote(InterestLevel interest, Instant when);

  /**
   * Get the interest level of the local in the remote.
   *
   * @return
   */
  Pair<InterestLevel, Instant> getLocalInterestLevelInRemote();

  /**
   * Set when the remote sends a keep alive.
   *
   * @param when
   */
  void setRemoteSentKeepAliveAt(Instant when);

  /**
   * Get when the remote sent the last keep alive.
   *
   * @return
   */
  Instant getRemoteSentKeepAliveAt();

  /**
   * Set when the local sends a keep alive.
   *
   * @param when
   */
  void setLocalSentKeepAliveAt(Instant when);

  /**
   * Get when the local sent the last keep alive.
   *
   * @return
   */
  Instant getLocalSentKeepAliveAt();

  /**
   * Log when the remote notifies the local that it has another piece.
   *
   * @param declaration
   */
  void setRemoteHasPiece(PieceDeclaration declaration);

  /**
   * Get the list of pieces that the remote has.
   *
   * @return
   */
  ImmutableList<PieceDeclaration> remoteHasPieces();

  /**
   * Log when the local notifies the remote that it has another piece.
   *
   * @param declaration
   */
  void setLocalHasPiece(PieceDeclaration declaration);

  /**
   * Get the list of pieces that the local has.
   *
   * @return
   */
  ImmutableList<PieceDeclaration> localHasPieces();

  /**
   * Log when the remote requests a piece.
   *
   * @param request
   */
  void setRemoteRequestedPiece(PieceRequest request);

  /**
   * Get the list of pieces that the remote has requested.
   *
   * @return
   */
  ImmutableList<PieceRequest> getRemoteRequestedPieces();

  /**
   * When a remote cancels a request, forget about it.
   *
   * @param request
   */
  void cancelRemoteRequestedPiece(PieceRequest request);

  /**
   * Log when the local requests a piece.
   *
   * @param request
   */
  void setLocalRequestedPiece(PieceRequest request);

  /**
   * Get the list of pieces that the local has requested.
   *
   * @return
   */
  ImmutableList<PieceRequest> getLocalRequestedPieces();

  /**
   * When the local cancels a request, forget about it.
   *
   * @param request
   */
  void cancelLocalRequestedPiece(PieceRequest request);

  /**
   * Log when the local uploads a piece.
   *
   * @param piece
   */
  void setLocalSentPiece(PieceUpload piece);

  /**
   * Get information about uploads sent to this remote.
   *
   * @return
   */
  ImmutableList<PieceUpload> piecesSent();

  /**
   * Log when the remote sends a piece.
   *
   * @param piece
   */
  void setRemoteSentPiece(PieceDownload piece);

  /**
   * Get the list of pieces that the remote has sent.
   *
   * @return
   */
  ImmutableList<PieceDownload> piecesReceived();

  /**
   * Note the port that the remote requested to be communicated with on.
   *
   * @param port
   */
  void setRemoteRequestedPort(int port);

  /**
   * Get the port that the remote requested to be communicated with on.
   * @return
   */
  int requestedPort();
}
