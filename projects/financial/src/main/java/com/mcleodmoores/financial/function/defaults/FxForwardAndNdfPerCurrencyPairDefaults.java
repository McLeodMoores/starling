/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.defaults;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValueRequirementNames.BLOCK_CURVE_SENSITIVITIES;
import static com.opengamma.engine.value.ValueRequirementNames.FX_CURRENCY_EXPOSURE;
import static com.opengamma.engine.value.ValueRequirementNames.FX_FORWARD_DETAILS;
import static com.opengamma.engine.value.ValueRequirementNames.FX_NDF_DETAILS;
import static com.opengamma.engine.value.ValueRequirementNames.FX_PRESENT_VALUE;
import static com.opengamma.engine.value.ValueRequirementNames.PAY_DISCOUNT_FACTOR;
import static com.opengamma.engine.value.ValueRequirementNames.PAY_ZERO_RATE;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE;
import static com.opengamma.engine.value.ValueRequirementNames.RECEIVE_DISCOUNT_FACTOR;
import static com.opengamma.engine.value.ValueRequirementNames.RECEIVE_ZERO_RATE;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES;

import java.util.Collections;
import java.util.Set;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 *
 */
public class FxForwardAndNdfPerCurrencyPairDefaults extends DefaultPropertyFunction {
  private static final String[] VALUE_REQUIREMENTS = new String[] {
      FX_PRESENT_VALUE,
      PRESENT_VALUE,
      FX_CURRENCY_EXPOSURE,
      BLOCK_CURVE_SENSITIVITIES,
      YIELD_CURVE_NODE_SENSITIVITIES,
      PAY_DISCOUNT_FACTOR,
      PAY_ZERO_RATE,
      RECEIVE_DISCOUNT_FACTOR,
      RECEIVE_ZERO_RATE,
      FX_FORWARD_DETAILS,
      FX_NDF_DETAILS
  };
  private final UnorderedCurrencyPair _underlying;
  private final String _curveExposuresName;

  public FxForwardAndNdfPerCurrencyPairDefaults(final String ccy1, final String ccy2, final String curveExposuresName) {
    super(ComputationTargetType.TRADE, true);
    _underlying = UnorderedCurrencyPair.of(Currency.of(ccy1), Currency.of(ccy2));
    _curveExposuresName = ArgumentChecker.notNull(curveExposuresName, "curveExposuresName");
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getTrade().getSecurity();
    if (security instanceof FXForwardSecurity || security instanceof NonDeliverableFXForwardSecurity) {
      final FinancialSecurity financialSecurity = (FinancialSecurity) security;
      final Currency payCurrency = financialSecurity.accept(ForexVisitors.getPayCurrencyVisitor());
      final Currency receiveCurrency = financialSecurity.accept(ForexVisitors.getReceiveCurrencyVisitor());
      return UnorderedCurrencyPair.of(payCurrency, receiveCurrency).equals(_underlying);
    }
    return false;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, CURVE_EXPOSURES);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target,
      final ValueRequirement desiredValue, final String propertyName) {
    switch (propertyName) {
      case CURVE_EXPOSURES:
        return Collections.singleton(_curveExposuresName);
      default:
        return null;
    }
  }
}
