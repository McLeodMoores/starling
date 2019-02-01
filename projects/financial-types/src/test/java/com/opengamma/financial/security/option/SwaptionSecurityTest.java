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

import com.opengamma.core.link.SecurityLink;
import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.LongShort;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Tests for {@link SwaptionSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class SwaptionSecurityTest extends AbstractBeanTestCase {
  private static final boolean PAYER = true;
  private static final ExternalId UNDERLYING_ID = ExternalId.of("eid", "1");
  private static final SecurityLink<FinancialSecurity> UNDERLYING_LINK = SecurityLink.resolvable(UNDERLYING_ID, FinancialSecurity.class);
  private static final boolean IS_LONG = false;
  private static final Expiry EXPIRY = new Expiry(DateUtils.getUTCDate(2020, 1, 1));
  private static final boolean CASH_SETTLED = true;
  private static final Currency CCY = Currency.AUD;
  private static final double NOTIONAL = 100000;
  private static final ExerciseType TYPE = new BermudanExerciseType();
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2022, 1, 1);

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(SwaptionSecurity.class,
        Arrays.asList("payer", "underlyingId", "longShort", "expiry", "cashSettled", "currency", "notional", "exerciseType", "settlementDate"),
        Arrays.asList(PAYER, UNDERLYING_ID, LongShort.SHORT, EXPIRY, CASH_SETTLED, CCY, NOTIONAL, TYPE, SETTLEMENT_DATE),
        Arrays.asList(!PAYER, ExternalId.of("eid", "2"), LongShort.LONG, new Expiry(DateUtils.getUTCDate(2021, 1, 1)), !CASH_SETTLED, Currency.BRL,
            NOTIONAL * 2, new EuropeanExerciseType(), SETTLEMENT_DATE.plusDays(1)));
  }

  /**
   * Tests that the fields are set.
   */
  public void testConstructor() {
    SwaptionSecurity security = new SwaptionSecurity();
    assertEquals(security.getSecurityType(), SwaptionSecurity.SECURITY_TYPE);
    assertNull(security.getCurrency());
    assertNull(security.getExerciseType());
    assertNull(security.getExpiry());
    assertEquals(security.getLongShort(), LongShort.LONG);
    assertNull(security.getNotional());
    assertNull(security.getSettlementDate());
    assertNull(security.getUnderlyingId());
    assertNull(security.getUnderlyingLink());
    security = new SwaptionSecurity(PAYER, UNDERLYING_ID, IS_LONG, EXPIRY, CASH_SETTLED, CCY);
    assertEquals(security.isCashSettled(), CASH_SETTLED);
    assertEquals(security.isLong(), IS_LONG);
    assertEquals(security.isPayer(), PAYER);
    assertEquals(security.getCurrency(), CCY);
    assertEquals(security.getExerciseType(), new EuropeanExerciseType());
    assertEquals(security.getExpiry(), EXPIRY);
    assertEquals(security.getLongShort(), LongShort.SHORT);
    assertNull(security.getNotional());
    assertNull(security.getSettlementDate());
    assertEquals(security.getUnderlyingId(), UNDERLYING_ID);
    assertEquals(security.getUnderlyingLink(), UNDERLYING_LINK);
    security = new SwaptionSecurity(PAYER, UNDERLYING_ID, IS_LONG, EXPIRY, CASH_SETTLED, CCY, NOTIONAL, TYPE, SETTLEMENT_DATE);
    assertEquals(security.isCashSettled(), CASH_SETTLED);
    assertEquals(security.isLong(), IS_LONG);
    assertEquals(security.isPayer(), PAYER);
    assertEquals(security.getCurrency(), CCY);
    assertEquals(security.getExerciseType(), TYPE);
    assertEquals(security.getExpiry(), EXPIRY);
    assertEquals(security.getLongShort(), LongShort.SHORT);
    assertEquals(security.getNotional(), NOTIONAL);
    assertEquals(security.getSettlementDate(), SETTLEMENT_DATE);
    assertEquals(security.getUnderlyingId(), UNDERLYING_ID);
    assertEquals(security.getUnderlyingLink(), UNDERLYING_LINK);
    security = new SwaptionSecurity(PAYER, UNDERLYING_LINK, IS_LONG, EXPIRY, CASH_SETTLED, CCY, NOTIONAL, TYPE, SETTLEMENT_DATE);
    assertEquals(security.isCashSettled(), CASH_SETTLED);
    assertEquals(security.isLong(), IS_LONG);
    assertEquals(security.isPayer(), PAYER);
    assertEquals(security.getCurrency(), CCY);
    assertEquals(security.getExerciseType(), TYPE);
    assertEquals(security.getExpiry(), EXPIRY);
    assertEquals(security.getLongShort(), LongShort.SHORT);
    assertEquals(security.getNotional(), NOTIONAL);
    assertEquals(security.getSettlementDate(), SETTLEMENT_DATE);
    assertEquals(security.getUnderlyingId(), UNDERLYING_ID);
    assertEquals(security.getUnderlyingLink(), UNDERLYING_LINK);
  }

  /**
   * Tests that the accept() method points to the correct method in the visitor.
   */
  public void testAccept() {
    final SwaptionSecurity swaption = new SwaptionSecurity(PAYER, UNDERLYING_LINK, IS_LONG, EXPIRY, CASH_SETTLED, CCY, NOTIONAL, TYPE, SETTLEMENT_DATE);
    assertEquals(swaption.accept(TestVisitor.INSTANCE), SwaptionSecurity.SECURITY_TYPE);
  }

  private static class TestVisitor extends FinancialSecurityVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitSwaptionSecurity(final SwaptionSecurity security) {
      return security.getSecurityType();
    }
  }
}
