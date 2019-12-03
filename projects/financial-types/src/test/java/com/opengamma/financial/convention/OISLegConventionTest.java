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
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Tests for {@link OISLegConvention}.
 */
@Test(groups = TestGroup.UNIT)
public class OISLegConventionTest extends AbstractBeanTestCase {
  private static final String NAME = "name";
  private static final ExternalIdBundle EIDS = ExternalIdBundle.of("eid", "1");
  private static final ExternalId ON_INDEX_CONVENTION = ExternalId.of("eid", "10");
  private static final Tenor PAYMENT_TENOR = Tenor.SIX_MONTHS;
  private static final int PAYMENT_LAG = 4;
  private static final BusinessDayConvention BDC = BusinessDayConventions.FOLLOWING;
  private static final int SETTLEMENT_DAYS = 2;
  private static final boolean IS_EOM = true;
  private static final StubType STUB_TYPE = StubType.LONG_END;
  private static final boolean IS_EXCHANGE_NOTIONAL = true;
  private static final OISLegConvention CONVENTION = new OISLegConvention(NAME, EIDS, ON_INDEX_CONVENTION, PAYMENT_TENOR, BDC, SETTLEMENT_DAYS, IS_EOM,
      STUB_TYPE, IS_EXCHANGE_NOTIONAL, PAYMENT_LAG);

  @Override
  public JodaBeanProperties<OISLegConvention> getJodaBeanProperties() {
    return new JodaBeanProperties<>(OISLegConvention.class,
        Arrays.asList("name", "externalIdBundle", "overnightIndexConvention", "paymentTenor", "businessDayConvention", "settlementDays", "isEOM", "stubType",
            "isExchangeNotional", "paymentLag"),
        Arrays.<Object> asList(NAME, EIDS, ON_INDEX_CONVENTION, PAYMENT_TENOR, BDC, SETTLEMENT_DAYS, IS_EOM, STUB_TYPE, IS_EXCHANGE_NOTIONAL, PAYMENT_LAG),
        Arrays.<Object> asList("other", ExternalIdBundle.of("eid", "2"), ExternalId.of("eid", "20"), Tenor.THREE_MONTHS,
            BusinessDayConventions.MODIFIED_FOLLOWING, SETTLEMENT_DAYS + 1, !IS_EOM, StubType.NONE, !IS_EXCHANGE_NOTIONAL, PAYMENT_LAG + 1));
  }

  /**
   * Tests the returned type.
   */
  public void testType() {
    assertEquals(CONVENTION.getConventionType(), OISLegConvention.TYPE);
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
    public String visitOISLegConvention(final OISLegConvention convention) {
      return "visited";
    }

  }
}
