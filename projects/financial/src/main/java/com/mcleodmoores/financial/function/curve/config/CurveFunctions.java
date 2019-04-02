/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.curve.config;

import java.util.List;

import com.mcleodmoores.financial.function.curve.NelsonSiegelBondCurveFunction;
import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;

/**
 * Function repository configuration source for the functions that create curves.
 */
public class CurveFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a source exposing the functions.
   * 
   * @return the source
   */
  public static FunctionConfigurationSource instance() {
    return new CurveFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(NelsonSiegelBondCurveFunction.class));
  }
}
