/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve.discounting;

import static com.opengamma.analytics.financial.provider.curve.CurveBuildingTestUtils.assertFiniteDifferenceSensitivities;
import static com.opengamma.analytics.financial.provider.curve.CurveBuildingTestUtils.assertMatrixEquals;
import static com.opengamma.analytics.financial.provider.curve.CurveBuildingTestUtils.assertNoSensitivities;
import static com.opengamma.analytics.financial.provider.curve.CurveBuildingTestUtils.assertYieldCurvesEqual;
import static com.opengamma.analytics.financial.provider.curve.CurveBuildingTestUtils.curveConstructionTest;
import static org.testng.AssertJUnit.assertEquals;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.financial.curve.interestrate.DiscountingMethodCurveBuilder;
import com.mcleodmoores.analytics.financial.curve.interestrate.DiscountingMethodCurveSetUp;
import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositON;
import com.opengamma.analytics.financial.instrument.index.GeneratorFRA;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapIborIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapIborIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.Index;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingTests;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.factory.FlatExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LinearInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.NamedInterpolator1dFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Builds and tests AUD discounting, 3m and 6m bank bill curves. Curves are constructed in two ways:
 * <ul>
 *  <li> Discounting, then 3m and 6m bank bill simultaneously;
 *  <li> Discounting, 3m and 6m bank bill simultaneously.
 * </ul>
 * In the first case, the discounting curve has no sensitivities to either of the bank bill curves. In the second case, the
 * discounting curve has sensitivities to all curves, although the sensitivities to the bank bill curves should be zero.
 * <p>
 * The discounting curve contains the overnight deposit rate and OIS. The 3m bank bill curve contains the 3m bank bill rate,
 * 3m FRAs, 3m/3m fixed / float swaps and 3m/6m basis swaps. The 6m bank bill curve contains the 6m bank bill rate, 3m/6m basis
 * swaps and 6m/6m fixed / float swaps.
 */
