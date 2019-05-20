/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.bond.functions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.mcleodmoores.financial.function.bond.PortfolioWeightedDurationFunction;
import com.mcleodmoores.financial.function.defaults.BondPerCountryDefaults;
import com.mcleodmoores.financial.function.defaults.BondPerCountryDefaults.BondType;
import com.mcleodmoores.financial.function.defaults.BondPerCurrencyDefaults;
import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

/**
 * Adds functions that use discounting to produce outputs for bonds.
 */
public class BondDiscountingMethodFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Returns an instance of this configuration.
   *
   * @return an instance
   */
  public static FunctionConfigurationSource instance() {
    return new BondDiscountingMethodFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(PortfolioWeightedDurationFunction.class));
  }

  /**
   * Provides default property values for bonds.
   */
  public static class BondDefaults extends AbstractFunctionConfigurationBean {

    /**
     * Provides per-country property values for bonds.
     */
    public static class CountryInfo implements InitializingBean {
      private String _curveExposuresName;
      private BondType _bondType;

      /**
       * Sets the curve exposure function name.
       *
       * @param curveExposuresName
       *          the name, not null
       */
      public void setCurveExposuresName(final String curveExposuresName) {
        _curveExposuresName = curveExposuresName;
      }

      /**
       * Gets the curve exposure function name.
       *
       * @return the name
       */
      public String getCurveExposuresName() {
        return _curveExposuresName;
      }

      /**
       * Sets the bond type.
       *
       * @param bondType
       *          the type, not null
       */
      public void setBondType(final String bondType) {
        _bondType = BondType.valueOf(bondType);
      }

      /**
       * Gets the bond type.
       *
       * @return the type
       */
      public BondType getBondType() {
        return _bondType;
      }

      @Override
      public void afterPropertiesSet() {
        ArgumentChecker.notNullInjected(getCurveExposuresName(), "curveExposuresName");
        ArgumentChecker.notNullInjected(getBondType(), "bondType");
      }
    }

    /**
     * Provides per-currency property values for bonds.
     */
    public static class CurrencyInfo implements InitializingBean {
      private String _curveExposuresName;
      private BondType _bondType;

      /**
       * Sets the curve exposure function name.
       *
       * @param curveExposuresName
       *          the name, not null
       */
      public void setCurveExposuresName(final String curveExposuresName) {
        _curveExposuresName = curveExposuresName;
      }

      /**
       * Gets the curve exposure function name.
       *
       * @return the name
       */
      public String getCurveExposuresName() {
        return _curveExposuresName;
      }

      /**
       * Sets the bond type.
       *
       * @param bondType
       *          the type, not null
       */
      public void setBondType(final String bondType) {
        _bondType = BondType.valueOf(bondType);
      }

      /**
       * Gets the bond type.
       *
       * @return the type
       */
      public BondType getBondType() {
        return _bondType;
      }

      @Override
      public void afterPropertiesSet() {
        ArgumentChecker.notNullInjected(getCurveExposuresName(), "curveExposuresName");
        ArgumentChecker.notNullInjected(getBondType(), "bondType");
      }
    }

    private final Map<Currency, CurrencyInfo> _perCurrencyInfo = new HashMap<>();
    private final Map<Country, CountryInfo> _perCountryInfo = new HashMap<>();

    /**
     * Sets per-currency property values.
     *
     * @param info
     *          the values
     */
    public void setCurrencyInfo(final Map<Currency, CurrencyInfo> info) {
      _perCurrencyInfo.clear();
      _perCountryInfo.clear();
      _perCurrencyInfo.putAll(info);
    }

    /**
     * Sets the per-country property values.
     *
     * @param info
     *          the values
     */
    public void setCountryInfo(final Map<Country, CountryInfo> info) {
      _perCurrencyInfo.clear();
      _perCountryInfo.clear();
      _perCountryInfo.putAll(info);
    }

    /**
     * Adds functions providing default values.
     *
     * @param functions
     *          a list of functions
     */
    protected void addDefaults(final List<FunctionConfiguration> functions) {
      for (final Map.Entry<Currency, CurrencyInfo> entry : _perCurrencyInfo.entrySet()) {
        final String[] args = { entry.getKey().getCode(), entry.getValue().getCurveExposuresName(), entry.getValue().getBondType().name() };
        functions.add(functionConfiguration(BondPerCurrencyDefaults.class, args));
      }
      for (final Map.Entry<Country, CountryInfo> entry : _perCountryInfo.entrySet()) {
        final String[] args = { entry.getKey().getCode(), entry.getValue().getCurveExposuresName(), entry.getValue().getBondType().name() };
        functions.add(functionConfiguration(BondPerCountryDefaults.class, args));
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
