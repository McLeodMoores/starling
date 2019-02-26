/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.future;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Tests for {@link IndexFutureSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class IndexFutureSecurityTest extends AbstractBeanTestCase {
  private static final Expiry EXPIRY = new Expiry(DateUtils.getUTCDate(2020, 3, 15));
  private static final String TRADING_EXCHANGE = "ABC";
  private static final String SETTLEMENT_EXCHANGE = "DEF";
  private static final Currency CCY = Currency.AUD;
  private static final double UNIT_AMOUNT = 25;
  private static final String CONTRACT_CATEGORY = "cat";
  private static final ExternalId UNDERLYING_ID = ExternalId.of("sec", "1");
  private static final IndexFutureSecurity FUTURE = new IndexFutureSecurity(EXPIRY, TRADING_EXCHANGE, SETTLEMENT_EXCHANGE, CCY, UNIT_AMOUNT, CONTRACT_CATEGORY,
      UNDERLYING_ID);

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(IndexFutureSecurity.class,
        Arrays.asList("expiry", "tradingExchange", "settlementExchange", "currency", "unitAmount", "contractCategory", "underlyingId"),
        Arrays.asList(EXPIRY, TRADING_EXCHANGE, SETTLEMENT_EXCHANGE, CCY, UNIT_AMOUNT, CONTRACT_CATEGORY, UNDERLYING_ID),
        Arrays.asList(new Expiry(DateUtils.getUTCDate(2020, 6, 15)), SETTLEMENT_EXCHANGE, TRADING_EXCHANGE, Currency.BRL, UNIT_AMOUNT * 2, "con",
            ExternalId.of("sec", "PQR")));
  }

  /**
   * Tests that fields are set in the constructor.
   */
  @Test
  public void testConstructor() {
    @SuppressWarnings("deprecation")
    IndexFutureSecurity future = new IndexFutureSecurity(EXPIRY, TRADING_EXCHANGE, SETTLEMENT_EXCHANGE, CCY, UNIT_AMOUNT, CONTRACT_CATEGORY);
    assertEquals(future.getExpiry(), EXPIRY);
    assertEquals(future.getTradingExchange(), TRADING_EXCHANGE);
    assertEquals(future.getSettlementExchange(), SETTLEMENT_EXCHANGE);
    assertEquals(future.getCurrency(), CCY);
    assertEquals(future.getUnitAmount(), UNIT_AMOUNT);
    assertEquals(future.getContractCategory(), CONTRACT_CATEGORY);
    assertNull(future.getUnderlyingId());
    future = new IndexFutureSecurity(EXPIRY, TRADING_EXCHANGE, SETTLEMENT_EXCHANGE, CCY, UNIT_AMOUNT, CONTRACT_CATEGORY, UNDERLYING_ID);
    assertEquals(future.getExpiry(), EXPIRY);
    assertEquals(future.getTradingExchange(), TRADING_EXCHANGE);
    assertEquals(future.getSettlementExchange(), SETTLEMENT_EXCHANGE);
    assertEquals(future.getCurrency(), CCY);
    assertEquals(future.getUnitAmount(), UNIT_AMOUNT);
    assertEquals(future.getContractCategory(), CONTRACT_CATEGORY);
    assertEquals(future.getUnderlyingId(), UNDERLYING_ID);
  }

  /**
   * Tests the security type.
   */
  public void testSecurityType() {
    assertEquals(FUTURE.getSecurityType(), FutureSecurity.SECURITY_TYPE);
  }

  /**
   * Tests that the accept() method points to the right method in the visitor.
   */
  public void testAcceptVisitor() {
    assertEquals(FUTURE.accept(TestVisitor.INSTANCE), "visited");
  }

  /**
   *
   */
  private static final class TestVisitor extends FinancialSecurityVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitIndexFutureSecurity(final IndexFutureSecurity security) {
      return "visited";
    }

  }
}
