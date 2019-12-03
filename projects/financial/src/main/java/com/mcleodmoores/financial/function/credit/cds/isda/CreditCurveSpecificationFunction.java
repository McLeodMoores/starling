/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.credit.cds.isda;

import static com.opengamma.engine.value.ValueRequirementNames.CURVE_SPECIFICATION;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.financial.function.credit.configs.CreditCurveSpecification;
import com.mcleodmoores.financial.function.credit.source.ConfigDbCreditCurveSpecificationSource;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.credit.CreditCurveIdentifier;

/**
 * A functions that gets a {@link CreditCurveSpecification}. The function has a {@link CreditCurveIdentifier} target type, so in general there will be one curve
 * per RED code / seniority / currency / restructuring type (if available).
 */
public class CreditCurveSpecificationFunction extends AbstractFunction {
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
   * A compiled function that returns the specification.
   */
  protected class MyCompiledFunction extends AbstractInvokingCompiledFunction {
    private final LocalDate _curveDate;

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
      final CreditCurveSpecification specification = _source.getCreditCurveSpecification((CreditCurveIdentifier) target.getValue(), _curveDate);
      if (specification == null) {
        throw new OpenGammaRuntimeException("Could not get CreditCurveSpecification for " + target.getValue());
      }
      return Collections
          .singleton(new ComputedValue(new ValueSpecification(CURVE_SPECIFICATION, target.toSpecification(), createValueProperties().get()), specification));
    }

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.CREDIT_CURVE_IDENTIFIER;
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      return Collections.singleton(new ValueSpecification(CURVE_SPECIFICATION, target.toSpecification(), createValueProperties().get()));
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target,
        final ValueRequirement desiredValue) {
      return Collections.emptySet();
    }
  }
}
