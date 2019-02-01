/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.equity;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link EquitySecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class EquitySecurityTest extends AbstractBeanTestCase {
  private static final String SHORT_NAME = "ABC";
  private static final String EXCHANGE = "DEF";
  private static final String EXCHANGE_CODE = "GHI";
  private static final String COMPANY_NAME = "JKL";
  private static final Currency CCY = Currency.AUD;
  private static final GICSCode GICS = GICSCode.of(20305020);
  private static final boolean PREFERRED = true;

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(EquitySecurity.class,
        Arrays.asList("shortName", "exchange", "exchangeCode", "companyName", "currency", "gicsCode", "preferred"),
        Arrays.asList(SHORT_NAME, EXCHANGE, EXCHANGE_CODE, COMPANY_NAME, CCY, GICS, PREFERRED),
        Arrays.asList(EXCHANGE, EXCHANGE_CODE, COMPANY_NAME, SHORT_NAME, Currency.USD, GICSCode.of(20305030), !PREFERRED));
  }

  /**
   * Tests that all fields are set in the constructor.
   */
  public void testConstructor() {
    EquitySecurity sec = new EquitySecurity();
    assertEquals(sec.getSecurityType(), EquitySecurity.SECURITY_TYPE);
    assertNull(sec.getCompanyName());
    assertNull(sec.getCurrency());
    assertNull(sec.getExchange());
    assertNull(sec.getExchangeCode());
    assertNull(sec.getGicsCode());
    assertNull(sec.getShortName());
    assertFalse(sec.isPreferred());
    sec = new EquitySecurity(EXCHANGE, EXCHANGE_CODE, COMPANY_NAME, CCY);
    assertEquals(sec.getSecurityType(), EquitySecurity.SECURITY_TYPE);
    assertEquals(sec.getCompanyName(), COMPANY_NAME);
    assertEquals(sec.getCurrency(), CCY);
    assertEquals(sec.getExchange(), EXCHANGE);
    assertEquals(sec.getExchangeCode(), EXCHANGE_CODE);
    assertNull(sec.getGicsCode());
    assertNull(sec.getShortName());
    assertFalse(sec.isPreferred());
  }

  /**
   * Tests that accept() calls the correct method.
   */
  public void testVisitor() {
    final EquitySecurity sec = new EquitySecurity(EXCHANGE, EXCHANGE_CODE, COMPANY_NAME, CCY);
    assertEquals(sec.accept(TestVisitor.INSTANCE), "EquitySecurity");
  }

  private static class TestVisitor extends FinancialSecurityVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitEquitySecurity(final EquitySecurity security) {
      return security.getClass().getSimpleName();
    }
  }
}
