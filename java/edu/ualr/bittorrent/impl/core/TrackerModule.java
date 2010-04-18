package edu.ualr.bittorrent.impl.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;

import edu.ualr.bittorrent.interfaces.Tracker;

public class TrackerModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Tracker.class).to(TrackerImpl.class);
    bind(Integer.class).annotatedWith(TrackerRequestInterval.class).toInstance(new Integer(5000));
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.FIELD, ElementType.PARAMETER})
  @BindingAnnotation
  public @interface TrackerRequestInterval {}
}
