/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.region.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;

import java.util.Collections;

import org.testng.annotations.Test;
import org.threeten.bp.ZoneId;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.region.RegionClassification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link SimpleRegion}.
 */
@Test(groups = TestGroup.UNIT)
public class SimpleRegionTest extends AbstractFudgeBuilderTestCase {

  /**
   * Tests that the ids cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullId() {
    new SimpleRegion().setExternalIdBundle(null);
  }

  /**
   * Tests that the name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName() {
    new SimpleRegion().setName(null);
  }

  /**
   * Tests that the full name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFullName() {
    new SimpleRegion().setFullName(null);
  }

  /**
   * Tests setting a null country.
   */
  @Test
  public void testSetNullCountry() {
    final SimpleRegion region = new SimpleRegion();
    assertNull(region.getCountry());
    region.addExternalId(ExternalId.of(ExternalSchemes.ISO_COUNTRY_ALPHA2, "US"));
    assertEquals(region.getCountry(), Country.US);
    region.setCountry(null);
    assertNull(region.getCountry());
  }

  /**
   * Tests setting a country.
   */
  @Test
  public void testAddCountry() {
    final SimpleRegion region = new SimpleRegion();
    assertNull(region.getCountry());
    region.setCountry(Country.US);
    assertEquals(region.getCountry(), Country.US);
  }

  /**
   * Tests replacing a country.
   */
  @Test
  public void testReplaceCountry() {
    final SimpleRegion region = new SimpleRegion();
    assertNull(region.getCountry());
    region.addExternalId(ExternalId.of(ExternalSchemes.ISO_COUNTRY_ALPHA2, "US"));
    region.setCountry(Country.AR);
    assertEquals(region.getCountry(), Country.AR);
  }

  /**
   * Tests setting a null currency.
   */
  @Test
  public void testSetNullCurrency() {
    final SimpleRegion region = new SimpleRegion();
    assertNull(region.getCountry());
    region.addExternalId(ExternalId.of(ExternalSchemes.ISO_CURRENCY_ALPHA3, "USD"));
    assertEquals(region.getCurrency(), Currency.USD);
    region.setCurrency(null);
    assertNull(region.getCurrency());
  }

  /**
   * Tests setting a currency.
   */
  @Test
  public void testAddCurrency() {
    final SimpleRegion region = new SimpleRegion();
    assertNull(region.getCurrency());
    region.setCurrency(Currency.USD);
    assertEquals(region.getCurrency(), Currency.USD);
  }

  /**
   * Tests replacing a currency.
   */
  @Test
  public void testReplaceCurrency() {
    final SimpleRegion region = new SimpleRegion();
    assertNull(region.getCurrency());
    region.addExternalId(ExternalId.of(ExternalSchemes.ISO_CURRENCY_ALPHA3, "USD"));
    region.setCurrency(Currency.AUD);
    assertEquals(region.getCurrency(), Currency.AUD);
  }

  /**
   * Tests setting a null time zone.
   */
  @Test
  public void testSetNullTimeZone() {
    final SimpleRegion region = new SimpleRegion();
    assertNull(region.getTimeZone());
    region.addExternalId(ExternalId.of(ExternalSchemes.TZDB_TIME_ZONE, "UTC"));
    assertEquals(region.getTimeZone(), ZoneId.of("UTC"));
    region.setTimeZone(null);
    assertNull(region.getTimeZone());
  }

  /**
   * Tests setting a time zone.
   */
  @Test
  public void testAddTimeZone() {
    final SimpleRegion region = new SimpleRegion();
    assertNull(region.getTimeZone());
    region.setTimeZone(ZoneId.of("Europe/London"));
    assertEquals(region.getTimeZone(), ZoneId.of("Europe/London"));
  }

