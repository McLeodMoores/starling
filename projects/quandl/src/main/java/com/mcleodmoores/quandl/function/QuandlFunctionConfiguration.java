/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.function;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.CombiningFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Sets up all functions for this project.
 */
public class QuandlFunctionConfiguration extends AbstractFunctionConfigurationBean {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(QuandlFunctionConfiguration.class);

  /**
   * Gets an instance of this function configuration source.
   * @return The function configuration source
   */
  public static FunctionConfigurationSource instance() {
    return new QuandlFunctionConfiguration().getObjectCreating();
  }

  /**
   * Adds defaults for curve functions to the repository.
   * @return The function configuration source populated with curve default functions.
   */
  protected FunctionConfigurationSource curveFunctions() {
    final QuandlCurveFunctions.Defaults defaults = new QuandlCurveFunctions.Defaults();
    setCurveDefaults(defaults);
    return getRepository(defaults);
  }

  /**
   * Sets the defaults for curves.
   * @param defaults The function configuration for curve defaults
   */
  protected void setCurveDefaults(final QuandlCurveFunctions.Defaults defaults) {
    defaults.setAbsoluteTolerance(1e-9);
    defaults.setRelativeTolerance(1e-9);
    defaults.setMaximumIterations(1000);
  }

  /**
   * Performs any validation required after the properties have been set and returns a fully-populated source.
   * @param functions The functions
   * @return The function configuration source
   */
  protected FunctionConfigurationSource getRepository(final SingletonFactoryBean<FunctionConfigurationSource> functions) {
    try {
      functions.afterPropertiesSet();
    } catch (final Exception e) {
      LOGGER.warn("Caught exception ", e);
      return null;
    }
    return functions.getObject();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functionConfigs) {
  }

  @Override
  protected FunctionConfigurationSource createObject() {
    return CombiningFunctionConfigurationSource.of(super.createObject(), curveFunctions());
  }
}
