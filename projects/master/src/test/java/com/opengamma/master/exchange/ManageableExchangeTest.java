/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.exchange;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.UniqueId;
import com.opengamma.master.exchange.ManageableExchange.Meta;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ManageableExchange}.
 */
@Test(groups = TestGroup.UNIT)
public class ManageableExchangeTest extends AbstractFudgeBuilderTestCase {
  private static final UniqueId UID = UniqueId.of("exch", "1");
  private static final ExternalIdBundle EIDS = ExternalIdBundle.of(ExternalId.of("eid", "lse"), ExternalId.of(ExternalSchemes.ISO_MIC, "FTSE"));
  private static final String NAME = "London Stock Exchange";
  private static final ExternalIdBundle REGION = ExternalSchemes.countryRegionId(Country.GB).toBundle();
  private static final ZoneId ZONE = ZoneId.of("Europe/London");
  private static final ManageableExchangeDetail DETAIL = new ManageableExchangeDetail();
  static {
    DETAIL.setDayStart("08:00");
    DETAIL.setDayEnd("16:00");
  }
  private static final List<ManageableExchangeDetail> DETAILS = Arrays.asList(DETAIL);

  /**
   * Tests that the identifiers cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIdentifiersConstructor() {
    new ManageableExchange(null, NAME, REGION, ZONE);
  }

  /**
   * Tests that the name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNameConstructor() {
    new ManageableExchange(EIDS, null, REGION, ZONE);
  }

  /**
   * Tests that the identifiers cannot be set to null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIdentifiersSetter() {
    new ManageableExchange().setExternalIdBundle(null);
  }

  /**
   * Tests that the name cannot be set to null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNameSetter() {
    new ManageableExchange().setName(null);
  }

  /**
   * Tests the clone.
   */
  public void testClone() {
    final ManageableExchange exchange = new ManageableExchange(EIDS, NAME, REGION, ZONE);
    exchange.setDetail(DETAILS);
    exchange.setUniqueId(UID);
    final ManageableExchange clone = exchange.clone();
    assertEquals(exchange.getDetail(), clone.getDetail());
    assertNotSame(exchange.getDetail(), clone.getDetail());
    assertEquals(exchange.getExternalIdBundle(), clone.getExternalIdBundle());
    assertEquals(exchange.getISOMic(), clone.getISOMic());
    assertEquals(exchange.getName(), clone.getName());
    assertEquals(exchange.getRegionIdBundle(), clone.getRegionIdBundle());
    assertEquals(exchange.getTimeZone(), clone.getTimeZone());
    assertEquals(exchange.getUniqueId(), clone.getUniqueId());
  }

  /**
   * Tests getting the ISO MIC.
   */
  public void testGetIsoMic() {
    final ManageableExchange exchange = new ManageableExchange();
    assertSame(exchange.getExternalIdBundle(), ExternalIdBundle.EMPTY);
    assertNull(exchange.getISOMic());
    exchange.setExternalIdBundle(EIDS);
    assertEquals(exchange.getISOMic(), "FTSE");
    exchange.setExternalIdBundle(EIDS.getExternalId(ExternalScheme.of("eid")).toBundle());
    assertNull(exchange.getISOMic());
  }

  /**
   * Tests setting the ISO MIC.
   */
  public void testSetIsoMic() {
    final ManageableExchange exchange = new ManageableExchange();
    assertSame(exchange.getExternalIdBundle(), ExternalIdBundle.EMPTY);
    assertNull(exchange.getISOMic());
    exchange.setISOMic("NYSE");
    assertEquals(exchange.getISOMic(), "NYSE");
    assertEquals(exchange.getExternalIdBundle(), ExternalIdBundle.of(ExternalSchemes.ISO_MIC, "NYSE"));
    exchange.setISOMic(EIDS.getValue(ExternalSchemes.ISO_MIC));
    assertEquals(exchange.getISOMic(), "FTSE");
    assertEquals(exchange.getExternalIdBundle(), ExternalIdBundle.of(ExternalSchemes.ISO_MIC, "FTSE"));
  }

