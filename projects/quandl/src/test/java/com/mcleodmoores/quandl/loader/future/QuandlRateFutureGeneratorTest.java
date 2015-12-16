/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.loader.future;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.quandl.QuandlConstants;
import com.mcleodmoores.quandl.convention.QuandlFedFundsFutureConvention;
import com.mcleodmoores.quandl.convention.QuandlStirFutureConvention;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.engine.InMemoryConventionSource;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.security.future.FederalFundsFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;
import com.opengamma.util.time.Tenor;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Unit tests for {@link QuandlRateFutureGenerator}.
 */
@Test(groups = TestGroup.UNIT)
public class QuandlRateFutureGeneratorTest {
  /** The loader */
  private static final QuandlRateFutureGenerator LOADER = new QuandlRateFutureGenerator();
  /** A convention source */
  private static final InMemoryConventionSource CONVENTION_SOURCE = new InMemoryConventionSource();

  static {
    final Convention stirConvention1 = new QuandlStirFutureConvention("CME/ER", QuandlConstants.ofCode("CME/ERZ2014").toBundle(), Currency.EUR,
        Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, "16:00", "America/Chicago", 2500, QuandlConstants.ofCode("FRED/EUR3MTD156N"), 3, DayOfWeek.MONDAY.name(),
        "CME", "CME", ExternalSchemes.countryRegionId(Country.US));
    final Convention stirConvention2 = new QuandlStirFutureConvention("CME/ED", QuandlConstants.ofPrefix("CME/ED").toBundle(), Currency.USD,
        Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, "16:00", "America/Chicago", 2500, QuandlConstants.ofCode("FRED/USD3MTD156N"), 3, DayOfWeek.MONDAY.name(),
        "CME", "CME", ExternalSchemes.countryRegionId(Country.US));
    CONVENTION_SOURCE.addConvention(stirConvention1);
    CONVENTION_SOURCE.addConvention(stirConvention2);
    final Convention ffConvention1 = new QuandlFedFundsFutureConvention("CME/FF", QuandlConstants.ofPrefix("CME/FF").toBundle(), "16:00",
        "America/Chicago", 500000, QuandlConstants.ofCode("FRED/FF"), "CME", "CME");
    final Convention ffConvention2 = new QuandlFedFundsFutureConvention("CBOT/FF", QuandlConstants.ofPrefix("CBOT/FF").toBundle(), "16:00",
        "America/Chicago", 500000, QuandlConstants.ofCode("FRED/FF"), "CBOT", "CBOT");
    CONVENTION_SOURCE.addConvention(ffConvention1);
    CONVENTION_SOURCE.addConvention(ffConvention2);
  }

  /**
   * Tests that null is returned when the line is null.
   */
  @Test
  public void testNullLine() {
    assertNull(LOADER.createSecurity(null));
  }

  /**
   * Tests that null is returned when the number of entries on a line is incorrect.
   */
  @Test
  public void testWrongNumberOfEntries() {
    assertNull(LOADER.createSecurity(new String[] {"CME/EDZ2014", "STIR FUTURE", "2014-12-17"}));
  }

  /**
   * Tests that null is returned when the expiry could not be constructed because the
   * date format is incorrect.
   */
  @Test
  public void testBadExpiryString() {
    final String[] line = new String[] {"CME/EDZ2014", "STIR FUTURE", "2014/12/17", "2500", "America/Chicago", "09:30-16:00", "CME", "CME",
        "USD", "FRED/USD3MTD156N"};
    assertNull(LOADER.createSecurity(line));
  }

  /**
   * Tests that null is returned when the expiry could not be constructed because the
   * trading times format is empty.
   */
  @Test
  public void testBadLastTradeTimeString1() {
    final String[] line = new String[] {"CME/EDZ2014", "STIR FUTURE", "2014-12-17", "2500", "America/Chicago", "", "CME", "CME",
        "USD", "FRED/USD3MTD156N"};
    assertNull(LOADER.createSecurity(line));
  }

  /**
   * Tests that null is returned when the expiry could not be constructed because the
   * trading times format is incorrect.
   */
  @Test
  public void testBadLastTradeTimeString2() {
    final String[] line = new String[] {"CME/EDZ2014", "STIR FUTURE", "2014-12-17", "2500", "America/Chicago", "09:30;16:00", "CME", "CME",
        "USD", "FRED/USD3MTD156N"};
    assertNull(LOADER.createSecurity(line));
  }

  /**
   * Tests that null is returned when the expiry could not be constructed because the
   * last time format is incorrect.
   */
  @Test
  public void testBadLastTradeTimeString3() {
    final String[] line = new String[] {"CME/EDZ2014", "STIR FUTURE", "2014-12-17", "2500", "America/Chicago", "09:30-16:00 17:00", "CME", "CME",
        "USD", "FRED/USD3MTD156N"};
    assertNull(LOADER.createSecurity(line));
  }

