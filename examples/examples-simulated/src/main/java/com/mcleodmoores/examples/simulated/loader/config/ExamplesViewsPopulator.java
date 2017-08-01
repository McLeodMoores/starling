/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.examples.simulated.loader.config;

import static com.mcleodmoores.examples.simulated.loader.ExamplesDatabasePopulator.AUD_SWAP_PORFOLIO_NAME;
import static com.mcleodmoores.examples.simulated.loader.ExamplesDatabasePopulator.FX_FORWARD_PORTFOLIO_NAME;
import static com.mcleodmoores.examples.simulated.loader.ExamplesDatabasePopulator.MULTI_CURRENCY_SWAP_PORTFOLIO_NAME;
import static com.mcleodmoores.examples.simulated.loader.ExamplesDatabasePopulator.VANILLA_FX_OPTION_PORTFOLIO_NAME;
import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValuePropertyNames.SURFACE;
import static com.opengamma.engine.value.ValueRequirementNames.CAPM_BETA;
import static com.opengamma.engine.value.ValueRequirementNames.FAIR_VALUE;
import static com.opengamma.engine.value.ValueRequirementNames.FORWARD_DELTA;
import static com.opengamma.engine.value.ValueRequirementNames.FORWARD_DRIFTLESS_THETA;
import static com.opengamma.engine.value.ValueRequirementNames.FORWARD_GAMMA;
import static com.opengamma.engine.value.ValueRequirementNames.FORWARD_VEGA;
import static com.opengamma.engine.value.ValueRequirementNames.FX_CURRENCY_EXPOSURE;
import static com.opengamma.engine.value.ValueRequirementNames.HISTORICAL_VAR;
import static com.opengamma.engine.value.ValueRequirementNames.NOTIONAL;
import static com.opengamma.engine.value.ValueRequirementNames.PAR_RATE;
import static com.opengamma.engine.value.ValueRequirementNames.PNL;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE;
import static com.opengamma.engine.value.ValueRequirementNames.SECURITY_IMPLIED_VOLATILITY;
import static com.opengamma.engine.value.ValueRequirementNames.SHARPE_RATIO;
import static com.opengamma.engine.value.ValueRequirementNames.VALUE_DELTA;
import static com.opengamma.engine.value.ValueRequirementNames.VALUE_GAMMA;
import static com.opengamma.engine.value.ValueRequirementNames.VALUE_VANNA;
import static com.opengamma.engine.value.ValueRequirementNames.VALUE_VEGA;
import static com.opengamma.engine.value.ValueRequirementNames.VALUE_VOMMA;
import static com.opengamma.engine.value.ValueRequirementNames.VEGA_MATRIX;
import static com.opengamma.engine.value.ValueRequirementNames.VEGA_QUOTE_MATRIX;
import static com.opengamma.engine.value.ValueRequirementNames.VOLATILITY_SURFACE_DATA;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES;
import static com.opengamma.financial.analytics.model.InstrumentTypeProperties.FOREX;
import static com.opengamma.financial.analytics.model.InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.examples.simulated.loader.ExampleEquityPortfolioLoader;
import com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues;
import com.opengamma.financial.currency.CurrencyConversionFunction;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
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

