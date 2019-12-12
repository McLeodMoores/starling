/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.examples.simulated.loader;

import static com.opengamma.engine.value.ValuePropertyNames.CALCULATION_METHOD;
import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_CONFIG;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_METHOD;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValuePropertyNames.SCALE;
import static com.opengamma.engine.value.ValuePropertyNames.SURFACE;
import static com.opengamma.engine.value.ValueRequirementNames.BOND_DETAILS;
import static com.opengamma.engine.value.ValueRequirementNames.BUCKETED_PV01;
import static com.opengamma.engine.value.ValueRequirementNames.CAPM_BETA;
import static com.opengamma.engine.value.ValueRequirementNames.CLEAN_PRICE;
import static com.opengamma.engine.value.ValueRequirementNames.CONVEXITY;
import static com.opengamma.engine.value.ValueRequirementNames.DELTA;
import static com.opengamma.engine.value.ValueRequirementNames.FAIR_VALUE;
import static com.opengamma.engine.value.ValueRequirementNames.FORWARD;
import static com.opengamma.engine.value.ValueRequirementNames.FX_CURRENCY_EXPOSURE;
import static com.opengamma.engine.value.ValueRequirementNames.GAMMA;
import static com.opengamma.engine.value.ValueRequirementNames.GAMMA_PV01;
import static com.opengamma.engine.value.ValueRequirementNames.HISTORICAL_VAR;
import static com.opengamma.engine.value.ValueRequirementNames.JENSENS_ALPHA;
import static com.opengamma.engine.value.ValueRequirementNames.MACAULAY_DURATION;
import static com.opengamma.engine.value.ValueRequirementNames.MODIFIED_DURATION;
import static com.opengamma.engine.value.ValueRequirementNames.PAR_RATE;
import static com.opengamma.engine.value.ValueRequirementNames.PNL;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE;
import static com.opengamma.engine.value.ValueRequirementNames.PV01;
import static com.opengamma.engine.value.ValueRequirementNames.RHO;
import static com.opengamma.engine.value.ValueRequirementNames.SECURITY_IMPLIED_VOLATILITY;
import static com.opengamma.engine.value.ValueRequirementNames.SHARPE_RATIO;
import static com.opengamma.engine.value.ValueRequirementNames.THETA;
import static com.opengamma.engine.value.ValueRequirementNames.TOTAL_RISK_ALPHA;
import static com.opengamma.engine.value.ValueRequirementNames.TREYNOR_RATIO;
import static com.opengamma.engine.value.ValueRequirementNames.VALUE_DELTA;
import static com.opengamma.engine.value.ValueRequirementNames.VALUE_GAMMA;
import static com.opengamma.engine.value.ValueRequirementNames.VALUE_GAMMA_P;
import static com.opengamma.engine.value.ValueRequirementNames.VALUE_PHI;
import static com.opengamma.engine.value.ValueRequirementNames.VALUE_RHO;
import static com.opengamma.engine.value.ValueRequirementNames.VALUE_THETA;
import static com.opengamma.engine.value.ValueRequirementNames.VALUE_VANNA;
import static com.opengamma.engine.value.ValueRequirementNames.VALUE_VEGA;
import static com.opengamma.engine.value.ValueRequirementNames.VALUE_VOMMA;
import static com.opengamma.engine.value.ValueRequirementNames.VEGA;
import static com.opengamma.engine.value.ValueRequirementNames.VEGA_MATRIX;
import static com.opengamma.engine.value.ValueRequirementNames.VEGA_QUOTE_MATRIX;
import static com.opengamma.engine.value.ValueRequirementNames.VOLATILITY_SURFACE_DATA;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE_JACOBIAN;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES;
import static com.opengamma.engine.value.ValueRequirementNames.YTM;
import static com.opengamma.engine.value.ValueRequirementNames.Z_SPREAD;
import static com.opengamma.examples.simulated.tool.ExampleDatabasePopulator.AUD_SWAP_PORFOLIO_NAME;
import static com.opengamma.examples.simulated.tool.ExampleDatabasePopulator.BONDS_PORTFOLIO_NAME;
import static com.opengamma.examples.simulated.tool.ExampleDatabasePopulator.EQUITY_OPTION_PORTFOLIO_NAME;
import static com.opengamma.examples.simulated.tool.ExampleDatabasePopulator.EUR_SWAP_PORTFOLIO_NAME;
import static com.opengamma.examples.simulated.tool.ExampleDatabasePopulator.FUTURE_PORTFOLIO_NAME;
import static com.opengamma.examples.simulated.tool.ExampleDatabasePopulator.FX_FORWARD_PORTFOLIO_NAME;
import static com.opengamma.examples.simulated.tool.ExampleDatabasePopulator.FX_VOLATILITY_SWAP_PORTFOLIO_NAME;
import static com.opengamma.examples.simulated.tool.ExampleDatabasePopulator.MULTI_CURRENCY_SWAPTION_PORTFOLIO_NAME;
import static com.opengamma.examples.simulated.tool.ExampleDatabasePopulator.MULTI_CURRENCY_SWAP_PORTFOLIO_NAME;
import static com.opengamma.examples.simulated.tool.ExampleDatabasePopulator.OIS_PORTFOLIO_NAME;
import static com.opengamma.examples.simulated.tool.ExampleDatabasePopulator.US_GOVERNMENT_BOND_PORTFOLIO_NAME;
import static com.opengamma.examples.simulated.tool.ExampleDatabasePopulator.VANILLA_FX_OPTION_PORTFOLIO_NAME;
import static com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues.BAW_METHOD;
import static com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues.BLACK_METHOD;
import static com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues.DISCOUNTING;
import static com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues.FORWARD_POINTS;
import static com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues.HISTORICAL_REALIZED_VARIANCE;
import static com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues.PROPERTY_REALIZED_VARIANCE_METHOD;
import static com.opengamma.financial.analytics.model.InstrumentTypeProperties.FOREX;
import static com.opengamma.financial.analytics.model.InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE;
import static com.opengamma.financial.analytics.model.InstrumentTypeProperties.SWAPTION_ATM;
import static com.opengamma.financial.analytics.model.InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME;
import static com.opengamma.financial.analytics.model.InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME;
import static com.opengamma.financial.analytics.model.InterpolatedDataProperties.X_INTERPOLATOR_NAME;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PAR_RATE_STRING;
import static com.opengamma.financial.analytics.model.horizon.ThetaPropertyNamesAndValues.PROPERTY_DAYS_TO_MOVE_FORWARD;
import static com.opengamma.financial.analytics.model.horizon.ThetaPropertyNamesAndValues.PROPERTY_THETA_CALCULATION_METHOD;
import static com.opengamma.financial.analytics.model.horizon.ThetaPropertyNamesAndValues.THETA_CONSTANT_SPREAD;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.math.interpolation.factory.LinearExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LinearInterpolator1dAdapter;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewCalculationConfiguration.MergedOutput;
import com.opengamma.engine.view.ViewCalculationConfiguration.MergedOutputAggregationType;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.analytics.model.bond.BondFunction;
import com.opengamma.financial.currency.CurrencyConversionFunction;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXVolatilitySwapSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Creates view definitions that use simulated market data and OpenGamma integration functions.
 */
