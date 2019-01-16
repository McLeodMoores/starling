/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.exchange;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;

import org.joda.beans.JodaBeanUtils;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.exchange.ExchangeDocument.Meta;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ExchangeDocument}.
 */
@Test(groups = TestGroup.UNIT)
public class ExchangeDocumentTest extends AbstractFudgeBuilderTestCase {
  private static final ExternalIdBundle EIDS = ExternalIdBundle.of("ex", "LSE");
  private static final String NAME = "London Stock Exchange";
  private static final ExternalIdBundle REGIONS = ExternalSchemes.countryRegionId(Country.GB).toBundle();
  private static final ZoneId TIME_ZONE = ZoneId.of("Europe/London");
  private static final ManageableExchange EXCHANGE = new ManageableExchange(EIDS, NAME, REGIONS, TIME_ZONE);
  private static final UniqueId UID = UniqueId.of("exch", "1");
  private static final Instant VERSION_FROM = Instant.ofEpochSecond(100);
  private static final Instant VERSION_TO = Instant.ofEpochSecond(200);
  private static final Instant CORRECTION_FROM = Instant.ofEpochSecond(150);
  private static final Instant CORRECTION_TO = Instant.ofEpochSecond(250);
  static {
    EXCHANGE.setUniqueId(UID);
  }

  /**
   * Tests that the exchange cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExchange() {
    new ExchangeDocument(null);
  }

  /**
   * Tests constructor equivalence.
   */
  @Test
  public void testConstructors() {
    final ExchangeDocument doc = new ExchangeDocument(EXCHANGE);
    doc.setVersionFromInstant(VERSION_FROM);
    doc.setVersionToInstant(VERSION_TO);
    doc.setCorrectionFromInstant(CORRECTION_FROM);
    doc.setCorrectionToInstant(CORRECTION_TO);
    final ExchangeDocument other = new ExchangeDocument();
    other.setExchange(EXCHANGE);
    assertNotEquals(doc.getUniqueId(), other.getUniqueId());
    JodaBeanUtils.equalIgnoring(doc, other, ExchangeDocument.meta().metaProperty("uniqueId"));
  }

  /**
   * Tests the unique id getter.
   */
  @Test
  public void testGetUniqueId() {
    final ExchangeDocument doc = new ExchangeDocument(EXCHANGE);
    // gets unique id from exchange
    assertEquals(doc.getUniqueId(), UID);
    doc.setExchange(null);
    // doesn't update
    assertEquals(doc.getUniqueId(), UID);
    final ManageableExchange exchange = EXCHANGE.clone();
    exchange.setName(NAME);
    exchange.setUniqueId(UID);
    doc.setExchange(exchange);
    assertEquals(doc.getUniqueId(), UID);
    // again, doesn't update
    doc.setUniqueId(null);
    exchange.setUniqueId(UID);
    assertNull(doc.getUniqueId());
  }

  /**
   * Tests the unique id setter.
   */
  @Test
  public void testSetUniqueId() {
    final ManageableExchange exchange = EXCHANGE.clone();
    exchange.setUniqueId(UID);
    exchange.setName(NAME);
    final ExchangeDocument doc = new ExchangeDocument(exchange);
    final UniqueId uid1 = UniqueId.of("uid", "2");
    final UniqueId uid2 = UniqueId.of("uid", "3");
    assertEquals(doc.getUniqueId(), UID);
    assertEquals(exchange.getUniqueId(), UID);
    assertEquals(doc.getObjectId(), exchange.getUniqueId().getObjectId());
    doc.setUniqueId(uid1);
    assertEquals(doc.getUniqueId(), uid1);
    assertEquals(exchange.getUniqueId(), UID);
    assertEquals(doc.getObjectId(), uid1.getUniqueId().getObjectId());
    doc.setExchange(null);
    doc.setUniqueId(uid2);
    assertEquals(doc.getUniqueId(), uid2);
    assertEquals(exchange.getUniqueId(), UID);
  }

  /**
   * Tests the name getter.
   */
  @Test
  public void testGetName() {
    final ManageableExchange exchange = EXCHANGE.clone();
    final ExchangeDocument doc = new ExchangeDocument(exchange);
    assertEquals(doc.getName(), NAME);
    doc.setExchange(null);
    assertNull(doc.getName());
  }

