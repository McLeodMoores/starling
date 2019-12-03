/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.bond;

import static com.mcleodmoores.analytics.financial.bond.BondConventionType.FRANCE_COMPOUND;
import static com.mcleodmoores.analytics.financial.bond.BondConventionType.ITALIAN_TREASURY;
import static com.mcleodmoores.analytics.financial.bond.BondConventionType.UK_BUMP_DMO;
import static com.mcleodmoores.analytics.financial.bond.BondConventionType.US_STREET;
import static com.mcleodmoores.analytics.financial.bond.TestBondConventionVisitor.INSTANCE;
import static org.testng.Assert.assertEquals;

import java.util.Collections;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.legalentity.CreditRating;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.Region;
import com.opengamma.analytics.financial.legalentity.Sector;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link BondConventionType}.
 */
@Test(groups = TestGroup.UNIT)
public class BondConventionTypeTest {
  private static final FixedCouponBondSecurity.Builder BOND = FixedCouponBondSecurity.builder()
      .withAccruedInterest(0.1)
      .withAccrualFactorToNextCoupon(0.04)
      .withCoupon(new CouponFixed(Currency.EUR, 0.05, 0.049, 0.01))
      .withCouponsPerYear(2)
      .withLegalEntity(new LegalEntity("A", "AB", Collections.<CreditRating>emptySet(), Sector.of("S"), Region.of("G"), false))
      .withNominal(new PaymentFixed(Currency.EUR, 0.05, 1.04))
      .withSettlementTime(0.04);

  /**
   * Tests a null visitor.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVisitor1() {
    FRANCE_COMPOUND.accept(null, BOND.withYieldConvention(FRANCE_COMPOUND).build());
  }

  /**
   * Tests a null visitor.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVisitor2() {
    FRANCE_COMPOUND.accept(null, BOND.withYieldConvention(FRANCE_COMPOUND).build(), "F");
  }

  /**
   * Tests a null visitor.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVisitor3() {
    ITALIAN_TREASURY.accept(null, BOND.withYieldConvention(ITALIAN_TREASURY).build());
  }

  /**
   * Tests a null visitor.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVisitor4() {
    ITALIAN_TREASURY.accept(null, BOND.withYieldConvention(ITALIAN_TREASURY).build(), "I");
  }

  /**
   * Tests a null visitor.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVisitor5() {
    UK_BUMP_DMO.accept(null, BOND.withYieldConvention(UK_BUMP_DMO).build());
  }

  /**
   * Tests a null visitor.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVisitor6() {
    UK_BUMP_DMO.accept(null, BOND.withYieldConvention(UK_BUMP_DMO).build(), "UK");
  }

  /**
   * Tests a null visitor.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVisitor7() {
    US_STREET.accept(null, BOND.withYieldConvention(US_STREET).build());
  }

  /**
   * Tests a null visitor.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVisitor8() {
    US_STREET.accept(null, BOND.withYieldConvention(US_STREET).build(), "US");
  }

  /**
   * Tests the visitor methods.
   */
  @Test
  public void testVisitor() {
    assertEquals(FRANCE_COMPOUND.accept(INSTANCE, BOND.withYieldConvention(FRANCE_COMPOUND).build()), "FRANCE_COMPOUND");
    assertEquals(FRANCE_COMPOUND.accept(INSTANCE, BOND.withYieldConvention(FRANCE_COMPOUND).build(), "1"), "FRANCE_COMPOUND 1");
    assertEquals(ITALIAN_TREASURY.accept(INSTANCE, BOND.withYieldConvention(ITALIAN_TREASURY).build()), "ITALIAN_TREASURY");
    assertEquals(ITALIAN_TREASURY.accept(INSTANCE, BOND.withYieldConvention(ITALIAN_TREASURY).build(), "1"), "ITALIAN_TREASURY 1");
    assertEquals(UK_BUMP_DMO.accept(INSTANCE, BOND.withYieldConvention(UK_BUMP_DMO).build()), "UK_BUMP_DMO");
    assertEquals(UK_BUMP_DMO.accept(INSTANCE, BOND.withYieldConvention(UK_BUMP_DMO).build(), "1"), "UK_BUMP_DMO 1");
    assertEquals(US_STREET.accept(INSTANCE, BOND.withYieldConvention(US_STREET).build()), "US_STREET");
    assertEquals(US_STREET.accept(INSTANCE, BOND.withYieldConvention(US_STREET).build(), "1"), "US_STREET 1");
  }
}
