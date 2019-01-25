/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.bond;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.id.ExternalId;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Tests for {@link FloatingRateNoteSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class FloatingRateNoteSecurityTest extends AbstractBeanTestCase {
  private static final Currency CURRENCY = Currency.USD;
  private static final Expiry MATURITY_DATE = new Expiry(DateUtils.getUTCDate(2020, 10, 10));
  private static final ZonedDateTime ISSUE_DATE = DateUtils.getUTCDate(2018, 10, 10);
  private static final double MINIMUM_INCREMENT = 1000;
  private static final int SETTLEMENT_DAYS = 2;
  private static final int RESET_DAYS = 3;
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final ExternalId REGION = ExternalSchemes.countryRegionId(Country.US);
  private static final ExternalId LEGAL_ENTITY = ExternalId.of("len", "ABC");
  private static final ExternalId BENCHMARK = ExternalId.of("ind", "3M LIBOR");
  private static final double SPREAD = 0.03;
  private static final double LEVERAGE = 0.5;
  private static final Frequency FREQUENCY = SimpleFrequency.QUARTERLY;

  @DataProvider(name = "propertyValues")
  @Override
  public Object[][] propertyValues() {
    return new Object[][] { { new JodaBeanProperties<>(FloatingRateNoteSecurity.class,
        Arrays.asList("currency", "maturityDate", "issueDate", "minimumIncrement", "daysToSettle", "resetDays", "dayCount", "regionId", "legalEntityId",
            "benchmarkRateId", "spread", "leverageFactor", "couponFrequency"),
        Arrays.asList(CURRENCY, MATURITY_DATE, ISSUE_DATE, MINIMUM_INCREMENT, SETTLEMENT_DAYS, RESET_DAYS, DAY_COUNT, REGION, LEGAL_ENTITY, BENCHMARK, SPREAD,
            LEVERAGE, FREQUENCY),
        Arrays.asList(Currency.AUD, new Expiry(DateUtils.getUTCDate(2021, 10, 10)), ISSUE_DATE.plusDays(1), MINIMUM_INCREMENT + 1, SETTLEMENT_DAYS + 1,
            RESET_DAYS + 1, DayCounts.ACT_365, LEGAL_ENTITY, BENCHMARK, REGION, SPREAD + 0.01, LEVERAGE + 0.3, SimpleFrequency.ANNUAL)) } };
  }

  /**
   * Tests the default value of the leverage.
   */
  public void testLeverageDefault() {
    final FloatingRateNoteSecurity security = new FloatingRateNoteSecurity(CURRENCY, MATURITY_DATE, ISSUE_DATE, MINIMUM_INCREMENT, SETTLEMENT_DAYS, RESET_DAYS,
        DAY_COUNT, REGION, LEGAL_ENTITY, BENCHMARK, SPREAD, FREQUENCY);
    assertEquals(security.getLeverageFactor(), 1.);
  }

  /**
   * Tests that the accept() method points to the correct method in the visitor.
   */
  public void testVisitor() {
    final FloatingRateNoteSecurity security = new FloatingRateNoteSecurity(CURRENCY, MATURITY_DATE, ISSUE_DATE, MINIMUM_INCREMENT, SETTLEMENT_DAYS, RESET_DAYS,
        DAY_COUNT, REGION, LEGAL_ENTITY, BENCHMARK, SPREAD, LEVERAGE, FREQUENCY);
    assertEquals(security.accept(TestVisitor.INSTANCE), "visited");
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
    public String visitFloatingRateNoteSecurity(final FloatingRateNoteSecurity security) {
      return "visited";
    }

  }

}
