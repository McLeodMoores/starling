/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.convention;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link BondConvention}.
 */
@Test(groups = TestGroup.UNIT)
public class BondConventionTest extends AbstractBeanTestCase {
  private static final String NAME = "name";
  private static final ExternalIdBundle EIDS = ExternalIdBundle.of("eid", "1");
  private static final int EX_DIVIDEND_DAYS = 7;
  private static final int SETTLEMENT_DAYS = 2;
  private static final BusinessDayConvention BDC = BusinessDayConventions.FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final boolean IS_CALCULATE_FROM_MATURITY = true;
  private static final BondConvention CONVENTION = new BondConvention(NAME, EIDS, EX_DIVIDEND_DAYS, SETTLEMENT_DAYS, BDC, IS_EOM, IS_CALCULATE_FROM_MATURITY);

  @Override
  public JodaBeanProperties<BondConvention> getJodaBeanProperties() {
    return new JodaBeanProperties<>(BondConvention.class,
        Arrays.asList("name", "externalIdBundle", "exDividendDays", "settlementDays", "businessDayConvention", "isEOM", "isCalculateScheduleFromMaturity"),
        Arrays.asList(NAME, EIDS, EX_DIVIDEND_DAYS, SETTLEMENT_DAYS, BDC, IS_EOM, IS_CALCULATE_FROM_MATURITY),
        Arrays.asList("other", ExternalIdBundle.of("eid", "2"), EX_DIVIDEND_DAYS + 1, SETTLEMENT_DAYS + 1, BusinessDayConventions.MODIFIED_FOLLOWING, !IS_EOM,
            !IS_CALCULATE_FROM_MATURITY));
  }

  /**
   * Tests the returned type.
   */
  public void testType() {
    assertEquals(CONVENTION.getConventionType(), BondConvention.TYPE);
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
    public String visitBondConvention(final BondConvention convention) {
      return "visited";
    }

  }
}
