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
 * Tests for {@link EquityConvention}.
 */
@Test(groups = TestGroup.UNIT)
public class EquityConventionTest extends AbstractBeanTestCase {
  private static final String NAME = "name";
  private static final ExternalIdBundle IDS = ExternalIdBundle.of("conv", "US EQUITY");
  private static final int EX_DIVIDEND_DAYS = 2;
  private static final FinancialConvention CONVENTION = new EquityConvention(NAME, IDS, EX_DIVIDEND_DAYS);

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(EquityConvention.class,
        Arrays.asList("name", "externalIdBundle", "exDividendPeriod"),
        Arrays.asList(NAME, IDS, EX_DIVIDEND_DAYS),
        Arrays.asList("other", ExternalIdBundle.of("conv", "AU DEPOSIT"), EX_DIVIDEND_DAYS + 1));
  }

  /**
   * Tests the returned type.
   */
  public void testType() {
    assertEquals(CONVENTION.getConventionType(), EquityConvention.TYPE);
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
    public String visitEquityConvention(final EquityConvention convention) {
      return "visited";
    }

  }

}
