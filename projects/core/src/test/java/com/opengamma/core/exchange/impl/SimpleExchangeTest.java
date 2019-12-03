/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.exchange.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZoneId;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link SimpleExchange}.
 */
@Test(groups = TestGroup.UNIT)
public class SimpleExchangeTest extends AbstractFudgeBuilderTestCase {
  private static final UniqueId UID = UniqueId.of("uid", "10");
  private static final ExternalIdBundle EID = ExternalIdBundle.of("eid", "1000");
  private static final ExternalIdBundle REGION_ID = ExternalSchemes.financialRegionId("GB").toBundle();
  private static final ZoneId ZONE_ID = ZoneId.of("Europe/London");
  private static final String NAME = "FTSE";

  /**
   * Tests that the name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName() {
    new SimpleExchange().setName(null);
  }

  /**
   * Tests adding an external id.
   */
  @Test
  public void testAddExternalId() {
    final SimpleExchange exchange = new SimpleExchange();
    exchange.setExternalIdBundle(EID);
    final SimpleExchange other = new SimpleExchange();
    other.addExternalId(ExternalId.of("eid", "1000"));
    assertEquals(exchange, other);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final SimpleExchange exchange = new SimpleExchange();
    exchange.setExternalIdBundle(EID);
    exchange.setName(NAME);
    exchange.setRegionIdBundle(REGION_ID);
    exchange.setTimeZone(ZONE_ID);
    exchange.setUniqueId(UID);
    assertEquals(exchange, exchange);
    assertNotEquals(null, exchange);
    assertNotEquals(EID, exchange);
    assertEquals(exchange.toString(), "SimpleExchange{uniqueId=uid~10, externalIdBundle=Bundle[eid~1000], "
        + "regionIdBundle=Bundle[FINANCIAL_REGION~GB], timeZone=Europe/London, name=FTSE}");
    final SimpleExchange other = new SimpleExchange();
    other.setExternalIdBundle(EID);
    other.setName(NAME);
    other.setRegionIdBundle(REGION_ID);
    other.setTimeZone(ZONE_ID);
    other.setUniqueId(UID);
    assertEquals(exchange, other);
    assertEquals(exchange.hashCode(), other.hashCode());
    other.setExternalIdBundle(ExternalIdBundle.of("eid", "2000"));
    assertNotEquals(exchange, other);
    other.setExternalIdBundle(EID);
    other.setName("CMX");
    assertNotEquals(exchange, other);
    other.setName(NAME);
    other.setRegionIdBundle(ExternalSchemes.financialRegionId("FR").toBundle());
    assertNotEquals(exchange, other);
    other.setRegionIdBundle(REGION_ID);
    other.setTimeZone(ZoneId.of("Europe/Paris"));
    assertNotEquals(exchange, other);
    other.setTimeZone(ZONE_ID);
    other.setUniqueId(UniqueId.of("uid", "20"));
    assertNotEquals(exchange, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final SimpleExchange exchange = new SimpleExchange();
    exchange.setExternalIdBundle(EID);
    exchange.setName(NAME);
    exchange.setRegionIdBundle(REGION_ID);
    exchange.setTimeZone(ZONE_ID);
    exchange.setUniqueId(UID);
    assertEquals(exchange.metaBean().externalIdBundle().get(exchange), EID);
    assertEquals(exchange.metaBean().name().get(exchange), NAME);
    assertEquals(exchange.metaBean().regionIdBundle().get(exchange), REGION_ID);
    assertEquals(exchange.metaBean().timeZone().get(exchange), ZONE_ID);
    assertEquals(exchange.metaBean().uniqueId().get(exchange), UID);
    assertEquals(exchange.property("externalIdBundle").get(), EID);
    assertEquals(exchange.property("name").get(), NAME);
    assertEquals(exchange.property("regionIdBundle").get(), REGION_ID);
    assertEquals(exchange.property("timeZone").get(), ZONE_ID);
    assertEquals(exchange.property("uniqueId").get(), UID);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final SimpleExchange exchange = new SimpleExchange();
    exchange.setExternalIdBundle(EID);
    exchange.setName(NAME);
    exchange.setRegionIdBundle(REGION_ID);
    exchange.setTimeZone(ZONE_ID);
    exchange.setUniqueId(UID);
    assertEncodeDecodeCycle(SimpleExchange.class, exchange);
  }
}
