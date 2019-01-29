/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.convention;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link FXForwardAndSwapConvention}.
 */
@Test(groups = TestGroup.UNIT)
public class FXForwardAndSwapConventionTest extends AbstractBeanTestCase {
  private static final String NAME = "name";
  private static final ExternalIdBundle IDS = ExternalIdBundle.of("conv", "USD/CAD FX FORWARD");
  private static final ExternalId SPOT_CONVENTION = ExternalId.of("conv", "USD/CAD");
  private static final BusinessDayConvention BDC = BusinessDayConventions.FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final FinancialConvention CONVENTION = new FXForwardAndSwapConvention(NAME, IDS, SPOT_CONVENTION, BDC, IS_EOM);

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(FXForwardAndSwapConvention.class,
        Arrays.asList("name", "externalIdBundle", "spotConvention", "businessDayConvention", "isEOM"), Arrays.asList(NAME, IDS, SPOT_CONVENTION, BDC, IS_EOM),
        Arrays.asList("other", ExternalIdBundle.of("conv", "USD/CHF FX FORWARD"), ExternalId.of("conv", "USD/CHF"), BusinessDayConventions.MODIFIED_FOLLOWING,
            !IS_EOM));
  }

  /**
   * Tests the returned type.
   */
  public void testType() {
    assertEquals(CONVENTION.getConventionType(), FXForwardAndSwapConvention.TYPE);
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
    public String visitFXForwardAndSwapConvention(final FXForwardAndSwapConvention convention) {
      return "visited";
    }

  }

}
