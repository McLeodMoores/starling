/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder.CurveTypeSetUpInterface.CurveFunction;
import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveAddYieldExisting;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveDiscountFactorInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveDiscountFactorInterpolatedNode;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolatedNode;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldNelsonSiegel;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldPeriodicInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.instrument.index.IndexConverter;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.interpolation.factory.LinearInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.NamedInterpolator1dFactory;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link HullWhiteMethodCurveTypeSetUp}.
 */
@Test(groups = TestGroup.UNIT)
public class HullWhiteMethodCurveTypeSetUpTest {
  private static final UniqueIdentifiable DISCOUNTING_ID = Currency.USD;
  private static final IborTypeIndex[] IBOR_INDICES = {
      new IborTypeIndex("A", Currency.USD, Tenor.THREE_MONTHS, 2, DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, true),
      new IborTypeIndex("B", Currency.USD, Tenor.SIX_MONTHS, 2, DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, true) };
  private static final OvernightIndex[] OVERNIGHT_INDICES = { new OvernightIndex("A", Currency.USD, DayCounts.ACT_360, 1),
      new OvernightIndex("B", Currency.USD, DayCounts.ACT_360, 1) };

  /**
   * Tests that the builder to copy cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBuilder() {
    new HullWhiteMethodCurveTypeSetUp(null);
  }

  /**
   * Tests that nulls cannot be passed into builder methods.
   */
  @Test
  public void testNullBuilderMethodInputs() {
    TestUtils.testNullBuilderMethodInputs(HullWhiteMethodCurveTypeSetUp.class, CurveTypeSetUpInterface.class);
  }

  /**
   * Tests that empty collections / arrays cannot be passed into builder methods.
   */
  @Test
  public void testEmptyBuilderMethodInputs() {
    TestUtils.testEmptyBuilderMethodInputs(HullWhiteMethodCurveTypeSetUp.class, CurveTypeSetUpInterface.class);
  }

  /**
   * Tests that a functional curve type cannot be created if an interpolated / fixed date curve type has already started to be constructed.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testFunctionalCurveState1() {
    new HullWhiteMethodCurveTypeSetUp()
        .withInterpolator(NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME))
        .functionalForm(CurveFunction.NELSON_SIEGEL);
  }

  /**
   * Tests that a functional curve type cannot be created if an interpolated / fixed date curve type has already started to be constructed.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testFunctionalCurveState2() {
    new HullWhiteMethodCurveTypeSetUp()
        .usingNodeDates(new LocalDateTime[] { LocalDateTime.now(), LocalDateTime.now().plusDays(1) })
        .functionalForm(CurveFunction.NELSON_SIEGEL);
  }

  /**
   * Tests that a functional curve type cannot be created if an interpolated / fixed date curve type has already started to be constructed.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testFunctionalCurveState3() {
    new HullWhiteMethodCurveTypeSetUp()
        .continuousInterpolationOnYield()
        .functionalForm(CurveFunction.NELSON_SIEGEL);
  }

  /**
   * Tests that a functional curve type cannot be created as a spread over another curve.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testFunctionalCurveState4() {
    new HullWhiteMethodCurveTypeSetUp()
        .asSpreadOver("BASE")
        .functionalForm(CurveFunction.NELSON_SIEGEL);
  }

  /**
   * Tests the functional form curve generator.
   */
  @Test
  public void testFunctionalFormCurveGenerator1() {
    final HullWhiteMethodCurveTypeSetUp setup = new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(DISCOUNTING_ID)
        .functionalForm(CurveFunction.NELSON_SIEGEL);
    assertTrue(setup.buildCurveGenerator(ZonedDateTime.now()) instanceof GeneratorCurveYieldNelsonSiegel);
  }

