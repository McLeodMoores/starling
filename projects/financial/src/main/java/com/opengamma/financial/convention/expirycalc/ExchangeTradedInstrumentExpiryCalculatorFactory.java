/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.expirycalc;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Factory containing instances of {@code ExchangeTradedInstrumentExpiryCalculator}.
 */
public class ExchangeTradedInstrumentExpiryCalculatorFactory {

  /** Map containing the instances */
  private static final BiMap<String, ExchangeTradedInstrumentExpiryCalculator> INSTANCES = HashBiMap.create();

  //TODO this should be moved out so that calculators can be added more easily (as for the daycount factory)
  static {
    INSTANCES.put(BondFutureOptionExpiryCalculator.NAME, BondFutureOptionExpiryCalculator.getInstance());
    INSTANCES.put(IMMFutureAndFutureOptionQuarterlyExpiryCalculator.NAME, IMMFutureAndFutureOptionQuarterlyExpiryCalculator.getInstance());
    INSTANCES.put(IMMFutureAndFutureOptionMonthlyExpiryCalculator.NAME, IMMFutureAndFutureOptionMonthlyExpiryCalculator.getInstance());
    INSTANCES.put(SoybeanFutureExpiryCalculator.NAME, SoybeanFutureExpiryCalculator.getInstance());
    INSTANCES.put(SoybeanFutureOptionExpiryCalculator.NAME, SoybeanFutureOptionExpiryCalculator.getInstance());
    INSTANCES.put(FedFundFutureAndFutureOptionMonthlyExpiryCalculator.NAME, FedFundFutureAndFutureOptionMonthlyExpiryCalculator.getInstance());
  }

  /**
   * Gets the named calculator.
   * 
   * @param name  the name of the expiry calculator, not null
   * @return  the calculator, not null
   * @throws IllegalArgumentException if the calculator was not found in the map
   */
  public static ExchangeTradedInstrumentExpiryCalculator getCalculator(final String name) {
    final ExchangeTradedInstrumentExpiryCalculator calculator = INSTANCES.get(name);
    if (calculator != null) {
      return calculator;
    }
    throw new IllegalArgumentException("Could not get calculator called " + name);
  }

}
