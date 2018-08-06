/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.bond;

/**
 * A visitor-style interface for bond yield conventions that allows different calculations
 * to be performed for different bond yield convention types.
 *
 * @param <DATA_TYPE>  the type (if any) of data required for the calculation
 * @param <RESULT_TYPE>  the type of the result
 *
 */
public interface DataYieldConventionTypeVisitor<DATA_TYPE, RESULT_TYPE> {

  /**
   * A method for bonds that have a US street yield convention.
   *
   * @param security  the bond, not null
   * @param data  the data
   * @return  the result
   */
  RESULT_TYPE visitUsStreet(FixedCouponBondSecurity security, DATA_TYPE data);

  /**
   * A method for bonds that have a UK debt management office yield convention.
   *
   * @param security  the bond, not null
   * @param data  the data
   * @return  the result
   */
  RESULT_TYPE visitUkDmo(FixedCouponBondSecurity security, DATA_TYPE data);

  /**
   * A method for bonds that have a French compounding yield convention.
   *
   * @param security  the bond, not null
   * @param data  the data
   * @return  the result
   */
  RESULT_TYPE visitFranceCompound(FixedCouponBondSecurity security, DATA_TYPE data);

  /**
   * A method for bonds that have an Italian Treasury yield convention.
   *
   * @param security  the bond, not null
   * @param data  the data
   * @return  the result
   */
  RESULT_TYPE visitItalyTreasury(FixedCouponBondSecurity security, DATA_TYPE data);

}
