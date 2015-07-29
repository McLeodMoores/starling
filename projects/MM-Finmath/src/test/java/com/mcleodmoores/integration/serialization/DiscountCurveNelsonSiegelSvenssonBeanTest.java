/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.serialization;

import static com.mcleodmoores.integration.testutils.FinmathSerializationTestUtils.assertCurveEquals;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.integration.adapter.FinmathDateUtils;

/**
 * Unit tests for {@link DiscountCurveNelsonSiegelSvenssonBean}.
 */
public class DiscountCurveNelsonSiegelSvenssonBeanTest extends CurveBeanTest {
  /** The name */
  private static final String NAME = "Curve";
  /** Reference date */
  private static final String REFERENCE_DATE = "2015-01-01";
  /** The parameters */
  private static final double[] PARAMETERS = new double[] {1, 2, 3, 4, 5, 6};
  /** The time scaling parameter */
  private static final double TIME_SCALING = 1;

  /**
   * Tests the behaviour when the number of parameters is wrong.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testParameters() {
    new DiscountCurveNelsonSiegelSvenssonBean(NAME, REFERENCE_DATE, new double[] {1, 2, 3, 4}, TIME_SCALING);
  }

  @Override
  @Test
  public void testBuildCurve() {
    final DiscountCurveNelsonSiegelSvenssonBean curve1 = new DiscountCurveNelsonSiegelSvenssonBean(NAME, REFERENCE_DATE, PARAMETERS, TIME_SCALING);
    final DiscountCurveNelsonSiegelSvenssonBean curve2 = new DiscountCurveNelsonSiegelSvenssonBean(NAME, REFERENCE_DATE, PARAMETERS, TIME_SCALING);
    // reference date isn't used
    final DiscountCurveNelsonSiegelSvenssonBean curve3 = new DiscountCurveNelsonSiegelSvenssonBean(NAME, null, PARAMETERS, TIME_SCALING);
    assertCurveEquals(curve1.buildCurve(), curve2.buildCurve());
    assertCurveEquals(curve1.buildCurve(), curve3.buildCurve());
  }

  @Override
  @Test
  public void testGetReferenceDate() {
    DiscountCurveNelsonSiegelSvenssonBean curve = new DiscountCurveNelsonSiegelSvenssonBean(NAME, null, PARAMETERS, TIME_SCALING);
    assertNull(curve.getReferenceDate());
    curve = new DiscountCurveNelsonSiegelSvenssonBean(NAME, REFERENCE_DATE, PARAMETERS, TIME_SCALING);
    assertEquals(FinmathDateUtils.convertToLocalDate(curve.getReferenceDate()), LocalDate.of(2015, 1, 1));
  }

}
