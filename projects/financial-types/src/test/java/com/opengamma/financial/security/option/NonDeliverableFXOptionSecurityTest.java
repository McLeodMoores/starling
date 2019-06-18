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
 * Tests for {@link NonDeliverableFXOptionSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class NonDeliverableFXOptionSecurityTest extends AbstractBeanTestCase {
  private static final Currency PUT_CURRENCY = Currency.AUD;
  private static final Currency CALL_CURRENCY = Currency.BRL;
  private static final double PUT_AMOUNT = 1000;
  private static final double CALL_AMOUNT = 2000;
  private static final Expiry EXPIRY = new Expiry(DateUtils.getUTCDate(2020, 2, 2));
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2020, 2, 3);
  private static final boolean IS_LONG = true;
  private static final ExerciseType TYPE = new AmericanExerciseType();
  private static final boolean DELIVER_IN_CALL_CURRENCY = true;

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(NonDeliverableFXOptionSecurity.class,
        Arrays.asList("putCurrency", "callCurrency", "putAmount", "callAmount", "expiry", "settlementDate", "longShort", "exerciseType",
            "deliveryInCallCurrency"),
        Arrays.asList(PUT_CURRENCY, CALL_CURRENCY, PUT_AMOUNT, CALL_AMOUNT, EXPIRY, SETTLEMENT_DATE, LongShort.LONG, TYPE, DELIVER_IN_CALL_CURRENCY),
        Arrays.asList(CALL_CURRENCY, PUT_CURRENCY, CALL_AMOUNT, PUT_AMOUNT, new Expiry(DateUtils.getUTCDate(2022, 2, 2)), SETTLEMENT_DATE.plusDays(1),
            LongShort.SHORT, new EuropeanExerciseType(), !DELIVER_IN_CALL_CURRENCY));
  }

  /**
   * Tests that fields are set in the constructor.
   */
  public void testConstructor() {
    NonDeliverableFXOptionSecurity security = new NonDeliverableFXOptionSecurity();
    assertEquals(security.getSecurityType(), NonDeliverableFXOptionSecurity.SECURITY_TYPE);
    assertEquals(security.getCallAmount(), 0.);
    assertNull(security.getCallCurrency());
    assertNull(security.getExerciseType());
    assertNull(security.getExpiry());
    assertEquals(security.getLongShort(), LongShort.LONG);
    assertEquals(security.getPutAmount(), 0.);
    assertNull(security.getPutCurrency());
    assertNull(security.getSettlementDate());
    assertEquals(security.isLong(), IS_LONG);
    assertEquals(security.isShort(), !IS_LONG);
    security = new NonDeliverableFXOptionSecurity(PUT_CURRENCY, CALL_CURRENCY, PUT_AMOUNT, CALL_AMOUNT, EXPIRY, SETTLEMENT_DATE, IS_LONG, TYPE,
        DELIVER_IN_CALL_CURRENCY);
    assertEquals(security.getSecurityType(), NonDeliverableFXOptionSecurity.SECURITY_TYPE);
    assertEquals(security.getCallAmount(), CALL_AMOUNT);
    assertEquals(security.getCallCurrency(), CALL_CURRENCY);
    assertEquals(security.getExerciseType(), TYPE);
    assertEquals(security.getExpiry(), EXPIRY);
    assertEquals(security.getLongShort(), LongShort.LONG);
    assertEquals(security.getPutAmount(), PUT_AMOUNT);
    assertEquals(security.getPutCurrency(), PUT_CURRENCY);
    assertEquals(security.getSettlementDate(), SETTLEMENT_DATE);
    assertEquals(security.isLong(), IS_LONG);
    assertEquals(security.isShort(), !IS_LONG);
  }

  /**
   * Tests the delivery currency logic.
   */
  public void testDeliveryCurrency() {
    final NonDeliverableFXOptionSecurity security = new NonDeliverableFXOptionSecurity(PUT_CURRENCY, CALL_CURRENCY, PUT_AMOUNT, CALL_AMOUNT, EXPIRY,
        SETTLEMENT_DATE, IS_LONG, TYPE, true);
    assertEquals(security.getDeliveryCurrency(), CALL_CURRENCY);
    security.setDeliveryInCallCurrency(false);
    assertEquals(security.getDeliveryCurrency(), PUT_CURRENCY);
  }

  /**
   * Tests that the accept() method points to the correct method in the visitor.
   */
  public void testAccept() {
    final NonDeliverableFXOptionSecurity security = new NonDeliverableFXOptionSecurity(PUT_CURRENCY, CALL_CURRENCY, PUT_AMOUNT, CALL_AMOUNT, EXPIRY,
        SETTLEMENT_DATE, IS_LONG, TYPE, DELIVER_IN_CALL_CURRENCY);
    assertEquals(security.accept(TestVisitor.INSTANCE), NonDeliverableFXOptionSecurity.SECURITY_TYPE);
  }

  /**
   *
   */
  private static final class TestVisitor extends FinancialSecurityVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
      return security.getSecurityType();
    }
  }
}
