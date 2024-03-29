/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondcurves;

import static com.opengamma.engine.value.ValueRequirementNames.BLOCK_CURVE_SENSITIVITIES;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.JACOBIAN_BUNDLE;

import java.util.HashSet;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueCurveSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.BondAndBondFutureFunctionUtils;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Calculates the sensitivities to the market quotes of a bond or bond future for all curves
 * to which the instruments are sensitive.
 */
public class BondAndBondFutureBCSFunction extends BondAndBondFutureFromCurvesFunction<ParameterIssuerProviderInterface, MultipleCurrencyMulticurveSensitivity> {
  /** The curve sensitivity calculator */
  private static final InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, MultipleCurrencyMulticurveSensitivity> PVCSDC =
      PresentValueCurveSensitivityIssuerCalculator.getInstance();
  /** The parameter sensitivity calculator */
  private static final ParameterSensitivityParameterCalculator<ParameterIssuerProviderInterface> PSC =
      new ParameterSensitivityParameterCalculator<>(PVCSDC);
  /** The market quote sensitivity calculator */
  private static final MarketQuoteSensitivityBlockCalculator<ParameterIssuerProviderInterface> CALCULATOR =
      new MarketQuoteSensitivityBlockCalculator<>(PSC);

  /**
   * Sets the value requirement name to
   * {@link com.opengamma.engine.value.ValueRequirementNames#BLOCK_CURVE_SENSITIVITIES}
   * and sets the calculator to null.
   */
  public BondAndBondFutureBCSFunction() {
    super(BLOCK_CURVE_SENSITIVITIES, null);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext context, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ZonedDateTime now = ZonedDateTime.now(context.getValuationClock());
    final InstrumentDerivative derivative = BondAndBondFutureFunctionUtils.getBondOrBondFutureDerivative(context, target, now, inputs);
    final ParameterIssuerProviderInterface issuerCurves = (ParameterIssuerProviderInterface) inputs.getValue(CURVE_BUNDLE);
    final CurveBuildingBlockBundle blocks = (CurveBuildingBlockBundle) inputs.getValue(JACOBIAN_BUNDLE);
    final Set<ComputedValue> result = new HashSet<>();
    final MultipleCurrencyParameterSensitivity sensitivities = CALCULATOR.fromInstrument(derivative, issuerCurves, blocks);
    for (final ValueRequirement desiredValue : desiredValues) {
      final ValueSpecification spec = new ValueSpecification(BLOCK_CURVE_SENSITIVITIES, target.toSpecification(), desiredValue.getConstraints().copy().get());
      result.add(new ComputedValue(spec, sensitivities));
    }
    return result;
  }

}
