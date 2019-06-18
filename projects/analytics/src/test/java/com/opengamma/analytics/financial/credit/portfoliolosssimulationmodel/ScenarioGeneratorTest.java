/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.portfoliolosssimulationmodel;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.credit.recoveryratemodel.RecoveryRateModel;
import com.opengamma.analytics.financial.credit.underlyingpool.UnderlyingPoolDummyPool;
import com.opengamma.analytics.financial.credit.underlyingpool.definition.UnderlyingPool;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 * @deprecated Deprecated
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class ScenarioGeneratorTest {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO :

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Flag to control if any test results are output to the console
  private static final boolean OUTPUT_RESULTS = false;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private static final int NUMBER_OF_SIMULATIONS = 12;

  private static final int DEFAULT_SIMULATION_SEED = 987654321;
  private static final int RECOVERY_RATE_SIMULATION_SEED = 987654321;

  private static final double SIMULATION_TIME_HORIZON = 1.0;

  private static final double HOMOGENEOUS_DEFAULT_CORRELATION = 0.5;
  private static final double HOMOGENEOUS_RECOVERY_CORRELATION = 0.0;
  private static final double HOMOGENOUS_DEFAULT_PROBABILITY = 0.5;

  // Create a pool construction object
  private static final UnderlyingPoolDummyPool POOL = new UnderlyingPoolDummyPool();

  // Build the underlying pool
  private static final UnderlyingPool OBLIGOR_UNIVERSE = UnderlyingPoolDummyPool.constructPool();

  // Extract the number of obligors in the simulation universe from the UnderlyingPool
  private static final int NUMBER_OF_OBLIGORS = OBLIGOR_UNIVERSE.getNumberOfObligors();

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private static final SimulationMethods SIM_METHODS = new SimulationMethods();

  private static final SimulationEngine SIMULATION_ENGINE = new SimulationEngine();

  private static final RecoveryRateModel[] RECOVERY_RATE_MODELS = SIM_METHODS.constructRecoveryRateModels(NUMBER_OF_OBLIGORS);

  private static final double[] DEFAULT_CORRELATION_VECTOR = SIM_METHODS.constructCorrelationVector(NUMBER_OF_OBLIGORS, HOMOGENEOUS_DEFAULT_CORRELATION);
  private static final double[] RECOVERY_CORRELATION_VECTOR = SIM_METHODS.constructCorrelationVector(NUMBER_OF_OBLIGORS, HOMOGENEOUS_RECOVERY_CORRELATION);
  private static final double[] DEFAULT_PROBABILITY_VECTOR = SIM_METHODS.constructCorrelationVector(NUMBER_OF_OBLIGORS, HOMOGENOUS_DEFAULT_PROBABILITY);

  private static final ScenarioGenerator SCENARIO_GENERATOR = new ScenarioGenerator(
      OBLIGOR_UNIVERSE,
      RECOVERY_RATE_MODELS,
      NUMBER_OF_SIMULATIONS,
      DEFAULT_SIMULATION_SEED,
      RECOVERY_RATE_SIMULATION_SEED,
      SIMULATION_TIME_HORIZON,
      DEFAULT_CORRELATION_VECTOR,
      RECOVERY_CORRELATION_VECTOR,
      DEFAULT_PROBABILITY_VECTOR);

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Test
  public void testCreditPortfolioLossModelScenarioGenerator() {

    SIMULATION_ENGINE.runSimulation(SCENARIO_GENERATOR, OUTPUT_RESULTS);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
