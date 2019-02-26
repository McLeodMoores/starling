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
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ExchangeTradedFundSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class ExchangeTradedFundSecurityTest extends AbstractBeanTestCase {
  private static final String SHORT_NAME = "ABC";
  private static final String EXCHANGE = "DEF";
  private static final ExternalId EXCHANGE_CODE = ExternalId.of("eid", "1");
  private static final ExternalId UNDERLYING_ID = ExternalId.of("eid", "2");

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(ExchangeTradedFundSecurity.class, Arrays.asList("shortName", "exchangeDescription", "exchangeCode", "underlyingId"),
        Arrays.asList(SHORT_NAME, EXCHANGE, EXCHANGE_CODE, UNDERLYING_ID), Arrays.asList(EXCHANGE, SHORT_NAME, UNDERLYING_ID, EXCHANGE_CODE));
  }

  /**
   * Tests that all fields are set in the constructor.
   */
  public void testConstructor() {
    ExchangeTradedFundSecurity sec = new ExchangeTradedFundSecurity();
    assertEquals(sec.getSecurityType(), ExchangeTradedFundSecurity.SECURITY_TYPE);
    assertNull(sec.getExchangeCode());
    assertNull(sec.getExchangeDescription());
    assertNull(sec.getShortName());
    assertNull(sec.getUnderlyingId());
    sec = new ExchangeTradedFundSecurity(SHORT_NAME, EXCHANGE, EXCHANGE_CODE, UNDERLYING_ID);
    assertEquals(sec.getSecurityType(), ExchangeTradedFundSecurity.SECURITY_TYPE);
    assertEquals(sec.getExchangeCode(), EXCHANGE_CODE);
    assertEquals(sec.getExchangeDescription(), EXCHANGE);
    assertEquals(sec.getShortName(), SHORT_NAME);
    assertEquals(sec.getUnderlyingId(), UNDERLYING_ID);
  }

  /**
   * Tests that accept() calls the correct method.
   */
  public void testVisitor() {
    final ExchangeTradedFundSecurity sec = new ExchangeTradedFundSecurity(SHORT_NAME, EXCHANGE, EXCHANGE_CODE, UNDERLYING_ID);
    assertEquals(sec.accept(TestVisitor.INSTANCE), "ExchangeTradedFundSecurity");
  }

  /**
   *
   */
  private static final class TestVisitor extends FinancialSecurityVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitExchangeTradedFundSecurity(final ExchangeTradedFundSecurity security) {
      return security.getClass().getSimpleName();
    }
  }
}
