/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.curve;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_METHOD;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_MARKET_DATA;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_SPECIFICATION;
import static com.opengamma.engine.value.ValueRequirementNames.JACOBIAN_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.NELSON_SIEGEL;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.NON_LINEAR_LEAST_SQUARE;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.NelsonSiegelSvennsonBondCurveModel;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.math.curve.FunctionalDoublesCurve;
import com.opengamma.analytics.math.function.ParameterizedFunction;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.analytics.math.statistics.leastsquare.NonLinearLeastSquare;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.curve.BillNodeConverter;
import com.opengamma.financial.analytics.curve.BondNodeConverter;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveNodeVisitorAdapter;
import com.opengamma.financial.analytics.curve.CurveSpecification;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IssuerCurveTypeConfiguration;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.id.ExternalId;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
public class NelsonSiegelBondCurveFunction extends AbstractFunction.NonCompiledInvoker {
  private static final NonLinearLeastSquare MINIMISER = new NonLinearLeastSquare();
  private static final NelsonSiegelSvennsonBondCurveModel MODEL = new NelsonSiegelSvennsonBondCurveModel();

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final Map<String, Object[]> data = new HashMap<>();
    CurveConstructionConfiguration curveConfig = null;
    for (final ComputedValue input : inputs.getAllValues()) {
      final String valueName = input.getSpecification().getValueName();
      switch (valueName) {
        case CURVE_SPECIFICATION: {
          final String curveName = input.getSpecification().getProperty(CURVE);
          if (!data.containsKey(curveName)) {
            data.put(curveName, new Object[2]);
          }
          final Object[] values = data.get(curveName);
          if (values[0] != null) {
            throw new OpenGammaRuntimeException("Have more than one curve specification for " + curveName);
          }
          values[0] = input.getValue();
          break;
        }
        case CURVE_MARKET_DATA: {
          final String curveName = input.getSpecification().getProperty(CURVE);
          if (!data.containsKey(curveName)) {
            data.put(curveName, new Object[2]);
          }
          final Object[] values = data.get(curveName);
          if (values[1] != null) {
            throw new OpenGammaRuntimeException("Have more than one snapshot data bundle for " + curveName);
          }
          values[1] = input.getValue();
          break;
        }
        case CURVE_CONSTRUCTION_CONFIG:
          curveConfig = (CurveConstructionConfiguration) input.getValue();
          break;
        default:
          throw new OpenGammaRuntimeException("Unexpected input " + input.getSpecification());
      }
    }
    if (curveConfig == null) {
      throw new OpenGammaRuntimeException("Could not get curve construction configuration");
    }
    final ValueProperties constraints = desiredValues.iterator().next().getConstraints();
    final ValueProperties.Builder builder =  createValueProperties()
        .with(CURVE_CALCULATION_METHOD, NON_LINEAR_LEAST_SQUARE)
        .with(PROPERTY_CURVE_TYPE, NELSON_SIEGEL)
        .with(CURVE_CONSTRUCTION_CONFIG, constraints.getValues(CURVE_CONSTRUCTION_CONFIG));
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
    final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(executionContext);
    final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(executionContext);
    final ConventionSource conventionSource = OpenGammaExecutionContext.getConventionSource(executionContext);
    final LegalEntitySource legalEntitySource = OpenGammaExecutionContext.getLegalEntitySource(executionContext);
    final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());
    final Map<Currency, YieldAndDiscountCurve> discountingCurves = new HashMap<>();
    final Map<IborIndex, YieldAndDiscountCurve> iborCurves = Collections.emptyMap();
    final Map<IndexON, YieldAndDiscountCurve> overnightCurves = Collections.emptyMap();
    final FXMatrix fxMatrix = new FXMatrix();
    final Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> issuerCurves = new HashMap<>();
    final Set<String> curveNames = new HashSet<>();
    final Set<ComputedValue> results = new HashSet<>();
    for (final Map.Entry<String, Object[]> entry : data.entrySet()) {
      final CurveSpecification specification = (CurveSpecification) entry.getValue()[0];
      final SnapshotDataBundle marketData = (SnapshotDataBundle) entry.getValue()[1];
      final String curveName = specification.getName();
      curveNames.add(curveName);
      final int n = specification.getNodes().size();
      final double[] t = new double[n];
      final double[] ytms = new double[n];
      int i = 0;
      for (final CurveNodeWithIdentifier node : specification.getNodes()) {
        final ExternalId dataId = node.getIdentifier();
        final CurveNodeVisitor<InstrumentDefinition<?>> converter = CurveNodeVisitorAdapter.<InstrumentDefinition<?>>builder()
            .billNodeVisitor(new BillNodeConverter(holidaySource, regionSource, securitySource, legalEntitySource, marketData, dataId, now))
            .bondNodeVisitor(new BondNodeConverter(conventionSource, holidaySource, regionSource, securitySource, marketData, dataId, now))
            .create();
        final InstrumentDerivative bondOrBill = node.getCurveNode().accept(converter).toDerivative(now);
        t[i] = bondOrBill.accept(LastTimeCalculator.getInstance());
        ytms[i++] = marketData.getDataPoint(dataId);
      }
      final DoubleMatrix1D initialValues = new DoubleMatrix1D(new double[] {1, 2, 3, 4, 2, 3 });
      final ParameterizedFunction<Double, DoubleMatrix1D, Double> parameterizedFunction = MODEL.getParameterizedFunction();
      final LeastSquareResults result = MINIMISER.solve(new DoubleMatrix1D(t), new DoubleMatrix1D(ytms), parameterizedFunction, initialValues);
      final DoubleMatrix1D parameters = result.getFitParameters();
      final FunctionalDoublesCurve curve = FunctionalDoublesCurve.from(parameterizedFunction.asFunctionOfArguments(parameters));
      final YieldCurve yieldCurve = YieldCurve.from(curve);
      final ValueSpecification spec = new ValueSpecification(YIELD_CURVE, target.toSpecification(), builder.copy().with(CURVE, curveName).get());
      results.add(new ComputedValue(spec, yieldCurve));
      for (final CurveGroupConfiguration group : curveConfig.getCurveGroups()) {
        for (final Map.Entry<String, List<? extends CurveTypeConfiguration>> typeEntry : group.getTypesForCurves().entrySet()) {
          if (typeEntry.getKey().equals(curveName)) {
            for (final CurveTypeConfiguration type : typeEntry.getValue()) {
              if (type instanceof IssuerCurveTypeConfiguration) {
                final IssuerCurveTypeConfiguration issuerType = (IssuerCurveTypeConfiguration) type;
                final Pair<Object, LegalEntityFilter<LegalEntity>> issuerFilter =
                    Pairs.<Object, LegalEntityFilter<LegalEntity>>of(issuerType.getKeys(), issuerType.getFilters());
                issuerCurves.put(issuerFilter, yieldCurve);
              } else if (type instanceof DiscountingCurveTypeConfiguration) {
                final DiscountingCurveTypeConfiguration discountingType = (DiscountingCurveTypeConfiguration) type;
                discountingCurves.put(Currency.of(discountingType.getReference()), yieldCurve);
              } else {
                throw new OpenGammaRuntimeException("Unhandled curve type " + type);
              }
            }
          }
        }
      }
    }
    final IssuerProviderDiscount bundle = new IssuerProviderDiscount(discountingCurves, iborCurves, overnightCurves, issuerCurves, fxMatrix);
    final ValueSpecification bundleSpec = new ValueSpecification(CURVE_BUNDLE, target.toSpecification(), builder.copy().with(CURVE, curveNames).get());
    final DoubleMatrix2D dummyJacobian = new DoubleMatrix2D(1, 1);
    final ValueSpecification jacobianSpec = new ValueSpecification(JACOBIAN_BUNDLE, target.toSpecification(), builder.copy().with(CURVE, curveNames).get());
    results.add(new ComputedValue(bundleSpec, bundle));
    results.add(new ComputedValue(jacobianSpec, dummyJacobian));
    return results;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.NULL;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties =  createValueProperties()
        .with(CURVE_CALCULATION_METHOD, NON_LINEAR_LEAST_SQUARE)
        .with(PROPERTY_CURVE_TYPE, NELSON_SIEGEL)
        .withAny(CURVE_CONSTRUCTION_CONFIG)
        .withAny(CURVE)
        .get();
    final Set<ValueSpecification> results = new HashSet<>();
    results.add(new ValueSpecification(CURVE_BUNDLE, target.toSpecification(), properties));
    results.add(new ValueSpecification(JACOBIAN_BUNDLE, target.toSpecification(), properties));
    results.add(new ValueSpecification(YIELD_CURVE, target.toSpecification(), properties));
    return results;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target,
      final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveConfigName = constraints.getValues(CURVE_CONSTRUCTION_CONFIG);
    if (curveConfigName == null || curveConfigName.isEmpty()) {
      return null;
    }
    final ValueProperties properties = ValueProperties.builder().with(CURVE_CONSTRUCTION_CONFIG, curveConfigName).get();
    return Collections.singleton(new ValueRequirement(CURVE_CONSTRUCTION_CONFIG, ComputationTargetSpecification.NULL, properties));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target,
      final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueSpecification resolved = inputs.entrySet().iterator().next().getKey();
    final String curveConstructionConfig = resolved.getProperty(CURVE_CONSTRUCTION_CONFIG);
    final Set<String> curveNames = resolved.getProperties().getValues(CURVE);
    final ValueProperties.Builder builder =  createValueProperties()
        .with(CURVE_CALCULATION_METHOD, NON_LINEAR_LEAST_SQUARE)
        .with(PROPERTY_CURVE_TYPE, NELSON_SIEGEL)
        .with(CURVE_CONSTRUCTION_CONFIG, curveConstructionConfig);
    final Set<ValueSpecification> results = new HashSet<>();
    results.add(new ValueSpecification(CURVE_BUNDLE, target.toSpecification(), builder.copy().with(CURVE, curveNames).get()));
    results.add(new ValueSpecification(JACOBIAN_BUNDLE, target.toSpecification(), builder.copy().with(CURVE, curveNames).get()));
    for (final String curveName : curveNames) {
      results.add(new ValueSpecification(YIELD_CURVE, target.toSpecification(), builder.copy().with(CURVE, curveName).get()));
    }
    return results;
  }

  @Override
  public Set<ValueRequirement> getAdditionalRequirements(final FunctionCompilationContext context, final ComputationTarget target,
      final Set<ValueSpecification> inputs, final Set<ValueSpecification> outputs) {
    final Set<ValueRequirement> requirements = new HashSet<>();
    final Set<String> curveNames = inputs.iterator().next().getProperties().getValues(CURVE);
    for (final String curveName : curveNames) {
      final ValueProperties properties = ValueProperties.builder().with(CURVE, curveName).get();
      requirements.add(new ValueRequirement(CURVE_SPECIFICATION, ComputationTargetSpecification.NULL, properties));
      requirements.add(new ValueRequirement(CURVE_MARKET_DATA, ComputationTargetSpecification.NULL, properties));
      //TODO get ytm market data
    }
    return requirements;
  }
}
