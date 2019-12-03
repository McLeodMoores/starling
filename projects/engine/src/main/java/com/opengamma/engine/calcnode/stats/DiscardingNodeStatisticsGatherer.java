/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.engine.calcnode.stats;

/**
 * Gatherer implementation that discards all received statistics.
 */
public final class DiscardingNodeStatisticsGatherer implements CalculationNodeStatisticsGatherer {

  @Override
  public void jobCompleted(final String nodeId, final int jobItems, final long executionNanos, final long durationNanos) {
    // no action
  }

  @Override
  public void jobFailed(final String nodeId, final long durationNanos) {
    // no action
  }

}