@Test(groups = TestGroup.UNIT)
public class AudDiscounting3mBankBill6mBankBillTest extends CurveBuildingTests {
  /** The interpolator used for all curves */
  private static final Interpolator1D INTERPOLATOR = NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME);
  /** A calendar containing only Saturday and Sunday holidays */
  private static final CalendarAdapter SYD = new CalendarAdapter(WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
  /** The base FX matrix */
  private static final FXMatrix FX_MATRIX = new FXMatrix(Currency.AUD);
  /** Generates OIS swaps for the discounting curve */
  private static final GeneratorSwapFixedON GENERATOR_OIS_AUD = GeneratorSwapFixedONMaster.getInstance().getGenerator("AUD1YRBAON", SYD);
  /** An overnight AUD index */
  private static final IndexON AUD_OVERNIGHT_INDEX = GENERATOR_OIS_AUD.getIndex();
  /** Generates the overnight deposit */
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_AUD =
      new GeneratorDepositON("AUD Deposit ON", Currency.AUD, SYD, AUD_OVERNIGHT_INDEX.getDayCount());
  /** Generates 3m fixed / 3m float swaps */
  private static final GeneratorSwapFixedIbor AUD3MBBSW3M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("AUD3MBBSW3M", SYD);
  /** Generates 6m fixed / 6m float swaps */
  private static final GeneratorSwapFixedIbor AUD6MBBSW6M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("AUD6MBBSW6M", SYD);
  /** Generates 3m float / 6m float swaps */
  private static final GeneratorSwapIborIbor AUDBBSW3MBBSW6M = GeneratorSwapIborIborMaster.getInstance().getGenerator("AUDBBSW3MBBSW6M", SYD);
  /** A 3M AUD bank bill index */
  private static final IborIndex AUD_3M_BANK_BILL_INDEX = AUD3MBBSW3M.getIborIndex();
  /** A 6M AUD bank bill index */
  private static final IborIndex AUD_6M_BANK_BILL_INDEX = AUD6MBBSW6M.getIborIndex();
  /** Generates 3m FRAs */
  private static final GeneratorFRA GENERATOR_FRA_3M = new GeneratorFRA("GENERATOR_FRA_3M", AUD_3M_BANK_BILL_INDEX, SYD);
  /** Generates the 3m bank bill deposit */
  private static final GeneratorDepositIbor GENERATOR_AUDBB3M = new GeneratorDepositIbor("GENERATOR_AUDBB3M", AUD_3M_BANK_BILL_INDEX, SYD);
  /** Generates the 6m bank bill deposit */
  private static final GeneratorDepositIbor GENERATOR_AUDBB6M = new GeneratorDepositIbor("GENERATOR_AUDBB6M", AUD_6M_BANK_BILL_INDEX, SYD);
  /** The curve construction date */
  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2011, 9, 28);
  /** The previous working day */
  private static final ZonedDateTime PREVIOUS_DATE = NOW.minusDays(1);
  /** Overnight index fixing series after today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_AUD_WITH_TODAY =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] { PREVIOUS_DATE, NOW }, new double[] {0.07, 0.08 });
  /** Overnight index fixing series before today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_AUD_WITHOUT_TODAY =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] { PREVIOUS_DATE }, new double[] {0.07 });
  /** 3m bank bill fixing series after today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_AUD3M_WITH_TODAY =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] { PREVIOUS_DATE, NOW }, new double[] {0.0035, 0.0036 });
  /** 3m bank bill fixing series before today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_AUD3M_WITHOUT_TODAY =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] { PREVIOUS_DATE }, new double[] {0.0035 });
  /** 6m bank bill fixing series after today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_AUD6M_WITH_TODAY =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] { PREVIOUS_DATE, NOW }, new double[] {0.0035, 0.0036 });
  /** 6m bank bill fixing series before today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_AUD6M_WITHOUT_TODAY =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] { PREVIOUS_DATE }, new double[] {0.0035 });
  /** Fixing time series created before the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITHOUT_TODAY = new HashMap<>();
  /** Fixing time series created after the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITH_TODAY = new HashMap<>();

  static {
    FIXING_TS_WITH_TODAY.put(AUD_OVERNIGHT_INDEX, TS_ON_AUD_WITH_TODAY);
    FIXING_TS_WITH_TODAY.put(AUD_3M_BANK_BILL_INDEX, TS_IBOR_AUD3M_WITH_TODAY);
    FIXING_TS_WITH_TODAY.put(AUD_6M_BANK_BILL_INDEX, TS_IBOR_AUD6M_WITH_TODAY);
    FIXING_TS_WITHOUT_TODAY.put(AUD_OVERNIGHT_INDEX, TS_ON_AUD_WITHOUT_TODAY);
    FIXING_TS_WITHOUT_TODAY.put(AUD_3M_BANK_BILL_INDEX, TS_IBOR_AUD3M_WITHOUT_TODAY);
    FIXING_TS_WITHOUT_TODAY.put(AUD_6M_BANK_BILL_INDEX, TS_IBOR_AUD6M_WITHOUT_TODAY);
  }
  /** The discounting curve name */
  private static final String CURVE_NAME_DSC_AUD = "AUD Dsc";
  /** The 3m bank bill curve name */
  private static final String CURVE_NAME_FWD3_AUD = "AUD Fwd 3M";
  /** The 6m bank bill curve name */
  private static final String CURVE_NAME_FWD6_AUD = "AUD Fwd 6M";
  /** Market values for the discounting curve */
  private static final double[] DSC_AUD_MARKET_QUOTES =
      new double[] {0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400 };
  /** Vanilla instrument generators for the discounting curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_AUD_GENERATORS =
      new GeneratorInstrument<?>[] {GENERATOR_DEPOSIT_ON_AUD, GENERATOR_OIS_AUD, GENERATOR_OIS_AUD,
    GENERATOR_OIS_AUD, GENERATOR_OIS_AUD, GENERATOR_OIS_AUD, GENERATOR_OIS_AUD, GENERATOR_OIS_AUD, GENERATOR_OIS_AUD,
    GENERATOR_OIS_AUD, GENERATOR_OIS_AUD, GENERATOR_OIS_AUD };
  /** Tenors for the discounting curve */
  private static final GeneratorAttributeIR[] DSC_AUD_ATTR;
  static {
    final Period[] tenors = new Period[] {Period.ofDays(0), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6),
        Period.ofMonths(9), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
    DSC_AUD_ATTR = new GeneratorAttributeIR[tenors.length];
    for (int i = 0; i < tenors.length; i++) {
      DSC_AUD_ATTR[i] = new GeneratorAttributeIR(tenors[i]);
    }
  }
  /** Market values for the 3m bank bill curve */
  private static final double[] FWD3_AUD_MARKET_QUOTES = new double[] {0.0420, 0.0420, 0.0420, 0.0420, 0.0430, 0.0470, 0.0020, 0.0020, 0.0020 };
  /** Vanilla instrument generators for the 3m bank bill curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD3_AUD_GENERATORS =
      new GeneratorInstrument<?>[] {GENERATOR_AUDBB3M, GENERATOR_FRA_3M, GENERATOR_FRA_3M, AUD3MBBSW3M, AUD3MBBSW3M, AUD3MBBSW3M, AUDBBSW3MBBSW6M,
    AUDBBSW3MBBSW6M, AUDBBSW3MBBSW6M };
  /** Attribute generators for the 3m bank bill curve */
  private static final GeneratorAttributeIR[] FWD3_AUD_ATTR;
  static {
    final Period[] tenors = new Period[] {Period.ofMonths(0), Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1), Period.ofYears(2),
        Period.ofYears(3), Period.ofYears(5), Period.ofYears(7), Period.ofYears(10) };
    FWD3_AUD_ATTR = new GeneratorAttributeIR[tenors.length];
    for (int i = 0; i < tenors.length; i++) {
      FWD3_AUD_ATTR[i] = new GeneratorAttributeIR(tenors[i]);
    }
  }
  /** Market values for the 6m bank bill curve */
  private static final double[] FWD6_AUD_MARKET_QUOTES = new double[] {0.0440, 0.0020, 0.0020, 0.0020, 0.0560, 0.0610, 0.0620 };
  /** Vanilla instrument generators for the 6m bank bill curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD6_AUD_GENERATORS =
      new GeneratorInstrument<?>[] {GENERATOR_AUDBB6M, AUDBBSW3MBBSW6M, AUDBBSW3MBBSW6M, AUDBBSW3MBBSW6M, AUD6MBBSW6M, AUD6MBBSW6M, AUD6MBBSW6M };
  /** Attribute generators for the 6m bank bill curve */
    private static final GeneratorAttributeIR[] FWD6_AUD_ATTR;
  static {
    final Period[] tenors = new Period[] {Period.ofMonths(0), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(5),
        Period.ofYears(7), Period.ofYears(10) };
    FWD6_AUD_ATTR = new GeneratorAttributeIR[tenors.length];
    for (int i = 0; i < tenors.length; i++) {
      FWD6_AUD_ATTR[i] = new GeneratorAttributeIR(tenors[i]);
    }
  }
  /** Already known curve data - contains only an empty FX matrix */
  private static final MulticurveProviderDiscount MULTICURVE_KNOWN_DATA = new MulticurveProviderDiscount(FX_MATRIX);
  /** Builder that constructs the discounting curve before the two bank bill curves */
  private static final DiscountingMethodCurveSetUp DISCOUNTING_THEN_BANK_BILLS_BUILDER = DiscountingMethodCurveBuilder.setUp()
      .buildingFirst(CURVE_NAME_DSC_AUD)
      .using(CURVE_NAME_DSC_AUD).forDiscounting(Currency.AUD).forOvernightIndex(AUD_OVERNIGHT_INDEX).withInterpolator(INTERPOLATOR)
      .thenBuilding(CURVE_NAME_FWD3_AUD, CURVE_NAME_FWD6_AUD)
      .using(CURVE_NAME_FWD3_AUD).forIborIndex(AUD_3M_BANK_BILL_INDEX).withInterpolator(INTERPOLATOR)
      .using(CURVE_NAME_FWD6_AUD).forIborIndex(AUD_6M_BANK_BILL_INDEX).withInterpolator(INTERPOLATOR)
      .withKnownData(MULTICURVE_KNOWN_DATA);
  /** Builder that constructs three curves */
  private static final DiscountingMethodCurveSetUp DISCOUNTING_AND_BANK_BILLS_BUILDER = DiscountingMethodCurveBuilder.setUp()
      .building(CURVE_NAME_DSC_AUD, CURVE_NAME_FWD3_AUD, CURVE_NAME_FWD6_AUD)
      .using(CURVE_NAME_DSC_AUD).forDiscounting(Currency.AUD).forOvernightIndex(AUD_OVERNIGHT_INDEX).withInterpolator(INTERPOLATOR)
      .using(CURVE_NAME_FWD3_AUD).forIborIndex(AUD_3M_BANK_BILL_INDEX).withInterpolator(INTERPOLATOR)
      .using(CURVE_NAME_FWD6_AUD).forIborIndex(AUD_6M_BANK_BILL_INDEX).withInterpolator(INTERPOLATOR)
      .withKnownData(MULTICURVE_KNOWN_DATA);
  static {
    for (int i = 0; i < DSC_AUD_MARKET_QUOTES.length; i++) {
      DISCOUNTING_THEN_BANK_BILLS_BUILDER.withNode(CURVE_NAME_DSC_AUD, DSC_AUD_GENERATORS[i], DSC_AUD_ATTR[i], DSC_AUD_MARKET_QUOTES[i]);
      DISCOUNTING_AND_BANK_BILLS_BUILDER.withNode(CURVE_NAME_DSC_AUD, DSC_AUD_GENERATORS[i], DSC_AUD_ATTR[i], DSC_AUD_MARKET_QUOTES[i]);
    }
    for (int i = 0; i < FWD3_AUD_MARKET_QUOTES.length; i++) {
      DISCOUNTING_THEN_BANK_BILLS_BUILDER.withNode(CURVE_NAME_FWD3_AUD, FWD3_AUD_GENERATORS[i], FWD3_AUD_ATTR[i], FWD3_AUD_MARKET_QUOTES[i]);
      DISCOUNTING_AND_BANK_BILLS_BUILDER.withNode(CURVE_NAME_FWD3_AUD, FWD3_AUD_GENERATORS[i], FWD3_AUD_ATTR[i], FWD3_AUD_MARKET_QUOTES[i]);
    }
    for (int i = 0; i < FWD6_AUD_MARKET_QUOTES.length; i++) {
      DISCOUNTING_THEN_BANK_BILLS_BUILDER.withNode(CURVE_NAME_FWD6_AUD, FWD6_AUD_GENERATORS[i], FWD6_AUD_ATTR[i], FWD6_AUD_MARKET_QUOTES[i]);
      DISCOUNTING_AND_BANK_BILLS_BUILDER.withNode(CURVE_NAME_FWD6_AUD, FWD6_AUD_GENERATORS[i], FWD6_AUD_ATTR[i], FWD6_AUD_MARKET_QUOTES[i]);
    }
  }
  /** Simultaneous discounting and bank bill curves before today's fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> DSC_BANK_BILLS_SIMULTANEOUS_BEFORE_FIXING;
  /** Simultaneous discounting and bank bill curves after today's fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> DSC_THEN_BANK_BILLS_BEFORE_FIXING;
  /** Discounting then bank bill curves before today's fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> DSC_BANK_BILLS_SIMULTANEOUS_AFTER_FIXING;
  /** Discounting then bank bill curves after today's fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> DSC_THEN_BANK_BILLS_AFTER_FIXING;

  static {
    DSC_BANK_BILLS_SIMULTANEOUS_BEFORE_FIXING = DISCOUNTING_AND_BANK_BILLS_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().buildCurves(NOW);
    DSC_BANK_BILLS_SIMULTANEOUS_AFTER_FIXING = DISCOUNTING_AND_BANK_BILLS_BUILDER.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().buildCurves(NOW);
    DSC_THEN_BANK_BILLS_BEFORE_FIXING = DISCOUNTING_THEN_BANK_BILLS_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().buildCurves(NOW);
    DSC_THEN_BANK_BILLS_AFTER_FIXING = DISCOUNTING_THEN_BANK_BILLS_BUILDER.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().buildCurves(NOW);
  }
  /** Calculation tolerance */
  private static final double EPS = 1.0e-9;

  @Override
  @Test
  public void testJacobianSize() {
    final int allQuotes = DSC_AUD_MARKET_QUOTES.length + FWD3_AUD_MARKET_QUOTES.length + FWD6_AUD_MARKET_QUOTES.length;
    // discounting curve first, then two coupled bank bill curves
    CurveBuildingBlockBundle fullJacobian = DSC_THEN_BANK_BILLS_BEFORE_FIXING.getSecond();
    Map<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> fullJacobianData = fullJacobian.getData();
    assertEquals(fullJacobianData.size(), 3);
    DoubleMatrix2D discountingJacobianMatrix = fullJacobianData.get(CURVE_NAME_DSC_AUD).getSecond();
    assertEquals(discountingJacobianMatrix.getNumberOfRows(), DSC_AUD_MARKET_QUOTES.length);
    assertEquals(discountingJacobianMatrix.getNumberOfColumns(), DSC_AUD_MARKET_QUOTES.length);
    DoubleMatrix2D bankBill3mJacobianMatrix = fullJacobianData.get(CURVE_NAME_FWD3_AUD).getSecond();
    assertEquals(bankBill3mJacobianMatrix.getNumberOfRows(), FWD3_AUD_MARKET_QUOTES.length);
    assertEquals(bankBill3mJacobianMatrix.getNumberOfColumns(), allQuotes);
    DoubleMatrix2D libor6mJacobianMatrix = fullJacobianData.get(CURVE_NAME_FWD6_AUD).getSecond();
    assertEquals(libor6mJacobianMatrix.getNumberOfRows(), FWD6_AUD_MARKET_QUOTES.length);
    assertEquals(libor6mJacobianMatrix.getNumberOfColumns(), allQuotes);
    // three curves fitted at the same time
    fullJacobian = DSC_BANK_BILLS_SIMULTANEOUS_BEFORE_FIXING.getSecond();
    fullJacobianData = fullJacobian.getData();
    assertEquals(fullJacobianData.size(), 3);
    discountingJacobianMatrix = fullJacobianData.get(CURVE_NAME_DSC_AUD).getSecond();
    assertEquals(discountingJacobianMatrix.getNumberOfRows(), DSC_AUD_MARKET_QUOTES.length);
    assertEquals(discountingJacobianMatrix.getNumberOfColumns(), allQuotes);
    bankBill3mJacobianMatrix = fullJacobianData.get(CURVE_NAME_FWD3_AUD).getSecond();
    assertEquals(bankBill3mJacobianMatrix.getNumberOfRows(), FWD3_AUD_MARKET_QUOTES.length);
    assertEquals(bankBill3mJacobianMatrix.getNumberOfColumns(), allQuotes);
    libor6mJacobianMatrix = fullJacobianData.get(CURVE_NAME_FWD6_AUD).getSecond();
    assertEquals(libor6mJacobianMatrix.getNumberOfRows(), FWD6_AUD_MARKET_QUOTES.length);
    assertEquals(libor6mJacobianMatrix.getNumberOfColumns(), allQuotes);
  }

  @Override
  @Test
  public void testInstrumentsInCurvePriceToZero() {
    // discounting then 3m then 6m
    Map<String, InstrumentDefinition<?>[]> definitions;
    // before fixing
    definitions = DISCOUNTING_THEN_BANK_BILLS_BUILDER.copy()
        .withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().getDefinitionsForCurves(NOW);
    curveConstructionTest(definitions.get(CURVE_NAME_DSC_AUD), DSC_THEN_BANK_BILLS_BEFORE_FIXING.getFirst(), PresentValueDiscountingCalculator.getInstance(),
        FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.AUD);
    curveConstructionTest(definitions.get(CURVE_NAME_FWD3_AUD), DSC_THEN_BANK_BILLS_BEFORE_FIXING.getFirst(), PresentValueDiscountingCalculator.getInstance(),
        FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.AUD);
    curveConstructionTest(definitions.get(CURVE_NAME_FWD6_AUD), DSC_THEN_BANK_BILLS_BEFORE_FIXING.getFirst(), PresentValueDiscountingCalculator.getInstance(),
        FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.AUD);
    // after fixing
    definitions = DISCOUNTING_THEN_BANK_BILLS_BUILDER.copy()
        .withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().getDefinitionsForCurves(NOW);
    curveConstructionTest(definitions.get(CURVE_NAME_DSC_AUD), DSC_THEN_BANK_BILLS_AFTER_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.AUD);
    curveConstructionTest(definitions.get(CURVE_NAME_FWD3_AUD), DSC_THEN_BANK_BILLS_AFTER_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.AUD);
    curveConstructionTest(definitions.get(CURVE_NAME_FWD6_AUD), DSC_THEN_BANK_BILLS_AFTER_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.AUD);
    // discounting and bank bills
    // before fixing
    definitions = DISCOUNTING_AND_BANK_BILLS_BUILDER.copy()
        .withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().getDefinitionsForCurves(NOW);
    curveConstructionTest(definitions.get(CURVE_NAME_DSC_AUD), DSC_BANK_BILLS_SIMULTANEOUS_BEFORE_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.AUD);
    curveConstructionTest(definitions.get(CURVE_NAME_FWD3_AUD), DSC_BANK_BILLS_SIMULTANEOUS_BEFORE_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.AUD);
    curveConstructionTest(definitions.get(CURVE_NAME_FWD6_AUD), DSC_BANK_BILLS_SIMULTANEOUS_BEFORE_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.AUD);
    // after fixing
    definitions = DISCOUNTING_AND_BANK_BILLS_BUILDER.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().getDefinitionsForCurves(NOW);
    curveConstructionTest(definitions.get(CURVE_NAME_DSC_AUD), DSC_BANK_BILLS_SIMULTANEOUS_AFTER_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.AUD);
    curveConstructionTest(definitions.get(CURVE_NAME_FWD3_AUD), DSC_BANK_BILLS_SIMULTANEOUS_AFTER_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.AUD);
    curveConstructionTest(definitions.get(CURVE_NAME_FWD6_AUD), DSC_BANK_BILLS_SIMULTANEOUS_AFTER_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.AUD);
  }

  @Override
  @Test
  public void testFiniteDifferenceSensitivities() {
    testDiscountingCurveSensitivities1(DSC_THEN_BANK_BILLS_BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY);
    testDiscountingCurveSensitivities1(DSC_THEN_BANK_BILLS_AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY);
    testDiscountingCurveSensitivities2(DSC_BANK_BILLS_SIMULTANEOUS_BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY);
    testDiscountingCurveSensitivities2(DSC_BANK_BILLS_SIMULTANEOUS_AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY);
    test3mBankBillCurveSensitivities(DSC_THEN_BANK_BILLS_BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, DISCOUNTING_THEN_BANK_BILLS_BUILDER);
    test3mBankBillCurveSensitivities(DSC_THEN_BANK_BILLS_AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY, DISCOUNTING_THEN_BANK_BILLS_BUILDER);
    test3mBankBillCurveSensitivities(DSC_BANK_BILLS_SIMULTANEOUS_BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, DISCOUNTING_AND_BANK_BILLS_BUILDER);
    test3mBankBillCurveSensitivities(DSC_BANK_BILLS_SIMULTANEOUS_AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY, DISCOUNTING_AND_BANK_BILLS_BUILDER);
    test6mBankBillCurveSensitivities(DSC_THEN_BANK_BILLS_BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, DISCOUNTING_THEN_BANK_BILLS_BUILDER);
    test6mBankBillCurveSensitivities(DSC_THEN_BANK_BILLS_AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY, DISCOUNTING_THEN_BANK_BILLS_BUILDER);
    test6mBankBillCurveSensitivities(DSC_BANK_BILLS_SIMULTANEOUS_BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, DISCOUNTING_AND_BANK_BILLS_BUILDER);
    test6mBankBillCurveSensitivities(DSC_BANK_BILLS_SIMULTANEOUS_AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY, DISCOUNTING_AND_BANK_BILLS_BUILDER);
  }

  /**
   * Tests the sensitivities of the discounting curve to changes in the market data points used in the
   * curves when the discounting curve has no sensitivity to the bank bill curve.
   * @param fullInverseJacobian  analytic sensitivities
   * @param fixingTs  the fixing time series
   */
  private static void testDiscountingCurveSensitivities1(final CurveBuildingBlockBundle fullInverseJacobian,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs) {
    // sensitivities to discounting
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, DISCOUNTING_THEN_BANK_BILLS_BUILDER, CURVE_NAME_DSC_AUD,
        CURVE_NAME_DSC_AUD, NOW, DSC_AUD_GENERATORS, DSC_AUD_ATTR, DSC_AUD_MARKET_QUOTES, false);
    // sensitivities to 3m bank bills should not have been calculated
    assertNoSensitivities(fullInverseJacobian, CURVE_NAME_DSC_AUD, CURVE_NAME_FWD3_AUD);
    // sensitivities to 6m bank bills should not have been calculated
    assertNoSensitivities(fullInverseJacobian, CURVE_NAME_DSC_AUD, CURVE_NAME_FWD6_AUD);
  }

  /**
   * Tests the sensitivities of the discounting curve to changes in the market data points used in the
   * curves when the discounting curve is constructed before the bank bill curve. Sensitivities to the
   * bank bill curve market data are calculated, but they should be equal to zero.
   * @param fullInverseJacobian  analytic sensitivities
   * @param fixingTs  the fixing time series
   */
  private static void testDiscountingCurveSensitivities2(final CurveBuildingBlockBundle fullInverseJacobian,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs) {
    // sensitivities to discounting
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, DISCOUNTING_AND_BANK_BILLS_BUILDER, CURVE_NAME_DSC_AUD,
        CURVE_NAME_DSC_AUD, NOW, DSC_AUD_GENERATORS, DSC_AUD_ATTR, DSC_AUD_MARKET_QUOTES, false);
    // sensitivities to 3m bank bills should be zero
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, DISCOUNTING_AND_BANK_BILLS_BUILDER, CURVE_NAME_DSC_AUD,
        CURVE_NAME_FWD3_AUD, NOW, FWD3_AUD_GENERATORS, FWD3_AUD_ATTR, FWD3_AUD_MARKET_QUOTES, true);
    // sensitivities to 6m bank bills should be zero
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, DISCOUNTING_AND_BANK_BILLS_BUILDER, CURVE_NAME_DSC_AUD,
        CURVE_NAME_FWD6_AUD, NOW, FWD6_AUD_GENERATORS, FWD6_AUD_ATTR, FWD6_AUD_MARKET_QUOTES, true);
  }

  /**
   * Tests the sensitivities of the 3m bank bill curve to changes in the market data points used in the
   * curves.
   * @param fullInverseJacobian  analytic sensitivities
   * @param fixingTs  the fixing time series
   * @param builder  the curve builder
   */
  private static void test3mBankBillCurveSensitivities(final CurveBuildingBlockBundle fullInverseJacobian,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs, final DiscountingMethodCurveSetUp builder) {
    // sensitivities to discounting
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, builder, CURVE_NAME_FWD3_AUD,
        CURVE_NAME_DSC_AUD, NOW, DSC_AUD_GENERATORS, DSC_AUD_ATTR, DSC_AUD_MARKET_QUOTES, false);
    // sensitivities to 3m bank bills
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, builder, CURVE_NAME_FWD3_AUD,
        CURVE_NAME_FWD3_AUD, NOW, FWD3_AUD_GENERATORS, FWD3_AUD_ATTR, FWD3_AUD_MARKET_QUOTES, false);
    // sensitivities to 6m bank bills
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, builder, CURVE_NAME_FWD3_AUD,
        CURVE_NAME_FWD6_AUD, NOW, FWD6_AUD_GENERATORS, FWD6_AUD_ATTR, FWD6_AUD_MARKET_QUOTES, false);
  }

  /**
   * Tests the sensitivities of the 6m bank bill curve to changes in the market data points used in the
   * curves.
   * @param fullInverseJacobian  analytic sensitivities
   * @param fixingTs  the fixing time series
   * @param builder  the curve builder
   */
  private static void test6mBankBillCurveSensitivities(final CurveBuildingBlockBundle fullInverseJacobian,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs, final DiscountingMethodCurveSetUp builder) {
    // sensitivities to discounting
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, builder, CURVE_NAME_FWD6_AUD,
        CURVE_NAME_DSC_AUD, NOW, DSC_AUD_GENERATORS, DSC_AUD_ATTR, DSC_AUD_MARKET_QUOTES, false);
    // sensitivities to 3m bank bills
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, builder, CURVE_NAME_FWD6_AUD,
        CURVE_NAME_FWD3_AUD, NOW, FWD3_AUD_GENERATORS, FWD3_AUD_ATTR, FWD3_AUD_MARKET_QUOTES, false);
    // sensitivities to 6m bank bills
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, builder, CURVE_NAME_FWD6_AUD,
        CURVE_NAME_FWD6_AUD, NOW, FWD6_AUD_GENERATORS, FWD6_AUD_ATTR, FWD6_AUD_MARKET_QUOTES, false);
  }

  @Override
  @Test
  public void testSameCurvesDifferentMethods() {
    // discounting curves
    YieldAndDiscountCurve curveBefore1 = DSC_THEN_BANK_BILLS_BEFORE_FIXING.getFirst().getCurve(Currency.AUD);
    YieldAndDiscountCurve curveBefore2 = DSC_BANK_BILLS_SIMULTANEOUS_BEFORE_FIXING.getFirst().getCurve(Currency.AUD);
    assertYieldCurvesEqual(curveBefore1, curveBefore2, EPS);
    YieldAndDiscountCurve curveAfter1 = DSC_THEN_BANK_BILLS_AFTER_FIXING.getFirst().getCurve(Currency.AUD);
    YieldAndDiscountCurve curveAfter2 = DSC_BANK_BILLS_SIMULTANEOUS_AFTER_FIXING.getFirst().getCurve(Currency.AUD);
    assertYieldCurvesEqual(curveAfter1, curveAfter2, EPS);
    // bank bill curves
    curveBefore1 = DSC_THEN_BANK_BILLS_BEFORE_FIXING.getFirst().getCurve(AUD_3M_BANK_BILL_INDEX);
    curveBefore2 = DSC_BANK_BILLS_SIMULTANEOUS_BEFORE_FIXING.getFirst().getCurve(AUD_3M_BANK_BILL_INDEX);
    assertYieldCurvesEqual(curveBefore1, curveBefore2, EPS);
    curveAfter1 = DSC_THEN_BANK_BILLS_AFTER_FIXING.getFirst().getCurve(AUD_3M_BANK_BILL_INDEX);
    curveAfter2 = DSC_BANK_BILLS_SIMULTANEOUS_AFTER_FIXING.getFirst().getCurve(AUD_3M_BANK_BILL_INDEX);
    assertYieldCurvesEqual(curveAfter1, curveAfter2, EPS);
    curveBefore1 = DSC_THEN_BANK_BILLS_BEFORE_FIXING.getFirst().getCurve(AUD_6M_BANK_BILL_INDEX);
    curveBefore2 = DSC_BANK_BILLS_SIMULTANEOUS_BEFORE_FIXING.getFirst().getCurve(AUD_6M_BANK_BILL_INDEX);
    assertYieldCurvesEqual(curveBefore1, curveBefore2, EPS);
    curveAfter1 = DSC_THEN_BANK_BILLS_AFTER_FIXING.getFirst().getCurve(AUD_6M_BANK_BILL_INDEX);
    curveAfter2 = DSC_BANK_BILLS_SIMULTANEOUS_AFTER_FIXING.getFirst().getCurve(AUD_6M_BANK_BILL_INDEX);
    assertYieldCurvesEqual(curveAfter1, curveAfter2, EPS);
    // discounting sensitivities are not the same, but the 6m bank bill matrices should be the same for both construction methods
    final DoubleMatrix2D matrixBefore1 = DSC_THEN_BANK_BILLS_BEFORE_FIXING.getSecond().getBlock(CURVE_NAME_FWD3_AUD).getSecond();
    final DoubleMatrix2D matrixBefore2 = DSC_BANK_BILLS_SIMULTANEOUS_BEFORE_FIXING.getSecond().getBlock(CURVE_NAME_FWD3_AUD).getSecond();
    assertMatrixEquals(matrixBefore1, matrixBefore2, EPS);
    final DoubleMatrix2D matrixAfter1 = DSC_THEN_BANK_BILLS_AFTER_FIXING.getSecond().getBlock(CURVE_NAME_FWD6_AUD).getSecond();
    final DoubleMatrix2D matrixAfter2 = DSC_BANK_BILLS_SIMULTANEOUS_AFTER_FIXING.getSecond().getBlock(CURVE_NAME_FWD6_AUD).getSecond();
    assertMatrixEquals(matrixAfter1, matrixAfter2, EPS);
  }

  /**
   * Performance test.
   */
  @Test(enabled = false)
  public void performance() {
    long startTime, endTime;
    final int nbTest = 100;

    startTime = System.currentTimeMillis();
    DiscountingMethodCurveBuilder builder = DISCOUNTING_THEN_BANK_BILLS_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder();
    for (int i = 0; i < nbTest; i++) {
      builder.buildCurves(NOW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " x 3 curves construction / 2 units: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 2 units: 08-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 810 ms for 100 sets.

    startTime = System.currentTimeMillis();
    builder = DISCOUNTING_AND_BANK_BILLS_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      builder.buildCurves(NOW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " x 3 curves construction / 1 unit: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 1 unit: 08-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 995 ms for 100 sets.

  }

  /**
   * Analyzes the shape of the forward curve.
   */
  @Test(enabled = false)
  public void forwardAnalysis() {
    final MulticurveProviderInterface marketDsc = DSC_THEN_BANK_BILLS_BEFORE_FIXING.getFirst();
    final int jump = 1;
    final int startIndex = 0;
    final int nbDate = 2750;
    ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(NOW, AUD_3M_BANK_BILL_INDEX.getSpotLag() + startIndex * jump, SYD);
    final double[] rateDsc = new double[nbDate];
    final double[] startTime = new double[nbDate];
    try (final FileWriter writer = new FileWriter("fwd-dsc.csv")) {
      for (int i = 0; i < nbDate; i++) {
        startTime[i] = TimeCalculator.getTimeBetween(NOW, startDate);
        final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, AUD_3M_BANK_BILL_INDEX, SYD);
        final double endTime = TimeCalculator.getTimeBetween(NOW, endDate);
        final double accrualFactor = AUD_3M_BANK_BILL_INDEX.getDayCount().getDayCountFraction(startDate, endDate);
        rateDsc[i] = marketDsc.getSimplyCompoundForwardRate(AUD_3M_BANK_BILL_INDEX, startTime[i], endTime, accrualFactor);
        startDate = ScheduleCalculator.getAdjustedDate(startDate, jump, SYD);
        writer.append(0.0 + "," + startTime[i] + "," + rateDsc[i] + "\n");
      }
      writer.flush();
      writer.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

}