  /**
   * Tests the exchange setter.
   */
  @Test
  public void testExchangeSetter() {
    final ManageableExchange exchange = EXCHANGE.clone();
    final ExchangeDocument doc = new ExchangeDocument(exchange);
    doc.setExchange(null); // fine
    assertNull(doc.getExchange());
    doc.setExchange(exchange);
    assertEquals(doc.getExchange(), EXCHANGE);
    assertEquals(doc.getName(), NAME);
    assertEquals(doc.getUniqueId(), UID);
    doc.setUniqueId(null);
    assertEquals(doc.getExchange(), EXCHANGE);
    assertEquals(doc.getName(), NAME);
    assertNull(doc.getUniqueId());
  }

  /**
   * Test value getter.
   */
  @Test
  public void testValueGetter() {
    final ExchangeDocument doc = new ExchangeDocument(EXCHANGE);
    assertEquals(doc.getValue(), EXCHANGE);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final ExchangeDocument doc = new ExchangeDocument(EXCHANGE);
    final ExchangeDocument other = new ExchangeDocument(EXCHANGE.clone());
    assertEquals(doc, doc);
    assertEquals(doc.toString(),
        "ExchangeDocument{versionFromInstant=null, versionToInstant=null, correctionFromInstant=null, correctionToInstant=null, "
            + "exchange=ManageableExchange{uniqueId=exch~1, externalIdBundle=Bundle[ex~LSE], name=London Stock Exchange, "
            + "regionIdBundle=Bundle[ISO_COUNTRY_ALPHA2~GB], timeZone=Europe/London, detail=[]}, uniqueId=exch~1}");
    assertEquals(doc, other);
    assertEquals(doc.hashCode(), other.hashCode());
    final ManageableExchange exchange = new ManageableExchange();
    other.setExchange(exchange);
    assertNotEquals(doc, other);
    other.setCorrectionFromInstant(CORRECTION_FROM);
    assertNotEquals(doc, other);
    other.setCorrectionFromInstant(CORRECTION_FROM);
    other.setCorrectionToInstant(CORRECTION_TO);
    assertNotEquals(doc, other);
    other.setCorrectionToInstant(CORRECTION_TO);
    other.setVersionFromInstant(VERSION_FROM);
    assertNotEquals(doc, other);
    other.setVersionFromInstant(VERSION_FROM);
    other.setVersionToInstant(VERSION_TO);
    assertNotEquals(doc, other);
    other.setVersionToInstant(VERSION_TO);
    other.setUniqueId(UniqueId.of("uid", "2"));
    assertNotEquals(doc, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final ManageableExchange exchange = EXCHANGE.clone();
    exchange.setUniqueId(UID);
    final ExchangeDocument doc = new ExchangeDocument(exchange);
    doc.setCorrectionFromInstant(CORRECTION_FROM);
    doc.setCorrectionToInstant(CORRECTION_TO);
    doc.setVersionFromInstant(VERSION_FROM);
    doc.setVersionToInstant(VERSION_TO);
    assertEquals(doc.propertyNames().size(), 6);
    final Meta bean = doc.metaBean();
    assertEquals(bean.correctionFromInstant().get(doc), CORRECTION_FROM);
    assertEquals(bean.correctionToInstant().get(doc), CORRECTION_TO);
    assertEquals(bean.exchange().get(doc), EXCHANGE);
    assertEquals(bean.uniqueId().get(doc), UID);
    assertEquals(bean.versionFromInstant().get(doc), VERSION_FROM);
    assertEquals(bean.versionToInstant().get(doc), VERSION_TO);
    assertEquals(doc.property("correctionFromInstant").get(), CORRECTION_FROM);
    assertEquals(doc.property("correctionToInstant").get(), CORRECTION_TO);
    assertEquals(doc.property("exchange").get(), EXCHANGE);
    assertEquals(doc.property("uniqueId").get(), UID);
    assertEquals(doc.property("versionFromInstant").get(), VERSION_FROM);
    assertEquals(doc.property("versionToInstant").get(), VERSION_TO);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final ExchangeDocument doc = new ExchangeDocument(EXCHANGE);
    assertEncodeDecodeCycle(ExchangeDocument.class, doc);
  }

}
