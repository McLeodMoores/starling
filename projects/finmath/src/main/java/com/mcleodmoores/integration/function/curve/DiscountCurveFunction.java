/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.function.curve;

import static com.mcleodmoores.integration.ValuePropertyNamesAndValues.FINMATH_CALCULATION_METHOD;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_DEFINITION;
import static com.opengamma.engine.value.ValueRequirementNames.DISCOUNT_CURVE;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.DISCOUNTING;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.finmath.marketdata.model.curves.Curve.ExtrapolationMethod;
import net.finmath.marketdata.model.curves.Curve.InterpolationEntity;
import net.finmath.marketdata.model.curves.Curve.InterpolationMethod;
import net.finmath.marketdata.model.curves.DiscountCurve;

import org.apache.commons.lang.NotImplementedException;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.curve.AbstractCurveDefinition;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.util.async.AsynchronousExecution;

/**
 *
 */
public class DiscountCurveFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final ValueProperties properties = desiredValue.getConstraints().copy().get();
    final YieldCurve yieldCurve = (YieldCurve) inputs.getValue(YIELD_CURVE);
    final AbstractCurveDefinition curveDefinition = (AbstractCurveDefinition) inputs.getValue(CURVE_DEFINITION);
    final DoublesCurve curve = yieldCurve.getCurve();
    if (!(curve instanceof InterpolatedDoublesCurve)) {
      throw new NotImplementedException("TODO");
    }
    if (!(curveDefinition instanceof InterpolatedCurveDefinition)) {
      throw new NotImplementedException("TODO");
    }
    final InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) curve;
    final InterpolatedCurveDefinition interpolatedDefinition = (InterpolatedCurveDefinition) curveDefinition;
    final int n = interpolatedCurve.size();
    final boolean[] isParameter = new boolean[n];
    Arrays.fill(isParameter, true);
    final String name = properties.getSingleValue(CURVE);
    final String interpolatorName = interpolatedDefinition.getInterpolatorName();
    final String extrapolatorName = interpolatedDefinition.getLeftExtrapolatorName(); //TODO make sure left and right are the same
    final DiscountCurve discountCurve = DiscountCurve.createDiscountCurveFromZeroRates(name, interpolatedCurve.getXDataAsPrimitive(),
        interpolatedCurve.getYDataAsPrimitive(), isParameter, InterpolationMethod.valueOf(interpolatorName), ExtrapolationMethod.valueOf(extrapolatorName),
        InterpolationEntity.VALUE); //TODO interpolation entity
    final ValueSpecification spec = new ValueSpecification(DISCOUNT_CURVE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, discountCurve));
  }

  @Override
  public boolean canHandleMissingInputs() {
    return true;
  }

  @Override
  public boolean canHandleMissingRequirements() {
    return true;
  }
  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.NULL;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .withAny(CURVE)
        .with(PROPERTY_CURVE_TYPE, FINMATH_CALCULATION_METHOD)
        .get();
    return Collections.singleton(new ValueSpecification(DISCOUNT_CURVE, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveName = constraints.getValues(CURVE);
    if (curveName == null || curveName.size() != 1) {
      return null;
    }
    final ValueProperties yieldCurveProperties = ValueProperties.builder()
        .with(CURVE, curveName)
        .with(PROPERTY_CURVE_TYPE, DISCOUNTING)
        .get();
    final ValueProperties definitionProperties = ValueProperties.builder()
        .with(CURVE, curveName)
        .get();
    final Set<ValueRequirement> requirements = new HashSet<>();
    requirements.add(new ValueRequirement(YIELD_CURVE, target.toSpecification(), yieldCurveProperties));
    requirements.add(new ValueRequirement(CURVE_DEFINITION, target.toSpecification(), definitionProperties));
    return requirements;
  }

}
