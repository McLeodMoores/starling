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
  private static Set<Pair<Currency, Currency>> s_currencyPairs = new HashSet<>();

  static {
    final InputStream is = StandardCurrencyPairs.class.getClassLoader().getResourceAsStream("com/opengamma/util/money/standard-currency-pairs.csv");
    parseCSV("standard-currency-pairs.csv", is);
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
          s_currencyPairs.add(Pairs.of(Currency.of(numerator), Currency.of(denominator)));
        } catch (final IllegalArgumentException iae) {
          LOGGER.warn("Couldn't create currency from " + filename + ":" + line);
        }
        line++;
      }
    } catch (final IOException ex) {
      LOGGER.warn("Couldn't read " + filename);
    }
  }

  public static boolean isStandardPair(final Currency numerator, final Currency denominator) {
    return s_currencyPairs.contains(Pairs.of(numerator, denominator));
  }

  public static boolean isSingleCurrencyNumerator(final Currency ccy) {
    return s_currencyPairs.contains(Pairs.of(ccy, Currency.USD));
  }

}
