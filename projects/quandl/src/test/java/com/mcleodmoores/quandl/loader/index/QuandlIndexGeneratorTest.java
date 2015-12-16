/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.loader.index;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.testng.annotations.Test;

import au.com.bytecode.opencsv.CSVReader;

import com.mcleodmoores.quandl.QuandlConstants;
import com.mcleodmoores.quandl.loader.index.QuandlIndexGenerator;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.financial.security.index.SwapIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link QuandlIndexGenerator}.
 */
public class QuandlIndexGeneratorTest {
  /** The loader */
  private static final QuandlIndexGenerator LOADER = new QuandlIndexGenerator();

  /**
   * Tests that null is returned if the inputs are null.
   */
  @Test
  public void testNullInputs() {
    assertNull(LOADER.createSecurity(null));
  }

  /**
   * Tests that null is returned if there is insufficient data in the inputs.
   */
  @Test
  public void testBadInputs() {
    assertNull(LOADER.createSecurity(new String[] {"FRED/USD1MTD156N"}));
  }

  /**
   * Tests that null is returned if an ibor index line does not have sufficient data.
   */
  @Test
  public void testBadIborIndexInputs() {
    assertNull(LOADER.createSecurity(new String[] {"FRED/USD1MTD156N", "IBOR INDEX", "NAME"}));
  }

  /**
   * Tests that null is returned if an swap index line does not have sufficient data.
   */
  @Test
  public void testBadSwapIndexInputs() {
    assertNull(LOADER.createSecurity(new String[] {"FRED/DSWP1", "SWAP INDEX", "NAME"}));
  }

  /**
   * Tests that null is returned if an unparseable tenor is supplied for an ibor index.
   */
  @Test
  public void testBadTenorStringForIborIndex() {
    assertNull(LOADER.createSecurity(new String[] {"FRED/USD1MTD156N", "IBOR INDEX", "NAME", "3M"}));
  }

  /**
   * Tests that null is returned if an unparseable tenor is supplied for a swap index.
   */
  @Test
  public void testBadTenorStringForSwapIndex() {
    assertNull(LOADER.createSecurity(new String[] {"FRED/DSWP8", "SWAP INDEX", "NAME", "8Y"}));
  }

  /**
   * Tests that null is returned if an unsupported index category is provided.
   */
  @Test
  public void testBadCategory() {
    assertNull(LOADER.createSecurity(new String[] {"FRED/USD1MTD156N", "TEST", "NAME"}));
  }

