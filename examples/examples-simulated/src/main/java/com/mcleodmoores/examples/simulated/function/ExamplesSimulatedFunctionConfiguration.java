/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.examples.simulated.function;

import static com.opengamma.engine.value.ValuePropertyNames.DIVIDEND_TYPE_NONE;

import java.util.List;

import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.engine.function.config.CombiningFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.model.equity.option.EquityOptionFunctions;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.lambdava.functions.Function1;

public class ExamplesSimulatedFunctionConfiguration extends ExamplesFunctionConfiguration {
 //private static final ExampleStandardFunctionConfiguration DELEGATE = new ExampleStandardFunctionConfiguration();

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

// @Override
// protected CurrencyInfo audCurrencyInfo() {
//   return DELEGATE.getCurrencyInfo("AUD");
// }

 //TODO override other methods

 @Override
 protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
 }

 protected void setEquityOptionInfo() {
   setEquityOptionInfo("AAPL", "USD");
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
   i.setForwardCurveInterpolator("model/equityoption", Interpolator1DFactory.DOUBLE_QUADRATIC);
   i.setForwardCurveLeftExtrapolator("model/equityoption", Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
   i.setForwardCurveRightExtrapolator("model/equityoption", Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
   i.setSurfaceCalculationMethod("model/equityoption", BlackVolatilitySurfacePropertyNamesAndValues.INTERPOLATED_BLACK_LOGNORMAL);
   i.setSurfaceInterpolationMethod("model/equityoption", BlackVolatilitySurfacePropertyNamesAndValues.SPLINE);
   i.setVolatilitySurface("model/equityoption", "DEFAULT");
   setEquityInfo(ticker, i);
 }

 /**
  * Overridden to allow separate curve default curve names (per currency) to be
  * set for equity options. The parent class sets the same curve names
  * for all equity instruments.
  * {@inheritDoc}
  */
 @Override
 protected FunctionConfigurationSource equityOptionFunctions() {
   super.equityOptionFunctions();
   final EquityOptionFunctions.EquityForwardDefaults forwardCurveDefaults = new EquityOptionFunctions.EquityForwardDefaults();
   setEquityOptionForwardCurveDefaults(forwardCurveDefaults);
   final EquityOptionFunctions.EquityOptionDefaults surfaceDefaults = new EquityOptionFunctions.EquityOptionDefaults();
   setEquityOptionSurfaceDefaults(surfaceDefaults);
   final FunctionConfigurationSource forwardCurveRepository = getRepository(forwardCurveDefaults);
   final FunctionConfigurationSource surfaceRepository = getRepository(surfaceDefaults);
   return CombiningFunctionConfigurationSource.of(forwardCurveRepository, surfaceRepository);
 }

 /**
  * Sets the per-equity forward curve defaults for equity option functions.
  * @param defaults The object containing the default values
  */
 protected void setEquityOptionForwardCurveDefaults(final EquityOptionFunctions.EquityForwardDefaults defaults) {
   defaults.setPerEquityInfo(getEquityInfo(new Function1<EquityInfo, EquityOptionFunctions.EquityInfo>() {
     @Override
     public EquityOptionFunctions.EquityInfo execute(final EquityInfo i) {
       final EquityOptionFunctions.EquityInfo d = new EquityOptionFunctions.EquityInfo();
       setEquityOptionForwardCurveDefaults(i, d);
       return d;
     }
   }));
 }

 /**
  * Sets the paths for the per-equity ticker default values for the forward curve used
  * in pricing with the keys:
  * <p>
  * <ul>
  * <li> Forward curve interpolator = model/equityoption
  * <li> Forward curve left extrapolator = model/equityoption
  * <li> Forward curve right extrapolator = model/equityoption
  * <li> Forward curve = model/equityoption
  * <li> Forward curve calculation method = model/equityoption
  * <li> Discounting curve = model/equityoption
  * <li> Discounting curve configuration = model/equityoption
  * <li> Discounting curve currency = model/equityoption
  * <li> Dividend type = model/equityoption
  * </ul>
  * @param i The per-equity info
  * @param defaults The object containing the default values
  */
 protected void setEquityOptionForwardCurveDefaults(final EquityInfo i, final EquityOptionFunctions.EquityInfo defaults) {
   defaults.setForwardCurveInterpolator(i.getForwardCurveInterpolator("model/equityoption"));
   defaults.setForwardCurveLeftExtrapolator(i.getForwardCurveLeftExtrapolator("model/equityoption"));
   defaults.setForwardCurveRightExtrapolator(i.getForwardCurveRightExtrapolator("model/equityoption"));
   defaults.setForwardCurve(i.getForwardCurve("model/equityoption"));
   defaults.setForwardCurveCalculationMethod(i.getForwardCurveCalculationMethod("model/equityoption"));
   defaults.setDiscountingCurve(i.getDiscountingCurve("model/equityoption"));
   defaults.setDiscountingCurveConfig(i.getDiscountingCurveConfig("model/equityoption"));
   defaults.setDiscountingCurveCurrency(i.getDiscountingCurveCurrency("model/equityoption"));
   defaults.setDividendType(i.getDiscountingCurve("model/equityoption"));
 }

 /**
  * Sets the per-equity surface defaults for equity option functions.
  * @param defaults The object containing the default values
  */
 protected void setEquityOptionSurfaceDefaults(final EquityOptionFunctions.EquityOptionDefaults defaults) {
   defaults.setPerEquityInfo(getEquityInfo(new Function1<EquityInfo, EquityOptionFunctions.EquityInfo>() {
     @Override
     public EquityOptionFunctions.EquityInfo execute(final EquityInfo i) {
       final EquityOptionFunctions.EquityInfo d = new EquityOptionFunctions.EquityInfo();
       setEquityOptionSurfaceDefaults(i, d);
       return d;
     }
   }));
 }

 /**
  * Sets the paths for the per-equity ticker default values for the surface used
  * in pricing with the keys<p>
  * <ul>
  * <li> Surface calculation method = model/equityoption
  * <li> Discounting curve name = model/equityoption
  * <li> Discounting curve calculation config = model/equityoption
  * <li> Volatility surface name = model/equityoption
  * <li> Surface interpolation method = model/equityoption
  * <li> Forward curve name = model/equityoption
  * <li> Forward curve calculation method = model/equityoption
  * </ul>
  * @param i The per-equity info
  * @param defaults The object containing the default values
  */
 protected void setEquityOptionSurfaceDefaults(final EquityInfo i, final EquityOptionFunctions.EquityInfo defaults) {
   defaults.setSurfaceCalculationMethod(i.getSurfaceCalculationMethod("model/equityoption"));
   defaults.setDiscountingCurve(i.getDiscountingCurve("model/equityoption"));
   defaults.setDiscountingCurveConfig(i.getDiscountingCurveConfig("model/equityoption"));
   defaults.setVolatilitySurface(i.getVolatilitySurface("model/equityoption"));
   defaults.setSurfaceInterpolationMethod(i.getSurfaceInterpolationMethod("model/equityoption"));
   defaults.setForwardCurve(i.getForwardCurve("model/equityoption"));
   defaults.setForwardCurveCalculationMethod(i.getForwardCurveCalculationMethod("model/equityoption"));
 }

}
