/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model;

import java.util.Collections;
import java.util.List;

import com.mcleodmoores.financial.function.bond.functions.BondDiscountingMethodFunctions;
import com.mcleodmoores.financial.function.credit.cds.isda.functions.IsdaFunctions;
import com.mcleodmoores.financial.function.fx.functions.FxDiscountingMethodFunctions;
import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.CombiningFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationBundle;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.function.config.SimpleFunctionConfigurationSource;
import com.opengamma.financial.analytics.model.black.BlackDiscountingPricingFunctions;
import com.opengamma.financial.analytics.model.bondcleanprice.BondCleanPriceFunctions;
import com.opengamma.financial.analytics.model.bondcurves.BondCurveFunctions;
import com.opengamma.financial.analytics.model.bondcurves.inflationbondcurves.InflationBondCurveFunctions;
import com.opengamma.financial.analytics.model.bondyield.BondYieldFunctions;
import com.opengamma.financial.analytics.model.carrlee.CarrLeeFunctions;
import com.opengamma.financial.analytics.model.cds.CDSFunctions;
import com.opengamma.financial.analytics.model.credit.CreditFunctions;
import com.opengamma.financial.analytics.model.curve.CurveFunctions;
import com.opengamma.financial.analytics.model.curve.forward.ForwardFunctions;
import com.opengamma.financial.analytics.model.discounting.DiscountingPricingFunctions;
import com.opengamma.financial.analytics.model.equity.EquityFunctions;
import com.opengamma.financial.analytics.model.forex.ForexFunctions;
import com.opengamma.financial.analytics.model.future.FutureFunctions;
import com.opengamma.financial.analytics.model.futureoption.FutureOptionFunctions;
import com.opengamma.financial.analytics.model.fx.FXForwardPricingFunctions;
import com.opengamma.financial.analytics.model.g2ppdiscounting.G2ppPricingFunctions;
import com.opengamma.financial.analytics.model.hullwhitediscounting.HullWhitePricingFunctions;
import com.opengamma.financial.analytics.model.irfutureoption.IRFutureOptionFunctions;
import com.opengamma.financial.analytics.model.option.OptionFunctions;
import com.opengamma.financial.analytics.model.pnl.PNLFunctions;
import com.opengamma.financial.analytics.model.sabr.SABRDiscountingPricingFunctions;
import com.opengamma.financial.analytics.model.sensitivities.SensitivitiesFunctions;
import com.opengamma.financial.analytics.model.simpleinstrument.SimpleInstrumentFunctions;
import com.opengamma.financial.analytics.model.timeseries.TimeSeriesFunctions;
import com.opengamma.financial.analytics.model.trs.TotalReturnSwapFunctions;
import com.opengamma.financial.analytics.model.var.VaRFunctions;
import com.opengamma.financial.analytics.model.volatility.VolatilityFunctions;

/**
 * Function repository configuration source for the functions contained in this package and sub-packages.
 */
