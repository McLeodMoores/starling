/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.convention;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;
import org.threeten.bp.LocalTime;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link IborIndexConvention}.
 */
@Test(groups = TestGroup.UNIT)
public class IborIndexConventionTest extends AbstractBeanTestCase {
  private static final String NAME = "name";
  private static final ExternalIdBundle EID = ExternalIdBundle.of("eid", "1");
  private static final DayCount DC = DayCounts.ACT_360;
  private static final BusinessDayConvention BDC = BusinessDayConventions.FOLLOWING;
  private static final int SETTLEMENT_DAYS = 2;
  private static final boolean IS_EOM = true;
  private static final Currency CCY = Currency.USD;
  private static final LocalTime FIXING_TIME = LocalTime.of(11, 00);
  private static final String FIXING_TIME_ZONE = "Americas/New York";
  private static final ExternalId FIXING_CALENDAR = ExternalSchemes.countryRegionId(Country.GB);
  private static final ExternalId REGION_CALENDAR = ExternalSchemes.countryRegionId(Country.US);
  private static final String FIXING_PAGE = "fixing";
  private static final IborIndexConvention CONVENTION = new IborIndexConvention(NAME, EID, DC, BDC, SETTLEMENT_DAYS, IS_EOM, CCY, FIXING_TIME, FIXING_TIME_ZONE,
      FIXING_CALENDAR, REGION_CALENDAR, FIXING_PAGE);

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(IborIndexConvention.class,
        Arrays.asList("name", "externalIdBundle", "dayCount", "businessDayConvention", "isEOM", "currency", "fixingTime", "fixingTimeZone", "fixingCalendar",
            "regionCalendar", "fixingPage"),
        Arrays.<Object> asList(NAME, EID, DC, BDC, IS_EOM, CCY, FIXING_TIME, FIXING_TIME_ZONE, FIXING_CALENDAR, REGION_CALENDAR, FIXING_PAGE),
        Arrays.<Object> asList("other", ExternalIdBundle.of("eid", "2"), DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING, !IS_EOM, Currency.AUD,
            FIXING_TIME.plusHours(1), "Americas/Los Angeles", REGION_CALENDAR, FIXING_CALENDAR, "page"));
  }

  /**
   * Tests the returned type.
   */
  public void testType() {
    assertEquals(CONVENTION.getConventionType(), IborIndexConvention.TYPE);
  }

  /**
   * Tests that the accept() method visits the right method.
   */
  public void testVisitor() {
    assertEquals(CONVENTION.accept(TestVisitor.INSTANCE), "visited");
  }

  private static class TestVisitor extends FinancialConventionVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitIborIndexConvention(final IborIndexConvention convention) {
      return "visited";
    }

  }

}
