/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.convention;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link FXSpotConvention}.
 */
@Test(groups = TestGroup.UNIT)
public class FXSpotConventionTest extends AbstractBeanTestCase {
  private static final String NAME = "name";
  private static final ExternalIdBundle IDS = ExternalIdBundle.of("conv", "USD/CAD FX SPOT");
  private static final int SETTLEMENT_DAYS = 1;
  private static final boolean USE_INTERMEDIATE_HOLIDAYS = true;
  private static final FinancialConvention CONVENTION = new FXSpotConvention(NAME, IDS, SETTLEMENT_DAYS, USE_INTERMEDIATE_HOLIDAYS);

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(FXSpotConvention.class, Arrays.asList("name", "externalIdBundle", "settlementDays", "useIntermediateUsHolidays"),
        Arrays.asList(NAME, IDS, SETTLEMENT_DAYS, USE_INTERMEDIATE_HOLIDAYS),
        Arrays.asList("other", ExternalIdBundle.of("conv", "USD/CHF FX SPOT"), SETTLEMENT_DAYS + 1, !USE_INTERMEDIATE_HOLIDAYS));
  }

  /**
   * Tests the returned type.
   */
  public void testType() {
    assertEquals(CONVENTION.getConventionType(), FXSpotConvention.TYPE);
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
    public String visitFXSpotConvention(final FXSpotConvention convention) {
      return "visited";
    }

  }

}
