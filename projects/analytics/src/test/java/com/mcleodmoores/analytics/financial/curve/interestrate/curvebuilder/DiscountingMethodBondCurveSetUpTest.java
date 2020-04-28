/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
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
public class DiscountingMethodBondCurveSetUpTest {
  private static final InstrumentDefinition<?> INSTRUMENT_1 = new CashDefinition(Currency.USD, ZonedDateTime.now(), ZonedDateTime.now().plusMonths(1), 1, 0.05,
      1. / 12);
  private static final InstrumentDefinition<?> INSTRUMENT_2 = new CashDefinition(Currency.EUR, ZonedDateTime.now(), ZonedDateTime.now().plusMonths(1), 1, 0.05,
      1. / 12);
  private static final CurveBuildingBlockBundle KNOWN;
  private static final FXMatrix FX = new FXMatrix(Currency.USD);

  static {
    final LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> bundle = new LinkedHashMap<>();
    bundle.put("D", Pairs.of(new CurveBuildingBlock(), DoubleMatrix2D.EMPTY_MATRIX));
    KNOWN = new CurveBuildingBlockBundle(bundle);
    FX.addCurrency(Currency.EUR, Currency.USD, 1.2);
  }

  /**
   * Tests that nulls cannot be passed into builder methods.
   */
  @Test
  public void testNullBuilderMethodInputs() {
    TestUtils.testNullBuilderMethodInputs(DiscountingMethodBondCurveSetUp.class, CurveSetUpInterface.class, "withKnownBundle");
  }

  /**
   * Tests that the curve name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurveName1() {
    new DiscountingMethodBondCurveSetUp().addNode(null, INSTRUMENT_1);
  }

  /**
   * Tests that the instrument cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInstrument() {
    new DiscountingMethodBondCurveSetUp().addNode("A", null);
  }

  /**
   * Tests that the FX matrix cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFxMatrix() {
    new DiscountingMethodBondCurveSetUp().addFxMatrix(null);
  }

  /**
   * Tests that the curve name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurveName2() {
    new DiscountingMethodBondCurveSetUp().removeNodes(null);
  }

  /**
   * Tests that empty collections / arrays cannot be passed into builder methods.
   */
  @Test
  public void testEmptyBuilderMethodInputs() {
    TestUtils.testEmptyBuilderMethodInputs(DiscountingMethodBondCurveSetUp.class, CurveSetUpInterface.class);
  }

  /**
   * Tests that the tolerances and max steps must be greater than zero.
   */
  @Test
  public void testNegativeBuilderMethodInputs() {
    TestUtils.testBuilderMethodsLowerRange(DiscountingMethodBondCurveSetUp.class, CurveSetUpInterface.class, 0, false);
  }

  /**
   * Tests that the builder cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBuilder() {
    new DiscountingMethodBondCurveSetUp(null);
  }

  /**
   * Tests that the building() method cannot be used twice.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuilding() {
    new DiscountingMethodBondCurveSetUp().building("A").building("B");
  }

  /**
   * Tests that the buildingFirst() method cannot be used twice.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuildingFirst() {
    new DiscountingMethodBondCurveSetUp().building("A").buildingFirst("B");
  }

  /**
   * Tests that the thenBuilding() method must be used after building() or buildingFirst().
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testThenBuilding() {
    new DiscountingMethodBondCurveSetUp().thenBuilding("B");
  }

  /**
   * Test duplicated curve type.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testDuplicatedCurveType() {
    new DiscountingMethodBondCurveSetUp().using("A").using("A");
  }

  /**
   * Tests that a pre-constructed curve cannot be set more than once.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testDuplicatedPreConstructedCurveForCurrency() {
    final YieldCurve curve1 = new YieldCurve("A", ConstantDoublesCurve.from(0.01));
    final YieldCurve curve2 = new YieldCurve("B", ConstantDoublesCurve.from(0.02));
    new DiscountingMethodBondCurveTypeSetUp().using(curve1).forDiscounting(Currency.USD).using(curve2).forDiscounting(Currency.USD).getBuilder();
  }

  /**
   * Tests that a pre-constructed curve cannot be set more than once.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testDuplicatedPreConstructedCurveForIborIndex() {
    final IborTypeIndex index = new IborTypeIndex("", Currency.USD, Tenor.THREE_MONTHS, 2, DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, false);
    final YieldCurve curve1 = new YieldCurve("A", ConstantDoublesCurve.from(0.01));
    final YieldCurve curve2 = new YieldCurve("B", ConstantDoublesCurve.from(0.02));
    new DiscountingMethodBondCurveTypeSetUp().using(curve1).forIndex(index).using(curve2).forIndex(index).getBuilder();
  }

  /**
   * Tests that a pre-constructed curve cannot be set more than once.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testDuplicatedPreConstructedCurveForOvernightIndex() {
    final OvernightIndex index = new OvernightIndex("", Currency.USD, DayCounts.ACT_360, 0);
    final YieldCurve curve1 = new YieldCurve("A", ConstantDoublesCurve.from(0.01));
    final YieldCurve curve2 = new YieldCurve("B", ConstantDoublesCurve.from(0.02));
    new DiscountingMethodBondCurveTypeSetUp().using(curve1).forIndex(index).using(curve2).forIndex(index).getBuilder();
  }

  /**
   * Tests the copy method.
   */
  @Test
  public void testCopy() {
    final DiscountingMethodBondCurveSetUp setup1 = new DiscountingMethodBondCurveSetUp()
        .building("B").using("B").forDiscounting(Currency.EUR).addNode("B", INSTRUMENT_2);
    DiscountingMethodBondCurveSetUp setup2 = setup1.copy();
    assertNotSame(setup1.getBuilder().getDiscountingCurves(), setup2.getBuilder().getDiscountingCurves());
    setup2 = setup1.copy()
        .using(new YieldCurve("C", ConstantDoublesCurve.from(0.02))).forDiscounting(Currency.USD);
    assertNotEquals(setup1.getBuilder().getKnownDiscountingCurves(), setup2.getBuilder().getKnownDiscountingCurves());
    assertNotSame(setup2.getBuilder().getKnownDiscountingCurves(), setup2.copy().getBuilder().getKnownDiscountingCurves());
    setup2 = setup1.copy()
        .addFxMatrix(FX);
    assertNotEquals(setup1.getBuilder().getFxMatrix(), setup2.getBuilder().getFxMatrix());
    assertNotSame(setup2.getBuilder().getFxMatrix(), setup2.copy().getBuilder().getFxMatrix());
    setup2 = setup1.copy()
        .withKnownBundle(KNOWN);
    assertNotEquals(setup1.getBuilder().getKnownBundle(), setup2.getBuilder().getKnownBundle());
    assertNotSame(setup2.getBuilder().getKnownBundle(), setup2.copy().getBuilder().getKnownBundle());
  }

