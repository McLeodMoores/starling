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

import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Tests for {@link EquityIndexFutureOptionSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class EquityIndexFutureOptionSecurityTest extends AbstractBeanTestCase {
  private static final String EXCHANGE = "ABC";
  private static final Expiry EXPIRY = new Expiry(DateUtils.getUTCDate(2020, 3, 20));
  private static final ExerciseType EXERCISE_TYPE = ExerciseType.of("European");
  private static final ExternalId UNDERLYING = ExternalId.of("eid", "1");
  private static final double POINT_VALUE = 25;
  private static final boolean MARGINED = true;
  private static final Currency CCY = Currency.AUD;
  private static final double STRIKE = 100;
  private static final OptionType OPTION_TYPE = OptionType.CALL;

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(EquityIndexFutureOptionSecurity.class,
        Arrays.asList("optionType", "strike", "currency", "underlyingId", "exerciseType", "expiry", "pointValue", "exchange", "margined"),
        Arrays.asList(OPTION_TYPE, STRIKE, CCY, UNDERLYING, EXERCISE_TYPE, EXPIRY, POINT_VALUE, EXCHANGE, MARGINED),
        Arrays.asList(OptionType.PUT, STRIKE + 2, Currency.BRL, ExternalId.of("eid", "2"), ExerciseType.of("American"),
            new Expiry(DateUtils.getUTCDate(2020, 3, 21)), POINT_VALUE + 1, "DEF", !MARGINED));
  }

  /**
   * Tests that all fields are set in the constructor.
   */
  public void testConstructor() {
    EquityIndexFutureOptionSecurity option = new EquityIndexFutureOptionSecurity();
    assertNull(option.getCurrency());
    assertNull(option.getExchange());
    assertNull(option.getExerciseType());
    assertNull(option.getExpiry());
    assertNull(option.getOptionType());
    assertEquals(option.getPointValue(), 0.);
    assertEquals(option.getSecurityType(), EquityIndexFutureOptionSecurity.SECURITY_TYPE);
    assertEquals(option.getStrike(), 0.);
    assertNull(option.getUnderlyingId());
    assertFalse(option.isMargined());
    option = new EquityIndexFutureOptionSecurity(EXCHANGE, EXPIRY, EXERCISE_TYPE, UNDERLYING, POINT_VALUE, MARGINED, CCY, STRIKE, OPTION_TYPE);
    assertEquals(option.getCurrency(), CCY);
    assertEquals(option.getExchange(), EXCHANGE);
    assertEquals(option.getExerciseType(), EXERCISE_TYPE);
    assertEquals(option.getExpiry(), EXPIRY);
    assertEquals(option.getOptionType(), OPTION_TYPE);
    assertEquals(option.getPointValue(), POINT_VALUE);
    assertEquals(option.getSecurityType(), EquityIndexFutureOptionSecurity.SECURITY_TYPE);
    assertEquals(option.getStrike(), STRIKE);
    assertEquals(option.getUnderlyingId(), UNDERLYING);
    assertEquals(option.isMargined(), MARGINED);
  }

  /**
   * Tests that the accept() method points to the correct method in the visitor.
   */
  public void testAccept() {
    final EquityIndexFutureOptionSecurity option = new EquityIndexFutureOptionSecurity(EXCHANGE, EXPIRY, EXERCISE_TYPE, UNDERLYING, POINT_VALUE, MARGINED, CCY,
        STRIKE, OPTION_TYPE);
    assertEquals(option.accept(TestVisitor.INSTANCE), EXCHANGE);
  }

  private static class TestVisitor extends FinancialSecurityVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity security) {
      return security.getExchange();
    }
  }
}
