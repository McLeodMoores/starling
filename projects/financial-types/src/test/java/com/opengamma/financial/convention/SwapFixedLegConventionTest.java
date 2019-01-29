/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.convention;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

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
import com.opengamma.util.time.Tenor;

/**
 * Tests for {@link SwapFixedLegConvention}.
 */
@Test(groups = TestGroup.UNIT)
public class SwapFixedLegConventionTest extends AbstractBeanTestCase {
  private static final String NAME = "name";
  private static final ExternalIdBundle EIDS = ExternalIdBundle.of("eid", "1");
  private static final Tenor PAYMENT_TENOR = Tenor.SIX_MONTHS;
  private static final DayCount DC = DayCounts.ACT_360;
  private static final BusinessDayConvention BDC = BusinessDayConventions.FOLLOWING;
  private static final Currency CCY = Currency.USD;
  private static final ExternalId REGION = ExternalSchemes.countryRegionId(Country.US);
  private static final int SETTLEMENT_DAYS = 2;
  private static final boolean IS_EOM = true;
  private static final StubType STUB_TYPE = StubType.LONG_END;
  private static final boolean IS_EXCHANGE_NOTIONAL = false;
  private static final int PAYMENT_LAG = 3;
  private static final SwapFixedLegConvention CONVENTION = new SwapFixedLegConvention(NAME, EIDS, PAYMENT_TENOR, DC, BDC, CCY, REGION, SETTLEMENT_DAYS, IS_EOM,
      STUB_TYPE, IS_EXCHANGE_NOTIONAL, PAYMENT_LAG);

  @Override
  public JodaBeanProperties<SwapFixedLegConvention> getJodaBeanProperties() {
    return new JodaBeanProperties<>(SwapFixedLegConvention.class,
        Arrays.asList("name", "externalIdBundle", "paymentTenor", "dayCount", "businessDayConvention", "currency", "regionCalendar", "settlementDays", "isEOM",
            "stubType", "isExchangeNotional", "paymentLag"),
        Arrays.<Object> asList(NAME, EIDS, PAYMENT_TENOR, DC, BDC, CCY, REGION, SETTLEMENT_DAYS, IS_EOM, STUB_TYPE, IS_EXCHANGE_NOTIONAL, PAYMENT_LAG),
        Arrays.<Object> asList("other", ExternalIdBundle.of("eid", "2"), Tenor.THREE_MONTHS, DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING,
            Currency.AUD, ExternalSchemes.countryRegionId(Country.AU), SETTLEMENT_DAYS + 1, !IS_EOM, StubType.BOTH, !IS_EXCHANGE_NOTIONAL, PAYMENT_LAG + 1));
  }

  /**
   * Tests the returned type.
   */
  public void testType() {
    assertEquals(CONVENTION.getConventionType(), SwapFixedLegConvention.TYPE);
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
    public String visitSwapFixedLegConvention(final SwapFixedLegConvention convention) {
      return "visited";
    }

  }

}
