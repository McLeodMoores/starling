/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.serialization.jodabeans;

import static com.mcleodmoores.integration.testutils.FinmathSerializationTestUtils.assertCurveEquals;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

import com.mcleodmoores.integration.serialization.CurveValueType;
import com.mcleodmoores.integration.serialization.jodabeans.DiscountCurveBean;
import com.opengamma.util.test.TestGroup;

import net.finmath.marketdata.model.curves.Curve.ExtrapolationMethod;
import net.finmath.marketdata.model.curves.Curve.InterpolationEntity;
import net.finmath.marketdata.model.curves.Curve.InterpolationMethod;

/**
 * Unit tests for {@link DiscountCurveBean}.
 */
@Test(groups = TestGroup.UNIT)
public class DiscountCurveBeanTest extends CurveBeanTest {
  /** The default interpolation method used in the Finmath library */
  private static final String DEFAULT_INTERPOLATION_METHOD = "LINEAR";
  /** The default extrapolation method used in the Finmath library */
  private static final String DEFAULT_EXTRAPOLATION_METHOD = "CONSTANT";
  /** The default interpolation entity used in the Finmath library */
  private static final String DEFAULT_INTERPOLATION_ENTITY = "LOG_OF_VALUE_PER_TIME";
  /** The discount curve name */
  private static final String NAME = "discount curve";
  /** The reference date string */
  private static final String REFERENCE_DATE_STRING = "2015-01-15";
  /** The times */
  private static final double[] TIMES = new double[] {1, 2, 3, 4};
  /** The values */
  private static final double[] VALUES = new double[] {0.01, 0.02, 0.03, 0.04};
  /** The parameter flags */
  private static final boolean[] IS_PARAMETER = new boolean[] {true, true, true, true};

  /**
   * Tests that the beans created with different constructors are not equal, even if the built
   * curves will be.
   */
  @Test
  public void testConstructors() {
    final String curveValueType = "ZERO_RATES";
    final DiscountCurveBean bean1 = new DiscountCurveBean(NAME, REFERENCE_DATE_STRING, TIMES, VALUES, IS_PARAMETER, curveValueType);
    final DiscountCurveBean bean2 = new DiscountCurveBean(NAME, REFERENCE_DATE_STRING, TIMES, VALUES, IS_PARAMETER, DEFAULT_INTERPOLATION_METHOD,
        DEFAULT_EXTRAPOLATION_METHOD, DEFAULT_INTERPOLATION_ENTITY, curveValueType);
    assertNotEquals(bean1, bean2);
  }

  /**
   * Tests that the input array sizes match.
   */
  @Test
  public void testInputArraySize() {
    try {
      new DiscountCurveBean(NAME, REFERENCE_DATE_STRING, TIMES, new double[] {0.01, 0.02}, IS_PARAMETER, CurveValueType.DISCOUNT_FACTORS.name());
      fail();
    } catch (final IllegalArgumentException e) {
      // expected
    }
    try {
      new DiscountCurveBean(NAME, REFERENCE_DATE_STRING, TIMES, VALUES, new boolean[] {true, true}, CurveValueType.DISCOUNT_FACTORS.name());
      fail();
    } catch (final IllegalArgumentException e) {
      // expected
    }
    try {
      new DiscountCurveBean(NAME, REFERENCE_DATE_STRING, TIMES, new double[] {0.01, 0.02}, IS_PARAMETER,
          InterpolationMethod.LINEAR.name(), ExtrapolationMethod.CONSTANT.name(), InterpolationEntity.LOG_OF_VALUE_PER_TIME.name(),
          CurveValueType.DISCOUNT_FACTORS.name());
      fail();
    } catch (final IllegalArgumentException e) {
      // expected
    }
    try {
      new DiscountCurveBean(NAME, REFERENCE_DATE_STRING, TIMES, VALUES, new boolean[] {true, true},
          InterpolationMethod.LINEAR.name(), ExtrapolationMethod.CONSTANT.name(), InterpolationEntity.LOG_OF_VALUE_PER_TIME.name(),
          CurveValueType.DISCOUNT_FACTORS.name());
      fail();
    } catch (final IllegalArgumentException e) {
      // expected
    }
  }

