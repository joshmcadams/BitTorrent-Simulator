package edu.ualr.bittorrent.impl.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.TypeLiteral;

import edu.ualr.bittorrent.Simulator;
import edu.ualr.bittorrent.SimulatorImpl;
import edu.ualr.bittorrent.interfaces.Metainfo;
import edu.ualr.bittorrent.interfaces.PeerProvider;
import edu.ualr.bittorrent.interfaces.Tracker;

public class ExperimentModule extends AbstractModule {
  private static final int DEFAULT_TRACKER_REQUEST_INTERVAL = 5000;
  private static final int DEFAULT_PIECE_LENGTH = 100;
  private static final int DEFAULT_PIECE_COUNT = 10;
  private static final int DEFAULT_THREAD_COUNT = 100;
  private static final ImmutableList<String> DEFAULT_FILE_NAME = ImmutableList
      .of("x.txt");

  @Override
  protected void configure() {
    // bindings for an individual tracker
    bind(Tracker.class).to(TrackerImpl.class);
    bindConstant().annotatedWith(TrackerRequestInterval.class).to(
        DEFAULT_TRACKER_REQUEST_INTERVAL);

    // binding for a list of trackers
    Tracker tracker = new TrackerImpl(DEFAULT_TRACKER_REQUEST_INTERVAL);
    ImmutableList<Tracker> trackers = ImmutableList.of(tracker);
    bind(new TypeLiteral<ImmutableList<Tracker>>() {
    }).toInstance(trackers);

    // binding for a map of data per piece
    Map<Integer, byte[]> data = Maps.newHashMap();
    bind(new TypeLiteral<Map<Integer, byte[]>>() {
    }).toInstance(data);

    List<String> pieces = Lists.newArrayList();

    for (int i = 0; i < DEFAULT_PIECE_COUNT; i++) {
      StringBuilder stringBuilder = new StringBuilder(DEFAULT_PIECE_LENGTH);
      for (int j = 0; j < DEFAULT_PIECE_LENGTH; j++) {
        stringBuilder.append('A');
      }
      String dataPiece = stringBuilder.toString();
      data.put(i, dataPiece.getBytes());
      try {
        pieces.add(new String(MessageDigest.getInstance("SHA").digest(
            dataPiece.getBytes())));
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
      }
    }

    bind(new TypeLiteral<ImmutableList<String>>() {
    }).toInstance(ImmutableList.copyOf(pieces));

    Metainfo.File file = new MetainfoImpl.FileImpl(new Long(
        DEFAULT_PIECE_LENGTH * DEFAULT_PIECE_COUNT), DEFAULT_FILE_NAME);
    ImmutableList<Metainfo.File> files = ImmutableList.of(file);

    bind(new TypeLiteral<ImmutableList<Metainfo.File>>() {
    }).toInstance(ImmutableList.copyOf(files));
    bindConstant().annotatedWith(PieceLength.class).to(DEFAULT_PIECE_LENGTH);
    bindConstant().annotatedWith(ThreadCount.class).to(DEFAULT_THREAD_COUNT);
    bind(Metainfo.class).to(MetainfoImpl.class);
    bind(PeerProvider.class).to(PeerProviderImpl.class);
    bind(Simulator.class).to(SimulatorImpl.class);
    bindConstant().annotatedWith(ExperimentTimeout.class).to(10000000L);
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target( { ElementType.FIELD, ElementType.PARAMETER })
  @BindingAnnotation
  public @interface ExperimentTimeout {
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target( { ElementType.FIELD, ElementType.PARAMETER })
  @BindingAnnotation
  public @interface ThreadCount {
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target( { ElementType.FIELD, ElementType.PARAMETER })
  @BindingAnnotation
  public @interface PieceLength {
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target( { ElementType.FIELD, ElementType.PARAMETER })
  @BindingAnnotation
  public @interface Trackers {
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target( { ElementType.FIELD, ElementType.PARAMETER })
  @BindingAnnotation
  public @interface TorrentData {
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target( { ElementType.FIELD, ElementType.PARAMETER })
  @BindingAnnotation
  public @interface TrackerRequestInterval {
  }
}
