/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode.stats;

/**
 * Gatherer implementation that discards all received statistics.
 */
public class DiscardingInvocationStatisticsGatherer implements FunctionInvocationStatisticsGatherer {

  @Override
  public void functionInvoked(
      final String configurationName, final String functionId, final int invocationCount,
      final double executionNanos, final double dataInputBytes, final double dataOutputBytes) {
    // no action
  }

}
