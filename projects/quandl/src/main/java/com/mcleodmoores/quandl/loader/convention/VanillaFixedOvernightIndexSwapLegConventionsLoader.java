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

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Creates {@link SwapFixedLegConvention}s from a csv file called "vanilla-fixed-ois-leg-conventions.csv".
 * These conventions are used to construct the fixed leg of a vanilla fixed/overnight indexed swap.
 */
public final class VanillaFixedOvernightIndexSwapLegConventionsLoader implements ConventionsLoader<SwapFixedLegConvention> {
  /** An instance of this loader. */
  public static final VanillaFixedOvernightIndexSwapLegConventionsLoader INSTANCE = new VanillaFixedOvernightIndexSwapLegConventionsLoader();
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(VanillaFixedOvernightIndexSwapLegConventionsLoader.class);
  /** The file name */
  private static final String FILE_NAME = "vanilla-fixed-ois-leg-conventions.csv";

  /**
   * Restricted constructor.
   */
  private VanillaFixedOvernightIndexSwapLegConventionsLoader() {
  }

  /**
   * Generates {@link SwapFixedLegConvention}s from a csv file.
   * @return  a set of conventions, or an empty set if the file was not available or no conventions could be created
   * @throws Exception  if there is a problem reading the file
   */
  @Override
  public Set<SwapFixedLegConvention> loadConventionsFromFile() throws Exception {
    try (InputStream resource = getClass().getResourceAsStream(FILE_NAME)) {
      if (resource == null) {
        LOGGER.error("Could not open file called {}", FILE_NAME);
        return Collections.emptySet();
      }
      try (CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(resource)))) {
        String[] line = reader.readNext(); // ignore headers
        final Set<SwapFixedLegConvention> conventions = new HashSet<>();
        while ((line = reader.readNext()) != null) {
          try {
            final String name = line[0];
            final Currency currency = Currency.of(line[1]);
            final Tenor tenor = Tenor.parse(line[2]);
            final DayCount dayCount = DayCountFactory.of(line[3]);
            final BusinessDayConvention businessDayConvention = BusinessDayConventionFactory.of(line[4]);
            final ExternalId regionCalendar = line[5].equalsIgnoreCase("TARGET")
                ? ExternalSchemes.financialRegionId("EU") : ExternalSchemes.countryRegionId(Country.of(line[5]));
            final int settlementDays = Integer.parseInt(line[6]);
            final boolean isEom = Boolean.parseBoolean(line[7]);
            final StubType stubType = StubType.valueOf(line[8].toUpperCase());
            final boolean isExchangeNotional = Boolean.parseBoolean(line[9]);
            final int paymentLag = Integer.parseInt(line[10]);
            final ExternalIdBundle idBundle = ExternalIdBundle.of(ExternalId.of("CONVENTION", name));
            conventions.add(new SwapFixedLegConvention(name, idBundle, tenor, dayCount, businessDayConvention, currency,
                regionCalendar, settlementDays, isEom, stubType, isExchangeNotional, paymentLag));
          } catch (final Exception e) {
            LOGGER.error(e.getMessage());
          }
        }
        return conventions;
      }
    }
  }
}
