/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.id;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZoneId;

import com.opengamma.id.ExternalId;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link ExternalSchemes}.
 */
@Test(groups = TestGroup.UNIT)
public class ExternalSchemesTest {

  /**
   * Tests the constant strings.
   */
  @SuppressWarnings("deprecation")
  public void testConstants() {
    assertEquals("ISIN", ExternalSchemes.ISIN.getName());
    assertEquals("CUSIP", ExternalSchemes.CUSIP.getName());
    assertEquals("SEDOL1", ExternalSchemes.SEDOL1.getName());
    assertEquals("BLOOMBERG_BUID", ExternalSchemes.BLOOMBERG_BUID.getName());
    assertEquals("BLOOMBERG_TICKER", ExternalSchemes.BLOOMBERG_TICKER.getName());
    assertEquals("BLOOMBERG_TCM", ExternalSchemes.BLOOMBERG_TCM.getName());
    assertEquals("RIC", ExternalSchemes.RIC.getName());
    assertEquals("MARKIT_RED_CODE", ExternalSchemes.MARKIT_RED_CODE.getName());
    assertEquals("ISDA", ExternalSchemes.ISDA.getName());
    assertEquals("BLOOMBERG_UUID", ExternalSchemes.BLOOMBERG_UUID.getName());
    assertEquals("BLOOMBERG_EMRSID", ExternalSchemes.BLOOMBERG_EMRSID.getName());
  }

  /**
   * Tests that the identifiers match.
   */
  public void testIdentifiers() {
    assertEquals(ExternalId.of("ISIN", "A"), ExternalSchemes.isinSecurityId("A"));
    assertEquals(ExternalId.of("CUSIP", "A"), ExternalSchemes.cusipSecurityId("A"));
    assertEquals(ExternalId.of("SEDOL1", "A"), ExternalSchemes.sedol1SecurityId("A"));
    assertEquals(ExternalId.of("BLOOMBERG_BUID", "A"), ExternalSchemes.bloombergBuidSecurityId("A"));
    assertEquals(ExternalId.of("BLOOMBERG_TICKER", "A"), ExternalSchemes.bloombergTickerSecurityId("A"));
    assertEquals(ExternalId.of("BLOOMBERG_TCM", "T 4.75 15/08/43 Govt"), ExternalSchemes.bloombergTCMSecurityId("T", "4.75", "15/08/43", "Govt"));
    assertEquals(ExternalId.of("RIC", "A"), ExternalSchemes.ricSecurityId("A"));
    assertEquals(ExternalId.of("MARKIT_RED_CODE", "A"), ExternalSchemes.markItRedCode("A"));
    assertEquals(ExternalId.of("ISDA", "A"), ExternalSchemes.isda("A"));
    assertEquals(ExternalId.of("WINDOWS_USER_ID", "A"), ExternalSchemes.windowsUserId("A"));
    assertEquals(ExternalId.of("BLOOMBERG_UUID", "A"), ExternalSchemes.bloombergUuidUserId("A"));
    assertEquals(ExternalId.of("BLOOMBERG_EMRSID", "A"), ExternalSchemes.bloombergEmrsUserId("A"));
    assertEquals(ExternalId.of("OG_SYNTHETIC_TICKER", "A"), ExternalSchemes.syntheticSecurityId("A"));
    assertEquals(ExternalId.of("ACTIVFEED_TICKER", "A"), ExternalSchemes.activFeedTickerSecurityId("A"));
    assertEquals(ExternalId.of("SURF", "A"), ExternalSchemes.tullettPrebonSecurityId("A"));
    assertEquals(ExternalId.of("ICAP", "A"), ExternalSchemes.icapSecurityId("A"));
    assertEquals(ExternalId.of("GMI", "A"), ExternalSchemes.gmiSecurityId("A"));
    assertEquals(ExternalId.of("FINANCIAL_REGION", "US"), ExternalSchemes.financialRegionId("US"));
    assertEquals(ExternalId.of("TZDB_TIME_ZONE", "Europe/London"), ExternalSchemes.timeZoneRegionId(ZoneId.of("Europe/London")));
    assertEquals(ExternalId.of("COPP_CLARK_LOCODE", "US123"), ExternalSchemes.coppClarkRegionId("US123"));
    assertEquals(ExternalId.of("UN_LOCODE_2010_2", "US123"), ExternalSchemes.unLocode20102RegionId("US123"));
    assertEquals(ExternalId.of("ISO_CURRENCY_ALPHA3", "USD"), ExternalSchemes.currencyRegionId(Currency.USD));
    assertEquals(ExternalId.of("ISO_COUNTRY_ALPHA2", "US"), ExternalSchemes.countryRegionId(Country.US));
    assertEquals(ExternalId.of("ISO_MIC", "NY90"), ExternalSchemes.isoMicExchangeId("NY90"));
    assertEquals(ExternalId.of("ISDA_HOLIDAY", "CAD"), ExternalSchemes.isdaHoliday("CAD"));
  }

