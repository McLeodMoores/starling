/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.position;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneOffset;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.position.ManageablePosition.Meta;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ManageablePosition}.
 */
@Test(groups = TestGroup.UNIT)
public class ManageablePositionTest extends AbstractFudgeBuilderTestCase {
  private static final UniqueId P_UID = UniqueId.of("uid", "1");
  private static final ExternalId S_EID = ExternalId.of("eid", "11");
  private static final BigDecimal QUANTITY = BigDecimal.valueOf(3857);
  private static final ManageableSecurityLink SECURITY_LINK = new ManageableSecurityLink(S_EID);
  private static final List<ManageableTrade> TRADES = new ArrayList<>();
  private static final Map<String, String> ATTRIBUTES = new HashMap<>();
  private static final ExternalId PROVIDER_ID = ExternalId.of("eid", "1000");
  static {
    final LocalDate tradeDate = LocalDate.of(2019, 1, 1);
    final OffsetTime tradeTime = OffsetTime.of(LocalTime.of(11, 0), ZoneOffset.UTC);
    final ExternalId counterpartyId = ExternalId.of("eid", "0395");
    for (int i = 0; i < 5; i++) {
      TRADES.add(new ManageableTrade(BigDecimal.valueOf(30457).add(BigDecimal.valueOf(i)), ExternalId.of("eid", Integer.toString(i)), tradeDate, tradeTime,
          counterpartyId));
      ATTRIBUTES.put("A", Integer.toString(i));
    }
  }

  /**
   * Tests that the quantity cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullQuantityConstructor1() {
    new ManageablePosition(null, S_EID);
  }

  /**
   * Tests that the quantity cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullQuantityConstructor2() {
    new ManageablePosition(null, S_EID.toBundle());
  }

  /**
   * Tests that the quantity cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullQuantitySetter() {
    new ManageablePosition().setQuantity(null);
  }

  /**
   * Tests that the security id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurityIdConstructor() {
    new ManageablePosition(QUANTITY, (ExternalId) null);
  }

  /**
   * Tests that the security id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurityIdBundleConstructor() {
    new ManageablePosition(QUANTITY, (ExternalIdBundle) null);
  }

  /**
   * Tests that the security link cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurityLinkSetter() {
    new ManageablePosition().setSecurityLink(null);
  }

  /**
   * Tests that the attributes cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAttributesSetter() {
    new ManageablePosition().setAttributes(null);
  }

  /**
   * Tests that the position cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPosition() {
    new ManageablePosition(null);
  }

  /**
   * Tests that the trade to add cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddNullTrade() {
    new ManageablePosition().addTrade(null);
  }

  /**
   * Tests the default constructor.
   */
  @Test
  public void testDefaultConstructor() {
    final ManageablePosition position = new ManageablePosition();
    assertTrue(position.getAttributes().isEmpty());
    assertEquals(position.getName(), "");
    assertNull(position.getProviderId());
    assertNull(position.getQuantity());
    assertNull(position.getSecurity());
    assertEquals(position.getSecurityLink(), new ManageableSecurityLink());
    assertTrue(position.getTrades().isEmpty());
    assertNull(position.getUniqueId());
  }

  /**
   * Tests construction from details.
   */
  @Test
  public void testConstructionFromDetails1() {
    final ManageablePosition position = new ManageablePosition(QUANTITY, S_EID);
    assertTrue(position.getAttributes().isEmpty());
    assertEquals(position.getName(), QUANTITY.toString() + " x " + S_EID.getValue());
    assertNull(position.getProviderId());
    assertEquals(position.getQuantity(), QUANTITY);
    assertNull(position.getSecurity());
    assertEquals(position.getSecurityLink(), SECURITY_LINK);
    assertTrue(position.getTrades().isEmpty());
    assertNull(position.getUniqueId());
  }

  /**
   * Tests construction from details.
   */
  @Test
  public void testConstructionFromDetails2() {
    final ManageablePosition position = new ManageablePosition(QUANTITY, S_EID.toBundle());
    assertTrue(position.getAttributes().isEmpty());
    assertEquals(position.getName(), QUANTITY.toString() + " x " + S_EID.getValue());
    assertNull(position.getProviderId());
    assertEquals(position.getQuantity(), QUANTITY);
    assertNull(position.getSecurity());
    assertEquals(position.getSecurityLink(), SECURITY_LINK);
    assertTrue(position.getTrades().isEmpty());
    assertNull(position.getUniqueId());
  }