  /**
   * Tests that null is returned when the unit amount could not be parsed.
   */
  @Test
  public void testBadUnitAmountString() {
    final String[] line = new String[] {"CME/EDZ2014", "STIR FUTURE", "2014-12-17", "250O", "America/Chicago", "09:30-16:00", "CME", "CME",
        "USD", "FRED/USD3MTD156N"};
    assertNull(LOADER.createSecurity(line));
  }

  /**
   * Tests that null is returned when the currency could not be parsed.
   */
  @Test
  public void testBadCurrencyString() {
    final String[] line = new String[] {"CME/EDZ2014", "STIR FUTURE", "2014-12-17", "2500", "America/Chicago", "09:30-16:00", "CME", "CME",
        "USDA", "FRED/USD3MTD156N"};
    assertNull(LOADER.createSecurity(line));
  }

  /**
   * Tests that null is returned when the underlying id could not be created.
   */
  @Test
  public void testBadUnderlyingIdString() {
    final String[] line = new String[] {"CME/EDZ2014", "STIR FUTURE", "2014-12-17", "2500", "America/Chicago", "09:30-16:00", "CME", "CME",
        "USD", ""};
    assertNull(LOADER.createSecurity(line));
  }

  /**
   * Tests that null is returned when the category is not handled.
   */
  @Test
  public void testUnhandledCategory() {
    final String[] line = new String[] {"CME/EDZ2014", "IR FUTURE", "2014-12-17", "2500", "America/Chicago", "09:30-16:00", "CME", "CME",
        "USD", "FRED/USD3MTD156N"};
    assertNull(LOADER.createSecurity(line));
  }

  /**
   * Tests the exception thrown when the convention source is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConventionSource() {
    LOADER.createSecurity(null, "CME/EDZ2014");
  }

  /**
   * Tests that null is returned when the Quandl code is null.
   */
  @Test
  public void testNullQuandlCode() {
    assertNull(LOADER.createSecurity(CONVENTION_SOURCE, null));
  }

  /**
   * Tests that null is returned when the Quandl code is too short.
   */
  @Test
  public void testShortCode() {
    assertNull(LOADER.createSecurity(CONVENTION_SOURCE, "Z2014"));
  }

  /**
   * Tests that null is returned when the last four characters of the code cannot
   * be parsed into a year.
   */
  @Test
  public void testBadYearInCode() {
    assertNull(LOADER.createSecurity(CONVENTION_SOURCE, "CME/ED2014Z"));
  }