  /**
   * Tests that the synthetic string cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSyntheticNull() {
    ExternalSchemes.syntheticSecurityId(null);
  }

  /**
   * Tests that the synthetic string cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSyntheticEmpty() {
    ExternalSchemes.syntheticSecurityId("");
  }

  /**
   * Tests that the ISIN string cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIsinNull() {
    ExternalSchemes.isinSecurityId(null);
  }

  /**
   * Tests that the ISIN string cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIsinEmpty() {
    ExternalSchemes.isinSecurityId("");
  }

  /**
   * Tests that the CUSIP string cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCusipNull() {
    ExternalSchemes.cusipSecurityId(null);
  }

  /**
   * Tests that the CUSIP string cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCusipEmpty() {
    ExternalSchemes.cusipSecurityId("");
  }

  /**
   * Tests that the SEDOL string cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSedol1Null() {
    ExternalSchemes.sedol1SecurityId(null);
  }

  /**
   * Tests that the SEDOL string cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSedol1Empty() {
    ExternalSchemes.sedol1SecurityId("");
  }

  /**
   * Tests that the BUID cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBloombergBuidNull() {
    ExternalSchemes.bloombergBuidSecurityId(null);
  }

  /**
   * Tests that the BUID cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBloombergBuidEmpty() {
    ExternalSchemes.bloombergBuidSecurityId("");
  }

  /**
   * Tests that the UUID cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBoombergUuidNull() {
    ExternalSchemes.bloombergUuidUserId(null);
  }

  /**
   * Tests that the UUID cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBoombergUuidEmpty() {
    ExternalSchemes.bloombergUuidUserId("");
  }

  /**
   * Tests that the EMRS cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBloombergEmrsidNull() {
    ExternalSchemes.bloombergEmrsUserId(null);
  }

  /**
   * Tests that the EMRS cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBloombergEmrsidEmpty() {
    ExternalSchemes.bloombergEmrsUserId("");
  }

  /**
   * Tests that the ticker cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBloombergTickerNull() {
    ExternalSchemes.bloombergTickerSecurityId(null);
  }

  /**
   * Tests that the ticker cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBloombergTickerEmpty() {
    ExternalSchemes.bloombergTickerSecurityId("");
  }

  /**
   * Tests that the TCM ticker cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBloombergTCMTickerNull() {
    ExternalSchemes.bloombergTCMSecurityId(null, "coupon", "maturity", "sector");
  }

  /**
   * Tests that the TCM ticker cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBloombergTCMTickerEmpty() {
    ExternalSchemes.bloombergTCMSecurityId("", "coupon", "maturity", "sector");
  }

  /**
   * Tests that the TCM coupon cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBloombergTCMCouponNull() {
    ExternalSchemes.bloombergTCMSecurityId("ticker", null, "maturity", "sector");
  }

  /**
   * Tests that the TCM coupon cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBloombergTCMCouponEmpty() {
    ExternalSchemes.bloombergTCMSecurityId("ticker", "", "maturity", "sector");
  }

  /**
   * Tests that the TCM coupon must be a double.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBloombergTCMCouponNotParsable() {
    ExternalSchemes.bloombergTCMSecurityId("ticker", "abc", "maturity", "sector");
  }

  /**
   * Tests that the TCM maturity cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBloombergTCMMaturityNull() {
    ExternalSchemes.bloombergTCMSecurityId("ticker", "coupon", null, "sector");
  }

  /**
   * Tests that the TCM maturity cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBloombergTCMMaturityEmpty() {
    ExternalSchemes.bloombergTCMSecurityId("ticker", "coupon", "", "sector");
  }

  /**
   * Tests that the TCM sector cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBloombergTCMSectorNull() {
    ExternalSchemes.bloombergTCMSecurityId("ticker", "coupon", "maturity", null);
  }

  /**
   * Tests that the TCM sector cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBloombergTCMSectorEmpty() {
    ExternalSchemes.bloombergTCMSecurityId("ticker", "coupon", "maturity", "");
  }

  /**
   * Tests that the RIC cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testRicNull() {
    ExternalSchemes.ricSecurityId(null);
  }

  /**
   * Tests that the RIC cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testRicEmpty() {
    ExternalSchemes.ricSecurityId("");
  }

  /**
   * Tests that the Markit id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMarkitRedCodeNull() {
    ExternalSchemes.markItRedCode(null);
  }

  /**
   * Tests that the Markit id cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMarkitRedCodeEmpty() {
    ExternalSchemes.markItRedCode("");
  }

  /**
   * Tests that the ISDA code cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIsdaNull() {
    ExternalSchemes.isda(null);
  }

  /**
   * Tests that the ISDA code cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIsdaEmpty() {
    ExternalSchemes.isda("");
  }

  /**
   * Tests that the ActivFeed code cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testActivFeedNull() {
    ExternalSchemes.activFeedTickerSecurityId(null);
  }

  /**
   * Tests that the ActivFeed code cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testActivFeedEmpty() {
    ExternalSchemes.activFeedTickerSecurityId("");
  }

  /**
   * Tests that the Tullet code cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testTulletNull() {
    ExternalSchemes.tullettPrebonSecurityId(null);
  }

  /**
   * Tests that the ActivFeed code cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testTulletEmpty() {
    ExternalSchemes.tullettPrebonSecurityId("");
  }

  /**
   * Tests that the ICAP code cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIcapNull() {
    ExternalSchemes.icapSecurityId(null);
  }

  /**
   * Tests that the ICAP code cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIcapEmpty() {
    ExternalSchemes.icapSecurityId("");
  }

  /**
   * Tests that the GMI code cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGmiNull() {
    ExternalSchemes.gmiSecurityId(null);
  }

  /**
   * Tests that the GMI code cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGmiEmpty() {
    ExternalSchemes.gmiSecurityId("");
  }

  /**
   * Tests that the financial region id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFinancialRegionIdNull() {
    ExternalSchemes.financialRegionId(null);
  }

  /**
   * Tests that the financial region id must be of a certain form.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFinancialRegionIdNotParse() {
    ExternalSchemes.gmiSecurityId("");
  }

  /**
   * Tests an invalid financial region.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalidRegion() {
    ExternalSchemes.financialRegionId("USD123");
  }

  /**
   * Tests an invalid Copp Clark region identifier.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalidCoppClarkRegionId() {
    ExternalSchemes.coppClarkRegionId("");
  }

  /**
   * Tests an invalid UN locode.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalidUnLocode() {
    ExternalSchemes.unLocode20102RegionId("ABCDEFEG");
  }

  /**
   * Tests an invalid ISO MIC code.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalidIsoMic() {
    ExternalSchemes.isoMicExchangeId("");
  }
}
