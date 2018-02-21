/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.convention;

/**
 * A list of convention groups.
 */
public class ConventionGroups {

  /**
   * Group representing index conventions e.g. overnight index, price index.
   */
  public static final String INDEX = "Index";

  /**
   * Group representing fixed income conventions e.g. a fixed swap leg convention
   * for a currency.
   */
  public static final String FIXED_INCOME = "Fixed Income";

  /**
   * Group representing FX conventions e.g. number of settlement days.
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

}