@SuppressWarnings("deprecation")
@Scriptable
public class ExamplesViewsPopulator extends AbstractTool<ToolContext> {
  /** Name of the default calculation configurations. */
  private static final String DEFAULT_CALC_CONFIG = "Default";
  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ExamplesViewsPopulator.class);
  /** A list of currencies. */
  public static final Currency[] SWAP_CURRENCIES = new Currency[] {
      Currency.USD,
      Currency.GBP,
      Currency.EUR,
      Currency.JPY,
      Currency.CHF };
  /** A list of OIS currencies */
  private static final Currency[] OIS_CURRENCIES = new Currency[] {
      Currency.USD,
      Currency.GBP,
      Currency.EUR,
      Currency.JPY };
  /** A list of currency pairs. */
  public static final UnorderedCurrencyPair[] CURRENCY_PAIRS = new UnorderedCurrencyPair[] {
      UnorderedCurrencyPair.of(Currency.USD, Currency.EUR),
      UnorderedCurrencyPair.of(Currency.USD, Currency.CHF),
      UnorderedCurrencyPair.of(Currency.USD, Currency.AUD),
      UnorderedCurrencyPair.of(Currency.USD, Currency.GBP),
//      UnorderedCurrencyPair.of(Currency.USD, Currency.JPY),
      UnorderedCurrencyPair.of(Currency.GBP, Currency.EUR),
      UnorderedCurrencyPair.of(Currency.CHF, Currency.JPY) };
  /** The default maximum delta calculation period */
  private static final long MAX_DELTA_PERIOD = 500L;
  /** The default maximum full calculation period */
  private static final long MAX_FULL_PERIOD = 500L;
  /** The default minimum delta calculation period */
  private static final long MIN_DELTA_PERIOD = 500L;
  /** The default minimum full calculation period */
  private static final long MIN_FULL_PERIOD = 500L;

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   *
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) {
    new ExamplesViewsPopulator().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    storeViewDefinition(getEquityViewDefinition(ExampleEquityPortfolioLoader.PORTFOLIO_NAME));
    //    storeViewDefinition(getSyntheticMultiCurrencySwapViewDefinitionWithSeparateOutputs(MULTI_CURRENCY_SWAP_PORTFOLIO_NAME));
    storeViewDefinition(getMultiCurrencySwapViewDefinition(MULTI_CURRENCY_SWAP_PORTFOLIO_NAME));
    storeViewDefinition(getAudSwapView1Definition(AUD_SWAP_PORFOLIO_NAME));
    //    storeViewDefinition(getSyntheticAudSwapView2Definition(AUD_SWAP_PORFOLIO_NAME));
    //    storeViewDefinition(getSyntheticAudSwapView3Definition(AUD_SWAP_PORFOLIO_NAME));
    //    storeViewDefinition(getSyntheticSwaptionParityViewDefinition(SWAPTION_PORTFOLIO_NAME));
    storeViewDefinition(getFxOptionViewDefinition(VANILLA_FX_OPTION_PORTFOLIO_NAME, "FX Option View"));
    storeViewDefinition(getFxOptionGreeksViewDefinition(VANILLA_FX_OPTION_PORTFOLIO_NAME, "FX Option Greeks View"));
    //    storeViewDefinition(getSyntheticAtmSwaptionViewDefinition(MULTI_CURRENCY_SWAPTION_PORTFOLIO_NAME, "Swaption Black Pricing View"));
    //    storeViewDefinition(getSyntheticSabrExtrapolationViewDefinition(MIXED_CMS_PORTFOLIO_NAME));
    //    storeViewDefinition(getSyntheticEurFixedIncomeViewDefinition(EUR_SWAP_PORTFOLIO_NAME, "EUR Swap Desk View"));
    storeViewDefinition(getFxForwardViewDefinition(FX_FORWARD_PORTFOLIO_NAME, "FX Forward View"));
    //    storeViewDefinition(getSyntheticFutureViewDefinition(FUTURE_PORTFOLIO_NAME, "Futures View"));
    //    storeViewDefinition(getSyntheticBondViewDefinition(US_GOVERNMENT_BOND_PORTFOLIO_NAME, "Government Bond View"));
    //    storeViewDefinition(getSyntheticFxVolatilitySwapViewDefinition(FX_VOLATILITY_SWAP_PORTFOLIO_NAME, "FX Volatility Swap View"));
    //    storeViewDefinition(getSyntheticOisViewDefinition(OIS_PORTFOLIO_NAME, "OIS View"));
    //    storeViewDefinition(getSyntheticMultiCountryBondViewDefinition(BONDS_PORTFOLIO_NAME, "Bond Portfolio View"));
  }

  /**
   * Creates a view definition for a portfolio containing only equities that produces:
   * <ul>
   * <li> ValueRequirementNames#FAIR_VALUE
   * <li> ValueRequirementNames#CAPM_BETA
   * <li> ValueRequirementNames#HISTORICAL_VAR
   * <li> ValueRequirementNames#SHARPE_RATIO
   * <li> ValueRequirementNames#PNL
   * </ul>
   * @param portfolioName The portfolio name
   * @return The view definition
   */
  private ViewDefinition getEquityViewDefinition(final String portfolioName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(portfolioName + " View", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    final long maxPeriod = 30000L;
    viewDefinition.setMaxFullCalculationPeriod(maxPeriod);
    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
    viewDefinition.setMaxDeltaCalculationPeriod(maxPeriod);

    final ViewCalculationConfiguration defaultCalc = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    defaultCalc.addPortfolioRequirement(EquitySecurity.SECURITY_TYPE, FAIR_VALUE, ValueProperties.builder().with(CURRENCY, "USD").get());
    final String[] valueRequirementNames = new String[] {CAPM_BETA, SHARPE_RATIO};
    addValueRequirements(defaultCalc, EquitySecurity.SECURITY_TYPE, valueRequirementNames);
    defaultCalc.addPortfolioRequirement(EquitySecurity.SECURITY_TYPE, PNL, ValueProperties.builder().with(CURRENCY, "USD").get());
    defaultCalc.addPortfolioRequirement(EquitySecurity.SECURITY_TYPE, HISTORICAL_VAR, ValueProperties.builder().with(CURRENCY, "USD").get());
    viewDefinition.addViewCalculationConfiguration(defaultCalc);
    return viewDefinition;
  }

//  /**
//   * Creates a view definition for a swap portfolio that produces:
//   * <ul>
//   * <li> ValueRequirementNames#PRESENT_VALUE
//   * <li> ValueRequirementNames#PV01 for named curves
//   * <li> ValueRequirementNames#YIELD_CURVE_NODE_SENSITIVITIES for named curves
//   * <li> ValueRequirementNames#YIELD_CURVE
//   * </ul>
//   * The curve-specific risk outputs are not collapsed into a single column.
//   * @param portfolioName The portfolio name
//   * @return The view definition
//   */
//  private ViewDefinition getSyntheticMultiCurrencySwapViewDefinitionWithSeparateOutputs(final String portfolioName) {
//    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
//    final ViewDefinition viewDefinition = new ViewDefinition(portfolioName + " View (outputs by ccy)", portfolioId, UserPrincipal.getTestUser());
//    viewDefinition.setDefaultCurrency(Currency.USD);
//    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
//    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
//    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
//    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
//    final ViewCalculationConfiguration defaultCalConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
//    defaultCalConfig.addPortfolioRequirementName(SwapSecurity.SECURITY_TYPE, PAR_RATE);
//    // The name "Default" has no special meaning, but means that the currency conversion function can never be used
//    // and so we get the instrument's natural currency
//    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE,
//        ValueProperties.with(CurrencyConversionFunction.ORIGINAL_CURRENCY, "Default").withOptional(CurrencyConversionFunction.ORIGINAL_CURRENCY).get());
//    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE, ValueProperties.with(CURRENCY, "USD").get());
//    for (int i = 0; i < SWAP_CURRENCIES.length; i++) {
//      final Currency ccy = SWAP_CURRENCIES[i];
//      final String ccyCode = ccy.getCode();
//      final ComputationTargetSpecification currencyTarget = ComputationTargetSpecification.of(ccy);
//      final String forwardCurveName = ccyCode.equals("USD") ? "Forward3M" : "Forward6M";
//      final ValueProperties discountingRiskProperties = ValueProperties.with(CURVE, "Discounting").with(CURVE_CURRENCY, ccyCode).get();
//      final ValueProperties discountingCurveProperties = ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_CONFIG, CURVE_CONFIG_NAMES[i]).get();
//      final ValueProperties forwardRiskProperties = ValueProperties.with(CURVE, forwardCurveName).with(CURVE_CURRENCY, ccyCode).get();
//      final ValueProperties forwardCurveProperties = ValueProperties.with(CURVE, forwardCurveName).with(CURVE_CALCULATION_CONFIG, CURVE_CONFIG_NAMES[i]).get();
//      defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PV01, discountingRiskProperties);
//      defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, discountingRiskProperties);
//      defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget, discountingCurveProperties));
//      defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, forwardRiskProperties);
//      defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PV01, forwardRiskProperties);
//      defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget, forwardCurveProperties));
//    }
//    viewDefinition.addViewCalculationConfiguration(defaultCalConfig);
//    return viewDefinition;
//  }

  /**
   * Creates a view definition for a swap portfolio that produces:
   * <ul>
   * <li> ValueRequirementNames#NOTIONAL
   * <li> ValueRequirementNames#FIXED_RATE
   * <li> ValueRequirementNames#SWAP_DETAILS
   * <li> ValueRequirementNames#PAR_RATE
   * <li> ValueRequirementNames#PRESENT_VALUE
   * <li> ValueRequirementNames#PV01 for named curves
   * <li> ValueRequirementNames#YIELD_CURVE_NODE_SENSITIVITIES for named curves
   * <li> ValueRequirementNames#YIELD_CURVE
   * </ul>
   * The curve-specific risk outputs are not collapsed into a single column.
   * @param portfolioName  the portfolio name
   * @return  the view definition
   */
  private ViewDefinition getMultiCurrencySwapViewDefinition(final String portfolioName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(portfolioName + " View", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
    final ValueProperties calcProperties =  ValueProperties.builder()
        .with(CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE, CurveCalculationPropertyNamesAndValues.DISCOUNTING)
        .get();
    final ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    calcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, NOTIONAL, ValueProperties.none());
    calcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PAR_RATE, calcProperties);
    calcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE,
        calcProperties.compose(ValueProperties.with(CurrencyConversionFunction.ORIGINAL_CURRENCY, "Default")
            .withOptional(CurrencyConversionFunction.ORIGINAL_CURRENCY).get()));
