/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.cashflow;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests for {@link CashFlowSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class CashFlowSecurityTest extends AbstractBeanTestCase {
  private static final Currency CCY = Currency.USD;
  private static final ZonedDateTime SETTLEMENT = DateUtils.getUTCDate(2020, 1, 1);
  private static final double NOTIONAL = 1000000;
  private static final CashFlowSecurity SECURITY = new CashFlowSecurity(CCY, SETTLEMENT, NOTIONAL);

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(CashFlowSecurity.class, Arrays.asList("securityType", "currency", "settlement", "amount"),
        Arrays.asList(CashFlowSecurity.SECURITY_TYPE, CCY, SETTLEMENT, NOTIONAL),
        Arrays.asList(CashSecurity.SECURITY_TYPE, Currency.AUD, SETTLEMENT.plusDays(1), NOTIONAL * 10));
  }

  /**
   * Tests the security type string.
   */
  public void testSecurityType() {
    assertEquals(SECURITY.getSecurityType(), CashFlowSecurity.SECURITY_TYPE);
  }

  /**
   * Tests that the accept() method points to the correct method in the visitor.
   */
  public void testVisitor() {
    assertEquals(SECURITY.accept(TestVisitor.INSTANCE), "visited");
  }

  /**
   *
   */
  private static final class TestVisitor extends FinancialSecurityVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitCashFlowSecurity(final CashFlowSecurity security) {
      return "visited";
    }

  }

}
