/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.defaults;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValueRequirementNames.BOND_DETAILS;
import static com.opengamma.engine.value.ValueRequirementNames.BUCKETED_PV01;
import static com.opengamma.engine.value.ValueRequirementNames.CONVEXITY;
import static com.opengamma.engine.value.ValueRequirementNames.MACAULAY_DURATION;
import static com.opengamma.engine.value.ValueRequirementNames.MODIFIED_DURATION;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE;
import static com.opengamma.engine.value.ValueRequirementNames.PV01;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES;
import static com.opengamma.engine.value.ValueRequirementNames.YTM;

import java.util.Collections;
import java.util.Set;

import com.mcleodmoores.financial.function.defaults.BondPerCountryDefaults.BondType;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.bond.BillSecurity;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides default property values per currency for bond trades to be used by functions.
 */
public class BondPerCurrencyDefaults extends DefaultPropertyFunction {
  private static final String[] VALUE_REQUIREMENTS = new String[] { PRESENT_VALUE, PV01, BUCKETED_PV01, YIELD_CURVE_NODE_SENSITIVITIES, BOND_DETAILS,
      MODIFIED_DURATION, MACAULAY_DURATION, CONVEXITY, YTM };
  private final String _currency;
  private final String _curveExposuresName;
  private final BondType _bondType;

  /**
   * @param currency
   *          the currency, not null
   * @param curveExposuresName
   *          the name of the curve exposure function, not null
   * @param bondType
   *          the type of the bond, not null
   */
  public BondPerCurrencyDefaults(final String currency, final String curveExposuresName, final String bondType) {
    super(ComputationTargetType.TRADE, true);
    _currency = ArgumentChecker.notNull(currency, "currency");
    _curveExposuresName = ArgumentChecker.notNull(curveExposuresName, "curveExposuresName");
    _bondType = BondType.valueOf(ArgumentChecker.notNull(bondType, "bondType"));
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getTrade().getSecurity();
    if (security instanceof GovernmentBondSecurity) {
      return _bondType == BondType.GOVERNMENT && ((GovernmentBondSecurity) security).getCurrency().getCode().equals(_currency);
    }
    if (security instanceof CorporateBondSecurity) {
      return _bondType == BondType.CORPORATE && ((CorporateBondSecurity) security).getCurrency().getCode().equals(_currency);
    }
    if (security instanceof MunicipalBondSecurity) {
      return _bondType == BondType.MUNICIPAL && ((MunicipalBondSecurity) security).getCurrency().getCode().equals(_currency);
    }
    if (security instanceof BillSecurity) {
      return ((BillSecurity) security).getCurrency().getCode().equals(_currency);
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