//    defaultCalcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE, ValueProperties.with(CURRENCY, "USD").get());
//    final MergedOutput discountingPV01Output = new MergedOutput("Discounting PV01", MergedOutputAggregationType.LINEAR);
//    final MergedOutput discountingYCNSOutput = new MergedOutput("Discounting Bucketed PV01", MergedOutputAggregationType.LINEAR);
//    final MergedOutput forwardPV01Output = new MergedOutput("Forward PV01", MergedOutputAggregationType.LINEAR);
//    final MergedOutput forwardYCNSOutput = new MergedOutput("Forward Bucketed PV01", MergedOutputAggregationType.LINEAR);
    for (final Currency ccy : SWAP_CURRENCIES) {
      final String ccyCode = ccy.getCode();
      final ComputationTargetSpecification currencyTarget = ComputationTargetSpecification.of(ccy);
//      final String forwardCurveName = ccyCode.equals("USD") ? "Forward3M" : "Forward6M";
//      final ValueProperties discountingRiskProperties = ValueProperties.with(CURVE, "Discounting").with(CURVE_CURRENCY, ccyCode).get();
//      final ValueProperties discountingCurveProperties = ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_CONFIG, CURVE_CONFIG_NAMES[i]).get();
//      final ValueProperties forwardRiskProperties = ValueProperties.with(CURVE, forwardCurveName).with(CURVE_CURRENCY, ccyCode).get();
//      final ValueProperties forwardCurveProperties = ValueProperties.with(CURVE, forwardCurveName).with(CURVE_CALCULATION_CONFIG, CURVE_CONFIG_NAMES[i]).get();
//      discountingPV01Output.addMergedRequirement(PV01, discountingRiskProperties);
//      discountingYCNSOutput.addMergedRequirement(YIELD_CURVE_NODE_SENSITIVITIES, discountingRiskProperties);
//      defaultCalcConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget, discountingCurveProperties));
//      forwardYCNSOutput.addMergedRequirement(YIELD_CURVE_NODE_SENSITIVITIES, forwardRiskProperties);
//      forwardPV01Output.addMergedRequirement(PV01, forwardRiskProperties);
//      defaultCalcConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget, forwardCurveProperties));
    }