  /**
   * Tests construction from details.
   */
  @Test
  public void testConstructionFromDetails3() {
    final ManageablePosition position = new ManageablePosition(P_UID, QUANTITY, S_EID.toBundle());
    assertTrue(position.getAttributes().isEmpty());
    assertEquals(position.getName(), QUANTITY.toString() + " x " + S_EID.getValue());
    assertNull(position.getProviderId());
    assertEquals(position.getQuantity(), QUANTITY);
    assertNull(position.getSecurity());
    assertEquals(position.getSecurityLink(), SECURITY_LINK);
    assertTrue(position.getTrades().isEmpty());
    assertEquals(position.getUniqueId(), P_UID);
  }

  /**
   * Tests the copy constructor.
   */
  @Test
  public void testCopyConstructor() {
    final ManageablePosition other = new ManageablePosition();
    setFields(other);
    final ManageablePosition position = new ManageablePosition(other);
    assertEquals(position.getAttributes(), other.getAttributes());
    assertEquals(position.getName(), other.getName());
    assertEquals(position.getProviderId(), other.getProviderId());
    assertEquals(position.getQuantity(), other.getQuantity());
    assertEquals(position.getSecurity(), other.getSecurity());
    assertEquals(position.getSecurityLink(), other.getSecurityLink());
    assertEquals(position.getTrades(), other.getTrades());
    assertEquals(position.getUniqueId(), other.getUniqueId());
  }

  /**
   * Tests the addition of a trade.
   */
  @Test
  public void testAddTrade() {
    final ManageablePosition position = new ManageablePosition();
    final List<ManageableTrade> addedSoFar = new ArrayList<>();
    for (final ManageableTrade trade : TRADES) {
      addedSoFar.add(trade);
      // appends trades
      position.addTrade(trade);
      assertEquals(position.getTrades(), addedSoFar);
    }
  }

  /**
   * Tests setting trades.
   */
  @Test
  public void testSetTrades() {
    final ManageablePosition position = new ManageablePosition();
    position.setTrades(TRADES);
    assertEquals(position.getTrades(), TRADES);
    // overwrites existing trades
    position.setTrades(Collections.singletonList(TRADES.get(0)));
    assertEquals(position.getTrades(), Collections.singletonList(TRADES.get(0)));
  }

