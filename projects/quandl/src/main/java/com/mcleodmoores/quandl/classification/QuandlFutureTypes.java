/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.classification;

import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSet;

/**
 * Hard-coded lists of valid Quandl future code patterns.
 */
//TODO #16 not good to have as hard-coded lists
public final class QuandlFutureTypes {

  /**
   * Restricted constructor.
   */
  private QuandlFutureTypes() {

  }

  /**
   * A list of valid rate future code patterns.
   */
  public static final Set<Pattern> VALID_RATE_FUTURE_PATTERNS = ImmutableSet.of(
      Pattern.compile("CME/ED"), // 3M Eurodollar
      Pattern.compile("CME/EM"), // 1M Eurodollar
      Pattern.compile("CME/EY"), // Euroyen TIBOR
      Pattern.compile("CME/FF"), // 1M Fed funds
      Pattern.compile("EUREX/FEO1"), // 1M EONIA
      Pattern.compile("EUREX/FEU3"), // 3M EONIA
      Pattern.compile("LIFFE/L"), // GBP LIBOR
      Pattern.compile("LIFFE/J"), // TIBOR
      Pattern.compile("LIFFE/I"), // EURIBOR
      Pattern.compile("LIFFE/S"), // Euroswiss
      Pattern.compile("LIFFE/EON"), // 1M EONIA
      Pattern.compile("MX/BAX"), // 3M CAD Banker's Acceptance
      Pattern.compile("SGX/EY"), // Euroyen TIBOR
      Pattern.compile("SGX/ED"), // Eurodollar
      Pattern.compile("SGX/EL"), // Euroyen LIBOR
      Pattern.compile("TFX/JBA") // 3M Euroyen
      );
}
