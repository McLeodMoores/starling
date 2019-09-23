/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureSecurity;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class FederalFundsFutureSecurityDiscountingMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IndexON INDEX_FEDFUND = MulticurveProviderDiscountDataSets.getIndexesON()[0];
  private static final WorkingDayCalendar NYC = MulticurveProviderDiscountDataSets.getUSDCalendar();

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 1, 30);

  private static final ZonedDateTime MARCH_1 = DateUtils.getUTCDate(2012, 3, 1);

  private static final FederalFundsFutureSecurityDefinition FUTURE_SECURITY_DEFINITION = FederalFundsFutureSecurityDefinition
      .fromFedFund(MARCH_1, INDEX_FEDFUND, CalendarAdapter.of(NYC));

  private static final FederalFundsFutureSecurity FUTURE_SECURITY = FUTURE_SECURITY_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final FederalFundsFutureSecurityDiscountingMethod METHOD_SECURITY = FederalFundsFutureSecurityDiscountingMethod
      .getInstance();

  private static final double TOLERANCE_PRICE = 1.0E-8;

  /**
   *
   */
  @Test
  public void priceBeforeFixing() {
    double interest = 0.0;
    final double[] ratePeriod = new double[FUTURE_SECURITY_DEFINITION.getFixingPeriodAccrualFactor().length];
    for (int i = 0; i < FUTURE_SECURITY_DEFINITION.getFixingPeriodAccrualFactor().length; i++) {
      ratePeriod[i] = MULTICURVES.getSimplyCompoundForwardRate(INDEX_FEDFUND, FUTURE_SECURITY.getFixingPeriodTime()[i],
          FUTURE_SECURITY.getFixingPeriodTime()[i + 1],
          FUTURE_SECURITY.getFixingPeriodAccrualFactor()[i]);
      interest += ratePeriod[i] * FUTURE_SECURITY.getFixingPeriodAccrualFactor()[i];
    }
    final double rate = interest / FUTURE_SECURITY.getFixingTotalAccrualFactor();
    final double priceExpected = 1.0 - rate;
    final double priceComputed = METHOD_SECURITY.price(FUTURE_SECURITY, MULTICURVES);
    assertEquals("Federal Funds Future Security: price", priceExpected, priceComputed, TOLERANCE_PRICE);
  }

  /**
   *
   */
  @Test
  public void priceAfterFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 3, 7);
    final ZonedDateTime[] dateFixing = new ZonedDateTime[] { DateUtils.getUTCDate(2012, 3, 1), DateUtils.getUTCDate(2012, 3, 2),
        DateUtils.getUTCDate(2012, 3, 5), DateUtils.getUTCDate(2012, 3, 6),
        DateUtils.getUTCDate(2012, 3, 7) };
    final double[] rateFixing = new double[] { 0.0010, 0.0011, 0.0012, 0.0013, 0.0014 };
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(dateFixing, rateFixing);
    final FederalFundsFutureSecurity futureSecurity = FUTURE_SECURITY_DEFINITION.toDerivative(referenceDate, fixingTS);
    double interest = futureSecurity.getAccruedInterest();
    final double[] ratePeriod = new double[futureSecurity.getFixingPeriodAccrualFactor().length];
    for (int i = 0; i < futureSecurity.getFixingPeriodAccrualFactor().length; i++) {
      ratePeriod[i] = MULTICURVES.getSimplyCompoundForwardRate(INDEX_FEDFUND, futureSecurity.getFixingPeriodTime()[i],
          futureSecurity.getFixingPeriodTime()[i + 1],
          futureSecurity.getFixingPeriodAccrualFactor()[i]);
      interest += ratePeriod[i] * futureSecurity.getFixingPeriodAccrualFactor()[i];
    }
    final double rate = interest / futureSecurity.getFixingTotalAccrualFactor();
    final double priceExpected = 1.0 - rate;
    final double priceComputed = METHOD_SECURITY.price(futureSecurity, MULTICURVES);
    assertEquals("Federal Funds Future Security: price", priceExpected, priceComputed, TOLERANCE_PRICE);
  }

}
