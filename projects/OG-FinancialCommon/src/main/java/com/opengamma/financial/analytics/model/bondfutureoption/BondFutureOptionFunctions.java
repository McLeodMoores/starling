/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondfutureoption;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.util.ArgumentChecker;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class BondFutureOptionFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static FunctionConfigurationSource instance() {
    return new BondFutureOptionFunctions().getObjectCreating();
  }

  /**
   * Function repository configuration source for the default functions contained in this package.
   */
  public static class Defaults extends AbstractFunctionConfigurationBean {

    /**
     * Currency specific data.
     */
    public static class CurrencyInfo implements InitializingBean {

      private String _curveConfig;
      private String _surfaceName;

      public void setCurveConfig(final String curveConfig) {
        _curveConfig = curveConfig;
      }

      public String getCurveConfig() {
        return _curveConfig;
      }

      public void setSurfaceName(final String surfaceName) {
        _surfaceName = surfaceName;
      }

      public String getSurfaceName() {
        return _surfaceName;
      }

      @Override
      public void afterPropertiesSet() {
        ArgumentChecker.notNullInjected(getCurveConfig(), "curveConfig");
        ArgumentChecker.notNullInjected(getSurfaceName(), "surfaceName");
      }

    }

    private final Map<String, CurrencyInfo> _perCurrencyInfo = new HashMap<String, CurrencyInfo>();

    public void setPerCurrencyInfo(final Map<String, CurrencyInfo> perCurrencyInfo) {
      _perCurrencyInfo.clear();
      _perCurrencyInfo.putAll(perCurrencyInfo);
    }

    public Map<String, CurrencyInfo> getPerCurrencyInfo() {
      return _perCurrencyInfo;
    }

    public void setCurrencyInfo(final String currency, final CurrencyInfo info) {
      _perCurrencyInfo.put(currency, info);
    }

    public CurrencyInfo getCurrencyInfo(final String currency) {
      return _perCurrencyInfo.get(currency);
    }

    protected void addBondFutureOptionDefaults(final List<FunctionConfiguration> functions) {
      final String[] args = new String[getPerCurrencyInfo().size() * 3];
      int i = 0;
      for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
        args[i++] = e.getKey();
        args[i++] = e.getValue().getCurveConfig();
        args[i++] = e.getValue().getSurfaceName();
      }
      functions.add(functionConfiguration(BondFutureOptionDefaults.class, args));
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      if (!getPerCurrencyInfo().isEmpty()) {
        addBondFutureOptionDefaults(functions);
      }
    }

  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(BondFutureOptionBlackPresentValueFunction.class));
    functions.add(functionConfiguration(BondFutureOptionBlackDeltaFunction.class));
    functions.add(functionConfiguration(BondFutureOptionBlackGammaFunction.class));
    functions.add(functionConfiguration(BondFutureOptionBlackPV01Function.class));
    functions.add(functionConfiguration(BondFutureOptionBlackYCNSFunction.class));
    functions.add(functionConfiguration(BondFutureOptionBlackVegaFunction.class));
    functions.add(functionConfiguration(BondFutureOptionBlackFromFuturePresentValueFunction.class));
  }

}
