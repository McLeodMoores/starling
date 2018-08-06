/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.instruments;

import com.mcleodmoores.analytics.financial.bond.FixedCouponBondSecurity;

/**
 * A visitor for bond securities that uses data other than security information.
 *
 * @param <DATA_TYPE>  the type of the data
 * @param <RESULT_TYPE>  the type of the result
 */
public interface DataBondSecurityVisitor<DATA_TYPE, RESULT_TYPE>  {

  /**
   * Visits a fixed coupon bond security.
   *
   * @param bond  the bond, not null
   * @param data  the data, not null
   * @return  the result
   */
  RESULT_TYPE visitFixedCouponBond(FixedCouponBondSecurity bond, DATA_TYPE data);
}
