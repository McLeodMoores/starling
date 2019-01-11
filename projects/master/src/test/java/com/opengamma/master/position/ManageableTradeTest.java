/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.position;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.joda.beans.MetaBean;
import org.joda.beans.Property;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneOffset;

import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.core.security.impl.SimpleSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.position.ManageableTrade.Meta;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ManageableTrade}.
 */
@Test(groups = TestGroup.UNIT)
public class ManageableTradeTest {
  private static final UniqueId S_UID = UniqueId.of("sec", "34");
  private static final ExternalIdBundle S_EIDS = ExternalIdBundle.of("abc", "def");
  private static final UniqueId T_UID = UniqueId.of("trade", "1");
  private static final UniqueId P_UID = UniqueId.of("pos", "100");
  private static final BigDecimal QUANTITY = BigDecimal.valueOf(1760);
  private static final SimpleSecurity SECURITY = new SimpleSecurity(S_UID, S_EIDS, "type", "name");
  static {
    SECURITY.setUniqueId(S_UID);
  }
  private static final ManageableSecurityLink SECURITY_LINK = ManageableSecurityLink.of(SECURITY);
  private static final ExternalId CTPTY = ExternalId.of("counter", "zxc");
  private static final LocalDate TRADE_DATE = LocalDate.of(2018, 1, 1);
  private static final LocalTime TRADE_TIME = LocalTime.of(1, 53);
  private static final OffsetTime TRADE_TIME_OFFSET = OffsetTime.of(TRADE_TIME, ZoneOffset.UTC);
  private static final Double PREMIUM = 3678.;
  private static final Currency PREMIUM_CURRENCY = Currency.AUD;
  private static final LocalDate PREMIUM_DATE = LocalDate.of(2018, 1, 2);
  private static final OffsetTime PREMIUM_TIME = OffsetTime.of(LocalTime.of(8, 0), ZoneOffset.UTC);
  private static final Map<String, String> ATTRIBUTES = new HashMap<>();
  private static final Deal DEAL = new DummyDeal(39857);
  private static final ExternalId PROVIDER_ID = ExternalId.of("prov", "9087");
  static {
    ATTRIBUTES.put("a", "b");
    ATTRIBUTES.put("c", "d");
  }

  /**
   * Tests that the trade cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTrade() {
    new ManageableTrade(null);
  }

  /**
   * Tests that the trade attributes cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAttributesSimpleTrade() {
    new ManageableTrade(new SimpleTrade());
  }

  /**
   * Tests that the trade attributes cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAttributesManageableTrade() {
    new ManageableTrade(new ManageableTrade());
  }

  /**
   * Tests that the trade attributes cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurityLinkSimpleTrade() {
    final SimpleTrade trade = new SimpleTrade();
    trade.setAttributes(ATTRIBUTES);
    new ManageableTrade(trade);
  }

  /**
   * Tests that the trade attributes cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurityLinkManageableTrade() {
    final DummyTrade dummyTrade = new DummyTrade();
    new ManageableTrade(dummyTrade);
  }

  /**
   * Tests that the counterparty id must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCounterpartyIdSimpleTrade() {
    final SimpleTrade simple = new SimpleTrade(SECURITY_LINK, QUANTITY, new SimpleCounterparty(CTPTY), TRADE_DATE, TRADE_TIME_OFFSET);
    simple.setCounterparty(null);
    new ManageableTrade(simple);
  }

  /**
   * Tests that the counterparty is used if available.
   */
  @Test
  public void testCounterpartyIdUsed() {
    final DummyTrade dummy = new DummyTrade();
    dummy.setCounterparty(CTPTY);
    dummy.setSecurityLink(SECURITY_LINK);
    final ManageableTrade manageable = new ManageableTrade(dummy);
    assertEquals(manageable.getCounterpartyExternalId(), dummy.getCounterpartyExternalId());
    assertEquals(manageable.getCounterparty(), dummy.getCounterparty());
  }

