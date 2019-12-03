/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.callspreadblack;

import java.util.List;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.analytics.model.forex.defaultproperties.FXDigitalCallSpreadBlackDefaults;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class CallSpreadBlackFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new CallSpreadBlackFunctions().getObjectCreating();
  }

  /**
   * Function repository configuration source for the defaults functions contained in this package.
   */
  public static class Defaults extends AbstractFunctionConfigurationBean {

    private double _callSpread = 0.0001;

    public void setCallSpread(final double callSpread) {
      _callSpread = callSpread;
    }

    public double getCallSpread() {
      return _callSpread;
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      functions.add(functionConfiguration(FXDigitalCallSpreadBlackDefaults.class, Double.toString(getCallSpread())));
    }

  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
  }

}
