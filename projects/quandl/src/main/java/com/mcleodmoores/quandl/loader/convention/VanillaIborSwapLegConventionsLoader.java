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

import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.time.Tenor;

/**
 * Creates {@link VanillaIborLegConvention}s from a csv file called "vanilla-ibor-swap-leg-conventions.csv".
 * These conventions are used to construct the ibor leg of a vanilla fixed/ibor swap.
 */
public final class VanillaIborSwapLegConventionsLoader implements ConventionsLoader<VanillaIborLegConvention> {
  /** An instance of this loader. */
  public static final VanillaIborSwapLegConventionsLoader INSTANCE = new VanillaIborSwapLegConventionsLoader();
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(VanillaIborSwapLegConventionsLoader.class);
  /** The file name */
  private static final String FILE_NAME = "vanilla-ibor-swap-leg-conventions.csv";

  /**
   * Restricted constructor.
   */
  private VanillaIborSwapLegConventionsLoader() {
  }

  /**
   * Generates {@link VanillaIborLegConvention}s from a csv file.
   * @return  a set of conventions, or an empty set if the file was not available or no conventions could be created
   * @throws Exception  if there is a problem reading the file
   */
  @Override
  public Set<VanillaIborLegConvention> loadConventionsFromFile() throws Exception {
    try (InputStream resource = getClass().getResourceAsStream(FILE_NAME)) {
      if (resource == null) {
        LOGGER.error("Could not open file called {}", FILE_NAME);
        return Collections.emptySet();
      }
      try (CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(resource)))) {
        String[] line = reader.readNext(); // ignore headers
        final Set<VanillaIborLegConvention> conventions = new HashSet<>();
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
            final Tenor resetTenor = Tenor.parse(line[2]);
            final int settlementDays = Integer.parseInt(line[3]);
            final boolean isEom = Boolean.parseBoolean(line[4]);
            final StubType stubType = StubType.valueOf(line[5].toUpperCase());
            final boolean isExchangeNotional = Boolean.parseBoolean(line[6]);
            final int paymentLag = Integer.parseInt(line[7]);
            final boolean isAdvanceFixing = Boolean.parseBoolean(line[8]);
            final String interpolationMethod = line[9];
            final ExternalIdBundle idBundle = ExternalIdBundle.of(ExternalId.of("CONVENTION", name));
            conventions.add(new VanillaIborLegConvention(name, idBundle, indexConventionId, isAdvanceFixing, interpolationMethod,
                resetTenor, settlementDays, isEom, stubType, isExchangeNotional, paymentLag));
          } catch (final Exception e) {
            LOGGER.error(e.getMessage());
          }
        }
        return conventions;
      }
    }
  }
}
