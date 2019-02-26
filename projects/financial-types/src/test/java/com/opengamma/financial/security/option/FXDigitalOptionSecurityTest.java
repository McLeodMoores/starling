/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.option;

import static org.testng.Assert.assertEquals;
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
 * Tests for {@link FXDigitalOptionSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class FXDigitalOptionSecurityTest extends AbstractBeanTestCase {
  private static final Currency PUT_CURRENCY = Currency.AUD;
  private static final Currency CALL_CURRENCY = Currency.BRL;
  private static final double PUT_AMOUNT = 1000;
  private static final double CALL_AMOUNT = 2000;
  private static final Currency PAYMENT_CURRENCY = Currency.CAD;
  private static final Expiry EXPIRY = new Expiry(DateUtils.getUTCDate(2020, 2, 2));
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2020, 2, 3);
  private static final boolean IS_LONG = true;

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(FXDigitalOptionSecurity.class,
        Arrays.asList("putCurrency", "callCurrency", "putAmount", "callAmount", "paymentCurrency", "expiry", "settlementDate", "longShort"),
        Arrays.asList(PUT_CURRENCY, CALL_CURRENCY, PUT_AMOUNT, CALL_AMOUNT, PAYMENT_CURRENCY, EXPIRY, SETTLEMENT_DATE, LongShort.LONG),
        Arrays.asList(CALL_CURRENCY, PAYMENT_CURRENCY, CALL_AMOUNT, PUT_AMOUNT, PUT_CURRENCY, new Expiry(DateUtils.getUTCDate(2022, 2, 2)),
            SETTLEMENT_DATE.plusDays(1), LongShort.SHORT));
  }

  /**
   * Tests that fields are set in the constructor.
   */
  public void testConstructor() {
    FXDigitalOptionSecurity security = new FXDigitalOptionSecurity();
    assertEquals(security.getSecurityType(), FXDigitalOptionSecurity.SECURITY_TYPE);
    assertEquals(security.getCallAmount(), 0.);
    assertNull(security.getCallCurrency());
    assertNull(security.getExpiry());
    assertEquals(security.getLongShort(), LongShort.LONG);
    assertNull(security.getPaymentCurrency());
    assertEquals(security.getPutAmount(), 0.);
    assertNull(security.getPutCurrency());
    assertNull(security.getSettlementDate());
    assertEquals(security.isLong(), IS_LONG);
    assertEquals(security.isShort(), !IS_LONG);
    security = new FXDigitalOptionSecurity(PUT_CURRENCY, CALL_CURRENCY, PUT_AMOUNT, CALL_AMOUNT, PAYMENT_CURRENCY, EXPIRY, SETTLEMENT_DATE, IS_LONG);
    assertEquals(security.getSecurityType(), FXDigitalOptionSecurity.SECURITY_TYPE);
    assertEquals(security.getCallAmount(), CALL_AMOUNT);
    assertEquals(security.getCallCurrency(), CALL_CURRENCY);
    assertEquals(security.getExpiry(), EXPIRY);
    assertEquals(security.getLongShort(), LongShort.LONG);
    assertEquals(security.getPaymentCurrency(), PAYMENT_CURRENCY);
    assertEquals(security.getPutAmount(), PUT_AMOUNT);
    assertEquals(security.getPutCurrency(), PUT_CURRENCY);
    assertEquals(security.getSettlementDate(), SETTLEMENT_DATE);
    assertEquals(security.isLong(), IS_LONG);
    assertEquals(security.isShort(), !IS_LONG);
  }

  /**
   * Tests that the accept() method points to the correct method in the visitor.
   */
  public void testAccept() {
    final FXDigitalOptionSecurity security = new FXDigitalOptionSecurity(PUT_CURRENCY, CALL_CURRENCY, PUT_AMOUNT, CALL_AMOUNT, PAYMENT_CURRENCY, EXPIRY,
        SETTLEMENT_DATE, IS_LONG);
    assertEquals(security.accept(TestVisitor.INSTANCE), FXDigitalOptionSecurity.SECURITY_TYPE);
  }

  /**
   *
   */
  private static final class TestVisitor extends FinancialSecurityVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
      return security.getSecurityType();
    }
  }
}
