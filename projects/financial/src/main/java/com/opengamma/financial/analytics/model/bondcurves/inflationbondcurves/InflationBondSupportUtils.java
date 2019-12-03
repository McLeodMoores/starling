/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondcurves.inflationbondcurves;

import java.util.HashSet;
import java.util.Set;

import com.opengamma.core.security.Security;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.InflationBondSecurity;

/**
 * Utility methods for establishing if a bond or bill is supported.
 */
public class InflationBondSupportUtils {
  private static final Set<YieldConvention> SUPPORTED_YIELD_CONVENTIONS = new HashSet<>();
  private static final Set<String> SUPPORTED_COUPON_TYPES = new HashSet<>();

  static {
    SUPPORTED_YIELD_CONVENTIONS.add(SimpleYieldConvention.US_STREET);
    SUPPORTED_YIELD_CONVENTIONS.add(SimpleYieldConvention.GERMAN_BOND);
    SUPPORTED_YIELD_CONVENTIONS.add(SimpleYieldConvention.AUSTRALIA_EX_DIVIDEND);
    SUPPORTED_YIELD_CONVENTIONS.add(SimpleYieldConvention.UK_BUMP_DMO_METHOD);
    SUPPORTED_YIELD_CONVENTIONS.add(SimpleYieldConvention.FRANCE_COMPOUND_METHOD);
    SUPPORTED_YIELD_CONVENTIONS.add(SimpleYieldConvention.ITALY_TREASURY_BONDS);
    SUPPORTED_YIELD_CONVENTIONS.add(SimpleYieldConvention.INDEX_LINKED_FLOAT);
    SUPPORTED_YIELD_CONVENTIONS.add(SimpleYieldConvention.UK_IL_BOND);
    SUPPORTED_COUPON_TYPES.add("FIXED");
  }

  public static boolean isSupported(final Security security) {
    if (security instanceof InflationBondSecurity) {
      final BondSecurity bondSecurity = (BondSecurity) security;
      if (SUPPORTED_YIELD_CONVENTIONS.contains(bondSecurity.getYieldConvention()) && SUPPORTED_COUPON_TYPES.contains(bondSecurity.getCouponType())) {
        return true;
      }
      return false;
    }
    return false;
  }

}
