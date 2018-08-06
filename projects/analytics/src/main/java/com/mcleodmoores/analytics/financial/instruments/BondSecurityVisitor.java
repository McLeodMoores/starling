/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.instruments;

import com.mcleodmoores.analytics.financial.bond.FixedCouponBondSecurity;

/**
 * A visitor for bond securities.
 *
 * @param <RESULT_TYPE>  the type of the result
 */
public interface BondSecurityVisitor<RESULT_TYPE>  {

  /**
   * Visits a fixed coupon bond security.
   *
   * @param bond  the bond, not null
   * @return  the result
   */
  RESULT_TYPE visitFixedCouponBond(FixedCouponBondSecurity bond);
}