  /**
   * Tests replacing a time zone.
   */
  @Test
  public void testReplaceTimeZone() {
    final SimpleRegion region = new SimpleRegion();
    assertNull(region.getTimeZone());
    region.addExternalId(ExternalId.of(ExternalSchemes.TZDB_TIME_ZONE, "UTC"));
    region.setTimeZone(ZoneId.of("Europe/London"));
    assertEquals(region.getTimeZone(), ZoneId.of("Europe/London"));
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final SimpleRegion region = new SimpleRegion();
    region.setName("name");
    region.setFullName("full name");
    region.setClassification(RegionClassification.DISPUTED_TERRITORY);
    region.setCountry(Country.GB);
    region.setCurrency(Currency.GBP);
    region.setParentRegionIds(Collections.singleton(UniqueId.of("uid", "1")));
    region.setTimeZone(ZoneId.of("Europe/London"));
    region.setUniqueId(UniqueId.of("uid", "2"));
    assertEquals(region, region);
    assertNotEquals(null, region);
    assertNotEquals(RegionClassification.DISPUTED_TERRITORY, region);
    final SimpleRegion other = new SimpleRegion();
    other.setName("name");
    other.setFullName("full name");
    other.setClassification(RegionClassification.DISPUTED_TERRITORY);
    other.setCountry(Country.GB);
    other.setCurrency(Currency.GBP);
    other.setParentRegionIds(Collections.singleton(UniqueId.of("uid", "1")));
    other.setTimeZone(ZoneId.of("Europe/London"));
    other.setUniqueId(UniqueId.of("uid", "2"));
    assertEquals(region, other);
    assertEquals(region.hashCode(), other.hashCode());
    assertEquals(region.toString(), "SimpleRegion{uniqueId=uid~2, externalIdBundle=Bundle[ISO_COUNTRY_ALPHA2~GB, ISO_CURRENCY_ALPHA3~GBP, "
        + "TZDB_TIME_ZONE~Europe/London], classification=DISPUTED_TERRITORY, parentRegionIds=[uid~1], name=name, fullName=full name, data=FlexiBean{}}");
    other.setName("other");
    assertNotEquals(region, other);
    other.setName("name");
    other.setFullName("other full name");
    assertNotEquals(region, other);
    other.setFullName("full name");
    other.setClassification(RegionClassification.ANTARCTIC_TERRITORY);
    assertNotEquals(region, other);
    other.setClassification(RegionClassification.DISPUTED_TERRITORY);
    other.setCountry(Country.US);
    assertNotEquals(region, other);
    other.setCountry(Country.GB);
    other.setCurrency(Currency.USD);
    assertNotEquals(region, other);
    other.setCurrency(Currency.GBP);
    other.setParentRegionIds(Collections.singleton(UniqueId.of("uid", "3")));
    assertNotEquals(region, other);
    other.setParentRegionIds(Collections.singleton(UniqueId.of("uid", "1")));
    other.setTimeZone(ZoneId.of("Europe/Paris"));
    assertNotEquals(region, other);
    other.setTimeZone(ZoneId.of("Europe/London"));
    other.setUniqueId(UniqueId.of("uid", "10"));
    assertNotEquals(region, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final SimpleRegion region = new SimpleRegion();
    region.setName("name");
    region.setFullName("full name");
    region.setClassification(RegionClassification.DISPUTED_TERRITORY);
    region.setCountry(Country.GB);
    region.setCurrency(Currency.GBP);
    region.setParentRegionIds(Collections.singleton(UniqueId.of("uid", "1")));
    region.setTimeZone(ZoneId.of("Europe/London"));
    region.setUniqueId(UniqueId.of("uid", "2"));
    final ExternalIdBundle ids = ExternalIdBundle.of(
        ExternalId.of(ExternalSchemes.ISO_COUNTRY_ALPHA2, Country.GB.getCode()),
        ExternalId.of(ExternalSchemes.ISO_CURRENCY_ALPHA3, Currency.GBP.getCode()),
        ExternalId.of(ExternalSchemes.TZDB_TIME_ZONE, "Europe/London"));

    assertEquals(region.metaBean().classification().get(region), RegionClassification.DISPUTED_TERRITORY);
    assertEquals(region.metaBean().externalIdBundle().get(region), ids);
    assertEquals(region.metaBean().name().get(region), "name");
    assertEquals(region.metaBean().fullName().get(region), "full name");
    assertEquals(region.metaBean().uniqueId().get(region), UniqueId.of("uid", "2"));

    assertEquals(region.property("classification").get(), RegionClassification.DISPUTED_TERRITORY);
    assertEquals(region.property("externalIdBundle").get(), ids);
    assertEquals(region.property("name").get(), "name");
    assertEquals(region.property("fullName").get(), "full name");
    assertEquals(region.property("uniqueId").get(), UniqueId.of("uid", "2"));
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final SimpleRegion region = new SimpleRegion();
    region.setName("name");
    region.setFullName("full name");
    region.setClassification(RegionClassification.DISPUTED_TERRITORY);
    region.setCountry(Country.GB);
    region.setCurrency(Currency.GBP);
    region.setParentRegionIds(Collections.singleton(UniqueId.of("uid", "1")));
    region.setTimeZone(ZoneId.of("Europe/London"));
    region.setUniqueId(UniqueId.of("uid", "2"));
    assertEquals(cycleObjectJodaXml(SimpleRegion.class, region), region);
  }
}
