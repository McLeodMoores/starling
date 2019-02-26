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
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ExchangeTradedFutureAndOptionConvention}.
 */
@Test(groups = TestGroup.UNIT)
public class ExchangeTradedFutureAndOptionConventionTest extends AbstractBeanTestCase {
  private static final String NAME = "name";
  private static final ExternalIdBundle IDS = ExternalIdBundle.of("conv", "US NYSE FUTURE");
  private static final ExternalId EXPIRY_CONVENTION = ExternalId.of("conv", "3RD FRIDAY");
  private static final ExternalId EXCHANGE_CALENDAR = ExternalSchemes.countryRegionId(Country.US);
  private static final FinancialConvention CONVENTION = new ExchangeTradedFutureAndOptionConvention(NAME, IDS, EXPIRY_CONVENTION, EXCHANGE_CALENDAR);

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(ExchangeTradedFutureAndOptionConvention.class,
        Arrays.asList("name", "externalIdBundle", "expiryConvention", "exchangeCalendar"), Arrays.asList(NAME, IDS, EXPIRY_CONVENTION, EXCHANGE_CALENDAR),
        Arrays.asList("other", ExternalIdBundle.of("conv", "IMM SWAP"), EXCHANGE_CALENDAR, EXPIRY_CONVENTION));
  }

  /**
   * Tests the returned type.
   */
  public void testType() {
    assertEquals(CONVENTION.getConventionType(), ExchangeTradedFutureAndOptionConvention.TYPE);
  }

  /**
   * Tests that this convention does not have an accept() method for a FinancialConventionVisitor.
   */
  @Test(expectedExceptions = IllegalStateException.class)
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

  }

}
