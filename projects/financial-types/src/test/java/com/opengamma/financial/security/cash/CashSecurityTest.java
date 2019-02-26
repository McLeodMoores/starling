/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.cash;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.id.ExternalId;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests for {@link CashSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class CashSecurityTest extends AbstractBeanTestCase {
  private static final Currency CCY = Currency.USD;
  private static final ExternalId REGION = ExternalSchemes.countryRegionId(Country.US);
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2019, 1, 1);
  private static final ZonedDateTime MATURITY_DATE = DateUtils.getUTCDate(2020, 1, 1);
  private static final DayCount DC = DayCounts.ACT_360;
  private static final double RATE = 0.001;
  private static final double NOTIONAL = 1000000;
  private static final CashSecurity SECURITY = new CashSecurity(CCY, REGION, START_DATE, MATURITY_DATE, DC, RATE, NOTIONAL);

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(CashSecurity.class,
        Arrays.asList("securityType", "currency", "regionId", "start", "maturity", "dayCount", "rate", "amount"),
        Arrays.asList(CashSecurity.SECURITY_TYPE, CCY, REGION, START_DATE, MATURITY_DATE, DC, RATE, NOTIONAL), Arrays.asList(CashBalanceSecurity.SECURITY_TYPE,
            Currency.CAD, ExternalSchemes.countryRegionId(Country.CA), MATURITY_DATE, START_DATE, DayCounts.ACT_365, RATE + 1, NOTIONAL * 10));
  }

  /**
   * Tests the security type string.
   */
  public void testSecurityType() {
    assertEquals(SECURITY.getSecurityType(), CashSecurity.SECURITY_TYPE);
  }

  /**
   * Tests that the accept() method points to the correct method in the visitor.
   */
  public void testVisitor() {
    assertEquals(SECURITY.accept(TestVisitor.INSTANCE), "visited");
  }

  /**
   *
   */
  private static final class TestVisitor extends FinancialSecurityVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitCashSecurity(final CashSecurity security) {
      return "visited";
    }

  }

}
