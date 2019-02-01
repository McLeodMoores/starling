/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.equity;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.id.ExternalId;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests for {@link EquityVarianceSwapSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class EquityVarianceSwapSecurityTest extends AbstractBeanTestCase {
  private static final ExternalId UNDERLYING_ID = ExternalId.of("ts", "1");
  private static final Currency CURRENCY = Currency.AUD;
  private static final double STRIKE = 7;
  private static final double NOTIONAL = 1000000;
  private static final boolean AS_VARIANCE = true;
  private static final double ANNUALIZATION = 252;
  private static final ZonedDateTime FIRST_OBS_DATE = DateUtils.getUTCDate(2020, 2, 1);
  private static final ZonedDateTime LAST_OBS_DATE = DateUtils.getUTCDate(2021, 2, 1);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2020, 1, 1);
  private static final ExternalId REGION = ExternalSchemes.countryRegionId(Country.AU);
  private static final Frequency OBS_FREQUENCY = SimpleFrequency.DAILY;

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(EquityVarianceSwapSecurity.class,
        Arrays.asList("spotUnderlyingId", "currency", "strike", "notional", "parameterizedAsVariance", "annualizationFactor", "firstObservationDate",
            "lastObservationDate", "settlementDate", "regionId", "observationFrequency"),
        Arrays.asList(UNDERLYING_ID, CURRENCY, STRIKE, NOTIONAL, AS_VARIANCE, ANNUALIZATION, FIRST_OBS_DATE, LAST_OBS_DATE, SETTLEMENT_DATE, REGION,
            OBS_FREQUENCY),
        Arrays.asList(REGION, Currency.BRL, STRIKE * 2, NOTIONAL * 2, !AS_VARIANCE, ANNUALIZATION + 1, LAST_OBS_DATE, SETTLEMENT_DATE, FIRST_OBS_DATE,
            UNDERLYING_ID, SimpleFrequency.CONTINUOUS));
  }

  /**
   * Tests that all fields are set in the constructor.
   */
  public void testConstructor() {
    EquityVarianceSwapSecurity swap = new EquityVarianceSwapSecurity();
    assertEquals(swap.getAnnualizationFactor(), 0.);
    assertNull(swap.getCurrency());
    assertNull(swap.getFirstObservationDate());
    assertNull(swap.getLastObservationDate());
    assertEquals(swap.getNotional(), 0.);
    assertNull(swap.getObservationFrequency());
    assertNull(swap.getRegionId());
    assertNull(swap.getSettlementDate());
    assertNull(swap.getSpotUnderlyingId());
    assertEquals(swap.getStrike(), 0.);
    assertFalse(swap.isParameterizedAsVariance());
    swap = new EquityVarianceSwapSecurity(UNDERLYING_ID, CURRENCY, STRIKE, NOTIONAL, AS_VARIANCE, ANNUALIZATION, FIRST_OBS_DATE, LAST_OBS_DATE, SETTLEMENT_DATE,
        REGION, OBS_FREQUENCY);
    assertEquals(swap.getAnnualizationFactor(), ANNUALIZATION);
    assertEquals(swap.getCurrency(), CURRENCY);
    assertEquals(swap.getFirstObservationDate(), FIRST_OBS_DATE);
    assertEquals(swap.getLastObservationDate(), LAST_OBS_DATE);
    assertEquals(swap.getNotional(), NOTIONAL);
    assertEquals(swap.getObservationFrequency(), OBS_FREQUENCY);
    assertEquals(swap.getRegionId(), REGION);
    assertEquals(swap.getSettlementDate(), SETTLEMENT_DATE);
    assertEquals(swap.getSpotUnderlyingId(), UNDERLYING_ID);
    assertEquals(swap.getStrike(), STRIKE);
    assertEquals(swap.isParameterizedAsVariance(), AS_VARIANCE);
  }

  /**
   * Tests that accept() calls the correct method.
   */
  public void testVisitor() {
    final EquityVarianceSwapSecurity swap = new EquityVarianceSwapSecurity(UNDERLYING_ID, CURRENCY, STRIKE, NOTIONAL, AS_VARIANCE, ANNUALIZATION,
        FIRST_OBS_DATE, LAST_OBS_DATE, SETTLEMENT_DATE, REGION, OBS_FREQUENCY);
    assertEquals(swap.accept(TestVisitor.INSTANCE), "EquityVarianceSwapSecurity");
  }

  private static class TestVisitor extends FinancialSecurityVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
      return security.getClass().getSimpleName();
    }
  }
}
