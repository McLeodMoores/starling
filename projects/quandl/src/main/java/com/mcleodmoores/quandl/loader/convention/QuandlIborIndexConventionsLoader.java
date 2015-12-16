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
import org.threeten.bp.LocalTime;

import au.com.bytecode.opencsv.CSVReader;

import com.mcleodmoores.quandl.QuandlConstants;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

/**
 * Creates {@link IborIndexConvention}s from a csv file called "ibor-index-conventions.csv". These conventions
 * are used to construct reference indices and as underlying conventions for instruments that have an ibor fixing
 * (e.g. a vanilla ibor swap leg).
 */
public final class QuandlIborIndexConventionsLoader implements ConventionsLoader<IborIndexConvention> {
  /** An instance of this loader. */
  public static final QuandlIborIndexConventionsLoader INSTANCE = new QuandlIborIndexConventionsLoader();
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(QuandlIborIndexConventionsLoader.class);
  /** The file name */
  private static final String FILE_NAME = "ibor-index-conventions.csv";

  /**
   * Restricted constructor.
   */
  private QuandlIborIndexConventionsLoader() {
  }

  /**
   * Generates {@link IborIndexConvention}s from a csv file.
   * @return  a set of conventions, or an empty set if the file was not available or no conventions could be created
   * @throws Exception  if there is a problem reading the file
   */
  @Override
  public Set<IborIndexConvention> loadConventionsFromFile() throws Exception {
    try (InputStream resource = getClass().getResourceAsStream(FILE_NAME)) {
      if (resource == null) {
        LOGGER.error("Could not open file called {}", FILE_NAME);
        return Collections.emptySet();
      }
      try (CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(resource)))) {
        String[] line = reader.readNext(); // ignore headers
        final Set<IborIndexConvention> conventions = new HashSet<>();
        while ((line = reader.readNext()) != null) {
          try {
            final String name = line[0];
            final Currency currency = Currency.of(line[1]);
            final DayCount dayCount = DayCountFactory.of(line[2]);
            final BusinessDayConvention businessDayConvention = BusinessDayConventionFactory.of(line[3]);
            final int settlementDays = Integer.parseInt(line[4]);
            final boolean isEom = Boolean.parseBoolean(line[5]);
            final LocalTime fixingTime = LocalTime.parse(line[6]);
            final String fixingTimeZone = line[7];
            final ExternalId fixingCalendar = line[8].equalsIgnoreCase("TARGET")
                ? ExternalSchemes.financialRegionId("EU") : ExternalSchemes.countryRegionId(Country.of(line[8]));
            final ExternalId regionCalendar = line[9].equalsIgnoreCase("TARGET")
                ? ExternalSchemes.financialRegionId("EU") : ExternalSchemes.countryRegionId(Country.of(line[9]));
            final String fixingPage = line[10];
            ExternalIdBundle idBundle = ExternalIdBundle.of(ExternalId.of("CONVENTION", name));
            for (int i = 11; i < line.length; i++) {
              final String code = line[i];
              if (!code.isEmpty()) {
                idBundle = idBundle.withExternalId(QuandlConstants.ofCode(line[i]));
              }
            }
            conventions.add(new IborIndexConvention(name, idBundle, dayCount, businessDayConvention, settlementDays, isEom, currency,
                fixingTime, fixingTimeZone, fixingCalendar, regionCalendar, fixingPage));
          } catch (final Exception e) {
            LOGGER.error(e.getMessage());
          }
        }
        return conventions;
      }
    }
  }
}