//    defaultCalcConfig.addMergedOutput(discountingPV01Output);
//    defaultCalcConfig.addMergedOutput(discountingYCNSOutput);
//    defaultCalcConfig.addMergedOutput(forwardPV01Output);
//    defaultCalcConfig.addMergedOutput(forwardYCNSOutput);
    viewDefinition.addViewCalculationConfiguration(calcConfig);
    return viewDefinition;
  }

  /**
   * Creates a view definition for an FX option portfolio that produces:
   * <ul>
   * <li> ValueRequirementNames#VOLATILITY_SURFACE_DATA
   * <li> ValueRequirementNames#VEGA_QUOTE_MATRIX
   * <li> ValueRequirementNames#VEGA_MATRIX
   * <li> ValueRequirementNames#YIELD_CURVE_NODE_SENSITIVITIES
   * <li> ValueRequirementNames#PRESENT_VALUE
   * <li> ValueRequirementNames#FX_CURRENCY_EXPOSURE
   * </ul>
   * @param portfolioName The portfolio name
   * @param viewName The view name
   * @return The view definition
   */
  private ViewDefinition getFxOptionViewDefinition(final String portfolioName, final String viewName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
    final ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    final Set<Currency> ccysAdded = new HashSet<>();
    for (final UnorderedCurrencyPair pair : CURRENCY_PAIRS) {
      final ComputationTargetSpecification target = ComputationTargetSpecification.of(pair.getUniqueId());
      final ValueProperties surfaceProperties = ValueProperties.builder()
          .with(SURFACE, "DEFAULT")
          .with(PROPERTY_SURFACE_INSTRUMENT_TYPE, FOREX)
          .get();
      calcConfig.addSpecificRequirement(new ValueRequirement(VOLATILITY_SURFACE_DATA, target, surfaceProperties));
      final ValueProperties currencyProperty = ValueProperties.builder().with(CURRENCY, "USD").get();
      calcConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, PRESENT_VALUE, currencyProperty);
      calcConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, FX_CURRENCY_EXPOSURE, ValueProperties.builder().get());
      if (!ccysAdded.contains(pair.getFirstCurrency())) {
        if (pair.getFirstCurrency().equals(Currency.USD)) {
          final ValueProperties curveProperties = ValueProperties.builder().with(CURVE, ExamplesFxImpliedCurveConfigsPopulator.USD_DEPOSIT_CURVE_NAME).get();
          calcConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, curveProperties);
          calcConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties));
          ccysAdded.add(pair.getFirstCurrency());
        } else {
          final ValueProperties curveProperties = ValueProperties.builder().with(CURVE,
              ExampleConfigUtils.generateFxImpliedCurveName(pair.getFirstCurrency().getCode())).get();
          calcConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, curveProperties);
          calcConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties));
          ccysAdded.add(pair.getFirstCurrency());
        }
      }
      if (!ccysAdded.contains(pair.getSecondCurrency())) {
        if (pair.getSecondCurrency().equals(Currency.USD)) {
          final ValueProperties curveProperties = ValueProperties.builder().with(CURVE, ExamplesFxImpliedCurveConfigsPopulator.USD_DEPOSIT_CURVE_NAME).get();
          calcConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, curveProperties);
          calcConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties));
          ccysAdded.add(pair.getFirstCurrency());
        } else {
          final ValueProperties curveProperties = ValueProperties.builder().with(CURVE,
              ExampleConfigUtils.generateFxImpliedCurveName(pair.getFirstCurrency().getCode())).get();
          calcConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, curveProperties);
          calcConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties));
          ccysAdded.add(pair.getFirstCurrency());
        }
      }
    }
    calcConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, VEGA_QUOTE_MATRIX, ValueProperties.builder().get());
    calcConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, VEGA_MATRIX, ValueProperties.builder().get());
    viewDefinition.addViewCalculationConfiguration(calcConfig);
    return viewDefinition;
  }

  /**
   * Creates a view definition for an FX option portfolio that produces:
   * <ul>
   * <li> ValueRequirementNames#PRESENT_VALUE
   * <li> ValueRequirementNames#VALUE_DELTA
   * <li> ValueRequirementNames#VALUE_VEGA
   * <li> ValueRequirementNames#VALUE_GAMMA_P
   * <li> ValueRequirementNames#VALUE_RHO
   * <li> ValueRequirementNames#VALUE_PHI
   * <li> ValueRequirementNames#VALUE_VOMMA
   * <li> ValueRequirementNames#VALUE_VANNA
   * <li> ValueRequirementNames#VALUE_THETA
   * <li> ValueRequirementNames#SECURITY_IMPLIED_VOLATILITY
   * </ul>
   * @param portfolioName The portfolio name
   * @param viewName The view name
   * @return The view definition
   */
  private ViewDefinition getFxOptionGreeksViewDefinition(final String portfolioName, final String viewName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
    final ViewCalculationConfiguration defaultCalculationConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    final ValueProperties currencyProperty = ValueProperties.builder().with(CURRENCY, "USD").get();
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, PRESENT_VALUE, currencyProperty);
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, SECURITY_IMPLIED_VOLATILITY, ValueProperties.builder().get());
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, FORWARD_DELTA, ValueProperties.builder().get());
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, FORWARD_VEGA, ValueProperties.builder().get());
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, FORWARD_GAMMA, ValueProperties.builder().get());
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, FORWARD_DRIFTLESS_THETA, ValueProperties.builder().get());
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, VALUE_DELTA, currencyProperty);
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, VALUE_GAMMA, currencyProperty);
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, VALUE_VEGA, currencyProperty);
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, VALUE_VANNA, currencyProperty);
    defaultCalculationConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, VALUE_VOMMA, currencyProperty);
    viewDefinition.addViewCalculationConfiguration(defaultCalculationConfig);
    return viewDefinition;
  }

  /**
   * Creates a view definition for FX volatility swaps that requests the surface data
   * for each currency pair and the fair value for each swap.
   * @param portfolioName The portfolio name
   * @param viewName The view name
   * @return The view definition
   */
  //  private ViewDefinition getSyntheticFxVolatilitySwapViewDefinition(final String portfolioName, final String viewName) {
  //    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
  //    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
  //    viewDefinition.setDefaultCurrency(Currency.USD);
  //    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
  //    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
  //    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
  //    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
  //    final ViewCalculationConfiguration defaultCalculationConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
  //    for (final UnorderedCurrencyPair pair : CURRENCY_PAIRS) {
  //      final ComputationTargetSpecification target = ComputationTargetSpecification.of(pair.getUniqueId());
  //      final ValueProperties properties = ValueProperties.builder()
  //          .with(SURFACE, "DEFAULT")
  //          .with(PROPERTY_SURFACE_INSTRUMENT_TYPE, FOREX)
  //          .get();
  //      defaultCalculationConfig.addSpecificRequirement(new ValueRequirement(VOLATILITY_SURFACE_DATA, target, properties));
  //    }
  //    final ValueProperties properties = ValueProperties.builder()
  //        .with(SURFACE, "DEFAULT")
  //        .with(X_INTERPOLATOR_NAME, Interpolator1DFactory.LINEAR)
  //        .with(LEFT_X_EXTRAPOLATOR_NAME, Interpolator1DFactory.LINEAR_EXTRAPOLATOR)
  //        .with(RIGHT_X_EXTRAPOLATOR_NAME, Interpolator1DFactory.LINEAR_EXTRAPOLATOR)
  //        .with(PROPERTY_REALIZED_VARIANCE_METHOD, HISTORICAL_REALIZED_VARIANCE)
  //        .with(CURVE_EXPOSURES, "Exposures")
  //        .get();
  //    defaultCalculationConfig.addPortfolioRequirement(FXVolatilitySwapSecurity.SECURITY_TYPE, FAIR_VALUE, properties);
  //    viewDefinition.addViewCalculationConfiguration(defaultCalculationConfig);
  //    return viewDefinition;
  //  }

  /**
   * Creates a view definition for a portfolio of AUD swaps where the curve configuration generates the
   * three yield curves (discounting, 3m forward and 6m forward) simultaneously. This view produces:
   * <ul>
   * <li> ValueRequirementNames#PRESENT_VALUE
   * <li> ValueRequirementNames#YIELD_CURVE_NODE_SENSITIVITIES
   * <li> ValueRequirementNames#YIELD_CURVE
   * </ul>
   * @param portfolioName The portfolio name
   * @return The view definition
   */
  private ViewDefinition getAudSwapView1Definition(final String portfolioName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition("AUD Swaps (3m / 6m basis) (1)", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.AUD);
    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
    final ValueProperties.Builder properties = ValueProperties.builder()
        .with(PROPERTY_CURVE_TYPE, CurveCalculationPropertyNamesAndValues.DISCOUNTING)
        .with(CURVE_EXPOSURES, "AUD Swaps (1)");
    final ViewCalculationConfiguration viewConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    viewConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE, properties.get());
    viewConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        properties.copy().with(CURVE, "AUD Discounting").get());
    viewConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        properties.copy().with(CURVE, "AUD 3M XIBOR").get());
    viewConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
        properties.copy().with(CURVE, "AUD 6M XIBOR").get());
    viewConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL,
        properties.copy().with(CURVE, "AUD Discounting").get()));
    viewConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL,
        properties.copy().with(CURVE, "AUD 3M XIBOR").get()));
    viewConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL,
        properties.copy().with(CURVE, "AUD 6M XIBOR").get()));
    viewDefinition.addViewCalculationConfiguration(viewConfig);
    return viewDefinition;
  }

  /**
   * Creates a view definition for a portfolio of AUD swaps where the curve configuration generates
   * the discounting curve and uses this as an exogenous input into the3m and 6m forward curve calculation.
   * This view produces:
   * <ul>
   * <li> ValueRequirementNames#PRESENT_VALUE
   * <li> ValueRequirementNames#YIELD_CURVE_NODE_SENSITIVITIES
   * <li> ValueRequirementNames#YIELD_CURVE
   * </ul>
   * @param portfolioName The portfolio name
   * @return The view definition
   */
  //  private ViewDefinition getSyntheticAudSwapView2Definition(final String portfolioName) {
  //    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
  //    final ViewDefinition viewDefinition = new ViewDefinition("AUD Swaps (3m / 6m basis) (2)", portfolioId, UserPrincipal.getTestUser());
  //    viewDefinition.setDefaultCurrency(Currency.AUD);
  //    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
  //    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
  //    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
  //    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
  //    final String curveConfig = "ForwardFromDiscountingAUDConfig";
  //    final ViewCalculationConfiguration defaultCalConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
  //    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE, ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig).get());
  //    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
  //        ValueProperties.with(CURVE, "ForwardBasis3M").with(CURVE_CALCULATION_CONFIG, curveConfig).get());
  //    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
  //        ValueProperties.with(CURVE, "ForwardBasis6M").with(CURVE_CALCULATION_CONFIG, curveConfig).get());
  //    final ComputationTargetSpecification currencyTarget = ComputationTargetSpecification.of(Currency.AUD);
  //    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget,
  //        ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
  //    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget,
  //        ValueProperties.with(CURVE, "ForwardBasis3M").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
  //    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget,
  //        ValueProperties.with(CURVE, "ForwardBasis6M").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
  //    viewDefinition.addViewCalculationConfiguration(defaultCalConfig);
  //    return viewDefinition;
  //  }

  /**
   * Creates a view definition for a portfolio of AUD swaps where the pricing uses a single
   * yield curve for both discounting and forward rates. This view produces:
   * <ul>
   * <li> ValueRequirementNames#PRESENT_VALUE
   * <li> ValueRequirementNames#YIELD_CURVE_NODE_SENSITIVITIES
   * <li> ValueRequirementNames#YIELD_CURVE
   * </ul>
   * @param portfolioName The portfolio name
   * @return The view definition
   */
  //  private ViewDefinition getSyntheticAudSwapView3Definition(final String portfolioName) {
  //    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
  //    final ViewDefinition viewDefinition = new ViewDefinition("AUD Swaps (no basis)", portfolioId, UserPrincipal.getTestUser());
  //    viewDefinition.setDefaultCurrency(Currency.AUD);
  //    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
  //    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
  //    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
  //    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
  //    final String curveConfig = "SingleAUDConfig";
  //    final ViewCalculationConfiguration defaultCalConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
  //    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE,
  //        ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig).get());
  //    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
  //        ValueProperties.with(CURVE, "Single").with(CURVE_CALCULATION_CONFIG, curveConfig).get());
  //    final ComputationTargetSpecification currencyTarget = ComputationTargetSpecification.of(Currency.AUD);
  //    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget,
  //        ValueProperties.with(CURVE, "Single").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
  //    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE_JACOBIAN, currencyTarget,
  //        ValueProperties.with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
  //    viewDefinition.addViewCalculationConfiguration(defaultCalConfig);
  //    return viewDefinition;
  //  }

  /**
   * Creates a view definition for swaptions priced using the Black method (i.e. no smile).
   * This view produces:
   * <ul>
   * <li> ValueRequirementNames#PRESENT_VALUE
   * <li> ValueRequirementNames#YIELD_CURVE_NODE_SENSITIVITIES
   * <li> ValueRequirementNames#PV01
   * <li> ValueRequirementNames#BUCKETED_PV01
   * <li> ValueRequirementNames#VOLATILITY_SURFACE_DATA
   * </ul>
   * @param portfolioName The portfolio name
   * @param viewName The view name
   * @return The view definition
   */
  //  private ViewDefinition getSyntheticAtmSwaptionViewDefinition(final String portfolioName, final String viewName) {
  //    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
  //    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
  //    viewDefinition.setDefaultCurrency(Currency.USD);
  //    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
  //    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
  //    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
  //    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
  //    final ViewCalculationConfiguration defaultCalculationConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
  //    for (final Map.Entry<Currency, String> entry : SWAPTION_CURRENCY_CONFIGS.entrySet()) {
  //      final ComputationTargetSpecification target = ComputationTargetSpecification.of(entry.getKey().getUniqueId());
  //      final ValueProperties surfaceProperties = ValueProperties.builder()
  //          .with(SURFACE, entry.getValue())
  //          .with(PROPERTY_SURFACE_INSTRUMENT_TYPE, SWAPTION_ATM)
  //          .get();
  //      defaultCalculationConfig.addSpecificRequirement(new ValueRequirement(VOLATILITY_SURFACE_DATA, target, surfaceProperties));
  //    }
  //    for (final Map.Entry<Currency, Pair<String, String>> entry : SWAPTION_CURVES.entrySet()) {
  //      ValueProperties properties = ValueProperties.builder()
  //          .with(CURVE, entry.getValue().getFirst())
  //          .with(CURVE_CURRENCY, entry.getKey().getCode())
  //          .with(CALCULATION_METHOD, BLACK_METHOD)
  //          .get();
  //      defaultCalculationConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, properties);
  //      defaultCalculationConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, BUCKETED_PV01, properties);
  //      defaultCalculationConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, PV01, properties);
  //      properties = ValueProperties.builder()
  //          .with(CURVE, entry.getValue().getSecond())
  //          .with(CURVE_CURRENCY, entry.getKey().getCode())
  //          .with(CALCULATION_METHOD, BLACK_METHOD)
  //          .get();
  //      defaultCalculationConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, properties);
  //      defaultCalculationConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, PV01, properties);
  //    }
  //    final ValueProperties calculationMethodProperty = ValueProperties.builder().with(CALCULATION_METHOD, BLACK_METHOD).get();
  //    defaultCalculationConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, PRESENT_VALUE, calculationMethodProperty);
  //    defaultCalculationConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, VALUE_VEGA, calculationMethodProperty);
  //    defaultCalculationConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, SECURITY_IMPLIED_VOLATILITY, ValueProperties.builder().get());
  //    viewDefinition.addViewCalculationConfiguration(defaultCalculationConfig);
  //    return viewDefinition;
  //  }

  /**
   * Creates a view definition for cap/floor CMS spreads, cap/floors and CMS swaps that are priced using SABR with and without extrapolation.
   * The view produces:
   * <ul>
   * <li> ValueRequirementNames#PRESENT_VALUE
   * <li> ValueRequirementNames#PRESENT_VALUE_SABR_ALPHA_SENSITIVITY
   * <li> ValueRequirementNames#PRESENT_VALUE_SABR_NU_SENSITIVITY
   * <li> ValueRequirementNames#PRESENT_VALUE_SABR_RHO_SENSITIVITY
   * <li> ValueRequirementNames#YIELD_CURVE_NODE_SENSITIVITIES
   * </ul>
   * for both calculation methods.
   * @param portfolioName The portfolio name
   * @return The view definition
   */
  //  private ViewDefinition getSyntheticSabrExtrapolationViewDefinition(final String portfolioName) {
  //    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
  //    final ViewDefinition viewDefinition = new ViewDefinition("Constant Maturity Swap View", portfolioId, UserPrincipal.getTestUser());
  //    viewDefinition.setDefaultCurrency(Currency.USD);
  //    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
  //    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
  //    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
  //    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
  //    final String curveConfig = "DefaultTwoCurveUSDConfig";
  //    final ViewCalculationConfiguration noExtrapolationConfig = new ViewCalculationConfiguration(viewDefinition, "No Extrapolation");
  //    final ViewCalculationConfiguration rightExtrapolationConfig = new ViewCalculationConfiguration(viewDefinition, "Right Extrapolation");
  //    final String[] securityTypes = new String[] {CapFloorCMSSpreadSecurity.SECURITY_TYPE, CapFloorSecurity.SECURITY_TYPE, SwapSecurity.SECURITY_TYPE };
  //    for (final String securityType : securityTypes) {
  //      final ValueProperties noExtrapolationProperties = ValueProperties.builder()
  //          .with(CURVE_CALCULATION_CONFIG, curveConfig)
  //          .with(CALCULATION_METHOD, SABRFunction.SABR_NO_EXTRAPOLATION)
  //          .get();
  //      final ValueProperties rightExtrapolationProperties = ValueProperties.builder()
  //          .with(CURVE_CALCULATION_CONFIG, curveConfig)
  //          .with(CALCULATION_METHOD, SABRFunction.SABR_RIGHT_EXTRAPOLATION)
  //          .get();
  //      noExtrapolationConfig.addPortfolioRequirement(securityType, PRESENT_VALUE, noExtrapolationProperties);
  //      noExtrapolationConfig.addPortfolioRequirement(securityType, PRESENT_VALUE_SABR_ALPHA_SENSITIVITY, noExtrapolationProperties);
  //      noExtrapolationConfig.addPortfolioRequirement(securityType, PRESENT_VALUE_SABR_NU_SENSITIVITY, noExtrapolationProperties);
  //      noExtrapolationConfig.addPortfolioRequirement(securityType, PRESENT_VALUE_SABR_RHO_SENSITIVITY, noExtrapolationProperties);
  //      noExtrapolationConfig.addPortfolioRequirement(securityType, YIELD_CURVE_NODE_SENSITIVITIES,
  //          noExtrapolationProperties.copy().with(CURVE, "Discounting").get());
  //      noExtrapolationConfig.addPortfolioRequirement(securityType, YIELD_CURVE_NODE_SENSITIVITIES,
  //          noExtrapolationProperties.copy().with(CURVE, "Forward3M").get());
  //      rightExtrapolationConfig.addPortfolioRequirement(securityType, PRESENT_VALUE, rightExtrapolationProperties);
  //      rightExtrapolationConfig.addPortfolioRequirement(securityType, PRESENT_VALUE_SABR_ALPHA_SENSITIVITY, rightExtrapolationProperties);
  //      rightExtrapolationConfig.addPortfolioRequirement(securityType, PRESENT_VALUE_SABR_NU_SENSITIVITY, rightExtrapolationProperties);
  //      rightExtrapolationConfig.addPortfolioRequirement(securityType, PRESENT_VALUE_SABR_RHO_SENSITIVITY, rightExtrapolationProperties);
  //      rightExtrapolationConfig.addPortfolioRequirement(securityType, YIELD_CURVE_NODE_SENSITIVITIES,
  //          rightExtrapolationProperties.copy().with(CURVE, "Discounting").get());
  //      rightExtrapolationConfig.addPortfolioRequirement(securityType, YIELD_CURVE_NODE_SENSITIVITIES,
  //          rightExtrapolationProperties.copy().with(CURVE, "Forward3M").get());
  //    }
  //    viewDefinition.addViewCalculationConfiguration(noExtrapolationConfig);
  //    viewDefinition.addViewCalculationConfiguration(rightExtrapolationConfig);
  //    return viewDefinition;
  //  }

  /**
   * Creates a view definition for a portfolio containing swaps and swaptions that demonstrates
   * swap / swaption parity using the SABR model with right extrapolation. This view produces:
   * <ul>
   * <li> ValueRequirementNames#PRESENT_VALUE
   * <li> ValueRequirementNames#PRESENT_VALUE_SABR_ALPHA_SENSITIVITY
   * <li> ValueRequirementNames#PRESENT_VALUE_SABR_NU_SENSITIVITY
   * <li> ValueRequirementNames#PRESENT_VALUE_SABR_RHO_SENSITIVITY
   * <li> ValueRequirementNames#YIELD_CURVE_NODE_SENSITIVITIES
   * <li> ValueRequirementNames#BUCKETED_PV01
   * <li> ValueRequirementNames#YIELD_CURVE
   * </ul>
   * @param portfolioName The portfolio name
   * @return The view definition
   */
  //  private ViewDefinition getSyntheticSwaptionParityViewDefinition(final String portfolioName) {
  //    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
  //    final ViewDefinition viewDefinition = new ViewDefinition("Swap / Swaption Parity", portfolioId, UserPrincipal.getTestUser());
  //    viewDefinition.setDefaultCurrency(Currency.USD);
  //    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
  //    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
  //    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
  //    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
  //    final String curveConfig = "DefaultTwoCurveUSDConfig";
  //    final ViewCalculationConfiguration defaultCalConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
  //    viewDefinition.addViewCalculationConfiguration(defaultCalConfig);
  //
  //    final ValueProperties sabrProperties = ValueProperties.builder()
  //        .with(CURVE_CALCULATION_CONFIG, curveConfig)
  //        .with(CALCULATION_METHOD, SABRFunction.SABR_RIGHT_EXTRAPOLATION)
  //        .withOptional(CALCULATION_METHOD)
  //        .get();
  //    final ValueProperties discountingCurveProperties = sabrProperties.copy().with(CURVE, "Discounting").get();
  //    final ValueProperties forwardCurveProperties = sabrProperties.copy().with(CURVE, "Forward3M").get();
  //
  //    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE, sabrProperties);
  //    defaultCalConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, PRESENT_VALUE, sabrProperties);
  //
  //    defaultCalConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, PRESENT_VALUE_SABR_ALPHA_NODE_SENSITIVITY, sabrProperties);
  //    defaultCalConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, PRESENT_VALUE_SABR_RHO_NODE_SENSITIVITY, sabrProperties);
  //    defaultCalConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, PRESENT_VALUE_SABR_NU_NODE_SENSITIVITY, sabrProperties);
  //
  //    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, discountingCurveProperties);
  //    defaultCalConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, forwardCurveProperties);
  //    defaultCalConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, discountingCurveProperties);
  //    defaultCalConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, forwardCurveProperties);
  //    defaultCalConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, BUCKETED_PV01, discountingCurveProperties);
  //    defaultCalConfig.addPortfolioRequirement(SwaptionSecurity.SECURITY_TYPE, BUCKETED_PV01, forwardCurveProperties);
  //
  //    final ComputationTargetSpecification currencyTarget = ComputationTargetSpecification.of(Currency.USD);
  //    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget,
  //        ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
  //    defaultCalConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget,
  //        ValueProperties.with(CURVE, "Forward3M").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig).get()));
  //    return viewDefinition;
  //  }

  /**
   * Creates a view definition for a portfolio of EUR swaps and interest rate futures. The view definition has
   * two column sets, one showing results when a discounting, 3m Euribor and 6m Euribor curve configuration is used
   * that does not contain futures, and the other showing an equivalent configuration with 3m interest rate futures
   * in the 3m Euribor curve.<p>
   * This view produces:
   * <ul>
   * <li> ValueRequirementNames#PRESENT_VALUE
   * <li> ValueRequirementNames#YIELD_CURVE_SENSITIVITIES
   * </ul>
   * @param portfolioName The portfolio name
   * @param viewName The view name
   * @return The view definition
   */
  //  private ViewDefinition getSyntheticEurFixedIncomeViewDefinition(final String portfolioName, final String viewName) {
  //    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
  //    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
  //    viewDefinition.setDefaultCurrency(Currency.EUR);
  //    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
  //    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
  //    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
  //    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
  //    final String curveConfig1 = "EUR-OIS-3M-6M";
  //    final String curveConfig2 = "EUR-OIS-3MFut-6M";
  //    final ViewCalculationConfiguration firstConfig = new ViewCalculationConfiguration(viewDefinition, "EUR-OIS-3M-6M");
  //    final ComputationTargetSpecification currencyTarget = ComputationTargetSpecification.of(Currency.EUR);
  //    firstConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE,
  //        ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig1).get());
  //    firstConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
  //        ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_CONFIG, curveConfig1).get());
  //    firstConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
  //        ValueProperties.with(CURVE, "Forward3M").with(CURVE_CALCULATION_CONFIG, curveConfig1).get());
  //    firstConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
  //        ValueProperties.with(CURVE, "Forward6M").with(CURVE_CALCULATION_CONFIG, curveConfig1).get());
  //    firstConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget,
  //        ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig1).get()));
  //    firstConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget,
  //        ValueProperties.with(CURVE, "Forward3M").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig1).get()));
  //    firstConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget,
  //        ValueProperties.with(CURVE, "Forward6M").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig1).get()));
  //    viewDefinition.addViewCalculationConfiguration(firstConfig);
  //    firstConfig.addPortfolioRequirement(FutureSecurity.SECURITY_TYPE, PRESENT_VALUE,
  //        ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig1).get());
  //    firstConfig.addPortfolioRequirement(FutureSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
  //        ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_CONFIG, curveConfig1).get());
  //    firstConfig.addPortfolioRequirement(FutureSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
  //        ValueProperties.with(CURVE, "Forward3M").with(CURVE_CALCULATION_CONFIG, curveConfig1).get());
  //    firstConfig.addPortfolioRequirement(FutureSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
  //        ValueProperties.with(CURVE, "Forward6M").with(CURVE_CALCULATION_CONFIG, curveConfig1).get());
  //    viewDefinition.addViewCalculationConfiguration(firstConfig);
  //    final ViewCalculationConfiguration secondConfig = new ViewCalculationConfiguration(viewDefinition, "EUR-OIS-3MFut-6M");
  //    secondConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE,
  //        ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig2).get());
  //    secondConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
  //        ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_CONFIG, curveConfig2).get());
  //    secondConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
  //        ValueProperties.with(CURVE, "Forward3MFut").with(CURVE_CALCULATION_CONFIG, curveConfig2).get());
  //    secondConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
  //        ValueProperties.with(CURVE, "Forward6M").with(CURVE_CALCULATION_CONFIG, curveConfig2).get());
  //    secondConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget,
  //        ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig2).get()));
  //    secondConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget,
  //        ValueProperties.with(CURVE, "Forward3MFut").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig2).get()));
  //    secondConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, currencyTarget,
  //        ValueProperties.with(CURVE, "Forward6M").with(CURVE_CALCULATION_METHOD, PAR_RATE_STRING).with(CURVE_CALCULATION_CONFIG, curveConfig2).get()));
  //    viewDefinition.addViewCalculationConfiguration(secondConfig);
  //    secondConfig.addPortfolioRequirement(FutureSecurity.SECURITY_TYPE, PRESENT_VALUE,
  //        ValueProperties.with(CURVE_CALCULATION_CONFIG, curveConfig2).get());
  //    secondConfig.addPortfolioRequirement(FutureSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
  //        ValueProperties.with(CURVE, "Discounting").with(CURVE_CALCULATION_CONFIG, curveConfig2).get());
  //    secondConfig.addPortfolioRequirement(FutureSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
  //        ValueProperties.with(CURVE, "Forward3MFut").with(CURVE_CALCULATION_CONFIG, curveConfig2).get());
  //    secondConfig.addPortfolioRequirement(FutureSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES,
  //        ValueProperties.with(CURVE, "Forward6M").with(CURVE_CALCULATION_CONFIG, curveConfig2).get());
  //    final ViewCalculationConfiguration thirdConfig = new ViewCalculationConfiguration(viewDefinition, "STIR futures MtM");
  //    thirdConfig.addPortfolioRequirement(FutureSecurity.SECURITY_TYPE, PRESENT_VALUE,
  //        ValueProperties.with(CALCULATION_METHOD, "MarkToMarket").get());
  //    viewDefinition.addViewCalculationConfiguration(firstConfig);
  //    viewDefinition.addViewCalculationConfiguration(secondConfig);
  //    viewDefinition.addViewCalculationConfiguration(thirdConfig);
  //    return viewDefinition;
  //  }

  /**
   * Creates a view definition for a portfolio of FX forwards with two column sets showing the results
   * of calculations by discounting with yield curves implied from FX forwards and of using the
   * FX forward quotes directly.<p>
   * This view produces:
   * <ul>
   * <li> ValueRequirementNames#PRESENT_VALUE
   * <li> ValueRequirementNames#FX_CURRENCY_EXPOSURE
   * </ul>
   * @param portfolioName The portfolio name
   * @param viewName The view name
   * @return The view definition
   */
  private ViewDefinition getFxForwardViewDefinition(final String portfolioName, final String viewName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
    final ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    final ValueProperties calcProperties = ValueProperties.builder()
        .with(CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE, CurveCalculationPropertyNamesAndValues.DISCOUNTING)
        .get();
    final Set<Currency> ccysAdded = new HashSet<>();
    for (final UnorderedCurrencyPair pair : CURRENCY_PAIRS) {
      final ValueProperties currencyProperty = ValueProperties.builder().with(CURRENCY, "USD").get();
      calcConfig.addPortfolioRequirement(FXForwardSecurity.SECURITY_TYPE, PRESENT_VALUE, currencyProperty);
      calcConfig.addPortfolioRequirement(FXForwardSecurity.SECURITY_TYPE, FX_CURRENCY_EXPOSURE, ValueProperties.builder().get());
      if (!ccysAdded.contains(pair.getFirstCurrency())) {
        if (pair.getFirstCurrency().equals(Currency.USD)) {
          final ValueProperties curveProperties = ValueProperties.builder().with(CURVE, ExamplesFxImpliedCurveConfigsPopulator.USD_DEPOSIT_CURVE_NAME).get();
          calcConfig.addPortfolioRequirement(FXForwardSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, curveProperties);
          calcConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties));
          ccysAdded.add(pair.getFirstCurrency());
        } else {
          final ValueProperties curveProperties = ValueProperties.builder().with(CURVE,
              ExampleConfigUtils.generateFxImpliedCurveName(pair.getFirstCurrency().getCode())).get();
          calcConfig.addPortfolioRequirement(FXForwardSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, curveProperties);
          calcConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties));
          ccysAdded.add(pair.getFirstCurrency());
        }
      }
      if (!ccysAdded.contains(pair.getSecondCurrency())) {
        if (pair.getSecondCurrency().equals(Currency.USD)) {
          final ValueProperties curveProperties = ValueProperties.builder().with(CURVE, ExamplesFxImpliedCurveConfigsPopulator.USD_DEPOSIT_CURVE_NAME).get();
          calcConfig.addPortfolioRequirement(FXForwardSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, curveProperties);
          calcConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties));
          ccysAdded.add(pair.getFirstCurrency());
        } else {
          final ValueProperties curveProperties = ValueProperties.builder().with(CURVE,
              ExampleConfigUtils.generateFxImpliedCurveName(pair.getFirstCurrency().getCode())).get();
          calcConfig.addPortfolioRequirement(FXForwardSecurity.SECURITY_TYPE, YIELD_CURVE_NODE_SENSITIVITIES, curveProperties);
          calcConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties));
          ccysAdded.add(pair.getFirstCurrency());
        }
      }
    }
    viewDefinition.addViewCalculationConfiguration(calcConfig);
    return viewDefinition;
  }

  /**
   * Creates a view definition for an index future portfolio producing:
   * <ul>
   * <li> ValueRequirementNames#PRESENT_VALUE
   * <li> ValueRequirementNames#PV01
   * <li> ValueRequirementNames#VALUE_DELTA
   * <li> ValueRequirementNames#VALUE_RHO
   * <li> ValueRequirementNames#FORWARD
   * </ul>
   * @param portfolioName The portfolio name
   * @param viewName The view name
   * @return The view definition
   */
  //  private ViewDefinition getSyntheticFutureViewDefinition(final String portfolioName, final String viewName) {
  //    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
  //    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
  //    viewDefinition.setDefaultCurrency(Currency.USD);
  //    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
  //    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
  //    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
  //    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
  //    final ViewCalculationConfiguration defaultCalConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
  //    addValueRequirements(defaultCalConfig, FutureSecurity.SECURITY_TYPE, new String[] {PRESENT_VALUE, PV01, VALUE_DELTA, VALUE_RHO, FORWARD });
  //    viewDefinition.addViewCalculationConfiguration(defaultCalConfig);
  //    return viewDefinition;
  //  }

  /**
   * Creates a view definition for a bond portfolio that showing the results of calculations that
   * use yield curves and the bond yield directly that produces:
   * <ul>
   * <li> ValueRequirementNames#CLEAN_PRICE
   * <li> ValueRequirementNames#MACAULAY_DURATION
   * <li> ValueRequirementNames#MODIFIED_DURATION
   * <li> ValueRequirementNames#PRESENT_VALUE
   * <li> ValueRequirementNames#YTM
   * </ul>
   * @param portfolioName The portfolio name
   * @param viewName The view name
   * @return The view definition
   */
  //  private ViewDefinition getSyntheticBondViewDefinition(final String portfolioName, final String viewName) {
  //    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
  //    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
  //    viewDefinition.setDefaultCurrency(Currency.USD);
  //    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
  //    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
  //    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
  //    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
  //    ViewCalculationConfiguration config = new ViewCalculationConfiguration(viewDefinition, "Curves");
  //    final ValueProperties curvesProperty = ValueProperties.with(CALCULATION_METHOD, BondFunction.FROM_CURVES_METHOD).get();
  //    final ValueProperties yieldsProperty = ValueProperties.with(CALCULATION_METHOD, BondFunction.FROM_YIELD_METHOD).get();
  //    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, CLEAN_PRICE, curvesProperty);
  //    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, MACAULAY_DURATION, curvesProperty);
  //    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, MODIFIED_DURATION, curvesProperty);
  //    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, PRESENT_VALUE, curvesProperty);
  //    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, YTM, curvesProperty);
  //    viewDefinition.addViewCalculationConfiguration(config);
  //    config = new ViewCalculationConfiguration(viewDefinition, "Yields");
  //    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, CLEAN_PRICE, yieldsProperty);
  //    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, MACAULAY_DURATION, yieldsProperty);
  //    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, MODIFIED_DURATION, yieldsProperty);
  //    viewDefinition.addViewCalculationConfiguration(config);
  //    return viewDefinition;
  //  }

  /**
   * Creates a view definition for the OIS portfolio that produces PV, par rate, PV01 and bucketed PV01.
   * @param portfolioName The portfolio name
   * @param viewName The view name
   * @return The view definition
   */
  //  private ViewDefinition getSyntheticOisViewDefinition(final String portfolioName, final String viewName) {
  //    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
  //    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
  //    viewDefinition.setDefaultCurrency(Currency.USD);
  //    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
  //    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
  //    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
  //    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
  //    final ViewCalculationConfiguration defaultCalcConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
  //    defaultCalcConfig.addPortfolioRequirementName(SwapSecurity.SECURITY_TYPE, PAR_RATE);
  //    defaultCalcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE,
  //        ValueProperties.with(CurrencyConversionFunction.ORIGINAL_CURRENCY, "Default").withOptional(CurrencyConversionFunction.ORIGINAL_CURRENCY).get());
  //    defaultCalcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE, ValueProperties.with(CURRENCY, "USD").get());
  //    final MergedOutput discountingPV01Output = new MergedOutput("PV01", MergedOutputAggregationType.LINEAR);
  //    final MergedOutput discountingYCNSOutput = new MergedOutput("Bucketed PV01", MergedOutputAggregationType.LINEAR);
  //    for (int i = 0; i < OIS_CURRENCIES.length; i++) {
  //      final Currency ccy = OIS_CURRENCIES[i];
  //      final String ccyCode = ccy.getCode();
  //      discountingPV01Output.addMergedRequirement(PV01, ValueProperties.with(CURVE, "Discounting").with(CURVE_CURRENCY, ccyCode).get());
  //      discountingYCNSOutput.addMergedRequirement(BUCKETED_PV01, ValueProperties.with(CURVE, "Discounting").with(CURVE_CURRENCY, ccyCode).get());
  //      defaultCalcConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.of(ccy),
  //          ValueProperties.builder().with(CURVE, "Discounting").with(CURVE_CALCULATION_CONFIG, CURVE_CONFIG_NAMES[i]).get()));
  //    }
  //    defaultCalcConfig.addMergedOutput(discountingPV01Output);
  //    defaultCalcConfig.addMergedOutput(discountingYCNSOutput);
  //    viewDefinition.addViewCalculationConfiguration(defaultCalcConfig);
  //    return viewDefinition;
  //  }

  /**
   * Creates a bond view that calculates clean price, modified and Macaulay durations using
   * both the clean price and the market yield quote, and present value and yield to maturity from
   * the clean price.
   * @param portfolioName The name of the portfolio
   * @param viewName The name of the view
   * @return The view definition
   */
  //  private ViewDefinition getSyntheticMultiCountryBondViewDefinition(final String portfolioName, final String viewName) {
  //    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
  //    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
  //    viewDefinition.setDefaultCurrency(Currency.USD);
  //    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
  //    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
  //    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
  //    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
  //    ViewCalculationConfiguration config = new ViewCalculationConfiguration(viewDefinition, "Bond Curves");
  //    ValueProperties properties = ValueProperties.builder()
  //        .with(PROPERTY_CURVE_TYPE, "Discounting")
  //        .with(CURVE_EXPOSURES, "Bond Exposures")
  //        .with(CALCULATION_METHOD, "Curves")
  //        .get();
  //    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, PRESENT_VALUE, properties);
  //    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, CLEAN_PRICE, properties);
  //    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, CONVEXITY, properties);
  //    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, BOND_DETAILS, properties);
  //    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, MACAULAY_DURATION, properties);
  //    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, MODIFIED_DURATION, properties);
  //    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, YTM, properties);
  //    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, GAMMA_PV01, properties);
  //    final String[] curveNames = new String[] {"USD Discounting", "US Government Bond" };
  //    for (final String curveName : curveNames) {
  //      final ValueProperties curveProperties = properties.copy()
  //          .with(CURVE, curveName)
  //          .get();
  //      config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, PV01, curveProperties);
  //      config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, BUCKETED_PV01, curveProperties);
  //    }
  //    final ValueProperties thetaProperties = properties.copy()
  //        .with(PROPERTY_DAYS_TO_MOVE_FORWARD, "1")
  //        .with(PROPERTY_THETA_CALCULATION_METHOD, THETA_CONSTANT_SPREAD)
  //        .get();
  //    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, VALUE_THETA, thetaProperties);
  //    final ValueProperties zSpreadProperties = ValueProperties.builder()
  //        .with(PROPERTY_CURVE_TYPE, "Discounting")
  //        .with(CURVE_EXPOSURES, "Bond Exposures")
  //        .with(CALCULATION_METHOD, "Yield")
  //        .with(CURVE, "US Government Bond")
  //        .get();
  //    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, Z_SPREAD, zSpreadProperties);
  //    ValueProperties curveProperties = ValueProperties.builder()
  //        .with(PROPERTY_CURVE_TYPE, "Discounting")
  //        .with(CURVE, "US Government Bond")
  //        .with(CURVE_CONSTRUCTION_CONFIG, "US Government Bond Configuration")
  //        .get();
  //    config.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties));
  //    viewDefinition.addViewCalculationConfiguration(config);
  //    config = new ViewCalculationConfiguration(viewDefinition, "OIS Curves");
  //    properties = ValueProperties.builder()
  //        .with(PROPERTY_CURVE_TYPE, "Discounting")
  //        .with(CURVE_EXPOSURES, "Bond OIS Exposures")
  //        .with(CALCULATION_METHOD, "Curves")
  //        .get();
  //    curveProperties = ValueProperties.builder()
  //        .with(PROPERTY_CURVE_TYPE, "Discounting")
  //        .with(CURVE_CONSTRUCTION_CONFIG, "Default USD Curves")
  //        .with(CURVE, "USD Discounting")
  //        .get();
  //    config.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, PRESENT_VALUE, properties);
  //    config.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties));
  //    viewDefinition.addViewCalculationConfiguration(config);
  //    return viewDefinition;
  //  }


  /**
   * Adds a list of value requirement names to a calculation configuration for a particular security type.
   * @param calcConfiguration The calculation configuration
   * @param securityType The security type
   * @param valueRequirementNames The value requirement names to add
   */
  private static void addValueRequirements(final ViewCalculationConfiguration calcConfiguration, final String securityType,
      final String[] valueRequirementNames) {
    for (final String valueRequirementName : valueRequirementNames) {
      calcConfiguration.addPortfolioRequirementName(securityType, valueRequirementName);
    }
  }

  /**
   * Gets the id for a portfolio name.
   * @param portfolioName The portfolio name
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
   * @param viewDefinition The view definition
   */
  private void storeViewDefinition(final ViewDefinition viewDefinition) {
    final ConfigItem<ViewDefinition> config = ConfigItem.of(viewDefinition, viewDefinition.getName(), ViewDefinition.class);
    ConfigMasterUtils.storeByName(getToolContext().getConfigMaster(), config);
  }

}
