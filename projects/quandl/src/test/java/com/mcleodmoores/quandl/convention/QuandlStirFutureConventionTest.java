/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.convention;

import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;

import com.mcleodmoores.quandl.QuandlConstants;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link QuandlStirFutureConvention}.
 */
@Test(groups = TestGroup.UNIT)
public class QuandlStirFutureConventionTest extends QuandlFutureConventionTestBase {
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
  /** The currency */
  private static final Currency CURRENCY = Currency.USD;
  /** The future tenor */
  private static final Tenor FUTURE_TENOR = Tenor.THREE_MONTHS;
  /** The underlying tenor */
  private static final Tenor UNDERLYING_TENOR = Tenor.THREE_MONTHS;
  /** The nth day of the month of the future expiry */
  private static final int NTH_DAY = 3;
  /** The expiry day */
  private static final String DAY_OF_WEEK = DayOfWeek.MONDAY.name();

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
  protected QuandlStirFutureConvention getEmptyInstance() {
    return new QuandlStirFutureConvention();
  }

  @Override
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullName1() {
    new QuandlStirFutureConvention(null, IDS, CURRENCY, FUTURE_TENOR, UNDERLYING_TENOR, TRADE_TIME, TIME_ZONE, UNIT_AMOUNT, UNDERLYING_CONVENTION,
        NTH_DAY, DAY_OF_WEEK, ExternalSchemes.countryRegionId(Country.US));
  }

  @Override
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullName2() {
    new QuandlStirFutureConvention(null, IDS, CURRENCY, FUTURE_TENOR, UNDERLYING_TENOR, TRADE_TIME, TIME_ZONE, UNIT_AMOUNT, UNDERLYING_CONVENTION,
        NTH_DAY, DAY_OF_WEEK, TRADING_EXCHANGE, SETTLEMENT_EXCHANGE, ExternalSchemes.countryRegionId(Country.US));
  }

  @Override
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullExternalIdBundle1() {
    new QuandlStirFutureConvention(NAME, null, CURRENCY, FUTURE_TENOR, UNDERLYING_TENOR, TRADE_TIME, TIME_ZONE, UNIT_AMOUNT, UNDERLYING_CONVENTION,
        NTH_DAY, DAY_OF_WEEK, ExternalSchemes.countryRegionId(Country.US));
  }

  @Override
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullExternalIdBundle2() {
    new QuandlStirFutureConvention(NAME, null, CURRENCY, FUTURE_TENOR, UNDERLYING_TENOR, TRADE_TIME, TIME_ZONE, UNIT_AMOUNT, UNDERLYING_CONVENTION,
        NTH_DAY, DAY_OF_WEEK, TRADING_EXCHANGE, SETTLEMENT_EXCHANGE, ExternalSchemes.countryRegionId(Country.US));
  }

  @Override
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullTradeTime1() {
    new QuandlStirFutureConvention(NAME, IDS, CURRENCY, FUTURE_TENOR, UNDERLYING_TENOR, null, TIME_ZONE, UNIT_AMOUNT, UNDERLYING_CONVENTION,
        NTH_DAY, DAY_OF_WEEK, ExternalSchemes.countryRegionId(Country.US));
  }

  @Override
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullTradeTime2() {
    new QuandlStirFutureConvention(NAME, IDS, CURRENCY, FUTURE_TENOR, UNDERLYING_TENOR, null, TIME_ZONE, UNIT_AMOUNT, UNDERLYING_CONVENTION,
        NTH_DAY, DAY_OF_WEEK, TRADING_EXCHANGE, SETTLEMENT_EXCHANGE, ExternalSchemes.countryRegionId(Country.US));
  }

