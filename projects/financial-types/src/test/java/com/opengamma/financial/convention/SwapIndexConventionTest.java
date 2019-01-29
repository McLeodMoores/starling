/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.convention;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;
import org.threeten.bp.LocalTime;

import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link SwapIndexConvention}.
 */
@Test(groups = TestGroup.UNIT)
public class SwapIndexConventionTest extends AbstractBeanTestCase {
  private static final String NAME = "name";
  private static final ExternalIdBundle IDS = ExternalIdBundle.of("conv", "US SWAP INDEX");
  private static final LocalTime FIXING_TIME = LocalTime.of(11, 0);
  private static final ExternalId SWAP_CONVENTION = ExternalId.of("conv", "US SWAP");
  private static final FinancialConvention CONVENTION = new SwapIndexConvention(NAME, IDS, FIXING_TIME, SWAP_CONVENTION);

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(SwapIndexConvention.class, Arrays.asList("name", "externalIdBundle", "fixingTime", "swapConvention"),
        Arrays.asList(NAME, IDS, FIXING_TIME, SWAP_CONVENTION),
        Arrays.asList("other", ExternalIdBundle.of("conv", "SWAP"), FIXING_TIME.plusHours(1), ExternalId.of("conv", "SWAP")));
  }

  /**
   * Tests the returned type.
   */
  public void testType() {
    assertEquals(CONVENTION.getConventionType(), SwapIndexConvention.TYPE);
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
    public String visitSwapIndexConvention(final SwapIndexConvention convention) {
      return "visited";
    }

  }

}
