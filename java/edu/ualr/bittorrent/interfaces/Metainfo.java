package edu.ualr.bittorrent.interfaces;

import com.google.common.collect.ImmutableList;

public interface Metainfo {
  ImmutableList<String> getPieces();
}
