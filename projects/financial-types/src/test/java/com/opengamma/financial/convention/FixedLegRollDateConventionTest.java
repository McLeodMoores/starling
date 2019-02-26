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
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Tests for {@link FixedLegRollDateConvention}.
 */
@Test(groups = TestGroup.UNIT)
public class FixedLegRollDateConventionTest extends AbstractBeanTestCase {
  private static final String NAME = "name";
  private static final ExternalIdBundle IDS = ExternalIdBundle.of("conv", "US IMM FIXED LEG");
  private static final Tenor PAYMENT_TENOR = Tenor.SIX_MONTHS;
  private static final DayCount DC = DayCounts.ACT_360;
  private static final Currency CCY = Currency.USD;
  private static final ExternalId REGION = ExternalSchemes.countryRegionId(Country.US);
  private static final StubType STUB_TYPE = StubType.LONG_END;
  private static final boolean IS_EXCHANGE_NOTIONAL = true;
  private static final int PAYMENT_LAG = 2;
  private static final FinancialConvention CONVENTION = new FixedLegRollDateConvention(NAME, IDS, PAYMENT_TENOR, DC, CCY, REGION, STUB_TYPE,
      IS_EXCHANGE_NOTIONAL, PAYMENT_LAG);

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(FixedLegRollDateConvention.class,
        Arrays.asList("name", "externalIdBundle", "paymentTenor", "dayCount", "currency", "regionCalendar", "stubType", "isExchangeNotional", "paymentLag"),
        Arrays.asList(NAME, IDS, PAYMENT_TENOR, DC, CCY, REGION, STUB_TYPE, IS_EXCHANGE_NOTIONAL, PAYMENT_LAG),
        Arrays.asList("other", ExternalIdBundle.of("conv", "IMM SWAP"), Tenor.THREE_MONTHS, DayCounts.ACT_365, Currency.AUD,
            ExternalSchemes.countryRegionId(Country.AU), StubType.BOTH, !IS_EXCHANGE_NOTIONAL, PAYMENT_LAG + 1));
  }

  /**
   * Tests the returned type.
   */
  public void testType() {
    assertEquals(CONVENTION.getConventionType(), FixedLegRollDateConvention.TYPE);
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
    public String visitFixedLegRollDateConvention(final FixedLegRollDateConvention convention) {
      return "visited";
    }

  }

}
