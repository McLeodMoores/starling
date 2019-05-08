/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.credit.cds.isda;

import static com.mcleodmoores.financial.function.properties.CurveCalculationProperties.ISDA;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_METHOD;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_MARKET_DATA;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_SPECIFICATION;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.ROOT_FINDING;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.threeten.bp.Clock;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.date.WorkingDayCalendar;
import com.mcleodmoores.financial.function.credit.cds.isda.util.IsdaFunctionUtils;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurveBuild;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDAInstrumentTypes;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.curve.ConfigDBCurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveSpecification;
import com.opengamma.financial.analytics.curve.CurveUtils;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;
import com.opengamma.util.tuple.Triple;

/**
 * A function that creates a yield curve using the ISDA methodology ({@link ISDACompliantYieldCurveBuild}). The only allowable node types are cash (*IBOR) and
 * swap rates.
 */
public class IsdaYieldCurveFunction extends AbstractFunction {
  private final String _configurationName;
  private CurveConstructionConfigurationSource _curveConstructionConfigurationSource;

  /**
   * @param configurationName
   *          the curve configuration name, not null
   */
  public IsdaYieldCurveFunction(final String configurationName) {
    _configurationName = ArgumentChecker.notNull(configurationName, "configurationName");
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _curveConstructionConfigurationSource = ConfigDBCurveConstructionConfigurationSource.init(context, this);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    final CurveConstructionConfiguration curveConstructionConfiguration = _curveConstructionConfigurationSource
        .getCurveConstructionConfiguration(_configurationName);
    if (curveConstructionConfiguration == null) {
      throw new OpenGammaRuntimeException("Could not get curve construction configuration called " + _configurationName);
    }
    if (!curveConstructionConfiguration.getExogenousConfigurations().isEmpty()) {
      throw new OpenGammaRuntimeException(
          "IsdaYieldCurveFunction does not support exogenous curves: problem in curve construction configuration called " + _configurationName);
    }
    final String[] curveNames = CurveUtils.getCurveNamesForConstructionConfiguration(curveConstructionConfiguration);
    final Set<String> uniqueNames = new HashSet<>(Arrays.asList(curveNames));
    if (uniqueNames.size() > 1) {
      throw new OpenGammaRuntimeException("IsdaYieldCurveFunction only produces one curve; have requested " + uniqueNames);
    }
    return new IsdaYieldCurveCompiledFunctionDefinition(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000),
        curveNames[0], curveConstructionConfiguration);
  }

  private final class IsdaYieldCurveCompiledFunctionDefinition extends AbstractInvokingCompiledFunction {
    private final String _curveName;
    private final ValueProperties _curveProperties;
    private final Set<ValueSpecification> _results;

    /**
     * @param earliestInvokation
     *          the earliest time for which this function is valid, null if there is no bound
     * @param latestInvokation
     *          the latest time for which this function is valid, null if there is no bound
     * @param curveName
     *          the name of the curve produced by this function, not null
     * @param config
     *          the curve construction configuration, not null
     */
    protected IsdaYieldCurveCompiledFunctionDefinition(final ZonedDateTime earliestInvokation, final ZonedDateTime latestInvokation, final String curveName,
        final CurveConstructionConfiguration config) {
      super(earliestInvokation, latestInvokation);
      _curveName = ArgumentChecker.notNull(curveName, "curveName");
      _results = new HashSet<>();
      // final ValueProperties properties = getBundleProperties(config.getName(), curveName);
      _curveProperties = getCurveProperties(config.getName(), curveName);
      _results.add(new ValueSpecification(YIELD_CURVE, ComputationTargetSpecification.NULL, _curveProperties));
      // _results.add(new ValueSpecification(CURVE_BUNDLE, ComputationTargetSpecification.NULL, properties));
    }

    // private ValueProperties getBundleProperties(final String configName, final String curveName) {
    // return createValueProperties().with(CURVE, curveName).with(CURVE_CALCULATION_METHOD, ROOT_FINDING).with(PROPERTY_CURVE_TYPE, ISDA)
    // .with(CURVE_CONSTRUCTION_CONFIG, configName).get();
    // }

    private ValueProperties getCurveProperties(final String configName, final String curveName) {
      return createValueProperties().with(CURVE, curveName).with(CURVE_CALCULATION_METHOD, ROOT_FINDING).with(PROPERTY_CURVE_TYPE, ISDA)
          .with(CURVE_CONSTRUCTION_CONFIG, configName).get();
    }

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
        final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
      final Clock snapshotClock = executionContext.getValuationClock();
      final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
      final LocalDate valuationDate = now.toLocalDate();
      final ConventionSource conventionSource = OpenGammaExecutionContext.getConventionSource(executionContext);
      final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
      final CurveSpecification specification = (CurveSpecification) inputs.getComputedValue(CURVE_SPECIFICATION).getValue();
      final SnapshotDataBundle marketData = (SnapshotDataBundle) inputs.getComputedValue(CURVE_MARKET_DATA).getValue();
      final int nNodes = specification.getNodes().size();
      final ISDAInstrumentTypes[] instrumentTypes = new ISDAInstrumentTypes[nNodes];
      final Period[] tenors = new Period[nNodes];
      final double[] rates = new double[nNodes];
      DayCount cashDayCount = null, swapDayCount = null;
      BusinessDayConvention swapBusinessDayConvention = null;
      Period swapInterval = null;
      Currency currency = null;
      int i = 0;
      for (final CurveNodeWithIdentifier node : specification.getNodes()) {
        if (node.getCurveNode() instanceof CashNode) {
          instrumentTypes[i] = ISDAInstrumentTypes.MoneyMarket;
          final CashNode curveNode = (CashNode) node.getCurveNode();
          tenors[i] = curveNode.getMaturityTenor().getPeriod();
          final Pair<DayCount, Currency> cashInfo = getCashDayCountConvention(conventionSource, curveNode);
          if (cashDayCount == null) {
            cashDayCount = cashInfo.getFirst();
            currency = cashInfo.getSecond();
          } else {
            checkCash(specification, cashDayCount, currency, curveNode, cashInfo);
          }
        } else if (node.getCurveNode() instanceof SwapNode) {
          final SwapNode curveNode = (SwapNode) node.getCurveNode();
          instrumentTypes[i] = ISDAInstrumentTypes.Swap;
          tenors[i] = ((SwapNode) node.getCurveNode()).getMaturityTenor().getPeriod();
          final Triple<DayCount, Tenor, Currency> swapFixedLegInfo = getSwapDayCountConvention(conventionSource, curveNode);
          if (swapDayCount == null) {
            swapDayCount = swapFixedLegInfo.getFirst();
            swapInterval = swapFixedLegInfo.getSecond().getPeriod();
          } else {
            checkSwapFixedLeg(specification, swapDayCount, swapInterval, curveNode, swapFixedLegInfo);
          }
          if (currency == null) {
            currency = swapFixedLegInfo.getThird();
          } else {
            if (!swapFixedLegInfo.getThird().equals(currency)) {
              throw new OpenGammaRuntimeException("Inconsistent swap fixed leg currency found in " + curveNode + " for " + specification.getName());
            }
          }
          final Pair<BusinessDayConvention, Currency> swapFloatingLegInfo = getSwapBusinessDayConvention(conventionSource, curveNode);
          if (swapBusinessDayConvention == null) {
            swapBusinessDayConvention = swapFloatingLegInfo.getFirst();
          } else {
            checkSwapFloatingLeg(specification, swapBusinessDayConvention, currency, curveNode, swapFloatingLegInfo);
          }
        } else {
          throw new OpenGammaRuntimeException(
              "Unexpected node type in ISDA yield curve definition: have " + node + " but can only support CashNode or SwapNode");
        }
        final Double rate = marketData.getDataPoint(node.getIdentifier());
        if (rate == null) {
          throw new OpenGammaRuntimeException("Could not get market data for " + node.getIdentifier());
        }
        rates[i++] = rate;
      }
      if (cashDayCount == null) {
        // have no cash nodes in curve
        cashDayCount = swapDayCount;
      }
      if (swapDayCount == null) {
        // have no swap nodes in curve
        swapDayCount = cashDayCount;
      }
      final Collection<Holiday> holidays = holidaySource.get(currency);
      final WorkingDayCalendar calendar = IsdaFunctionUtils.getCalendar(currency, holidays);
      final ISDACompliantYieldCurve yieldCurve = ISDACompliantYieldCurveBuild.build(valuationDate, valuationDate, instrumentTypes, tenors, rates, cashDayCount,
          swapDayCount, swapInterval, DayCounts.ACT_365, swapBusinessDayConvention, calendar);
      final ValueSpecification spec = new ValueSpecification(YIELD_CURVE, target.toSpecification(), _curveProperties);
      return Collections.singleton(new ComputedValue(spec, yieldCurve));
    }

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.NULL;
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      return _results;
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target,
        final ValueRequirement desiredValue) {
      final Set<ValueRequirement> requirements = new HashSet<>();
      final ValueProperties properties = ValueProperties.builder().with(CURVE, _curveName).get();
      final ValueProperties configProperties = ValueProperties.builder().with(CURVE_CONSTRUCTION_CONFIG, _configurationName).get();
      requirements.add(new ValueRequirement(CURVE_MARKET_DATA, ComputationTargetSpecification.NULL, properties));
      requirements.add(new ValueRequirement(CURVE_SPECIFICATION, ComputationTargetSpecification.NULL, properties));
      requirements.add(new ValueRequirement(CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES, ComputationTargetSpecification.NULL, configProperties));
      return requirements;
    }

    private Pair<DayCount, Currency> getCashDayCountConvention(final ConventionSource conventionSource, final CashNode node) {
      final Convention convention = conventionSource.getSingle(node.getConvention());
      if (convention instanceof IborIndexConvention) {
        final IborIndexConvention indexConvention = (IborIndexConvention) convention;
        return Pairs.of(indexConvention.getDayCount(), indexConvention.getCurrency());
      }
      throw new OpenGammaRuntimeException("Unhandled convention type " + convention.getClass() + " for " + node + ": expected IborIndexConvention");
    }

    private Triple<DayCount, Tenor, Currency> getSwapDayCountConvention(final ConventionSource conventionSource, final SwapNode node) {
      final Convention convention = conventionSource.getSingle(node.getPayLegConvention());
      if (convention instanceof SwapFixedLegConvention) {
        final SwapFixedLegConvention payLegConvention = (SwapFixedLegConvention) convention;
        return Triple.of(payLegConvention.getDayCount(), payLegConvention.getPaymentTenor(), payLegConvention.getCurrency());
      }
      throw new OpenGammaRuntimeException(
          "Unhandled swap pay leg convention type " + convention.getClass() + " for " + node + ": expected SwapFixedLegConvention");
    }

    private Pair<BusinessDayConvention, Currency> getSwapBusinessDayConvention(final ConventionSource conventionSource, final SwapNode node) {
      final Convention convention = conventionSource.getSingle(node.getReceiveLegConvention());
      if (convention instanceof VanillaIborLegConvention) {
        final Convention indexConvention = conventionSource.getSingle(((VanillaIborLegConvention) convention).getIborIndexConvention());
        if (indexConvention instanceof IborIndexConvention) {
          final IborIndexConvention iborIndexConvention = (IborIndexConvention) indexConvention;
          return Pairs.of(iborIndexConvention.getBusinessDayConvention(), iborIndexConvention.getCurrency());
        }
        throw new OpenGammaRuntimeException(
            "Unhandled index convention type " + indexConvention.getClass() + " for " + node + ": expected IborIndexConvention");
      }
      throw new OpenGammaRuntimeException(
          "Unhandled swap receive leg convention type " + convention.getClass() + " for " + node + ": expected VanillaIborLegConvention");
    }

    private void checkCash(final CurveSpecification specification, final DayCount cashDayCount, final Currency currency, final CashNode curveNode,
        final Pair<DayCount, Currency> cashInfo) {
      if (!cashInfo.getFirst().getName().equals(cashDayCount.getName())) {
        throw new OpenGammaRuntimeException("Inconsistent cash day count convention found in " + curveNode + " for " + specification.getName());
      }
      if (!cashInfo.getSecond().equals(currency)) {
        throw new OpenGammaRuntimeException("Inconsistent cash currency convention found in " + curveNode + " for " + specification.getName());
      }
    }

    private void checkSwapFixedLeg(final CurveSpecification specification, final DayCount swapDayCount, final Period swapInterval, final SwapNode curveNode,
        final Triple<DayCount, Tenor, Currency> swapFixedLegInfo) {
      if (!swapFixedLegInfo.getFirst().getName().equals(swapDayCount.getName())) {
        throw new OpenGammaRuntimeException("Inconsistent swap fixed leg day count convention found in " + curveNode + " for " + specification.getName());
      }
      if (!swapFixedLegInfo.getSecond().getPeriod().equals(swapInterval)) {
        throw new OpenGammaRuntimeException("Inconsistent swap fixed leg payment tenor found in " + curveNode + " for " + specification.getName());
      }
    }

    private void checkSwapFloatingLeg(final CurveSpecification specification, final BusinessDayConvention swapBusinessDayConvention, final Currency currency,
        final SwapNode curveNode, final Pair<BusinessDayConvention, Currency> swapFloatingLegInfo) {
      if (!swapFloatingLegInfo.getFirst().getName().equals(swapBusinessDayConvention.getName())) {
        throw new OpenGammaRuntimeException(
            "Inconsistent swap *IBOR leg business day convention found in underlying of receive leg in " + curveNode + " for " + specification.getName());
      }
      if (!swapFloatingLegInfo.getSecond().equals(currency)) {
        throw new OpenGammaRuntimeException(
            "Inconsistent swap *IBOR leg currency found in underlying of receive leg in " + curveNode + " for " + specification.getName());
      }
    }
  }
}