  /**
   * Tests that an interpolated curve type cannot be created if a functional form has already been supplied.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testInterpolatedCurveState() {
    new HullWhiteMethodCurveTypeSetUp()
        .functionalForm(CurveFunction.NELSON_SIEGEL)
        .withInterpolator(NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME));
  }

  /**
   * Tests the interpolated curve generator.
   */
  @Test
  public void testInterpolatedCurveGenerator1() {
    final HullWhiteMethodCurveTypeSetUp setup = new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(DISCOUNTING_ID)
        .withInterpolator(NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME));
    assertTrue(setup.buildCurveGenerator(ZonedDateTime.now()) instanceof GeneratorCurveYieldInterpolated);
  }

  /**
   * Tests the interpolated curve generator.
   */
  @Test
  public void testInterpolatedCurveGenerator2() {
    final HullWhiteMethodCurveTypeSetUp setup = new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(DISCOUNTING_ID)
        .asSpreadOver("BASE")
        .withInterpolator(NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME));
    assertTrue(setup.buildCurveGenerator(ZonedDateTime.now()) instanceof GeneratorCurveAddYieldExisting);
  }

  /**
   * Tests that an fixed node curve type cannot be created if a functional form has already been supplied.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testFixedNodeCurveState() {
    new HullWhiteMethodCurveTypeSetUp()
        .functionalForm(CurveFunction.NELSON_SIEGEL)
        .usingNodeDates(new LocalDateTime[] { LocalDateTime.now(), LocalDateTime.now().plusDays(1) });
  }

  /**
   * Tests that the fixed node curve generator cannot be created without an interpolator.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testFixedNodeGeneratorNoInterpolator() {
    final HullWhiteMethodCurveTypeSetUp setup = new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(DISCOUNTING_ID)
        .usingNodeDates(new LocalDateTime[] { LocalDateTime.now(), LocalDateTime.now().plusDays(1) });
    setup.buildCurveGenerator(ZonedDateTime.now());
  }

  /**
   * Tests that there must be at least 2 nodes.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNumberOfNodes() {
    new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(DISCOUNTING_ID)
        .withInterpolator(NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME))
        .usingNodeDates(new LocalDateTime[0])
        .buildCurveGenerator(ZonedDateTime.now());
  }

  /**
   * Tests the fixed curve node generator.
   */
  @Test
  public void testFixedNodeGenerator1() {
    final HullWhiteMethodCurveTypeSetUp setup = new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(DISCOUNTING_ID)
        .withInterpolator(NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME))
        .usingNodeDates(new LocalDateTime[] { LocalDateTime.now(), LocalDateTime.now().plusDays(1) });
    assertTrue(setup.buildCurveGenerator(ZonedDateTime.now()) instanceof GeneratorCurveYieldInterpolatedNode);
  }

  /**
   * Tests the fixed curve node generator.
   */
  @Test
  public void testFixedNodeGenerator2() {
    final HullWhiteMethodCurveTypeSetUp setup = new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(DISCOUNTING_ID)
        .asSpreadOver("BASE")
        .withInterpolator(NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME))
        .usingNodeDates(new LocalDateTime[] { LocalDateTime.now(), LocalDateTime.now().plusDays(1) });
    assertTrue(setup.buildCurveGenerator(ZonedDateTime.now()) instanceof GeneratorCurveAddYieldExisting);
  }

  /**
   * Tests that an interpolated on yield curve type cannot be created if a functional form has already been set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testInterpolatedOnYieldCurveState1() {
    new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(DISCOUNTING_ID)
        .functionalForm(CurveFunction.NELSON_SIEGEL)
        .continuousInterpolationOnYield();
  }

  /**
   * Tests that an interpolated on yield curve type cannot be created if the type has already been set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testInterpolatedOnYieldCurveState2() {
    new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(DISCOUNTING_ID)
        .continuousInterpolationOnDiscountFactors()
        .continuousInterpolationOnYield();
  }

  /**
   * Tests that an interpolator must be set to interpolate on yields.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testInterpolatorSetForInterpolatedOnYield() {
    new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(DISCOUNTING_ID)
        .continuousInterpolationOnYield()
        .buildCurveGenerator(ZonedDateTime.now());
  }

  /**
   * Tests the curve generator when interpolation on yield is explicitly chosen.
   */
  @Test
  public void testInterpolatedOnYieldCurveGenerator1() {
    final HullWhiteMethodCurveTypeSetUp setup = new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(DISCOUNTING_ID)
        .withInterpolator(NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME))
        .continuousInterpolationOnYield();
    assertTrue(setup.buildCurveGenerator(ZonedDateTime.now()) instanceof GeneratorCurveYieldInterpolated);
    assertTrue(setup.usingNodeDates(new LocalDateTime[] { LocalDateTime.now(), LocalDateTime.now().plusDays(1) })
        .buildCurveGenerator(ZonedDateTime.now()) instanceof GeneratorCurveYieldInterpolatedNode);
  }

  /**
   * Tests the curve generator when interpolation on yield is explicitly chosen.
   */
  @Test
  public void testInterpolatedOnYieldCurveGenerator2() {
    final HullWhiteMethodCurveTypeSetUp setup = new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(DISCOUNTING_ID)
        .withInterpolator(NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME))
        .asSpreadOver("BASE")
        .continuousInterpolationOnYield();
    assertTrue(setup.buildCurveGenerator(ZonedDateTime.now()) instanceof GeneratorCurveAddYieldExisting);
    assertTrue(setup.usingNodeDates(new LocalDateTime[] { LocalDateTime.now(), LocalDateTime.now().plusDays(1) })
        .buildCurveGenerator(ZonedDateTime.now()) instanceof GeneratorCurveAddYieldExisting);
  }

  /**
   * Tests that an interpolated on discount factor curve type cannot be created if a functional form has already been set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testInterpolatedOnDiscountFactorCurveState1() {
    new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(DISCOUNTING_ID)
        .functionalForm(CurveFunction.NELSON_SIEGEL)
        .continuousInterpolationOnDiscountFactors();
  }

  /**
   * Tests that an interpolated on discount factor curve type cannot be created if the type has already been set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testInterpolatedOnDiscountFactorCurveState2() {
    new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(DISCOUNTING_ID)
        .continuousInterpolationOnYield()
        .continuousInterpolationOnDiscountFactors();
  }

  /**
   * Tests that an interpolator must be set to interpolate on discount factors.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testInterpolatorSetForInterpolatedOnDiscountFactors() {
    new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(DISCOUNTING_ID)
        .continuousInterpolationOnDiscountFactors()
        .buildCurveGenerator(ZonedDateTime.now());
  }

  /**
   * Tests the curve generator when interpolation on discount factors is chosen.
   */
  @Test
  public void testInterpolatedOnDiscountFactorCurveGenerator1() {
    final HullWhiteMethodCurveTypeSetUp setup = new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(DISCOUNTING_ID)
        .withInterpolator(NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME))
        .continuousInterpolationOnDiscountFactors();
    assertTrue(setup.buildCurveGenerator(ZonedDateTime.now()) instanceof GeneratorCurveDiscountFactorInterpolated);
    assertTrue(setup.usingNodeDates(new LocalDateTime[] { LocalDateTime.now(), LocalDateTime.now().plusDays(1) })
        .buildCurveGenerator(ZonedDateTime.now()) instanceof GeneratorCurveDiscountFactorInterpolatedNode);
  }

  /**
   * Tests the curve generator when interpolation on discount factors is chosen.
   */
  @Test
  public void testInterpolatedOnDiscountFactorCurveGenerator2() {
    final HullWhiteMethodCurveTypeSetUp setup = new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(DISCOUNTING_ID)
        .withInterpolator(NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME))
        .asSpreadOver("BASE")
        .continuousInterpolationOnDiscountFactors();
    assertTrue(setup.buildCurveGenerator(ZonedDateTime.now()) instanceof GeneratorCurveAddYieldExisting);
    assertTrue(setup.usingNodeDates(new LocalDateTime[] { LocalDateTime.now(), LocalDateTime.now().plusDays(1) })
        .buildCurveGenerator(ZonedDateTime.now()) instanceof GeneratorCurveAddYieldExisting);
  }

  /**
   * Tests that there must be at least one compounding period per year.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCompoundingPeriodsPerYear() {
    new HullWhiteMethodCurveTypeSetUp()
        .periodicInterpolationOnYield(0);
  }

  /**
   * Tests that a periodically compounded curve cannot be created if a functional form has already been set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testPeriodicCurveState1() {
    new HullWhiteMethodCurveTypeSetUp()
        .functionalForm(CurveFunction.NELSON_SIEGEL)
        .periodicInterpolationOnYield(4);
  }

  /**
   * Tests that a periodically compounded curve cannot be created if the type has already been set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testPeriodicCurveState2() {
    new HullWhiteMethodCurveTypeSetUp()
        .continuousInterpolationOnDiscountFactors()
        .periodicInterpolationOnYield(4);
  }

  /**
   * Tests that an interpolator must be set to interpolate on yields.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testInterpolatorSetForPeriodicCurve() {
    new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(DISCOUNTING_ID)
        .periodicInterpolationOnYield(4)
        .buildCurveGenerator(ZonedDateTime.now());
  }

  /**
   * Tests that node dates cannot be set for curves with periodic yields.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testNoNodeDatesForPeriodicCurves1() {
    new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(DISCOUNTING_ID)
        .withInterpolator(NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME))
        .periodicInterpolationOnYield(4)
        .usingNodeDates(new LocalDateTime[] { LocalDateTime.now(), LocalDateTime.now().plusDays(1) });
  }

  /**
   * Tests that node dates cannot be set for curves with periodic yields.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testNoNodeDatesForPeriodicCurves2() {
    new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(DISCOUNTING_ID)
        .withInterpolator(NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME))
        .usingNodeDates(new LocalDateTime[] { LocalDateTime.now(), LocalDateTime.now().plusDays(1) })
        .periodicInterpolationOnYield(4);
  }

  /**
   * Tests the curve generator when a periodic curve is chosen.
   */
  @Test
  public void testPeriodicCurveGenerator1() {
    final HullWhiteMethodCurveTypeSetUp setup = new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(DISCOUNTING_ID)
        .withInterpolator(NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME))
        .periodicInterpolationOnYield(4);
    assertTrue(setup.buildCurveGenerator(ZonedDateTime.now()) instanceof GeneratorCurveYieldPeriodicInterpolated);
  }

  /**
   * Tests the curve generator when a periodic curve is chosen.
   */
  @Test
  public void testPeriodicCurveGenerator2() {
    final HullWhiteMethodCurveTypeSetUp setup = new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(DISCOUNTING_ID)
        .withInterpolator(NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME))
        .asSpreadOver("BASE")
        .periodicInterpolationOnYield(4);
    assertTrue(setup.buildCurveGenerator(ZonedDateTime.now()) instanceof GeneratorCurveAddYieldExisting);
  }

  /**
   * Tests the getters.
   */
  @Test
  public void testGetters() {
    final HullWhiteMethodCurveTypeSetUp setup = new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(DISCOUNTING_ID)
        .forIndex(IBOR_INDICES)
        .forIndex(OVERNIGHT_INDICES);
    assertEquals(setup.getDiscountingCurveId(), DISCOUNTING_ID);
    assertArrayEquals(setup.getIborCurveIndices().toArray(new IborTypeIndex[0]), IBOR_INDICES);
    assertArrayEquals(setup.getOvernightCurveIndices().toArray(new OvernightIndex[0]), OVERNIGHT_INDICES);
  }

  /**
   * Tests that a functional curve type cannot be created as a spread over another curve.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testSpreadCurveState() {
    new HullWhiteMethodCurveTypeSetUp()
        .functionalForm(CurveFunction.NELSON_SIEGEL)
        .asSpreadOver("BASE");
  }

  /**
   * Tests that both node time calculators cannot be set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBothNodeTimeCalculatorsNotSet1() {
    new HullWhiteMethodCurveTypeSetUp()
        .usingInstrumentMaturity()
        .usingLastFixingEndTime();
  }

  /**
   * Tests that both node time calculators cannot be set.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBothNodeTimeCalculatorsNotSet2() {
    new HullWhiteMethodCurveTypeSetUp()
        .usingLastFixingEndTime()
        .usingInstrumentMaturity();
  }

  /**
   * Tests the toString() method.
   */
  @Test
  public void testToString() {
    HullWhiteMethodCurveTypeSetUp setup = new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(DISCOUNTING_ID)
        .forIndex(IBOR_INDICES)
        .forIndex(OVERNIGHT_INDICES)
        .withInterpolator(NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME));
    assertEquals(setup.toString(), "HullWhiteMethodCurveTypeSetUp[discountingCurveId=USD, "
        + "iborIndices=[IborIndex[A, currency=USD, tenor=P3M, day count=Actual/360, business day convention=Following, spot lag=2, end-of-month], "
        + "IborIndex[B, currency=USD, tenor=P6M, day count=Actual/360, business day convention=Following, spot lag=2, end-of-month]], "
        + "overnightIndices=[OvernightIndex[A, currency=USD, day count=Actual/360, publication lag=1], "
        + "OvernightIndex[B, currency=USD, day count=Actual/360, publication lag=1]], interpolator=Linear]");
    setup = new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(DISCOUNTING_ID)
        .forIndex(IBOR_INDICES)
        .forIndex(OVERNIGHT_INDICES)
        .functionalForm(CurveFunction.NELSON_SIEGEL);
    assertEquals(setup.toString(), "HullWhiteMethodCurveTypeSetUp[discountingCurveId=USD, "
        + "iborIndices=[IborIndex[A, currency=USD, tenor=P3M, day count=Actual/360, business day convention=Following, spot lag=2, end-of-month], "
        + "IborIndex[B, currency=USD, tenor=P6M, day count=Actual/360, business day convention=Following, spot lag=2, end-of-month]], "
        + "overnightIndices=[OvernightIndex[A, currency=USD, day count=Actual/360, publication lag=1], "
        + "OvernightIndex[B, currency=USD, day count=Actual/360, publication lag=1]], functionalForm=NELSON_SIEGEL]");
    setup = new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(DISCOUNTING_ID)
        .forIndex(IBOR_INDICES)
        .forIndex(OVERNIGHT_INDICES)
        .withInterpolator(NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME))
        .periodicInterpolationOnYield(4);
    assertEquals(setup.toString(), "HullWhiteMethodCurveTypeSetUp[discountingCurveId=USD, "
        + "iborIndices=[IborIndex[A, currency=USD, tenor=P3M, day count=Actual/360, business day convention=Following, spot lag=2, end-of-month], "
        + "IborIndex[B, currency=USD, tenor=P6M, day count=Actual/360, business day convention=Following, spot lag=2, end-of-month]], "
        + "overnightIndices=[OvernightIndex[A, currency=USD, day count=Actual/360, publication lag=1], "
        + "OvernightIndex[B, currency=USD, day count=Actual/360, publication lag=1]], interpolator=Linear, "
        + "periodsPerYear=4, interpolation on yield]");
    setup = new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(DISCOUNTING_ID)
        .forIndex(IBOR_INDICES)
        .forIndex(OVERNIGHT_INDICES)
        .withInterpolator(NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME))
        .continuousInterpolationOnYield();
    assertEquals(setup.toString(), "HullWhiteMethodCurveTypeSetUp[discountingCurveId=USD, "
        + "iborIndices=[IborIndex[A, currency=USD, tenor=P3M, day count=Actual/360, business day convention=Following, spot lag=2, end-of-month], "
        + "IborIndex[B, currency=USD, tenor=P6M, day count=Actual/360, business day convention=Following, spot lag=2, end-of-month]], "
        + "overnightIndices=[OvernightIndex[A, currency=USD, day count=Actual/360, publication lag=1], "
        + "OvernightIndex[B, currency=USD, day count=Actual/360, publication lag=1]], interpolator=Linear, "
        + "interpolation on yield]");
    setup = new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(DISCOUNTING_ID)
        .forIndex(IBOR_INDICES)
        .forIndex(OVERNIGHT_INDICES)
        .withInterpolator(NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME))
        .continuousInterpolationOnDiscountFactors();
    assertEquals(setup.toString(), "HullWhiteMethodCurveTypeSetUp[discountingCurveId=USD, "
        + "iborIndices=[IborIndex[A, currency=USD, tenor=P3M, day count=Actual/360, business day convention=Following, spot lag=2, end-of-month], "
        + "IborIndex[B, currency=USD, tenor=P6M, day count=Actual/360, business day convention=Following, spot lag=2, end-of-month]], "
        + "overnightIndices=[OvernightIndex[A, currency=USD, day count=Actual/360, publication lag=1], "
        + "OvernightIndex[B, currency=USD, day count=Actual/360, publication lag=1]], interpolator=Linear, "
        + "interpolation on discount factors]");
    final LocalDateTime date = LocalDateTime.of(LocalDate.of(2000, 1, 1), LocalTime.of(0, 0));
    setup = new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(DISCOUNTING_ID)
        .forIndex(IBOR_INDICES)
        .forIndex(OVERNIGHT_INDICES)
        .withInterpolator(NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME))
        .continuousInterpolationOnDiscountFactors()
        .usingNodeDates(new LocalDateTime[] { date, date.plusDays(1) });
    assertEquals(setup.toString(), "HullWhiteMethodCurveTypeSetUp[discountingCurveId=USD, "
        + "iborIndices=[IborIndex[A, currency=USD, tenor=P3M, day count=Actual/360, business day convention=Following, spot lag=2, end-of-month], "
        + "IborIndex[B, currency=USD, tenor=P6M, day count=Actual/360, business day convention=Following, spot lag=2, end-of-month]], "
        + "overnightIndices=[OvernightIndex[A, currency=USD, day count=Actual/360, publication lag=1], "
        + "OvernightIndex[B, currency=USD, day count=Actual/360, publication lag=1]], interpolator=Linear, "
        + "nodeDates=[2000-01-01T00:00, 2000-01-02T00:00], "
        + "interpolation on discount factors]");
    setup = new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(DISCOUNTING_ID)
        .forIndex(IBOR_INDICES)
        .forIndex(OVERNIGHT_INDICES)
        .withInterpolator(NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME))
        .continuousInterpolationOnDiscountFactors()
        .usingInstrumentMaturity();
    assertEquals(setup.toString(), "HullWhiteMethodCurveTypeSetUp[discountingCurveId=USD, "
        + "iborIndices=[IborIndex[A, currency=USD, tenor=P3M, day count=Actual/360, business day convention=Following, spot lag=2, end-of-month], "
        + "IborIndex[B, currency=USD, tenor=P6M, day count=Actual/360, business day convention=Following, spot lag=2, end-of-month]], "
        + "overnightIndices=[OvernightIndex[A, currency=USD, day count=Actual/360, publication lag=1], "
        + "OvernightIndex[B, currency=USD, day count=Actual/360, publication lag=1]], interpolator=Linear, "
        + "interpolation on discount factors, using instrument maturity]");
    setup = new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(DISCOUNTING_ID)
        .forIndex(IBOR_INDICES)
        .forIndex(OVERNIGHT_INDICES)
        .withInterpolator(NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME))
        .continuousInterpolationOnDiscountFactors()
        .usingLastFixingEndTime();
    assertEquals(setup.toString(), "HullWhiteMethodCurveTypeSetUp[discountingCurveId=USD, "
        + "iborIndices=[IborIndex[A, currency=USD, tenor=P3M, day count=Actual/360, business day convention=Following, spot lag=2, end-of-month], "
        + "IborIndex[B, currency=USD, tenor=P6M, day count=Actual/360, business day convention=Following, spot lag=2, end-of-month]], "
        + "overnightIndices=[OvernightIndex[A, currency=USD, day count=Actual/360, publication lag=1], "
        + "OvernightIndex[B, currency=USD, day count=Actual/360, publication lag=1]], interpolator=Linear, "
        + "interpolation on discount factors, using last fixing period end]");
    setup = new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(DISCOUNTING_ID)
        .forIndex(IBOR_INDICES)
        .forIndex(OVERNIGHT_INDICES)
        .withInterpolator(NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME))
        .continuousInterpolationOnDiscountFactors()
        .usingLastFixingEndTime()
        .asSpreadOver("BASE");
    assertEquals(setup.toString(), "HullWhiteMethodCurveTypeSetUp[discountingCurveId=USD, "
        + "iborIndices=[IborIndex[A, currency=USD, tenor=P3M, day count=Actual/360, business day convention=Following, spot lag=2, end-of-month], "
        + "IborIndex[B, currency=USD, tenor=P6M, day count=Actual/360, business day convention=Following, spot lag=2, end-of-month]], "
        + "overnightIndices=[OvernightIndex[A, currency=USD, day count=Actual/360, publication lag=1], "
        + "OvernightIndex[B, currency=USD, day count=Actual/360, publication lag=1]], interpolator=Linear, "
        + "interpolation on discount factors, using last fixing period end, "
        + "baseCurve=BASE]");
  }

  /**
   * Test index array equivalence with multiple calls.
   */
  @Test
  public void testMultipleForIndexCalls() {
    final HullWhiteMethodCurveTypeSetUp setup1 = new HullWhiteMethodCurveTypeSetUp()
        .forIndex(IBOR_INDICES)
        .forIndex(OVERNIGHT_INDICES);
    final HullWhiteMethodCurveTypeSetUp setup2 = new HullWhiteMethodCurveTypeSetUp()
        .forIndex(IBOR_INDICES[0])
        .forIndex(IBOR_INDICES[1])
        .forIndex(OVERNIGHT_INDICES[0])
        .forIndex(OVERNIGHT_INDICES[1]);
    assertEquals(setup1.getIborCurveIndices(), setup2.getIborCurveIndices());
    assertEquals(setup1.getOvernightCurveIndices(), setup2.getOvernightCurveIndices());
  }

  /**
   * Test node date equivalence with multiple calls.
   */
  @Test
  public void testMultipleNodeDateCalls() {
    final HullWhiteMethodCurveTypeSetUp setup1 = new HullWhiteMethodCurveTypeSetUp()
        .usingNodeDates(LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 0)),
            LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(12, 0)),
            LocalDateTime.of(LocalDate.now().plusDays(2), LocalTime.of(12, 0)));
    final HullWhiteMethodCurveTypeSetUp setup2 = new HullWhiteMethodCurveTypeSetUp()
        .usingNodeDates(LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 0)))
        .usingNodeDates(LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(12, 0)),
            LocalDateTime.of(LocalDate.now().plusDays(2), LocalTime.of(12, 0)));
    final HullWhiteMethodCurveTypeSetUp setup3 = new HullWhiteMethodCurveTypeSetUp()
        .usingNodeDates(LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 0)))
        .usingNodeDates(LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(12, 0)))
        .usingNodeDates(LocalDateTime.of(LocalDate.now().plusDays(2), LocalTime.of(12, 0)));
    assertEquals(setup1.getFixedNodeDates(), setup2.getFixedNodeDates());
    assertEquals(setup1.getFixedNodeDates(), setup3.getFixedNodeDates());
  }

  /**
   * Tests that the different node point calculators produce different node points in the curve.
   */
  @Test
  public void testTimeCalculator() {
    final ZonedDateTime date = DateUtils.getUTCDate(2016, 12, 1);
    final ZonedDateTime fixingStart = DateUtils.getUTCDate(2017, 1, 1);
    final ZonedDateTime fixingEnd = fixingStart.plusMonths(3);
    final ZonedDateTime payment = fixingEnd.plusDays(2);
    final IborTypeIndex ibor = new IborTypeIndex("I", Currency.USD, Tenor.THREE_MONTHS, 0, DayCounts.ACT_360,
        BusinessDayConventions.FOLLOWING, false);
    final HullWhiteMethodCurveTypeSetUp setup1 = new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(Currency.USD)
        .forIndex(ibor)
        .withInterpolator(NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME))
        .usingInstrumentMaturity();
    final HullWhiteMethodCurveTypeSetUp setup2 = new HullWhiteMethodCurveTypeSetUp()
        .forDiscounting(Currency.USD)
        .forIndex(ibor)
        .withInterpolator(NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME))
        .usingLastFixingEndTime();
    final CouponIborDefinition coupon = new CouponIborDefinition(Currency.USD, payment, fixingStart, fixingEnd, 0.25, 1, fixingStart,
        fixingStart,
        fixingEnd, 0.25, IndexConverter.toIborIndex(ibor), CalendarAdapter.of(WeekendWorkingDayCalendar.SATURDAY_SUNDAY));
    final InstrumentDerivative[] data = new InstrumentDerivative[] { coupon.toDerivative(date) };
    final GeneratorYDCurve generator1 = setup1.buildCurveGenerator(date).finalGenerator(data);
    final GeneratorYDCurve generator2 = setup2.buildCurveGenerator(date).finalGenerator(data);
    final double node1 = ((YieldCurve) generator1.generateCurve("A", new MulticurveProviderDiscount(), new double[] { 0.01 })).getCurve()
        .getXData()[0];
    final double node2 = ((YieldCurve) generator2.generateCurve("A", new MulticurveProviderDiscount(), new double[] { 0.01 })).getCurve()
        .getXData()[0];
    assertTrue(node1 > node2);
  }
}
