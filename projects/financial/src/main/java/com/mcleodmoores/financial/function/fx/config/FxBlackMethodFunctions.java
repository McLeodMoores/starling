/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.fx.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.mcleodmoores.financial.function.defaults.VanillaFxOptionPerCurrencyPairDefaults;
import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 *
 */
public class FxBlackMethodFunctions extends AbstractFunctionConfigurationBean {

  public static FunctionConfigurationSource instance() {
    return new FxBlackMethodFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
  }

  public static class FxOptionDefaults extends AbstractFunctionConfigurationBean {

    public static class CurrencyPairInfo implements InitializingBean {
      private String _surfaceName;
      private String _curveExposuresName;
      private String _xInterpolatorName;
      private String _leftXExtrapolatorName;
      private String _rightXExtrapolatorName;

      public void setSurfaceName(final String surfaceName) {
        _surfaceName = surfaceName;
      }

      public String getSurfaceName() {
        return _surfaceName;
      }

      public void setCurveExposuresName(final String curveExposuresName) {
        _curveExposuresName = curveExposuresName;
      }

      public String getCurveExposuresName() {
        return _curveExposuresName;
      }

      public void setXInterpolatorName(final String xInterpolatorName) {
        _xInterpolatorName = xInterpolatorName;
      }

      public String getXInterpolatorName() {
        return _xInterpolatorName;
      }

      public void setLeftXExtrapolatorName(final String leftXExtrapolatorName) {
        _leftXExtrapolatorName = leftXExtrapolatorName;
      }

      public String getLeftXExtrapolatorName() {
        return _leftXExtrapolatorName;
      }

      public void setRightXExtrapolatorName(final String rightXExtrapolatorName) {
        _rightXExtrapolatorName = rightXExtrapolatorName;
      }

      public String getRightXExtrapolatorName() {
        return _rightXExtrapolatorName;
      }

      @Override
      public void afterPropertiesSet() {
        ArgumentChecker.notNullInjected(getSurfaceName(), "surfaceName");
        ArgumentChecker.notNullInjected(getCurveExposuresName(), "curveExposuresName");
        ArgumentChecker.notNullInjected(getXInterpolatorName(), "xInterpolatorName");
        ArgumentChecker.notNullInjected(getLeftXExtrapolatorName(), "leftXExtrapolatorName");
        ArgumentChecker.notNullInjected(getRightXExtrapolatorName(), "rightXExtrapolatorName");
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
            key.getFirstCurrency().toString(),
            key.getSecondCurrency().toString(),
            value.getSurfaceName(),
            value.getCurveExposuresName(),
            value.getXInterpolatorName(),
            value.getLeftXExtrapolatorName(),
            value.getRightXExtrapolatorName()
        };
        functions.add(functionConfiguration(VanillaFxOptionPerCurrencyPairDefaults.class, args));
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
