/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.convention;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link RollDateFRAConvention}.
 */
@Test(groups = TestGroup.UNIT)
public class RollDateFRAConventionTest extends AbstractBeanTestCase {
  private static final String NAME = "name";
  private static final ExternalIdBundle IDS = ExternalIdBundle.of("conv", "US IMM FRA");
  private static final ExternalId INDEX_CONVENTION = ExternalId.of("conv", "USD 3M LIBOR");
  private static final ExternalId ROLL_DATE_CONVENTION = ExternalId.of("conv", "IMM");
  private static final FinancialConvention CONVENTION = new RollDateFRAConvention(NAME, IDS, INDEX_CONVENTION, ROLL_DATE_CONVENTION);

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(RollDateFRAConvention.class, Arrays.asList("name", "externalIdBundle", "indexConvention", "rollDateConvention"),
        Arrays.asList(NAME, IDS, INDEX_CONVENTION, ROLL_DATE_CONVENTION),
        Arrays.asList("other", ExternalIdBundle.of("conv", "IMM FRA"), ROLL_DATE_CONVENTION, INDEX_CONVENTION));
  }

  /**
   * Tests the returned type.
   */
  public void testType() {
    assertEquals(CONVENTION.getConventionType(), RollDateFRAConvention.TYPE);
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
    public String visitIMMFRAConvention(final RollDateFRAConvention convention) {
      return "visited";
    }

  }

}
