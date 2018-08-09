/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.convention.bond;

/**
 *
 */
@YieldConventionType(aliases = { "US street", "STREET CONVENTION" })
public class UsStreetConvention implements BondYieldConvention {
  public static final String NAME = "US STREET";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public <RESULT_TYPE> RESULT_TYPE accept(final BondYieldConventionVisitor<?, RESULT_TYPE> visitor) {
    return null; // visitor.visitUsStreet(this);
  }

  @Override
  public <DATA_TYPE, RESULT_TYPE> RESULT_TYPE accept(final BondYieldConventionVisitor<DATA_TYPE, RESULT_TYPE> visitor, final DATA_TYPE data) {
    return null; //visitor.visitUsStreet(this, data);
  }

}
