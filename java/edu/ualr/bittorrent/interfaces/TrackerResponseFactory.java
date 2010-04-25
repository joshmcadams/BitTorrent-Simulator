package edu.ualr.bittorrent.interfaces;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.internal.Nullable;

public interface TrackerResponseFactory {
  public TrackerResponse create(@Assisted("trackerId") byte[] trackerId,
      @Assisted("peers") ImmutableList<Peer> peers,
      @Assisted("seederCount") int seederCount,
      @Assisted("leecherCount") int leecherCount,
      @Assisted("interval") int interval,
      @Nullable @Assisted("minInterval") Integer minInterval,
      @Nullable @Assisted("warningMessage") String warningMessage,
      @Nullable @Assisted("failureMessage") String failureMessage);
}
