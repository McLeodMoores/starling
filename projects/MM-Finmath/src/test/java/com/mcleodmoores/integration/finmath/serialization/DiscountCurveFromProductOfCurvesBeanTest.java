/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.finmath.serialization;

import static com.mcleodmoores.integration.finmath.testutils.FinmathSerializationTestUtils.assertCurveEquals;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.DiscountCurveInterface;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Sets;
import com.mcleodmoores.integration.finmath.adapter.FinmathDateUtils;

/**
 * Units tests for {@link DiscountCurveFromProductOfCurvesBean}.
 */
@Test
public class DiscountCurveFromProductOfCurvesBeanTest extends CurveBeanTest {
  /** Times */
  private static final double[] TIMES = new double[] {1, 2, 3, 4};
  /** Discount factors */
  private static final double[] DISCOUNT_FACTORS = new double[] {0.9, 0.8, 0.7, 0.6};
  /** Reference date */
  private static final String REFERENCE_DATE = "2015-01-01";
  /** First curve */
  private static final DiscountCurve CURVE1 = DiscountCurve.createDiscountCurveFromDiscountFactors("Curve1", TIMES, DISCOUNT_FACTORS);
  /** Second curve */
  private static final DiscountCurve CURVE2 = DiscountCurve.createDiscountCurveFromDiscountFactors("Curve2", TIMES, DISCOUNT_FACTORS);
  /** Third curve */
  private static final DiscountCurve CURVE3 = DiscountCurve.createDiscountCurveFromDiscountFactors("Curve3", TIMES, DISCOUNT_FACTORS);

  @Override
  @Test
  public void testBuildCurve() {
    final DiscountCurveFromProductOfCurvesBean bean1 = new DiscountCurveFromProductOfCurvesBean("Curve4", REFERENCE_DATE,
        new DiscountCurveInterface[]{CURVE1, CURVE2, CURVE3});
    final DiscountCurveFromProductOfCurvesBean bean2 = new DiscountCurveFromProductOfCurvesBean("Curve4", REFERENCE_DATE,
        Sets.<DiscountCurveInterface>newHashSet(CURVE1, CURVE2, CURVE3));
    assertCurveEquals(bean1.buildCurve(), bean2.buildCurve());
  }

  @Override
  @Test
  public void testGetReferenceDate() {
    DiscountCurveFromProductOfCurvesBean bean = new DiscountCurveFromProductOfCurvesBean("Curve4", null,
        new DiscountCurveInterface[]{CURVE1, CURVE2, CURVE3});
    assertNull(bean.getReferenceDate());
    bean = new DiscountCurveFromProductOfCurvesBean("Curve4", REFERENCE_DATE, new DiscountCurveInterface[]{CURVE1, CURVE2, CURVE3});
    assertEquals(FinmathDateUtils.convertToLocalDate(bean.getReferenceDate()), LocalDate.of(2015, 1, 1));
  }

  /**
   * Tests the behaviour when the reference date string cannot be parsed.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testMalformedReferenceDate() {
    final DiscountCurveFromProductOfCurvesBean curve = new DiscountCurveFromProductOfCurvesBean("Curve4", "2015/01/15",
        new DiscountCurveInterface[]{CURVE1, CURVE2, CURVE3});
    curve.getReferenceDate();
  }
}
