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
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests for {@link CreditDefaultSwapOptionSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class CreditDefaultSwapOptionSecurityTest extends AbstractBeanTestCase {
  private static final boolean IS_BUY = true;
  private static final ExternalId BUYER = ExternalId.of("eid", "abc");
  private static final ExternalId SELLER = ExternalId.of("eid", "def");
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2020, 1, 1);
  private static final ZonedDateTime MATURITY_DATE = DateUtils.getUTCDate(2022, 1, 1);
  private static final Currency CCY = Currency.AUD;
  private static final double NOTIONAL = 1000000;
  private static final double STRIKE = 0.75;
  private static final boolean IS_KNOCK_OUT = true;
  private static final boolean IS_PAYER = true;
  private static final ExerciseType TYPE = new AmericanExerciseType();
  private static final ExternalId UNDERLYING = ExternalId.of("eid", "1");

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(CreditDefaultSwapOptionSecurity.class,
        Arrays.asList("buy", "protectionBuyer", "protectionSeller", "startDate", "maturityDate", "currency", "notional", "strike", "knockOut", "payer",
            "exerciseType", "underlyingId"),
        Arrays.asList(IS_BUY, BUYER, SELLER, START_DATE, MATURITY_DATE, CCY, NOTIONAL, STRIKE, IS_KNOCK_OUT, IS_PAYER, TYPE, UNDERLYING),
        Arrays.asList(!IS_BUY, SELLER, UNDERLYING, MATURITY_DATE, START_DATE, Currency.BRL, NOTIONAL * 2, STRIKE * 2, !IS_KNOCK_OUT, !IS_PAYER,
            new EuropeanExerciseType(), BUYER));
  }

  /**
   * Tests that fields are set in the constructor.
   */
  public void testConstructor() {
    CreditDefaultSwapOptionSecurity security = new CreditDefaultSwapOptionSecurity();
    assertEquals(security.getSecurityType(), CreditDefaultSwapOptionSecurity.SECURITY_TYPE);
    assertNull(security.getCurrency());
    assertNull(security.getExerciseType());
    assertNull(security.getMaturityDate());
    assertNull(security.getNotional());
    assertNull(security.getProtectionBuyer());
    assertNull(security.getProtectionSeller());
    assertNull(security.getStartDate());
    assertNull(security.getStrike());
    assertNull(security.getUnderlyingId());
    assertFalse(security.isBuy());
    assertFalse(security.isKnockOut());
    assertFalse(security.isPayer());
    security = new CreditDefaultSwapOptionSecurity(IS_BUY, BUYER, SELLER, START_DATE, MATURITY_DATE, CCY, NOTIONAL, STRIKE, IS_KNOCK_OUT, IS_PAYER, TYPE,
        UNDERLYING);
    assertEquals(security.getSecurityType(), CreditDefaultSwapOptionSecurity.SECURITY_TYPE);
    assertEquals(security.getCurrency(), CCY);
    assertEquals(security.getExerciseType(), TYPE);
    assertEquals(security.getMaturityDate(), MATURITY_DATE);
    assertEquals(security.getNotional(), NOTIONAL);
    assertEquals(security.getProtectionBuyer(), BUYER);
    assertEquals(security.getProtectionSeller(), SELLER);
    assertEquals(security.getStartDate(), START_DATE);
    assertEquals(security.getStrike(), STRIKE);
    assertEquals(security.getUnderlyingId(), UNDERLYING);
    assertEquals(security.isBuy(), IS_BUY);
    assertEquals(security.isKnockOut(), IS_KNOCK_OUT);
    assertEquals(security.isPayer(), IS_PAYER);
  }

  /**
   * Tests that the accept() method points to the correct method in the visitor.
   */
  public void testAccept() {
    final CreditDefaultSwapOptionSecurity security = new CreditDefaultSwapOptionSecurity(IS_BUY, BUYER, SELLER, START_DATE, MATURITY_DATE, CCY, NOTIONAL,
        STRIKE, IS_KNOCK_OUT, IS_PAYER, TYPE, UNDERLYING);
    assertEquals(security.accept(TestVisitor.INSTANCE), CreditDefaultSwapOptionSecurity.SECURITY_TYPE);
  }

  private static class TestVisitor extends FinancialSecurityVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitCreditDefaultSwapOptionSecurity(final CreditDefaultSwapOptionSecurity security) {
      return security.getSecurityType();
    }
  }
}
