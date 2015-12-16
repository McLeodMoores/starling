/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.classification;

import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSet;

/**
 * Hard-coded lists of valid Quandl cash code patterns.
 */
//TODO #16 not good to have as hard-coded lists
public final class QuandlCashTypes {

  /**
   * Restricted constructor.
   */
  private QuandlCashTypes() {
  }

  /**
   * A list of valid Ibor patterns.
   */
  public static final Set<Pattern> VALID_IBOR_PATTERNS = ImmutableSet.of(
      Pattern.compile("(^FRED/USD)[0-9]+\\S+(TD156N$)")); // USD LIBOR

  /**
   * A list of valid overnight patterns.
   */
  public static final Set<Pattern> VALID_OVERNIGHT_PATTERNS = ImmutableSet.of(
      Pattern.compile("FRED/USDONTD156N")); // USD Fed Funds overnight
}
