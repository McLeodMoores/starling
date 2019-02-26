/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.credit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.core.link.SecurityLink;
import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Tests for {@link IndexCDSSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class IndexCDSSecurityTest extends AbstractBeanTestCase {
  private static final LocalDate TRADE_DATE = LocalDate.of(2020, 1, 1);
  private static final boolean BUY_PROTECTION = true;
  private static final ExternalId INDEX_ID = ExternalId.of("eid", "1");
  private static final SecurityLink<IndexCDSDefinitionSecurity> INDEX_LINK = SecurityLink.resolvable(INDEX_ID);
  private static final Tenor INDEX_TENOR = Tenor.THREE_YEARS;
  private static final InterestRateNotional NOTIONAL = new InterestRateNotional(Currency.USD, 10000000);
  private static final ExternalIdBundle IDS = ExternalIdBundle.of("eid", "2");
  private static final String NAME = "name";

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(IndexCDSSecurity.class, Arrays.asList("tradeDate", "buyProtection", "underlyingIndex", "indexTenor", "notional"),
        Arrays.asList(TRADE_DATE, BUY_PROTECTION, INDEX_LINK, INDEX_TENOR, NOTIONAL), Arrays.asList(TRADE_DATE.plusDays(1), !BUY_PROTECTION,
            SecurityLink.resolvable(ExternalId.of("eid", "2")), Tenor.FOUR_YEARS, new InterestRateNotional(Currency.AUD, NOTIONAL.getAmount() * 2)));
  }

  /**
   * Tests that fields are set in the constructor.
   */
  public void testConstructor() {
    IndexCDSSecurity security = new IndexCDSSecurity();
    assertEquals(security.getSecurityType(), IndexCDSSecurity.SECURITY_TYPE);
    assertTrue(security.getExternalIdBundle().isEmpty());
    assertNull(security.getIndexTenor());
    assertEquals(security.getName(), "");
    assertNull(security.getNotional());
    assertNull(security.getTradeDate());
    assertNull(security.getUnderlyingIndex());
    assertFalse(security.isBuyProtection());
    security = new IndexCDSSecurity(IDS, BUY_PROTECTION, INDEX_LINK, INDEX_TENOR, TRADE_DATE, NOTIONAL);
    assertEquals(security.getSecurityType(), IndexCDSSecurity.SECURITY_TYPE);
    assertEquals(security.getExternalIdBundle(), IDS);
    assertEquals(security.getIndexTenor(), INDEX_TENOR);
    assertEquals(security.getName(), "");
    assertEquals(security.getNotional(), NOTIONAL);
    assertEquals(security.getTradeDate(), TRADE_DATE);
    assertEquals(security.getUnderlyingIndex(), INDEX_LINK);
    assertEquals(security.isBuyProtection(), BUY_PROTECTION);
    security = new IndexCDSSecurity(IDS, BUY_PROTECTION, INDEX_ID, INDEX_TENOR, TRADE_DATE, NOTIONAL);
    assertEquals(security.getSecurityType(), IndexCDSSecurity.SECURITY_TYPE);
    assertEquals(security.getExternalIdBundle(), IDS);
    assertEquals(security.getIndexTenor(), INDEX_TENOR);
    assertEquals(security.getName(), "");
    assertEquals(security.getNotional(), NOTIONAL);
    assertEquals(security.getTradeDate(), TRADE_DATE);
    assertEquals(security.getUnderlyingIndex(), INDEX_LINK);
    assertEquals(security.isBuyProtection(), BUY_PROTECTION);
    security = new IndexCDSSecurity(IDS, NAME, BUY_PROTECTION, INDEX_LINK, INDEX_TENOR, TRADE_DATE, NOTIONAL);
    assertEquals(security.getSecurityType(), IndexCDSSecurity.SECURITY_TYPE);
    assertEquals(security.getExternalIdBundle(), IDS);
    assertEquals(security.getIndexTenor(), INDEX_TENOR);
    assertEquals(security.getName(), NAME);
    assertEquals(security.getNotional(), NOTIONAL);
    assertEquals(security.getTradeDate(), TRADE_DATE);
    assertEquals(security.getUnderlyingIndex(), INDEX_LINK);
    assertEquals(security.isBuyProtection(), BUY_PROTECTION);
    security = new IndexCDSSecurity(IDS, NAME, BUY_PROTECTION, INDEX_ID, INDEX_TENOR, TRADE_DATE, NOTIONAL);
    assertEquals(security.getSecurityType(), IndexCDSSecurity.SECURITY_TYPE);
    assertEquals(security.getExternalIdBundle(), IDS);
    assertEquals(security.getIndexTenor(), INDEX_TENOR);
    assertEquals(security.getName(), NAME);
    assertEquals(security.getNotional(), NOTIONAL);
    assertEquals(security.getTradeDate(), TRADE_DATE);
    assertEquals(security.getUnderlyingIndex(), INDEX_LINK);
    assertEquals(security.isBuyProtection(), BUY_PROTECTION);
  }

  /**
   * Tests that the accept() method calls the correct method in the visitor.
   */
  public void testAccept() {
    final IndexCDSSecurity security = new IndexCDSSecurity(IDS, NAME, BUY_PROTECTION, INDEX_LINK, INDEX_TENOR, TRADE_DATE, NOTIONAL);
    assertEquals(security.accept(TestVisitor.INSTANCE), NAME);
  }

  /**
   *
   */
  private static final class TestVisitor extends FinancialSecurityVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitIndexCDSSecurity(final IndexCDSSecurity security) {
      return security.getName();
    }
  }
}
