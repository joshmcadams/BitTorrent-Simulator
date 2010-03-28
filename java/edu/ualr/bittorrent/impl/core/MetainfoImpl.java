package edu.ualr.bittorrent.impl.core;

import com.google.common.collect.ImmutableList;

import edu.ualr.bittorrent.interfaces.Metainfo;

public class MetainfoImpl implements Metainfo {

  public ImmutableList<String> getPieces() {
    return ImmutableList.of(
        "12345678901234567890",
        "12345678901234567891",
        "12345678901234567892",
        "12345678901234567893",
        "12345678901234567894"
    );
  }
}
