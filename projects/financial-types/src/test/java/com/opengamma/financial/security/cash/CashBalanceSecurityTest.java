/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.cash;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link CashBalanceSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class CashBalanceSecurityTest extends AbstractBeanTestCase {
  private static final Currency CCY = Currency.USD;
  private static final double NOTIONAL = 1000000;
  private static final CashBalanceSecurity SECURITY = new CashBalanceSecurity(CCY, NOTIONAL);

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(CashBalanceSecurity.class, Arrays.asList("securityType", "currency", "amount"),
        Arrays.asList(CashBalanceSecurity.SECURITY_TYPE, CCY, NOTIONAL), Arrays.asList(CashSecurity.SECURITY_TYPE, Currency.CAD, NOTIONAL * 10));
  }

  /**
   * Tests the security type string.
   */
  public void testSecurityType() {
    assertEquals(SECURITY.getSecurityType(), CashBalanceSecurity.SECURITY_TYPE);
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
    public String visitCashBalanceSecurity(final CashBalanceSecurity security) {
      return "visited";
    }

  }

}