  /**
   * Tests that a dummy counterparty id is set.
   */
  @Test
  public void testDummyCounterpartyId() {
    final DummyTrade dummy = new DummyTrade();
    dummy.setSecurityLink(SECURITY_LINK);
    final ManageableTrade manageable = new ManageableTrade(dummy);
    assertEquals(manageable.getCounterpartyExternalId(), ExternalId.of(Counterparty.DEFAULT_SCHEME, "DUMMY COUNTERPARTY"));
    assertEquals(manageable.getCounterparty(), new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "DUMMY COUNTERPARTY")));
  }

  /**
   * Tests construction from a SimpleTrade.
   */
  @Test
  public void testConstructionFromSimpleTrade() {
    final SimpleTrade simple = new SimpleTrade(SECURITY_LINK, QUANTITY, new SimpleCounterparty(CTPTY), TRADE_DATE, TRADE_TIME_OFFSET);
    simple.setAttributes(ATTRIBUTES);
    final ManageableTrade manageable = new ManageableTrade(simple);
    assertEquals(manageable.getAttributes(), simple.getAttributes());
    assertEquals(manageable.getCounterparty(), simple.getCounterparty());
    assertEquals(manageable.getCounterpartyExternalId(), CTPTY);
    assertNull(manageable.getDeal());
    assertNull(manageable.getParentPositionId());
    assertNull(manageable.getPremium());
    assertNull(manageable.getPremiumCurrency());
    assertNull(manageable.getPremiumDate());
    assertNull(manageable.getPremiumTime());
    assertNull(manageable.getProviderId());
    assertEquals(manageable.getQuantity(), QUANTITY);
    assertEquals(manageable.getSecurity(), SECURITY);
    assertEquals(manageable.getSecurityLink(), SECURITY_LINK);
    assertEquals(manageable.getTradeDate(), TRADE_DATE);
    assertEquals(manageable.getTradeTime(), TRADE_TIME_OFFSET);
    assertNull(manageable.getUniqueId());
  }

  /**
   * Tests construction from another ManageableTrade.
   */
  @Test
  public void testCopyConstructor() {
    final ManageableTrade other = new ManageableTrade();
    setFields(other);
    final ManageableTrade manageable = new ManageableTrade(other);
    assertEquals(manageable.getAttributes(), other.getAttributes());
    assertEquals(manageable.getCounterparty(), other.getCounterparty());
    assertEquals(manageable.getCounterpartyExternalId(), other.getCounterpartyExternalId());
    assertNull(manageable.getDeal());
    assertNull(manageable.getParentPositionId());
    assertEquals(manageable.getPremium(), other.getPremium());
    assertEquals(manageable.getPremiumCurrency(), other.getPremiumCurrency());
    assertEquals(manageable.getPremiumDate(), other.getPremiumDate());
    assertEquals(manageable.getPremiumTime(), other.getPremiumTime());
    assertNull(manageable.getProviderId());
    assertEquals(manageable.getQuantity(), other.getQuantity());
    assertEquals(manageable.getSecurity(), other.getSecurity());
    assertEquals(manageable.getSecurityLink(), other.getSecurityLink());
    assertEquals(manageable.getTradeDate(), other.getTradeDate());
    assertEquals(manageable.getTradeTime(), other.getTradeTime());
    assertNull(manageable.getUniqueId());
  }

  /**
   * Tests construction from trade details.
   */
  @Test
  public void testConstructionFromDetails1() {
    final ExternalId eid = S_EIDS.getExternalIds().iterator().next();
    final ManageableTrade trade = new ManageableTrade(QUANTITY, eid, TRADE_DATE, TRADE_TIME_OFFSET, CTPTY);
    assertEquals(trade.getAttributes(), new HashMap<String, String>());
    assertEquals(trade.getCounterparty(), new SimpleCounterparty(CTPTY));
    assertEquals(trade.getCounterpartyExternalId(), CTPTY);
    assertNull(trade.getDeal());
    assertNull(trade.getParentPositionId());
    assertNull(trade.getPremium());
    assertNull(trade.getPremiumCurrency());
    assertNull(trade.getPremiumDate());
    assertNull(trade.getPremiumTime());
    assertNull(trade.getProviderId());
    assertEquals(trade.getQuantity(), QUANTITY);
    assertNull(trade.getSecurity());
    assertEquals(trade.getSecurityLink(), new ManageableSecurityLink(eid));
    assertEquals(trade.getTradeDate(), TRADE_DATE);
    assertEquals(trade.getTradeTime(), TRADE_TIME_OFFSET);
    assertNull(trade.getUniqueId());
  }

  /**
   * Tests construction from trade details.
   */
  @Test
  public void testConstructionFromDetails2() {
    final ManageableTrade trade = new ManageableTrade(QUANTITY, S_EIDS, TRADE_DATE, TRADE_TIME_OFFSET, CTPTY);
    assertEquals(trade.getAttributes(), new HashMap<String, String>());
    assertEquals(trade.getCounterparty(), new SimpleCounterparty(CTPTY));
    assertEquals(trade.getCounterpartyExternalId(), CTPTY);
    assertNull(trade.getDeal());
    assertNull(trade.getParentPositionId());
    assertNull(trade.getPremium());
    assertNull(trade.getPremiumCurrency());
    assertNull(trade.getPremiumDate());
    assertNull(trade.getPremiumTime());
    assertNull(trade.getProviderId());
    assertEquals(trade.getQuantity(), QUANTITY);
    assertNull(trade.getSecurity());
    assertEquals(trade.getSecurityLink(), new ManageableSecurityLink(S_EIDS));
    assertEquals(trade.getTradeDate(), TRADE_DATE);
    assertEquals(trade.getTradeTime(), TRADE_TIME_OFFSET);
    assertNull(trade.getUniqueId());
  }

  /**
   * Tests adding an attribute.
   */
  @Test
  public void testAddAttribute() {
    final String k1 = "k1", k2 = "k2", v1 = "v1", v2 = "v2";
    final ManageableTrade trade = new ManageableTrade();
    assertTrue(trade.getAttributes().isEmpty());
    trade.addAttribute(k1, v1);
    assertEquals(trade.getAttributes().keySet(), Collections.singleton(k1));
    assertEquals(trade.getAttributes().values(), Collections.singleton(v1));
    trade.addAttribute(k2, v2);
    assertEquals(trade.getAttributes().keySet(), new HashSet<>(Arrays.asList(k1, k2)));
    assertEquals(trade.getAttributes().values(), new HashSet<>(Arrays.asList(v1, v2)));
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final ManageableTrade trade = new ManageableTrade();
    setFields(trade);
    final ManageableTrade other = new ManageableTrade();
    setFields(other);
    assertEquals(trade, trade);
    assertEquals(trade.toString(),
        "ManageableTrade{uniqueId=trade~1, parentPositionId=pos~100, quantity=1760, "
            + "securityLink=ManageableSecurityLink{objectId=sec~34, externalId=Bundle[], target=SimpleSecurity{uniqueId=sec~34, "
            + "externalIdBundle=Bundle[abc~def], securityType=type, name=name, attributes={}}}, counterpartyExternalId=counter~zxc, "
            + "tradeDate=2018-01-01, tradeTime=01:53Z, premium=3678.0, premiumCurrency=AUD, premiumDate=2018-01-02, premiumTime=08:00Z, "
            + "attributes={a=b, c=d}, deal=com.opengamma.master.position.ManageableTradeTest$DummyDeal@9bb1, providerId=prov~9087}");
    assertEquals(trade, other);
    assertEquals(trade.hashCode(), other.hashCode());
    other.setAttributes(new HashMap<String, String>());
    assertNotEquals(trade, other);
    setFields(other);
    other.setCounterpartyExternalId(PROVIDER_ID);
    assertNotEquals(trade, other);
    setFields(other);
    other.setDeal(null);
    assertNotEquals(trade, other);
    setFields(other);
    other.setParentPositionId(null);
    assertNotEquals(trade, other);
    setFields(other);
    other.setPremium(null);
    assertNotEquals(trade, other);
    setFields(other);
    other.setPremiumCurrency(null);
    assertNotEquals(trade, other);
    setFields(other);
    other.setPremiumDate(null);
    assertNotEquals(trade, other);
    setFields(other);
    other.setPremiumTime(null);
    assertNotEquals(trade, other);
    setFields(other);
    other.setProviderId(null);
    assertNotEquals(trade, other);
    setFields(other);
    other.setQuantity(QUANTITY.add(BigDecimal.ONE));
    assertNotEquals(trade, other);
    setFields(other);
    other.setSecurityLink(ManageableSecurityLink.of(new SimpleSecurity(T_UID, S_EIDS, "type", "name")));
    assertNotEquals(trade, other);
    setFields(other);
    other.setTradeDate(TRADE_DATE.plusDays(1));
    assertNotEquals(trade, other);
    setFields(other);
    other.setTradeTime(null);
    assertNotEquals(trade, other);
    setFields(other);
    other.setUniqueId(null);
    assertNotEquals(trade, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final ManageableTrade trade = new ManageableTrade();
    setFields(trade);
    assertEquals(trade.propertyNames().size(), 14);
    final Meta bean = trade.metaBean();
    assertEquals(bean.attributes().get(trade), ATTRIBUTES);
    assertEquals(bean.counterpartyExternalId().get(trade), CTPTY);
    assertEquals(bean.deal().get(trade), DEAL);
    assertEquals(bean.parentPositionId().get(trade), P_UID);
    assertEquals(bean.premium().get(trade), PREMIUM);
    assertEquals(bean.premiumCurrency().get(trade), PREMIUM_CURRENCY);
    assertEquals(bean.premiumDate().get(trade), PREMIUM_DATE);
    assertEquals(bean.premiumTime().get(trade), PREMIUM_TIME);
    assertEquals(bean.providerId().get(trade), PROVIDER_ID);
    assertEquals(bean.quantity().get(trade), QUANTITY);
    assertEquals(bean.securityLink().get(trade), SECURITY_LINK);
    assertEquals(bean.tradeDate().get(trade), TRADE_DATE);
    assertEquals(bean.tradeTime().get(trade), TRADE_TIME_OFFSET);
    assertEquals(bean.uniqueId().get(trade), T_UID);
    assertEquals(trade.property("attributes").get(), ATTRIBUTES);
    assertEquals(trade.property("counterpartyExternalId").get(), CTPTY);
    assertEquals(trade.property("deal").get(), DEAL);
    assertEquals(trade.property("parentPositionId").get(), P_UID);
    assertEquals(trade.property("premium").get(), PREMIUM);
    assertEquals(trade.property("premiumCurrency").get(), PREMIUM_CURRENCY);
    assertEquals(trade.property("premiumDate").get(), PREMIUM_DATE);
    assertEquals(trade.property("premiumTime").get(), PREMIUM_TIME);
    assertEquals(trade.property("providerId").get(), PROVIDER_ID);
    assertEquals(trade.property("quantity").get(), QUANTITY);
    assertEquals(trade.property("securityLink").get(), SECURITY_LINK);
    assertEquals(trade.property("tradeDate").get(), TRADE_DATE);
    assertEquals(trade.property("tradeTime").get(), TRADE_TIME_OFFSET);
    assertEquals(trade.property("uniqueId").get(), T_UID);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {

  }

  private static void setFields(final ManageableTrade trade) {
    trade.setAttributes(ATTRIBUTES);
    trade.setCounterpartyExternalId(CTPTY);
    trade.setDeal(DEAL);
    trade.setParentPositionId(P_UID);
    trade.setPremium(PREMIUM);
    trade.setPremiumCurrency(PREMIUM_CURRENCY);
    trade.setPremiumDate(PREMIUM_DATE);
    trade.setPremiumTime(PREMIUM_TIME);
    trade.setProviderId(PROVIDER_ID);
    trade.setQuantity(QUANTITY);
    trade.setSecurityLink(SECURITY_LINK);
    trade.setTradeDate(TRADE_DATE);
    trade.setTradeTime(TRADE_TIME_OFFSET);
    trade.setUniqueId(T_UID);
  }

  private static final class DummyDeal implements Deal {
    private final int _val;

    public DummyDeal(final int val) {
      _val = val;
    }

    @Override
    public MetaBean metaBean() {
      return null;
    }

    @Override
    public <R> Property<R> property(final String arg0) {
      return null;
    }

    @Override
    public Set<String> propertyNames() {
      return null;
    }

    @Override
    public int hashCode() {
      return _val;
    }

    @Override
    public boolean equals(final Object o) {
      if (o instanceof DummyDeal) {
        return _val == ((DummyDeal) o)._val;
      }
      return false;
    }
  }

  private static class DummyTrade implements Trade {
    private Counterparty _counterparty;
    private SecurityLink _securityLink;

    @Override
    public UniqueId getUniqueId() {
      return null;
    }

    @Override
    public BigDecimal getQuantity() {
      return null;
    }

    public void setSecurityLink(final SecurityLink securityLink) {
      _securityLink = securityLink;
    }

    @Override
    public SecurityLink getSecurityLink() {
      return _securityLink;
    }

    @Override
    public Security getSecurity() {
      return null;
    }

    @Override
    public Map<String, String> getAttributes() {
      return Collections.<String, String> emptyMap();
    }

    @Override
    public void setAttributes(final Map<String, String> attributes) {
    }

    @Override
    public void addAttribute(final String key, final String value) {
    }

    public void setCounterparty(final ExternalId id) {
      _counterparty = new SimpleCounterparty(id);
    }

    @Override
    public Counterparty getCounterparty() {
      return _counterparty;
    }

    public ExternalId getCounterpartyExternalId() {
      return _counterparty.getExternalId();
    }

    @Override
    public LocalDate getTradeDate() {
      return null;
    }

    @Override
    public OffsetTime getTradeTime() {
      return null;
    }

    @Override
    public Double getPremium() {
      return null;
    }

    @Override
    public Currency getPremiumCurrency() {
      return null;
    }

    @Override
    public LocalDate getPremiumDate() {
      return null;
    }

    @Override
    public OffsetTime getPremiumTime() {
      return null;
    }

  }
}
