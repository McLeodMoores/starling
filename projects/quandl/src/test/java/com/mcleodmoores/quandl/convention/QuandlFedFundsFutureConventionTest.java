/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.convention;

import org.testng.annotations.Test;

import com.mcleodmoores.quandl.QuandlConstants;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link QuandlFedFundsFutureConvention}.
 */
@Test(groups = TestGroup.UNIT)
public class QuandlFedFundsFutureConventionTest extends QuandlFutureConventionTestBase {
  /** The name of the convention */
  private static final String NAME = "Name";
  /** The external ids for the convention */
  private static final ExternalIdBundle IDS = ExternalIdBundle.of(QuandlConstants.ofCode("ABC/TEST"), ExternalId.of("CONVENTION", "TEST"));
  /** The trade time */
  private static final String TRADE_TIME = "16:00";
  /** The time zone string */
  private static final String TIME_ZONE = "London";
  /** The unit amount */
  private static final double UNIT_AMOUNT = 25000;
  /** The underlying convention id */
  private static final ExternalId UNDERLYING_CONVENTION = QuandlConstants.ofCode("ABC/LIBOR");
  /** The trading exchange */
  private static final String TRADING_EXCHANGE = "ABC1";
  /** The settlement exchange */
  private static final String SETTLEMENT_EXCHANGE = "ABC2";

  @Override
  protected String getName() {
    return NAME;
  }

  @Override
  protected ExternalIdBundle getIds() {
    return IDS;
  }

  @Override
  protected String getTradeTime() {
    return TRADE_TIME;
  }

  @Override
  protected String getTimeZone() {
    return TIME_ZONE;
  }

  @Override
  protected double getUnitAmount() {
    return UNIT_AMOUNT;
  }

  @Override
  protected ExternalId getUnderlyingId() {
    return UNDERLYING_CONVENTION;
  }

  @Override
  protected String getTradingExchange() {
    return TRADING_EXCHANGE;
  }

  @Override
  protected String getSettlementExchange() {
    return SETTLEMENT_EXCHANGE;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected QuandlFedFundsFutureConvention getEmptyInstance() {
    return new QuandlFedFundsFutureConvention();
  }

  @Override
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullName1() {
    new QuandlFedFundsFutureConvention(null, IDS, TRADE_TIME, TIME_ZONE, UNIT_AMOUNT, UNDERLYING_CONVENTION);
  }

  @Override
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullName2() {
    new QuandlFedFundsFutureConvention(null, IDS, TRADE_TIME, TIME_ZONE, UNIT_AMOUNT, UNDERLYING_CONVENTION,
        TRADING_EXCHANGE, SETTLEMENT_EXCHANGE);
  }

  @Override
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullExternalIdBundle1() {
    new QuandlFedFundsFutureConvention(NAME, null, TRADE_TIME, TIME_ZONE, UNIT_AMOUNT, UNDERLYING_CONVENTION);
  }

  @Override
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullExternalIdBundle2() {
    new QuandlFedFundsFutureConvention(NAME, null, TRADE_TIME, TIME_ZONE, UNIT_AMOUNT, UNDERLYING_CONVENTION,
        TRADING_EXCHANGE, SETTLEMENT_EXCHANGE);
  }

  @Override
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullTradeTime1() {
    new QuandlFedFundsFutureConvention(NAME, IDS, null, TIME_ZONE, UNIT_AMOUNT, UNDERLYING_CONVENTION);
  }

  @Override
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullTradeTime2() {
    new QuandlFedFundsFutureConvention(NAME, IDS, null, TIME_ZONE, UNIT_AMOUNT, UNDERLYING_CONVENTION,
        TRADING_EXCHANGE, SETTLEMENT_EXCHANGE);
  }

}
