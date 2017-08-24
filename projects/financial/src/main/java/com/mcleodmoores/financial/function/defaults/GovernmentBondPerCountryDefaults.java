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

import java.util.Collections;
import java.util.Set;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.position.Trade;
import com.opengamma.core.region.Region;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.bond.BillSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class GovernmentBondPerCountryDefaults extends DefaultPropertyFunction {
  private static final String[] VALUE_REQUIREMENTS = new String[] {
      PRESENT_VALUE,
      PV01,
      BUCKETED_PV01,
      YIELD_CURVE_NODE_SENSITIVITIES,
      BOND_DETAILS,
      MODIFIED_DURATION,
      MACAULAY_DURATION,
      CONVEXITY
  };
  private final String _countryCode;
  private final String _curveExposuresName;

  public GovernmentBondPerCountryDefaults(final String countryCode, final String curveExposuresName) {
    super(ComputationTargetType.TRADE, true);
    _countryCode = ArgumentChecker.notNull(countryCode, "countryCode");
    _curveExposuresName = ArgumentChecker.notNull(curveExposuresName, "curveExposuresName");
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Trade trade = target.getTrade();
    final Security security = trade.getSecurity();
    if (security instanceof GovernmentBondSecurity) {
      return ((GovernmentBondSecurity) security).getIssuerDomicile().equals(_countryCode);
    }
    if (security instanceof BillSecurity) {
      // assuming all bills are government-issued
      final ExternalId regionId = ((BillSecurity) security).getRegionId();
      try {
        final Region region = OpenGammaCompilationContext.getRegionSource(context).getSingle(regionId.toBundle());
        if (region != null && region.getCountry().getCode().equals(_countryCode)) {
          return true;
        }
      } catch (final DataNotFoundException e) {
        return false;
      }
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

