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
 * Tests for {@link FXBarrierOptionSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class FXBarrierOptionSecurityTest extends AbstractBeanTestCase {
  private static final Currency PUT_CURRENCY = Currency.AUD;
  private static final Currency CALL_CURRENCY = Currency.BRL;
  private static final double PUT_AMOUNT = 1000;
  private static final double CALL_AMOUNT = 2000;
  private static final Expiry EXPIRY = new Expiry(DateUtils.getUTCDate(2020, 2, 2));
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2020, 2, 3);
  private static final BarrierType BARRIER_TYPE = BarrierType.UP;
  private static final BarrierDirection BARRIER_DIRECTION = BarrierDirection.KNOCK_IN;
  private static final MonitoringType MONITORING_TYPE = MonitoringType.DISCRETE;
  private static final SamplingFrequency SAMPLING_FREQUENCY = SamplingFrequency.DAILY_CLOSE;
  private static final double BARRIER_LEVEL = 2.1;
  private static final boolean IS_LONG = true;

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(FXBarrierOptionSecurity.class,
        Arrays.asList("putCurrency", "callCurrency", "putAmount", "callAmount", "expiry", "settlementDate", "barrierType", "barrierDirection", "monitoringType",
            "samplingFrequency", "barrierLevel", "longShort"),
        Arrays.asList(PUT_CURRENCY, CALL_CURRENCY, PUT_AMOUNT, CALL_AMOUNT, EXPIRY, SETTLEMENT_DATE, BARRIER_TYPE, BARRIER_DIRECTION, MONITORING_TYPE,
            SAMPLING_FREQUENCY, BARRIER_LEVEL, LongShort.LONG),
        Arrays.asList(CALL_CURRENCY, PUT_CURRENCY, CALL_AMOUNT, PUT_AMOUNT, new Expiry(DateUtils.getUTCDate(2022, 2, 2)), SETTLEMENT_DATE.plusDays(1),
            BarrierType.DOWN, BarrierDirection.KNOCK_OUT, MonitoringType.CONTINUOUS, SamplingFrequency.CONTINUOUS, BARRIER_LEVEL * 1.1, LongShort.SHORT));
  }

  /**
   * Tests that fields are set in the constructor.
   */
  public void testConstructor() {
    FXBarrierOptionSecurity security = new FXBarrierOptionSecurity();
    assertEquals(security.getSecurityType(), FXBarrierOptionSecurity.SECURITY_TYPE);
    assertNull(security.getBarrierDirection());
    assertEquals(security.getBarrierLevel(), 0.);
    assertNull(security.getBarrierType());
    assertEquals(security.getCallAmount(), 0.);
    assertNull(security.getCallCurrency());
    assertNull(security.getExpiry());
    assertEquals(security.getLongShort(), LongShort.LONG);
    assertNull(security.getMonitoringType());
    assertEquals(security.getPutAmount(), 0.);
    assertNull(security.getPutCurrency());
    assertNull(security.getSamplingFrequency());
    assertNull(security.getSettlementDate());
    assertEquals(security.isLong(), IS_LONG);
    assertEquals(security.isShort(), !IS_LONG);
    security = new FXBarrierOptionSecurity(PUT_CURRENCY, CALL_CURRENCY, PUT_AMOUNT, CALL_AMOUNT, EXPIRY, SETTLEMENT_DATE, BARRIER_TYPE, BARRIER_DIRECTION,
        MONITORING_TYPE, SAMPLING_FREQUENCY, BARRIER_LEVEL, IS_LONG);
    assertEquals(security.getSecurityType(), FXBarrierOptionSecurity.SECURITY_TYPE);
    assertEquals(security.getBarrierDirection(), BARRIER_DIRECTION);
    assertEquals(security.getBarrierLevel(), BARRIER_LEVEL);
    assertEquals(security.getBarrierType(), BARRIER_TYPE);
    assertEquals(security.getCallAmount(), CALL_AMOUNT);
    assertEquals(security.getCallCurrency(), CALL_CURRENCY);
    assertEquals(security.getExpiry(), EXPIRY);
    assertEquals(security.getLongShort(), LongShort.LONG);
    assertEquals(security.getMonitoringType(), MONITORING_TYPE);
    assertEquals(security.getPutAmount(), PUT_AMOUNT);
    assertEquals(security.getPutCurrency(), PUT_CURRENCY);
    assertEquals(security.getSamplingFrequency(), SAMPLING_FREQUENCY);
    assertEquals(security.getSettlementDate(), SETTLEMENT_DATE);
    assertEquals(security.isLong(), IS_LONG);
    assertEquals(security.isShort(), !IS_LONG);
  }

  /**
   * Tests that the accept() method points to the correct method in the visitor.
   */
  public void testAccept() {
    final FXBarrierOptionSecurity security = new FXBarrierOptionSecurity(PUT_CURRENCY, CALL_CURRENCY, PUT_AMOUNT, CALL_AMOUNT, EXPIRY, SETTLEMENT_DATE,
        BARRIER_TYPE, BARRIER_DIRECTION, MONITORING_TYPE, SAMPLING_FREQUENCY, BARRIER_LEVEL, IS_LONG);
    assertEquals(security.accept(TestVisitor.INSTANCE), FXBarrierOptionSecurity.SECURITY_TYPE);
  }

  /**
   *
   */
  private static final class TestVisitor extends FinancialSecurityVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
      return security.getSecurityType();
    }
  }
}
