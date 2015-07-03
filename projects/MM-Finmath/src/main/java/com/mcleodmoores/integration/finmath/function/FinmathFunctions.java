/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.finmath.function;

import java.util.List;

import com.mcleodmoores.integration.finmath.function.curve.CurveFunctions;
import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.CombiningFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.analytics.ircurve.IRCurveFunctions;

/**
 *
 */
public class FinmathFunctions extends AbstractFunctionConfigurationBean {

  public static FunctionConfigurationSource instance() {
    return new FinmathFunctions().getObjectCreating();
  }

  protected FunctionConfigurationSource finmathCurveFunctionConfiguration() {
    return CurveFunctions.instance();
  }

  @Override
  protected FunctionConfigurationSource createObject() {
    return CombiningFunctionConfigurationSource.of(super.createObject(), finmathCurveFunctionConfiguration(),
        IRCurveFunctions.instance(), com.opengamma.financial.analytics.model.curve.CurveFunctions.instance());
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    //TODO
  }
}
