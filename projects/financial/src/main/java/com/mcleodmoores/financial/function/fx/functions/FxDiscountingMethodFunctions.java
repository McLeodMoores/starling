/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.fx.functions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.mcleodmoores.financial.function.defaults.FxForwardAndNdfPerCurrencyPairDefaults;
import com.mcleodmoores.financial.function.fx.FxForwardDiscountingPvFunction;
import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 *
 */
public class FxDiscountingMethodFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Creates an instance of this function configuration source.
   * @return A function configuration source populated with pricing functions
   * from this package.
   */
  public static FunctionConfigurationSource instance() {
    return new FxDiscountingMethodFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(FxForwardDiscountingPvFunction.class));
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

}
