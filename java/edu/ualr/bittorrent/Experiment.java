package edu.ualr.bittorrent;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;

import edu.ualr.bittorrent.impl.core.ExperimentModule;
import edu.ualr.bittorrent.impl.core.SimulatorImpl;
import edu.ualr.bittorrent.impl.core.ExperimentModule.ExperimentTimeout;

public class Experiment {

  /**
   * Run an experiment.
   *
   * @param args
   */
  public static void main(String[] args) {
    Injector injector = Guice.createInjector(new ExperimentModule());
    SimulatorImpl simulator = injector.getInstance(SimulatorImpl.class);
    simulator.runExperiment(injector.getInstance(Key.get(Long.class,
        ExperimentTimeout.class)));
  }
}
