/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.convention;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;
import org.threeten.bp.Period;

import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Tests for {@link CMSLegConvention}.
 */
@Test(groups = TestGroup.UNIT)
public class CMSLegConventionTest extends AbstractBeanTestCase {
  private static final String NAME = "name";
  private static final ExternalIdBundle EIDS = ExternalIdBundle.of("eid", "1");
  private static final ExternalId SWAP_INDEX_CONVENTION = ExternalId.of("eid", "10");
  private static final Tenor PAYMENT_TENOR = Tenor.of(Period.ofMonths(6));
  private static final boolean IS_ADVANCE_FIXING = true;
  private static final CMSLegConvention CONVENTION = new CMSLegConvention(NAME, EIDS, SWAP_INDEX_CONVENTION, PAYMENT_TENOR, IS_ADVANCE_FIXING);

  @Override
  public JodaBeanProperties<CMSLegConvention> getJodaBeanProperties() {
    return new JodaBeanProperties<>(CMSLegConvention.class,
        Arrays.asList("name", "externalIdBundle", "swapIndexConvention", "paymentTenor", "isAdvanceFixing"),
        Arrays.<Object> asList(NAME, EIDS, SWAP_INDEX_CONVENTION, PAYMENT_TENOR, IS_ADVANCE_FIXING),
        Arrays.<Object> asList("other", ExternalIdBundle.of("eid", "2"), ExternalId.of("eid", "20"), Tenor.THREE_MONTHS, !IS_ADVANCE_FIXING));
  }

  /**
   * Tests the returned type.
   */
  public void testType() {
    assertEquals(CONVENTION.getConventionType(), CMSLegConvention.TYPE);
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
    public String visitCMSLegConvention(final CMSLegConvention convention) {
      return "visited";
    }

  }
}
