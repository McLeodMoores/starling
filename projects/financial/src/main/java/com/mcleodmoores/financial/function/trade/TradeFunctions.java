/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.trade;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;

/**
 * Adds functions that provide information about trades.
 */
public class TradeFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Creates an instance of this function configuration source.
   * @return  the populated source
   */
  public static FunctionConfigurationSource instance() {
    return new TradeFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(FixedRateFunction.class));
    functions.add(functionConfiguration(FxForwardDetailsFunction.class));
    functions.add(functionConfiguration(FxNdfDetailsFunction.class));
  }
}
