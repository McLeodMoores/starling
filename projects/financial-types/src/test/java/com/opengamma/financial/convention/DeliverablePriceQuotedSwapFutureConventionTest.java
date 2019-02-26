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
 * Tests for {@link DeliverablePriceQuotedSwapFutureConvention}.
 */
@Test(groups = TestGroup.UNIT)
public class DeliverablePriceQuotedSwapFutureConventionTest extends AbstractBeanTestCase {
  private static final String NAME = "name";
  private static final ExternalIdBundle IDS = ExternalIdBundle.of("conv", "US IMM SWAP FUTURE");
  private static final ExternalId EXPIRY_CONVENTION = ExternalId.of("conv", "US IMM FUTURE");
  private static final ExternalId EXCHANGE_CALENDAR = ExternalSchemes.countryRegionId(Country.US);
  private static final ExternalId SWAP_CONVENTION = ExternalId.of("conv", "US IMM SWAP");
  private static final double NOTIONAL = 100000;
  private static final FinancialConvention CONVENTION = new DeliverablePriceQuotedSwapFutureConvention(NAME, IDS, EXPIRY_CONVENTION, EXCHANGE_CALENDAR,
      SWAP_CONVENTION, NOTIONAL);

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(DeliverablePriceQuotedSwapFutureConvention.class,
        Arrays.asList("name", "externalIdBundle", "expiryConvention", "exchangeCalendar", "swapConvention", "notional"),
        Arrays.asList(NAME, IDS, EXPIRY_CONVENTION, EXCHANGE_CALENDAR, SWAP_CONVENTION, NOTIONAL),
        Arrays.asList("other", ExternalIdBundle.of("conv", "IMM SWAP"), EXCHANGE_CALENDAR, SWAP_CONVENTION, EXPIRY_CONVENTION, NOTIONAL * 2));
  }

  /**
   * Tests the returned type.
   */
  public void testType() {
    assertEquals(CONVENTION.getConventionType(), DeliverablePriceQuotedSwapFutureConvention.TYPE);
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
    public String visitDeliverablePriceQuotedSwapFutureConvention(final DeliverablePriceQuotedSwapFutureConvention convention) {
      return "visited";
    }

  }

}
