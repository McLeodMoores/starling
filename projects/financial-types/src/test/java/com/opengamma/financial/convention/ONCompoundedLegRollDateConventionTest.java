/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.convention;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Tests for {@link ONCompoundedLegRollDateConvention}.
 */
@Test(groups = TestGroup.UNIT)
public class ONCompoundedLegRollDateConventionTest extends AbstractBeanTestCase {
  private static final String NAME = "name";
  private static final ExternalIdBundle EIDS = ExternalIdBundle.of("eid", "1");
  private static final ExternalId ON_INDEX_CONVENTION = ExternalId.of("eid", "10");
  private static final Tenor PAYMENT_TENOR = Tenor.SIX_MONTHS;
  private static final StubType STUB_TYPE = StubType.LONG_END;
  private static final boolean IS_EXCHANGE_NOTIONAL = true;
  private static final int PAYMENT_LAG = 4;
  private static final ONCompoundedLegRollDateConvention CONVENTION = new ONCompoundedLegRollDateConvention(NAME, EIDS, ON_INDEX_CONVENTION, PAYMENT_TENOR,
      STUB_TYPE, IS_EXCHANGE_NOTIONAL, PAYMENT_LAG);

  @Override
  public JodaBeanProperties<ONCompoundedLegRollDateConvention> getJodaBeanProperties() {
    return new JodaBeanProperties<>(ONCompoundedLegRollDateConvention.class,
        Arrays.asList("name", "externalIdBundle", "overnightIndexConvention", "paymentTenor", "stubType", "isExchangeNotional", "paymentLag"),
        Arrays.<Object> asList(NAME, EIDS, ON_INDEX_CONVENTION, PAYMENT_TENOR, STUB_TYPE, IS_EXCHANGE_NOTIONAL, PAYMENT_LAG), Arrays.<Object> asList("other",
            ExternalIdBundle.of("eid", "2"), ExternalId.of("eid", "20"), Tenor.THREE_MONTHS, StubType.NONE, !IS_EXCHANGE_NOTIONAL, PAYMENT_LAG + 1));
  }

  /**
   * Tests the returned type.
   */
  public void testType() {
    assertEquals(CONVENTION.getConventionType(), ONCompoundedLegRollDateConvention.TYPE);
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
    public String visitONCompoundedLegRollDateConvention(final ONCompoundedLegRollDateConvention convention) {
      return "visited";
    }

  }
}
