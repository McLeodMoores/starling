/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.option;

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
 * Tests for {@link EquityBarrierOptionSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class EquityBarrierOptionSecurityTest extends AbstractBeanTestCase {
  private static final OptionType OPTION_TYPE = OptionType.CALL;
  private static final double STRIKE = 100;
  private static final Currency CCY = Currency.AUD;
  private static final ExternalId UNDERLYING = ExternalId.of("eid", "1");
  private static final ExerciseType EXERCISE_TYPE = ExerciseType.of("European");
  private static final Expiry EXPIRY = new Expiry(DateUtils.getUTCDate(2020, 3, 20));
  private static final double POINT_VALUE = 25;
  private static final String EXCHANGE = "ABC";
  private static final BarrierType BARRIER_TYPE = BarrierType.DOWN;
  private static final BarrierDirection BARRIER_DIRECTION = BarrierDirection.KNOCK_OUT;
  private static final MonitoringType MONITORING_TYPE = MonitoringType.DISCRETE;
  private static final SamplingFrequency FREQUENCY = SamplingFrequency.CONTINUOUS;
  private static final double BARRIER_LEVEL = 80;

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(EquityBarrierOptionSecurity.class,
        Arrays.asList("optionType", "strike", "currency", "underlyingId", "exerciseType", "expiry", "pointValue", "exchange", "barrierType", "barrierDirection",
            "monitoringType", "samplingFrequency", "barrierLevel"),
        Arrays.asList(OPTION_TYPE, STRIKE, CCY, UNDERLYING, EXERCISE_TYPE, EXPIRY, POINT_VALUE, EXCHANGE, BARRIER_TYPE, BARRIER_DIRECTION, MONITORING_TYPE,
            FREQUENCY, BARRIER_LEVEL),
        Arrays.asList(OptionType.PUT, STRIKE + 2, Currency.BRL, ExternalId.of("eid", "2"), ExerciseType.of("American"),
            new Expiry(DateUtils.getUTCDate(2020, 3, 21)), POINT_VALUE + 1, "DEF", BarrierType.UP, BarrierDirection.KNOCK_IN, MonitoringType.CONTINUOUS,
            SamplingFrequency.DAILY_CLOSE, BARRIER_LEVEL + 2));
  }

  /**
   * Tests that all fields are set in the constructor.
   */
  public void testConstructor() {
    EquityBarrierOptionSecurity option = new EquityBarrierOptionSecurity();
    assertNull(option.getBarrierDirection());
    assertEquals(option.getBarrierLevel(), 0.);
    assertNull(option.getBarrierType());
    assertNull(option.getCurrency());
    assertNull(option.getExchange());
    assertNull(option.getExerciseType());
    assertNull(option.getExpiry());
    assertNull(option.getMonitoringType());
    assertNull(option.getOptionType());
    assertEquals(option.getPointValue(), 0.);
    assertNull(option.getSamplingFrequency());
    assertEquals(option.getSecurityType(), EquityBarrierOptionSecurity.SECURITY_TYPE);
    assertEquals(option.getStrike(), 0.);
    assertNull(option.getUnderlyingId());
    option = new EquityBarrierOptionSecurity(OPTION_TYPE, STRIKE, CCY, UNDERLYING, EXERCISE_TYPE, EXPIRY, POINT_VALUE, EXCHANGE, BARRIER_TYPE,
        BARRIER_DIRECTION, MONITORING_TYPE, FREQUENCY, BARRIER_LEVEL);
    assertEquals(option.getBarrierDirection(), BARRIER_DIRECTION);
    assertEquals(option.getBarrierLevel(), BARRIER_LEVEL);
    assertEquals(option.getBarrierType(), BARRIER_TYPE);
    assertEquals(option.getCurrency(), CCY);
    assertEquals(option.getExchange(), EXCHANGE);
    assertEquals(option.getExerciseType(), EXERCISE_TYPE);
    assertEquals(option.getExpiry(), EXPIRY);
    assertEquals(option.getMonitoringType(), MONITORING_TYPE);
    assertEquals(option.getOptionType(), OPTION_TYPE);
    assertEquals(option.getPointValue(), POINT_VALUE);
    assertEquals(option.getSamplingFrequency(), FREQUENCY);
    assertEquals(option.getSecurityType(), EquityBarrierOptionSecurity.SECURITY_TYPE);
    assertEquals(option.getStrike(), STRIKE);
    assertEquals(option.getUnderlyingId(), UNDERLYING);
  }

  /**
   * Tests that the accept() method points to the correct method in the visitor.
   */
  public void testAccept() {
    final EquityBarrierOptionSecurity option = new EquityBarrierOptionSecurity(OPTION_TYPE, STRIKE, CCY, UNDERLYING, EXERCISE_TYPE, EXPIRY, POINT_VALUE,
        EXCHANGE, BARRIER_TYPE, BARRIER_DIRECTION, MONITORING_TYPE, FREQUENCY, BARRIER_LEVEL);
    assertEquals(option.accept(TestVisitor.INSTANCE), EXCHANGE);
  }

  private static class TestVisitor extends FinancialSecurityVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
      return security.getExchange();
    }
  }
}
