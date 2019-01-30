/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.future;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.joda.beans.Bean;
import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Tests for {@link BondFutureSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class BondFutureSecurityTest extends AbstractBeanTestCase {
  private static final Expiry EXPIRY = new Expiry(DateUtils.getUTCDate(2020, 3, 15));
  private static final String TRADING_EXCHANGE = "ABC";
  private static final String SETTLEMENT_EXCHANGE = "DEF";
  private static final Currency CCY = Currency.AUD;
  private static final double UNIT_AMOUNT = 25;
  private static final List<? extends BondFutureDeliverable> BASKET = Arrays.asList(new BondFutureDeliverable(ExternalIdBundle.of("sec", "1"), 0.95),
      new BondFutureDeliverable(ExternalIdBundle.of("sec", "2"), 1.23), new BondFutureDeliverable(ExternalIdBundle.of("sec", "3"), 1.01),
      new BondFutureDeliverable(ExternalIdBundle.of("sec", "4"), 0.987));
  private static final ZonedDateTime FIRST_DELIVERY_DATE = DateUtils.getUTCDate(2020, 3, 15);
  private static final ZonedDateTime LAST_DELIVERY_DATE = DateUtils.getUTCDate(2020, 4, 15);
  private static final String CONTRACT_CATEGORY = "cat";
  private static final BondFutureSecurity FUTURE = new BondFutureSecurity(EXPIRY, TRADING_EXCHANGE, SETTLEMENT_EXCHANGE, CCY, UNIT_AMOUNT, BASKET,
      FIRST_DELIVERY_DATE, LAST_DELIVERY_DATE, CONTRACT_CATEGORY);

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(BondFutureSecurity.class,
        Arrays.asList("expiry", "tradingExchange", "settlementExchange", "currency", "unitAmount", "basket", "firstDeliveryDate", "lastDeliveryDate",
            "contractCategory"),
        Arrays.asList(EXPIRY, TRADING_EXCHANGE, SETTLEMENT_EXCHANGE, CCY, UNIT_AMOUNT, BASKET, FIRST_DELIVERY_DATE, LAST_DELIVERY_DATE, CONTRACT_CATEGORY),
        Arrays.asList(new Expiry(DateUtils.getUTCDate(2020, 6, 15)), SETTLEMENT_EXCHANGE, TRADING_EXCHANGE, Currency.BRL, UNIT_AMOUNT * 2,
            BASKET.subList(0, 1), LAST_DELIVERY_DATE, FIRST_DELIVERY_DATE, "con"));
  }

  /**
   * Tests that fields are set in the constructor.
   */
  @Test
  public void testConstructor() {
    final BondFutureSecurity future = new BondFutureSecurity(EXPIRY, TRADING_EXCHANGE, SETTLEMENT_EXCHANGE, CCY, UNIT_AMOUNT, BASKET, FIRST_DELIVERY_DATE,
        LAST_DELIVERY_DATE, CONTRACT_CATEGORY);
    assertEquals(future.getExpiry(), EXPIRY);
    assertEquals(future.getTradingExchange(), TRADING_EXCHANGE);
    assertEquals(future.getSettlementExchange(), SETTLEMENT_EXCHANGE);
    assertEquals(future.getCurrency(), CCY);
    assertEquals(future.getUnitAmount(), UNIT_AMOUNT);
    assertEquals(future.getBasket(), BASKET);
    assertEquals(future.getFirstDeliveryDate(), FIRST_DELIVERY_DATE);
    assertEquals(future.getLastDeliveryDate(), LAST_DELIVERY_DATE);
    assertEquals(future.getContractCategory(), CONTRACT_CATEGORY);
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

  private static class TestVisitor extends FinancialSecurityVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitBondFutureSecurity(final BondFutureSecurity security) {
      return "visited";
    }

  }
}
