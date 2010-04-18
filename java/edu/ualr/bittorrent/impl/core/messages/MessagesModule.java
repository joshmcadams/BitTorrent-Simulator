package edu.ualr.bittorrent.impl.core.messages;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryProvider;

import edu.ualr.bittorrent.interfaces.messages.BitFieldFactory;
import edu.ualr.bittorrent.interfaces.messages.CancelFactory;
import edu.ualr.bittorrent.interfaces.messages.ChokeFactory;
import edu.ualr.bittorrent.interfaces.messages.HandshakeFactory;
import edu.ualr.bittorrent.interfaces.messages.HaveFactory;
import edu.ualr.bittorrent.interfaces.messages.InterestedFactory;
import edu.ualr.bittorrent.interfaces.messages.KeepAliveFactory;
import edu.ualr.bittorrent.interfaces.messages.NotInterestedFactory;
import edu.ualr.bittorrent.interfaces.messages.PieceFactory;
import edu.ualr.bittorrent.interfaces.messages.PortFactory;
import edu.ualr.bittorrent.interfaces.messages.RequestFactory;
import edu.ualr.bittorrent.interfaces.messages.UnchokeFactory;

/**
 * Guice mappings to create factories for providing default implementations for messages.
 */
public class MessagesModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(BitFieldFactory.class).toProvider(
        FactoryProvider.newFactory(BitFieldFactory.class, BitFieldImpl.class));

    bind(CancelFactory.class).toProvider(
        FactoryProvider.newFactory(CancelFactory.class, CancelImpl.class));

    bind(ChokeFactory.class).toProvider(
        FactoryProvider.newFactory(ChokeFactory.class, ChokeImpl.class));

    bind(HandshakeFactory.class)
        .toProvider(
            FactoryProvider.newFactory(HandshakeFactory.class,
                HandshakeImpl.class));

    bind(HaveFactory.class).toProvider(
        FactoryProvider.newFactory(HaveFactory.class, HaveImpl.class));

    bind(InterestedFactory.class).toProvider(
        FactoryProvider.newFactory(InterestedFactory.class,
            InterestedImpl.class));

    bind(KeepAliveFactory.class)
        .toProvider(
            FactoryProvider.newFactory(KeepAliveFactory.class,
                KeepAliveImpl.class));

    bind(NotInterestedFactory.class).toProvider(
        FactoryProvider.newFactory(NotInterestedFactory.class,
            NotInterestedImpl.class));

    bind(PieceFactory.class).toProvider(
        FactoryProvider.newFactory(PieceFactory.class, PieceImpl.class));

    bind(PortFactory.class).toProvider(
        FactoryProvider.newFactory(PortFactory.class, PortImpl.class));

    bind(RequestFactory.class).toProvider(
        FactoryProvider.newFactory(RequestFactory.class, RequestImpl.class));

    bind(UnchokeFactory.class).toProvider(
        FactoryProvider.newFactory(UnchokeFactory.class, UnchokeImpl.class));

  }
}
