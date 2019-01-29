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
 * Tests for {@link VanillaIborLegRollDateConvention}.
 */
@Test(groups = TestGroup.UNIT)
public class VanillaIborLegRollDateConventionTest extends AbstractBeanTestCase {
  private static final String NAME = "name";
  private static final ExternalIdBundle EIDS = ExternalIdBundle.of("eid", "1");
  private static final ExternalId IBOR_CONVENTION = ExternalId.of("eid", "2");
  private static final boolean IS_ADVANCE_FIXING = true;
  private static final Tenor RESET_TENOR = Tenor.SIX_MONTHS;
  private static final StubType STUB_TYPE = StubType.LONG_END;
  private static final boolean IS_EXCHANGE_NOTIONAL = false;
  private static final int PAYMENT_LAG = 3;
  private static final VanillaIborLegRollDateConvention CONVENTION = new VanillaIborLegRollDateConvention(NAME, EIDS, IBOR_CONVENTION, IS_ADVANCE_FIXING,
      RESET_TENOR, STUB_TYPE, IS_EXCHANGE_NOTIONAL, PAYMENT_LAG);

  @Override
  public JodaBeanProperties<VanillaIborLegRollDateConvention> getJodaBeanProperties() {
    return new JodaBeanProperties<>(VanillaIborLegRollDateConvention.class,
        Arrays.asList("name", "externalIdBundle", "iborIndexConvention", "isAdvanceFixing", "resetTenor", "stubType", "isExchangeNotional", "paymentLag"),
        Arrays.<Object> asList(NAME, EIDS, IBOR_CONVENTION, IS_ADVANCE_FIXING, RESET_TENOR, STUB_TYPE, IS_EXCHANGE_NOTIONAL, PAYMENT_LAG),
        Arrays.<Object> asList("other", ExternalIdBundle.of("eid", "2"), ExternalId.of("eid", "3"), !IS_ADVANCE_FIXING, Tenor.THREE_MONTHS, StubType.BOTH,
            !IS_EXCHANGE_NOTIONAL, PAYMENT_LAG + 1));
  }

  /**
   * Tests the returned type.
   */
  public void testType() {
    assertEquals(CONVENTION.getConventionType(), VanillaIborLegRollDateConvention.TYPE);
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
    public String visitVanillaIborLegRollDateConvention(final VanillaIborLegRollDateConvention convention) {
      return "visited";
    }

  }

}