  /**
   * Tests the object.
   */
  public void testObject() {
    final ManageableExchange exchange = new ManageableExchange(EIDS, NAME, REGION, ZONE);
    exchange.setDetail(DETAILS);
    exchange.setUniqueId(UID);
    final ManageableExchange other = new ManageableExchange(EIDS, NAME, REGION, ZONE);
    other.setDetail(DETAILS);
    other.setUniqueId(UID);
    assertEquals(exchange, exchange);
    assertEquals(exchange.toString(),
        "ManageableExchange{uniqueId=exch~1, externalIdBundle=Bundle[ISO_MIC~FTSE, eid~lse], name=London Stock Exchange, "
            + "regionIdBundle=Bundle[ISO_COUNTRY_ALPHA2~GB], timeZone=Europe/London, detail=[ManageableExchangeDetail{productGroup=null, "
            + "productName=null, productType=null, productCode=null, calendarStart=null, calendarEnd=null, dayStart=08:00, dayRangeType=null, "
            + "dayEnd=16:00, phaseName=null, phaseType=null, phaseStart=null, phaseEnd=null, randomStartMin=null, randomStartMax=null, "
            + "randomEndMin=null, randomEndMax=null, lastConfirmed=null, notes=null}]}");
    assertEquals(exchange, other);
    assertEquals(exchange.hashCode(), other.hashCode());
    other.setDetail(Arrays.asList(new ManageableExchangeDetail()));
    assertNotEquals(exchange, other);
    other.setDetail(DETAILS);
    other.setExternalIdBundle(EIDS.getExternalId(ExternalSchemes.ISO_MIC).toBundle());
    assertNotEquals(exchange, other);
    other.setExternalIdBundle(EIDS);
    other.setName("other");
    assertNotEquals(exchange, other);
    other.setName(NAME);
    other.setRegionIdBundle(ExternalSchemes.countryRegionId(Country.US).toBundle());
    assertNotEquals(exchange, other);
    other.setRegionIdBundle(REGION);
    other.setTimeZone(ZoneOffset.UTC);
    assertNotEquals(exchange, other);
    other.setTimeZone(ZONE);
    other.setUniqueId(UniqueId.of("uid", "1"));
    assertNotEquals(exchange, other);
  }

  /**
   * Tests the bean.
   */
  public void testBean() {
    final ManageableExchange exchange = new ManageableExchange(EIDS, NAME, REGION, ZONE);
    exchange.setDetail(DETAILS);
    exchange.setUniqueId(UID);
    assertEquals(exchange.propertyNames().size(), 6);
    final Meta bean = exchange.metaBean();
    assertEquals(bean.detail().get(exchange), DETAILS);
    assertEquals(bean.externalIdBundle().get(exchange), EIDS);
    assertEquals(bean.name().get(exchange), NAME);
    assertEquals(bean.regionIdBundle().get(exchange), REGION);
    assertEquals(bean.timeZone().get(exchange), ZONE);
    assertEquals(bean.uniqueId().get(exchange), UID);
    assertEquals(exchange.property("detail").get(), DETAILS);
    assertEquals(exchange.property("externalIdBundle").get(), EIDS);
    assertEquals(exchange.property("name").get(), NAME);
    assertEquals(exchange.property("regionIdBundle").get(), REGION);
    assertEquals(exchange.property("timeZone").get(), ZONE);
    assertEquals(exchange.property("uniqueId").get(), UID);
  }

  /**
   * Tests a cycle.
   */
  public void testCycle() {
    final ManageableExchange exchange = new ManageableExchange(EIDS, NAME, REGION, ZONE);
    exchange.setDetail(DETAILS);
    exchange.setUniqueId(UID);
    assertEncodeDecodeCycle(ManageableExchange.class, exchange);
  }
}
