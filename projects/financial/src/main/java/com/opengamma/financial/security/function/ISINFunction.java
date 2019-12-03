/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * <p>
 * Please see distribution for license.
 */
package com.opengamma.financial.security.function;

import static com.google.common.collect.Sets.newHashSet;
import static com.opengamma.engine.function.dsl.Function.function;
import static com.opengamma.engine.function.dsl.Function.output;
import static com.opengamma.engine.function.dsl.TargetSpecificationReference.originalTarget;
import static com.opengamma.engine.value.ValueRequirementNames.ISIN;
import static com.opengamma.lambdava.streams.Lambdava.functional;

import java.util.Set;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.dsl.FunctionSignature;
import com.opengamma.engine.function.dsl.functions.BaseNonCompiledInvoker;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * If attached to security's ExternalIdBundle, displays its {@link ExternalSchemes#BLOOMBERG_TICKER}.
 */
public class ISINFunction extends BaseNonCompiledInvoker {

  @Override
  protected FunctionSignature functionSignature() {

    return function("ISINFunction", ComputationTargetType.POSITION_OR_TRADE)
        .outputs(
            output(ISIN)
                .targetSpec(originalTarget())
                .properties(ValueProperties.all()))
        .inputs();
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext,
      final FunctionInputs inputs,
      final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {

    final ValueRequirement desiredValue = functional(desiredValues).first();
    final ValueSpecification valueSpecification = ValueSpecification.of(desiredValue.getValueName(),
        target.toSpecification(),
        desiredValue.getConstraints());

    final Security security = target.getPositionOrTrade().getSecurity();
    if (security != null) {
      final ExternalIdBundle externalIdBundle = security.getExternalIdBundle();
      if (externalIdBundle != null) {
        final ExternalId externalId = externalIdBundle.getExternalId(ExternalSchemes.ISIN);
        if (externalId != null) {
          return newHashSet(new ComputedValue(valueSpecification, externalId.getValue()));
        }
      }
    }
    return newHashSet(new ComputedValue(valueSpecification, ""));

  }

  @Override
  public String getUniqueId() {
    return "ISIN";
  }
}
