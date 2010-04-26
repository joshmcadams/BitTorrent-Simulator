package edu.ualr.bittorrent.interfaces;

import org.joda.time.Instant;

import com.google.common.collect.ImmutableList;

/**
 * Interface for simulated metainfo objects. All optional methods have comments
 * that indicate so. Please return null when you do not implement a method and
 * do not throw an exception. Unknown data in metainfo files is ignored by most
 * clients and missing optional fields are simply skipped in supporting clients.
 * Exceptions would just muddy things up too much.
 *
 * @author Josh McAdams
 */
public interface Metainfo {
  /**
   * Return a list of {@link Tracker}s. This method accounts for both the
   * 'announce' and 'announce-list' portions of the metainfo.
   *
   * @return list of {@link Tracker}s
   */
  public ImmutableList<Tracker> getTrackers();

  /**
   * Optional date that the torrent was created. Corresponds to the 'creation
   * date' field of the metainfo.
   *
   * @return date of torrent creation
   */
  public Instant getCreationDate();

  /**
   * Optional comment string. Corresponds to the 'comment' field of the
   * metainfo.
   *
   * @return free-form comment
   */
  public String getComment();

  /**
   * Optional name and version of the program used to create the torrent.
   * Corresponds with the 'created by' field of the metainfo.
   *
   * @return name and version of creating program
   */
  public String getCreatedBy();

  /**
   * Optional name of the string encoding format used to generate the 'pieces'
   * of metainfo typically found. Corresponds with the 'encoding' field of the
   * metainfo.
   *
   * @return encoding used when generating pieces
   */
  public String getEncoding();

  /**
   * All remaining methods correspond to data found in the 'info' has of the
   * metainfo. Since this is just a simulator, there is no need to actually pass
   * around the info, so it will not be available; however, the contents of the
   * hash will be represented as accessor methods.
   */
  public byte[] getInfoHash();

  /**
   * Length of chunks of data used to calculate pieces. This corresponds with
   * the 'piece length' field in the info hash of the metainfo.
   *
   * @return length of data chunks used to calculate piece signatures
   */
  public Integer getPieceLength();

  /**
   * Get a list of strings that represent the piece signatures for the torrent.
   * This corresponds with the 'pieces' field in the info hash of the metainfo.
   *
   * @return list of pieces
   */
  public ImmutableList<String> getPieces();

  /**
   * Optional indicator that tells clients that this torrent should be shared
   * under the umbrella of a specific set of trakers, or that the torrent can be
   * tracked by anyone. Corresponds with the 'private' field in the info hash of
   * the metainfo.
   *
   * @return numeric privacy indicator; typically 0 for non-private and 1 for
   *         private
   */
  public Integer getPrivate();

  public Integer getLastPieceIndex();
  public Integer getLastPieceSize();
  public Integer getTotalDownloadSize();

  /**
   * Interface for representing logical files in the torrent. For a single file
   * torrent, the data in the file maps directly to the info hash of the
   * metainfo. For a multiple file torrent, the data maps to individual elements
   * of the 'files' field of the info hash of the metainfo.
   */
  public interface File {
    /**
     * Get the name of the file. This corresponds with the 'name' field of the
     * info hash in a single file torrent. This corresponds with the 'name'
     * field in a single element of the 'files' list in the info hash of the
     * metainfo for a multiple-file torrent. The data is presented as a list of
     * strings. Each element in the list constitutes a path component with the
     * final element being the file name.
     *
     * @return the name of the file (possibly with a path)
     */
    public ImmutableList<String> getName();

    /**
     * Get the length of the file. This corresponds with the 'length' field of
     * the info hash in a single file torrent. This corresponds with the
     * 'length' field in a single element of the 'files' list in the info hash
     * of the metainfo for a multiple-file torrent.
     *
     * @return the length of the file
     */
    public Integer getLength();

    /**
     * Optional MD5 signature for the file. This corresponds with the 'md5sum'
     * field of the info hash in a single file torrent. This corresponds with
     * the 'md5sum' field in a single element of the 'files' list in the info
     * hash of the metainfo for a multiple-file torrent.
     *
     * @return MD5 signature of the file
     */
    public String getMd5Sum();
  }

  /**
   * For a single-file torrent, this will be the name of the file. For a
   * multi-file torrent, this is an optional advistory path in which to place
   * all of the files. The data is presented as a list of strings. Each element
   * in the list constitutes a path component with the final element being the
   * file name for a single-file torrent, or just another path element for a
   * mutli-file torrent. Note that with a multi-file torrent, the retun value
   * can be null or an empty list. For a single file torrent, this value is
   * required and cannot be an empty list.
   *
   * This method corresponds to the 'name' field in the info hash of the
   * metainfo.
   *
   * @return file path and possibly name
   */
  public ImmutableList<String> getName();

  /**
   * For a single-file torrent, this will be the length of the file. For a
   * mulit-file torrent, this will be null. This method corresponds to the
   * 'length' field in the info hash of the metainfo.
   *
   * @return required file length for a single-file torrent; null for a
   *         multi-file torrent
   */
  public Integer getLength();

  /**
   * For a single-file torrent, this will be the optional MD5 signature of the
   * file. For a mulit-file torrent, this will be null. This method corresponds
   * to the 'md5sum' field in the info hash of the metainfo.
   *
   * @return MD5 signature for a single-file torrent; null for a multi-file
   *         torrent
   */
  public String getMd5Sum();

  /**
   * Return a list of {@link File} objects found in this torrent. For a single
   * file torrent, there will be a single object. For a multiple-file torrent,
   * there can be more than one {@link File} object. This method most closely
   * resembles the 'files' field in the info hash of a multi-file torrent;
   * however, we are overloading it and pushing a single file object through
   * too.
   *
   * @return list of files represented by this torrent
   */
  public ImmutableList<File> getFiles();
}