  /**
   * Test the removal of a trade.
   */
  @Test
  public void testRemoveTrade() {
    final ManageablePosition position = new ManageablePosition();
    position.setTrades(TRADES);
    assertEquals(position.getTrades(), TRADES);
    // removing null has no effect
    assertFalse(position.removeTrade(null));
    assertEquals(position.getTrades(), TRADES);
    final ManageableTrade trade = new ManageableTrade(BigDecimal.valueOf(3045), ExternalId.of("eid", "erg"), LocalDate.now(), OffsetTime.now(),
        ExternalId.of("eid", "iejrgoirej"));
    assertFalse(position.removeTrade(trade));
    assertEquals(position.getTrades(), TRADES);
    assertTrue(position.removeTrade(TRADES.get(0)));
    assertEquals(position.getTrades(), TRADES.subList(1, TRADES.size()));
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final ManageablePosition position = new ManageablePosition();
    setFields(position);
    final ManageablePosition other = new ManageablePosition();
    setFields(other);
    assertEquals(position, position);
    assertEquals(position.toString(),
        "ManageablePosition{uniqueId=uid~1, quantity=3857, securityLink=ManageableSecurityLink{objectId=null, "
            + "externalId=Bundle[eid~11], target=null}, trades=[ManageableTrade{uniqueId=null, parentPositionId=null, quantity=30457, "
            + "securityLink=ManageableSecurityLink{objectId=null, externalId=Bundle[eid~0], target=null}, counterpartyExternalId=eid~0395, "
            + "tradeDate=2019-01-01, tradeTime=11:00Z, premium=null, premiumCurrency=null, premiumDate=null, premiumTime=null, attributes={}, "
            + "deal=null, providerId=null}, ManageableTrade{uniqueId=null, parentPositionId=null, quantity=30458, "
            + "securityLink=ManageableSecurityLink{objectId=null, externalId=Bundle[eid~1], target=null}, counterpartyExternalId=eid~0395, "
            + "tradeDate=2019-01-01, tradeTime=11:00Z, premium=null, premiumCurrency=null, premiumDate=null, premiumTime=null, attributes={}, "
            + "deal=null, providerId=null}, ManageableTrade{uniqueId=null, parentPositionId=null, quantity=30459, "
            + "securityLink=ManageableSecurityLink{objectId=null, externalId=Bundle[eid~2], target=null}, counterpartyExternalId=eid~0395, "
            + "tradeDate=2019-01-01, tradeTime=11:00Z, premium=null, premiumCurrency=null, premiumDate=null, premiumTime=null, attributes={}, "
            + "deal=null, providerId=null}, ManageableTrade{uniqueId=null, parentPositionId=null, quantity=30460, "
            + "securityLink=ManageableSecurityLink{objectId=null, externalId=Bundle[eid~3], target=null}, counterpartyExternalId=eid~0395, "
            + "tradeDate=2019-01-01, tradeTime=11:00Z, premium=null, premiumCurrency=null, premiumDate=null, premiumTime=null, attributes={}, "
            + "deal=null, providerId=null}, ManageableTrade{uniqueId=null, parentPositionId=null, quantity=30461, "
            + "securityLink=ManageableSecurityLink{objectId=null, externalId=Bundle[eid~4], target=null}, counterpartyExternalId=eid~0395, "
            + "tradeDate=2019-01-01, tradeTime=11:00Z, premium=null, premiumCurrency=null, premiumDate=null, premiumTime=null, attributes={}, "
            + "deal=null, providerId=null}], attributes={A=4}, providerId=eid~1000}");
    assertEquals(position, position);
    assertEquals(position.hashCode(), other.hashCode());
    other.setAttributes(Collections.<String, String> emptyMap());
    assertNotEquals(position, other);
    setFields(other);
    other.setProviderId(S_EID);
    assertNotEquals(position, other);
    setFields(other);
    other.setQuantity(QUANTITY.add(BigDecimal.ONE));
    assertNotEquals(position, other);
    setFields(other);
    other.setSecurityLink(new ManageableSecurityLink(PROVIDER_ID));
    assertNotEquals(position, other);
    setFields(other);
    other.setTrades(TRADES.subList(0, 1));
    assertNotEquals(position, other);
    setFields(other);
    other.setUniqueId(UniqueId.of("uid", "34533"));
    assertNotEquals(position, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final ManageablePosition position = new ManageablePosition();
    setFields(position);
    assertEquals(position.propertyNames().size(), 7);
    final Meta bean = position.metaBean();
    assertEquals(bean.attributes().get(position), ATTRIBUTES);
    assertEquals(bean.providerId().get(position), PROVIDER_ID);
    assertEquals(bean.quantity().get(position), QUANTITY);
    assertEquals(bean.securityLink().get(position), SECURITY_LINK);
    assertEquals(bean.trades().get(position), TRADES);
    assertEquals(bean.uniqueId().get(position), P_UID);
    assertEquals(position.property("attributes").get(), ATTRIBUTES);
    assertEquals(position.property("providerId").get(), PROVIDER_ID);
    assertEquals(position.property("quantity").get(), QUANTITY);
    assertEquals(position.property("securityLink").get(), SECURITY_LINK);
    assertEquals(position.property("trades").get(), TRADES);
    assertEquals(position.property("uniqueId").get(), P_UID);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final ManageablePosition position = new ManageablePosition();
    setFields(position);
    assertEncodeDecodeCycle(ManageablePosition.class, position);
  }

  private static void setFields(final ManageablePosition position) {
    position.setAttributes(ATTRIBUTES);
    position.setProviderId(PROVIDER_ID);
    position.setQuantity(QUANTITY);
    position.setSecurityLink(SECURITY_LINK);
    position.setTrades(TRADES);
    position.setUniqueId(P_UID);
  }

}
