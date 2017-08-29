/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.examples.simulated.loader.config;

import static com.mcleodmoores.examples.simulated.loader.ExamplesDatabasePopulator.AUD_SWAP_PORFOLIO_NAME;
import static com.mcleodmoores.examples.simulated.loader.ExamplesDatabasePopulator.FUTURE_PORTFOLIO_NAME;
import static com.mcleodmoores.examples.simulated.loader.ExamplesDatabasePopulator.FX_FORWARD_PORTFOLIO_NAME;
import static com.mcleodmoores.examples.simulated.loader.ExamplesDatabasePopulator.MULTI_CURRENCY_SWAP_PORTFOLIO_NAME;
import static com.mcleodmoores.examples.simulated.loader.ExamplesDatabasePopulator.OIS_PORTFOLIO_NAME;
import static com.mcleodmoores.examples.simulated.loader.ExamplesDatabasePopulator.USD_TREASURIES_PORTFOLIO_NAME;
import static com.mcleodmoores.examples.simulated.loader.ExamplesDatabasePopulator.VANILLA_FX_OPTION_PORTFOLIO_NAME;
import static com.opengamma.engine.value.ValuePropertyNames.CALCULATION_METHOD;
import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValuePropertyNames.SURFACE;
import static com.opengamma.engine.value.ValueRequirementNames.BOND_DETAILS;
import static com.opengamma.engine.value.ValueRequirementNames.BUCKETED_PV01;
import static com.opengamma.engine.value.ValueRequirementNames.CAPM_BETA;
import static com.opengamma.engine.value.ValueRequirementNames.FAIR_VALUE;
import static com.opengamma.engine.value.ValueRequirementNames.FIXED_CASH_FLOWS;
import static com.opengamma.engine.value.ValueRequirementNames.FLOATING_CASH_FLOWS;
import static com.opengamma.engine.value.ValueRequirementNames.FORWARD;
import static com.opengamma.engine.value.ValueRequirementNames.FORWARD_DELTA;
import static com.opengamma.engine.value.ValueRequirementNames.FORWARD_DRIFTLESS_THETA;
import static com.opengamma.engine.value.ValueRequirementNames.FORWARD_GAMMA;
import static com.opengamma.engine.value.ValueRequirementNames.FORWARD_VEGA;
import static com.opengamma.engine.value.ValueRequirementNames.FX_CURRENCY_EXPOSURE;
import static com.opengamma.engine.value.ValueRequirementNames.FX_FORWARD_DETAILS;
import static com.opengamma.engine.value.ValueRequirementNames.FX_PRESENT_VALUE;
import static com.opengamma.engine.value.ValueRequirementNames.HISTORICAL_VAR;
import static com.opengamma.engine.value.ValueRequirementNames.MACAULAY_DURATION;
import static com.opengamma.engine.value.ValueRequirementNames.MODIFIED_DURATION;
import static com.opengamma.engine.value.ValueRequirementNames.NOTIONAL;
import static com.opengamma.engine.value.ValueRequirementNames.PAR_RATE;
import static com.opengamma.engine.value.ValueRequirementNames.PNL;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE;
import static com.opengamma.engine.value.ValueRequirementNames.PV01;
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
import static com.opengamma.engine.value.ValueRequirementNames.YTM;
import static com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues.CURVES_METHOD;
import static com.opengamma.financial.analytics.model.InstrumentTypeProperties.FOREX;
import static com.opengamma.financial.analytics.model.InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.DISCOUNTING;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.NELSON_SIEGEL;
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
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.examples.simulated.loader.ExampleEquityPortfolioLoader;
import com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.discounting.DiscountingYCNSFunction;
import com.opengamma.financial.currency.CurrencyConversionFunction;
import com.opengamma.financial.security.bond.BillSecurity;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.future.FutureSecurity;
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
  /** A list of currency pairs. */
  public static final UnorderedCurrencyPair[] CURRENCY_PAIRS = new UnorderedCurrencyPair[] {
      UnorderedCurrencyPair.of(Currency.USD, Currency.EUR),
      UnorderedCurrencyPair.of(Currency.USD, Currency.CHF),
      UnorderedCurrencyPair.of(Currency.USD, Currency.AUD),
      UnorderedCurrencyPair.of(Currency.USD, Currency.GBP),
      UnorderedCurrencyPair.of(Currency.GBP, Currency.EUR)};
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
    storeViewDefinition(getSwapDetailsViewDefinition(MULTI_CURRENCY_SWAP_PORTFOLIO_NAME, "Swap Details View"));
    storeViewDefinition(getSwapViewDefinition(MULTI_CURRENCY_SWAP_PORTFOLIO_NAME, "Swap View"));
    storeViewDefinition(getOisViewDefinition(OIS_PORTFOLIO_NAME, "OIS View"));
    storeViewDefinition(getAudSwapViewDefinition(AUD_SWAP_PORFOLIO_NAME));
    storeViewDefinition(getFxOptionViewDefinition(VANILLA_FX_OPTION_PORTFOLIO_NAME, "FX Option View"));
    storeViewDefinition(getFxOptionGreeksViewDefinition(VANILLA_FX_OPTION_PORTFOLIO_NAME, "FX Option Greeks View"));
    storeViewDefinition(getFxForwardViewDefinition(FX_FORWARD_PORTFOLIO_NAME, "FX Forward View"));
    storeViewDefinition(getFxForwardDetailsViewDefinition(FX_FORWARD_PORTFOLIO_NAME, "FX Forward Details View"));
    storeViewDefinition(getFutureViewDefinition(FUTURE_PORTFOLIO_NAME, "Futures View"));
    storeViewDefinition(getUsTreasuriesViewDefinition(USD_TREASURIES_PORTFOLIO_NAME, "US Treasuries View"));
  }

  /**
   * Creates a view definition for a portfolio containing only equities that produces:
   * <ul>
   * <li> {@link ValueRequirementNames#FAIR_VALUE}
   * <li> {@link ValueRequirementNames#CAPM_BETA}
   * <li> {@link ValueRequirementNames#HISTORICAL_VAR}
   * <li> {@link ValueRequirementNames#SHARPE_RATIO}
   * <li> {@link ValueRequirementNames#PNL}
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

  /**
   * Creates a view definition for a swap portfolio that produces:
   * <ul>
   * <li> {@link ValueRequirementNames#NOTIONAL}
   * <li> {@link ValueRequirementNames#FIXED_RATE}
   * <li> {@link ValueRequirementNames#SWAP_DETAILS}
   * </ul>
   * The curve-specific risk outputs are not collapsed into a single column.
   * @param portfolioName  the portfolio name
   * @return  the view definition
   */
  private ViewDefinition getSwapDetailsViewDefinition(final String portfolioName, final String viewName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
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
    calcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE,
        calcProperties.compose(ValueProperties.with(CurrencyConversionFunction.ORIGINAL_CURRENCY, "Default")
            .withOptional(CurrencyConversionFunction.ORIGINAL_CURRENCY).get()));
    calcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE,
        calcProperties.compose(ValueProperties.with(CURRENCY, "USD").get()));
    calcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, FIXED_CASH_FLOWS, calcProperties);
    calcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, FLOATING_CASH_FLOWS, calcProperties);
    viewDefinition.addViewCalculationConfiguration(calcConfig);
    return viewDefinition;
  }

  /**
   * Creates a view definition for a swap portfolio that produces:
   * <ul>
   *  <li> {@link ValueRequirementNames#PRESENT_VALUE}
   *  <li> {@link ValueRequirementNames#PV01}
   *  <li> {@link ValueRequirementNames#BUCKETED_PV01}
   * <ul>
   */
  private ViewDefinition getSwapViewDefinition(final String portfolioName, final String viewName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
    final ValueProperties calcProperties =  ValueProperties.builder()
        .with(CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE, CurveCalculationPropertyNamesAndValues.DISCOUNTING)
        .get();
    final ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    calcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE, calcProperties.copy().with(CURRENCY, "USD").get());
    calcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PAR_RATE, calcProperties.copy().get());
    calcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, BUCKETED_PV01,
        calcProperties.copy().with(CURVE, "USD Discounting").with(CURRENCY, "USD").with(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY, "USD").get());
    calcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, BUCKETED_PV01,
        calcProperties.copy().with(CURVE, "USD 3M LIBOR").with(CURRENCY, "USD").with(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY, "USD").get());
    calcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, BUCKETED_PV01,
        calcProperties.copy().with(CURVE, "EUR Discounting").with(CURRENCY, "USD").with(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY, "EUR").get());
    calcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, BUCKETED_PV01,
        calcProperties.copy().with(CURVE, "EUR 6M EURIBOR").with(CURRENCY, "USD").with(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY, "EUR").get());
    calcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, BUCKETED_PV01,
        calcProperties.copy().with(CURVE, "CHF Discounting").with(CURRENCY, "USD").with(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY, "CHF").get());
    calcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, BUCKETED_PV01,
        calcProperties.copy().with(CURVE, "CHF 6M LIBOR").with(CURRENCY, "USD").with(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY, "CHF").get());
    calcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, BUCKETED_PV01,
        calcProperties.copy().with(CURVE, "GBP Discounting").with(CURRENCY, "USD").with(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY, "GBP").get());
    calcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, BUCKETED_PV01,
        calcProperties.copy().with(CURVE, "GBP 6M LIBOR").with(CURRENCY, "USD").with(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY, "GBP").get());
    calcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, BUCKETED_PV01,
        calcProperties.copy().with(CURVE, "JPY Discounting").with(CURRENCY, "USD").with(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY, "JPY").get());
    calcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, BUCKETED_PV01,
        calcProperties.copy().with(CURVE, "JPY 6M TIBOR").with(CURRENCY, "USD").with(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY, "JPY").get());
    viewDefinition.addViewCalculationConfiguration(calcConfig);
    return viewDefinition;
  }

  /**
   * Creates a view definition for an OIS portfolio that produces:
   * <ul>
   *  <li> {@link ValueRequirementNames#PRESENT_VALUE}
   *  <li> {@link ValueRequirementNames#PAR_RATE}
   *  <li> {@link ValueRequirementNames#BUCKETED_PV01}
   * <ul>
   */
  private ViewDefinition getOisViewDefinition(final String portfolioName, final String viewName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
    final ValueProperties calcProperties =  ValueProperties.builder()
        .with(CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE, CurveCalculationPropertyNamesAndValues.DISCOUNTING)
        .get();
    final ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    calcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE, calcProperties.copy().with(CURRENCY, "USD").get());
    calcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PV01,
        calcProperties.copy().with(CURVE, "USD Discounting").with(CURRENCY, "USD").with(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY, "USD").get());
    calcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, BUCKETED_PV01,
        calcProperties.copy().with(CURVE, "USD Discounting").with(CURRENCY, "USD").with(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY, "USD").get());
    calcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PV01,
        calcProperties.copy().with(CURVE, "EUR Discounting").with(CURRENCY, "USD").with(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY, "EUR").get());
    calcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, BUCKETED_PV01,
        calcProperties.copy().with(CURVE, "EUR Discounting").with(CURRENCY, "USD").with(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY, "EUR").get());
    calcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PV01,
        calcProperties.copy().with(CURVE, "CHF Discounting").with(CURRENCY, "USD").with(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY, "CHF").get());
    calcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, BUCKETED_PV01,
        calcProperties.copy().with(CURVE, "CHF Discounting").with(CURRENCY, "USD").with(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY, "CHF").get());
    calcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PV01,
        calcProperties.copy().with(CURVE, "GBP Discounting").with(CURRENCY, "USD").with(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY, "GBP").get());
    calcConfig.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, BUCKETED_PV01,
        calcProperties.copy().with(CURVE, "GBP Discounting").with(CURRENCY, "USD").with(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY, "GBP").get());
    viewDefinition.addViewCalculationConfiguration(calcConfig);
    return viewDefinition;
  }

  /**
   * Creates a view definition for an FX option portfolio that produces:
   * <ul>
   * <li> {@link ValueRequirementNames#VOLATILITY_SURFACE_DATA}
   * <li> {@link ValueRequirementNames#VEGA_QUOTE_MATRIX}
   * <li> {@link ValueRequirementNames#VEGA_MATRIX}
   * <li> {@link ValueRequirementNames#YIELD_CURVE_NODE_SENSITIVITIES}
   * <li> {@link ValueRequirementNames#PRESENT_VALUE}
   * <li> {@link ValueRequirementNames#FX_CURRENCY_EXPOSURE}
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
          calcConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, BUCKETED_PV01,
              curveProperties.copy().with(CURRENCY, "USD").with(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY, "USD").get());
          calcConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties));
          ccysAdded.add(pair.getFirstCurrency());
        } else {
          final ValueProperties curveProperties = ValueProperties.builder().with(CURVE,
              ExampleConfigUtils.generateFxImpliedCurveName(pair.getFirstCurrency().getCode())).get();
          calcConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, BUCKETED_PV01,
              curveProperties.copy().with(CURRENCY, "USD").with(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY,
                  pair.getFirstCurrency().getCode()).get());
          calcConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties));
          ccysAdded.add(pair.getFirstCurrency());
        }
      }
      if (!ccysAdded.contains(pair.getSecondCurrency())) {
        if (pair.getSecondCurrency().equals(Currency.USD)) {
          final ValueProperties curveProperties = ValueProperties.builder().with(CURVE, ExamplesFxImpliedCurveConfigsPopulator.USD_DEPOSIT_CURVE_NAME).get();
          calcConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, BUCKETED_PV01,
              curveProperties.copy().with(CURRENCY, "USD").with(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY, "USD").get());
          calcConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties));
          ccysAdded.add(pair.getFirstCurrency());
        } else {
          final ValueProperties curveProperties = ValueProperties.builder().with(CURVE,
              ExampleConfigUtils.generateFxImpliedCurveName(pair.getFirstCurrency().getCode())).get();
          calcConfig.addPortfolioRequirement(FXOptionSecurity.SECURITY_TYPE, BUCKETED_PV01,
              curveProperties.copy().with(CURRENCY, "USD").with(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY,
                  pair.getFirstCurrency().getCode()).get());
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
   * <li> {@link ValueRequirementNames#PRESENT_VALUE}
   * <li> {@link ValueRequirementNames#VALUE_DELTA}
   * <li> {@link ValueRequirementNames#VALUE_VEGA}
   * <li> {@link ValueRequirementNames#VALUE_GAMMA_P}
   * <li> {@link ValueRequirementNames#VALUE_RHO}
   * <li> {@link ValueRequirementNames#VALUE_PHI}
   * <li> {@link ValueRequirementNames#VALUE_VOMMA}
   * <li> {@link ValueRequirementNames#VALUE_VANNA}
   * <li> {@link ValueRequirementNames#VALUE_THETA}
   * <li> {@link ValueRequirementNames#SECURITY_IMPLIED_VOLATILITY}
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
   * Creates a view definition for a portfolio of AUD swaps where the curve configuration generates the
   * three yield curves (discounting, 3m forward and 6m forward) simultaneously. This view produces:
   * <ul>
   * <li> {@link ValueRequirementNames#PRESENT_VALUE}
   * <li> {@link ValueRequirementNames#YIELD_CURVE_NODE_SENSITIVITIES}
   * <li> {@link ValueRequirementNames#YIELD_CURVE}
   * </ul>
   * @param portfolioName The portfolio name
   * @return The view definition
   */
  private ViewDefinition getAudSwapViewDefinition(final String portfolioName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition("AUD Swap View", portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.AUD);
    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
    final ValueProperties.Builder properties1 = ValueProperties.builder()
        .with(PROPERTY_CURVE_TYPE, DISCOUNTING)
        .with(CURVE_EXPOSURES, "AUD Swaps (1)");
    final ViewCalculationConfiguration viewConfig1 = new ViewCalculationConfiguration(viewDefinition, "Simultaneous curve construction");
    viewConfig1.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE, properties1.get());
    viewConfig1.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, BUCKETED_PV01,
        properties1.copy().with(CURVE, "AUD Discounting").get());
    viewConfig1.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, BUCKETED_PV01,
        properties1.copy().with(CURVE, "AUD 3M BANK BILL").get());
    viewConfig1.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, BUCKETED_PV01,
        properties1.copy().with(CURVE, "AUD 6M BANK BILL").get());
    viewConfig1.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL,
        ValueProperties.with(PROPERTY_CURVE_TYPE, DISCOUNTING)
        .with(CURVE_CONSTRUCTION_CONFIG, "AUD Bank Bill Curves (1)").with(CURVE, "AUD Discounting").get()));
    viewConfig1.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL,
        ValueProperties.with(PROPERTY_CURVE_TYPE, DISCOUNTING)
        .with(CURVE_CONSTRUCTION_CONFIG, "AUD Bank Bill Curves (1)").with(CURVE, "AUD 3M BANK BILL").get()));
    viewConfig1.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL,
        ValueProperties.with(PROPERTY_CURVE_TYPE, DISCOUNTING)
        .with(CURVE_CONSTRUCTION_CONFIG, "AUD Bank Bill Curves (1)").with(CURVE, "AUD 6M BANK BILL").get()));
    viewDefinition.addViewCalculationConfiguration(viewConfig1);
    final ValueProperties.Builder properties2 = ValueProperties.builder()
        .with(PROPERTY_CURVE_TYPE, DISCOUNTING)
        .with(CURVE_EXPOSURES, "AUD Swaps (2)");
    final ViewCalculationConfiguration viewConfig2 = new ViewCalculationConfiguration(viewDefinition, "Successive curve construction");
    viewConfig2.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, PRESENT_VALUE, properties1.get());
    viewConfig2.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, BUCKETED_PV01,
        properties2.copy().with(CURVE, "AUD Discounting").get());
    viewConfig2.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, BUCKETED_PV01,
        properties2.copy().with(CURVE, "AUD 3M BANK BILL").get());
    viewConfig2.addPortfolioRequirement(SwapSecurity.SECURITY_TYPE, BUCKETED_PV01,
        properties2.copy().with(CURVE, "AUD 6M BANK BILL").get());
    viewConfig2.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL,
        ValueProperties.with(PROPERTY_CURVE_TYPE, DISCOUNTING)
        .with(CURVE_CONSTRUCTION_CONFIG, "AUD Bank Bill Curves (2)").with(CURVE, "AUD Discounting").get()));
    viewConfig2.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL,
        ValueProperties.with(PROPERTY_CURVE_TYPE, DISCOUNTING)
        .with(CURVE_CONSTRUCTION_CONFIG, "AUD Bank Bill Curves (2)").with(CURVE, "AUD 3M BANK BILL").get()));
    viewConfig2.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL,
        ValueProperties.with(PROPERTY_CURVE_TYPE, DISCOUNTING)
        .with(CURVE_CONSTRUCTION_CONFIG, "AUD Bank Bill Curves (2)").with(CURVE, "AUD 6M BANK BILL").get()));
    viewDefinition.addViewCalculationConfiguration(viewConfig2);
    return viewDefinition;
  }

  /**
   * Creates a view definition for a portfolio of FX forwards with two column sets showing the results
   * of calculations by discounting with yield curves implied from FX forwards and of using the
   * FX forward quotes directly.<p>
   * This view produces:
   * <ul>
   * <li> {@link ValueRequirementNames#PRESENT_VALUE}
   * <li> {@link ValueRequirementNames#FX_CURRENCY_EXPOSURE}
   * <li> {@link ValueRequirementNames#YIELD_CURVE_NODE_SENSITIVITIES}
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
    final Set<Currency> ccysAdded = new HashSet<>();
    for (final UnorderedCurrencyPair pair : CURRENCY_PAIRS) {
      final ValueProperties currencyProperty = ValueProperties.builder().with(CURRENCY, "USD").get();
      calcConfig.addPortfolioRequirement(FXForwardSecurity.SECURITY_TYPE, PRESENT_VALUE, currencyProperty.copy()
          .with(CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE, DISCOUNTING).get());
      calcConfig.addPortfolioRequirement(FXForwardSecurity.SECURITY_TYPE, FX_CURRENCY_EXPOSURE,
          ValueProperties.with(CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE, DISCOUNTING).get());
      final String ccy = pair.getFirstCurrency().getCode();
      if (!ccysAdded.contains(pair.getFirstCurrency())) {
        if (pair.getFirstCurrency().equals(Currency.USD)) {
          final ValueProperties curveProperties = ValueProperties.builder().with(CURVE, ExamplesFxImpliedCurveConfigsPopulator.USD_DEPOSIT_CURVE_NAME)
              .with(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY, "USD").with(CURRENCY, "USD").get();
          calcConfig.addPortfolioRequirement(FXForwardSecurity.SECURITY_TYPE, BUCKETED_PV01, curveProperties);
          calcConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties));
          ccysAdded.add(pair.getFirstCurrency());
        } else {
          final ValueProperties curveProperties = ValueProperties.builder().with(CURVE,
              ExampleConfigUtils.generateFxImpliedCurveName(ccy)).get();
          calcConfig.addPortfolioRequirement(FXForwardSecurity.SECURITY_TYPE, BUCKETED_PV01, curveProperties
              .copy().with(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY, ccy).with(CURRENCY, "USD").get());
          calcConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties));
          ccysAdded.add(pair.getFirstCurrency());
        }
      }
      if (!ccysAdded.contains(pair.getSecondCurrency())) {
        if (pair.getSecondCurrency().equals(Currency.USD)) {
          final ValueProperties curveProperties = ValueProperties.builder().with(CURVE, ExamplesFxImpliedCurveConfigsPopulator.USD_DEPOSIT_CURVE_NAME)
              .with(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY, "USD").with(CURRENCY, "USD").get();
          calcConfig.addPortfolioRequirement(FXForwardSecurity.SECURITY_TYPE, BUCKETED_PV01, curveProperties);
          calcConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties));
          ccysAdded.add(pair.getFirstCurrency());
        } else {
          final ValueProperties curveProperties = ValueProperties.builder().with(CURVE,
              ExampleConfigUtils.generateFxImpliedCurveName(ccy)).get();
          calcConfig.addPortfolioRequirement(FXForwardSecurity.SECURITY_TYPE, BUCKETED_PV01, curveProperties
              .copy().with(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY, ccy).with(CURRENCY, "USD").get());
          calcConfig.addSpecificRequirement(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties));
          ccysAdded.add(pair.getFirstCurrency());
        }
      }
    }
    viewDefinition.addViewCalculationConfiguration(calcConfig);
    return viewDefinition;
  }

  /**
   * Creates a view definition for a portfolio of FX forwards with two column sets showing the results
   * of calculations by discounting with yield curves implied from FX forwards and of using the
   * FX forward quotes directly.<p>
   * This view produces:
   * <ul>
   * <li> {@link ValueRequirementNames#PRESENT_VALUE}
   * <li> {@link ValueRequirementNames#FX_CURRENCY_EXPOSURE}
   * <li> {@link ValueRequirementNames#YIELD_CURVE_NODE_SENSITIVITIES}
   * </ul>
   * @param portfolioName The portfolio name
   * @param viewName The view name
   * @return The view definition
   */
  private ViewDefinition getFxForwardDetailsViewDefinition(final String portfolioName, final String viewName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
    final ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    final ValueProperties calculationMethodProperty = ValueProperties.builder()
        .with(CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE, DISCOUNTING)
        .get();
    calcConfig.addPortfolioRequirement(FXForwardSecurity.SECURITY_TYPE, FX_PRESENT_VALUE, calculationMethodProperty);
    calcConfig.addPortfolioRequirement(FXForwardSecurity.SECURITY_TYPE, PRESENT_VALUE, calculationMethodProperty.copy().with(CURRENCY, "USD").get());
    calcConfig.addPortfolioRequirement(FXForwardSecurity.SECURITY_TYPE, FX_FORWARD_DETAILS, calculationMethodProperty);
    viewDefinition.addViewCalculationConfiguration(calcConfig);
    return viewDefinition;
  }

  /**
   * Creates a view definition for an index future portfolio producing:
   * <ul>
   * <li> {@link ValueRequirementNames#PRESENT_VALUE}
   * <li> {@link ValueRequirementNames#VALUE_DELTA}
   * <li> {@link ValueRequirementNames#VALUE_RHO}
   * </ul>
   * @param portfolioName The portfolio name
   * @param viewName The view name
   * @return The view definition
   */
  private ViewDefinition getFutureViewDefinition(final String portfolioName, final String viewName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
    final ViewCalculationConfiguration defaultCalConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    addValueRequirements(defaultCalConfig, FutureSecurity.SECURITY_TYPE, new String[] {PRESENT_VALUE, VALUE_DELTA, FORWARD });
    viewDefinition.addViewCalculationConfiguration(defaultCalConfig);
    return viewDefinition;
  }

  private ViewDefinition getUsTreasuriesViewDefinition(final String portfolioName, final String viewName) {
    final UniqueId portfolioId = getPortfolioId(portfolioName).toLatest();
    final ViewDefinition viewDefinition = new ViewDefinition(viewName, portfolioId, UserPrincipal.getTestUser());
    viewDefinition.setDefaultCurrency(Currency.USD);
    viewDefinition.setMaxDeltaCalculationPeriod(MAX_DELTA_PERIOD);
    viewDefinition.setMaxFullCalculationPeriod(MAX_FULL_PERIOD);
    viewDefinition.setMinDeltaCalculationPeriod(MIN_DELTA_PERIOD);
    viewDefinition.setMinFullCalculationPeriod(MIN_FULL_PERIOD);
    final ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDefinition, DEFAULT_CALC_CONFIG);
    final ValueProperties properties1 = ValueProperties.builder()
        .with(PROPERTY_CURVE_TYPE, DISCOUNTING)
        .with(CALCULATION_METHOD, CURVES_METHOD)
        .get();
    final ValueProperties properties2 = ValueProperties.builder()
        .with(PROPERTY_CURVE_TYPE, NELSON_SIEGEL)
        .with(CALCULATION_METHOD, CURVES_METHOD)
        .get();
    calcConfig.addSpecificRequirement(
        new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL,
            ValueProperties.with(PROPERTY_CURVE_TYPE, DISCOUNTING).with(CURVE_CONSTRUCTION_CONFIG, "US Treasury").get()));
    calcConfig.addSpecificRequirement(
        new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL,
            ValueProperties.with(PROPERTY_CURVE_TYPE, NELSON_SIEGEL).with(CURVE_CONSTRUCTION_CONFIG, "US Treasury").get()));
    calcConfig.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, PRESENT_VALUE, properties1.copy().with(CURRENCY, "USD").get());
    calcConfig.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, PRESENT_VALUE, properties2.copy().with(CURRENCY, "USD").get());
    calcConfig.addPortfolioRequirement(BillSecurity.SECURITY_TYPE, PRESENT_VALUE, properties1.copy().with(CURRENCY, "USD").get());
    calcConfig.addPortfolioRequirement(BillSecurity.SECURITY_TYPE, PRESENT_VALUE, properties2.copy().with(CURRENCY, "USD").get());
    calcConfig.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, YTM, properties1);
    calcConfig.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, YTM, properties2);
    calcConfig.addPortfolioRequirement(BillSecurity.SECURITY_TYPE, YTM, properties1);
    calcConfig.addPortfolioRequirement(BillSecurity.SECURITY_TYPE, YTM, properties2);
    calcConfig.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, MODIFIED_DURATION, properties1);
    calcConfig.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, MODIFIED_DURATION, properties2);
    calcConfig.addPortfolioRequirement(BillSecurity.SECURITY_TYPE, MODIFIED_DURATION, properties1);
    calcConfig.addPortfolioRequirement(BillSecurity.SECURITY_TYPE, MODIFIED_DURATION, properties2);
    calcConfig.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, MACAULAY_DURATION, properties1);
    calcConfig.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, MACAULAY_DURATION, properties2);
    calcConfig.addPortfolioRequirement(BillSecurity.SECURITY_TYPE, MACAULAY_DURATION, properties1);
    calcConfig.addPortfolioRequirement(BillSecurity.SECURITY_TYPE, MACAULAY_DURATION, properties2);
    calcConfig.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, BOND_DETAILS, properties1);
    calcConfig.addPortfolioRequirement(BondSecurity.SECURITY_TYPE, BOND_DETAILS, properties2);
    calcConfig.addPortfolioRequirement(BillSecurity.SECURITY_TYPE, BOND_DETAILS, properties1);
    calcConfig.addPortfolioRequirement(BillSecurity.SECURITY_TYPE, BOND_DETAILS, properties2);
    viewDefinition.addViewCalculationConfiguration(calcConfig);
    return viewDefinition;
  }

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
