/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.future;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Tests for {@link EquityFutureSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class EquityFutureSecurityTest extends AbstractBeanTestCase {
  private static final Expiry EXPIRY = new Expiry(DateUtils.getUTCDate(2020, 3, 15));
  private static final String TRADING_EXCHANGE = "ABC";
  private static final String SETTLEMENT_EXCHANGE = "DEF";
  private static final Currency CCY = Currency.AUD;
  private static final double UNIT_AMOUNT = 25;
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2020, 3, 17);
  private static final String CONTRACT_CATEGORY = "cat";
  private static final ExternalId UNDERLYING_ID = ExternalId.of("sec", "1");
  private static final EquityFutureSecurity FUTURE = new EquityFutureSecurity(EXPIRY, TRADING_EXCHANGE, SETTLEMENT_EXCHANGE, CCY, UNIT_AMOUNT, SETTLEMENT_DATE,
      UNDERLYING_ID, CONTRACT_CATEGORY);

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(EquityFutureSecurity.class,
        Arrays.asList("expiry", "tradingExchange", "settlementExchange", "currency", "unitAmount", "settlementDate", "underlyingId", "contractCategory"),
        Arrays.asList(EXPIRY, TRADING_EXCHANGE, SETTLEMENT_EXCHANGE, CCY, UNIT_AMOUNT, SETTLEMENT_DATE, UNDERLYING_ID, CONTRACT_CATEGORY),
        Arrays.asList(new Expiry(DateUtils.getUTCDate(2020, 6, 15)), SETTLEMENT_EXCHANGE, TRADING_EXCHANGE, Currency.BRL, UNIT_AMOUNT * 2,
            SETTLEMENT_DATE.plusDays(1), ExternalId.of("sec", "PQR"), "con"));
  }

  /**
   * Tests that fields are set in the constructor.
   */
  @Test
  public void testConstructor() {
    final EquityFutureSecurity future = new EquityFutureSecurity(EXPIRY, TRADING_EXCHANGE, SETTLEMENT_EXCHANGE, CCY, UNIT_AMOUNT, SETTLEMENT_DATE,
        UNDERLYING_ID, CONTRACT_CATEGORY);
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
    public String visitEquityFutureSecurity(final EquityFutureSecurity security) {
      return "visited";
    }

  }
}
