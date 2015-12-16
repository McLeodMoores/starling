/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.convention;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

import com.mcleodmoores.quandl.QuandlConstants;
import com.mcleodmoores.quandl.convention.QuandlFutureConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * Base class for unit tests for classes that extend {@link QuandlFutureConvention}.
 */
public abstract class QuandlFutureConventionTestBase {

  //TODO use data providers
  /**
   * Gets the name of the convention.
   * @return The name
   */
  protected abstract String getName();

  /**
   * Gets the ids of the convention.
   * @return The ids
   */
  protected abstract ExternalIdBundle getIds();

  /**
   * Gets the last trade time of the future.
   * @return The last trade time
   */
  protected abstract String getTradeTime();

  /**
   * Gets the trading time zone of the future.
   * @return The trading time zone
   */
  protected abstract String getTimeZone();

  /**
   * Gets the unit amount of the future.
   * @return The unit amount
   */
  protected abstract double getUnitAmount();

  /**
   * Gets the id of the underlying.
   * @return The id of the underlying
   */
  protected abstract ExternalId getUnderlyingId();

  /**
   * Gets the trading exchange.
   * @return The trading exchange
   */
  protected abstract String getTradingExchange();

  /**
   * Gets the settlement exchange.
   * @return The settlement exchange
   */
  protected abstract String getSettlementExchange();

  /**
   * Gets an empty instance of the convention to be tested.
   * @return An empty instance
   * @param <T> The type of the convention
   */
  protected abstract <T extends QuandlFutureConvention> T getEmptyInstance();

  /**
   * Tests the behaviour when a null convention name is supplied to the constructor that does not
   * take trading and settlement exchange names.
   */
  public abstract void testConstructorNullName1();

  /**
   * Tests the behaviour when a null convention name is supplied to the constructor that takes trading
   * and settlement exchange names.
   */
  public abstract void testConstructorNullName2();

  /**
   * Tests the behaviour when a null convention name is supplied to the setter.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMethodNullName() {
    getEmptyInstance().setName(null);
  }

  /**
   * Tests the behaviour when a null external id bundle is supplied to the constructor that does not
   * take trading and settlement exchange names.
   */
  @Test
  public abstract void testConstructorNullExternalIdBundle1();

  /**
   * Tests the behaviour when a null external id bundle is supplied to the constructor that takes trading
   * and settlement exchange names.
   */
  @Test
  public abstract void testConstructorNullExternalIdBundle2();

  /**
   * Tests the behaviour when a null external id bundle is supplied to the setter.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMethodNullExternalIdBundle() {
    getEmptyInstance().setExternalIdBundle(null);
  }

  /**
   * Tests the behaviour when a null trade time is supplied to the constructor that does not
   * take trading and settlement exchange names.
   */
  @Test
  public abstract void testConstructorNullTradeTime1();

  /**
   * Tests the behaviour when a null trade time is supplied to the constructor that takes trading
   * and settlement exchange names.
   */
  @Test
  public abstract void testConstructorNullTradeTime2();

  /**
   * Tests the behaviour when a null trade time is supplied to the setter.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTradeTime() {
    getEmptyInstance().setLastTradeTime(null);
  }

  /**
   * Tests that the trading exchange name is either taken from the field, parsed from the Quandl code or null.
   */
  @Test
  public void testGetTradingExchangeName() {
    QuandlFutureConvention convention = getEmptyInstance();
    convention.setTradingExchange("ABC");
    // exchange has been set
    assertEquals("ABC", convention.getTradingExchange());
    convention = getEmptyInstance();
    // ids are null, expecting null
    assertNull(convention.getTradingExchange());
    convention = getEmptyInstance();
    convention.setExternalIdBundle(ExternalIdBundle.of(QuandlConstants.ofCode("ABC/TEST")));
    // exchange has been parsed from Quandl code, using all characters before the /
    assertEquals("ABC", convention.getTradingExchange());
    convention = getEmptyInstance();
    convention.setExternalIdBundle(ExternalIdBundle.of(ExternalId.of("TEST", "TEST")));
    // can't parse Quandl code, expecting null
    assertNull(convention.getTradingExchange());
    convention = getEmptyInstance();
    // exchange has not been set, expecting null
    assertNull(convention.getTradingExchange());
  }

  /**
   * Tests that the settlement exchange name is either taken from the field, parsed from the Quandl code or null.
   */
  @Test
  public void testGetSettlementExchangeName() {
    QuandlFutureConvention convention = getEmptyInstance();
    convention.setSettlementExchange("ABC");
    // exchange has been set
    assertEquals("ABC", convention.getSettlementExchange());
    convention = getEmptyInstance();
    // ids are null, expecting null
    assertNull(convention.getSettlementExchange());
    convention = getEmptyInstance();
    convention.setExternalIdBundle(ExternalIdBundle.of(QuandlConstants.ofCode("ABC/TEST")));
    // exchange has been parsed from Quandl code, using all characters before the /
    assertEquals("ABC", convention.getSettlementExchange());
    convention = getEmptyInstance();
    convention.setExternalIdBundle(ExternalIdBundle.of(ExternalId.of("TEST", "TEST")));
    // can't parse Quandl code, expecting null
    assertNull(convention.getSettlementExchange());
    convention = getEmptyInstance();
    // exchange has not been set, expecting null
    assertNull(convention.getSettlementExchange());
  }
}
