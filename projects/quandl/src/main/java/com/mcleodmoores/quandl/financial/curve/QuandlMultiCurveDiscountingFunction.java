/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.financial.curve;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_METHOD;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.JACOBIAN_BUNDLE;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.ROOT_FINDING;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.core.DateSet;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.CurveNodeConverter;
import com.opengamma.financial.analytics.curve.CalendarSwapNodeConverter;
import com.opengamma.financial.analytics.curve.CashNodeConverter;
import com.opengamma.financial.analytics.curve.ConfigDBCurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveNodeVisitorAdapter;
import com.opengamma.financial.analytics.curve.CurveUtils;
import com.opengamma.financial.analytics.curve.FRANodeConverter;
import com.opengamma.financial.analytics.curve.FXForwardNodeConverter;
import com.opengamma.financial.analytics.curve.RollDateFRANodeConverter;
import com.opengamma.financial.analytics.curve.RollDateSwapNodeConverter;
import com.opengamma.financial.analytics.curve.SwapNodeConverter;
import com.opengamma.financial.analytics.curve.ThreeLegBasisSwapNodeConverter;
import com.opengamma.financial.analytics.curve.credit.ConfigDBCurveDefinitionSource;
import com.opengamma.financial.analytics.curve.credit.CurveDefinitionSource;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.analytics.model.curve.MultiCurveDiscountingFunction;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.config.ConfigSourceQuery;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;

/**
 * A function that constructs a bundle of curves using
 * {@link com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator} and
 * {@link com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator}
 * that can use Quandl data.
 */
public class QuandlMultiCurveDiscountingFunction extends MultiCurveDiscountingFunction {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(QuandlMultiCurveDiscountingFunction.class);
  /** The configuration name */
  private final String _configurationName;
  /** A curve construction configuration source */
  private CurveConstructionConfigurationSource _curveConstructionConfigurationSource;
  /** A curve definition source */
  private CurveDefinitionSource _curveDefinitionSource;

  /**
   * @param configurationName The configuration name, not null
   */
  public QuandlMultiCurveDiscountingFunction(final String configurationName) {
    super(configurationName);
    _configurationName = configurationName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _curveConstructionConfigurationSource = ConfigDBCurveConstructionConfigurationSource.init(context, this);
    _curveDefinitionSource = ConfigDBCurveDefinitionSource.init(context, this);
  }

  @Override
  public CompiledFunctionDefinition getCompiledFunction(final ZonedDateTime earliestInvocation, final ZonedDateTime latestInvocation, final String[] curveNames,
      final Set<ValueRequirement> exogenousRequirements, final CurveConstructionConfiguration curveConstructionConfiguration) {
    return new QuandlCompiledFunctionDefinition(earliestInvocation, latestInvocation, curveNames, exogenousRequirements, curveConstructionConfiguration, null);
  }