  @Test
  @Override
  public void testBuildCurve() {
    // all information
    final DiscountCurveBean discountFactorCurve1 = new DiscountCurveBean(NAME, REFERENCE_DATE_STRING, TIMES, VALUES, IS_PARAMETER,
        InterpolationMethod.LINEAR.name(), ExtrapolationMethod.CONSTANT.name(), InterpolationEntity.LOG_OF_VALUE_PER_TIME.name(),
        CurveValueType.DISCOUNT_FACTORS.name());
    // default interpolator names
    final DiscountCurveBean discountFactorCurve2 = new DiscountCurveBean(NAME, REFERENCE_DATE_STRING, TIMES, VALUES, IS_PARAMETER,
        CurveValueType.DISCOUNT_FACTORS.name());
    // null reference date
    final DiscountCurveBean discountFactorCurve3 = new DiscountCurveBean(NAME, null, TIMES, VALUES, IS_PARAMETER,
        CurveValueType.DISCOUNT_FACTORS.name());
    // null reference date, default interpolator names
    final DiscountCurveBean discountFactorCurve4 = new DiscountCurveBean(NAME, null, TIMES, VALUES, IS_PARAMETER,
        InterpolationMethod.LINEAR.name(), ExtrapolationMethod.CONSTANT.name(), InterpolationEntity.LOG_OF_VALUE_PER_TIME.name(),
        CurveValueType.DISCOUNT_FACTORS.name());
    assertCurveEquals(discountFactorCurve1.buildCurve(), discountFactorCurve2.buildCurve());
    assertCurveEquals(discountFactorCurve1.buildCurve(), discountFactorCurve3.buildCurve());
    assertCurveEquals(discountFactorCurve1.buildCurve(), discountFactorCurve4.buildCurve());
    // all information
    final DiscountCurveBean zeroRateCurve1 = new DiscountCurveBean(NAME, REFERENCE_DATE_STRING, TIMES, VALUES, IS_PARAMETER,
        InterpolationMethod.LINEAR.name(), ExtrapolationMethod.CONSTANT.name(), InterpolationEntity.LOG_OF_VALUE_PER_TIME.name(),
        CurveValueType.ZERO_RATES.name());
    // default interpolator names
    final DiscountCurveBean zeroRateCurve2 = new DiscountCurveBean(NAME, REFERENCE_DATE_STRING, TIMES, VALUES, IS_PARAMETER,
        CurveValueType.ZERO_RATES.name());
    // null reference date
    final DiscountCurveBean zeroRateCurve3 = new DiscountCurveBean(NAME, null, TIMES, VALUES, IS_PARAMETER,
        InterpolationMethod.LINEAR.name(), ExtrapolationMethod.CONSTANT.name(), InterpolationEntity.LOG_OF_VALUE_PER_TIME.name(),
        CurveValueType.ZERO_RATES.name());
    // null reference date, default interpolator names
    final DiscountCurveBean zeroRateCurve4 = new DiscountCurveBean(NAME, null, TIMES, VALUES, IS_PARAMETER,
        CurveValueType.ZERO_RATES.name());
    assertCurveEquals(zeroRateCurve1.buildCurve(), zeroRateCurve2.buildCurve());
    assertCurveEquals(zeroRateCurve1.buildCurve(), zeroRateCurve3.buildCurve());
    assertCurveEquals(zeroRateCurve1.buildCurve(), zeroRateCurve4.buildCurve());
  }

  @Override
  @Test
  public void testGetReferenceDate() {
    DiscountCurveBean curve = new DiscountCurveBean(NAME, null, TIMES, VALUES, IS_PARAMETER,
        CurveValueType.ZERO_RATES.name());
    assertNull(curve.getReferenceDate());
    curve = new DiscountCurveBean(NAME, REFERENCE_DATE_STRING, TIMES, VALUES, IS_PARAMETER, CurveValueType.ZERO_RATES.name());
    assertEquals(curve.getReferenceDate(), new LocalDate(2015, 1, 15));
  }

  /**
   * Tests the behaviour when the reference date string cannot be parsed.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMalformedReferenceDate() {
    final DiscountCurveBean curve = new DiscountCurveBean(NAME, "2015//01//15", TIMES, VALUES, IS_PARAMETER, CurveValueType.DISCOUNT_FACTORS.name());
    curve.getReferenceDate();
  }

  /**
   * Tests the behaviour when an unsupported curve value type is built.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testUnsupportedType() {
    final DiscountCurveBean curve = new DiscountCurveBean(NAME, null, TIMES, VALUES, IS_PARAMETER,
        CurveValueType.FORWARD_RATES.name());
    curve.buildCurve();
  }
}
