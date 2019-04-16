/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.credit.cds.isda;

import static com.mcleodmoores.financial.function.properties.CurveCalculationProperties.ISDA;
import static com.opengamma.core.value.MarketDataRequirementNames.MARKET_VALUE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_METHOD;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_CONSTRUCTION_CONFIG;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_MARKET_DATA;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_SPECIFICATION;
import static com.opengamma.engine.value.ValueRequirementNames.HAZARD_RATE_CURVE;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;

import java.util.HashSet;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.isdastandardmodel.FastCreditCurveBuilder;
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
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.model.credit.CreditSecurityToIdentifierVisitor;
import com.opengamma.financial.credit.CdsRecoveryRateIdentifier;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.credit.StandardCDSSecurity;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.credit.CreditCurveIdentifier;

/**
 * A function that creates a credit curve using the ISDA methodology ({@link FastCreditCurveBuilder}).
 */
public class IsdaCreditCurveFunction extends AbstractFunction.NonCompiledInvoker {
  private CreditSecurityToIdentifierVisitor _cdsIdentifierVisitor;

  @Override
  public void init(final FunctionCompilationContext context) {
    _cdsIdentifierVisitor = new CreditSecurityToIdentifierVisitor(OpenGammaCompilationContext.getSecuritySource(context));
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final StandardCDSSecurity cds = (StandardCDSSecurity) target.getSecurity();
    final CreditCurveIdentifier cdsId = cds.accept(_cdsIdentifierVisitor);
    final Double recoveryRate = (Double) inputs.getValue(MARKET_VALUE);
    if (recoveryRate == null) {
      final CdsRecoveryRateIdentifier recoveryRateId = CdsRecoveryRateIdentifier.forSamedayCds(cdsId.getRedCode(), cdsId.getCurrency(), cdsId.getSeniority());
      throw new OpenGammaRuntimeException("Could not get recovery rate for " + target.getSecurity() + ": tried " + recoveryRateId);
    }
    return null;
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
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.STANDARD_CDS_SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final Set<ValueSpecification> results = new HashSet<>();
    final ValueProperties properties = createValueProperties().with(CURVE_CALCULATION_METHOD, ISDA).withAny(CURVE_CONSTRUCTION_CONFIG).get();
    results.add(new ValueSpecification(HAZARD_RATE_CURVE, target.toSpecification(), properties));
    return results;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final String config = desiredValue.getConstraint(CURVE_CONSTRUCTION_CONFIG);
    if (config == null) {
      return null;
    }
    final StandardCDSSecurity cds = (StandardCDSSecurity) target.getSecurity();
    final CreditCurveIdentifier cdsId = cds.accept(_cdsIdentifierVisitor);
    final CdsRecoveryRateIdentifier recoveryRateId = CdsRecoveryRateIdentifier.forSamedayCds(cdsId.getRedCode(), cdsId.getCurrency(), cdsId.getSeniority());
    final Set<ValueRequirement> requirements = new HashSet<>();
    final ValueProperties curveProperties = ValueProperties.builder().with(CURVE_CONSTRUCTION_CONFIG, config).with(PROPERTY_CURVE_TYPE, ISDA).get();
    requirements.add(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties));
    requirements.add(new ValueRequirement(CURVE_SPECIFICATION, ComputationTargetSpecification.of(cdsId), ValueProperties.none()));
    requirements.add(new ValueRequirement(CURVE_MARKET_DATA, ComputationTargetSpecification.of(cdsId), ValueProperties.none()));
    requirements.add(new ValueRequirement(MARKET_VALUE, ComputationTargetType.PRIMITIVE, recoveryRateId.getExternalId()));
    return requirements;
  }

}
