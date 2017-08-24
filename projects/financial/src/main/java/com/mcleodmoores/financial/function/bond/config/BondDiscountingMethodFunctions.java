/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.bond.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.mcleodmoores.financial.function.bond.PortfolioWeightedDurationFunction;
import com.mcleodmoores.financial.function.defaults.GovernmentBondPerCountryDefaults;
import com.mcleodmoores.financial.function.defaults.GovernmentBondPerCurrencyDefaults;
import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class BondDiscountingMethodFunctions extends AbstractFunctionConfigurationBean {

  public static FunctionConfigurationSource instance() {
    return new BondDiscountingMethodFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(PortfolioWeightedDurationFunction.class));
  }

  public static class GovernmentBondDefaults extends AbstractFunctionConfigurationBean {

    public static class CountryInfo implements InitializingBean {
      private String _curveExposuresName;

      public void setCurveExposuresName(final String curveExposuresName) {
        _curveExposuresName = curveExposuresName;
      }

      public String getCurveExposuresName() {
        return _curveExposuresName;
      }

      @Override
      public void afterPropertiesSet() {
        ArgumentChecker.notNullInjected(getCurveExposuresName(), "curveExposuresName");
      }
    }

    public static class CurrencyInfo implements InitializingBean {
      private String _curveExposuresName;

      public void setCurveExposuresName(final String curveExposuresName) {
        _curveExposuresName = curveExposuresName;
      }

      public String getCurveExposuresName() {
        return _curveExposuresName;
      }

      @Override
      public void afterPropertiesSet() {
        ArgumentChecker.notNullInjected(getCurveExposuresName(), "curveExposuresName");
      }
    }

    private final Map<Currency, CurrencyInfo> _perCurrencyInfo = new HashMap<>();
    private final Map<Country, CountryInfo> _perCountryInfo = new HashMap<>();

    public void setCurrencyInfo(final Map<Currency, CurrencyInfo> info) {
      _perCurrencyInfo.clear();
      _perCountryInfo.clear();
      _perCurrencyInfo.putAll(info);
    }

    public void setCountryInfo(final Map<Country, CountryInfo> info) {
      _perCurrencyInfo.clear();
      _perCountryInfo.clear();
      _perCountryInfo.putAll(info);
    }

    protected void addDefaults(final List<FunctionConfiguration> functions) {
      for (final Map.Entry<Currency, CurrencyInfo> entry : _perCurrencyInfo.entrySet()) {
        final String[] args = {
            entry.getKey().getCode(),
            entry.getValue().getCurveExposuresName()
        };
        functions.add(functionConfiguration(GovernmentBondPerCurrencyDefaults.class, args));
      }
      for (final Map.Entry<Country, CountryInfo> entry : _perCountryInfo.entrySet()) {
        final String[] args = {
            entry.getKey().getCode(),
            entry.getValue().getCurveExposuresName()
        };
        functions.add(functionConfiguration(GovernmentBondPerCountryDefaults.class, args));
      }
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      if (!_perCountryInfo.isEmpty() || !_perCurrencyInfo.isEmpty()) {
        addDefaults(functions);
      }
    }
  }
}
