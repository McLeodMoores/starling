/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.defaults;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValueRequirementNames.BOND_DETAILS;
import static com.opengamma.engine.value.ValueRequirementNames.BUCKETED_PV01;
import static com.opengamma.engine.value.ValueRequirementNames.CLEAN_PRICE;
import static com.opengamma.engine.value.ValueRequirementNames.CONVEXITY;
import static com.opengamma.engine.value.ValueRequirementNames.CREDIT_SPREAD;
import static com.opengamma.engine.value.ValueRequirementNames.DIRTY_PRICE;
import static com.opengamma.engine.value.ValueRequirementNames.HAZARD_RATE;
import static com.opengamma.engine.value.ValueRequirementNames.MACAULAY_DURATION;
import static com.opengamma.engine.value.ValueRequirementNames.MODIFIED_DURATION;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE;
import static com.opengamma.engine.value.ValueRequirementNames.PV01;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES;
import static com.opengamma.engine.value.ValueRequirementNames.YTM;

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
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides default property values per country for bond trades to be used by functions.
 */
public class BondPerCountryDefaults extends DefaultPropertyFunction {

  /**
   * Represents the type of bonds.
   */
  public enum BondType {
    /**
     * Government bonds.
     */
    GOVERNMENT,
    /**
     * Corporate bonds.
     */
    CORPORATE,
    /**
     * Municipal bonds.
     */
    MUNICIPAL
  }

  private static final String[] VALUE_REQUIREMENTS = new String[] { PRESENT_VALUE, PV01, BUCKETED_PV01, YIELD_CURVE_NODE_SENSITIVITIES, BOND_DETAILS,
    MODIFIED_DURATION, MACAULAY_DURATION, CONVEXITY, YTM, CREDIT_SPREAD, CLEAN_PRICE, DIRTY_PRICE, HAZARD_RATE };
  private final String _countryCode;
  private final String _curveExposuresName;
  private final BondType _bondType;

  /**
   * @param countryCode
   *          the country, not null
   * @param curveExposuresName
   *          the name of the curve exposure function, not null
   * @param bondType
   *          the type of the bond, not null
   */
  public BondPerCountryDefaults(final String countryCode, final String curveExposuresName, final String bondType) {
    super(ComputationTargetType.TRADE, true);
    _countryCode = ArgumentChecker.notNull(countryCode, "countryCode");
    _curveExposuresName = ArgumentChecker.notNull(curveExposuresName, "curveExposuresName");
    _bondType = BondType.valueOf(ArgumentChecker.notNull(bondType, "bondType"));
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Trade trade = target.getTrade();
    final Security security = trade.getSecurity();
    if (security instanceof GovernmentBondSecurity) {
      return _bondType == BondType.GOVERNMENT && ((GovernmentBondSecurity) security).getIssuerDomicile().equals(_countryCode);
    }
    if (security instanceof CorporateBondSecurity) {
      return _bondType == BondType.CORPORATE && ((CorporateBondSecurity) security).getIssuerDomicile().equals(_countryCode);
    }
    if (security instanceof MunicipalBondSecurity) {
      return _bondType == BondType.MUNICIPAL && ((MunicipalBondSecurity) security).getIssuerDomicile().equals(_countryCode);
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
