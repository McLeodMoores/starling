/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.option;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.LongShort;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Tests for {@link NonDeliverableFXDigitalOptionSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class NonDeliverableFXDigitalOptionSecurityTest extends AbstractBeanTestCase {
  private static final Currency PUT_CURRENCY = Currency.AUD;
  private static final Currency CALL_CURRENCY = Currency.BRL;
  private static final double PUT_AMOUNT = 1000;
  private static final double CALL_AMOUNT = 2000;
  private static final Currency PAYMENT_CURRENCY = Currency.CAD;
  private static final Expiry EXPIRY = new Expiry(DateUtils.getUTCDate(2020, 2, 2));
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2020, 2, 3);
  private static final boolean IS_LONG = true;
  private static final boolean DELIVERY_IN_CALL = true;

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(NonDeliverableFXDigitalOptionSecurity.class,
        Arrays.asList("putCurrency", "callCurrency", "putAmount", "callAmount", "paymentCurrency", "expiry", "settlementDate", "longShort",
            "deliverInCallCurrency"),
        Arrays.asList(PUT_CURRENCY, CALL_CURRENCY, PUT_AMOUNT, CALL_AMOUNT, PAYMENT_CURRENCY, EXPIRY, SETTLEMENT_DATE, LongShort.LONG, DELIVERY_IN_CALL),
        Arrays.asList(CALL_CURRENCY, PAYMENT_CURRENCY, CALL_AMOUNT, PUT_AMOUNT, PUT_CURRENCY, new Expiry(DateUtils.getUTCDate(2022, 2, 2)),
            SETTLEMENT_DATE.plusDays(1), LongShort.SHORT, !DELIVERY_IN_CALL));
  }

  /**
   * Tests that fields are set in the constructor.
   */
  public void testConstructor() {
    NonDeliverableFXDigitalOptionSecurity security = new NonDeliverableFXDigitalOptionSecurity();
    assertEquals(security.getSecurityType(), NonDeliverableFXDigitalOptionSecurity.SECURITY_TYPE);
    assertEquals(security.getCallAmount(), 0.);
    assertNull(security.getCallCurrency());
    assertNull(security.getExpiry());
    assertEquals(security.getLongShort(), LongShort.LONG);
    assertNull(security.getPaymentCurrency());
    assertEquals(security.getPutAmount(), 0.);
    assertNull(security.getPutCurrency());
    assertNull(security.getSettlementDate());
    assertFalse(security.isDeliverInCallCurrency());
    assertEquals(security.isLong(), IS_LONG);
    assertEquals(security.isShort(), !IS_LONG);
    security = new NonDeliverableFXDigitalOptionSecurity(PUT_CURRENCY, CALL_CURRENCY, PUT_AMOUNT, CALL_AMOUNT, PAYMENT_CURRENCY, EXPIRY, SETTLEMENT_DATE,
        IS_LONG, DELIVERY_IN_CALL);
    assertEquals(security.getSecurityType(), NonDeliverableFXDigitalOptionSecurity.SECURITY_TYPE);
    assertEquals(security.getCallAmount(), CALL_AMOUNT);
    assertEquals(security.getCallCurrency(), CALL_CURRENCY);
    assertEquals(security.getExpiry(), EXPIRY);
    assertEquals(security.getLongShort(), LongShort.LONG);
    assertEquals(security.getPaymentCurrency(), PAYMENT_CURRENCY);
    assertEquals(security.getPutAmount(), PUT_AMOUNT);
    assertEquals(security.getPutCurrency(), PUT_CURRENCY);
    assertEquals(security.getSettlementDate(), SETTLEMENT_DATE);
    assertEquals(security.isDeliverInCallCurrency(), true);
    assertEquals(security.isLong(), IS_LONG);
    assertEquals(security.isShort(), !IS_LONG);
  }

  /**
   * Tests that the accept() method points to the correct method in the visitor.
   */
  public void testAccept() {
    final NonDeliverableFXDigitalOptionSecurity security = new NonDeliverableFXDigitalOptionSecurity(PUT_CURRENCY, CALL_CURRENCY, PUT_AMOUNT, CALL_AMOUNT,
        PAYMENT_CURRENCY, EXPIRY, SETTLEMENT_DATE, IS_LONG, DELIVERY_IN_CALL);
    assertEquals(security.accept(TestVisitor.INSTANCE), NonDeliverableFXDigitalOptionSecurity.SECURITY_TYPE);
  }

  private static class TestVisitor extends FinancialSecurityVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
      return security.getSecurityType();
    }
  }
}
