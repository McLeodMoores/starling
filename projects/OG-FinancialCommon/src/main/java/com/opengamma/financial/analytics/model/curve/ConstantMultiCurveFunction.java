/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.model.curve;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_METHOD;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_SENSITIVITY_CURRENCY;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_DEFINITION;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_MARKET_DATA;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_SPECIFICATION;
import static com.opengamma.engine.value.ValueRequirementNames.JACOBIAN_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.DIRECT;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.curve.AbstractCurveDefinition;
import com.opengamma.financial.analytics.curve.ConfigDBCurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.ConstantCurveDefinition;
import com.opengamma.financial.analytics.curve.ConstantCurveSpecification;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.CurveUtils;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IborCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.OvernightCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.credit.ConfigDBCurveDefinitionSource;
import com.opengamma.financial.analytics.curve.credit.CurveDefinitionSource;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Produces yield curves using the {@link CurveCalculationPropertyNamesAndValues#CONSTANT_FROM_RATE} method.
 * <p>
 * This function uses information from the {@link ConstantCurveSpecification} referenced in the
 * {@link CurveConstructionConfiguration} to create a curve bundle that contains a yield curve with underlying
 * type {@link ConstantDoublesCurve}. This means that this function handles only configurations where all
 * referenced curves are constant curves.
 * <p>
 * The market data id in the specification is assumed to be a <b>rate</b> and so no transformations are performed on this value.
 */
public class ConstantMultiCurveFunction extends
  MultiCurveFunction<MulticurveProviderInterface, MulticurveDiscountBuildingRepository, GeneratorYDCurve, MulticurveSensitivity> {
  /** The logger. */
  /* package */static final Logger LOGGER = LoggerFactory.getLogger(ConstantMultiCurveFunction.class);
  /** A curve construction configuration source */
  private CurveConstructionConfigurationSource _curveConstructionConfigurationSource;
  /** A curve definition source */
  private CurveDefinitionSource _curveDefinitionSource;

  /**
   * @param curveConfigurationName  the curve configuration name, not null
   */
  public ConstantMultiCurveFunction(final String curveConfigurationName) {
    super(curveConfigurationName);
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _curveConstructionConfigurationSource = ConfigDBCurveConstructionConfigurationSource.init(context, this);
    _curveDefinitionSource = ConfigDBCurveDefinitionSource.init(context, this);
  }

  @Override
  protected String getCurveTypeProperty() {
    return CurveCalculationPropertyNamesAndValues.DISCOUNTING;
  }

  @Override
  protected InstrumentDerivativeVisitor<MulticurveProviderInterface, Double> getCalculator() {
    throw new UnsupportedOperationException("Constant curves do not use a calculator");
  }

  @Override
  protected InstrumentDerivativeVisitor<MulticurveProviderInterface, MulticurveSensitivity> getSensitivityCalculator() {
    throw new UnsupportedOperationException("Constant curves do not use a sensitivity calculator");
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    //TODO work out a way to use dependency graph to get curve information for this config
    final String curveConstructionConfigurationName = getCurveConstructionConfigurationName();
    final CurveConstructionConfiguration curveConstructionConfiguration =
        _curveConstructionConfigurationSource.getCurveConstructionConfiguration(curveConstructionConfigurationName);
    if (curveConstructionConfiguration == null) {
      throw new OpenGammaRuntimeException("Could not get curve construction configuration called " + curveConstructionConfigurationName);
    }
    boolean allCurvesConstant = true;
    for (final CurveGroupConfiguration group : curveConstructionConfiguration.getCurveGroups()) {
      for (final Map.Entry<String, List<? extends CurveTypeConfiguration>> entry : group.getTypesForCurves().entrySet()) {
        final AbstractCurveDefinition definition = _curveDefinitionSource.getDefinition(entry.getKey());
        if (!(definition instanceof ConstantCurveDefinition)) {
          allCurvesConstant = false;
          break;
        }
      }
    }
    final Set<ValueRequirement> exogenousRequirements = new HashSet<>();
    if (curveConstructionConfiguration.getExogenousConfigurations() != null) {
      final List<String> exogenousConfigurations = curveConstructionConfiguration.getExogenousConfigurations();
      for (final String name : exogenousConfigurations) {
        //TODO deal with arbitrary depth
        final ValueProperties properties = ValueProperties.builder()
            .with(CURVE_CONSTRUCTION_CONFIG, name)
            .with(CURVE_CALCULATION_METHOD, DIRECT)
            .get();
        exogenousRequirements.add(new ValueRequirement(CURVE_BUNDLE, ComputationTargetSpecification.NULL, properties));
        exogenousRequirements.add(new ValueRequirement(JACOBIAN_BUNDLE, ComputationTargetSpecification.NULL, properties));
      }
    }
    final String[] curveNames = CurveUtils.getCurveNamesForConstructionConfiguration(curveConstructionConfiguration);
    try {
      return new ConstantMultiCurveCompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000),
          curveNames, exogenousRequirements, curveConstructionConfiguration, allCurvesConstant);
    } catch (final Throwable e) {
      throw new OpenGammaRuntimeException(e.getMessage() + ": problem in CurveConstructionConfiguration called " + curveConstructionConfigurationName);
    }
  }

  @Override
  public CompiledFunctionDefinition getCompiledFunction(final ZonedDateTime earliestInvocation, final ZonedDateTime latestInvocation,
      final String[] curveNames, final Set<ValueRequirement> exogenousRequirements, final CurveConstructionConfiguration curveConstructionConfiguration) {
    throw new UnsupportedOperationException();
  }

  @Override
  public CompiledFunctionDefinition getCompiledFunction(final ZonedDateTime earliestInvocation, final ZonedDateTime latestInvocation,
      final String[] curveNames, final Set<ValueRequirement> exogenousRequirements, final CurveConstructionConfiguration curveConstructionConfiguration,
      final String[] currencies) {
    throw new UnsupportedOperationException();
  }

  /**
   * Compiled function implementation.
   */
  protected class ConstantMultiCurveCompiledFunction extends CurveCompiledFunctionDefinition {
    /** The curve construction configuration */
    private final CurveConstructionConfiguration _curveConstructionConfiguration;
    /** True if all curves in the configuration are constant. */
    private final boolean _allCurvesConstant;

    /**
     * @param earliestInvocation  the earliest time for which this function is valid, null if there is no bound
     * @param latestInvocation  the latest time for which this function is valid, null if there is no bound
     * @param curveNames  the names of the curves produced by this function, not null
     * @param exogenousRequirements  the exogenous requirements, not null
     * @param curveConstructionConfiguration  the curve construction configuration, not null
     * @param currencies  not used
     * @param allCurvesConstant  true if all curves in the configuration are constant curves
     */
    protected ConstantMultiCurveCompiledFunction(final ZonedDateTime earliestInvocation, final ZonedDateTime latestInvocation,
        final String[] curveNames, final Set<ValueRequirement> exogenousRequirements,
        final CurveConstructionConfiguration curveConstructionConfiguration, final String[] currencies,
        final boolean allCurvesConstant) {
      this(earliestInvocation, latestInvocation, curveNames, exogenousRequirements, curveConstructionConfiguration, allCurvesConstant);
    }

    /**
     * @param earliestInvocation  the earliest time for which this function is valid, null if there is no bound
     * @param latestInvocation  the latest time for which this function is valid, null if there is no bound
     * @param curveNames  the names of the curves produced by this function, not null
     * @param exogenousRequirements  the exogenous requirements, not null
     * @param curveConstructionConfiguration  the curve construction configuration, not null
     * @param allCurvesConstant  true if all curves in the configuration are constant curves
     */
    @SuppressWarnings("deprecation")
    protected ConstantMultiCurveCompiledFunction(final ZonedDateTime earliestInvocation, final ZonedDateTime latestInvocation,
        final String[] curveNames, final Set<ValueRequirement> exogenousRequirements,
        final CurveConstructionConfiguration curveConstructionConfiguration, final boolean allCurvesConstant) {
      super(earliestInvocation, latestInvocation, curveNames, YIELD_CURVE, exogenousRequirements);
      ArgumentChecker.notNull(curveConstructionConfiguration, "curve construction configuration");
      _curveConstructionConfiguration = curveConstructionConfiguration;
      _allCurvesConstant = allCurvesConstant;
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      return _allCurvesConstant;
    }

    @Override
    public boolean canHandleMissingRequirements() {
      return true;
    }

    @Override
    public boolean canHandleMissingInputs() {
      return true;
    }

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
        final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
      final ValueProperties.Builder propertiesBuilder = desiredValues.iterator().next().getConstraints().copy()
          .withoutAny(CURVE)
          .with(CURVE, Arrays.asList(getCurveNames()));
      final ValueProperties properties  = propertiesBuilder.get();
      final ValueSpecification bundleSpec = new ValueSpecification(CURVE_BUNDLE, ComputationTargetSpecification.NULL, properties);
      final ValueSpecification jacobianSpec = new ValueSpecification(JACOBIAN_BUNDLE, ComputationTargetSpecification.NULL, properties);
      final MulticurveProviderDiscount curveBundle = (MulticurveProviderDiscount) getKnownData(inputs);
      final LinkedHashMap<String, Pair<Integer, Integer>> unitMap = new LinkedHashMap<>();
      final LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> unitBundles = new LinkedHashMap<>();
      int totalCurves = 0;
      for (final CurveGroupConfiguration group: _curveConstructionConfiguration.getCurveGroups()) {
        for (final Map.Entry<String, List<? extends CurveTypeConfiguration>> entry: group.getTypesForCurves().entrySet()) {
          final String curveName = entry.getKey();
          final List<? extends CurveTypeConfiguration> types = entry.getValue();
          final ValueProperties curveProperties = ValueProperties.builder().with(CURVE, curveName).get();
          final Object dataObject = inputs.getValue(new ValueRequirement(CURVE_MARKET_DATA, ComputationTargetSpecification.NULL, curveProperties));
          if (dataObject == null) {
            throw new OpenGammaRuntimeException("Could not get yield curve data");
          }
          final SnapshotDataBundle marketData = (SnapshotDataBundle) dataObject;
          final ConstantCurveSpecification specification =
              (ConstantCurveSpecification) inputs.getValue(new ValueRequirement(CURVE_SPECIFICATION, ComputationTargetSpecification.NULL, curveProperties));
          final Double marketValue = marketData.getDataPoint(specification.getIdentifier());
          if (marketValue == null) {
            throw new OpenGammaRuntimeException("Could not get market value for " + specification.getIdentifier());
          }
          final ConstantDoublesCurve constantCurve = ConstantDoublesCurve.from(marketValue, curveName);
          final YieldCurve yieldCurve = new YieldCurve(curveName, constantCurve);
          for (final CurveTypeConfiguration type: types) {
            if (type instanceof DiscountingCurveTypeConfiguration) {
              final Currency currency = Currency.parse(((DiscountingCurveTypeConfiguration) type).getReference());
              curveBundle.setCurve(currency, yieldCurve);
            } else if (type instanceof IborCurveTypeConfiguration) {
              final ConventionSource conventionSource = OpenGammaExecutionContext.getConventionSource(executionContext);
              final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(executionContext);
              final IborCurveTypeConfiguration iborCurveType = (IborCurveTypeConfiguration) type;
              final IborIndex index = CurveUtils.getIborIndexFromConfiguration(iborCurveType, securitySource, conventionSource);
              curveBundle.setCurve(index, yieldCurve);
            } else if (type instanceof OvernightCurveTypeConfiguration) {
              final ConventionSource conventionSource = OpenGammaExecutionContext.getConventionSource(executionContext);
              final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(executionContext);
              final OvernightCurveTypeConfiguration overnightCurveType = (OvernightCurveTypeConfiguration) type;
              final IndexON index = CurveUtils.getOvernightIndexFromConfiguration(overnightCurveType, securitySource, conventionSource);
              curveBundle.setCurve(index, yieldCurve);
            }
          }
          final double[][] jacobian = new double[][] {{1}};
          unitMap.put(curveName, Pairs.of(totalCurves, 1));
          unitBundles.put(curveName, Pairs.of(new CurveBuildingBlock(unitMap), new DoubleMatrix2D(jacobian)));
          totalCurves++;
        }
      }
      final Pair<MulticurveProviderInterface, CurveBuildingBlockBundle> pair =
          Pairs.of((MulticurveProviderInterface) curveBundle, new CurveBuildingBlockBundle(unitBundles));
      return getResults(bundleSpec, jacobianSpec, properties, pair);
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext compilationContext, final ComputationTarget target,
        final ValueRequirement desiredValue) {
      final Set<ValueRequirement> requirements = new HashSet<>();
      for (final String curveName : getCurveNames()) {
        final ValueProperties properties = ValueProperties.builder().with(CURVE, curveName).get();
        requirements.add(new ValueRequirement(CURVE_DEFINITION, ComputationTargetSpecification.NULL, properties));
        requirements.add(new ValueRequirement(CURVE_MARKET_DATA, ComputationTargetSpecification.NULL, properties));
        requirements.add(new ValueRequirement(CURVE_SPECIFICATION, ComputationTargetSpecification.NULL, properties));
      }
      return requirements;
    }

    @Override
    protected MulticurveProviderInterface getKnownData(final FunctionInputs inputs) {
      final FXMatrix fxMatrix = new FXMatrix();
      MulticurveProviderDiscount knownData;
      if (getExogenousRequirements().isEmpty()) {
        knownData = new MulticurveProviderDiscount(fxMatrix);
      } else {
        knownData = (MulticurveProviderDiscount) inputs.getValue(CURVE_BUNDLE);
        if (knownData == null) {
          throw new OpenGammaRuntimeException("Could not get known data: exogenous requirements were " + getExogenousRequirements());
        }
        knownData.setForexMatrix(fxMatrix);
      }
      return knownData;
    }

    @Override
    protected Pair<MulticurveProviderInterface, CurveBuildingBlockBundle> getCurves(final FunctionInputs inputs, final ZonedDateTime now,
        final MulticurveDiscountBuildingRepository builder, final MulticurveProviderInterface knownData, final FunctionExecutionContext context,
        final FXMatrix fx) {
      return null;
    }

    @Override
    protected MulticurveDiscountBuildingRepository getBuilder(final double absoluteTolerance, final double relativeTolerance, final int maxIterations) {
      // Returns null because builder is not used
      return null;
    }

    @Override
    protected GeneratorYDCurve getGenerator(final CurveDefinition definition, final LocalDate valuationDate) {
      // Returns null because generator is not used
      return null;
    }

    @Override
    protected CurveNodeVisitor<InstrumentDefinition<?>> getCurveNodeConverter(final FunctionExecutionContext context, final SnapshotDataBundle marketData,
        final ExternalId dataId, final HistoricalTimeSeriesBundle historicalData, final ZonedDateTime valuationTime, final FXMatrix fxMatrix) {
      // No need to convert to InstrumentDefinition if we are not fitting the curve.
      return null;
    }

    @Override
    protected Set<ComputedValue> getResults(final ValueSpecification bundleSpec, final ValueSpecification jacobianSpec, final ValueProperties bundleProperties,
        final Pair<MulticurveProviderInterface, CurveBuildingBlockBundle> pair) {
      final Set<ComputedValue> result = new HashSet<>();
      final MulticurveProviderDiscount provider = (MulticurveProviderDiscount) pair.getFirst();
      result.add(new ComputedValue(bundleSpec, provider));
      result.add(new ComputedValue(jacobianSpec, pair.getSecond()));
      for (final String curveName : getCurveNames()) {
        final ValueProperties curveProperties = bundleProperties.copy()
            .with(CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE, getCurveTypeProperty())
            .withoutAny(CURVE)
            .withoutAny(CURVE_SENSITIVITY_CURRENCY)
            .with(CURVE, curveName)
            .get();
        final YieldAndDiscountCurve curve = provider.getCurve(curveName);
        if (curve == null) {
          LOGGER.error("Could not get curve called {} from configuration {}", curveName, getCurveConstructionConfigurationName());
        } else {
          final ValueSpecification curveSpec = new ValueSpecification(YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties);
          result.add(new ComputedValue(curveSpec, curve));
        }
      }
      return result;
    }

    /**
     * Gets the result properties for a curve.
     *
     * @param curveName The curve name
     * @return The result properties
     */
    @Override
    @SuppressWarnings("synthetic-access")
    protected ValueProperties getCurveProperties(final String curveName) {
      return createValueProperties()
          .with(CURVE, curveName)
          .with(CURVE_CALCULATION_METHOD, DIRECT)
          .with(PROPERTY_CURVE_TYPE, getCurveTypeProperty())
          .with(CURVE_CONSTRUCTION_CONFIG, getCurveConstructionConfigurationName())
          .get();
    }

    /**
     * Gets the result properties for a curve bundle.
     *
     * @param curveNames All of the curves produced by this function
     * @param sensitivityCurrencies The set of currencies to which the curves produce sensitivities
     * @return The result properties
     */
    @Override
    @SuppressWarnings("synthetic-access")
    protected ValueProperties getBundleProperties(final String[] curveNames, final String[] sensitivityCurrencies) {
      return createValueProperties()
          .with(CURVE_CALCULATION_METHOD, DIRECT)
          .with(PROPERTY_CURVE_TYPE, getCurveTypeProperty())
          .with(CURVE_CONSTRUCTION_CONFIG, getCurveConstructionConfigurationName())
          .with(CURVE, curveNames)
          .get();
    }

  }
}