@SuppressWarnings("deprecation")
@Scriptable
public class ExampleViewsPopulator extends AbstractTool<ToolContext> {
  /** Name of the default calculation configurations. */
  private static final String DEFAULT_CALC_CONFIG = "Default";
  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ExampleViewsPopulator.class);
  /** A list of currencies. */
  private static final Currency[] SWAP_CURRENCIES = new Currency[] { Currency.USD, Currency.GBP, Currency.EUR, Currency.JPY, Currency.CHF };
  /** A list of OIS currencies */
  private static final Currency[] OIS_CURRENCIES = new Currency[] { Currency.USD, Currency.GBP, Currency.EUR, Currency.JPY };
  /** A list of curve configuration names. */
  private static final String[] CURVE_CONFIG_NAMES = new String[] { "DefaultTwoCurveUSDConfig", "DefaultTwoCurveGBPConfig",
      "DefaultTwoCurveEURConfig",
      "DefaultTwoCurveJPYConfig", "DefaultTwoCurveCHFConfig" };
  /** A list of currency pairs. */
  public static final UnorderedCurrencyPair[] CURRENCY_PAIRS = new UnorderedCurrencyPair[] {
      UnorderedCurrencyPair.of(Currency.USD, Currency.EUR),
      UnorderedCurrencyPair.of(Currency.USD, Currency.CHF), UnorderedCurrencyPair.of(Currency.USD, Currency.AUD),
      UnorderedCurrencyPair.of(Currency.USD, Currency.GBP), UnorderedCurrencyPair.of(Currency.USD, Currency.JPY),
      UnorderedCurrencyPair.of(Currency.GBP, Currency.EUR), UnorderedCurrencyPair.of(Currency.CHF, Currency.JPY) };
  /** Map of currencies to swaption surface / cube names. */
  public static final Map<Currency, String> SWAPTION_CURRENCY_CONFIGS = new HashMap<>();
  /** Map of countries to swaption surface / cube names. */
  public static final Map<String, String> SWAPTION_COUNTRY_CONFIGS = new HashMap<>();
  /** Map of currencies to curves. */
  public static final Map<Currency, Pair<String, String>> SWAPTION_CURVES = new HashMap<>();
  /** List of (curve construction configuration, curve definition) pairs for bond TRS issuers */
  private static final List<Pair<String, String>> BOND_TRS_ISSUER_CURVES = new ArrayList<>();
  /** The default maximum delta calculation period */
  private static final long MAX_DELTA_PERIOD = 500L;
  /** The default maximum full calculation period */
  private static final long MAX_FULL_PERIOD = 500L;
  /** The default minimum delta calculation period */
  private static final long MIN_DELTA_PERIOD = 500L;
  /** The default minimum full calculation period */
  private static final long MIN_FULL_PERIOD = 500L;

  static {
    SWAPTION_CURRENCY_CONFIGS.put(Currency.USD, "PROVIDER1");
    SWAPTION_CURRENCY_CONFIGS.put(Currency.GBP, "PROVIDER1");
    SWAPTION_CURRENCY_CONFIGS.put(Currency.EUR, "PROVIDER2");
    SWAPTION_CURRENCY_CONFIGS.put(Currency.JPY, "PROVIDER3");
    SWAPTION_CURRENCY_CONFIGS.put(Currency.CHF, "PROVIDER2");
    SWAPTION_COUNTRY_CONFIGS.put("US", "FWD SWAP PROVIDER1");
    SWAPTION_COUNTRY_CONFIGS.put("GB", "FWD SWAP PROVIDER1");
    SWAPTION_COUNTRY_CONFIGS.put("EU", "FWD SWAP PROVIDER2");
    SWAPTION_COUNTRY_CONFIGS.put("JP", "FWD SWAP PROVIDER3");
    SWAPTION_COUNTRY_CONFIGS.put("SF", "FWD_SWAP PROVIDER2");
    SWAPTION_CURVES.put(Currency.USD, Pairs.of("Discounting", "Forward3M"));
    SWAPTION_CURVES.put(Currency.GBP, Pairs.of("Discounting", "Forward6M"));
    SWAPTION_CURVES.put(Currency.EUR, Pairs.of("Discounting", "Forward6M"));
    SWAPTION_CURVES.put(Currency.JPY, Pairs.of("Discounting", "Forward6M"));
    SWAPTION_CURVES.put(Currency.CHF, Pairs.of("Discounting", "Forward6M"));
    BOND_TRS_ISSUER_CURVES.add(Pairs.of("UG Government Bond Configuration", "UG Government Curve"));
  }

  // -------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   *
   * @param args
   *          the standard tool arguments, not null
   */
  public static void main(final String[] args) {
    new ExampleViewsPopulator().invokeAndTerminate(args);
  }

  // -------------------------------------------------------------------------
  @Override
  protected void doRun() {
    storeViewDefinition(getSyntheticEquityViewDefinition(ExampleEquityPortfolioLoader.PORTFOLIO_NAME));
    storeViewDefinition(getSyntheticMultiCurrencySwapViewDefinitionWithSeparateOutputs(MULTI_CURRENCY_SWAP_PORTFOLIO_NAME));
    storeViewDefinition(getSyntheticMultiCurrencySwapViewDefinition(MULTI_CURRENCY_SWAP_PORTFOLIO_NAME));
    storeViewDefinition(getSyntheticAudSwapView1Definition(AUD_SWAP_PORFOLIO_NAME));
    storeViewDefinition(getSyntheticAudSwapView2Definition(AUD_SWAP_PORFOLIO_NAME));
    storeViewDefinition(getSyntheticAudSwapView3Definition(AUD_SWAP_PORFOLIO_NAME));
    storeViewDefinition(getSyntheticFxOptionViewDefinitio(VANILLA_FX_OPTION_PORTFOLIO_NAME, "FX Option View"));
    storeViewDefinition(getSyntheticFxOptionGreeksViewDefinition(VANILLA_FX_OPTION_PORTFOLIO_NAME, "FX Option Greeks View"));
    storeViewDefinition(getSyntheticAtmSwaptionViewDefinition(MULTI_CURRENCY_SWAPTION_PORTFOLIO_NAME, "Swaption Black Pricing View"));
    storeViewDefinition(getSyntheticEurFixedIncomeViewDefinition(EUR_SWAP_PORTFOLIO_NAME, "EUR Swap Desk View"));
    storeViewDefinition(getSyntheticFxForwardViewDefinition(FX_FORWARD_PORTFOLIO_NAME, "FX Forward View"));
    storeViewDefinition(getSyntheticFutureViewDefinition(FUTURE_PORTFOLIO_NAME, "Futures View"));
    storeViewDefinition(getSyntheticBondViewDefinition(US_GOVERNMENT_BOND_PORTFOLIO_NAME, "Government Bond View"));
    storeViewDefinition(getSyntheticFxVolatilitySwapViewDefinition(FX_VOLATILITY_SWAP_PORTFOLIO_NAME, "FX Volatility Swap View"));
    storeViewDefinition(getSyntheticOisViewDefinition(OIS_PORTFOLIO_NAME, "OIS View"));
    storeViewDefinition(getSyntheticMultiCountryBondViewDefinition(BONDS_PORTFOLIO_NAME, "Bond Portfolio View"));
    storeViewDefinition(getSyntheticEquityOptionViewDefinition(EQUITY_OPTION_PORTFOLIO_NAME, "Equity Option View"));
  }

  /**
   * Creates a view definition for a portfolio containing only equities that produces:
   * <ul>
   * <li>ValueRequirementNames#FAIR_VALUE
   * <li>ValueRequirementNames#CAPM_BETA
   * <li>ValueRequirementNames#HISTORICAL_VAR
   * <li>ValueRequirementNames#SHARPE_RATIO
   * <li>ValueRequirementNames#TREYNOR_RATIO
   * <li>ValueRequirementNames#JENSENS_ALPHA
   * <li>ValueRequirementNames#TOTAL_RISK_ALPHA
   * <li>ValueRequirementNames#PNL
   * </ul>
   *
   * @param portfolioName
   *          The portfolio name
   * @return The view definition
   */
  private ViewDefinition getSyntheticEquityViewDefinition(final String portfolioName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(portfolioName + " View", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    final long maxPeriod = 30000L;
    viewDefinition.setMaxFullCalculationPeriod(maxPeriod);
    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
    viewDefinition.setMaxDeltaCalculationPeriod(maxPeriod);

    final ViewCalculationConfiguration defaultCalc = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    final String[] valueRequirementNames = new String[] { FAIR_VALUE, CAPM_BETA, HISTORICAL_VAR, SHARPE_RATIO, TREYNOR_RATIO, JENSENS_ALPHA,
        TOTAL_RISK_ALPHA,
        PNL };
    addValueRequirements(defaultCalc, EquitySecurity.SECURITY_TYPE, valueRequirementNames);
    viewDefinition.addViewCalculationConfiguration(defaultCalc);
    return viewDefinition;
  }

  /**
   * Creates a view definition for a swap portfolio that produces:
   * <ul>
   * <li>ValueRequirementNames#PRESENT_VALUE
   * <li>ValueRequirementNames#PV01 for named curves
   * <li>ValueRequirementNames#YIELD_CURVE_NODE_SENSITIVITIES for named curves
   * <li>ValueRequirementNames#YIELD_CURVE
   * </ul>
   * The curve-specific risk outputs are not collapsed into a single column.
   *
   * @param portfolioName
   *          The portfolio name
   * @return The view definition
   */
  private ViewDefinition getSyntheticMultiCurrencySwapViewDefinitionWithSeparateOutputs(final String portfolioName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(portfolioName + " View (outputs by ccy)", portfolioId,
        UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
    final ViewCalculationConfiguration defaultCalConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    defaultCalConfig.addPortfolioRequirementName(SwapSecurity.SECURITY_TYPE, PAR_RATE);
    // The name "Default" has no special meaning, but means that the currency conversion function can never be used
    // and so we get the instrument's natural currency
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE,
        ValueProperties.with(CurrencyConversionFunction.ORIGINAL_CURRENCY, "Default")
            .withOptional(CurrencyConversionFunction.ORIGINAL_CURRENCY).get());
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE, ValueProperties.with(CURRENCY, "USD").get());
    for (int i = 0; i < SWAP_CURRENCIES.length; i++) {
      final Currency ccy = SWAP_CURRENCIES[i];
      final String ccyCode = ccy.getCode();
      final ComputationTargetSpecification currencyTarget = ComputationTargetSpecification.of(ccy);
      final String forwardCurveName = ccyCode.equals("USD") ? "Forward3M" : "Forward6M";
      final ValueProperties discountingRiskProperties = ValueProperties.with(CURVE, "Discounting").with(CURVE_CURRENCY, ccyCode).get();
      final ValueProperties discountingCurveProperties = ValueProperties.with(CURVE, "Discounting")
          .with(CURVE_CALCULATION_CONFIG, CURVE_CONFIG_NAMES[i]).get();
      final ValueProperties forwardRiskProperties = ValueProperties.with(CURVE, forwardCurveName).with(CURVE_CURRENCY, ccyCode).get();
      final ValueProperties forwardCurveProperties = ValueProperties.with(CURVE, forwardCurveName)
          .with(CURVE_CALCULATION_CONFIG, CURVE_CONFIG_NAMES[i]).get();
      defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PV01, discountingRiskProperties);
      defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, discountingRiskProperties);
      defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget, discountingCurveProperties));
      defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, forwardRiskProperties);
      defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PV01, forwardRiskProperties);
      defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget, forwardCurveProperties));
    }
    viewDefinition.addViewCalculationConfiguration(defaultCalConfig);
    return viewDefinition;
  }

  /**
   * Creates a view definition for a swap portfolio that produces:
   * <ul>
   * <li>ValueRequirementNames#PRESENT_VALUE
   * <li>ValueRequirementNames#PV01 for named curves
   * <li>ValueRequirementNames#YIELD_CURVE_NODE_SENSITIVITIES for named curves
   * <li>ValueRequirementNames#YIELD_CURVE
   * </ul>
   * The curve-specific risk outputs are not collapsed into a single column.
   *
   * @param portfolioName
   *          The portfolio name
   * @return The view definition
   */
  private ViewDefinition getSyntheticMultiCurrencySwapViewDefinition(final String portfolioName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(portfolioName + " View", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
    final ViewCalculationConfiguration defaultCalcConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    defaultCalcConfig.addPortfolioRequirementName(SwapSecurity.SECURITY_TYPE, PAR_RATE);
    // The name "Default" has no special meaning, but means that the currency conversion function can never be
    // used and so we get the instrument's natural currency
    defaultCalcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE,
        ValueProperties.with(CurrencyConversionFunction.ORIGINAL_CURRENCY, "Default")
            .withOptional(CurrencyConversionFunction.ORIGINAL_CURRENCY).get());
    defaultCalcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE, ValueProperties.with(CURRENCY, "USD").get());
    final MergedOutput discountingPV01Output = new MergedOutput("Discounting PV01", MergedOutputAggregationType.LINEAR);
    final MergedOutput discountingYCNSOutput = new MergedOutput("Discounting Bucketed PV01", MergedOutputAggregationType.LINEAR);
    final MergedOutput forwardPV01Output = new MergedOutput("Forward PV01", MergedOutputAggregationType.LINEAR);
    final MergedOutput forwardYCNSOutput = new MergedOutput("Forward Bucketed PV01", MergedOutputAggregationType.LINEAR);
    for (int i = 0; i < SWAP_CURRENCIES.length; i++) {
      final Currency ccy = SWAP_CURRENCIES[i];
      final String ccyCode = ccy.getCode();
      final ComputationTargetSpecification currencyTarget = ComputationTargetSpecification.of(ccy);
      final String forwardCurveName = ccyCode.equals("USD") ? "Forward3M" : "Forward6M";
      final ValueProperties discountingRiskProperties = ValueProperties.with(CURVE, "Discounting").with(CURVE_CURRENCY, ccyCode).get();
      final ValueProperties discountingCurveProperties = ValueProperties.with(CURVE, "Discounting")
          .with(CURVE_CALCULATION_CONFIG, CURVE_CONFIG_NAMES[i]).get();
      final ValueProperties forwardRiskProperties = ValueProperties.with(CURVE, forwardCurveName).with(CURVE_CURRENCY, ccyCode).get();
      final ValueProperties forwardCurveProperties = ValueProperties.with(CURVE, forwardCurveName)
          .with(CURVE_CALCULATION_CONFIG, CURVE_CONFIG_NAMES[i]).get();
      discountingPV01Output.addMergedRequirement(PV01, discountingRiskProperties);
      discountingYCNSOutput.addMergedRequirement(YIELD_CURVE_NODE_SENSITIVITIES, discountingRiskProperties);
      defaultCalcConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget, discountingCurveProperties));
      forwardYCNSOutput.addMergedRequirement(YIELD_CURVE_NODE_SENSITIVITIES, forwardRiskProperties);
      forwardPV01Output.addMergedRequirement(PV01, forwardRiskProperties);
      defaultCalcConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget, forwardCurveProperties));
    }
    defaultCalcConfig.addMergedOutput(discountingPV01Output);
    defaultCalcConfig.addMergedOutput(discountingYCNSOutput);
    defaultCalcConfig.addMergedOutput(forwardPV01Output);
    defaultCalcConfig.addMergedOutput(forwardYCNSOutput);
    viewDefinition.addViewCalculationConfiguration(defaultCalcConfig);
    return viewDefinition;
  }

  /**
   * Creates a view definition for an FX option portfolio that produces:
   * <ul>
   * <li>ValueRequirementNames#VOLATILITY_SURFACE_DATA
   * <li>ValueRequirementNames#VEGA_QUOTE_MATRIX
   * <li>ValueRequirementNames#VEGA_MATRIX
   * <li>ValueRequirementNames#YIELD_CURVE_NODE_SENSITIVITIES
   * <li>ValueRequirementNames#PRESENT_VALUE
   * <li>ValueRequirementNames#FX_CURRENCY_EXPOSURE
   * </ul>
   *
   * @param portfolioName
   *          The portfolio name
   * @param viewName
   *          The view name
   * @return The view definition
   */
  private ViewDefinition getSyntheticFxOptionViewDefinitio(final String portfolioName, final String viewName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
    final ViewCalculationConfiguration defaultCalculationConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    final Set<Currency> ccysAdded = new HashSet<>();
    for (final UnorderedCurrencyPair pair : CURRENCY_PAIRS) {
      final ComputationTargetSpecification target = ComputationTargetSpecification.of(pair.getUniqueId());
      final ValueProperties surfaceProperties = ValueProperties.builder().with(SURFACE, "DEFAULT")
          .with(PROPERTY_SURFACE_INSTRUMENT_TYPE, FOREX).get();
      defaultCalculationConfig.addSpecificRequirement(new ValueRequirement(VOLATILITY_SURFACE_DATA, target, surfaceProperties));
      defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, VEGA_QUOTE_MATRIX, surfaceProperties);
      defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, VEGA_MATRIX, surfaceProperties);
      if (!ccysAdded.contains(pair.getFirstCurrency())) {
        final String ccy = pair.getFirstCurrency().getCode();
        final ValueProperties curveProperties = ValueProperties.builder().with(CURVE, "Discounting").with(CURVE_CURRENCY, ccy).get();
        defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, curveProperties);
        ccysAdded.add(pair.getFirstCurrency());
      }
      if (!ccysAdded.contains(pair.getSecondCurrency())) {
        final String ccy = pair.getSecondCurrency().getCode();
        final ValueProperties curveProperties = ValueProperties.builder().with(CURVE, "Discounting").with(CURVE_CURRENCY, ccy).get();
        defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, curveProperties);
        ccysAdded.add(pair.getSecondCurrency());
      }
    }
    final ValueProperties currencyProperty = ValueProperties.builder().with(CURRENCY, "USD").get();
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, PRESENT_VALUE, currencyProperty);
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, FX_CURRENCY_EXPOSURE, ValueProperties.builder().get());
    viewDefinition.addViewCalculationConfiguration(defaultCalculationConfig);
    return viewDefinition;
  }

  /**
   * Creates a view definition for an FX option portfolio that produces:
   * <ul>
   * <li>ValueRequirementNames#PRESENT_VALUE
   * <li>ValueRequirementNames#VALUE_DELTA
   * <li>ValueRequirementNames#VALUE_VEGA
   * <li>ValueRequirementNames#VALUE_GAMMA_P
   * <li>ValueRequirementNames#VALUE_RHO
   * <li>ValueRequirementNames#VALUE_PHI
   * <li>ValueRequirementNames#VALUE_VOMMA
   * <li>ValueRequirementNames#VALUE_VANNA
   * <li>ValueRequirementNames#VALUE_THETA
   * <li>ValueRequirementNames#SECURITY_IMPLIED_VOLATILITY
   * </ul>
   *
   * @param portfolioName
   *          The portfolio name
   * @param viewName
   *          The view name
   * @return The view definition
   */
  private ViewDefinition getSyntheticFxOptionGreeksViewDefinition(final String portfolioName, final String viewName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
    final ViewCalculationConfiguration defaultCalculationConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    final ValueProperties currencyProperty = ValueProperties.builder().with(CURRENCY, "USD").get();
    final ValueProperties vegaProperty = currencyProperty.copy().with(SCALE, "100.").get();
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, PRESENT_VALUE, currencyProperty);
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, VALUE_DELTA, currencyProperty);
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, VALUE_VEGA, vegaProperty);
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, VALUE_GAMMA, currencyProperty);
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, VALUE_GAMMA_P, currencyProperty);
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, VALUE_RHO, currencyProperty);
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, VALUE_PHI, currencyProperty);
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, VALUE_VOMMA, currencyProperty);
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, VALUE_VANNA, currencyProperty);
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, VALUE_THETA, currencyProperty);
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, SECURITY_IMPLIED_VOLATILITY,
        ValueProperties.builder().get());
    viewDefinition.addViewCalculationConfiguration(defaultCalculationConfig);
    return viewDefinition;
  }

  /**
   * Creates a view definition for FX volatility swaps that requests the surface data for each currency pair and the fair value for each
   * swap.
   *
   * @param portfolioName
   *          The portfolio name
   * @param viewName
   *          The view name
   * @return The view definition
   */
  private ViewDefinition getSyntheticFxVolatilitySwapViewDefinition(final String portfolioName, final String viewName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
    final ViewCalculationConfiguration defaultCalculationConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    for (final UnorderedCurrencyPair pair : CURRENCY_PAIRS) {
      final ComputationTargetSpecification target = ComputationTargetSpecification.of(pair.getUniqueId());
      final ValueProperties properties = ValueProperties.builder().with(SURFACE, "DEFAULT").with(PROPERTY_SURFACE_INSTRUMENT_TYPE, FOREX)
          .get();
      defaultCalculationConfig.addSpecificRequirement(new ValueRequirement(VOLATILITY_SURFACE_DATA, target, properties));
    }
    final ValueProperties properties = ValueProperties.builder().with(SURFACE, "DEFAULT")
        .with(X_INTERPOLATOR_NAME, LinearInterpolator1dAdapter.NAME)
        .with(LEFT_X_EXTRAPOLATOR_NAME, LinearExtrapolator1dAdapter.NAME).with(RIGHT_X_EXTRAPOLATOR_NAME, LinearExtrapolator1dAdapter.NAME)
        .with(PROPERTY_REALIZED_VARIANCE_METHOD, HISTORICAL_REALIZED_VARIANCE).with(CURVE_EXPOSURES, "Exposures").get();
    defaultCalculationConfig.addPortfolioRequirement(FXVolatilitySwapSecurity.SECURITY_TYPE, FAIR_VALUE, properties);
    viewDefinition.addViewCalculationConfiguration(defaultCalculationConfig);
    return viewDefinition;
  }

  /**
   * Creates a view definition for a portfolio of AUD swaps where the curve configuration generates the three yield curves (discounting, 3m
   * forward and 6m forward) simultaneously. This view produces:
   * <ul>
   * <li>ValueRequirementNames#PRESENT_VALUE
   * <li>ValueRequirementNames#YIELD_CURVE_NODE_SENSITIVITIES
   * <li>ValueRequirementNames#YIELD_CURVE
   * </ul>
   *
   * @param portfolioName
   *          The portfolio name
   * @return The view definition
   */
  private ViewDefinition getSyntheticAudSwapView1Definition(final String portfolioName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition("AUD Swaps (3m / 6m basis) (1)", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.AUD);
    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
    final String curveConfig = "DefaultThreeCurveAUDConfig";
    final ViewCalculationConfiguration defaultCalConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE,
        ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig).get());
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_CONFIG, curveConfig).get());
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        ValueProperties.with(CURVE, "ForwardBasis3M").with(CURVE_CALCULATION_CONFIG, curveConfig).get());
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        ValueProperties.with(CURVE, "ForwardBasis6M").with(CURVE_CALCULATION_CONFIG, curveConfig).get());
    final ComputationTargetSpecification currencyTarget = ComputationTargetSpecification.of(Currency.AUD);
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget,
        ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING)
            .with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget,
        ValueProperties.with(CURVE, "ForwardBasis3M").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING)
            .with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget,
        ValueProperties.with(CURVE, "ForwardBasis6M").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING)
            .with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE_JACOBIAN, currencyTarget,
        ValueProperties.with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
    viewDefinition.addViewCalculationConfiguration(defaultCalConfig);
    return viewDefinition;
  }

  /**
   * Creates a view definition for a portfolio of AUD swaps where the curve configuration generates the discounting curve and uses this as
   * an exogenous input into the3m and 6m forward curve calculation. This view produces:
   * <ul>
   * <li>ValueRequirementNames#PRESENT_VALUE
   * <li>ValueRequirementNames#YIELD_CURVE_NODE_SENSITIVITIES
   * <li>ValueRequirementNames#YIELD_CURVE
   * </ul>
   *
   * @param portfolioName
   *          The portfolio name
   * @return The view definition
   */
  private ViewDefinition getSyntheticAudSwapView2Definition(final String portfolioName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition("AUD Swaps (3m / 6m basis) (2)", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.AUD);
    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
    final String curveConfig = "ForwardFromDiscountingAUDConfig";
    final ViewCalculationConfiguration defaultCalConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE,
        ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig).get());
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        ValueProperties.with(CURVE, "ForwardBasis3M").with(CURVE_CALCULATION_CONFIG, curveConfig).get());
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        ValueProperties.with(CURVE, "ForwardBasis6M").with(CURVE_CALCULATION_CONFIG, curveConfig).get());
    final ComputationTargetSpecification currencyTarget = ComputationTargetSpecification.of(Currency.AUD);
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget,
        ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING)
            .with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget,
        ValueProperties.with(CURVE, "ForwardBasis3M").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING)
            .with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget,
        ValueProperties.with(CURVE, "ForwardBasis6M").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING)
            .with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE_JACOBIAN, currencyTarget,
        ValueProperties.with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
    viewDefinition.addViewCalculationConfiguration(defaultCalConfig);
    return viewDefinition;
  }

  /**
   * Creates a view definition for a portfolio of AUD swaps where the pricing uses a single yield curve for both discounting and forward
   * rates. This view produces:
   * <ul>
   * <li>ValueRequirementNames#PRESENT_VALUE
   * <li>ValueRequirementNames#YIELD_CURVE_NODE_SENSITIVITIES
   * <li>ValueRequirementNames#YIELD_CURVE
   * </ul>
   *
   * @param portfolioName
   *          The portfolio name
   * @return The view definition
   */
  private ViewDefinition getSyntheticAudSwapView3Definition(final String portfolioName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition("AUD Swaps (no basis)", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.AUD);
    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
    final String curveConfig = "SingleAUDConfig";
    final ViewCalculationConfiguration defaultCalConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE,
        ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig).get());
    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        ValueProperties.with(CURVE, "Single").with(CURVE_CALCULATION_CONFIG, curveConfig).get());
    final ComputationTargetSpecification currencyTarget = ComputationTargetSpecification.of(Currency.AUD);
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget,
        ValueProperties.with(CURVE, "Single").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig)
            .get()));
    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE_JACOBIAN, currencyTarget,
        ValueProperties.with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
    viewDefinition.addViewCalculationConfiguration(defaultCalConfig);
    return viewDefinition;
  }

  /**
   * Creates a view definition for swaptions priced using the Black method (i.e. no smile). This view produces:
   * <ul>
   * <li>ValueRequirementNames#PRESENT_VALUE
   * <li>ValueRequirementNames#YIELD_CURVE_NODE_SENSITIVITIES
   * <li>ValueRequirementNames#PV01
   * <li>ValueRequirementNames#BUCKETED_PV01
   * <li>ValueRequirementNames#VOLATILITY_SURFACE_DATA
   * </ul>
   *
   * @param portfolioName
   *          The portfolio name
   * @param viewName
   *          The view name
   * @return The view definition
   */
  private ViewDefinition getSyntheticAtmSwaptionViewDefinition(final String portfolioName, final String viewName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
    final ViewCalculationConfiguration defaultCalculationConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    for (final Map.Entry<Currency, String> entry : SWAPTION_CURRENCY_CONFIGS.entrySet()) {
      final ComputationTargetSpecification target = ComputationTargetSpecification.of(entry.getKey().getUniqueId());
      final ValueProperties surfaceProperties = ValueProperties.builder().with(SURFACE, entry.getValue())
          .with(PROPERTY_SURFACE_INSTRUMENT_TYPE, SWAPTION_ATM)
          .get();
      defaultCalculationConfig.addSpecificRequirement(new ValueRequirement(VOLATILITY_SURFACE_DATA, target, surfaceProperties));
    }
    for (final Map.Entry<Currency, Pair<String, String>> entry : SWAPTION_CURVES.entrySet()) {
      ValueProperties properties = ValueProperties.builder().with(CURVE, entry.getValue().getFirst())
          .with(CURVE_CURRENCY, entry.getKey().getCode())
          .with(CALCULATION_METHOD, BLACK_METHOD).get();
      defaultCalculationConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, properties);
      defaultCalculationConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, BUCKETED_PV01, properties);
      defaultCalculationConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, PV01, properties);
      properties = ValueProperties.builder().with(CURVE, entry.getValue().getSecond()).with(CURVE_CURRENCY, entry.getKey().getCode())
          .with(CALCULATION_METHOD, BLACK_METHOD).get();
      defaultCalculationConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, properties);
      defaultCalculationConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, PV01, properties);
    }
    final ValueProperties calculationMethodProperty = ValueProperties.builder().with(CALCULATION_METHOD, BLACK_METHOD).get();
    defaultCalculationConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, PRESENT_VALUE, calculationMethodProperty);
    defaultCalculationConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, VALUE_VEGA, calculationMethodProperty);
    defaultCalculationConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, SECURITY_IMPLIED_VOLATILITY,
        ValueProperties.builder().get());
    viewDefinition.addViewCalculationConfiguration(defaultCalculationConfig);
    return viewDefinition;
  }

  /**
   * Creates a view definition for a portfolio of EUR swaps and interest rate futures. The view definition has two column sets, one showing
   * results when a discounting, 3m Euribor and 6m Euribor curve configuration is used that does not contain futures, and the other showing
   * an equivalent configuration with 3m interest rate futures in the 3m Euribor curve.
   * <p>
   * This view produces:
   * <ul>
   * <li>ValueRequirementNames#PRESENT_VALUE
   * <li>ValueRequirementNames#YIELD_CURVE_SENSITIVITIES
   * </ul>
   *
   * @param portfolioName
   *          The portfolio name
   * @param viewName
   *          The view name
   * @return The view definition
   */
  private ViewDefinition getSyntheticEurFixedIncomeViewDefinition(final String portfolioName, final String viewName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.EUR);
    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
    final String curveConfig1 = "EUR-OIS-3M-6M";
    final String curveConfig2 = "EUR-OIS-3MFut-6M";
    final ViewCalculationConfiguration firstConfig = new ViewCalculationConfiguration(viewDefinition, "EUR-OIS-3M-6M");
    final ComputationTargetSpecification currencyTarget = ComputationTargetSpecification.of(Currency.EUR);
    firstConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE,
        ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig1).get());
    firstConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_CONFIG, curveConfig1).get());
    firstConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        ValueProperties.with(CURVE, "Forward3M").with(CURVE_CALCULATION_CONFIG, curveConfig1).get());
    firstConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        ValueProperties.with(CURVE, "Forward6M").with(CURVE_CALCULATION_CONFIG, curveConfig1).get());
    firstConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget,
        ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING)
            .with(CURVE_CALCULATION_CONFIG, curveConfig1).get()));
    firstConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget,
        ValueProperties.with(CURVE, "Forward3M").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING)
            .with(CURVE_CALCULATION_CONFIG, curveConfig1).get()));
    firstConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget,
        ValueProperties.with(CURVE, "Forward6M").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING)
            .with(CURVE_CALCULATION_CONFIG, curveConfig1).get()));
    viewDefinition.addViewCalculationConfiguration(firstConfig);
    firstConfig.addPortfolioRequirement(FutureSecurity.SECURITY_TYPE, PRESENT_VALUE,
        ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig1).get());
    firstConfig.addPortfolioRequirement(FutureSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_CONFIG, curveConfig1).get());
    firstConfig.addPortfolioRequirement(FutureSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        ValueProperties.with(CURVE, "Forward3M").with(CURVE_CALCULATION_CONFIG, curveConfig1).get());
    firstConfig.addPortfolioRequirement(FutureSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        ValueProperties.with(CURVE, "Forward6M").with(CURVE_CALCULATION_CONFIG, curveConfig1).get());
    viewDefinition.addViewCalculationConfiguration(firstConfig);
    final ViewCalculationConfiguration secondConfig = new ViewCalculationConfiguration(viewDefinition, "EUR-OIS-3MFut-6M");
    secondConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE,
        ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig2).get());
    secondConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_CONFIG, curveConfig2).get());
    secondConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        ValueProperties.with(CURVE, "Forward3MFut").with(CURVE_CALCULATION_CONFIG, curveConfig2).get());
    secondConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        ValueProperties.with(CURVE, "Forward6M").with(CURVE_CALCULATION_CONFIG, curveConfig2).get());
    secondConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget,
        ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING)
            .with(CURVE_CALCULATION_CONFIG, curveConfig2).get()));
    secondConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget,
        ValueProperties.with(CURVE, "Forward3MFut").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING)
            .with(CURVE_CALCULATION_CONFIG, curveConfig2).get()));
    secondConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget,
        ValueProperties.with(CURVE, "Forward6M").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING)
            .with(CURVE_CALCULATION_CONFIG, curveConfig2).get()));
    viewDefinition.addViewCalculationConfiguration(secondConfig);
    secondConfig.addPortfolioRequirement(FutureSecurity.SECURITY_TYPE, PRESENT_VALUE,
        ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig2).get());
    secondConfig.addPortfolioRequirement(FutureSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_CONFIG, curveConfig2).get());
    secondConfig.addPortfolioRequirement(FutureSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        ValueProperties.with(CURVE, "Forward3MFut").with(CURVE_CALCULATION_CONFIG, curveConfig2).get());
    secondConfig.addPortfolioRequirement(FutureSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        ValueProperties.with(CURVE, "Forward6M").with(CURVE_CALCULATION_CONFIG, curveConfig2).get());
    final ViewCalculationConfiguration thirdConfig = new ViewCalculationConfiguration(viewDefinition, "STIR futures MtM");
    thirdConfig.addPortfolioRequirement(FutureSecurity.SECURITY_TYPE, PRESENT_VALUE,
        ValueProperties.with(CALCULATION_METHOD, "MarkToMarket").get());
    viewDefinition.addViewCalculationConfiguration(firstConfig);
    viewDefinition.addViewCalculationConfiguration(secondConfig);
    viewDefinition.addViewCalculationConfiguration(thirdConfig);
    return viewDefinition;
  }

  /**
   * Creates a view definition for a portfolio of FX forwards with two column sets showing the results of calculations by discounting with
   * yield curves implied from FX forwards and of using the FX forward quotes directly.
   * <p>
   * This view produces:
   * <ul>
   * <li>ValueRequirementNames#PRESENT_VALUE
   * <li>ValueRequirementNames#FX_CURRENCY_EXPOSURE
   * </ul>
   *
   * @param portfolioName
   *          The portfolio name
   * @param viewName
   *          The view name
   * @return The view definition
   */
  private ViewDefinition getSyntheticFxForwardViewDefinition(final String portfolioName, final String viewName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
    final ViewCalculationConfiguration calculationConfig1 = new ViewCalculationConfiguration(viewDefinition, "FX Implied Curves");
    final ViewCalculationConfiguration calculationConfig2 = new ViewCalculationConfiguration(viewDefinition, "FX Forward Points");
    final ValueProperties discountingProperties = ValueProperties.builder().with(CALCULATION_METHOD, DISCOUNTING).get();
    final ValueProperties forwardPointsProperties = ValueProperties.builder().with(CALCULATION_METHOD, FORWARD_POINTS).get();
    calculationConfig1.addPortfolioRequirement(FXForwardSecurity.SECURITY_TYPE, PRESENT_VALUE,
        discountingProperties.copy().with(CURRENCY, "USD").get());
    calculationConfig2.addPortfolioRequirement(FXForwardSecurity.SECURITY_TYPE, PRESENT_VALUE,
        forwardPointsProperties.copy().with(CURRENCY, "USD").get());
    calculationConfig1.addPortfolioRequirement(FXForwardSecurity.SECURITY_TYPE, FX_CURRENCY_EXPOSURE, discountingProperties);
    calculationConfig2.addPortfolioRequirement(FXForwardSecurity.SECURITY_TYPE, FX_CURRENCY_EXPOSURE, forwardPointsProperties);
    viewDefinition.addViewCalculationConfiguration(calculationConfig1);
    viewDefinition.addViewCalculationConfiguration(calculationConfig2);
    return viewDefinition;
  }

  /**
   * Creates a view definition for an index future portfolio producing:
   * <ul>
   * <li>ValueRequirementNames#PRESENT_VALUE
   * <li>ValueRequirementNames#PV01
   * <li>ValueRequirementNames#VALUE_DELTA
   * <li>ValueRequirementNames#VALUE_RHO
   * <li>ValueRequirementNames#FORWARD
   * </ul>
   *
   * @param portfolioName
   *          The portfolio name
   * @param viewName
   *          The view name
   * @return The view definition
   */
  private ViewDefinition getSyntheticFutureViewDefinition(final String portfolioName, final String viewName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
    final ViewCalculationConfiguration defaultCalConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    addValueRequirements(defaultCalConfig, FutureSecurity.SECURITY_TYPE,
        new String[] { PRESENT_VALUE, PV01, VALUE_DELTA, VALUE_RHO, FORWARD });
    viewDefinition.addViewCalculationConfiguration(defaultCalConfig);
    return viewDefinition;
  }

  /**
   * Creates a view definition for a bond portfolio that showing the results of calculations that use yield curves and the bond yield
   * directly that produces:
   * <ul>
   * <li>ValueRequirementNames#CLEAN_PRICE
   * <li>ValueRequirementNames#MACAULAY_DURATION
   * <li>ValueRequirementNames#MODIFIED_DURATION
   * <li>ValueRequirementNames#PRESENT_VALUE
   * <li>ValueRequirementNames#YTM
   * </ul>
   *
   * @param portfolioName
   *          The portfolio name
   * @param viewName
   *          The view name
   * @return The view definition
   */
  private ViewDefinition getSyntheticBondViewDefinition(final String portfolioName, final String viewName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
    ViewCalculationConfiguration config = new ViewCalculationConfiguration(viewDefinition, "Curves");
    final ValueProperties curvesProperty = ValueProperties.with(CALCULATION_METHOD, BondFunction.FROM_CURVES_METHOD).get();
    final ValueProperties yieldsProperty = ValueProperties.with(CALCULATION_METHOD, BondFunction.FROM_YIELD_METHOD).get();
    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, CLEAN_PRICE, curvesProperty);
    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, MACAULAY_DURATION, curvesProperty);
    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, MODIFIED_DURATION, curvesProperty);
    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, PRESENT_VALUE, curvesProperty);
    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, YTM, curvesProperty);
    viewDefinition.addViewCalculationConfiguration(config);
    config = new ViewCalculationConfiguration(viewDefinition, "Yields");
    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, CLEAN_PRICE, yieldsProperty);
    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, MACAULAY_DURATION, yieldsProperty);
    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, MODIFIED_DURATION, yieldsProperty);
    viewDefinition.addViewCalculationConfiguration(config);
    return viewDefinition;
  }

  /**
   * Creates a view definition for the OIS portfolio that produces PV, par rate, PV01 and bucketed PV01.
   *
   * @param portfolioName
   *          The portfolio name
   * @param viewName
   *          The view name
   * @return The view definition
   */
  private ViewDefinition getSyntheticOisViewDefinition(final String portfolioName, final String viewName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
    final ViewCalculationConfiguration defaultCalcConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    defaultCalcConfig.addPortfolioRequirementName(SwapSecurity.SECURITY_TYPE, PAR_RATE);
    defaultCalcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE,
        ValueProperties.with(CurrencyConversionFunction.ORIGINAL_CURRENCY, "Default")
            .withOptional(CurrencyConversionFunction.ORIGINAL_CURRENCY).get());
    defaultCalcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE, ValueProperties.with(CURRENCY, "USD").get());
    final MergedOutput discountingPV01Output = new MergedOutput("PV01", MergedOutputAggregationType.LINEAR);
    final MergedOutput discountingYCNSOutput = new MergedOutput("Bucketed PV01", MergedOutputAggregationType.LINEAR);
    for (int i = 0; i < OIS_CURRENCIES.length; i++) {
      final Currency ccy = OIS_CURRENCIES[i];
      final String ccyCode = ccy.getCode();
      discountingPV01Output.addMergedRequirement(PV01, ValueProperties.with(CURVE, "Discounting").with(CURVE_CURRENCY, ccyCode).get());
      discountingYCNSOutput.addMergedRequirement(BUCKETED_PV01,
          ValueProperties.with(CURVE, "Discounting").with(CURVE_CURRENCY, ccyCode).get());
      defaultCalcConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.of(ccy),
          ValueProperties.builder().with(CURVE, "Discounting").with(CURVE_CALCULATION_CONFIG, CURVE_CONFIG_NAMES[i]).get()));
    }
    defaultCalcConfig.addMergedOutput(discountingPV01Output);
    defaultCalcConfig.addMergedOutput(discountingYCNSOutput);
    viewDefinition.addViewCalculationConfiguration(defaultCalcConfig);
    return viewDefinition;
  }

  /**
   * Creates a bond view that calculates clean price, modified and Macaulay durations using both the clean price and the market yield quote,
   * and present value and yield to maturity from the clean price.
   *
   * @param portfolioName
   *          The name of the portfolio
   * @param viewName
   *          The name of the view
   * @return The view definition
   */
  private ViewDefinition getSyntheticMultiCountryBondViewDefinition(final String portfolioName, final String viewName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
    ViewCalculationConfiguration config = new ViewCalculationConfiguration(viewDefinition, "Bond Curves");
    ValueProperties properties = ValueProperties.builder().with(PROPERTY_CURVE_TYPE, "Discounting").with(CURVE_EXPOSURES, "Bond Exposures")
        .with(CALCULATION_METHOD, "Curves").get();
    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, PRESENT_VALUE, properties);
    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, CLEAN_PRICE, properties);
    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, CONVEXITY, properties);
    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, BOND_DETAILS, properties);
    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, MACAULAY_DURATION, properties);
    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, MODIFIED_DURATION, properties);
    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, YTM, properties);
    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, GAMMA_PV01, properties);
    final String[] curveNames = new String[] { "USD Discounting", "US Government Bond" };
    for (final String curveName : curveNames) {
      final ValueProperties curveProperties = properties.copy().with(CURVE, curveName).get();
      config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, PV01, curveProperties);
      config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, BUCKETED_PV01, curveProperties);
    }
    final ValueProperties thetaProperties = properties.copy().with(PROPERTY_DAYS_TO_MOVE_FORWARD, "1")
        .with(PROPERTY_THETA_CALCULATION_METHOD, THETA_CONSTANT_SPREAD).get();
    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, VALUE_THETA, thetaProperties);
    final ValueProperties zSpreadProperties = ValueProperties.builder().with(PROPERTY_CURVE_TYPE, "Discounting")
        .with(CURVE_EXPOSURES, "Bond Exposures")
        .with(CALCULATION_METHOD, "Yield").with(CURVE, "US Government Bond").get();
    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, Z_SPREAD, zSpreadProperties);
    ValueProperties curveProperties = ValueProperties.builder().with(PROPERTY_CURVE_TYPE, "Discounting").with(CURVE, "US Government Bond")
        .with(CURVE_CONSTRUCTION_CONFIG, "US Government Bond Configuration").get();
    config.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties));
    viewDefinition.addViewCalculationConfiguration(config);
    config = new ViewCalculationConfiguration(viewDefinition, "OIS Curves");
    properties = ValueProperties.builder().with(PROPERTY_CURVE_TYPE, "Discounting").with(CURVE_EXPOSURES, "Bond OIS Exposures")
        .with(CALCULATION_METHOD, "Curves").get();
    curveProperties = ValueProperties.builder().with(PROPERTY_CURVE_TYPE, "Discounting")
        .with(CURVE_CONSTRUCTION_CONFIG, "Default USD Curves")
        .with(CURVE, "USD Discounting").get();
    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, PRESENT_VALUE, properties);
    config.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties));
    viewDefinition.addViewCalculationConfiguration(config);
    return viewDefinition;
  }

  /**
   * Creates a view definition for equity options and equities that asks for the PV, value delta, delta, gamma, theta, rho and vega using
   * the Black method if appropriate.
   *
   * @param portfolioName
   *          The portfolio name
   * @param viewName
   *          The view name
   * @return The view definition
   */
  private ViewDefinition getSyntheticEquityOptionViewDefinition(final String portfolioName, final String viewName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
    viewDefinition.setMinFullCalculationPeriod(MAX_FULL_PERIOD);
    final ViewCalculationConfiguration blackConfig = new ViewCalculationConfiguration(viewDefinition, "Black");
    final ValueProperties blackConstraints = ValueProperties.with(CALCULATION_METHOD, BLACK_METHOD).get();
    blackConfig.addPortfolioRequirement(EquityOptionSecurity.SECURITY_TYPE, VALUE_DELTA, blackConstraints);
    blackConfig.addPortfolioRequirement(EquityOptionSecurity.SECURITY_TYPE, PRESENT_VALUE, blackConstraints);
    blackConfig.addPortfolioRequirement(EquityOptionSecurity.SECURITY_TYPE, DELTA, blackConstraints);
    blackConfig.addPortfolioRequirement(EquityOptionSecurity.SECURITY_TYPE, GAMMA, blackConstraints);
    blackConfig.addPortfolioRequirement(EquityOptionSecurity.SECURITY_TYPE, THETA, blackConstraints);
    blackConfig.addPortfolioRequirement(EquityOptionSecurity.SECURITY_TYPE, RHO, blackConstraints);
    blackConfig.addPortfolioRequirement(EquityOptionSecurity.SECURITY_TYPE, VEGA, blackConstraints);
    viewDefinition.addViewCalculationConfiguration(blackConfig);
    final ViewCalculationConfiguration bawConfig = new ViewCalculationConfiguration(viewDefinition, "Barone-Adesi-Whaley");
    final ValueProperties bawConstraints = ValueProperties.with(CALCULATION_METHOD, BAW_METHOD).get();
    bawConfig.addPortfolioRequirement(EquityOptionSecurity.SECURITY_TYPE, VALUE_DELTA, bawConstraints);
    bawConfig.addPortfolioRequirement(EquityOptionSecurity.SECURITY_TYPE, PRESENT_VALUE, bawConstraints);
    bawConfig.addPortfolioRequirement(EquityOptionSecurity.SECURITY_TYPE, DELTA, bawConstraints);
    bawConfig.addPortfolioRequirement(EquityOptionSecurity.SECURITY_TYPE, GAMMA, bawConstraints);
    bawConfig.addPortfolioRequirement(EquityOptionSecurity.SECURITY_TYPE, THETA, bawConstraints);
    bawConfig.addPortfolioRequirement(EquityOptionSecurity.SECURITY_TYPE, RHO, bawConstraints);
    bawConfig.addPortfolioRequirement(EquityOptionSecurity.SECURITY_TYPE, VEGA, bawConstraints);
    viewDefinition.addViewCalculationConfiguration(bawConfig);
    return viewDefinition;
  }

  /**
   * Adds a list of value requirement names to a calculation configuration for a particular security type.
   *
   * @param calcConfiguration
   *          The calculation configuration
   * @param securityType
   *          The security type
   * @param valueRequirementNames
   *          The value requirement names to add
   */
  private static void addValueRequirements(final ViewCalculationConfiguration calcConfiguration, final String securityType,
      final String[] valueRequirementNames) {
    for (final String valueRequirementName : valueRequirementNames) {
      calcConfiguration.addPortfolioRequirementName(securityType, valueRequirementName);
    }
  }

  /**
   * Gets the id for a portfolio name.
   *
   * @param portfolioName
   *          The portfolio name
   * @return The unique id of the portfolio
   */
  private UniqueId getPortfolioId(final String portfolioName) {
    final PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
    searchRequest.setName(portfolioName);
    final PortfolioSearchResult searchResult = getToolContext().getPortfolioMaster().search(searchRequest);
    if (searchResult.getFirstPortfolio() == null) {
      LOGGER.error("Couldn't find portfolio {}", portfolioName);
      throw new OpenGammaRuntimeException("Couldn't find portfolio " + portfolioName);
    }
    return searchResult.getFirstPortfolio().getUniqueId();
  }

  /**
   * Stores a view definition in the config master.
   *
   * @param viewDefinition
   *          The view definition
   */
  private void storeViewDefinition(final ViewDefinition viewDefinition) {
    final ConfigItem<ViewDefinition> config = ConfigItem.of(viewDefinition, viewDefinition.getName(), ViewDefinition.class);
    ConfigMasterUtils.storeByName(getToolContext().getConfigMaster(), config);
  }

}
