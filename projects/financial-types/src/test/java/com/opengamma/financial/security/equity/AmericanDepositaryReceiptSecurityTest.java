/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.equity;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link AmericanDepositaryReceiptSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class AmericanDepositaryReceiptSecurityTest extends AbstractBeanTestCase {
  private static final String SHORT_NAME = "ABC";
  private static final String EXCHANGE = "DEF";
  private static final String EXCHANGE_CODE = "GHI";
  private static final String COMPANY_NAME = "JKL";
  private static final Currency CCY = Currency.AUD;
  private static final Currency FOREIGN = Currency.BRL;
  private static final GICSCode GICS = GICSCode.of(20305020);
  private static final ExternalIdBundle UNDERLYING = ExternalIdBundle.of("eid", "1");

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(AmericanDepositaryReceiptSecurity.class,
        Arrays.asList("shortName", "exchange", "exchangeCode", "companyName", "currency", "foreignCurrency", "gicsCode", "underlyingIdBundle"),
        Arrays.asList(SHORT_NAME, EXCHANGE, EXCHANGE_CODE, COMPANY_NAME, CCY, FOREIGN, GICS, UNDERLYING),
        Arrays.asList(EXCHANGE, EXCHANGE_CODE, COMPANY_NAME, SHORT_NAME, FOREIGN, CCY, GICSCode.of(20305030), ExternalIdBundle.of("eid", "2")));
  }

  /**
   * Tests that all fields are set in the constructor.
   */
  public void testConstructor() {
    AmericanDepositaryReceiptSecurity sec = new AmericanDepositaryReceiptSecurity();
    assertEquals(sec.getSecurityType(), AmericanDepositaryReceiptSecurity.SECURITY_TYPE);
    assertNull(sec.getCompanyName());
    assertNull(sec.getCurrency());
    assertNull(sec.getExchange());
    assertNull(sec.getExchangeCode());
    assertNull(sec.getForeignCurrency());
    assertNull(sec.getGicsCode());
    assertNull(sec.getShortName());
    assertNull(sec.getUnderlyingIdBundle());
    sec = new AmericanDepositaryReceiptSecurity(EXCHANGE, EXCHANGE_CODE, COMPANY_NAME, CCY, FOREIGN, UNDERLYING);
    assertEquals(sec.getSecurityType(), AmericanDepositaryReceiptSecurity.SECURITY_TYPE);
    assertEquals(sec.getCompanyName(), COMPANY_NAME);
    assertEquals(sec.getCurrency(), CCY);
    assertEquals(sec.getExchange(), EXCHANGE);
    assertEquals(sec.getExchangeCode(), EXCHANGE_CODE);
    assertEquals(sec.getForeignCurrency(), FOREIGN);
    assertNull(sec.getGicsCode());
    assertNull(sec.getShortName());
    assertEquals(sec.getUnderlyingIdBundle(), UNDERLYING);
    sec = new AmericanDepositaryReceiptSecurity(SHORT_NAME, EXCHANGE, EXCHANGE_CODE, COMPANY_NAME, CCY, FOREIGN, GICS, UNDERLYING);
    assertEquals(sec.getSecurityType(), AmericanDepositaryReceiptSecurity.SECURITY_TYPE);
    assertEquals(sec.getCompanyName(), COMPANY_NAME);
    assertEquals(sec.getCurrency(), CCY);
    assertEquals(sec.getExchange(), EXCHANGE);
    assertEquals(sec.getExchangeCode(), EXCHANGE_CODE);
    assertEquals(sec.getForeignCurrency(), FOREIGN);
    assertEquals(sec.getGicsCode(), GICS);
    assertEquals(sec.getShortName(), SHORT_NAME);
    assertEquals(sec.getUnderlyingIdBundle(), UNDERLYING);
  }

  /**
   * Tests that accept() calls the correct method.
   */
  public void testVisitor() {
    final AmericanDepositaryReceiptSecurity sec = new AmericanDepositaryReceiptSecurity(SHORT_NAME, EXCHANGE, EXCHANGE_CODE, COMPANY_NAME, CCY, FOREIGN, GICS,
        UNDERLYING);
    assertEquals(sec.accept(TestVisitor.INSTANCE), "AmericanDepositaryReceiptSecurity");
  }

  /**
   *
   */
  private static final class TestVisitor extends FinancialSecurityVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitAmericanDepositaryReceiptSecurity(final AmericanDepositaryReceiptSecurity security) {
      return security.getClass().getSimpleName();
    }
  }
}
