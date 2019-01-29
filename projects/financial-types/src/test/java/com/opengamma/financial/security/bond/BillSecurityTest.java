/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.bond;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.id.ExternalId;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Tests for {@link BillSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class BillSecurityTest extends AbstractBeanTestCase {
  private static final Currency CURRENCY = Currency.USD;
  private static final Expiry MATURITY_DATE = new Expiry(DateUtils.getUTCDate(2020, 10, 10));
  private static final ZonedDateTime ISSUE_DATE = DateUtils.getUTCDate(2018, 10, 10);
  private static final double MINIMUM_INCREMENT = 1000;
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final ExternalId REGION = ExternalSchemes.countryRegionId(Country.US);
  private static final YieldConvention YIELD = SimpleYieldConvention.US_TREASURY_EQUIVALANT;
  private static final ExternalId LEGAL_ENTITY = ExternalId.of("len", "ABC");

  @Override
  public JodaBeanProperties<BillSecurity> getJodaBeanProperties() {
    return new JodaBeanProperties<>(BillSecurity.class,
        Arrays.asList("currency", "maturityDate", "issueDate", "minimumIncrement", "daysToSettle", "dayCount", "regionId", "yieldConvention", "legalEntityId"),
        Arrays.asList(CURRENCY, MATURITY_DATE, ISSUE_DATE, MINIMUM_INCREMENT, SETTLEMENT_DAYS, DAY_COUNT, REGION, YIELD, LEGAL_ENTITY),
        Arrays.asList(Currency.AUD, new Expiry(DateUtils.getUTCDate(2020, 11, 10)), ISSUE_DATE.plusDays(1), MINIMUM_INCREMENT + 1, SETTLEMENT_DAYS + 1,
            DayCounts.ACT_365, LEGAL_ENTITY, SimpleYieldConvention.AUSTRALIA_EX_DIVIDEND, REGION));
  }

  /**
   * Tests that the accept() method points to the correct method in the visitor.
   */
  public void testVisitor() {
    final BillSecurity security = new BillSecurity(CURRENCY, MATURITY_DATE, ISSUE_DATE, MINIMUM_INCREMENT, SETTLEMENT_DAYS, REGION, YIELD, DAY_COUNT,
        LEGAL_ENTITY);
    assertEquals(security.accept(TestVisitor.INSTANCE), "visited");
  }

  /**
   * Overridden because there is no Fudge builder for the yield convention.
   */
  @Override
  @Test(dataProvider = "propertyValues")
  protected <TYPE extends Bean> void testCycle(final JodaBeanProperties<TYPE> properties) {
    final TYPE security = constructAndPopulateBeanBuilder(properties).build();
    assertEquals(security, cycleObjectJodaXml(properties.getType(), security));
  }

  private static class TestVisitor extends FinancialSecurityVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
      return "not visited";
    }

    @Override
    public String visitBillSecurity(final BillSecurity security) {
      return "visited";
    }

  }

}
