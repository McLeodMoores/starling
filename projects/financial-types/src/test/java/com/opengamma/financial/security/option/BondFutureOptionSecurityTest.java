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
 * Tests for {@link BondFutureOptionSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class BondFutureOptionSecurityTest extends AbstractBeanTestCase {
  private static final String TRADING_EXCHANGE = "abc";
  private static final String SETTLEMENT_EXCHANGE = "def";
  private static final Expiry EXPIRY = new Expiry(DateUtils.getUTCDate(2020, 2, 2));
  private static final ExerciseType TYPE = new AmericanExerciseType();
  private static final ExternalId UNDERLYING_ID = ExternalId.of("eid", "1");
  private static final double POINT_VALUE = 25;
  private static final boolean IS_MARGINED = true;
  private static final Currency CCY = Currency.AUD;
  private static final double STRIKE = 100;
  private static final OptionType OPTION_TYPE = OptionType.CALL;

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(BondFutureOptionSecurity.class,
        Arrays.asList("tradingExchange", "settlementExchange", "expiry", "exerciseType", "underlyingId", "pointValue", "margined", "currency", "strike",
            "optionType"),
        Arrays.asList(TRADING_EXCHANGE, SETTLEMENT_EXCHANGE, EXPIRY, TYPE, UNDERLYING_ID, POINT_VALUE, IS_MARGINED, CCY, STRIKE, OPTION_TYPE),
        Arrays.asList(SETTLEMENT_EXCHANGE, TRADING_EXCHANGE, new Expiry(DateUtils.getUTCDate(2022, 2, 2)), new EuropeanExerciseType(),
            ExternalId.of("eid", "2"), POINT_VALUE * 2, !IS_MARGINED, Currency.BRL, STRIKE * 2, OptionType.PUT));
  }

  /**
   * Tests that fields are set in the constructor.
   */
  public void testConstructor() {
    BondFutureOptionSecurity security = new BondFutureOptionSecurity();
    assertEquals(security.getSecurityType(), BondFutureOptionSecurity.SECURITY_TYPE);
    assertNull(security.getCurrency());
    assertNull(security.getExerciseType());
    assertNull(security.getExpiry());
    assertNull(security.getOptionType());
    assertEquals(security.getPointValue(), 0.);
    assertNull(security.getSettlementExchange());
    assertEquals(security.getStrike(), 0.);
    assertNull(security.getTradingExchange());
    assertNull(security.getUnderlyingId());
    assertFalse(security.isMargined());
    security = new BondFutureOptionSecurity(TRADING_EXCHANGE, SETTLEMENT_EXCHANGE, EXPIRY, TYPE, UNDERLYING_ID, POINT_VALUE, IS_MARGINED, CCY, STRIKE,
        OPTION_TYPE);
    assertEquals(security.getSecurityType(), BondFutureOptionSecurity.SECURITY_TYPE);
    assertEquals(security.getCurrency(), CCY);
    assertEquals(security.getExerciseType(), TYPE);
    assertEquals(security.getExpiry(), EXPIRY);
    assertEquals(security.getOptionType(), OPTION_TYPE);
    assertEquals(security.getPointValue(), POINT_VALUE);
    assertEquals(security.getSettlementExchange(), SETTLEMENT_EXCHANGE);
    assertEquals(security.getStrike(), STRIKE);
    assertEquals(security.getTradingExchange(), TRADING_EXCHANGE);
    assertEquals(security.getUnderlyingId(), UNDERLYING_ID);
    assertEquals(security.isMargined(), IS_MARGINED);
  }

  /**
   * Tests that the accept() method points to the correct method in the visitor.
   */
  public void testAccept() {
    final BondFutureOptionSecurity security = new BondFutureOptionSecurity(TRADING_EXCHANGE, SETTLEMENT_EXCHANGE, EXPIRY, TYPE, UNDERLYING_ID, POINT_VALUE,
        IS_MARGINED, CCY, STRIKE, OPTION_TYPE);
    assertEquals(security.accept(TestVisitor.INSTANCE), BondFutureOptionSecurity.SECURITY_TYPE);
  }

  private static class TestVisitor extends FinancialSecurityVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitBondFutureOptionSecurity(final BondFutureOptionSecurity security) {
      return security.getSecurityType();
    }
  }
}