@SuppressWarnings("deprecation")
public class ModelFunctions extends AbstractFunctionConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package and its sub-packages.
   *
   * @return the configuration source exposing functions from this package and its sub-packages
   */
  public static FunctionConfigurationSource instance() {
    return new ModelFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(MarginPriceFunction.class));
    functions.add(functionConfiguration(PVCashBalanceFunction.class));
    functions.add(functionConfiguration(FXCurrencyExposureFunction.class));
  }

  /**
   * Adds functions that produce bond analytics from the clean price.
   *
   * @return A configuration source containing bond functions
   */
  protected FunctionConfigurationSource bondCleanPriceFunctionConfiguration() {
    return BondCleanPriceFunctions.instance();
  }

  /**
   * Adds functions that produce bond analytics from yield curves.
   *
   * @return A configuration source containing bond functions
   */
  protected FunctionConfigurationSource bondCurveFunctionConfiguration() {
    return CombiningFunctionConfigurationSource.of(BondCurveFunctions.instance(), BondDiscountingMethodFunctions.instance());
  }

  /**
   * Adds functions that produce bond analytics from yield curves.
   *
   * @return A configuration source containing bond functions
   */
  protected FunctionConfigurationSource inflationbondCurveFunctionConfiguration() {
    return InflationBondCurveFunctions.instance();
  }

  /**
   * Adds functions that produce bond analytics from the clean price.
   *
   * @return A configuration source containing bond functions
   */
  protected FunctionConfigurationSource bondYieldFunctionConfiguration() {
    return BondYieldFunctions.instance();
  }

  /**
   * Adds functions that produce analytics for volatility swaps using the Carr-Lee model.
   *
   * @return A configuration source containing pricing and analytics functions
   */
  protected FunctionConfigurationSource carrLeeFunctionConfiguration() {
    return CarrLeeFunctions.instance();
  }

  /**
   * Adds CDS functions.
   *
   * @return A configuration source containing CDS functions
   */
  protected FunctionConfigurationSource cdsFunctionConfiguration() {
    return CDSFunctions.instance();
  }

  /**
   * Adds functions that produce credit instrument analytics using the ISDA model.
   *
   * @return A configuration source containing ISDA model functions
   */
  protected FunctionConfigurationSource isdaModelFunctionConfiguration() {
    return CombiningFunctionConfigurationSource.of(IsdaFunctions.instance());
  }

  /**
   * Adds credit functions.
   *
   * @return A configuration source containing credit functions
   */
  protected FunctionConfigurationSource creditFunctionConfiguration() {
    return CreditFunctions.instance();
  }

  /**
   * Adds functions that produce curves.
   *
   * @return A configuration source containing curve functions
   */
  protected FunctionConfigurationSource curveFunctionConfiguration() {
    return CombiningFunctionConfigurationSource.of(CurveFunctions.instance(), com.mcleodmoores.financial.function.curve.functions.CurveFunctions.instance());
  }

  /**
   * Adds functions that use the ISDA model.
   *
   * @return a configuration source containing ISDA model functions
   */
  protected FunctionConfigurationSource idsaFunctionConfiguration() {
    return IsdaFunctions.instance();
  }

  /**
   * Adds equity functions.
   *
   * @return A configuration source containing equity functions
   */
  protected FunctionConfigurationSource equityFunctionConfiguration() {
    return EquityFunctions.instance();
  }

  /**
   * Adds pricing functions that use curves constructed with the discounting method.
   *
   * @return A configuration source containing these functions.
   */
  protected FunctionConfigurationSource discountingFunctionConfiguration() {
    return CombiningFunctionConfigurationSource.of(DiscountingPricingFunctions.instance(), FxDiscountingMethodFunctions.instance());
  }

  /**
   * Adds pricing functions that use Black surfaces and curve constructed with the discounting method.
   *
   * @return A configuration source containing these functions
   */
  protected FunctionConfigurationSource blackDiscountingFunctionConfiguration() {
    return BlackDiscountingPricingFunctions.instance();
  }

  /**
   * Adds pricing functions that use curves constructed using the Hull-White one factor discounting method.
   *
   * @return A configuration source containing these functions
   */
  protected FunctionConfigurationSource hullWhitePricingFunctionConfiguration() {
    return HullWhitePricingFunctions.instance();
  }

  /**
   * Adds pricing functions that use curves constructed using the G2++ discounting method.
   *
   * @return A configuration source containing these functions
   */
  protected FunctionConfigurationSource g2ppPricingFunctionConfiguration() {
    return G2ppPricingFunctions.instance();
  }

  protected FunctionConfigurationSource fxPricingFunctionConfiguration() {
    return FXForwardPricingFunctions.instance();
  }

  protected FunctionConfigurationSource forwardFunctionConfiguration() {
    return ForwardFunctions.instance();
  }

  protected FunctionConfigurationSource forexFunctionConfiguration() {
    return ForexFunctions.instance();
  }

  protected FunctionConfigurationSource futureFunctionConfiguration() {
    return FutureFunctions.instance();
  }

  protected FunctionConfigurationSource futureOptionFunctionConfiguration() {
    return FutureOptionFunctions.instance();
  }

  /**
   * Adds interest rate future-specific functions.
   *
   * @return A configuration source containing the deprecated interest rate future functions.
   * @deprecated The current versions of these functions are added in {@link ModelFunctions#blackDiscountingFunctionConfiguration}
   */
  @Deprecated
  protected FunctionConfigurationSource interestRateFutureFunctionConfiguration() {
    return FutureFunctions.deprecated();
  }

  protected FunctionConfigurationSource irFutureOptionFunctionConfiguration() {
    return IRFutureOptionFunctions.instance();
  }

  /**
   * Adds general option functions.
   *
   * @return A configuration source containing option functions
   * @deprecated The underlying-specific functions should be used
   */
  @Deprecated
  protected FunctionConfigurationSource optionFunctionConfiguration() {
    return OptionFunctions.instance();
  }

  protected FunctionConfigurationSource pnlFunctionConfiguration() {
    return PNLFunctions.instance();
  }

  protected FunctionConfigurationSource riskFactorFunctionConfiguration() {
    // TODO
    return new SimpleFunctionConfigurationSource(new FunctionConfigurationBundle(Collections.<FunctionConfiguration> emptyList()));
  }

  protected FunctionConfigurationSource sabrDiscountingFunctionConfiguration() {
    return SABRDiscountingPricingFunctions.instance();
  }

  protected FunctionConfigurationSource sensitivitiesFunctionConfiguration() {
    return SensitivitiesFunctions.instance();
  }

  protected FunctionConfigurationSource simpleInstrumentFunctionConfiguration() {
    return SimpleInstrumentFunctions.instance();
  }

  protected FunctionConfigurationSource varFunctionConfiguration() {
    return VaRFunctions.instance();
  }

  protected FunctionConfigurationSource volatilityFunctionConfiguration() {
    return VolatilityFunctions.instance();
  }

  protected FunctionConfigurationSource futureCurveFunctionConfiguration() {
    return com.opengamma.financial.analytics.model.curve.future.FutureFunctions.instance();
  }

  /**
   * Adds time series functions.
   *
   * @return A configuration source containing time series functions
   */
  protected FunctionConfigurationSource timeSeriesFunctionConfiguration() {
    return TimeSeriesFunctions.instance();
  }

  /**
   * Adds total return swap functions.
   *
   * @return A configuration source containing total return swap functions
   */
  protected FunctionConfigurationSource totalReturnSwapFunctionConfiguration() {
    return TotalReturnSwapFunctions.instance();
  }

  @Override
  protected FunctionConfigurationSource createObject() {
    return CombiningFunctionConfigurationSource.of(super.createObject(), bondCleanPriceFunctionConfiguration(), bondCurveFunctionConfiguration(),
        inflationbondCurveFunctionConfiguration(), bondYieldFunctionConfiguration(), carrLeeFunctionConfiguration(), curveFunctionConfiguration(),
        equityFunctionConfiguration(), forexFunctionConfiguration(), futureFunctionConfiguration(), futureOptionFunctionConfiguration(),
        irFutureOptionFunctionConfiguration(), pnlFunctionConfiguration(), riskFactorFunctionConfiguration(),
        sensitivitiesFunctionConfiguration(), simpleInstrumentFunctionConfiguration(), varFunctionConfiguration(),
        volatilityFunctionConfiguration(), forwardFunctionConfiguration(), futureCurveFunctionConfiguration(),
        discountingFunctionConfiguration(), hullWhitePricingFunctionConfiguration(), fxPricingFunctionConfiguration(), blackDiscountingFunctionConfiguration(),
        sabrDiscountingFunctionConfiguration(), g2ppPricingFunctionConfiguration(), timeSeriesFunctionConfiguration(), totalReturnSwapFunctionConfiguration(),
        isdaModelFunctionConfiguration());
  }

}