  /**
   * Tests that the builder cannot be constructed if no curves have been set up.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testNoCurves() {
    new DiscountingMethodBondCurveSetUp().getBuilder();
  }

  /**
   * Tests that the builder cannot be constructed if nodes haven't been added for all curves.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testNodesNotAdded() {
    final DiscountingMethodBondCurveSetUp setup = new DiscountingMethodBondCurveSetUp()
        .building("A", "B")
        .using("A").forDiscounting(Currency.USD)
        .addNode("A", INSTRUMENT_1)
        .using("B").forDiscounting(Currency.EUR);
    setup.getBuilder();
  }

  /**
   * Tests that the builder cannot be constructed if nodes have been added for a curve that has not had its building order configured.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCurveBuildOrderNotConfigured() {
    final DiscountingMethodBondCurveSetUp setup = new DiscountingMethodBondCurveSetUp()
        .building("A")
        .using("A").forDiscounting(Currency.USD)
        .addNode("A", INSTRUMENT_1)
        .addNode("B", INSTRUMENT_2)
        .addNode("C", INSTRUMENT_1);
    setup.getBuilder();
  }

  /**
   * Tests that the builder cannot be constructed if nodes have been added for a curve that has not had been configured.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testCurveNotConfigured() {
    final DiscountingMethodBondCurveSetUp setup = new DiscountingMethodBondCurveSetUp()
        .building("A", "B", "C", "D")
        .using("A").forDiscounting(Currency.USD)
        .addNode("A", INSTRUMENT_1)
        .addNode("B", INSTRUMENT_2)
        .addNode("C", INSTRUMENT_1)
        .addNode("D", INSTRUMENT_2);
    setup.getBuilder();
  }

  /**
   * Tests that nodes are added for the correct curves.
   */
  @Test
  public void testAddNodes() {
    final DiscountingMethodBondCurveSetUp setup = new DiscountingMethodBondCurveSetUp()
        .building("A", "B")
        .using("A").forDiscounting(Currency.USD)
        .addNode("A", INSTRUMENT_1)
        .using("B").forDiscounting(Currency.EUR)
        .addNode("B", INSTRUMENT_2);
    final DiscountingMethodBondCurveBuilder builder = setup.getBuilder();
    final List<Pair<String, UniqueIdentifiable>> discountingCurves = builder.getDiscountingCurves();
    assertEquals(discountingCurves.size(), 2);
    assertTrue(discountingCurves.contains(Pairs.of("A", Currency.USD)));
    assertTrue(discountingCurves.contains(Pairs.of("B", Currency.EUR)));
    final Map<String, List<InstrumentDefinition<?>>> nodes = builder.getNodes();
    assertEquals(nodes.size(), 2);
    assertEquals(nodes.get("A").size(), 1);
    assertEquals(nodes.get("B").size(), 1);
    assertEquals(nodes.get("A").iterator().next(), INSTRUMENT_1);
    assertEquals(nodes.get("B").iterator().next(), INSTRUMENT_2);
  }

  /**
   * Test that all nodes are removed.
   */
  @Test
  public void testRemoveAllNodes() {
    final DiscountingMethodBondCurveSetUp setup = new DiscountingMethodBondCurveSetUp()
        .building("A", "B").using("A").forDiscounting(Currency.USD).using("B").forDiscounting(Currency.EUR)
        .addNode("A", INSTRUMENT_1)
        .addNode("B", INSTRUMENT_2);
    setup.removeNodes("A");
    assertTrue(setup.getBuilder().getNodes().containsKey("A"));
    assertNull(setup.getBuilder().getNodes().get("A"));
    assertEquals(setup.getBuilder().getNodes().get("B").size(), 1);
  }

  /**
   * Test that the curve name is removed.
   */
  @Test
  public void testRemoveCurve() {
    final DiscountingMethodBondCurveSetUp setup = new DiscountingMethodBondCurveSetUp()
        .building("A", "B").using("A").forDiscounting(Currency.USD).using("B").forDiscounting(Currency.EUR)
        .addNode("A", INSTRUMENT_1)
        .addNode("B", INSTRUMENT_2);
    setup.removeCurve("A");
    assertFalse(setup.getBuilder().getNodes().containsKey("A"));
    assertNull(setup.getBuilder().getNodes().get("A"));
    assertEquals(setup.getBuilder().getNodes().get("B").size(), 1);
  }
}
