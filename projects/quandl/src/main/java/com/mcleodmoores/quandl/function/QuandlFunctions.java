/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.function;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.CombiningFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;

/**
 * Adds all functions for this project to the repository.
 */
public class QuandlFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Creates an instance of this function configuration source.
   * @return The function configuration source
   */
  public static FunctionConfigurationSource instance() {
    return new QuandlFunctions().getObjectCreating();
  }

  /**
   * Returns a function configuration source containing functions that produce curves.
   * @return The function configuration source
   */
  protected FunctionConfigurationSource quandlCurveConfiguration() {
    return CombiningFunctionConfigurationSource.of(QuandlCurveFunctions.instance());
  }

  @Override
  protected FunctionConfigurationSource createObject() {
    return CombiningFunctionConfigurationSource.of(super.createObject(), quandlCurveConfiguration());
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
  }
}
