/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.loader.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;

/**
 * Creates a {@link CurrencyPairs} configuration from currency-pairs.csv.
 */
public final class QuandlCurrencyPairsGenerator {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(QuandlCurrencyPairsGenerator.class);

  /**
   * Restricted constructor.
   */
  private QuandlCurrencyPairsGenerator() {
  }

  /**
   * Creates a currency pairs configuration from a file.
   * @return The configuration
   */
  public static CurrencyPairs createConfiguration() {
    try (final BufferedReader reader =
        new BufferedReader(new InputStreamReader(QuandlCurrencyPairsGenerator.class.getResourceAsStream("currency-pairs.csv")))) {
      String pairStr;
      final Set<CurrencyPair> pairs = new HashSet<>();
      while ((pairStr = reader.readLine()) != null) {
        try {
          final CurrencyPair pair = CurrencyPair.parse(pairStr.trim());
          pairs.add(pair);
        } catch (final IllegalArgumentException e) {
          LOGGER.warn("Unable to create currency pair from " + pairStr, e);
        }
      }
      return CurrencyPairs.of(pairs);
    } catch (final IOException e) {
      LOGGER.warn(e.getMessage());
    }
    throw new IllegalStateException("Could not create CurrencyPairs");
  }
}
