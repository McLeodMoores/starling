/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.convention;

/**
 * A list of convention groups.
 */
public final class ConventionGroups {

  /**
   * Group representing index conventions e.g. overnight index, price index.
   */
  public static final String INDEX = "Index";

  /**
   * Group representing conventions for instruments with defined roll dates (e.g. IMM dates).
   */
  public static final String ROLL_DATE_CONVENTION = "Roll Date";

  /**
   * Group representing conventions for swap legs. These are not complete conventions but are combined to produce a full convention.
   */
  public static final String SWAP_LEG_CONVENTION = "Swap Leg";

  /**
   * Group representing exchange-traded futures.
   */
  public static final String ETF = "ETF";

  /**
   * Group representing fixed income conventions e.g. a swap convention for a currency.
   */
  public static final String FIXED_INCOME = "Fixed Income";

  /**
   * Group representing inflation conventions.
   */
  public static final String INFLATION = "Inflation";

  /**
   * Group representing FX conventions.
   */
  public static final String FX = "FX";

  /**
   * Group representing bond conventions e.g. the day-count and yield convention.
   */
  public static final String BOND = "Bond";

  /**
   * Group representing miscellaneous conventions.
   */
  public static final String MISC = "Miscellaneous";

  private ConventionGroups() {
  }
}
