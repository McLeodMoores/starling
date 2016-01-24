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

import com.mcleodmoores.quandl.QuandlConstants;
import com.mcleodmoores.quandl.convention.QuandlStirFutureConvention;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Creates {@link QuandlStirFutureConvention}s from a csv file called "stir-future-conventions.csv". These
 * conventions contain information that is required to construct interest rate futures.
 */
public final class QuandlStirFutureConventionsLoader implements ConventionsLoader<QuandlStirFutureConvention> {
  /** An instance of this loader. */
  public static final QuandlStirFutureConventionsLoader INSTANCE = new QuandlStirFutureConventionsLoader();
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(QuandlStirFutureConventionsLoader.class);
  /** The file name */
  private static final String FILE_NAME = "stir-future-conventions.csv";

  /**
   * Restricted constructor.
   */
  private QuandlStirFutureConventionsLoader() {
  }

  /**
   * Generates {@link QuandlStirFutureConvention}s from a csv file.
   * @return  a set of conventions, or an empty set if the file was not available or no conventions could be created
   * @throws Exception  if there is a problem reading the file
   */
  @Override
  public Set<QuandlStirFutureConvention> loadConventionsFromFile() throws Exception {
    try (InputStream resource = getClass().getResourceAsStream(FILE_NAME)) {
      if (resource == null) {
        LOGGER.error("Could not open file called {}", FILE_NAME);
        return Collections.emptySet();
      }
      try (CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(resource)))) {
        String[] line = reader.readNext(); // ignore headers
        final Set<QuandlStirFutureConvention> conventions = new HashSet<>();
        while ((line = reader.readNext()) != null) {
          try {
            final String name = line[0];
            final Currency currency = Currency.of(line[1]);
            final ExternalIdBundle idBundle = ExternalIdBundle.of(ExternalId.of("CONVENTION", name), QuandlConstants.ofPrefix(line[2]));
            final Tenor futureTenor = Tenor.parse(line[3]);
            final Tenor underlyingTenor = Tenor.parse(line[4]);
            final String lastTradingTime = line[5];
            final String timeZone = line[6];
            final Double unitAmount = Double.parseDouble(line[7]);
            final ExternalId underlyingConventionId = ExternalId.of("CONVENTION", line[8]);
            final int nthDay = Integer.parseInt(line[9]);
            final String dayOfWeek = line[10].toUpperCase();
            final String tradingExchange = line[11];
            final String settlementExchange = line[12];
            final ExternalId tradingExchangeCalendarId = ExternalSchemes.countryRegionId(Country.of(line[13]));
            conventions.add(new QuandlStirFutureConvention(name, idBundle, currency, futureTenor, underlyingTenor, lastTradingTime,
                timeZone, unitAmount, underlyingConventionId, nthDay, dayOfWeek, tradingExchange, settlementExchange, tradingExchangeCalendarId));
          } catch (final Exception e) {
            LOGGER.error(e.getMessage());
          }
        }
        return conventions;
      }
    }
  }
}