  /**
   * Tests that null is returned when the month code is invalid.
   */
  @Test
  public void testBadMonthCode() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    final Convention convention = new QuandlStirFutureConvention("CME/ED", QuandlConstants.ofCode("CME/EDC2014").toBundle(), Currency.USD,
        Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, "16:00", "America/Chicago", 2500, QuandlConstants.ofCode("FRED/USD3MTD156N"), 3, DayOfWeek.MONDAY.name(),
        ExternalSchemes.countryRegionId(Country.US));
    conventionSource.addConvention(convention);
    assertNull(LOADER.createSecurity(conventionSource, "CME/EDC2014"));
  }

  /**
   * Tests that null is returned if a convention could not be found for either the prefix
   * or the full code.
   */
  @Test
  public void testNoConvention() {
    assertNull(LOADER.createSecurity(new InMemoryConventionSource(), "CME/EDZ2014"));
  }

  /**
   * Test that null is returned for an unsupported convention type.
   */
  @Test
  public void testUnhandledConventionType() {
    final Convention convention = new DepositConvention("Test", QuandlConstants.ofCode("CME/EDZ2014").toBundle(), DayCounts.ACT_360,
        BusinessDayConventions.FOLLOWING, 0, false, Currency.USD, ExternalSchemes.financialRegionId("US"));
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(convention);
    assertNull(LOADER.createSecurity(conventionSource, "CME/EDZ2014"));
  }

  /**
   * Tests the creation of an interest rate future security from a file.
   * @throws IOException If the test data file could not be opened
   */
  @Test
  public void testLoadStirFutureFromFile() throws IOException {
    try (InputStream resource = QuandlRateFutureGenerator.class.getResourceAsStream("StirFuture.csv")) {
      if (resource == null) {
        fail("Could not get file called StirFuture.csv");
      }
      try (CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(resource)))) {
        reader.readNext(); //ignore headers
        final Security generatedSecurity = LOADER.createSecurity(reader.readNext());
        final ManageableSecurity expectedSecurity =
            new InterestRateFutureSecurity(new Expiry(ZonedDateTime.of(LocalDateTime.of(2014, 12, 17, 16, 0), ZoneOffset.UTC),
                ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR), "CME", "CME", Currency.USD, 2500, QuandlConstants.ofCode("FRED/USD3MTD156N"), "STIR FUTURE");
        expectedSecurity.setName("CME/EDZ2014");
        expectedSecurity.setExternalIdBundle(QuandlConstants.ofCode("CME/EDZ2014").toBundle());
        assertEquals(generatedSecurity, expectedSecurity);
      }
    }
  }

  /**
   * Tests the creation of an interest rate future security from a convention and a code.
   */
  @Test
  public void testLoadStirFutureFromConvention() {
    // convention source contains a convention for the prefix
    Security generatedSecurity = LOADER.createSecurity(CONVENTION_SOURCE, "CME/EDZ2014");
    ManageableSecurity expectedSecurity = new InterestRateFutureSecurity(new Expiry(ZonedDateTime.of(LocalDateTime.of(2014, 12, 15, 16, 0), ZoneOffset.UTC),
        ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR), "CME", "CME", Currency.USD, 2500, QuandlConstants.ofCode("FRED/USD3MTD156N"), "STIR FUTURE");
    expectedSecurity.setName("CME/EDZ2014");
    expectedSecurity.setExternalIdBundle(QuandlConstants.ofCode("CME/EDZ2014").toBundle());
    assertEquals(expectedSecurity, generatedSecurity);
    // convention source does not contain a convention for the prefix but does for the full code
    generatedSecurity = LOADER.createSecurity(CONVENTION_SOURCE, "CME/ERZ2014");
    expectedSecurity = new InterestRateFutureSecurity(new Expiry(ZonedDateTime.of(LocalDateTime.of(2014, 12, 15, 16, 0), ZoneOffset.UTC),
        ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR), "CME", "CME", Currency.EUR, 2500, QuandlConstants.ofCode("FRED/EUR3MTD156N"), "STIR FUTURE");
    expectedSecurity.setName("CME/ERZ2014");
    expectedSecurity.setExternalIdBundle(QuandlConstants.ofCode("CME/ERZ2014").toBundle());
    assertEquals(generatedSecurity, expectedSecurity);
  }

  /**
   * Tests the creation of a Fed funds future security from a file.
   * @throws IOException If the test data file could not be opened
   */
  @Test
  public void testLoadFedFundsFutureFromFile() throws IOException {
    try (InputStream resource = QuandlRateFutureGenerator.class.getResourceAsStream("FedFundsFuture.csv")) {
      if (resource == null) {
        fail("Could not get file called FedFunds.csv");
      }
      try (CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(resource)))) {
        reader.readNext(); //ignore headers
        final Security generatedSecurity = LOADER.createSecurity(reader.readNext());
        final ManageableSecurity expectedSecurity =
            new FederalFundsFutureSecurity(new Expiry(ZonedDateTime.of(LocalDateTime.of(2014, 12, 31, 16, 0), ZoneOffset.UTC),
                ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR), "CME", "CME", Currency.USD, 500000, QuandlConstants.ofCode("FRED/DFF"), "FED FUNDS FUTURE");
        expectedSecurity.setName("CME/FFZ2014");
        expectedSecurity.setExternalIdBundle(QuandlConstants.ofCode("CME/FFZ2014").toBundle());
        assertEquals(generatedSecurity, expectedSecurity);
      }
    }
  }

  /**
   * Tests the creation of Fed funds future security from a convention and a code.
   */
  @Test
  public void testLoadFedFundsFutureFromConvention() {
    // convention source contains a convention for the prefix
    Security generatedSecurity = LOADER.createSecurity(CONVENTION_SOURCE, "CME/FFZ2014");
    ManageableSecurity expectedSecurity = new FederalFundsFutureSecurity(new Expiry(ZonedDateTime.of(LocalDateTime.of(2014, 12, 31, 16, 0), ZoneOffset.UTC),
        ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR), "CME", "CME", Currency.USD, 500000, QuandlConstants.ofCode("FRED/FF"), "FED FUNDS FUTURE");
    expectedSecurity.setName("CME/FFZ2014");
    expectedSecurity.setExternalIdBundle(QuandlConstants.ofCode("CME/FFZ2014").toBundle());
    assertEquals(expectedSecurity, generatedSecurity);
    // convention source does not contain a convention for the prefix but does for the full code
    generatedSecurity = LOADER.createSecurity(CONVENTION_SOURCE, "CBOT/FFZ2014");
    expectedSecurity = new FederalFundsFutureSecurity(new Expiry(ZonedDateTime.of(LocalDateTime.of(2014, 12, 31, 16, 0), ZoneOffset.UTC),
        ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR), "CBOT", "CBOT", Currency.USD, 500000, QuandlConstants.ofCode("FRED/FF"), "FED FUNDS FUTURE");
    expectedSecurity.setName("CBOT/FFZ2014");
    expectedSecurity.setExternalIdBundle(QuandlConstants.ofCode("CBOT/FFZ2014").toBundle());
    assertEquals(generatedSecurity, expectedSecurity);
  }
}
