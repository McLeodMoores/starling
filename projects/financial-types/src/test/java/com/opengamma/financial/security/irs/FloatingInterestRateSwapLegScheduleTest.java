/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.irs;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.threeten.bp.LocalDate;

import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.convention.FloatingInterestRateSwapLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.id.ExternalIdBundle;

/**
 * Tests for {@link FloatingInterestRateSwapLegSchedule}.
 */
public class FloatingInterestRateSwapLegScheduleTest extends AbstractBeanTestCase {
  private static final FloatingInterestRateSwapLegConvention CONVENTION = new FloatingInterestRateSwapLegConvention("name", ExternalIdBundle.of("eid", "1"));
  static {
    CONVENTION.setCalculationBusinessDayConvention(BusinessDayConventions.PRECEDING);
    CONVENTION.setCalculationFrequency(SimpleFrequency.SEMI_ANNUAL);
    CONVENTION.setDayCountConvention(DayCounts.ACT_360);
    CONVENTION.setMaturityBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    CONVENTION.setPaymentDayConvention(BusinessDayConventions.MODIFIED_PRECEDING);
    CONVENTION.setPaymentFrequency(SimpleFrequency.SEMI_ANNUAL);
    CONVENTION.setRateType(FloatingRateType.CMS);
    CONVENTION.setFixingBusinessDayConvention(BusinessDayConventions.FOLLOWING);
    CONVENTION.setResetFrequency(SimpleFrequency.SEMI_ANNUAL);
    CONVENTION.setResetBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
  }

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    final FloatingInterestRateSwapLegConvention otherConvention = CONVENTION.clone();
    otherConvention.setName("other");
    return new JodaBeanProperties<>(
        FloatingInterestRateSwapLegSchedule.class, Arrays.asList("convention", "dates", "paymentDates", "calculationDates"), Arrays.asList(CONVENTION,
            new int[] { 1, 2 }, new LocalDate[] { LocalDate.of(2020, 1, 1), LocalDate.of(2021, 1, 1) }, new LocalDate[] { LocalDate.of(2020, 7, 1) }),
        Arrays.asList(otherConvention, new int[0], new LocalDate[0], new LocalDate[0]));
  }

}