  /**
   * Tests the creation of ibor indices from a file.
   * @throws IOException If the test data file could not be opened
   */
  @Test
  public void testLoadIborIndexFromFile() throws IOException {
    try (InputStream resource = QuandlIndexGenerator.class.getResourceAsStream("IborIndex.csv")) {
      if (resource == null) {
        fail("Could not get file called IborIndex.csv");
      }
      try (CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(resource)))) {
        final List<String[]> lines = reader.readAll();

        // all fields filled in
        String[] iborIndexData = lines.get(1); // ignore headers
        ManageableSecurity expectedSecurity = new IborIndex("USD 1m Libor", Tenor.ONE_MONTH, ExternalId.of("CONVENTION", "USD Libor"));
        expectedSecurity.setExternalIdBundle(ExternalIdBundle.of(QuandlConstants.ofCode("FRED/USD1MTD156N"), ExternalId.of("QUANDL_RATE", "USD 1m Libor")));
        assertEquals(expectedSecurity, LOADER.createSecurity(iborIndexData));

        // bad convention identifier - will use Quandl code
        iborIndexData = lines.get(2);
        expectedSecurity = new IborIndex("USD 3m Libor", Tenor.THREE_MONTHS, QuandlConstants.ofCode("FRED/USD3MTD156N"));
        expectedSecurity.setExternalIdBundle(ExternalIdBundle.of(QuandlConstants.ofCode("FRED/USD3MTD156N"), ExternalId.of("QUANDL_RATE", "USD 3m Libor")));
        assertEquals(expectedSecurity, LOADER.createSecurity(iborIndexData));

        // no convention identifier - will use Quandl code
        iborIndexData = lines.get(3);
        expectedSecurity = new IborIndex("USD 6m Libor", Tenor.SIX_MONTHS, QuandlConstants.ofCode("FRED/USD6MTD156N"));
        expectedSecurity.setExternalIdBundle(ExternalIdBundle.of(QuandlConstants.ofCode("FRED/USD6MTD156N"), ExternalId.of("QUANDL_RATE", "USD 6m Libor")));
        assertEquals(expectedSecurity, LOADER.createSecurity(iborIndexData));

        // no additional identifiers
        iborIndexData = lines.get(4);
        expectedSecurity = new IborIndex("USD 12m Libor", Tenor.ofMonths(12), ExternalId.of("CONVENTION", "USD Libor"));
        expectedSecurity.setExternalIdBundle(ExternalIdBundle.of(QuandlConstants.ofCode("FRED/USD12MTD156N")));
        assertEquals(expectedSecurity, LOADER.createSecurity(iborIndexData));
      }
    }
  }

  /**
   * Tests the creation of overnight indices from a file.
   * @throws IOException If the test data file could not be opened
   */
  @Test
  public void testLoadOvernightIndexFromFile() throws IOException {
    try (InputStream resource = QuandlIndexGenerator.class.getResourceAsStream("OvernightIndex.csv")) {
      if (resource == null) {
        fail("Could not get file called OvernightIndex.csv");
      }
      try (CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(resource)))) {
        final List<String[]> lines = reader.readAll();

        // all fields filled in
        String[] overnightIndexData = lines.get(1); // ignore headers
        ManageableSecurity expectedSecurity = new OvernightIndex("USD Overnight", ExternalId.of("CONVENTION", "USD Overnight"));
        expectedSecurity.setExternalIdBundle(ExternalIdBundle.of(QuandlConstants.ofCode("FRED/USDONTD156N"), ExternalId.of("QUANDL_RATE", "USD Overnight")));
        assertEquals(expectedSecurity, LOADER.createSecurity(overnightIndexData));

        // bad convention identifier - will use Quandl code
        overnightIndexData = lines.get(2);
        expectedSecurity = new OvernightIndex("USD Overnight", QuandlConstants.ofCode("FRED/USDONTD156N"));
        expectedSecurity.setExternalIdBundle(ExternalIdBundle.of(QuandlConstants.ofCode("FRED/USDONTD156N"), ExternalId.of("QUANDL_RATE", "USD Overnight")));
        assertEquals(expectedSecurity, LOADER.createSecurity(overnightIndexData));

        // no convention identifier - will use Quandl code
        overnightIndexData = lines.get(3);
        expectedSecurity = new OvernightIndex("USD Overnight", QuandlConstants.ofCode("FRED/USDONTD156N"));
        expectedSecurity.setExternalIdBundle(ExternalIdBundle.of(QuandlConstants.ofCode("FRED/USDONTD156N"), ExternalId.of("QUANDL_RATE", "USD Overnight")));
        assertEquals(expectedSecurity, LOADER.createSecurity(overnightIndexData));

        // no additional identifiers
        overnightIndexData = lines.get(4);
        expectedSecurity = new OvernightIndex("USD Overnight", ExternalId.of("CONVENTION", "USD Overnight"));
        expectedSecurity.setExternalIdBundle(ExternalIdBundle.of(QuandlConstants.ofCode("FRED/USDONTD156N")));
        assertEquals(expectedSecurity, LOADER.createSecurity(overnightIndexData));
      }
    }
  }

  /**
   * Tests the creation of swap indices from a file.
   * @throws IOException If the test data file could not be opened
   */
  @Test
  public void testLoadSwapIndexFromFile() throws IOException {
    try (InputStream resource = QuandlIndexGenerator.class.getResourceAsStream("SwapIndex.csv")) {
      if (resource == null) {
        fail("Could not get file called SwapIndex.csv");
      }
      try (CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(resource)))) {
        final List<String[]> lines = reader.readAll();

        // all fields filled in
        String[] swapIndex = lines.get(1); // ignore headers
        ManageableSecurity expectedSecurity = new SwapIndex("USD 1y Swap", Tenor.ONE_YEAR, ExternalId.of("CONVENTION", "USD Swap Index"));
        expectedSecurity.setExternalIdBundle(ExternalIdBundle.of(QuandlConstants.ofCode("FRED/DSWP1"), ExternalId.of("QUANDL_RATE", "USD Swap Index")));
        assertEquals(expectedSecurity, LOADER.createSecurity(swapIndex));

        // bad convention identifier - will use Quandl code
        swapIndex = lines.get(2);
        expectedSecurity = new SwapIndex("USD 2y Swap", Tenor.TWO_YEARS, QuandlConstants.ofCode("FRED/DSWP2"));
        expectedSecurity.setExternalIdBundle(ExternalIdBundle.of(QuandlConstants.ofCode("FRED/DSWP2"), ExternalId.of("QUANDL_RATE", "USD Swap Index")));
        assertEquals(expectedSecurity, LOADER.createSecurity(swapIndex));

        // no convention identifier - will use Quandl code
        swapIndex = lines.get(3);
        expectedSecurity = new SwapIndex("USD 3y Swap", Tenor.THREE_YEARS, QuandlConstants.ofCode("FRED/DSWP3"));
        expectedSecurity.setExternalIdBundle(ExternalIdBundle.of(QuandlConstants.ofCode("FRED/DSWP3"), ExternalId.of("QUANDL_RATE", "USD Swap Index")));
        assertEquals(expectedSecurity, LOADER.createSecurity(swapIndex));

        // no additional identifiers
        swapIndex = lines.get(4);
        expectedSecurity = new SwapIndex("USD 4y Swap", Tenor.FOUR_YEARS, ExternalId.of("CONVENTION", "USD Swap Index"));
        expectedSecurity.setExternalIdBundle(ExternalIdBundle.of(QuandlConstants.ofCode("FRED/DSWP4")));
        assertEquals(expectedSecurity, LOADER.createSecurity(swapIndex));
      }
    }
  }
}