  @Override
  public CompiledFunctionDefinition getCompiledFunction(final ZonedDateTime earliestInvocation, final ZonedDateTime latestInvocation, final String[] curveNames,
      final Set<ValueRequirement> exogenousRequirements, final CurveConstructionConfiguration curveConstructionConfiguration, final String[] currencies) {
    return new QuandlCompiledFunctionDefinition(earliestInvocation, latestInvocation, curveNames, exogenousRequirements, curveConstructionConfiguration,
        currencies);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    final CurveConstructionConfiguration curveConstructionConfiguration =
        _curveConstructionConfigurationSource.getCurveConstructionConfiguration(_configurationName);
    if (curveConstructionConfiguration == null) {
      throw new OpenGammaRuntimeException("Could not get curve construction configuration called " + _configurationName);
    }
    final Set<ValueRequirement> exogenousRequirements = new HashSet<>();
    if (curveConstructionConfiguration.getExogenousConfigurations() != null) {
      final List<String> exogenousConfigurations = curveConstructionConfiguration.getExogenousConfigurations();
      for (final String name : exogenousConfigurations) {
        //TODO deal with arbitrary depth
        final ValueProperties properties = ValueProperties.builder()
            .with(CURVE_CONSTRUCTION_CONFIG, name)
            .with(CURVE_CALCULATION_METHOD, ROOT_FINDING)
            .get();
        exogenousRequirements.add(new ValueRequirement(CURVE_BUNDLE, ComputationTargetSpecification.NULL, properties));
        exogenousRequirements.add(new ValueRequirement(JACOBIAN_BUNDLE, ComputationTargetSpecification.NULL, properties));
      }
    }
    final String[] curveNames = CurveUtils.getCurveNamesForConstructionConfiguration(curveConstructionConfiguration);
    final ConventionSource conventionSource = OpenGammaCompilationContext.getConventionSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    try {
      final CurveNodeVisitor<Set<Currency>> visitor = new QuandlCurveNodeCurrencyVisitor(conventionSource, securitySource, configSource);
      final Set<Currency> currencies = CurveUtils.getCurrencies(curveConstructionConfiguration, _curveDefinitionSource, _curveConstructionConfigurationSource,
          visitor);
      final String[] currencyStrings = new String[currencies.size()];
      int i = 0;
      for (final Currency currency : currencies) {
        currencyStrings[i++] = currency.getCode();
      }
      return getCompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000),
          curveNames, exogenousRequirements, curveConstructionConfiguration, currencyStrings);
    } catch (final Throwable e) {
      LOGGER.error("{}: problem in CurveConstructionConfiguration called {}", e.getMessage(), _configurationName);
      LOGGER.error("Full stack trace", e);
      throw new OpenGammaRuntimeException(e.getMessage() + ": problem in CurveConstructionConfiguration called " + _configurationName);
    }
  }

  /**
   * Extends the parent class functionality to use {@link QuandlRateFutureNodeConverter} when converting nodes to instrument definitions.
   */
  protected class QuandlCompiledFunctionDefinition extends MyCompiledFunctionDefinition {

    /**
     * Creates an instance.
     * @param earliestInvocation The earliest time for which the compilation is valid
     * @param latestInvocation The latest time for which the compilation is valid
     * @param curveNames The curve names
     * @param exogenousRequirements The exogenous requirements
     * @param curveConstructionConfiguration The curve construction configuration
     * @param currencies The currencies associated with the curves
     */
    protected QuandlCompiledFunctionDefinition(final ZonedDateTime earliestInvocation, final ZonedDateTime latestInvocation, final String[] curveNames,
        final Set<ValueRequirement> exogenousRequirements, final CurveConstructionConfiguration curveConstructionConfiguration,
        final String[] currencies) {
      super(earliestInvocation, latestInvocation, curveNames, exogenousRequirements, curveConstructionConfiguration, currencies);
    }

    @Override
    protected CurveNodeConverter getCurveNodeConverter(final ConventionSource conventionSource) {
      return new QuandlCurveNodeConverter(conventionSource);
    }

    @Override
    protected CurveNodeVisitor<InstrumentDefinition<?>> getCurveNodeConverter(final FunctionExecutionContext context,
        final SnapshotDataBundle marketData, final ExternalId dataId, final HistoricalTimeSeriesBundle historicalData,
        final ZonedDateTime valuationTime, final FXMatrix fx) {
      final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(context);
      final ConventionSource conventionSource = OpenGammaExecutionContext.getConventionSource(context);
      final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(context);
      final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(context);
      final ConfigSource configSource = OpenGammaExecutionContext.getConfigSource(context);
      final ConfigSourceQuery<DateSet> dateSetQuery = new ConfigSourceQuery<>(configSource, DateSet.class,
          VersionCorrection.of(valuationTime.toInstant(), valuationTime.toInstant()));
      return CurveNodeVisitorAdapter.<InstrumentDefinition<?>>builder()
          .cashNodeVisitor(new CashNodeConverter(securitySource, conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .calendarSwapNode(new CalendarSwapNodeConverter(securitySource, conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime,
              dateSetQuery))
          .fraNode(new FRANodeConverter(securitySource, conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .fxForwardNode(new FXForwardNodeConverter(conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .immFRANode(new RollDateFRANodeConverter(securitySource, conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .immSwapNode(new RollDateSwapNodeConverter(securitySource, conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .rateFutureNode(new QuandlRateFutureNodeConverter(securitySource, conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .swapNode(new SwapNodeConverter(securitySource, conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime, fx))
          .threeLegBasisSwapNode(new ThreeLegBasisSwapNodeConverter(securitySource, conventionSource, holidaySource, regionSource, marketData, dataId,
              valuationTime))
          .create();
    }

    @Override
    public boolean canHandleMissingRequirements() {
      return true;
    }

    @Override
    public boolean canHandleMissingInputs() {
      return true;
    }
  }
}
