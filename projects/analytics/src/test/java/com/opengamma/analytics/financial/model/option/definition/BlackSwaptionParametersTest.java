/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.model.option.parameters.BlackFlatSwaptionParameters;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.factory.FlatExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LinearInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.NamedInterpolator1dFactory;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BlackSwaptionParametersTest {

  /**
   * The linear interpolator/ flat extrapolator. Used for SABR parameters interpolation.
   */
  private static final Interpolator1D LINEAR_FLAT = NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME,
      FlatExtrapolator1dAdapter.NAME,
      FlatExtrapolator1dAdapter.NAME);
  private static final GridInterpolator2D INTERPOLATOR_2D = new GridInterpolator2D(LINEAR_FLAT, LINEAR_FLAT);
  private static final WorkingDayCalendar CALENDAR = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR6M = GENERATOR_SWAP_MASTER.getGenerator("EUR1YEURIBOR6M", CALENDAR);
  private static final InterpolatedDoublesSurface BLACK_SURFACE = InterpolatedDoublesSurface
      .from(new double[] { 0.5, 1.0, 5.0, 0.5, 1.0, 5.0 }, new double[] { 2, 2, 2, 10, 10, 10 }, new double[] { 0.35,
          0.34, 0.25, 0.30, 0.25, 0.20 }, INTERPOLATOR_2D);
  private static final BlackFlatSwaptionParameters BLACK_SWAPTION = new BlackFlatSwaptionParameters(BLACK_SURFACE, EUR1YEURIBOR6M);

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBlack() {
    new BlackFlatSwaptionParameters(null, EUR1YEURIBOR6M);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullGenerator() {
    new BlackFlatSwaptionParameters(BLACK_SURFACE, null);
  }

  /**
   * Tests the object getters.
   */
  @Test
  public void getter() {
    assertEquals("Black Swaption Surface: getter", BLACK_SURFACE, BLACK_SWAPTION.getVolatilitySurface());
    assertEquals("Black Swaption Surface: getter", EUR1YEURIBOR6M, BLACK_SWAPTION.getGeneratorSwap());
  }

  /**
   * Tests the object equal and hash code methods.
   */
  @Test
  public void equalHash() {
    assertTrue("Black Swaption Surface: equal and hash code", BLACK_SWAPTION.equals(BLACK_SWAPTION));
    final BlackFlatSwaptionParameters other = new BlackFlatSwaptionParameters(BLACK_SURFACE, EUR1YEURIBOR6M);
    assertTrue("Black Swaption Surface: equal and hash code", BLACK_SWAPTION.equals(other));
    assertEquals("Black Swaption Surface: equal and hash code", BLACK_SWAPTION.hashCode(), other.hashCode());
    BlackFlatSwaptionParameters modified = new BlackFlatSwaptionParameters(BLACK_SURFACE,
        GENERATOR_SWAP_MASTER.getGenerator("EUR1YEURIBOR3M", CALENDAR));
    assertFalse("Black Swaption Surface: equal and hash code", BLACK_SWAPTION.equals(modified));
    final InterpolatedDoublesSurface surface2 = InterpolatedDoublesSurface.from(new double[] { 0.5, 1.0, 5.0, 0.5, 1.0, 5.0 },
        new double[] { 2, 2, 2, 10, 10, 10 }, new double[] { 0.35, 0.34, 0.25, 0.31, 0.25,
            0.20 },
        INTERPOLATOR_2D);
    modified = new BlackFlatSwaptionParameters(surface2, EUR1YEURIBOR6M);
    assertFalse("Black Swaption Surface: equal and hash code", BLACK_SWAPTION.equals(modified));
  }

}
