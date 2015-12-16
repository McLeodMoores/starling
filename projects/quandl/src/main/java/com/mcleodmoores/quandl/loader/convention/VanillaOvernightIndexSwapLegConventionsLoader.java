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

import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.time.Tenor;

/**
 * Creates {@link OISLegConvention}s from a csv file called "vanilla-ois-leg-conventions.csv".
 * These conventions are used to construct the overnight-indexed leg of a vanilla overnight-indexed
 * swap.
 */
public final class VanillaOvernightIndexSwapLegConventionsLoader implements ConventionsLoader<OISLegConvention> {
  /** An instance of this loader. */
  public static final VanillaOvernightIndexSwapLegConventionsLoader INSTANCE = new VanillaOvernightIndexSwapLegConventionsLoader();
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(VanillaOvernightIndexSwapLegConventionsLoader.class);
  /** The file name */
  private static final String FILE_NAME = "vanilla-ois-leg-conventions.csv";

  /**
   * Restricted constructor.
   */
  private VanillaOvernightIndexSwapLegConventionsLoader() {
  }

  /**
   * Generates {@link OISLegConvention}s from a csv file.
   * @return  a set of conventions, or an empty set if the file was not available or no conventions could be created
   * @throws Exception  if there is a problem reading the file
   */
  @Override
  public Set<OISLegConvention> loadConventionsFromFile() throws Exception {
    try (InputStream resource = getClass().getResourceAsStream(FILE_NAME)) {
      if (resource == null) {
        LOGGER.error("Could not open file called {}", FILE_NAME);
        return Collections.emptySet();
      }
      try (CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(resource)))) {
        String[] line = reader.readNext(); // ignore headers
        final Set<OISLegConvention> conventions = new HashSet<>();
        while ((line = reader.readNext()) != null) {
          try {
            final String name = line[0];
            final String indexConventionString = line[1];
            final ExternalId indexConventionId;
            if (indexConventionString.contains("~")) {
              // assume that the full convention identifier has been used
              indexConventionId = ExternalId.parse(indexConventionString);
            } else {
              indexConventionId = ExternalId.of("CONVENTION", indexConventionString);
            }
            final Tenor paymentTenor = Tenor.parse(line[2]);
            final BusinessDayConvention businessDayConvention = BusinessDayConventionFactory.of(line[3]);
            final int settlementDays = Integer.parseInt(line[4]);
            final boolean isEom = Boolean.parseBoolean(line[5]);
            final StubType stubType = StubType.valueOf(line[6].toUpperCase());
            final boolean isExchangeNotional = Boolean.parseBoolean(line[7]);
            final int paymentLag = Integer.parseInt(line[8]);
            final ExternalIdBundle idBundle = ExternalIdBundle.of(ExternalId.of("CONVENTION", name));
            conventions.add(new OISLegConvention(name, idBundle, indexConventionId, paymentTenor, businessDayConvention,
                settlementDays, isEom, stubType, isExchangeNotional, paymentLag));
          } catch (final Exception e) {
            LOGGER.error(e.getMessage());
          }
        }
        return conventions;
      }
    }
  }
}
