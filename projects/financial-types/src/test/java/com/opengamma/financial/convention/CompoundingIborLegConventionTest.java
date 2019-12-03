/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.convention;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.CompoundingType;
import com.opengamma.financial.AbstractBeanTestCase;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Tests for {@link CompoundingIborLegConvention}.
 */
@Test(groups = TestGroup.UNIT)
public class CompoundingIborLegConventionTest extends AbstractBeanTestCase {
  private static final String NAME = "name";
  private static final ExternalIdBundle EIDS = ExternalIdBundle.of("eid", "1");
  private static final ExternalId IBOR_INDEX_CONVENTION = ExternalId.of("eid", "10");
  private static final Tenor PAYMENT_TENOR = Tenor.SIX_MONTHS;
  private static final CompoundingType COMPOUNDING = CompoundingType.FLAT_COMPOUNDING;
  private static final Tenor COMPOSITION_TENOR = Tenor.THREE_MONTHS;
  private static final StubType STUB_TYPE = StubType.LONG_END;
  private static final boolean IS_EXCHANGE_NOTIONAL = true;
  private static final int SETTLEMENT_DAYS = 2;
  private static final boolean IS_EOM = true;
  private static final StubType LEG_STUB_TYPE = StubType.LONG_START;
  private static final int PAYMENT_LAG = 4;
  private static final CompoundingIborLegConvention CONVENTION = new CompoundingIborLegConvention(NAME, EIDS, IBOR_INDEX_CONVENTION, PAYMENT_TENOR, COMPOUNDING,
      COMPOSITION_TENOR, STUB_TYPE, SETTLEMENT_DAYS, IS_EOM, LEG_STUB_TYPE, IS_EXCHANGE_NOTIONAL, PAYMENT_LAG);

  @Override
  public JodaBeanProperties<CompoundingIborLegConvention> getJodaBeanProperties() {
    return new JodaBeanProperties<>(CompoundingIborLegConvention.class,
        Arrays.asList("name", "externalIdBundle", "iborIndexConvention", "paymentTenor", "compoundingType", "compositionTenor", "stubTypeCompound",
            "settlementDays", "isEOM", "stubTypeLeg", "isExchangeNotional", "paymentLag"),
        Arrays.<Object> asList(NAME, EIDS, IBOR_INDEX_CONVENTION, PAYMENT_TENOR, COMPOUNDING, COMPOSITION_TENOR, STUB_TYPE, SETTLEMENT_DAYS, IS_EOM,
            LEG_STUB_TYPE, IS_EXCHANGE_NOTIONAL, PAYMENT_LAG),
        Arrays.<Object> asList("other", ExternalIdBundle.of("eid", "2"), ExternalId.of("eid", "20"), Tenor.THREE_MONTHS, CompoundingType.COMPOUNDING,
            Tenor.ONE_MONTH, StubType.NONE, SETTLEMENT_DAYS + 1, !IS_EOM, StubType.LONG_END, !IS_EXCHANGE_NOTIONAL, PAYMENT_LAG + 1));
  }

  /**
   * Tests the returned type.
   */
  public void testType() {
    assertEquals(CONVENTION.getConventionType(), CompoundingIborLegConvention.TYPE);
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
    public String visitCompoundingIborLegConvention(final CompoundingIborLegConvention convention) {
      return "visited";
    }

  }
}
