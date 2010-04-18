package edu.ualr.bittorrent.impl.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.TypeLiteral;

import edu.ualr.bittorrent.interfaces.Tracker;

public class TrackerModule extends AbstractModule {
  private static final int DEFAULT_TRACKER_REQUEST_INTERVAL = 5000;

  @Override
  protected void configure() {
    bind(Tracker.class).to(TrackerImpl.class);
    bindConstant().annotatedWith(TrackerRequestInterval.class).to(DEFAULT_TRACKER_REQUEST_INTERVAL);

    Tracker tracker = new TrackerImpl(DEFAULT_TRACKER_REQUEST_INTERVAL);
    ImmutableList<Tracker> trackers = ImmutableList.of(tracker);
    TypeLiteral<List<Tracker>> type = new TypeLiteral<List<Tracker>>() {};
    bind(type).toInstance(trackers);
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.FIELD, ElementType.PARAMETER})
  @BindingAnnotation
  public @interface Trackers {}

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.FIELD, ElementType.PARAMETER})
  @BindingAnnotation
  public @interface TrackerRequestInterval {}
}
