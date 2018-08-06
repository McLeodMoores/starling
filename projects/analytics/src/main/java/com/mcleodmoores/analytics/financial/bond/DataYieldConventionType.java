/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.bond;

/**
 * An interface for yield convention types e.g. US street or JGB yield.
 */
public interface DataYieldConventionType {

  /**
   * A visitor-style method that dispatches from the yield convention defined in the bond security to
   * the particular method for that yield in the calculator.
   *
   * @param <DATA_TYPE>  the type of the data used in the calculation
   * @param <RESULT_TYPE>  the type of the result
   * @param visitor  the visitor, not null
   * @param bond  the bond security, not null
   * @param data  the data, can be null
   * @return  the result
   */
  <DATA_TYPE, RESULT_TYPE> RESULT_TYPE accept(DataYieldConventionTypeVisitor<DATA_TYPE, RESULT_TYPE> visitor, FixedCouponBondSecurity bond, DATA_TYPE data);

}
