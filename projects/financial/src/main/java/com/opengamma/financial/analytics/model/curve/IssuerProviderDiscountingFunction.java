/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.model.curve;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_SENSITIVITY_CURRENCY;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_DEFINITION;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_MARKET_DATA;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_SPECIFICATION;
import static com.opengamma.engine.value.ValueRequirementNames.FX_MATRIX;
import static com.opengamma.engine.value.ValueRequirementNames.JACOBIAN_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.DISCOUNTING;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.LinkedListMultimap;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldConstant;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillTransactionDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.calculator.issuer.ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.ParSpreadMarketQuoteIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.SingleCurveBundle;
import com.opengamma.analytics.financial.provider.curve.issuer.IssuerDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.BondAndBondFutureTradeWithEntityConverter;
import com.opengamma.financial.analytics.curve.AbstractCurveDefinition;
import com.opengamma.financial.analytics.curve.AbstractCurveSpecification;
import com.opengamma.financial.analytics.curve.BillNodeConverter;
import com.opengamma.financial.analytics.curve.BondNodeConverter;
import com.opengamma.financial.analytics.curve.CashNodeConverter;
import com.opengamma.financial.analytics.curve.ConstantCurveSpecification;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveNodeVisitorAdapter;
import com.opengamma.financial.analytics.curve.CurveSpecification;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.CurveUtils;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.FRANodeConverter;
import com.opengamma.financial.analytics.curve.FXForwardNodeConverter;
import com.opengamma.financial.analytics.curve.IborCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.curve.IssuerCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.OvernightCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.RateFutureNodeConverter;
import com.opengamma.financial.analytics.curve.RollDateFRANodeConverter;
import com.opengamma.financial.analytics.curve.RollDateSwapNodeConverter;
import com.opengamma.financial.analytics.curve.SwapNodeConverter;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Produces price index curves using the discounting method.
 */
