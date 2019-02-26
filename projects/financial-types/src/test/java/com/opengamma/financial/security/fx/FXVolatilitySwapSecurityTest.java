/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.fx;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.swap.VolatilitySwapType;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests for {@link FXVolatilitySwapSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class FXVolatilitySwapSecurityTest extends AbstractBeanTestCase {
  private static final Currency BASE_CURRENCY = Currency.AUD;
  private static final Currency COUNTER_CURRENCY = Currency.BRL;
  private static final double NOTIONAL = 1000000;
  private static final VolatilitySwapType TYPE = VolatilitySwapType.VEGA;
  private static final double STRIKE = 7;
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2020, 1, 1);
  private static final ZonedDateTime MATURITY_DATE = DateUtils.getUTCDate(2021, 1, 1);
  private static final double ANNUALIZATION = 252;
  private static final ZonedDateTime FIRST_OBS_DATE = DateUtils.getUTCDate(2020, 2, 1);
  private static final ZonedDateTime LAST_OBS_DATE = DateUtils.getUTCDate(2021, 2, 1);
  private static final Frequency OBS_FREQUENCY = SimpleFrequency.DAILY;
  private static final ExternalId UNDERLYING_TS = ExternalId.of("ts", "1");

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(FXVolatilitySwapSecurity.class,
        Arrays.asList("baseCurrency", "counterCurrency", "notional", "volatilitySwapType", "strike", "settlementDate", "maturityDate", "annualizationFactor",
            "firstObservationDate", "lastObservationDate", "observationFrequency", "underlyingId"),
        Arrays.asList(BASE_CURRENCY, COUNTER_CURRENCY, NOTIONAL, TYPE, STRIKE, SETTLEMENT_DATE, MATURITY_DATE, ANNUALIZATION, FIRST_OBS_DATE, LAST_OBS_DATE,
            OBS_FREQUENCY, UNDERLYING_TS),
        Arrays.asList(COUNTER_CURRENCY, BASE_CURRENCY, NOTIONAL * 2, VolatilitySwapType.VOLATILITY, STRIKE * 2, MATURITY_DATE, SETTLEMENT_DATE,
            ANNUALIZATION + 1, LAST_OBS_DATE, FIRST_OBS_DATE, SimpleFrequency.CONTINUOUS, ExternalId.of("ts", "2")));
  }

  /**
   * Tests that all fields are set in the constructor.
   */
  public void testConstructor() {
    FXVolatilitySwapSecurity fx = new FXVolatilitySwapSecurity();
    assertEquals(fx.getAnnualizationFactor(), 0.);
    assertNull(fx.getBaseCurrency());
    assertNull(fx.getCounterCurrency());
    assertNull(fx.getCurrency());
    assertNull(fx.getFirstObservationDate());
    assertNull(fx.getLastObservationDate());
    assertNull(fx.getMaturityDate());
    assertEquals(fx.getNotional(), 0.);
    assertNull(fx.getObservationFrequency());
    assertNull(fx.getSettlementDate());
    assertEquals(fx.getStrike(), 0.);
    assertNull(fx.getUnderlyingId());
    fx = new FXVolatilitySwapSecurity(NOTIONAL, TYPE, STRIKE, SETTLEMENT_DATE, MATURITY_DATE, ANNUALIZATION, FIRST_OBS_DATE, LAST_OBS_DATE, OBS_FREQUENCY,
        BASE_CURRENCY, COUNTER_CURRENCY);
    assertEquals(fx.getAnnualizationFactor(), ANNUALIZATION);
    assertEquals(fx.getBaseCurrency(), BASE_CURRENCY);
    assertEquals(fx.getCounterCurrency(), COUNTER_CURRENCY);
    assertEquals(fx.getCurrency(), COUNTER_CURRENCY);
    assertEquals(fx.getFirstObservationDate(), FIRST_OBS_DATE);
    assertEquals(fx.getLastObservationDate(), LAST_OBS_DATE);
    assertEquals(fx.getMaturityDate(), MATURITY_DATE);
    assertEquals(fx.getNotional(), NOTIONAL);
    assertEquals(fx.getObservationFrequency(), OBS_FREQUENCY);
    assertEquals(fx.getSettlementDate(), SETTLEMENT_DATE);
    assertEquals(fx.getStrike(), STRIKE);
    assertNull(fx.getUnderlyingId());
    fx = new FXVolatilitySwapSecurity(NOTIONAL, TYPE, STRIKE, SETTLEMENT_DATE, MATURITY_DATE, ANNUALIZATION, FIRST_OBS_DATE, LAST_OBS_DATE, OBS_FREQUENCY,
        UNDERLYING_TS, BASE_CURRENCY, COUNTER_CURRENCY);
    assertEquals(fx.getAnnualizationFactor(), ANNUALIZATION);
    assertEquals(fx.getBaseCurrency(), BASE_CURRENCY);
    assertEquals(fx.getCounterCurrency(), COUNTER_CURRENCY);
    assertEquals(fx.getCurrency(), COUNTER_CURRENCY);
    assertEquals(fx.getFirstObservationDate(), FIRST_OBS_DATE);
    assertEquals(fx.getLastObservationDate(), LAST_OBS_DATE);
    assertEquals(fx.getMaturityDate(), MATURITY_DATE);
    assertEquals(fx.getNotional(), NOTIONAL);
    assertEquals(fx.getObservationFrequency(), OBS_FREQUENCY);
    assertEquals(fx.getSettlementDate(), SETTLEMENT_DATE);
    assertEquals(fx.getStrike(), STRIKE);
    assertEquals(fx.getUnderlyingId(), UNDERLYING_TS);
  }

  /**
   * Tests that accept() calls the correct method.
   */
  public void testVisitor() {
    final FXVolatilitySwapSecurity fx = new FXVolatilitySwapSecurity(NOTIONAL, TYPE, STRIKE, SETTLEMENT_DATE, MATURITY_DATE, ANNUALIZATION, FIRST_OBS_DATE,
        LAST_OBS_DATE, OBS_FREQUENCY, BASE_CURRENCY, COUNTER_CURRENCY);
    assertEquals(fx.accept(TestVisitor.INSTANCE), "FXVolatilitySwapSecurity");
  }

  /**
   *
   */
  private static final class TestVisitor extends FinancialSecurityVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitFXVolatilitySwapSecurity(final FXVolatilitySwapSecurity security) {
      return security.getClass().getSimpleName();
    }
  }
}
