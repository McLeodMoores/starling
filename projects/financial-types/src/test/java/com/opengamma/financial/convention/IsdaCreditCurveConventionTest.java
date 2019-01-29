/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.convention;

import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.fudgemsg.FudgeRuntimeContextException;
import org.joda.beans.Bean;
import org.testng.annotations.Test;
import org.threeten.bp.Period;

import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.opengamma.analytics.financial.credit.isdastandardmodel.StubType;
import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link IsdaCreditCurveConvention}.
 */
@Test(groups = TestGroup.UNIT)
public class IsdaCreditCurveConventionTest extends AbstractBeanTestCase {
  private static final String NAME = "name";
  private static final ExternalIdBundle IDS = ExternalIdBundle.of("conv", "CDS");
  private static final int STEP_IN = 2;
  private static final int CASH_SETTLE = 3;
  private static final boolean PAY_ACCRUAL_ON_DEFAULT = true;
  private static final Period COUPON_INTERVAL = Period.ofMonths(3);
  private static final StubType STUB_TYPE = StubType.BACKLONG;
  private static final boolean PROTECT_FROM_START = false;
  private static final BusinessDayConvention BDC = BusinessDayConventions.FOLLOWING;
  private static final CalendarAdapter CALENDAR = new CalendarAdapter(WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
  private static final DayCount ACCRUAL_DAY_COUNT = DayCounts.ACT_360;
  private static final DayCount CURVE_DAY_COUNT = DayCounts.ACT_365;
  private static final IsdaCreditCurveConvention CONVENTION = new IsdaCreditCurveConvention();
  static {
    CONVENTION.setAccrualDayCount(ACCRUAL_DAY_COUNT);
    CONVENTION.setBusinessDayConvention(BDC);
    CONVENTION.setCashSettle(CASH_SETTLE);
    CONVENTION.setCouponInterval(COUPON_INTERVAL);
    CONVENTION.setCurveDayCount(CURVE_DAY_COUNT);
    CONVENTION.setExternalIdBundle(IDS);
    CONVENTION.setName(NAME);
    CONVENTION.setPayAccOnDefault(PAY_ACCRUAL_ON_DEFAULT);
    CONVENTION.setProtectFromStartOfDay(PROTECT_FROM_START);
    CONVENTION.setRegionCalendar(CALENDAR);
    CONVENTION.setStepIn(STEP_IN);
    CONVENTION.setStubType(STUB_TYPE);
  }

  @Override
  public JodaBeanProperties<IsdaCreditCurveConvention> getJodaBeanProperties() {
    return new JodaBeanProperties<>(IsdaCreditCurveConvention.class,
        Arrays.asList("name", "externalIdBundle", "accrualDayCount", "businessDayConvention", "cashSettle", "couponInterval", "curveDayCount",
            "payAccOnDefault", "protectFromStartOfDay", "regionCalendar", "stepIn", "stubType"),
        Arrays.asList(NAME, IDS, ACCRUAL_DAY_COUNT, BDC, CASH_SETTLE, COUPON_INTERVAL, CURVE_DAY_COUNT, PAY_ACCRUAL_ON_DEFAULT, PROTECT_FROM_START,
            CALENDAR, STEP_IN, STUB_TYPE),
        Arrays.asList("other", ExternalIdBundle.of("eid", "2"), CURVE_DAY_COUNT, BusinessDayConventions.MODIFIED_FOLLOWING, CASH_SETTLE + 1,
            Period.ofYears(1), ACCRUAL_DAY_COUNT, !PAY_ACCRUAL_ON_DEFAULT, !PROTECT_FROM_START, new CalendarAdapter(WeekendWorkingDayCalendar.FRIDAY_SATURDAY),
            STEP_IN + 1, StubType.NONE));
  }

  /**
   * This object cannot be converted to a Fudge message or Joda bean because of
   * the CalendarAdapter.
   */
  @Override
  @Test(dataProvider = "propertyValues")
  protected <TYPE extends Bean> void testCycle(final JodaBeanProperties<TYPE> properties) {
    try {
      assertEquals(CONVENTION, cycleObjectProxy(IsdaCreditCurveConvention.class, CONVENTION));
      fail();
    } catch (final FudgeRuntimeContextException e) {
    }
    try {
      assertEquals(CONVENTION, cycleObjectBytes(IsdaCreditCurveConvention.class, CONVENTION));
      fail();
    } catch (final FudgeRuntimeContextException e) {
    }
    try {
      assertEquals(CONVENTION, cycleObjectXml(IsdaCreditCurveConvention.class, CONVENTION));
      fail();
    } catch (final FudgeRuntimeContextException e) {
    }
    try {
      assertEquals(CONVENTION, cycleObjectJodaXml(IsdaCreditCurveConvention.class, CONVENTION));
      fail();
    } catch (final RuntimeException e) {
    }
  }
}
