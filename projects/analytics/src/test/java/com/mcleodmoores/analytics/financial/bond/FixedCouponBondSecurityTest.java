/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.bond;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.legalentity.CreditRating;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.Region;
import com.opengamma.analytics.financial.legalentity.Sector;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link FixedCouponBondSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class FixedCouponBondSecurityTest {
  private static final Currency CCY = Currency.EUR;
  private static final PaymentFixed PAYMENT_1 = new PaymentFixed(CCY, 0.05, 1.04);
  private static final PaymentFixed PAYMENT_2 = new PaymentFixed(CCY, 0.025, 1.05);
  private static final PaymentFixed PAYMENT_3 = new PaymentFixed(CCY, 0.15, 1.06);
  private static final CouponFixed COUPON_1 = new CouponFixed(CCY, 0.05, 0.049, 0.01);
  private static final CouponFixed COUPON_2 = new CouponFixed(CCY, 0.025, 0.049, 0.01);
  private static final CouponFixed COUPON_3 = new CouponFixed(CCY, 0.035, 0.049, 0.01);
  private static final CouponFixed COUPON_4 = new CouponFixed(CCY, 0.015, 0.049, 0.01);
  private static final CouponFixed COUPON_5 = new CouponFixed(CCY, 0.005, 0.049, 0.01);
  private static final LegalEntity ENTITY = new LegalEntity("A", "AB", Collections.<CreditRating>emptySet(), Sector.of("S"), Region.of("G"), false);
  private static final double EPS = 1e-15;

  /**
   * The legal entity cannot be set to null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullLegalEntity() {
    FixedCouponBondSecurity.builder().withLegalEntity(null);
  }

  /**
   * The yield convention cannot be set to null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullYieldConvention() {
    FixedCouponBondSecurity.builder().withYieldConvention(null);
  }

  /**
   * The settlement time cannot be negative.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeSettlementTime() {
    FixedCouponBondSecurity.builder().withSettlementTime(-0.03);
  }

  /**
   * The accrued interest cannot be negative.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeAccruedInterest() {
    FixedCouponBondSecurity.builder().withAccruedInterest(-100);
  }

  /**
   * The number of coupons per year cannot be negative.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeCouponsPerYear() {
    FixedCouponBondSecurity.builder().withCouponsPerYear(-1);
  }

  /**
   * The number of coupons per year cannot be zero.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZeroCouponsPerYear() {
    FixedCouponBondSecurity.builder().withCouponsPerYear(0);
  }

  /**
   * The accrual factor to the next coupon cannot be negative.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeAccrualFactor() {
    FixedCouponBondSecurity.builder().withAccrualFactorToNextCoupon(-1);
  }

  /**
   * The payment cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPayment() {
    FixedCouponBondSecurity.builder().withNominal(null);
  }

  /**
   * The payment array cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPaymentArray() {
    FixedCouponBondSecurity.builder().withNominals((PaymentFixed[]) null);
  }

  /**
   * The payment list cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPaymentList() {
    FixedCouponBondSecurity.builder().withNominals((List<PaymentFixed>) null);
  }

  /**
   * The coupon cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCoupon() {
    FixedCouponBondSecurity.builder().withCoupon(null);
  }

  /**
   * The coupon array cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCouponArray() {
    FixedCouponBondSecurity.builder().withCoupons((CouponFixed[]) null);
  }

  /**
   * The coupon list cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCouponList() {
    FixedCouponBondSecurity.builder().withCoupons((List<CouponFixed>) null);
  }

  /**
   * The nominals must be set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testNominalSet() {
    FixedCouponBondSecurity.builder()
        .withAccrualFactorToNextCoupon(0.01)
        .withAccruedInterest(0.1)
        .withCoupon(COUPON_1)
        .withCouponsPerYear(2)
        .withLegalEntity(ENTITY)
        .withSettlementTime(0.04)
        .withYieldConvention(BondConventionType.ITALIAN_TREASURY)
        .build();
  }

  /**
   * The coupons must be set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCouponsSet() {
    FixedCouponBondSecurity.builder()
      .withAccrualFactorToNextCoupon(0.01)
      .withAccruedInterest(0.1)
      .withCouponsPerYear(2)
      .withLegalEntity(ENTITY)
      .withNominal(PAYMENT_1)
      .withSettlementTime(0.04)
      .withYieldConvention(BondConventionType.ITALIAN_TREASURY)
      .build();
  }

  /**
   * The legal entity must be set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testLegalEntitySet() {
    FixedCouponBondSecurity.builder()
      .withAccrualFactorToNextCoupon(0.01)
      .withAccruedInterest(0.1)
      .withCoupon(COUPON_1)
      .withCouponsPerYear(2)
      .withNominal(PAYMENT_1)
      .withSettlementTime(0.04)
      .withYieldConvention(BondConventionType.ITALIAN_TREASURY)
      .build();
  }

  /**
   * The yield convention must be set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testYieldConventionSet() {
    FixedCouponBondSecurity.builder()
      .withAccrualFactorToNextCoupon(0.01)
      .withAccruedInterest(0.1)
      .withCoupon(COUPON_1)
      .withCouponsPerYear(2)
      .withLegalEntity(ENTITY)
      .withNominal(PAYMENT_1)
      .withSettlementTime(0.04)
      .build();
  }

  /**
   * The settlement time must be set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testSettlementTimeSet() {
    FixedCouponBondSecurity.builder()
      .withAccrualFactorToNextCoupon(0.01)
      .withAccruedInterest(0.1)
      .withCoupon(COUPON_1)
      .withCouponsPerYear(2)
      .withLegalEntity(ENTITY)
      .withNominal(PAYMENT_1)
      .withYieldConvention(BondConventionType.ITALIAN_TREASURY)
      .build();
  }

  /**
   * The accrued interest must be set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testAccruedInterestSet() {
    FixedCouponBondSecurity.builder()
      .withAccrualFactorToNextCoupon(0.01)
      .withCoupon(COUPON_1)
      .withCouponsPerYear(2)
      .withLegalEntity(ENTITY)
      .withNominal(PAYMENT_1)
      .withSettlementTime(0.04)
      .withYieldConvention(BondConventionType.ITALIAN_TREASURY)
      .build();
  }

  /**
   * The coupons per year must be set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCouponsPerYearSet() {
    FixedCouponBondSecurity.builder()
      .withAccrualFactorToNextCoupon(0.01)
      .withAccruedInterest(0.1)
      .withCoupon(COUPON_1)
      .withLegalEntity(ENTITY)
      .withNominal(PAYMENT_1)
      .withSettlementTime(0.04)
      .withYieldConvention(BondConventionType.ITALIAN_TREASURY)
      .build();
  }

  /**
   * The accrual factor to next coupon must be set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testAccrualFactorSet() {
    FixedCouponBondSecurity.builder()
      .withAccruedInterest(0.1)
      .withCoupon(COUPON_1)
      .withCouponsPerYear(2)
      .withLegalEntity(ENTITY)
      .withNominal(PAYMENT_1)
      .withSettlementTime(0.04)
      .withYieldConvention(BondConventionType.ITALIAN_TREASURY)
      .build();
  }

  /**
   * Tests that the nominals are sorted by payment times.
   */
  @Test
  public void testNominalsSorted() {
    final PaymentFixed[] expected = new PaymentFixed[] {PAYMENT_2, PAYMENT_1, PAYMENT_3};
    FixedCouponBondSecurity bond;
    bond = FixedCouponBondSecurity.builder()
        .withAccruedInterest(0.1)
        .withAccrualFactorToNextCoupon(0.04)
        .withCoupon(COUPON_1)
        .withCouponsPerYear(2)
        .withLegalEntity(ENTITY)
        .withNominal(PAYMENT_1)
        .withNominal(PAYMENT_2)
        .withNominal(PAYMENT_3)
        .withSettlementTime(0.04)
        .withYieldConvention(BondConventionType.ITALIAN_TREASURY)
        .build();
    assertArrayEquals(bond.getNominalPayments().getPayments(), expected);
    bond = FixedCouponBondSecurity.builder()
        .withAccruedInterest(0.1)
        .withAccrualFactorToNextCoupon(0.04)
        .withCoupon(COUPON_1)
        .withCouponsPerYear(2)
        .withLegalEntity(ENTITY)
        .withNominals(new PaymentFixed[] {PAYMENT_1, PAYMENT_2, PAYMENT_3})
        .withSettlementTime(0.04)
        .withYieldConvention(BondConventionType.ITALIAN_TREASURY)
        .build();
    assertArrayEquals(bond.getNominalPayments().getPayments(), expected);
    bond = FixedCouponBondSecurity.builder()
        .withAccruedInterest(0.1)
        .withAccrualFactorToNextCoupon(0.04)
        .withCoupon(COUPON_1)
        .withCouponsPerYear(2)
        .withLegalEntity(ENTITY)
        .withNominals(Arrays.asList(PAYMENT_1, PAYMENT_2, PAYMENT_3))
        .withSettlementTime(0.04)
        .withYieldConvention(BondConventionType.ITALIAN_TREASURY)
        .build();
      assertArrayEquals(bond.getNominalPayments().getPayments(), expected);
  }

  /**
   * Tests that the coupons are sorted by payment times.
   */
  @Test
  public void testCouponsSorted() {
    final CouponFixed[] expected = new CouponFixed[] {COUPON_5, COUPON_4, COUPON_2, COUPON_3, COUPON_1};
    FixedCouponBondSecurity bond;
    bond = FixedCouponBondSecurity.builder()
        .withAccruedInterest(0.1)
        .withAccrualFactorToNextCoupon(0.04)
        .withCoupon(COUPON_1)
        .withCoupon(COUPON_2)
        .withCoupon(COUPON_3)
        .withCoupon(COUPON_4)
        .withCoupon(COUPON_5)
        .withCouponsPerYear(2)
        .withLegalEntity(ENTITY)
        .withNominal(PAYMENT_1)
        .withSettlementTime(0.04)
        .withYieldConvention(BondConventionType.ITALIAN_TREASURY)
        .build();
    assertArrayEquals(bond.getCoupons().getPayments(), expected);
    bond = FixedCouponBondSecurity.builder()
        .withAccruedInterest(0.1)
        .withAccrualFactorToNextCoupon(0.04)
        .withCoupons(new CouponFixed[] {COUPON_1, COUPON_2, COUPON_3, COUPON_4, COUPON_5})
        .withCouponsPerYear(2)
        .withLegalEntity(ENTITY)
        .withNominal(PAYMENT_1)
        .withSettlementTime(0.04)
        .withYieldConvention(BondConventionType.ITALIAN_TREASURY)
        .build();
    assertArrayEquals(bond.getCoupons().getPayments(), expected);
    bond = FixedCouponBondSecurity.builder()
        .withAccruedInterest(0.1)
        .withAccrualFactorToNextCoupon(0.04)
        .withCoupons(Arrays.asList(COUPON_1, COUPON_2, COUPON_3, COUPON_4, COUPON_5))
        .withCouponsPerYear(2)
        .withLegalEntity(ENTITY)
        .withNominal(PAYMENT_1)
        .withSettlementTime(0.04)
        .withYieldConvention(BondConventionType.ITALIAN_TREASURY)
        .build();
      assertArrayEquals(bond.getCoupons().getPayments(), expected);
  }

  /**
   * Tests hashCode and equals.
   */
  @Test
  public void testHashCodeEquals() {
    final FixedCouponBondSecurity bond = FixedCouponBondSecurity.builder()
        .withAccruedInterest(0.1)
        .withAccrualFactorToNextCoupon(0.04)
        .withCoupon(COUPON_1)
        .withCouponsPerYear(2)
        .withLegalEntity(ENTITY)
        .withNominal(PAYMENT_1)
        .withSettlementTime(0.04)
        .withYieldConvention(BondConventionType.ITALIAN_TREASURY)
        .build();
    FixedCouponBondSecurity other;
    other = FixedCouponBondSecurity.builder()
        .withAccruedInterest(0.1)
        .withAccrualFactorToNextCoupon(0.04)
        .withCoupon(COUPON_1)
        .withCouponsPerYear(2)
        .withLegalEntity(ENTITY)
        .withNominal(PAYMENT_1)
        .withSettlementTime(0.04)
        .withYieldConvention(BondConventionType.ITALIAN_TREASURY)
        .build();
    assertEquals(bond, bond);
    assertEquals(bond, other);
    assertEquals(bond.hashCode(), other.hashCode());
    assertNotEquals(null, bond);
    assertNotEquals(new Object(), bond);
    other = FixedCouponBondSecurity.builder()
        .withAccruedInterest(0.2)
        .withAccrualFactorToNextCoupon(0.04)
        .withCoupon(COUPON_1)
        .withCouponsPerYear(2)
        .withLegalEntity(ENTITY)
        .withNominal(PAYMENT_1)
        .withSettlementTime(0.04)
        .withYieldConvention(BondConventionType.ITALIAN_TREASURY)
        .build();
    assertNotEquals(other, bond);
    other = FixedCouponBondSecurity.builder()
        .withAccruedInterest(0.1)
        .withAccrualFactorToNextCoupon(0.041)
        .withCoupon(COUPON_1)
        .withCouponsPerYear(2)
        .withLegalEntity(ENTITY)
        .withNominal(PAYMENT_1)
        .withSettlementTime(0.04)
        .withYieldConvention(BondConventionType.ITALIAN_TREASURY)
        .build();
    assertNotEquals(other, bond);
    other = FixedCouponBondSecurity.builder()
        .withAccruedInterest(0.1)
        .withAccrualFactorToNextCoupon(0.04)
        .withCoupon(COUPON_2)
        .withCouponsPerYear(2)
        .withLegalEntity(ENTITY)
        .withNominal(PAYMENT_1)
        .withSettlementTime(0.04)
        .withYieldConvention(BondConventionType.ITALIAN_TREASURY)
        .build();
    assertNotEquals(other, bond);
    other = FixedCouponBondSecurity.builder()
        .withAccruedInterest(0.1)
        .withAccrualFactorToNextCoupon(0.04)
        .withCoupon(COUPON_1)
        .withCouponsPerYear(1)
        .withLegalEntity(ENTITY)
        .withNominal(PAYMENT_1)
        .withSettlementTime(0.04)
        .withYieldConvention(BondConventionType.ITALIAN_TREASURY)
        .build();
    assertNotEquals(other, bond);
    other = FixedCouponBondSecurity.builder()
        .withAccruedInterest(0.1)
        .withAccrualFactorToNextCoupon(0.04)
        .withCoupon(COUPON_1)
        .withCouponsPerYear(2)
        .withLegalEntity(new LegalEntity("", "", null, null, null))
        .withNominal(PAYMENT_1)
        .withSettlementTime(0.04)
        .withYieldConvention(BondConventionType.ITALIAN_TREASURY)
        .build();
    assertNotEquals(other, bond);
    other = FixedCouponBondSecurity.builder()
        .withAccruedInterest(0.1)
        .withAccrualFactorToNextCoupon(0.04)
        .withCoupon(COUPON_1)
        .withCouponsPerYear(2)
        .withLegalEntity(ENTITY)
        .withNominal(PAYMENT_2)
        .withSettlementTime(0.04)
        .withYieldConvention(BondConventionType.ITALIAN_TREASURY)
        .build();
    assertNotEquals(other, bond);
    other = FixedCouponBondSecurity.builder()
        .withAccruedInterest(0.1)
        .withAccrualFactorToNextCoupon(0.04)
        .withCoupon(COUPON_1)
        .withCouponsPerYear(2)
        .withLegalEntity(ENTITY)
        .withNominal(PAYMENT_1)
        .withSettlementTime(0.041)
        .withYieldConvention(BondConventionType.ITALIAN_TREASURY)
        .build();
    assertNotEquals(other, bond);
    other = FixedCouponBondSecurity.builder()
        .withAccruedInterest(0.1)
        .withAccrualFactorToNextCoupon(0.04)
        .withCoupon(COUPON_1)
        .withCouponsPerYear(2)
        .withLegalEntity(ENTITY)
        .withNominal(PAYMENT_1)
        .withSettlementTime(0.04)
        .withYieldConvention(BondConventionType.FRANCE_COMPOUND)
        .build();
    assertNotEquals(other, bond);
  }

  /**
   * Tests the getters.
   */
  @Test
  public void testGetters() {
    final FixedCouponBondSecurity bond = FixedCouponBondSecurity.builder()
        .withAccruedInterest(0.1)
        .withAccrualFactorToNextCoupon(0.04)
        .withCoupon(COUPON_1)
        .withCouponsPerYear(2)
        .withLegalEntity(ENTITY)
        .withNominal(PAYMENT_1)
        .withSettlementTime(0.04)
        .withYieldConvention(BondConventionType.ITALIAN_TREASURY)
        .build();
    assertEquals(bond.getAccrualFactorToNextCoupon(), 0.04, EPS);
    assertEquals(bond.getAccruedInterest(), 0.1, EPS);
    assertEquals(bond.getCoupons(), new AnnuityCouponFixed(new CouponFixed[] {COUPON_1}));
    assertEquals(bond.getCouponsPerYear(), 2);
    assertEquals(bond.getLegalEntity(), ENTITY);
    assertEquals(bond.getNominalPayments(), new AnnuityPaymentFixed(new PaymentFixed[] {PAYMENT_1}));
    assertEquals(bond.getSettlementTime(), 0.04, EPS);
    assertEquals(bond.getYieldConventionType(), BondConventionType.ITALIAN_TREASURY);
  }

  /**
   * Tests toString.
   */
  @Test
  public void testToString() {
    final FixedCouponBondSecurity bond = FixedCouponBondSecurity.builder()
        .withAccruedInterest(0.1)
        .withAccrualFactorToNextCoupon(0.04)
        .withCoupon(COUPON_1)
        .withCoupon(COUPON_2)
        .withCouponsPerYear(2)
        .withLegalEntity(ENTITY)
        .withNominal(PAYMENT_1)
        .withNominal(PAYMENT_2)
        .withSettlementTime(0.04)
        .withYieldConvention(BondConventionType.ITALIAN_TREASURY)
        .build();
    final String s = "FixedCouponBondSecurity[settlementTime=0.04, couponsPerYear=2, accruedInterest=0.1, accrualFactorToNextCoupon=0.04, yieldConvention=ITALIAN_TREASURY, legalEntity=LegalEntity{ticker=A, shortName=AB, creditRatings=[], sector=Sector{name=S, classifications=FlexiBean{}}, region=Region{name=G, countries=[], currencies=[]}, hasDefaulted=false}"
    + "\nnotionalPayments=[Currency=EUR, payment time=0.025, amount = 1.05, Currency=EUR, payment time=0.05, amount = 1.04]"
    + "\ncouponPayments=[CouponFixed[currency=EUR, fixedRate=0.01, amount=4.9E-4, paymentYearFraction=0.049, notional=1.0, referenceAmount=1.0, paymentTime=0.025]"
    + "\n\t\t\t\tCouponFixed[currency=EUR, fixedRate=0.01, amount=4.9E-4, paymentYearFraction=0.049, notional=1.0, referenceAmount=1.0, paymentTime=0.05]]]";
    assertEquals(bond.toString(), s);
  }

  /**
   * Tests the visitor.
   */
  @Test
  public void testVisitor() {
    final FixedCouponBondSecurity bond = FixedCouponBondSecurity.builder()
        .withAccruedInterest(0.1)
        .withAccrualFactorToNextCoupon(0.04)
        .withCoupon(COUPON_1)
        .withCouponsPerYear(2)
        .withLegalEntity(ENTITY)
        .withNominal(PAYMENT_1)
        .withSettlementTime(0.04)
        .withYieldConvention(BondConventionType.ITALIAN_TREASURY)
        .build();
    assertEquals(bond.accept(TestBondVisitor.INSTANCE), -0.1, EPS);
    assertEquals(bond.accept(TestBondVisitor.INSTANCE, 10), 0.01, EPS);
  }
}