  /**
   * Tests the behaviour when a null currency is supplied to the setter.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMethodNullCurrency() {
    getEmptyInstance().setCurrency(null);
  }

  /**
   * Tests the behaviour when a null currency is supplied to the constructor that does not
   * take trading and settlement exchange names.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullCurrency1() {
    new QuandlStirFutureConvention(NAME, IDS, null, FUTURE_TENOR, UNDERLYING_TENOR, TRADE_TIME, TIME_ZONE, UNIT_AMOUNT, UNDERLYING_CONVENTION,
        NTH_DAY, DAY_OF_WEEK, ExternalSchemes.countryRegionId(Country.US));
  }

  /**
   * Tests the behaviour when a null currency is supplied to the constructor that takes trading
   * and settlement exchange names.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullCurrency2() {
    new QuandlStirFutureConvention(NAME, IDS, null, FUTURE_TENOR, UNDERLYING_TENOR, TRADE_TIME, TIME_ZONE, UNIT_AMOUNT, UNDERLYING_CONVENTION,
        NTH_DAY, DAY_OF_WEEK, TRADING_EXCHANGE, SETTLEMENT_EXCHANGE, ExternalSchemes.countryRegionId(Country.US));
  }

  /**
   * Tests the behaviour when a null future tenor is supplied to the setter.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMethodNullFutureTenor() {
    getEmptyInstance().setFutureTenor(null);
  }

  /**
   * Tests the behaviour when a null future tenor is supplied to the constructor that does not
   * take trading and settlement exchange names.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullFutureTenor1() {
    new QuandlStirFutureConvention(NAME, IDS, CURRENCY, null, UNDERLYING_TENOR, TRADE_TIME, TIME_ZONE, UNIT_AMOUNT, UNDERLYING_CONVENTION,
        NTH_DAY, DAY_OF_WEEK, ExternalSchemes.countryRegionId(Country.US));
  }

  /**
   * Tests the behaviour when a null future tenor is supplied to the constructor that takes trading
   * and settlement exchange names.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullFutureTenor2() {
    new QuandlStirFutureConvention(NAME, IDS, CURRENCY, null, UNDERLYING_TENOR, TRADE_TIME, TIME_ZONE, UNIT_AMOUNT, UNDERLYING_CONVENTION,
        NTH_DAY, DAY_OF_WEEK, TRADING_EXCHANGE, SETTLEMENT_EXCHANGE, ExternalSchemes.countryRegionId(Country.US));
  }

  /**
   * Tests the behaviour when a null underlying tenor is supplied to the setter.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMethodNullUnderlyingTenor() {
    getEmptyInstance().setUnderlyingTenor(null);
  }

  /**
   * Tests the behaviour when a null underlying tenor is supplied to the constructor that does not
   * take trading and settlement exchange names.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullUnderlyingTenor1() {
    new QuandlStirFutureConvention(NAME, IDS, CURRENCY, FUTURE_TENOR, null, TRADE_TIME, TIME_ZONE, UNIT_AMOUNT, UNDERLYING_CONVENTION,
        NTH_DAY, DAY_OF_WEEK, ExternalSchemes.countryRegionId(Country.US));
  }

  /**
   * Tests the behaviour when a null underlying tenor is supplied to the constructor that takes trading
   * and settlement exchange names.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullUnderlyingTenor2() {
    new QuandlStirFutureConvention(NAME, IDS, CURRENCY, FUTURE_TENOR, null, TRADE_TIME, TIME_ZONE, UNIT_AMOUNT, UNDERLYING_CONVENTION,
        NTH_DAY, DAY_OF_WEEK, TRADING_EXCHANGE, SETTLEMENT_EXCHANGE, ExternalSchemes.countryRegionId(Country.US));
  }

  /**
   * Tests the behaviour when a null day of week is supplied to the setter.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMethodNullDayOfWeek() {
    getEmptyInstance().setDayOfWeek(null);
  }

  /**
   * Tests the behaviour when a null day of week is supplied to the constructor that does not
   * take trading and settlement exchange names.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullDayOfWeek1() {
    new QuandlStirFutureConvention(NAME, IDS, CURRENCY, FUTURE_TENOR, UNDERLYING_TENOR, TRADE_TIME, TIME_ZONE, UNIT_AMOUNT, UNDERLYING_CONVENTION,
        NTH_DAY, null, ExternalSchemes.countryRegionId(Country.US));
  }

  /**
   * Tests the behaviour when a null day of week is supplied to the constructor that takes trading
   * and settlement exchange names.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullDayOfWeek2() {
    new QuandlStirFutureConvention(NAME, IDS, CURRENCY, FUTURE_TENOR, UNDERLYING_TENOR, TRADE_TIME, TIME_ZONE, UNIT_AMOUNT, UNDERLYING_CONVENTION,
        NTH_DAY, null, TRADING_EXCHANGE, SETTLEMENT_EXCHANGE, ExternalSchemes.countryRegionId(Country.US));
  }

  /**
   * Tests the behaviour when a null trading exchange calendar id is supplied to the setter.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMethodNullTradingExchangeCalendarId() {
    getEmptyInstance().setTradingExchangeCalendarId(null);
  }

  /**
   * Tests the behaviour when a null trading exchange calendar id is supplied to the constructor that does not
   * take trading and settlement exchange names.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullTradingExchangeCalendarId1() {
    new QuandlStirFutureConvention(NAME, IDS, CURRENCY, FUTURE_TENOR, UNDERLYING_TENOR, TRADE_TIME, TIME_ZONE, UNIT_AMOUNT, UNDERLYING_CONVENTION,
        NTH_DAY, DayOfWeek.WEDNESDAY.name(), null);
  }

  /**
   * Tests the behaviour when a null day of week is supplied to the constructor that takes trading
   * and settlement exchange names.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullTradingExchangeCalendarId2() {
    new QuandlStirFutureConvention(NAME, IDS, CURRENCY, FUTURE_TENOR, UNDERLYING_TENOR, TRADE_TIME, TIME_ZONE, UNIT_AMOUNT, UNDERLYING_CONVENTION,
        NTH_DAY, DayOfWeek.WEDNESDAY.name(), TRADING_EXCHANGE, SETTLEMENT_EXCHANGE, null);
  }
}
