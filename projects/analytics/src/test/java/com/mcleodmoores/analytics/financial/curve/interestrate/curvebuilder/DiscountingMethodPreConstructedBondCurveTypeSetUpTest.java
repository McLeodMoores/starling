/**
 * Copyright (C) 2020 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityShortName;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class DiscountingMethodPreConstructedBondCurveTypeSetUpTest {
  private static final UniqueIdentifiable CCY = Currency.GBP;
  private static final IborTypeIndex[] IBOR = new IborTypeIndex[] {
      new IborTypeIndex("3M", Currency.GBP, Tenor.THREE_MONTHS, 2, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, false),
      new IborTypeIndex("6M", Currency.GBP, Tenor.SIX_MONTHS, 2, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, false) };
  private static final OvernightIndex[] OVERNIGHT = new OvernightIndex[] { new OvernightIndex("SONIA", Currency.GBP, DayCounts.ACT_360, 1) };
  private static final Pair<Object, LegalEntityFilter<LegalEntity>> ISSUER = Pairs.<Object, LegalEntityFilter<LegalEntity>> of("US GOVT",
      new LegalEntityShortName());
  private static final DiscountingMethodBondCurveSetUp SETUP = new DiscountingMethodBondCurveSetUp();

  /**
   * Tests that the discounting id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDiscountingId() {
    new DiscountingMethodPreConstructedBondCurveTypeSetUp(SETUP).forDiscounting(null);
  }

  /**
   * Tests that the ibor indices cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIborIndices() {
    new DiscountingMethodPreConstructedBondCurveTypeSetUp(SETUP).forIndex((IborTypeIndex[]) null);
  }

  /**
   * Tests that the overnight indices cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullOvernightIndices() {
    new DiscountingMethodPreConstructedBondCurveTypeSetUp(SETUP).forIndex((OvernightIndex[]) null);
  }

  /**
   * Tests that the issuers cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIssuers() {
    new DiscountingMethodPreConstructedBondCurveTypeSetUp(SETUP).forIssuer(null);
  }

  /**
   * Test the getters.
   */
  @Test
  public void testGetters() {
    final DiscountingMethodPreConstructedBondCurveTypeSetUp setup = new DiscountingMethodPreConstructedBondCurveTypeSetUp(SETUP);
    setup.forDiscounting(CCY);
    setup.forIndex(IBOR);
    setup.forIndex(OVERNIGHT);
    setup.forIssuer(ISSUER);
    assertEquals(CCY, setup.getDiscountingCurveId());
    assertEquals(IBOR, setup.getIborCurveIndices().toArray());
    assertEquals(OVERNIGHT, setup.getOvernightCurveIndices().toArray());
    assertEquals(new Pair[] { ISSUER }, setup.getIssuers().toArray());
  }

  /**
   * Tests equals and hashCode.
   */
  @Test
  public void testEqualsHashCode() {
    final DiscountingMethodPreConstructedBondCurveTypeSetUp setup = new DiscountingMethodPreConstructedBondCurveTypeSetUp(SETUP);
    setup.forDiscounting(CCY);
    setup.forIndex(IBOR);
    setup.forIndex(OVERNIGHT);
    setup.forIssuer(ISSUER);
    DiscountingMethodPreConstructedBondCurveTypeSetUp other = new DiscountingMethodPreConstructedBondCurveTypeSetUp(SETUP);
    other.forDiscounting(CCY);
    other.forIndex(IBOR);
    other.forIndex(OVERNIGHT);
    other.forIssuer(ISSUER);
    assertEquals(setup, other);
    assertEquals(setup.hashCode(), other.hashCode());
    other = new DiscountingMethodPreConstructedBondCurveTypeSetUp(SETUP);
    other.forIndex(IBOR);
    other.forIndex(OVERNIGHT);
    other.forIssuer(ISSUER);
    assertNotEquals(other, setup);
    other = new DiscountingMethodPreConstructedBondCurveTypeSetUp(SETUP);
    other.forDiscounting(CCY);
    other.forIndex(OVERNIGHT);
    other.forIssuer(ISSUER);
    assertNotEquals(other, setup);
    other = new DiscountingMethodPreConstructedBondCurveTypeSetUp(SETUP);
    other.forDiscounting(CCY);
    other.forIndex(IBOR);
    other.forIssuer(ISSUER);
    assertNotEquals(other, setup);
    other = new DiscountingMethodPreConstructedBondCurveTypeSetUp(SETUP);
    other.forDiscounting(CCY);
    other.forIndex(IBOR);
    other.forIndex(OVERNIGHT);
    assertNotEquals(other, setup);
  }
}
