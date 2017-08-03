/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.discounting;

import static com.opengamma.engine.value.ValueRequirementNames.FX_PRESENT_VALUE;
import static com.opengamma.engine.value.ValueRequirementNames.PAY_DISCOUNT_FACTOR;
import static com.opengamma.engine.value.ValueRequirementNames.PAY_ZERO_RATE;
import static com.opengamma.engine.value.ValueRequirementNames.RECEIVE_DISCOUNT_FACTOR;
import static com.opengamma.engine.value.ValueRequirementNames.RECEIVE_ZERO_RATE;

import java.util.HashSet;
import java.util.Set;

import org.threeten.bp.Instant;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.forex.FXUtils;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the FX present value of instruments using curves constructed using
 * the discounting method and returns the values used in pricing.
 */
public class DiscountingFXPVFunction extends DiscountingFunction {
  /** The present value calculator. */
  static final InstrumentDerivativeVisitor<MulticurveProviderInterface, MultipleCurrencyAmount> CALCULATOR =
      PresentValueDiscountingCalculator.getInstance();

  /**
   * Sets the value requirements to {@link ValueRequirementNames#FX_PRESENT_VALUE},
   * {@link ValueRequirementNames#PAY_DISCOUNT_FACTOR}, {@link ValueRequirementNames#PAY_ZERO_RATE},
   * {@link ValueRequirementNames#RECEIVE_DISCOUNT_FACTOR}, {@link ValueRequirementNames#RECEIVE_ZERO_RATE}.
   */
  public DiscountingFXPVFunction() {
    super(FX_PRESENT_VALUE, PAY_DISCOUNT_FACTOR, PAY_ZERO_RATE, RECEIVE_DISCOUNT_FACTOR, RECEIVE_ZERO_RATE);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new DiscountingCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), false) {

      @Override
      public boolean canApplyTo(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
        final Security security = target.getTrade().getSecurity();
        return security instanceof FXForwardSecurity || security instanceof NonDeliverableFXForwardSecurity;
      }

      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
          final ComputationTarget target, final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative,
          final FXMatrix fxMatrix) {
        final FinancialSecurity security = (FinancialSecurity) target.getTrade().getSecurity();
        final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
        final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
        final double paymentTime = derivative.accept(LastTimeCalculator.getInstance());
        final MulticurveProviderDiscount data = getMergedProviders(inputs, fxMatrix);
        final ValueRequirement desiredValue = desiredValues.iterator().next();
        final ValueProperties properties = desiredValue.getConstraints().copy().get();
        final MultipleCurrencyAmount mca = derivative.accept(CALCULATOR, data);
        final ValueSpecification fxPvSpec = new ValueSpecification(FX_PRESENT_VALUE, target.toSpecification(), properties);
        final ValueSpecification payDfSpec = new ValueSpecification(PAY_DISCOUNT_FACTOR, target.toSpecification(), properties);
        final ValueSpecification payZeroSpec = new ValueSpecification(PAY_ZERO_RATE, target.toSpecification(), properties);
        final ValueSpecification receiveDfSpec = new ValueSpecification(RECEIVE_DISCOUNT_FACTOR, target.toSpecification(), properties);
        final ValueSpecification receiveZeroSpec = new ValueSpecification(RECEIVE_ZERO_RATE, target.toSpecification(), properties);
        final Set<ComputedValue> results = new HashSet<>();
        results.add(new ComputedValue(fxPvSpec, FXUtils.getMultipleCurrencyAmountAsMatrix(mca)));
        results.add(new ComputedValue(payDfSpec, data.getDiscountFactor(payCurrency, paymentTime)));
        results.add(new ComputedValue(payZeroSpec, data.getCurve(payCurrency).getInterestRate(paymentTime)));
        results.add(new ComputedValue(receiveDfSpec, data.getDiscountFactor(receiveCurrency, paymentTime)));
        results.add(new ComputedValue(receiveZeroSpec, data.getCurve(receiveCurrency).getInterestRate(paymentTime)));
        return results;
      }
    };
  }

}
