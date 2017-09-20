/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.bond;

import static com.opengamma.engine.value.ValuePropertyNames.CALCULATION_METHOD;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValueRequirementNames.MACAULAY_DURATION;
import static com.opengamma.engine.value.ValueRequirementNames.MODIFIED_DURATION;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
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
import com.opengamma.id.UniqueId;
import com.opengamma.util.async.AsynchronousExecution;

/**
 *
 */
public class PortfolioWeightedDurationFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final Map<UniqueId, Double[]> dataByTrade = new HashMap<>();
    double totalPv = 0;
    for (final ComputedValue input : inputs.getAllValues()) {
      final ValueSpecification specification = input.getSpecification();
      final ComputationTargetSpecification targetSpec = specification.getTargetSpecification();
      if (targetSpec.getType() != ComputationTargetType.TRADE) {
        continue;
      }
      final String valueName = specification.getValueName();
      final UniqueId uid = targetSpec.getUniqueId();
      if (!dataByTrade.containsKey(uid)) {
        dataByTrade.put(uid, new Double[3]);
      }
      final Double[] data = dataByTrade.get(uid);
      switch (valueName) {
        case PRESENT_VALUE:
          final Double pv = (Double) input.getValue();
          if (data[0] != null) {
            throw new OpenGammaRuntimeException("Already have a present value for " + specification);
          }
          data[0] = pv;
          totalPv += pv;
          break;
        case MODIFIED_DURATION:
          final Double modified = (Double) input.getValue();
          if (data[1] != null) {
            throw new OpenGammaRuntimeException("Already have a modified duration for " + specification);
          }
          data[1] = modified;
          break;
        case MACAULAY_DURATION:
          final Double macaulay = (Double) input.getValue();
          if (data[2] != null) {
            throw new OpenGammaRuntimeException("Already have a Macaulay duration for " + specification);
          }
          data[2] = macaulay;
          break;
        default:
          throw new OpenGammaRuntimeException("Unrecognised input " + valueName);
      }
    }
    double weightedModifiedDuration = 0;
    double weightedMacaulayDuration = 0;
    for (final Map.Entry<UniqueId, Double[]> entry : dataByTrade.entrySet()) {
      final Double pv = entry.getValue()[0];
      if (pv == null) {
        throw new OpenGammaRuntimeException("Could not get present value for " + entry.getKey());
      }
      final Double modified = entry.getValue()[1];
      if (modified == null) {
        throw new OpenGammaRuntimeException("Could not get modified duration for " + entry.getKey());
      }
      final Double macaulay = entry.getValue()[2];
      if (macaulay == null) {
        throw new OpenGammaRuntimeException("Could not get Macaulay duration for " + entry.getKey());
      }
      weightedModifiedDuration += modified * pv / totalPv;
      weightedMacaulayDuration += macaulay * pv / totalPv;
    }
    final Set<ComputedValue> results = new HashSet<>();
    final ValueProperties properties = desiredValues.iterator().next().getConstraints().copy().get();
    results.add(new ComputedValue(new ValueSpecification(MODIFIED_DURATION, target.toSpecification(), properties), weightedModifiedDuration));
    results.add(new ComputedValue(new ValueSpecification(MACAULAY_DURATION, target.toSpecification(), properties), weightedMacaulayDuration));
    return results;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PORTFOLIO.or(ComputationTargetType.PORTFOLIO_NODE);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .withAny(CALCULATION_METHOD)
        .withAny(CURVE_EXPOSURES)
        .withAny(PROPERTY_CURVE_TYPE)
        .get();
    final Set<ValueSpecification> results = new HashSet<>();
    results.add(new ValueSpecification(MODIFIED_DURATION, target.toSpecification(), properties));
    results.add(new ValueSpecification(MACAULAY_DURATION, target.toSpecification(), properties));
    return results;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<ValueRequirement> requirements = new HashSet<>();
    if (target.getType() == ComputationTargetType.PORTFOLIO_NODE || target.getType() == ComputationTargetType.PORTFOLIO) {
      final PortfolioNode portfolio = target.getPortfolioNode();
      for (final Position position : portfolio.getPositions()) {
        for (final Trade trade : position.getTrades()) {
          requirements.add(new ValueRequirement(MACAULAY_DURATION, ComputationTargetSpecification.of(trade), constraints));
          requirements.add(new ValueRequirement(MODIFIED_DURATION, ComputationTargetSpecification.of(trade), constraints));
          requirements.add(new ValueRequirement(PRESENT_VALUE, ComputationTargetSpecification.of(trade), constraints));
        }
      }
    } else {
      return null;
    }
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target,
      final Map<ValueSpecification, ValueRequirement> inputs) {
    String calculationMethod = null;
    String curveExposures = null;
    String curveType = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueSpecification resolved = entry.getKey();
      if (calculationMethod == null) {
        calculationMethod = resolved.getProperty(CALCULATION_METHOD);
        curveExposures = resolved.getProperty(CURVE_EXPOSURES);
        curveType = resolved.getProperty(PROPERTY_CURVE_TYPE);
      } else {
        if (!resolved.getProperty(CALCULATION_METHOD).equals(calculationMethod)) {
          return null;
        }
        if (!resolved.getProperty(CURVE_EXPOSURES).equals(curveExposures)) {
          return null;
        }
        if (!resolved.getProperty(PROPERTY_CURVE_TYPE).equals(curveType)) {
          return null;
        }
      }
    }
    if (calculationMethod == null) {
      return null;
    }
    final ValueProperties properties = createValueProperties()
        .with(CALCULATION_METHOD, calculationMethod)
        .with(CURVE_EXPOSURES, curveExposures)
        .with(PROPERTY_CURVE_TYPE, curveType)
        .get();
    final Set<ValueSpecification> results = new HashSet<>();
    results.add(new ValueSpecification(MODIFIED_DURATION, target.toSpecification(), properties));
    results.add(new ValueSpecification(MACAULAY_DURATION, target.toSpecification(), properties));
    return results;
  }
}
