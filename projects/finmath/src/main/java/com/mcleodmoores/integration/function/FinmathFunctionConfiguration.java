/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.function;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.CombiningFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.analytics.CurrencyPairsDefaults;
import com.opengamma.financial.analytics.model.curve.CurveFunctions;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.lambdava.functions.Function1;
import com.opengamma.util.SingletonFactoryBean;
import com.opengamma.web.spring.StandardFunctionConfiguration.CurrencyInfo;

/**
 * Constructs a function repository for Finmath functions.
 */
public class FinmathFunctionConfiguration extends AbstractFunctionConfigurationBean {
  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(FinmathFunctionConfiguration.class);
  /** The per-currency info */
  private final Map<String, CurrencyInfo> _perCurrencyInfo = new HashMap<>();

  public static FunctionConfigurationSource instance() {
    return new FinmathFunctionConfiguration().getObjectCreating();
  }

  public FinmathFunctionConfiguration() {
    setDefaultCurrencyInfo();
  }

  protected void setDefaultCurrencyInfo() {
    setCurrencyInfo("USD", usdCurrencyInfo());
  }

  protected CurrencyInfo usdCurrencyInfo() {
    return defaultCurrencyInfo("USD");
  }

  protected CurrencyInfo defaultCurrencyInfo(final String currency) {
    return new CurrencyInfo(currency);
  }

  public void setCurrencyInfo(final String currency, final CurrencyInfo info) {
    _perCurrencyInfo.put(currency, info);
  }

  public CurrencyInfo getCurrencyInfo(final String currency) {
    return _perCurrencyInfo.get(currency);
  }

  public Map<String, CurrencyInfo> getPerCurrencyInfo() {
    return _perCurrencyInfo;
  }

  protected <T> Map<String, T> getCurrencyInfo(final Function1<CurrencyInfo, T> filter) {
    final Map<String, T> result = new HashMap<>();
    for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
      final T entry = filter.execute(e.getValue());
      if (entry instanceof InitializingBean) {
        try {
          ((InitializingBean) entry).afterPropertiesSet();
        } catch (final Exception ex) {
          LOGGER.debug("Skipping {}", e.getKey());
          LOGGER.trace("Caught exception", e);
          continue;
        }
      }
      if (entry != null) {
        result.put(e.getKey(), entry);
      }
    }
    return result;
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(CurrencyPairsDefaults.class, CurrencyPairs.DEFAULT_CURRENCY_PAIRS));
  }

  protected FunctionConfigurationSource getRepository(final SingletonFactoryBean<FunctionConfigurationSource> defaults) {
    try {
      defaults.afterPropertiesSet();
    } catch (final Exception e) {
      LOGGER.warn("Caught exception ", e);
      return null;
    }
    return defaults.getObject();
  }

  protected FunctionConfigurationSource curveFunctions() {
    final CurveFunctions.Defaults defaults = new CurveFunctions.Defaults();
    setCurveDefaults(defaults);
    return getRepository(defaults);
  }

  protected void setCurveDefaults(final CurveFunctions.Defaults defaults) {
    defaults.setAbsoluteTolerance(1e-9);
    defaults.setMaximumIterations(1000);
    defaults.setRelativeTolerance(1e-9);
  }

  @Override
  protected FunctionConfigurationSource createObject() {
    return CombiningFunctionConfigurationSource.of(super.createObject(), curveFunctions());
  }
}
