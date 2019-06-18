/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.holiday.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.net.URI;
import java.net.URISyntaxException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link DataHolidaySourceUris}.
 */
@Test(groups = TestGroup.UNIT)
public class DataHolidaySourceUrisTest {
  private static final VersionCorrection VC = VersionCorrection.of(Instant.ofEpochSecond(10000), Instant.ofEpochSecond(20000));
  private static final UniqueId UID = UniqueId.of("hol", "1");
  private static final ObjectId OID = UID.getObjectId();
  private static final ExternalIdBundle EIDS = ExternalIdBundle.of(ExternalSchemes.countryRegionId(Country.AU));
  private URI _baseUri;

  /**
   * Sets up the URI
   *
   * @throws URISyntaxException
   *           if the path is wrong
   */
  @BeforeMethod
  public void createUri() throws URISyntaxException {
    _baseUri = new URI("path/to/");
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetNullBaseUri1() {
    DataHolidaySourceUris.uriGet(null, UID);
  }

  /**
   * Tests that the unique id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetNullUid() {
    DataHolidaySourceUris.uriGet(_baseUri, (UniqueId) null);
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriGetUidNoVersion() {
    final URI uri = DataHolidaySourceUris.uriGet(_baseUri, UID);
    assertEquals(uri.getPath(), "path/to/holidays/" + UID.toString());
    assertNull(uri.getQuery());
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriGetUidVersion() {
    final UniqueId uid = UID.withVersion(VersionCorrection.LATEST.toString());
    final URI uri = DataHolidaySourceUris.uriGet(_baseUri, uid);
    assertEquals(uri.getPath(), "path/to/holidays/" + UID.toString());
    assertEquals(uri.getQuery(), "version=VLATEST.CLATEST");
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetNullBaseUri2() {
    DataHolidaySourceUris.uriGet(null, OID, VC);
  }

  /**
   * Tests that the object id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetNullOid() {
    DataHolidaySourceUris.uriGet(_baseUri, (ObjectId) null, VC);
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriGetOid() {
    final URI uri = DataHolidaySourceUris.uriGet(_baseUri, OID, null);
    assertEquals(uri.getPath(), "path/to/holidays/" + UID.toString());
    assertNull(uri.getQuery());
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriGetOidVersion() {
    final URI uri = DataHolidaySourceUris.uriGet(_baseUri, OID, VC);
    assertEquals(uri.getPath(), "path/to/holidays/" + OID.toString());
    assertEquals(uri.getQuery(), "versionAsOf=1970-01-01T02:46:40Z&correctedTo=1970-01-01T05:33:20Z");
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetNullBaseUri3() {
    DataHolidaySourceUris.uriGet(null, Currency.AUD);
  }

  /**
   * Tests that the currency cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetNullCurrency() {
    DataHolidaySourceUris.uriGet(_baseUri, (Currency) null);
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriGetCurrency() {
    final URI uri = DataHolidaySourceUris.uriGet(_baseUri, Currency.AUD);
    assertEquals(uri.getPath(), "path/to/holidaySearches/retrieve");
    assertEquals(uri.getQuery(), "currency=AUD");
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetNullBaseUri4() {
    DataHolidaySourceUris.uriGet(null, HolidayType.BANK, EIDS);
  }

  /**
   * Tests that the holiday type cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetNullHolidayType() {
    DataHolidaySourceUris.uriGet(_baseUri, (HolidayType) null, EIDS);
  }

  /**
   * Tests that the external id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetNullEids() {
    DataHolidaySourceUris.uriGet(_baseUri, HolidayType.BANK, null);
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriGetHolidayTypeEids() {
    final URI uri = DataHolidaySourceUris.uriGet(_baseUri, HolidayType.BANK, EIDS);
    assertEquals(uri.getPath(), "path/to/holidaySearches/retrieve");
    assertEquals(uri.getQuery(), "holidayType=BANK&id=ISO_COUNTRY_ALPHA2~AU");
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriSearchCheckNullBaseUri() {
    DataHolidaySourceUris.uriSearchCheck(null, LocalDate.now(), HolidayType.BANK, Currency.AUD, EIDS);
  }

  /**
   * Tests that the date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriSearchCheckNullDate() {
    DataHolidaySourceUris.uriSearchCheck(_baseUri, null, HolidayType.BANK, Currency.AUD, EIDS);
  }

  /**
   * Tests that the holiday type cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriSearchCheckNullHolidayType() {
    DataHolidaySourceUris.uriSearchCheck(_baseUri, LocalDate.now(), null, Currency.AUD, EIDS);
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriSearchCheckNullCurrencyEids() {
    final LocalDate date = LocalDate.of(2020, 1, 1);
    final URI uri = DataHolidaySourceUris.uriSearchCheck(_baseUri, date, HolidayType.BANK, null, null);
    assertEquals(uri.getPath(), "path/to/holidaySearches/check");
    assertEquals(uri.getQuery(), "date=2020-01-01&holidayType=BANK");
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriSearchCheckNullEids() {
    final LocalDate date = LocalDate.of(2020, 1, 1);
    final URI uri = DataHolidaySourceUris.uriSearchCheck(_baseUri, date, HolidayType.BANK, Currency.AUD, null);
    assertEquals(uri.getPath(), "path/to/holidaySearches/check");
    assertEquals(uri.getQuery(), "date=2020-01-01&holidayType=BANK&currency=AUD");
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriSearchCheck() {
    final LocalDate date = LocalDate.of(2020, 1, 1);
    final URI uri = DataHolidaySourceUris.uriSearchCheck(_baseUri, date, HolidayType.BANK, Currency.AUD, EIDS);
    assertEquals(uri.getPath(), "path/to/holidaySearches/check");
    assertEquals(uri.getQuery(), "date=2020-01-01&holidayType=BANK&currency=AUD&id=ISO_COUNTRY_ALPHA2~AU");
  }
}
