/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * 
 */
public class BondZSpreadPresentValueSensitivityFromCurveCleanPriceFunction extends BondZSpreadPresentValueSensitivityFunction {

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String curveName = null;
    String riskFreeCurveName = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> input : inputs.entrySet()) {
      if (ValueRequirementNames.YIELD_CURVE.equals(input.getKey().getValueName())) {
        curveName = input.getKey().getProperty(ValuePropertyNames.CURVE);
      } else if (ValueRequirementNames.CLEAN_PRICE.equals(input.getKey().getValueName())) {
        riskFreeCurveName = input.getKey().getProperty(BondFunction.PROPERTY_RISK_FREE_CURVE);
      }
    }
    assert curveName != null;
    assert riskFreeCurveName != null;
    final String creditCurveName = riskFreeCurveName;
    final ValueProperties.Builder properties = getResultProperties(riskFreeCurveName, creditCurveName, curveName);
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_Z_SPREAD_SENSITIVITY, target.toSpecification(), properties.get()));
  }

  @Override
  protected ValueRequirement getCleanPriceRequirement(final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> riskFreeCurves = desiredValue.getConstraints().getValues(BondFunction.PROPERTY_RISK_FREE_CURVE);
    if (riskFreeCurves == null || riskFreeCurves.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique risk-free curve name");
    }
    final Set<String> creditCurves = desiredValue.getConstraints().getValues(BondFunction.PROPERTY_CREDIT_CURVE);
    if (creditCurves == null || creditCurves.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique credit curve name");
    }
    final String riskFreeCurveName = riskFreeCurves.iterator().next();
    final String creditCurveName = creditCurves.iterator().next();
    final ValueProperties.Builder properties = ValueProperties.builder()
        .with(BondFunction.PROPERTY_RISK_FREE_CURVE, riskFreeCurveName)
        .with(BondFunction.PROPERTY_CREDIT_CURVE, creditCurveName)
        .with(ValuePropertyNames.CALCULATION_METHOD, BondFunction.FROM_CURVES_METHOD);
    return new ValueRequirement(ValueRequirementNames.CLEAN_PRICE, target.toSpecification(), properties.get());
  }

  @Override
  protected String getCalculationMethodName() {
    return BondFunction.FROM_CURVES_METHOD;
  }
}
