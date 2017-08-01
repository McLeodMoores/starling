/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.model.discounting;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
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
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the present value of FX forwards using curves constructed using the discounting method.
 */
public class DiscountingPvFxForwardFunction extends DiscountingFunction {
  /** The present value calculator. */
  static final InstrumentDerivativeVisitor<MulticurveProviderInterface, MultipleCurrencyAmount> CALCULATOR =
      PresentValueDiscountingCalculator.getInstance();

  /**
   * Sets the value requirement to {@link ValueRequirementNames#PRESENT_VALUE}.
   */
  public DiscountingPvFxForwardFunction() {
    super(PRESENT_VALUE);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new DiscountingCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), true) {

      @Override
      public boolean canApplyTo(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
        final Security security = target.getTrade().getSecurity();
        if (security instanceof SwapSecurity) {
          if (InterestRateInstrumentType.isFixedIncomeInstrumentType((SwapSecurity) security)) {
            return InterestRateInstrumentType.getInstrumentTypeFromSecurity((SwapSecurity) security) == InterestRateInstrumentType.SWAP_CROSS_CURRENCY;
          }
          return false;
        }
        return security instanceof FXForwardSecurity || security instanceof NonDeliverableFXForwardSecurity;
      }

      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
          final ComputationTarget target, final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative,
          final FXMatrix fxMatrix) {
        final MulticurveProviderInterface data = getMergedProviders(inputs, fxMatrix);
        final MultipleCurrencyAmount mca = derivative.accept(CALCULATOR, data);
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        final ValueProperties properties = desiredValue.getConstraints().copy().get();
        final Currency currency = Currency.of(Iterables.getOnlyElement(properties.getValues(CURRENCY)));
        final ValueSpecification spec = new ValueSpecification(PRESENT_VALUE, target.toSpecification(), properties);
        return Collections.singleton(new ComputedValue(spec, mca.getAmount(currency)));
      }

    };
  }
}
