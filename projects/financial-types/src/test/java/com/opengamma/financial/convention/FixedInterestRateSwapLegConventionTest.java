/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.convention;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.annuity.CompoundingMethod;
import com.opengamma.analytics.financial.instrument.annuity.DateRelativeTo;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link FixedInterestRateSwapLegConvention}.
 */
@Test(groups = TestGroup.UNIT)
public class FixedInterestRateSwapLegConventionTest extends AbstractBeanTestCase {
  private static final String NAME = "name";
  private static final ExternalIdBundle EIDS = ExternalIdBundle.of("eid", "1");
  private static final Set<ExternalId> PAYMENT_CALENDARS = Collections.singleton(ExternalSchemes.countryRegionId(Country.US));
  private static final Set<ExternalId> CALCULATION_CALENDARS = Collections.singleton(ExternalSchemes.countryRegionId(Country.GB));
  private static final Set<ExternalId> MATURITY_CALENDARS = Collections.singleton(ExternalSchemes.countryRegionId(Country.EU));
  private static final BusinessDayConvention PAYMENT_BDC = BusinessDayConventions.FOLLOWING;
  private static final BusinessDayConvention CALCULATION_BDC = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final BusinessDayConvention MATURITY_BDC = BusinessDayConventions.MODIFIED_PRECEDING;
  private static final DayCount DC = DayCounts.ACT_360;
  private static final Frequency PAYMENT_FREQUENCY = SimpleFrequency.ANNUAL;
  private static final Frequency CALCULATION_FREQUENCY = SimpleFrequency.BIMONTHLY;
  private static final DateRelativeTo PAYMENT_RELATIVE_TO = DateRelativeTo.END;
  private static final boolean ADJUSTED_ACCRUAL = true;
  private static final int SETTLEMENT_DAYS = 2;
  private static final RollConvention ROLL_CONVENTION = RollConvention.EIGHT;
  private static final CompoundingMethod COMPOUNDING_METHOD = CompoundingMethod.FLAT;
  private static final int PAYMENT_LAG = 3;
  private static final FixedInterestRateSwapLegConvention CONVENTION = new FixedInterestRateSwapLegConvention(NAME, EIDS, PAYMENT_CALENDARS,
      CALCULATION_CALENDARS, MATURITY_CALENDARS, PAYMENT_BDC, CALCULATION_BDC, MATURITY_BDC, DC, PAYMENT_FREQUENCY, CALCULATION_FREQUENCY, PAYMENT_RELATIVE_TO,
      ADJUSTED_ACCRUAL, SETTLEMENT_DAYS, ROLL_CONVENTION, COMPOUNDING_METHOD, PAYMENT_LAG);

  @Override
  public JodaBeanProperties<FixedInterestRateSwapLegConvention> getJodaBeanProperties() {
    return new JodaBeanProperties<>(FixedInterestRateSwapLegConvention.class,
        Arrays.asList("name", "externalIdBundle", "paymentCalendars", "calculationCalendars", "maturityCalendars", "paymentDayConvention",
            "calculationBusinessDayConvention", "maturityBusinessDayConvention", "dayCountConvention", "paymentFrequency", "calculationFrequency",
            "paymentRelativeTo", "adjustedAccrual", "settlementDays", "rollConvention", "compoundingMethod", "paymentLag"),
        Arrays.<Object> asList(NAME, EIDS, PAYMENT_CALENDARS, CALCULATION_CALENDARS, MATURITY_CALENDARS, PAYMENT_BDC, CALCULATION_BDC, MATURITY_BDC, DC,
            PAYMENT_FREQUENCY, CALCULATION_FREQUENCY, PAYMENT_RELATIVE_TO, ADJUSTED_ACCRUAL, SETTLEMENT_DAYS, ROLL_CONVENTION, COMPOUNDING_METHOD, PAYMENT_LAG),
        Arrays.<Object> asList("other", ExternalIdBundle.of("eid", "2"), CALCULATION_CALENDARS, MATURITY_CALENDARS, PAYMENT_CALENDARS, CALCULATION_BDC,
            MATURITY_BDC, PAYMENT_BDC, DayCounts.ACT_365, CALCULATION_FREQUENCY, PAYMENT_FREQUENCY, DateRelativeTo.START, !ADJUSTED_ACCRUAL,
            SETTLEMENT_DAYS + 1, RollConvention.EIGHTEEN, CompoundingMethod.NONE, PAYMENT_LAG + 1));
  }

  /**
   * Tests the returned type.
   */
  public void testType() {
    assertEquals(CONVENTION.getConventionType(), FixedInterestRateSwapLegConvention.TYPE);
  }

  /**
   * Tests that the accept() method visits the right method.
   */
  public void testVisitor() {
    assertEquals(CONVENTION.accept(TestVisitor.INSTANCE), "visited");
  }

  /**
   *
   */
  private static final class TestVisitor extends FinancialConventionVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitFixedInterestRateSwapLegConvention(final FixedInterestRateSwapLegConvention convention) {
      return "visited";
    }

  }

}
