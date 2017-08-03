/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.discounting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 *
 */
public class DiscountingPricingFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Creates an instance of this function configuration source.
   * @return A function configuration source populated with pricing functions
   * from this package.
   */
  public static FunctionConfigurationSource instance() {
    return new DiscountingPricingFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(DiscountingAllPV01Function.class));
    functions.add(functionConfiguration(DiscountingBCSFunction.class));
    functions.add(functionConfiguration(DiscountingCurrencyExposureFunction.class));
    functions.add(functionConfiguration(DiscountingFXPVFunction.class));
    functions.add(functionConfiguration(DiscountingMarketQuoteFunction.class));
    functions.add(functionConfiguration(DiscountingPVFunction.class));
    functions.add(functionConfiguration(DiscountingParRateFunction.class));
    functions.add(functionConfiguration(DiscountingPV01Function.class));
    functions.add(functionConfiguration(DiscountingSwapLegDetailFunction.class, "false"));
    functions.add(functionConfiguration(DiscountingSwapLegDetailFunction.class, "true"));
    functions.add(functionConfiguration(DiscountingYCNSFunction.class));
    functions.add(functionConfiguration(FxForwardDiscountingPvFunction.class));

    functions.add(functionConfiguration(XCcySwapDiscountingFxPvFunction.class));
    functions.add(functionConfiguration(DiscountingInterpolatedPVFunction.class));
    functions.add(functionConfiguration(DiscountingInterpolatedAllPV01Function.class));
    functions.add(functionConfiguration(DiscountingInterpolatedPV01Function.class));
    functions.add(functionConfiguration(DiscountingInterpolatedParRateFunction.class));

    functions.add(functionConfiguration(DiscountingInflationBCSFunction.class));
    functions.add(functionConfiguration(DiscountingInflationPVFunction.class));
    functions.add(functionConfiguration(DiscountingInflationParSpreadFunction.class));
    functions.add(functionConfiguration(DiscountingInflationPV01Function.class));
    functions.add(functionConfiguration(DiscountingInflationYCNSFunction.class));
  }

  public static class FxForwardDefaults extends AbstractFunctionConfigurationBean {

    public static class CurrencyPairInfo implements InitializingBean {
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

    private final Map<UnorderedCurrencyPair, CurrencyPairInfo> _info = new HashMap<>();

    public void setCurrencyPairInfo(final Map<UnorderedCurrencyPair, CurrencyPairInfo> info) {
      _info.clear();
      _info.putAll(info);
    }

    protected void addDefaults(final List<FunctionConfiguration> functions) {
      for (final Map.Entry<UnorderedCurrencyPair, CurrencyPairInfo> entry : _info.entrySet()) {
        final UnorderedCurrencyPair key = entry.getKey();
        final CurrencyPairInfo value = entry.getValue();
        final String[] args = {
            key.getFirstCurrency().getCode(),
            key.getSecondCurrency().getCode(),
            value.getCurveExposuresName()
        };
        functions.add(functionConfiguration(FxForwardAndNdfPerCurrencyPairDefaults.class, args));
      }
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      if (!_info.isEmpty()) {
        addDefaults(functions);
      }
    }
  }

  public static class LinearRatesDefaults extends AbstractFunctionConfigurationBean {

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

    private final Map<Currency, CurrencyInfo> _info = new HashMap<>();

    public void setCurrencyInfo(final Map<Currency, CurrencyInfo> info) {
      _info.clear();
      _info.putAll(info);
    }

    protected void addDefaults(final List<FunctionConfiguration> functions) {
      for (final Map.Entry<Currency, CurrencyInfo> entry : _info.entrySet()) {
        final Currency key = entry.getKey();
        final CurrencyInfo value = entry.getValue();
        final String[] args = {
            key.getCode(),
            value.getCurveExposuresName()
        };
        functions.add(functionConfiguration(LinearRatesPerCurrencyDefaults.class, args));
      }
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      if (!_info.isEmpty()) {
        addDefaults(functions);
      }
    }
  }

}