/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.swap;

import com.opengamma.financial.security.swap.FixedInflationSwapLeg;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FixedVarianceSwapLeg;
import com.opengamma.financial.security.swap.FloatingGearingIRLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.FloatingVarianceSwapLeg;
import com.opengamma.financial.security.swap.InflationIndexSwapLeg;
import com.opengamma.financial.security.swap.SwapLegVisitor;
import com.opengamma.masterdb.security.hibernate.EnumUserType;

/**
 * Custom Hibernate usertype for the SwapLegType enum
 */
public class SwapLegTypeUserType extends EnumUserType<SwapLegType> {

  private static final String FIXED_INTEREST = "Fixed interest";
  private static final String FLOATING_INTEREST = "Floating interest";
  private static final String FLOATING_SPREAD_INTEREST = "Floating spread interest";
  private static final String FLOATING_GEARING_INTEREST = "Floating gearing interest";
  private static final String FIXED_VARIANCE = "Fixed variance";
  private static final String FLOATING_VARIANCE = "Floating variance";
  private static final String FIXED_INFLATION = "Fixed inflation";
  private static final String INFLATION_INDEX = "Inflation index";

  public SwapLegTypeUserType() {
    super(SwapLegType.class, SwapLegType.values());
  }

  @Override
  protected String enumToStringNoCache(final SwapLegType value) {
    return value.accept(new SwapLegVisitor<String>() {

      @Override
      public String visitFixedInterestRateLeg(final FixedInterestRateLeg swapLeg) {
        return FIXED_INTEREST;
      }

      @Override
      public String visitFloatingInterestRateLeg(final FloatingInterestRateLeg swapLeg) {
        return FLOATING_INTEREST;
      }

      @Override
      public String visitFloatingSpreadIRLeg(final FloatingSpreadIRLeg swapLeg) {
        return FLOATING_SPREAD_INTEREST;
      }

      @Override
      public String visitFloatingGearingIRLeg(final FloatingGearingIRLeg swapLeg) {
        return FLOATING_GEARING_INTEREST;
      }

      @Override
      public String visitFixedVarianceSwapLeg(final FixedVarianceSwapLeg swapLeg) {
        return FIXED_VARIANCE;
      }

      @Override
      public String visitFloatingVarianceSwapLeg(final FloatingVarianceSwapLeg swapLeg) {
        return FLOATING_VARIANCE;
      }

      @Override
      public String visitInflationIndexSwapLeg(final InflationIndexSwapLeg swapLeg) {
        return FIXED_INFLATION;
      }

      @Override
      public String visitFixedInflationSwapLeg(final FixedInflationSwapLeg swapLeg) {
        return INFLATION_INDEX;
      }
    });
  }

}
