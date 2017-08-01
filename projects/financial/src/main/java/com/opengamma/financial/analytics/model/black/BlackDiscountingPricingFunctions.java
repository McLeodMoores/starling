/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.black;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * Adds Black pricing and risk functions to the function configuration.
 */
public class BlackDiscountingPricingFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Gets an instance of this class.
   * @return The instance
   */
  public static FunctionConfigurationSource instance() {
    return new BlackDiscountingPricingFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(BlackDiscountingBCSCapFloorFunction.class));
    functions.add(functionConfiguration(BlackDiscountingPVCapFloorFunction.class));
    functions.add(functionConfiguration(BlackDiscountingPV01CapFloorFunction.class));
    functions.add(functionConfiguration(BlackDiscountingYCNSCapFloorFunction.class));

    functions.add(functionConfiguration(BlackDiscountingBCSFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingCurrencyExposureFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingForwardDeltaFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingForwardDriftlessThetaFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingForwardGammaFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingForwardVegaFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingImpliedVolatilityFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingPV01FXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingPVFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingFXPVFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingSpotDeltaFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingSpotGammaFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingValueDeltaFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingValueGammaFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingValueGammaSpotFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingValueThetaFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingValueVannaFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingValueVegaFxOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingValueVommaFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingVegaMatrixFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingVegaQuoteMatrixFXOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingYCNSFXOptionFunction.class));

    functions.add(functionConfiguration(BlackDiscountingBCSIRFutureOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingDeltaIRFutureOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingForwardIRFutureOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingGammaIRFutureOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingImpliedVolatilityIRFutureOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingPositionDeltaIRFutureOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingPositionGammaIRFutureOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingPositionVegaIRFutureOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingPV01IRFutureOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingPVIRFutureOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingValueDeltaIRFutureOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingValueGammaIRFutureOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingValueVegaIRFutureOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingWeightedVegaIRFutureOptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingYCNSIRFutureOptionFunction.class));

    functions.add(functionConfiguration(BlackDiscountingBCSSwaptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingImpliedVolatilitySwaptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingPVSwaptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingPV01SwaptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingValueVegaSwaptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingVegaMatrixSwaptionFunction.class));
    functions.add(functionConfiguration(BlackDiscountingYCNSSwaptionFunction.class));
    functions.add(functionConfiguration(ConstantBlackDiscountingBCSSwaptionFunction.class));
    functions.add(functionConfiguration(ConstantBlackDiscountingImpliedVolatilitySwaptionFunction.class));
    functions.add(functionConfiguration(ConstantBlackDiscountingPVSwaptionFunction.class));
    functions.add(functionConfiguration(ConstantBlackDiscountingPV01SwaptionFunction.class));
    functions.add(functionConfiguration(ConstantBlackDiscountingValueVegaSwaptionFunction.class));
    functions.add(functionConfiguration(ConstantBlackDiscountingYCNSSwaptionFunction.class));
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
        functions.add(functionConfiguration(BlackDiscountingVanillaFxOptionPerCurrencyPairDefaults.class, args));
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
