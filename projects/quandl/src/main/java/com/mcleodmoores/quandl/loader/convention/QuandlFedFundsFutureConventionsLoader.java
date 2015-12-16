/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.loader.convention;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

import com.mcleodmoores.quandl.QuandlConstants;
import com.mcleodmoores.quandl.convention.QuandlFedFundsFutureConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * Creates {@link QuandlFedFundsFutureConvention}s from a csv file called "fed-funds-future-conventions.csv". These
 * conventions contain information that required to construct interest rate futures.
 */
public final class QuandlFedFundsFutureConventionsLoader implements ConventionsLoader<QuandlFedFundsFutureConvention> {
  /** An instance of this loader. */
  public static final QuandlFedFundsFutureConventionsLoader INSTANCE = new QuandlFedFundsFutureConventionsLoader();
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(QuandlFedFundsFutureConventionsLoader.class);
  /** The file name */
  private static final String FILE_NAME = "fed-funds-future-conventions.csv";

  /**
   * Restricted constructor.
   */
  private QuandlFedFundsFutureConventionsLoader() {
  }

  /**
   * Generates {@link QuandlFedFundsFutureConvention}s from a csv file.
   * @return  a set of conventions, or an empty set if the file was not available or no conventions could be created
   * @throws Exception  if there is a problem reading the file
   */
  @Override
  public Set<QuandlFedFundsFutureConvention> loadConventionsFromFile() throws Exception {
    try (InputStream resource = getClass().getResourceAsStream(FILE_NAME)) {
      if (resource == null) {
        LOGGER.error("Could not open file called {}", FILE_NAME);
        return Collections.emptySet();
      }
      try (CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(resource)))) {
        String[] line = reader.readNext(); // ignore headers
        final Set<QuandlFedFundsFutureConvention> conventions = new HashSet<>();
        while ((line = reader.readNext()) != null) {
          try {
            final String name = line[0];
            final ExternalIdBundle idBundle = ExternalIdBundle.of(ExternalId.of("CONVENTION", name), QuandlConstants.ofPrefix(line[1]));
            final String lastTradingTime = line[2];
            final String timeZone = line[3];
            final Double unitAmount = Double.parseDouble(line[4]);
            final ExternalId underlyingConventionId = ExternalId.of("CONVENTION", line[5]);
            final String tradingExchange = line[6];
            final String settlementExchange = line[7];
            conventions.add(new QuandlFedFundsFutureConvention(name, idBundle, lastTradingTime, timeZone, unitAmount, underlyingConventionId,
                tradingExchange, settlementExchange));
          } catch (final Exception e) {
            LOGGER.error(e.getMessage());
          }
        }
        return conventions;
      }
    }
  }
}
