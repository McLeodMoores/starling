/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.engine.exec.stats;

import com.opengamma.id.UniqueId;

/**
 * Discards any statistics.
 */
public class DiscardingGraphStatisticsGathererProvider implements GraphExecutorStatisticsGathererProvider {

  /**
   * Instance of a statistics gatherer that doesn't do anything.
   */
  public static final GraphExecutorStatisticsGatherer GATHERER_INSTANCE = new GraphExecutorStatisticsGatherer() {

    @Override
    public void graphExecuted(final String calcConfig, final int nodeCount, final long executionTime, final long duration) {
      // No action
    }

    @Override
    public void graphProcessed(final String calcConfig, final int totalJobs, final double meanJobSize, final double meanJobCycleCost,
        final double meanJobIOCost) {
      // No action
    }

  };

  @Override
  public GraphExecutorStatisticsGatherer getStatisticsGatherer(final UniqueId viewProcessId) {
    return GATHERER_INSTANCE;
  }

}
