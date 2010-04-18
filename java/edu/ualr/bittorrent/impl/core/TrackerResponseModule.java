package edu.ualr.bittorrent.impl.core;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryProvider;

import edu.ualr.bittorrent.interfaces.TrackerResponseFactory;

public class TrackerResponseModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(TrackerResponseFactory.class).toProvider(
        FactoryProvider.newFactory(TrackerResponseFactory.class, TrackerResponseImpl.class));
  }
}
