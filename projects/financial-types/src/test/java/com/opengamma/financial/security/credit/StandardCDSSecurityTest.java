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

import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link StandardCDSSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class StandardCDSSecurityTest extends AbstractBeanTestCase {
  private static final LocalDate TRADE_DATE = LocalDate.of(2020, 1, 1);
  private static final LocalDate MATURITY_DATE = LocalDate.of(2022, 1, 1);
  private static final ExternalId REFERENCE_ENTITY = ExternalId.of("eid", "1");
  private static final boolean BUY_PROTECTION = true;
  private static final InterestRateNotional NOTIONAL = new InterestRateNotional(Currency.USD, 10000000);
  private static final DebtSeniority SENIORITY = DebtSeniority.SENIOR;
  private static final double COUPON = 0.01;
  private static final ExternalIdBundle IDS = ExternalIdBundle.of("eid", "2");
  private static final String NAME = "name";

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(StandardCDSSecurity.class,
        Arrays.asList("tradeDate", "maturityDate", "referenceEntity", "buyProtection", "notional", "debtSeniority", "coupon"),
        Arrays.asList(TRADE_DATE, MATURITY_DATE, REFERENCE_ENTITY, BUY_PROTECTION, NOTIONAL, SENIORITY, COUPON), Arrays.asList(MATURITY_DATE, TRADE_DATE,
            ExternalId.of("eid", "2"), !BUY_PROTECTION, new InterestRateNotional(Currency.AUD, NOTIONAL.getAmount() * 2), DebtSeniority.JRSUBUT2, COUPON * 5));
  }

  /**
   * Tests that fields are set in the constructor.
   */
  public void testConstructor() {
    StandardCDSSecurity security = new StandardCDSSecurity();
    assertEquals(security.getSecurityType(), StandardCDSSecurity.SECURITY_TYPE);
    assertEquals(security.getCoupon(), 0.);
    assertNull(security.getDebtSeniority());
    assertTrue(security.getExternalIdBundle().isEmpty());
    assertNull(security.getMaturityDate());
    assertEquals(security.getName(), "");
    assertNull(security.getNotional());
    assertNull(security.getTradeDate());
    assertFalse(security.isBuyProtection());
    security = new StandardCDSSecurity(IDS, TRADE_DATE, MATURITY_DATE, REFERENCE_ENTITY, NOTIONAL, BUY_PROTECTION, COUPON, SENIORITY);
    assertEquals(security.getSecurityType(), StandardCDSSecurity.SECURITY_TYPE);
    assertEquals(security.getCoupon(), COUPON);
    assertEquals(security.getDebtSeniority(), SENIORITY);
    assertEquals(security.getExternalIdBundle(), IDS);
    assertEquals(security.getMaturityDate(), MATURITY_DATE);
    assertEquals(security.getName(), "");
    assertEquals(security.getNotional(), NOTIONAL);
    assertEquals(security.getTradeDate(), TRADE_DATE);
    assertEquals(security.isBuyProtection(), BUY_PROTECTION);
    security = new StandardCDSSecurity(IDS, NAME, TRADE_DATE, MATURITY_DATE, REFERENCE_ENTITY, NOTIONAL, BUY_PROTECTION, COUPON, SENIORITY);
    assertEquals(security.getSecurityType(), StandardCDSSecurity.SECURITY_TYPE);
    assertEquals(security.getCoupon(), COUPON);
    assertEquals(security.getDebtSeniority(), SENIORITY);
    assertEquals(security.getExternalIdBundle(), IDS);
    assertEquals(security.getMaturityDate(), MATURITY_DATE);
    assertEquals(security.getName(), NAME);
    assertEquals(security.getNotional(), NOTIONAL);
    assertEquals(security.getTradeDate(), TRADE_DATE);
    assertEquals(security.isBuyProtection(), BUY_PROTECTION);
  }

  /**
   * Tests that the accept() method calls the correct method in the visitor.
   */
  public void testAccept() {
    final StandardCDSSecurity security = new StandardCDSSecurity(IDS, NAME, TRADE_DATE, MATURITY_DATE, REFERENCE_ENTITY, NOTIONAL, BUY_PROTECTION, COUPON,
        SENIORITY);
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
    public String visitStandardCDSSecurity(final StandardCDSSecurity security) {
      return security.getName();
    }
  }
}
