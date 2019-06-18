/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.convention;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;

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
 * Tests for {@link DepositConvention}.
 */
@Test(groups = TestGroup.UNIT)
public class DepositConventionTest extends AbstractBeanTestCase {
  private static final String NAME = "name";
  private static final ExternalIdBundle IDS = ExternalIdBundle.of("conv", "US DEPOSIT");
  private static final DayCount DC = DayCounts.ACT_360;
  private static final BusinessDayConvention BDC = BusinessDayConventions.FOLLOWING;
  private static final int SETTLEMENT_DAYS = 2;
  private static final boolean IS_EOM = true;
  private static final Currency CCY = Currency.USD;
  private static final ExternalId REGION = ExternalSchemes.countryRegionId(Country.US);
  private static final FinancialConvention CONVENTION = new DepositConvention(NAME, IDS, DC, BDC, SETTLEMENT_DAYS, IS_EOM, CCY, REGION);

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(DepositConvention.class,
        Arrays.asList("name", "externalIdBundle", "dayCount", "businessDayConvention", "settlementDays", "isEOM", "currency", "regionCalendar"),
        Arrays.asList(NAME, IDS, DC, BDC, SETTLEMENT_DAYS, IS_EOM, CCY, REGION),
        Arrays.asList("other", ExternalIdBundle.of("conv", "AU DEPOSIT"), DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING, SETTLEMENT_DAYS + 1,
            !IS_EOM, Currency.AUD, ExternalSchemes.countryRegionId(Country.AU)));
  }

  /**
   * Tests the returned type.
   */
  public void testType() {
    assertEquals(CONVENTION.getConventionType(), DepositConvention.TYPE);
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
    public String visitDepositConvention(final DepositConvention convention) {
      return "visited";
    }

  }

}
