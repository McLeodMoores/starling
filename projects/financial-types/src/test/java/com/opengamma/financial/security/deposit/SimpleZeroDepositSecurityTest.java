/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.deposit;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.id.ExternalId;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests for {@link SimpleZeroDepositSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class SimpleZeroDepositSecurityTest extends AbstractBeanTestCase {
  private static final Currency CCY = Currency.USD;
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2019, 1, 1);
  private static final ZonedDateTime MATURITY_DATE = DateUtils.getUTCDate(2020, 1, 1);
  private static final double RATE = 0.001;
  private static final ExternalId REGION = ExternalSchemes.countryRegionId(Country.US);
  private static final SimpleZeroDepositSecurity SECURITY = new SimpleZeroDepositSecurity(CCY, START_DATE, MATURITY_DATE, RATE, REGION);

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(SimpleZeroDepositSecurity.class, Arrays.asList("securityType", "currency", "startDate", "maturityDate", "rate", "region"),
        Arrays.asList(SimpleZeroDepositSecurity.SECURITY_TYPE, CCY, START_DATE, MATURITY_DATE, RATE, REGION), Arrays.asList(
            ContinuousZeroDepositSecurity.SECURITY_TYPE, Currency.CAD, MATURITY_DATE, START_DATE, RATE + 1, ExternalSchemes.countryRegionId(Country.CA)));
  }

  /**
   * Tests the security type string.
   */
  public void testSecurityType() {
    assertEquals(SECURITY.getSecurityType(), SimpleZeroDepositSecurity.SECURITY_TYPE);
  }

  /**
   * Tests that the accept() method points to the correct method in the visitor.
   */
  public void testVisitor() {
    assertEquals(SECURITY.accept(TestVisitor.INSTANCE), "visited");
  }

  private static class TestVisitor extends FinancialSecurityVisitorAdapter<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitSimpleZeroDepositSecurity(final SimpleZeroDepositSecurity security) {
      return "visited";
    }

  }

}
