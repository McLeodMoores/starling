/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.examples.simulated.function;

import static com.opengamma.engine.value.ValuePropertyNames.DIVIDEND_TYPE_NONE;

import java.util.List;

import com.mcleodmoores.examples.simulated.loader.config.ExamplesViewsPopulator;
import com.opengamma.analytics.math.interpolation.factory.DoubleQuadraticInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LinearExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LinearInterpolator1dAdapter;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

public class ExamplesSimulatedFunctionConfiguration extends ExamplesFunctionConfiguration {

  public static FunctionConfigurationSource instance() {
    return new ExamplesSimulatedFunctionConfiguration().getObjectCreating();
  }

  public ExamplesSimulatedFunctionConfiguration() {
    setMark2MarketField("CLOSE");
    setCostOfCarryField("COST_OF_CARRY");
    setAbsoluteTolerance(0.0001);
    setRelativeTolerance(0.0001);
    setMaximumIterations(1000);
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    super.addAllConfigurations(functions);
  }

  @Override
  protected void setEquityOptionInfo() {
    setEquityOptionInfo("AAPL", "USD");
  }

  @Override
  protected void setFxOptionInfo() {
    final UnorderedCurrencyPair[] currencies = ExamplesViewsPopulator.CURRENCY_PAIRS;
    for (final UnorderedCurrencyPair c : currencies) {
      setVanillaFxOptionInfo(c.getFirstCurrency(), c.getSecondCurrency());
    }
  }

  @Override
  protected void setFxForwardInfo() {
    final UnorderedCurrencyPair[] currencies = ExamplesViewsPopulator.CURRENCY_PAIRS;
    for (final UnorderedCurrencyPair c : currencies) {
      setFxForwardInfo(c.getFirstCurrency(), c.getSecondCurrency());
    }
  }

  /**
   * Creates empty default per-equity information objects for equity options.
   * @param ticker The equity ticker
   * @param curveCurrency The currency target of the discounting curve (usually, but not necessarily,
   * the currency of the equity).
   */
  protected void setEquityOptionInfo(final String ticker, final String curveCurrency) {
    final EquityInfo i = defaultEquityInfo(ticker);
    final String discountingCurveConfigName = "DefaultTwoCurve" + curveCurrency + "Config";
    i.setDiscountingCurve("model/equityoption", "Discounting");
    i.setDiscountingCurveConfig("model/equityoption", discountingCurveConfigName);
    i.setDiscountingCurveCurrency("model/equityoption", curveCurrency);
    i.setDividendType("model/equityoption", DIVIDEND_TYPE_NONE);
    i.setForwardCurve("model/equityoption", "Discounting");
    i.setForwardCurveCalculationMethod("model/equityoption", ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD);
    i.setForwardCurveInterpolator("model/equityoption", DoubleQuadraticInterpolator1dAdapter.NAME);
    i.setForwardCurveLeftExtrapolator("model/equityoption", LinearExtrapolator1dAdapter.NAME);
    i.setForwardCurveRightExtrapolator("model/equityoption", LinearExtrapolator1dAdapter.NAME);
    i.setSurfaceCalculationMethod("model/equityoption", BlackVolatilitySurfacePropertyNamesAndValues.INTERPOLATED_BLACK_LOGNORMAL);
    i.setSurfaceInterpolationMethod("model/equityoption", BlackVolatilitySurfacePropertyNamesAndValues.SPLINE);
    i.setVolatilitySurface("model/equityoption", "DEFAULT");
    setEquityInfo(ticker, i);
  }

  protected void setVanillaFxOptionInfo(final Currency ccy1, final Currency ccy2) {
    final FxOptionInfo i = defaultVanillaFxOptionInfo(ccy1, ccy2);
    i.setSurfaceName("model/vanillafxoption", "DEFAULT");
    i.setCurveExposureName("model/vanillafxoption", "FX Exposures");
    i.setXInterpolatorName("model/vanillafxoption", LinearInterpolator1dAdapter.NAME);
    i.setLeftXExtrapolatorName("model/vanillafxoption", LinearExtrapolator1dAdapter.NAME);
    i.setRightXExtrapolatorName("model/vanillafxoption", LinearExtrapolator1dAdapter.NAME);
    setVanillaFxOptionInfo(ccy1, ccy2, i);
  }

  protected void setFxForwardInfo(final Currency ccy1, final Currency ccy2) {
    final FxForwardInfo i = defaultFxForwardInfo(ccy1, ccy2);
    i.setCurveExposureName("model/fxforward", "FX Exposures");
    setFxForwardInfo(ccy1, ccy2, i);
  }
}
