/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Look up whether you a pair of currencies is in the standard order.
 */
public class StandardCurrencyPairs {

  private static final Logger LOGGER = LoggerFactory.getLogger(StandardCurrencyPairs.class);
  private static final Set<Pair<Currency, Currency>> CURRENCY_PAIRS = new HashSet<>();

  static {
    final String filename = "com/opengamma/util/money/standard-currency-pairs.csv";
    try (InputStream is = StandardCurrencyPairs.class.getClassLoader().getResourceAsStream(filename)) {
      parseCSV("standard-currency-pairs.csv", is);
    } catch (final IOException e) {
      LOGGER.warn("Could not read " + filename);
    }
  }

  private static void parseCSV(final String filename, final InputStream is) {
    List<String[]> rows;
    try (CSVReader reader = new CSVReader(new InputStreamReader(new BufferedInputStream(is)))) {
      rows = reader.readAll();
      int line = 1;
      for (final String[] row : rows) {
        final String numerator = row[0].trim();
        final String denominator = row[1].trim();
        try {
          CURRENCY_PAIRS.add(Pairs.of(Currency.of(numerator), Currency.of(denominator)));
        } catch (final IllegalArgumentException iae) {
          LOGGER.warn("Couldn't create currency from " + filename + ":" + line);
        }
        line++;
      }
    } catch (final IOException ex) {
      LOGGER.warn("Couldn't read " + filename);
    }
  }

  /**
   * Returns true if the pair is in the list of known currency pairs and in standard base / counter order.
   *
   * @param numerator  the numerator currency, not null
   * @param denominator  the denominator currency, not null
   * @return  true if the pair is in the list of standard currency pairs
   */
  public static boolean isStandardPair(final Currency numerator, final Currency denominator) {
    return CURRENCY_PAIRS.contains(Pairs.of(numerator, denominator));
  }

  /**
   * Returns true if there is an entry for CCY/USD in the list of known currency pairs.
   *
   * @param ccy  the currency, not null
   * @return  true if there is a CCY/USD entry
   */
  public static boolean isSingleCurrencyNumerator(final Currency ccy) {
    return CURRENCY_PAIRS.contains(Pairs.of(ccy, Currency.USD));
  }

}
