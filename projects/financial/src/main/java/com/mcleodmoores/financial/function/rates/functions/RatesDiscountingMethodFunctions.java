/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.rates.functions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.mcleodmoores.financial.function.defaults.LinearRatesPerCurrencyDefaults;
import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.analytics.model.discounting.DiscountingPricingFunctions;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class RatesDiscountingMethodFunctions extends AbstractFunctionConfigurationBean {

  public static FunctionConfigurationSource instance() {
    return new DiscountingPricingFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
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
