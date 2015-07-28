/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.model.bondcurves;

import static com.opengamma.engine.value.ValueRequirementNames.ALL_PV01S;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.discounting.PV01CurveParametersCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueCurveSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.BondAndBondFutureFunctionUtils;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Calculates the PV01 of a bond or bond future for all curves to which the instruments are sensitive.
 */
public class BondAndBondFutureAllPv01Function extends BondAndBondFutureFromCurvesFunction<ParameterIssuerProviderInterface, ReferenceAmount<Pair<String, Currency>>> {
  /** The PV01 calculator */
  private static final InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, ReferenceAmount<Pair<String, Currency>>> CALCULATOR =
      new PV01CurveParametersCalculator<>(PresentValueCurveSensitivityIssuerCalculator.getInstance());

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#ALL_PV01S} and
   * sets the calculator to {@link PV01CurveParametersCalculator}
   */
  public BondAndBondFutureAllPv01Function() {
    super(ALL_PV01S, CALCULATOR);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext context, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final ValueProperties properties = desiredValue.getConstraints();
    final ZonedDateTime now = ZonedDateTime.now(context.getValuationClock());
    final InstrumentDerivative derivative = BondAndBondFutureFunctionUtils.getBondOrBondFutureDerivative(context, target, now, inputs);
    final ParameterIssuerProviderInterface issuerCurves = (ParameterIssuerProviderInterface) inputs.getValue(CURVE_BUNDLE);
    final ReferenceAmount<Pair<String, Currency>> pv01 = derivative.accept(CALCULATOR, issuerCurves);
    final ValueSpecification spec = new ValueSpecification(ALL_PV01S, target.toSpecification(), properties.copy().get());
    return Collections.singleton(new ComputedValue(spec, pv01.getMap()));
  }
}