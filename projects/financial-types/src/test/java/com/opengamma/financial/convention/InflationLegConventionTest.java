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
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link InflationLegConvention}.
 */
@Test(groups = TestGroup.UNIT)
public class InflationLegConventionTest extends AbstractBeanTestCase {
  private static final String NAME = "name";
  private static final ExternalIdBundle EIDS = ExternalIdBundle.of("eid", "1");
  private static final BusinessDayConvention BDC = BusinessDayConventions.FOLLOWING;
  private static final DayCount DC = DayCounts.ACT_360;
  private static final boolean IS_EOM = true;
  private static final int MONTH_LAG = 6;
  private static final int SPOT_LAG = 2;
  private static final ExternalId PRICE_INDEX_CONVENTION = ExternalId.of("eid", "2");
  private static final InflationLegConvention CONVENTION = new InflationLegConvention(NAME, EIDS, BDC, DC, IS_EOM, MONTH_LAG, SPOT_LAG, PRICE_INDEX_CONVENTION);

  @Override
  public JodaBeanProperties<InflationLegConvention> getJodaBeanProperties() {
    return new JodaBeanProperties<>(InflationLegConvention.class,
        Arrays.asList("name", "externalIdBundle", "businessDayConvention", "dayCount", "isEOM", "monthLag", "spotLag", "priceIndexConvention"),
        Arrays.<Object> asList(NAME, EIDS, BDC, DC, IS_EOM, MONTH_LAG, SPOT_LAG, PRICE_INDEX_CONVENTION),
        Arrays.<Object> asList("other", ExternalIdBundle.of("eid", "2"), BusinessDayConventions.MODIFIED_FOLLOWING, DayCounts.ACT_365, !IS_EOM, MONTH_LAG + 1,
            SPOT_LAG + 1, ExternalId.of("eid", "3")));
  }

  /**
   * Tests the returned type.
   */
  public void testType() {
    assertEquals(CONVENTION.getConventionType(), InflationLegConvention.TYPE);
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
    public String visitInflationLegConvention(final InflationLegConvention convention) {
      return "visited";
    }

  }
}
