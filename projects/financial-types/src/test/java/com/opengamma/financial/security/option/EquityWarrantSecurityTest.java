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
 * Tests for {@link EquityWarrantSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class EquityWarrantSecurityTest extends AbstractBeanTestCase {
  private static final OptionType OPTION_TYPE = OptionType.CALL;
  private static final double STRIKE = 100;
  private static final Currency CCY = Currency.AUD;
  private static final ExternalId UNDERLYING = ExternalId.of("eid", "1");
  private static final ExerciseType EXERCISE_TYPE = ExerciseType.of("European");
  private static final Expiry EXPIRY = new Expiry(DateUtils.getUTCDate(2020, 3, 20));
  private static final double UNDERLYING_SHARES = 25;
  private static final String EXCHANGE = "ABC";

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(EquityWarrantSecurity.class,
        Arrays.asList("optionType", "strike", "currency", "underlyingId", "exerciseType", "expiry", "underlyingShares", "exchange"),
        Arrays.asList(OPTION_TYPE, STRIKE, CCY, UNDERLYING, EXERCISE_TYPE, EXPIRY, UNDERLYING_SHARES, EXCHANGE),
        Arrays.asList(OptionType.PUT, STRIKE + 2, Currency.BRL, ExternalId.of("eid", "2"), ExerciseType.of("American"),
            new Expiry(DateUtils.getUTCDate(2020, 3, 21)), UNDERLYING_SHARES + 1, "DEF"));
  }

  /**
   * Tests that all fields are set in the constructor.
   */
  public void testConstructor() {
    EquityWarrantSecurity option = new EquityWarrantSecurity();
    assertNull(option.getCurrency());
    assertNull(option.getExchange());
    assertNull(option.getExerciseType());
    assertNull(option.getExpiry());
    assertNull(option.getOptionType());
    assertEquals(option.getSecurityType(), EquityWarrantSecurity.SECURITY_TYPE);
    assertEquals(option.getStrike(), 0.);
    assertNull(option.getUnderlyingId());
    assertEquals(option.getUnderlyingShares(), 0.);
    option = new EquityWarrantSecurity(OPTION_TYPE, STRIKE, CCY, UNDERLYING, EXERCISE_TYPE, EXPIRY, UNDERLYING_SHARES, EXCHANGE);
    assertEquals(option.getCurrency(), CCY);
    assertEquals(option.getExchange(), EXCHANGE);
    assertEquals(option.getExerciseType(), EXERCISE_TYPE);
    assertEquals(option.getExpiry(), EXPIRY);
    assertEquals(option.getOptionType(), OPTION_TYPE);
    assertEquals(option.getSecurityType(), EquityWarrantSecurity.SECURITY_TYPE);
    assertEquals(option.getStrike(), STRIKE);
    assertEquals(option.getUnderlyingId(), UNDERLYING);
    assertEquals(option.getUnderlyingShares(), UNDERLYING_SHARES);
  }

  /**
   * Tests that the accept() method points to the correct method in the visitor.
   */
  public void testAccept() {
    final EquityWarrantSecurity option = new EquityWarrantSecurity(OPTION_TYPE, STRIKE, CCY, UNDERLYING, EXERCISE_TYPE, EXPIRY, UNDERLYING_SHARES, EXCHANGE);
    assertEquals(option.accept(TestVisitor.INSTANCE), EXCHANGE);
  }

  private static class TestVisitor extends FinancialSecurityVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitEquityWarrantSecurity(final EquityWarrantSecurity security) {
      return security.getExchange();
    }
  }
}
