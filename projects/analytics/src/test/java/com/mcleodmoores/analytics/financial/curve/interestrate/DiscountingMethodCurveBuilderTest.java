/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Unit tests for {@link DiscountingMethodBondCurveBuilder}.
 */
public class DiscountingMethodCurveBuilderTest {
  private static final List<List<String>> CURVE_NAMES = Arrays.asList(Arrays.asList("A", "B"), Arrays.asList("C"));
  private static final List<Pair<String, UniqueIdentifiable>> DISCOUNTING = Arrays.asList(Pairs.<String, UniqueIdentifiable>of("A", Currency.USD),
      Pairs.<String, UniqueIdentifiable>of("C", Currency.EUR));
  private static final List<Pair<String, List<IborTypeIndex>>> IBOR = Arrays.asList(Pairs.of("B",
      Arrays.asList(new IborTypeIndex("Z", Currency.USD, Tenor.THREE_MONTHS, 2, DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, false),
                    new IborTypeIndex("Z", Currency.USD, Tenor.SIX_MONTHS, 2, DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, false))));
  private static final List<Pair<String, List<OvernightIndex>>> OVERNIGHT = Arrays.asList(
      Pairs.of("A", Arrays.asList(new OvernightIndex("X", Currency.USD, DayCounts.ACT_360, 1))),
      Pairs.of("C", Arrays.asList(new OvernightIndex("V", Currency.EUR, DayCounts.ACT_360, 0))));
  private static final Map<String, List<InstrumentDefinition<?>>> NODES = new HashMap<>();
  private static final FXMatrix FX = new FXMatrix(Currency.USD, Currency.EUR, 0.7);
  private static final Map<String, ? extends CurveTypeSetUpInterface> TYPES = new HashMap<>();
  private static final Map<? extends PreConstructedCurveTypeSetUp, YieldAndDiscountCurve> KNOWN_CURVES = new HashMap<>();
  private static final CurveBuildingBlockBundle SENSITIVITIES = new CurveBuildingBlockBundle();
  private static final double TOLERANCE = 1e-12;
  private static final int MAX_STEPS = 1000;

  /**
   * Tests that the curve names cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurveNames() {
    new DiscountingMethodCurveBuilder(null, DISCOUNTING, IBOR, OVERNIGHT, NODES, TYPES, FX, KNOWN_CURVES, SENSITIVITIES, TOLERANCE, TOLERANCE, MAX_STEPS);
  }

  /**
   * Tests that the curve names cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyCurveNames() {
    new DiscountingMethodCurveBuilder(new ArrayList<List<String>>(), DISCOUNTING, IBOR, OVERNIGHT, NODES, TYPES, FX, KNOWN_CURVES, SENSITIVITIES, TOLERANCE, TOLERANCE, MAX_STEPS);
  }

  /**
   * Tests that the discounting curve map cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurves1() {

  }
}
