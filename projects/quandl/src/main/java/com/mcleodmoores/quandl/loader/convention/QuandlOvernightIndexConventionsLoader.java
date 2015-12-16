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
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

/**
 * Creates {@link OvernightIndexConvention}s from a csv file called "overnight-index-conventions.csv". These conventions
 * are used to construct reference indices and as underlying conventions for instruments that have an overnight fixing
 * (e.g. a Fed fund future).
 */
public final class QuandlOvernightIndexConventionsLoader implements ConventionsLoader<OvernightIndexConvention> {
  /** An instance of this loader. */
  public static final QuandlOvernightIndexConventionsLoader INSTANCE = new QuandlOvernightIndexConventionsLoader();
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(QuandlOvernightIndexConventionsLoader.class);
  /** The file name */
  private static final String FILE_NAME = "overnight-index-conventions.csv";

  /**
   * Restricted constructor.
   */
  private QuandlOvernightIndexConventionsLoader() {
  }

  /**
   * Generates {@link OvernightIndexConvention}s from a csv file.
   * @return  a set of conventions, or an empty set if the file was not available or no conventions could be created
   * @throws Exception  if there is a problem reading the file
   */
  @Override
  public Set<OvernightIndexConvention> loadConventionsFromFile() throws Exception {
    try (InputStream resource = getClass().getResourceAsStream(FILE_NAME)) {
      if (resource == null) {
        LOGGER.error("Could not open file called {}", FILE_NAME);
        return Collections.emptySet();
      }
      try (CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(resource)))) {
        String[] line = reader.readNext(); // ignore headers
        final Set<OvernightIndexConvention> conventions = new HashSet<>();
        while ((line = reader.readNext()) != null) {
          try {
            final String name = line[0];
            final Currency currency = Currency.of(line[1]);
            final DayCount dayCount = DayCountFactory.of(line[2]);
            final int publicationLag = Integer.parseInt(line[3]);
            final ExternalId regionCalendar = line[4].equalsIgnoreCase("TARGET")
                ? ExternalSchemes.financialRegionId("EU") : ExternalSchemes.countryRegionId(Country.of(line[4]));
            ExternalIdBundle idBundle = ExternalIdBundle.of(ExternalId.of("CONVENTION", name));
            for (int i = 5; i < line.length; i++) {
              final String code = line[i];
              if (!code.isEmpty()) {
                idBundle = idBundle.withExternalId(QuandlConstants.ofCode(line[i]));
              }
            }
            conventions.add(new OvernightIndexConvention(name, idBundle, dayCount, publicationLag, currency, regionCalendar));
          } catch (final Exception e) {
            LOGGER.error(e.getMessage());
          }
        }
        return conventions;
      }
    }
  }
}
