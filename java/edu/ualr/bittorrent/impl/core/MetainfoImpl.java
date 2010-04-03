package edu.ualr.bittorrent.impl.core;

import org.joda.time.Instant;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.ualr.bittorrent.interfaces.Metainfo;
import edu.ualr.bittorrent.interfaces.Tracker;

public class MetainfoImpl implements Metainfo {
  final ImmutableList<Tracker> trackers;
  final ImmutableList<String> pieces;
  final ImmutableList<File> files;
  final Long pieceLength;

  public MetainfoImpl(ImmutableList<Tracker> trackers, ImmutableList<String> pieces,
      Long pieceLength, ImmutableList<File> files) {
    this.trackers = Preconditions.checkNotNull(trackers);
    this.pieces = Preconditions.checkNotNull(pieces);
    this.pieceLength = Preconditions.checkNotNull(pieceLength);
    this.files = Preconditions.checkNotNull(files);

    Preconditions.checkArgument(trackers.size() > 0, "At least one tracker is required");
    Preconditions.checkArgument(pieces.size() > 0, "At least one piece is required");
    Preconditions.checkArgument(files.size() > 0, "At least one file is required");
}

  public String getComment() {
    return null; /* optional field that we are opting out of providing */
  }

  public String getCreatedBy() {
    return null; /* optional field that we are opting out of providing */
  }

  public Instant getCreationDate() {
    return null; /* optional field that we are opting out of providing */
  }

  public String getEncoding() {
    return null; /* optional field that we are opting out of providing */
  }

  public ImmutableList<File> getFiles() {
    return files;
  }

  public Long getLength() {
    if (files.size() > 1) {
      return null;
    }
    return files.get(0).getLength();
  }

  public String getMd5Sum() {
    return null; /* optional field that we are opting out of providing */
  }

  public ImmutableList<String> getName() {
    if (files.size() > 1) {
      return null;
    }
    return files.get(0).getName();
  }

  public Long getPieceLength() {
    return pieceLength;
  }

  public ImmutableList<String> getPieces() {
    return pieces;
  }

  public Integer getPrivate() {
    return null; /* optional field that we are opting out of providing */
  }

  public ImmutableList<Tracker> getTrackers() {
    return trackers;
  }

  public static class FileImpl implements Metainfo.File {
    final Long length;
    final ImmutableList<String> name;

    public FileImpl(Long length, ImmutableList<String> name) {
      this.length = Preconditions.checkNotNull(length);
      this.name = Preconditions.checkNotNull(name);
      Preconditions.checkArgument(name.size() > 0, "At least one name component is required");
    }

    public Long getLength() {
      return length;
    }

    public String getMd5Sum() {
      return null; /* optional field that we are opting out of providing */
    }

    public ImmutableList<String> getName() {
      return name;
    }

  }
}
