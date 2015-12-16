/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.classification;

import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSet;

/**
 * Hard-coded lists of valid Quandl swap code patterns.
 */
public final class QuandlSwapTypes {

  /**
   * Restricted constructor.
   */
  private QuandlSwapTypes() {
  }

  /**
   * A list of valid swap patterns.
   */
  public static final Set<Pattern> VALID_SWAP_PATTERNS = ImmutableSet.of(
      Pattern.compile("(^FRED/DSWP)[0-9]+") // USD swap rates
      );
}