public class IssuerProviderDiscountingFunction extends
  MultiCurveFunction<ParameterIssuerProviderInterface, IssuerDiscountBuildingRepository, GeneratorYDCurve, MulticurveSensitivity> {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(IssuerProviderDiscountingFunction.class);
  /** The calculator */
  // TODO: [PLAT-5430] A mechanism to change the calculator should be implemented.
  private static final ParSpreadMarketQuoteIssuerDiscountingCalculator PSXIC = ParSpreadMarketQuoteIssuerDiscountingCalculator.getInstance();
  /** The sensitivity calculator */
  private static final ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator PSXCSIC =
      ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator.getInstance();

  /**
   * @param configurationName The configuration name, not null
   */
  public IssuerProviderDiscountingFunction(final String configurationName) {
    super(configurationName);
  }

  @Override
  public CompiledFunctionDefinition getCompiledFunction(final ZonedDateTime earliestInvokation, final ZonedDateTime latestInvokation, final String[] curveNames,
      final Set<ValueRequirement> exogenousRequirements, final CurveConstructionConfiguration curveConstructionConfiguration) {
    return new MyCompiledFunctionDefinition(earliestInvokation, latestInvokation, curveNames, exogenousRequirements, curveConstructionConfiguration);
  }

  @Override
  public CompiledFunctionDefinition getCompiledFunction(final ZonedDateTime earliestInvokation, final ZonedDateTime latestInvokation, final String[] curveNames,
      final Set<ValueRequirement> exogenousRequirements, final CurveConstructionConfiguration curveConstructionConfiguration, final String[] currencies) {
    return new MyCompiledFunctionDefinition(earliestInvokation, latestInvokation, curveNames, exogenousRequirements, curveConstructionConfiguration,
        currencies);
  }

  @Override
  protected InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, Double> getCalculator() {
    return PSXIC;
  }

  @Override
  protected InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, MulticurveSensitivity> getSensitivityCalculator() {
    return PSXCSIC;
  }

  @Override
  protected String getCurveTypeProperty() {
    return DISCOUNTING;
  }

  /**
   * Compiled function implementation.
   */
  protected class MyCompiledFunctionDefinition extends CurveCompiledFunctionDefinition {
    /** The curve construction configuration */
    private final CurveConstructionConfiguration _curveConstructionConfiguration;

    /**
     * @param earliestInvokation The earliest time for which this function is valid, null if there is no bound
     * @param latestInvokation The latest time for which this function is valid, null if there is no bound
     * @param curveNames The names of the curves produced by this function, not null
     * @param exogenousRequirements The exogenous requirements, not null
     * @param curveConstructionConfiguration The curve construction configuration, not null
     */
    protected MyCompiledFunctionDefinition(final ZonedDateTime earliestInvokation, final ZonedDateTime latestInvokation, final String[] curveNames,
        final Set<ValueRequirement> exogenousRequirements, final CurveConstructionConfiguration curveConstructionConfiguration) {
      this(earliestInvokation, latestInvokation, curveNames, exogenousRequirements, curveConstructionConfiguration, null);
    }

    /**
     * @param earliestInvokation The earliest time for which this function is valid, null if there is no bound
     * @param latestInvokation The latest time for which this function is valid, null if there is no bound
     * @param curveNames The names of the curves produced by this function, not null
     * @param exogenousRequirements The exogenous requirements, not null
     * @param curveConstructionConfiguration The curve construction configuration, not null
     * @param currencies The set of currencies to which the curves produce sensitivities
     */
    protected MyCompiledFunctionDefinition(final ZonedDateTime earliestInvokation, final ZonedDateTime latestInvokation, final String[] curveNames,
        final Set<ValueRequirement> exogenousRequirements, final CurveConstructionConfiguration curveConstructionConfiguration,
        final String[] currencies) {
      super(earliestInvokation, latestInvokation, curveNames, ValueRequirementNames.YIELD_CURVE, exogenousRequirements, currencies);
      ArgumentChecker.notNull(curveConstructionConfiguration, "curve construction configuration");
      _curveConstructionConfiguration = curveConstructionConfiguration;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Pair<ParameterIssuerProviderInterface, CurveBuildingBlockBundle> getCurves(final FunctionInputs inputs, final ZonedDateTime now,
        final IssuerDiscountBuildingRepository builder, final ParameterIssuerProviderInterface knownData, final FunctionExecutionContext context, final FXMatrix fx) {
      final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(context);
      final ConventionSource conventionSource = OpenGammaExecutionContext.getConventionSource(context);
      final ValueProperties curveConstructionProperties = ValueProperties.builder()
          .with(CURVE_CONSTRUCTION_CONFIG, _curveConstructionConfiguration.getName())
          .get();
      final HistoricalTimeSeriesBundle timeSeries =
          (HistoricalTimeSeriesBundle) inputs.getValue(new ValueRequirement(CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES,
              ComputationTargetSpecification.NULL, curveConstructionProperties));
      final int nGroups = _curveConstructionConfiguration.getCurveGroups().size();
      final MultiCurveBundle<GeneratorYDCurve>[] curveBundles = new MultiCurveBundle[nGroups];
      final LinkedHashMap<String, Currency> discountingMap = new LinkedHashMap<>();
      final LinkedHashMap<String, IborIndex[]> forwardIborMap = new LinkedHashMap<>();
      final LinkedHashMap<String, IndexON[]> forwardONMap = new LinkedHashMap<>();
      final LinkedListMultimap<String, Pair<Object, LegalEntityFilter<LegalEntity>>> issuerMap = LinkedListMultimap.create();
      //TODO comparator to sort groups by order
      int i = 0; // Implementation Note: loop on the groups
      for (final CurveGroupConfiguration group : _curveConstructionConfiguration.getCurveGroups()) { // Group - start
        int j = 0;
        final int nCurves = group.getTypesForCurves().size();
        final SingleCurveBundle<GeneratorYDCurve>[] singleCurves = new SingleCurveBundle[nCurves];
        for (final Map.Entry<String, List<? extends CurveTypeConfiguration>> entry : group.getTypesForCurves().entrySet()) {
          final List<IborIndex> iborIndexList = new ArrayList<>();
          final List<IndexON> overnightIndexList = new ArrayList<>();
          final String curveName = entry.getKey();
          final ValueProperties properties = ValueProperties.builder().with(CURVE, curveName).get();
          final AbstractCurveSpecification specification =
              (AbstractCurveSpecification) inputs.getValue(new ValueRequirement(CURVE_SPECIFICATION, ComputationTargetSpecification.NULL, properties));
          final AbstractCurveDefinition definition =
              (AbstractCurveDefinition) inputs.getValue(new ValueRequirement(CURVE_DEFINITION, ComputationTargetSpecification.NULL, properties));
          final SnapshotDataBundle snapshot =
              (SnapshotDataBundle) inputs.getValue(new ValueRequirement(CURVE_MARKET_DATA, ComputationTargetSpecification.NULL, properties));
          final InstrumentDerivative[] derivativesForCurve;
          final double[] parameterGuessForCurves;
          if (specification instanceof CurveSpecification) {
            final CurveSpecification curveSpecification = (CurveSpecification) specification;
            final int nNodes = curveSpecification.getNodes().size();
            derivativesForCurve = new InstrumentDerivative[nNodes];
            parameterGuessForCurves = new double[nNodes];
            int k = 0;
            for (final CurveNodeWithIdentifier node : curveSpecification.getNodes()) { // Node points - start
              final Double marketData = snapshot.getDataPoint(node.getIdentifier());
              if (marketData == null) {
                throw new OpenGammaRuntimeException("Could not get market data for " + node.getIdentifier());
              }
              parameterGuessForCurves[k] = 0.02; // TODO: [PlAT-5883] Get a better starting point.
              final InstrumentDefinition<?> definitionForNode = node.getCurveNode().accept(getCurveNodeConverter(context,
                  snapshot, node.getIdentifier(), timeSeries, now, fx));
              derivativesForCurve[k++] = getCurveNodeConverter(conventionSource).getDerivative(node, definitionForNode, now, timeSeries);
            }
            final GeneratorYDCurve generator = getGenerator((CurveDefinition) definition, now.toLocalDate());
            singleCurves[j++] = new SingleCurveBundle<>(curveName, derivativesForCurve, generator.initialGuess(parameterGuessForCurves), generator);
          } else if (specification instanceof ConstantCurveSpecification) {
            // TODO fix this
            final ConstantCurveSpecification constantSpecification = (ConstantCurveSpecification) specification;
            if (!constantSpecification.getDataField().equals(MarketDataRequirementNames.YIELD_YIELD_TO_MATURITY_MID)) {
              throw new OpenGammaRuntimeException("Cannot handle field " + constantSpecification.getDataField());
            }
            derivativesForCurve = new InstrumentDerivative[1];
            parameterGuessForCurves = new double[] {0.03};
            final FinancialSecurity security = (FinancialSecurity) securitySource.getSingle(constantSpecification.getIdentifier().toBundle());
            final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(context);
            final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(context);
            final BondAndBondFutureTradeWithEntityConverter converter = new BondAndBondFutureTradeWithEntityConverter(holidaySource, conventionSource,
                regionSource, securitySource, null);
            final InstrumentDefinition<?> instrumentDefinition = security.accept(converter);
            final InstrumentDefinition<?> transaction;
            final Double marketData = snapshot.getDataPoint(constantSpecification.getIdentifier());
            if (marketData == null) {
              throw new OpenGammaRuntimeException("Could not get market data for " + constantSpecification.getIdentifier());
            }
            if (instrumentDefinition instanceof BondFixedSecurityDefinition) {
              transaction = BondFixedTransactionDefinition.fromYield((BondFixedSecurityDefinition) instrumentDefinition, 1, now, marketData);
            } else if (instrumentDefinition instanceof BillSecurityDefinition) {
              //TODO this will throw an exception
              transaction = BillTransactionDefinition.fromYield((BillSecurityDefinition) instrumentDefinition, 1, now, marketData, (WorkingDayCalendar) null);
            } else {
              throw new OpenGammaRuntimeException("Cannot handle definitions of type " + instrumentDefinition.getClass());
            }
            final InstrumentDerivative derivative = transaction.toDerivative(now);
            final GeneratorCurveYieldConstant generator = new GeneratorCurveYieldConstant();
            singleCurves[j++] = new SingleCurveBundle<GeneratorYDCurve>(curveName, new InstrumentDerivative[] {derivative},
                generator.initialGuess(parameterGuessForCurves), generator);
          } else {
            throw new OpenGammaRuntimeException("Cannot handle specifications of type " + specification.getClass() + ": " + specification);
          }
          for (final CurveTypeConfiguration type : entry.getValue()) { // Type - start
            if (type instanceof DiscountingCurveTypeConfiguration) {
              discountingMap.put(curveName, CurveUtils.getCurrencyFromConfiguration((DiscountingCurveTypeConfiguration) type));
            } else if (type instanceof IborCurveTypeConfiguration) {
              iborIndexList.add(CurveUtils.getIborIndexFromConfiguration((IborCurveTypeConfiguration) type, securitySource, conventionSource));
            } else if (type instanceof OvernightCurveTypeConfiguration) {
              overnightIndexList.add(CurveUtils.getOvernightIndexFromConfiguration((OvernightCurveTypeConfiguration) type, securitySource, conventionSource));
            } else if (type instanceof IssuerCurveTypeConfiguration) {
              final IssuerCurveTypeConfiguration issuer = (IssuerCurveTypeConfiguration) type;
              issuerMap.put(curveName, Pairs.<Object, LegalEntityFilter<LegalEntity>>of(issuer.getKeys(), issuer.getFilters()));
            } else {
              throw new OpenGammaRuntimeException("Cannot handle " + type.getClass());
            }
          } // type - end
          if (!iborIndexList.isEmpty()) {
            forwardIborMap.put(curveName, iborIndexList.toArray(new IborIndex[iborIndexList.size()]));
          }
          if (!overnightIndexList.isEmpty()) {
            forwardONMap.put(curveName, overnightIndexList.toArray(new IndexON[overnightIndexList.size()]));
          }
        }
        final MultiCurveBundle<GeneratorYDCurve> groupBundle = new MultiCurveBundle<>(singleCurves);
        curveBundles[i++] = groupBundle;
      } // Group - end
      //TODO this is only in here because the code in analytics doesn't use generics properly
      final Pair<IssuerProviderDiscount, CurveBuildingBlockBundle> temp = builder.makeCurvesFromDerivatives(curveBundles,
          (IssuerProviderDiscount) knownData, discountingMap, forwardIborMap, forwardONMap, issuerMap, getCalculator(), getSensitivityCalculator());
      final CurveBuildingBlockBundle exogenousJacobians = new CurveBuildingBlockBundle();
      for (final ComputedValue input : inputs.getAllValues()) {
        final String valueName = input.getSpecification().getValueName();
        if (valueName.equals(JACOBIAN_BUNDLE)) {
          exogenousJacobians.addAll((CurveBuildingBlockBundle) input.getValue());
        }
      }
      final LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> unitBundles = new LinkedHashMap<>();
      for (final Map.Entry<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> entry : exogenousJacobians.getData().entrySet()) {
        unitBundles.put(entry.getKey(), entry.getValue());
      }
      unitBundles.putAll(temp.getSecond().getData());
      final Pair<ParameterIssuerProviderInterface, CurveBuildingBlockBundle> result = Pairs.of((ParameterIssuerProviderInterface) temp.getFirst(), new CurveBuildingBlockBundle(unitBundles));
      return result;
    }

    @Override
    protected IssuerProviderInterface getKnownData(final FunctionInputs inputs) {
      final FXMatrix fxMatrix = (FXMatrix) inputs.getValue(FX_MATRIX);
      //TODO requires that the discounting curves are supplied externally
      IssuerProviderDiscount knownData;
      if (getExogenousRequirements().isEmpty()) {
        knownData = new IssuerProviderDiscount(fxMatrix);
      } else {
        knownData = new IssuerProviderDiscount((MulticurveProviderDiscount) inputs.getValue(CURVE_BUNDLE));
        knownData.getMulticurveProvider().setForexMatrix(fxMatrix);
      }
      return knownData;
    }

    @Override
    protected IssuerDiscountBuildingRepository getBuilder(final double absoluteTolerance, final double relativeTolerance, final int maxIterations) {
      return new IssuerDiscountBuildingRepository(absoluteTolerance, relativeTolerance, maxIterations);
    }

    @Override
    protected GeneratorYDCurve getGenerator(final CurveDefinition definition, final LocalDate valuationDate) {
      if (definition instanceof InterpolatedCurveDefinition) {
        final InterpolatedCurveDefinition interpolatedDefinition = (InterpolatedCurveDefinition) definition;
        final String interpolatorName = interpolatedDefinition.getInterpolatorName();
        final String leftExtrapolatorName = interpolatedDefinition.getLeftExtrapolatorName();
        final String rightExtrapolatorName = interpolatedDefinition.getRightExtrapolatorName();
        final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
        return new GeneratorCurveYieldInterpolated(getMaturityCalculator(), interpolator);
      }
      throw new OpenGammaRuntimeException("Cannot handle curves of type " + definition.getClass());
    }

    @Override
    protected CurveNodeVisitor<InstrumentDefinition<?>> getCurveNodeConverter(final FunctionExecutionContext context,
        final SnapshotDataBundle marketData, final ExternalId dataId, final HistoricalTimeSeriesBundle historicalData,
        final ZonedDateTime valuationTime, final FXMatrix fx) {
      final ConventionSource conventionSource = OpenGammaExecutionContext.getConventionSource(context);
      final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(context);
      final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(context);
      final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(context);
      final LegalEntitySource legalEntitySource = OpenGammaExecutionContext.getLegalEntitySource(context);
      return CurveNodeVisitorAdapter.<InstrumentDefinition<?>>builder()
          .billNodeVisitor(new BillNodeConverter(holidaySource, regionSource, securitySource, legalEntitySource, marketData, dataId, valuationTime))
          .bondNodeVisitor(new BondNodeConverter(conventionSource, holidaySource, regionSource, securitySource, marketData, dataId, valuationTime))
          .cashNodeVisitor(new CashNodeConverter(securitySource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .fraNode(new FRANodeConverter(securitySource, conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .fxForwardNode(new FXForwardNodeConverter(conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .immFRANode(new RollDateFRANodeConverter(securitySource, conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .immSwapNode(new RollDateSwapNodeConverter(securitySource, conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .rateFutureNode(new RateFutureNodeConverter(securitySource, conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .swapNode(new SwapNodeConverter(securitySource, conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime, fx))
          .create();
    }

    @Override
    protected Set<ComputedValue> getResults(final ValueSpecification bundleSpec, final ValueSpecification jacobianSpec,
        final ValueProperties bundleProperties, final Pair<ParameterIssuerProviderInterface, CurveBuildingBlockBundle> pair) {
      final Set<ComputedValue> result = new HashSet<>();
      final IssuerProviderDiscount provider = (IssuerProviderDiscount) pair.getFirst();
      result.add(new ComputedValue(bundleSpec, provider));
      result.add(new ComputedValue(jacobianSpec, pair.getSecond()));
      for (final String curveName : getCurveNames()) {
        final ValueProperties curveProperties = bundleProperties.copy()
            .withoutAny(CURVE)
            .withoutAny(CURVE_SENSITIVITY_CURRENCY)
            .with(CURVE, curveName)
            .get();
        YieldAndDiscountCurve curve = provider.getIssuerCurve(curveName);
        if (curve == null) {
          curve = provider.getMulticurveProvider().getCurve(curveName);
        }
        if (curve == null) {
          LOGGER.error("Could not get curve called {} from configuration {}", curveName, getCurveConstructionConfigurationName());
        } else {
          final ValueSpecification curveSpec = new ValueSpecification(YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties);
          result.add(new ComputedValue(curveSpec, curve));
        }
      }
      return result;
    }
  }
}

