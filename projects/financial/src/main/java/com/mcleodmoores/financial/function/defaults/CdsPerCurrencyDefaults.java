/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.defaults;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValueRequirementNames.ACCRUED_DAYS;
import static com.opengamma.engine.value.ValueRequirementNames.ACCRUED_PREMIUM;
import static com.opengamma.engine.value.ValueRequirementNames.BUCKETED_CS01;
import static com.opengamma.engine.value.ValueRequirementNames.CLEAN_PRESENT_VALUE;
import static com.opengamma.engine.value.ValueRequirementNames.CLEAN_PRICE;
import static com.opengamma.engine.value.ValueRequirementNames.DIRTY_PRESENT_VALUE;
import static com.opengamma.engine.value.ValueRequirementNames.IR01;
import static com.opengamma.engine.value.ValueRequirementNames.PARALLEL_CS01;
import static com.opengamma.engine.value.ValueRequirementNames.POINTS_UPFRONT;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE;
import static com.opengamma.engine.value.ValueRequirementNames.PRINCIPAL;
import static com.opengamma.engine.value.ValueRequirementNames.QUOTED_SPREAD;
import static com.opengamma.engine.value.ValueRequirementNames.RR01;
import static com.opengamma.engine.value.ValueRequirementNames.UPFRONT_AMOUNT;

import java.util.Collections;
import java.util.Set;

import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.credit.LegacyCDSSecurity;
import com.opengamma.financial.security.credit.StandardCDSSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides default property values per currency for CDS trades to be used by functions.
 */
public class CdsPerCurrencyDefaults extends DefaultPropertyFunction {
  private static final String[] VALUE_REQUIREMENTS = new String[] { ACCRUED_DAYS, ACCRUED_PREMIUM, POINTS_UPFRONT, CLEAN_PRESENT_VALUE, DIRTY_PRESENT_VALUE,
      CLEAN_PRICE, QUOTED_SPREAD, UPFRONT_AMOUNT, PARALLEL_CS01, PRINCIPAL, PRESENT_VALUE, BUCKETED_CS01, RR01, IR01 };
  private final String _currencyCode;
  private final String _curveExposuresName;

  /**
   * @param currencyCode
   *          the currency, not null
   * @param curveExposuresName
   *          the name of the curve exposure function, not null
   */
  public CdsPerCurrencyDefaults(final String currencyCode, final String curveExposuresName) {
    super(ComputationTargetType.TRADE, true);
    _currencyCode = ArgumentChecker.notNull(currencyCode, "currencyCode");
    _curveExposuresName = ArgumentChecker.notNull(curveExposuresName, "curveExposuresName");
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Trade trade = target.getTrade();
    final Security security = trade.getSecurity();
    if (security instanceof StandardCDSSecurity) {
      return ((StandardCDSSecurity) security).getNotional().getCurrency().getCode().equals(_currencyCode);
    }
    if (security instanceof LegacyCDSSecurity) {
      return ((LegacyCDSSecurity) security).getNotional().getCurrency().getCode().equals(_currencyCode);
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
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue,
      final String propertyName) {
    switch (propertyName) {
      case CURVE_EXPOSURES:
        return Collections.singleton(_curveExposuresName);
      default:
        return null;
    }
  }
}
