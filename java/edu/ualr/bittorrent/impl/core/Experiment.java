package edu.ualr.bittorrent.impl.core;

import java.security.NoSuchAlgorithmException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;

import edu.ualr.bittorrent.SimulatorImpl;
import edu.ualr.bittorrent.impl.core.ExperimentModule.ExperimentTimeout;

public class Experiment {

  /**
   * Run an experiment.
   *
   * @param args
   * @throws NoSuchAlgorithmException
   */
  public static void main(String[] args) throws NoSuchAlgorithmException {
    Injector injector = Guice.createInjector(new ExperimentModule());

    SimulatorImpl simulator = injector.getInstance(SimulatorImpl.class);
    simulator.runExperiment(injector.getInstance(Key.get(Long.class,
        ExperimentTimeout.class)));
  }
}
