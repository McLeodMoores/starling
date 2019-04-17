/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.credit.cds.isda;

import static com.opengamma.core.value.MarketDataRequirementNames.MARKET_VALUE;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_MARKET_DATA;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_SPECIFICATION;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.financial.function.credit.configs.CreditCurveDefinition;
import com.mcleodmoores.financial.function.credit.configs.CreditCurveSpecification;
import com.mcleodmoores.financial.function.credit.source.ConfigDbCreditCurveSpecificationSource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.marketdata.ExternalIdBundleResolver;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.credit.CreditCurveIdentifier;

/**
 * A function that retrieves market data for {@link CreditCurveDefinition}s. The function has a {@link CreditCurveIdentifier} target type, so in general there
 * will be one curve per RED code / seniority / currency / restructuring type (if available).
 */
public class CreditCurveMarketDataFunction extends AbstractFunction {
  private static final Logger LOGGER = LoggerFactory.getLogger(CreditCurveMarketDataFunction.class);
  private ConfigDbCreditCurveSpecificationSource _source;

  @Override
  public void init(final FunctionCompilationContext context) {
    _source = ConfigDbCreditCurveSpecificationSource.init(context, this);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    return new MyCompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000), atZDT.toLocalDate());
  }

  /**
   * A compiled function that returns the curve market data.
   */
  protected class MyCompiledFunction extends AbstractInvokingCompiledFunction {
    private final LocalDate _curveDate;
    private CreditCurveSpecification _specification;

    /**
     * @param earliestInvocation
     *          the earliest time for which this function is valid
     * @param latestInvocation
     *          the latest time for which this function is valid
     * @param curveDate
     *          the curve date
     */
    public MyCompiledFunction(final ZonedDateTime earliestInvocation, final ZonedDateTime latestInvocation, final LocalDate curveDate) {
      super(earliestInvocation, latestInvocation);
      _curveDate = curveDate;
    }

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
        final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
      final Collection<CurveNodeWithIdentifier> nodes = _specification.getNodes();
      final ExternalIdBundleResolver resolver = new ExternalIdBundleResolver(executionContext.getComputationTargetResolver());
      final SnapshotDataBundle marketData = new SnapshotDataBundle();
      for (final CurveNodeWithIdentifier id : nodes) {
        final ComputedValue value;
        if (id.getDataField() != null) {
          value = inputs.getComputedValue(new ValueRequirement(id.getDataField(), ComputationTargetType.PRIMITIVE, id.getIdentifier()));
        } else {
          value = inputs.getComputedValue(new ValueRequirement(MARKET_VALUE, ComputationTargetType.PRIMITIVE, id.getIdentifier()));
        }
        if (value != null) {
          final ExternalIdBundle identifiers = value.getSpecification().getTargetSpecification().accept(resolver);
          marketData.setDataPoint(identifiers, (Double) value.getValue());
        } else {
          LOGGER.info("Could not get market data for {}", id);
        }
      }
      final Set<ComputedValue> results = new HashSet<>();
      results.add(new ComputedValue(new ValueSpecification(CURVE_SPECIFICATION, target.toSpecification(), createValueProperties().get()), _specification));
      results.add(new ComputedValue(new ValueSpecification(CURVE_MARKET_DATA, target.toSpecification(), createValueProperties().get()), marketData));
      return results;
    }

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.CREDIT_CURVE_IDENTIFIER;
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
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      final Set<ValueSpecification> results = new HashSet<>();
      results.add(new ValueSpecification(CURVE_MARKET_DATA, target.toSpecification(), createValueProperties().get()));
      results.add(new ValueSpecification(CURVE_SPECIFICATION, target.toSpecification(), createValueProperties().get()));
      return results;
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target,
        final ValueRequirement desiredValue) {
      _specification = _source.getCreditCurveSpecification((CreditCurveIdentifier) target.getValue(), _curveDate);
      if (_specification == null) {
        LOGGER.warn("Could not get CreditCurveSpecification for " + target.getValue());
        return null;
      }
      final Set<ValueRequirement> requirements = new HashSet<>();
      for (final CurveNodeWithIdentifier node : _specification.getNodes()) {
        requirements.add(new ValueRequirement(node.getDataField(), ComputationTargetType.PRIMITIVE, node.getIdentifier()));
      }
      return requirements;
    }
  }
}
