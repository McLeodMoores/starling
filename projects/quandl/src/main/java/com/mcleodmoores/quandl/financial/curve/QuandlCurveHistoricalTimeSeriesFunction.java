/**
 * Copyright (C) 2015 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.financial.curve;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.link.ConventionLink;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.curve.CurveSpecification;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.PointsCurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.ZeroCouponInflationNode;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.convention.FederalFundsFutureConvention;
import com.opengamma.financial.convention.InflationLegConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.security.index.PriceIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Function to source time series data for each of the instruments in a {@link CurveSpecification} from a {@link HistoricalTimeSeriesSource} attached to the
 * execution context.
 */
public class QuandlCurveHistoricalTimeSeriesFunction extends AbstractFunction.NonCompiledInvoker {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(QuandlCurveHistoricalTimeSeriesFunction.class);

  private static String parseString(final String str) {
    if (str.length() == 0) {
      return null;
    }
    return str;
  }

  private static boolean parseBoolean(final String str) {
    return HistoricalTimeSeriesFunctionUtils.YES_VALUE.equals(str);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final HistoricalTimeSeriesSource timeSeriesSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String resolutionKey = parseString(desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.RESOLUTION_KEY_PROPERTY));
    final LocalDate startDate = DateConstraint.evaluate(executionContext, desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY));
    final boolean includeStart = parseBoolean(desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY));
    final LocalDate endDate = DateConstraint.evaluate(executionContext, desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY));
    final boolean includeEnd = parseBoolean(desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY));
    final CurveSpecification curve = (CurveSpecification) inputs.getAllValues().iterator().next().getValue();
    final HistoricalTimeSeriesBundle bundle = new HistoricalTimeSeriesBundle();
    for (final CurveNodeWithIdentifier node : curve.getNodes()) {
      ExternalIdBundle id = ExternalIdBundle.of(node.getIdentifier());
      String dataField = node.getDataField();
      HistoricalTimeSeries timeSeries = timeSeriesSource.getHistoricalTimeSeries(dataField, id, resolutionKey, startDate, includeStart, endDate, includeEnd);
      if (timeSeries != null) {
        if (timeSeries.getTimeSeries().isEmpty()) {
          LOGGER.info("Time series for {} is empty", id);
        } else {
          bundle.add(dataField, id, timeSeries);
        }
      } else {
        LOGGER.info("Couldn't get time series for {}", id);
      }
      if (node instanceof PointsCurveNodeWithIdentifier) {
        final PointsCurveNodeWithIdentifier pointsNode = (PointsCurveNodeWithIdentifier) node;
        id = ExternalIdBundle.of(pointsNode.getUnderlyingIdentifier());
        dataField = pointsNode.getUnderlyingDataField();
        timeSeries = timeSeriesSource.getHistoricalTimeSeries(dataField, id, resolutionKey, startDate, includeStart, endDate, includeEnd);
        if (timeSeries != null) {
          if (timeSeries.getTimeSeries().isEmpty()) {
            LOGGER.info("Time series for {} is empty", id);
          } else {
            bundle.add(dataField, id, timeSeries);
          }
        } else {
          LOGGER.info("Couldn't get time series for {}", id);
        }
      } else if (node instanceof QuandlCurveNodeWithIdentifierAndUnderlying) {
        final QuandlCurveNodeWithIdentifierAndUnderlying nodeWithUnderlying = (QuandlCurveNodeWithIdentifierAndUnderlying) node;
        id = ExternalIdBundle.of(nodeWithUnderlying.getUnderlyingIdentifier());
        dataField = nodeWithUnderlying.getUnderlyingDataField();
        timeSeries = timeSeriesSource.getHistoricalTimeSeries(dataField, id, resolutionKey, startDate, includeStart, endDate, includeEnd);
        if (timeSeries != null) {
          if (timeSeries.getTimeSeries().isEmpty()) {
            LOGGER.info("Time series for {} is empty", id);
          } else {
            bundle.add(dataField, id, timeSeries);
          }
        } else {
          LOGGER.info("Couldn't get time series for {}", id);
        }
      }
      // TODO this should all be replaced with a visitor once the same method adapter is available
      if (node.getCurveNode() instanceof ZeroCouponInflationNode) {
        final ZeroCouponInflationNode inflationNode = (ZeroCouponInflationNode) node.getCurveNode();
        final ConventionSource conventionSource = OpenGammaExecutionContext.getConventionSource(executionContext);
        final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(executionContext);
        final InflationLegConvention inflationLegConvention = conventionSource.getSingle(inflationNode.getInflationLegConvention(),
            InflationLegConvention.class);
        final Security sec = securitySource.getSingle(inflationLegConvention.getPriceIndexConvention().toBundle());
        if (sec == null) {
          throw new OpenGammaRuntimeException("Index with id " + inflationLegConvention.getPriceIndexConvention() + " was null");
        }
        if (!(sec instanceof PriceIndex)) {
          throw new OpenGammaRuntimeException("Index with id " + inflationLegConvention.getPriceIndexConvention() + " not of type PriceIndex");
        }
        final PriceIndex indexSecurity = (PriceIndex) sec;
        final String priceIndexField = MarketDataRequirementNames.MARKET_VALUE; // TODO
        final HistoricalTimeSeries priceIndexSeries = timeSeriesSource.getHistoricalTimeSeries(priceIndexField, indexSecurity.getExternalIdBundle(),
            resolutionKey, startDate, includeStart, endDate, true);
        if (priceIndexSeries != null) {
          if (priceIndexSeries.getTimeSeries().isEmpty()) {
            LOGGER.info("Time series for {} is empty", indexSecurity.getExternalIdBundle());
          } else {
            bundle.add(dataField, indexSecurity.getExternalIdBundle(), priceIndexSeries);
          }
        } else {
          LOGGER.info("Couldn't get time series for {}", indexSecurity.getExternalIdBundle());
        }
      }
      // Fixing series are required for Fed Fund futures: underlying overnight index fixing (when fixing month has started)
      if (node.getCurveNode() instanceof RateFutureNode) { // Start Fed Fund futures
        // TODO all of this code will throw an exception if the convention is not available
        final RateFutureNode nodeRateFut = (RateFutureNode) node.getCurveNode();
        if (!(node instanceof QuandlCurveNodeWithIdentifierAndUnderlying)) {
          LOGGER.warn("Curve node with identifier for Fed funds future did not contain information about the underlying: using Market_Value "
              + "and the identifier from the convention");
          final Convention conventionRateFut = ConventionLink.resolvable(nodeRateFut.getFutureConvention(), Convention.class).resolve();
          if (conventionRateFut instanceof FederalFundsFutureConvention) {
            final FederalFundsFutureConvention conventionFedFundFut = (FederalFundsFutureConvention) conventionRateFut;
            final ExternalId onIndexConventionId = conventionFedFundFut.getIndexConvention();
            final String onIndexField = MarketDataRequirementNames.MARKET_VALUE;
            HistoricalTimeSeries onIndexSeries = timeSeriesSource.getHistoricalTimeSeries(onIndexField, onIndexConventionId.toBundle(),
                resolutionKey, startDate, includeStart, endDate, true);
            if (onIndexSeries != null) {
              if (onIndexSeries.getTimeSeries().isEmpty()) {
                LOGGER.info("Time series for {} is empty", onIndexConventionId);
              } else {
                bundle.add(dataField, onIndexConventionId.toBundle(), onIndexSeries);
              }
            } else {
              LOGGER.info("Couldn't get time series for {}, trying identifiers from index convention", onIndexConventionId);
              final OvernightIndexConvention overnightIndexConvention = ConventionLink
                  .resolvable(conventionFedFundFut.getIndexConvention(), OvernightIndexConvention.class).resolve();
              // try the external ids of the underlying convention in case the convention id used in the Fed funds convention
              // is not the overnight index ticker
              onIndexSeries = timeSeriesSource.getHistoricalTimeSeries(onIndexField, overnightIndexConvention.getExternalIdBundle(),
                  resolutionKey, startDate, includeStart, endDate, true);
              if (onIndexSeries != null) {
                if (onIndexSeries.getTimeSeries().isEmpty()) {
                  LOGGER.info("Time series for {} is empty", onIndexConventionId);
                } else {
                  bundle.add(dataField, onIndexConventionId.toBundle(), onIndexSeries);
                }
              }
            }
          }
        }
      } // End Fed Fund futures
    }
    return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.CURVE_HISTORICAL_TIME_SERIES, target.toSpecification(),
        desiredValue.getConstraints()), bundle));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.NULL;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .withAny(ValuePropertyNames.CURVE)
        .withAny(HistoricalTimeSeriesFunctionUtils.RESOLUTION_KEY_PROPERTY)
        .withAny(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY)
        .with(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE, HistoricalTimeSeriesFunctionUtils.NO_VALUE)
        .withAny(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY)
        .with(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE, HistoricalTimeSeriesFunctionUtils.NO_VALUE)
        .get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.CURVE_HISTORICAL_TIME_SERIES, ComputationTargetSpecification.NULL, properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    ValueProperties.Builder constraints = null;
    Set<String> values = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.RESOLUTION_KEY_PROPERTY);
    if (values == null || values.isEmpty()) {
      constraints = desiredValue.getConstraints().copy().with(HistoricalTimeSeriesFunctionUtils.RESOLUTION_KEY_PROPERTY, "");
    } else if (values.size() > 1) {
      constraints = desiredValue.getConstraints().copy().withoutAny(HistoricalTimeSeriesFunctionUtils.RESOLUTION_KEY_PROPERTY)
          .with(HistoricalTimeSeriesFunctionUtils.RESOLUTION_KEY_PROPERTY, values.iterator().next());
    }
    values = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY);
    if (values == null || values.isEmpty()) {
      if (constraints == null) {
        constraints = desiredValue.getConstraints().copy();
      }
      constraints.with(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY, "Null");
    }
    values = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY);
    if (values == null || values.size() != 1) {
      if (constraints == null) {
        constraints = desiredValue.getConstraints().copy();
      }
      constraints.with(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE);
    }
    values = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY);
    if (values == null || values.isEmpty()) {
      if (constraints == null) {
        constraints = desiredValue.getConstraints().copy();
      }
      constraints.with(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY, "Now");
    }
    values = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY);
    if (values == null || values.size() != 1) {
      if (constraints == null) {
        constraints = desiredValue.getConstraints().copy();
      }
      constraints.with(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE);
    }
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    if (constraints == null) {
      // We can satisfy the desired value as-is, just ask for the curve specification to drive our behavior
      final ValueProperties curveConstraints;
      values = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE);
      if (values != null) {
        if (values.isEmpty()) {
          curveConstraints = ValueProperties.withAny(ValuePropertyNames.CURVE).get();
        } else {
          curveConstraints = ValueProperties.with(ValuePropertyNames.CURVE, values).get();
        }
      } else {
        curveConstraints = ValueProperties.none();
      }
      return Collections.singleton(new ValueRequirement(ValueRequirementNames.CURVE_SPECIFICATION, targetSpec, curveConstraints));
    }
    // We need to substitute ourselves with the adjusted constraints
    return Collections.singleton(new ValueRequirement(ValueRequirementNames.CURVE_HISTORICAL_TIME_SERIES, targetSpec, constraints.get()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target,
      final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueSpecification input = inputs.keySet().iterator().next();
    if (ValueRequirementNames.CURVE_HISTORICAL_TIME_SERIES.equals(input.getValueName())) {
      // Use the substituted result
      return Collections.singleton(input);
    }
    // Use full results - graph builder will compose correctly against the desired value
    return getResults(context, target);
  }

}
