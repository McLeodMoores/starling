/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve.discounting;

import static com.opengamma.analytics.financial.provider.curve.discounting.DiscountingMethodCurveUtils.curveConstructionTest;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.date.CalendarAdapter;
import com.opengamma.analytics.date.WeekendWorkingDayCalendar;
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
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.Index;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.builder.CurveBuilderSetUp;
import com.opengamma.analytics.financial.provider.curve.builder.DiscountingMethodCurveBuilder;
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
 * Build of curve in several blocks with relevant Jacobian matrices.
 * Three curves in EUR; no futures.
 */
@Test(groups = TestGroup.UNIT)
public class EurDiscounting3mLibor6mLiborTest {
  private static final Interpolator1D INTERPOLATOR = NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME);
  private static final CalendarAdapter TARGET = new CalendarAdapter(WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
  private static final FXMatrix FX_MATRIX = new FXMatrix(Currency.EUR);
  private static final GeneratorSwapFixedON GENERATOR_OIS_EUR = GeneratorSwapFixedONMaster.getInstance().getGenerator("EUR1YEONIA", TARGET);
  /** An overnight EUR index */
  private static final IndexON EUR_OVERNIGHT_INDEX = GENERATOR_OIS_EUR.getIndex();
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_EUR = new GeneratorDepositON("EUR Deposit ON", Currency.EUR, TARGET, EUR_OVERNIGHT_INDEX.getDayCount());
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR3M = GENERATOR_SWAP_MASTER.getGenerator("EUR1YEURIBOR3M", TARGET);
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR6M = GENERATOR_SWAP_MASTER.getGenerator("EUR1YEURIBOR6M", TARGET);
  /** A 3M EURIBOR index */
  private static final IborIndex EUR_3M_EURIBOR_INDEX = EUR1YEURIBOR3M.getIborIndex();
  /** A 6M EURIBOR index */
  private static final IborIndex EUR_6M_EURIBOR_INDEX = EUR1YEURIBOR6M.getIborIndex();
  /** A 3M EUR LIBOR index */
  private static final IborIndex EUR_3M_LIBOR_INDEX = new IborIndex(Currency.EUR, Period.ofMonths(3), 2, EUR_3M_EURIBOR_INDEX.getDayCount(), EUR_3M_EURIBOR_INDEX.getBusinessDayConvention(), true, "EUROLIBOR3M");
  /** A 6M EUR LIBOR index */
  private static final IborIndex EUR_6M_LIBOR_INDEX = new IborIndex(Currency.EUR, Period.ofMonths(6), 2, EUR_6M_EURIBOR_INDEX.getDayCount(), EUR_6M_EURIBOR_INDEX.getBusinessDayConvention(), true, "EUROLIBOR6M");
  private static final GeneratorFRA GENERATOR_FRA_3M = new GeneratorFRA("GENERATOR_FRA_3M", EUR_3M_EURIBOR_INDEX, TARGET);
  private static final GeneratorFRA GENERATOR_FRA_6M = new GeneratorFRA("GENERATOR_FRA_6M", EUR_6M_EURIBOR_INDEX, TARGET);
  private static final GeneratorDepositIbor GENERATOR_EURIBOR3M = new GeneratorDepositIbor("GENERATOR_EURIBOR3M", EUR_3M_EURIBOR_INDEX, TARGET);
  private static final GeneratorDepositIbor GENERATOR_EURIBOR6M = new GeneratorDepositIbor("GENERATOR_EURIBOR6M", EUR_6M_EURIBOR_INDEX, TARGET);

  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2011, 9, 28);

  private static final ZonedDateTimeDoubleTimeSeries TS_ON_EUR_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_EUR_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.07, 0.08 });

  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_EUR3M_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.0035, 0.0036 });
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_EUR3M_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27) },
      new double[] {0.0035 });
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_EUR6M_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
      DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.0036, 0.0037 });
    private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_EUR6M_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27) },
        new double[] {0.0036 });

  /** Fixing time series created before the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITHOUT_TODAY = new HashMap<>();
  /** Fixing time series created after the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITH_TODAY = new HashMap<>();
  static {
    FIXING_TS_WITHOUT_TODAY.put(EUR_OVERNIGHT_INDEX, TS_ON_EUR_WITHOUT_TODAY);
    FIXING_TS_WITHOUT_TODAY.put(EUR_3M_LIBOR_INDEX, TS_IBOR_EUR3M_WITHOUT_TODAY);
    FIXING_TS_WITHOUT_TODAY.put(EUR_3M_EURIBOR_INDEX, TS_IBOR_EUR3M_WITHOUT_TODAY);
    FIXING_TS_WITHOUT_TODAY.put(EUR_6M_LIBOR_INDEX, TS_IBOR_EUR6M_WITHOUT_TODAY);
    FIXING_TS_WITHOUT_TODAY.put(EUR_6M_EURIBOR_INDEX, TS_IBOR_EUR6M_WITHOUT_TODAY);
    FIXING_TS_WITH_TODAY.put(EUR_OVERNIGHT_INDEX, TS_ON_EUR_WITH_TODAY);
    FIXING_TS_WITH_TODAY.put(EUR_3M_LIBOR_INDEX, TS_IBOR_EUR3M_WITH_TODAY);
    FIXING_TS_WITH_TODAY.put(EUR_3M_EURIBOR_INDEX, TS_IBOR_EUR3M_WITH_TODAY);
    FIXING_TS_WITH_TODAY.put(EUR_6M_LIBOR_INDEX, TS_IBOR_EUR6M_WITH_TODAY);
    FIXING_TS_WITH_TODAY.put(EUR_6M_EURIBOR_INDEX, TS_IBOR_EUR6M_WITH_TODAY);
  }

  private static final String CURVE_NAME_DSC_EUR = "EUR Dsc";
  private static final String CURVE_NAME_FWD3_EUR = "EUR Fwd 3M";
  private static final String CURVE_NAME_FWD6_EUR = "EUR Fwd 6M";

  /** Market values for the dsc EUR curve */
  private static final double[] DSC_EUR_MARKET_QUOTES = new double[] {0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400 };
  /** Generators for the dsc EUR curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_EUR_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_DEPOSIT_ON_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR,
    GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR };
  /** Tenors for the dsc EUR curve */
  private static final Period[] DSC_EUR_TENOR = new Period[] {Period.ofDays(0), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3),
    Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1),
    Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
  private static final GeneratorAttributeIR[] DSC_EUR_ATTR = new GeneratorAttributeIR[DSC_EUR_TENOR.length];
  static {
    for (int i = 0; i < DSC_EUR_TENOR.length; i++) {
      DSC_EUR_ATTR[i] = new GeneratorAttributeIR(DSC_EUR_TENOR[i]);
    }
  }

  /** Market values for the Fwd 3M EUR curve */
  private static final double[] FWD3_EUR_MARKET_QUOTES = new double[] {0.0420, 0.0420, 0.0420, 0.0420, 0.0430, 0.0470, 0.0540, 0.0570, 0.0600 };
  /** Generators for the Fwd 3M EUR curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD3_EUR_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_EURIBOR3M, GENERATOR_FRA_3M, GENERATOR_FRA_3M, EUR1YEURIBOR3M,
    EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M };
  /** Tenors for the Fwd 3M EUR curve */
  private static final Period[] FWD3_EUR_TENOR = new Period[] {Period.ofMonths(0), Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1),
    Period.ofYears(2), Period.ofYears(3), Period.ofYears(5),
    Period.ofYears(7), Period.ofYears(10) };
  private static final GeneratorAttributeIR[] FWD3_EUR_ATTR = new GeneratorAttributeIR[FWD3_EUR_TENOR.length];
  static {
    for (int i = 0; i < FWD3_EUR_TENOR.length; i++) {
      FWD3_EUR_ATTR[i] = new GeneratorAttributeIR(FWD3_EUR_TENOR[i]);
    }
  }

  /** Market values for the Fwd 3M EUR curve */
  private static final double[] FWD6_EUR_MARKET_QUOTES = new double[] {0.0440, 0.0440, 0.0440, 0.0445, 0.0485, 0.0555, 0.0580, 0.0610 };
  /** Generators for the Fwd 3M EUR curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD6_EUR_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_EURIBOR6M, GENERATOR_FRA_6M, GENERATOR_FRA_6M, EUR1YEURIBOR6M,
    EUR1YEURIBOR6M, EUR1YEURIBOR6M, EUR1YEURIBOR6M, EUR1YEURIBOR6M };
  /** Tenors for the Fwd 3M EUR curve */
  private static final Period[] FWD6_EUR_TENOR = new Period[] {Period.ofMonths(0), Period.ofMonths(9), Period.ofMonths(12), Period.ofYears(2),
    Period.ofYears(3), Period.ofYears(5), Period.ofYears(7),
    Period.ofYears(10) };
  private static final GeneratorAttributeIR[] FWD6_EUR_ATTR = new GeneratorAttributeIR[FWD6_EUR_TENOR.length];
  static {
    for (int i = 0; i < FWD6_EUR_TENOR.length; i++) {
      FWD6_EUR_ATTR[i] = new GeneratorAttributeIR(FWD6_EUR_TENOR[i]);
    }
  }
  private static final MulticurveProviderDiscount MULTICURVE_KNOWN_DATA = new MulticurveProviderDiscount(FX_MATRIX);
  private static final CurveBuilderSetUp DISCOUNTING_THEN_LIBORS_BUILDER = DiscountingMethodCurveBuilder.setUp()
      .buildingFirst(CURVE_NAME_DSC_EUR)
      .using(CURVE_NAME_DSC_EUR).forDiscounting(Currency.EUR).forOvernightIndex(EUR_OVERNIGHT_INDEX).withInterpolator(INTERPOLATOR)
      .thenBuilding(CURVE_NAME_FWD3_EUR)
      .using(CURVE_NAME_FWD3_EUR).forIborIndex(EUR_3M_LIBOR_INDEX, EUR_3M_EURIBOR_INDEX).withInterpolator(INTERPOLATOR)
      .thenBuilding(CURVE_NAME_FWD6_EUR)
      .using(CURVE_NAME_FWD6_EUR).forIborIndex(EUR_6M_LIBOR_INDEX, EUR_6M_EURIBOR_INDEX).withInterpolator(INTERPOLATOR)
      .withKnownData(MULTICURVE_KNOWN_DATA);
  private static final CurveBuilderSetUp DISCOUNTING_AND_LIBORS_BUILDER = DiscountingMethodCurveBuilder.setUp()
      .building(CURVE_NAME_DSC_EUR, CURVE_NAME_FWD3_EUR, CURVE_NAME_FWD6_EUR)
      .using(CURVE_NAME_DSC_EUR).forDiscounting(Currency.EUR).forOvernightIndex(EUR_OVERNIGHT_INDEX).withInterpolator(INTERPOLATOR)
      .using(CURVE_NAME_FWD3_EUR).forIborIndex(EUR_3M_LIBOR_INDEX, EUR_3M_EURIBOR_INDEX).withInterpolator(INTERPOLATOR)
      .using(CURVE_NAME_FWD6_EUR).forIborIndex(EUR_6M_LIBOR_INDEX, EUR_6M_EURIBOR_INDEX).withInterpolator(INTERPOLATOR)
      .withKnownData(MULTICURVE_KNOWN_DATA);
  static {
    for (int i = 0; i < DSC_EUR_MARKET_QUOTES.length; i++) {
      DISCOUNTING_THEN_LIBORS_BUILDER.withNode(CURVE_NAME_DSC_EUR, DSC_EUR_GENERATORS[i], DSC_EUR_ATTR[i], DSC_EUR_MARKET_QUOTES[i]);
      DISCOUNTING_AND_LIBORS_BUILDER.withNode(CURVE_NAME_DSC_EUR, DSC_EUR_GENERATORS[i], DSC_EUR_ATTR[i], DSC_EUR_MARKET_QUOTES[i]);
    }
    for (int i = 0; i < FWD3_EUR_MARKET_QUOTES.length; i++) {
      DISCOUNTING_THEN_LIBORS_BUILDER.withNode(CURVE_NAME_FWD3_EUR, FWD3_EUR_GENERATORS[i], FWD3_EUR_ATTR[i], FWD3_EUR_MARKET_QUOTES[i]);
      DISCOUNTING_AND_LIBORS_BUILDER.withNode(CURVE_NAME_FWD3_EUR, FWD3_EUR_GENERATORS[i], FWD3_EUR_ATTR[i], FWD3_EUR_MARKET_QUOTES[i]);
    }
    for (int i = 0; i < FWD6_EUR_MARKET_QUOTES.length; i++) {
      DISCOUNTING_THEN_LIBORS_BUILDER.withNode(CURVE_NAME_FWD6_EUR, FWD6_EUR_GENERATORS[i], FWD6_EUR_ATTR[i], FWD6_EUR_MARKET_QUOTES[i]);
      DISCOUNTING_AND_LIBORS_BUILDER.withNode(CURVE_NAME_FWD6_EUR, FWD6_EUR_GENERATORS[i], FWD6_EUR_ATTR[i], FWD6_EUR_MARKET_QUOTES[i]);
    }
    DISCOUNTING_THEN_LIBORS_BUILDER.withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().buildCurves(NOW);
    DISCOUNTING_AND_LIBORS_BUILDER.withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().buildCurves(NOW);
  }

  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> DSC_LIBOR_SIMULTANEOUS_BEFORE_FIXING;
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> DSC_THEN_LIBOR_BEFORE_FIXING;
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> DSC_LIBOR_SIMULTANEOUS_AFTER_FIXING;
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> DSC_THEN_LIBOR_AFTER_FIXING;

  private static final double TOLERANCE_CAL = 1.0E-9;

  static {
    DSC_LIBOR_SIMULTANEOUS_BEFORE_FIXING = DISCOUNTING_AND_LIBORS_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().buildCurves(NOW);
    DSC_LIBOR_SIMULTANEOUS_AFTER_FIXING = DISCOUNTING_AND_LIBORS_BUILDER.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().buildCurves(NOW);
    DSC_THEN_LIBOR_BEFORE_FIXING = DISCOUNTING_THEN_LIBORS_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().buildCurves(NOW);
    DSC_THEN_LIBOR_AFTER_FIXING = DISCOUNTING_THEN_LIBORS_BUILDER.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().buildCurves(NOW);
  }

  @Test
  public void testJacobianSizes() {
    final int allQuotes = DSC_EUR_MARKET_QUOTES.length + FWD3_EUR_MARKET_QUOTES.length + FWD6_EUR_MARKET_QUOTES.length;
    // discounting curve first, then 3m, then 6m
    CurveBuildingBlockBundle fullJacobian = DSC_THEN_LIBOR_BEFORE_FIXING.getSecond();
    Map<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> fullJacobianData = fullJacobian.getData();
    assertEquals(fullJacobianData.size(), 3);
    DoubleMatrix2D discountingJacobianMatrix = fullJacobianData.get(CURVE_NAME_DSC_EUR).getSecond();
    assertEquals(discountingJacobianMatrix.getNumberOfRows(), DSC_EUR_MARKET_QUOTES.length);
    assertEquals(discountingJacobianMatrix.getNumberOfColumns(), DSC_EUR_MARKET_QUOTES.length);
    DoubleMatrix2D libor3mJacobianMatrix = fullJacobianData.get(CURVE_NAME_FWD3_EUR).getSecond();
    assertEquals(libor3mJacobianMatrix.getNumberOfRows(), FWD3_EUR_MARKET_QUOTES.length);
    assertEquals(libor3mJacobianMatrix.getNumberOfColumns(), DSC_EUR_MARKET_QUOTES.length + FWD3_EUR_MARKET_QUOTES.length);
    DoubleMatrix2D libor6mJacobianMatrix = fullJacobianData.get(CURVE_NAME_FWD6_EUR).getSecond();
    assertEquals(libor6mJacobianMatrix.getNumberOfRows(), FWD6_EUR_MARKET_QUOTES.length);
    assertEquals(libor6mJacobianMatrix.getNumberOfColumns(), allQuotes);
    // three curves fitted at the same time
    fullJacobian = DSC_LIBOR_SIMULTANEOUS_BEFORE_FIXING.getSecond();
    fullJacobianData = fullJacobian.getData();
    assertEquals(fullJacobianData.size(), 3);
    discountingJacobianMatrix = fullJacobianData.get(CURVE_NAME_DSC_EUR).getSecond();
    assertEquals(discountingJacobianMatrix.getNumberOfRows(), DSC_EUR_MARKET_QUOTES.length);
    assertEquals(discountingJacobianMatrix.getNumberOfColumns(), allQuotes);
    libor3mJacobianMatrix = fullJacobianData.get(CURVE_NAME_FWD3_EUR).getSecond();
    assertEquals(libor3mJacobianMatrix.getNumberOfRows(), FWD3_EUR_MARKET_QUOTES.length);
    assertEquals(libor3mJacobianMatrix.getNumberOfColumns(), allQuotes);
    libor6mJacobianMatrix = fullJacobianData.get(CURVE_NAME_FWD6_EUR).getSecond();
    assertEquals(libor6mJacobianMatrix.getNumberOfRows(), FWD6_EUR_MARKET_QUOTES.length);
    assertEquals(libor6mJacobianMatrix.getNumberOfColumns(), allQuotes);
  }

  @Test
  public void testInstrumentsInCurvePriceToZero() {
    InstrumentDefinition<?>[] definitions;
    // discounting then 3m then 6m
    Map<String, InstrumentDefinition<?>[]> definitionsForDiscountingThenLibors;
    // before fixing
    definitionsForDiscountingThenLibors = DISCOUNTING_THEN_LIBORS_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().getDefinitionsForCurves(NOW);
    definitions = definitionsForDiscountingThenLibors.get(CURVE_NAME_DSC_EUR);
    curveConstructionTest(definitions, DSC_THEN_LIBOR_BEFORE_FIXING.getFirst(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.EUR);
    definitions = definitionsForDiscountingThenLibors.get(CURVE_NAME_FWD3_EUR);
    curveConstructionTest(definitions, DSC_THEN_LIBOR_BEFORE_FIXING.getFirst(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.EUR);
    definitions = definitionsForDiscountingThenLibors.get(CURVE_NAME_FWD6_EUR);
    curveConstructionTest(definitions, DSC_THEN_LIBOR_BEFORE_FIXING.getFirst(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.EUR);
    // after fixing
    definitionsForDiscountingThenLibors = DISCOUNTING_THEN_LIBORS_BUILDER.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().getDefinitionsForCurves(NOW);
    definitions = definitionsForDiscountingThenLibors.get(CURVE_NAME_DSC_EUR);
    curveConstructionTest(definitions, DSC_THEN_LIBOR_AFTER_FIXING.getFirst(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.EUR);
    definitions = definitionsForDiscountingThenLibors.get(CURVE_NAME_FWD3_EUR);
    curveConstructionTest(definitions, DSC_THEN_LIBOR_AFTER_FIXING.getFirst(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.EUR);
    definitions = definitionsForDiscountingThenLibors.get(CURVE_NAME_FWD6_EUR);
    curveConstructionTest(definitions, DSC_THEN_LIBOR_AFTER_FIXING.getFirst(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.EUR);
    // discounting and libors
    Map<String, InstrumentDefinition<?>[]> definitionsForDiscountingAndLibors;
    // before fixing
    definitionsForDiscountingAndLibors = DISCOUNTING_AND_LIBORS_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().getDefinitionsForCurves(NOW);
    definitions = definitionsForDiscountingAndLibors.get(CURVE_NAME_DSC_EUR);
    curveConstructionTest(definitions, DSC_LIBOR_SIMULTANEOUS_BEFORE_FIXING.getFirst(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.EUR);
    definitions = definitionsForDiscountingAndLibors.get(CURVE_NAME_FWD3_EUR);
    curveConstructionTest(definitions, DSC_LIBOR_SIMULTANEOUS_BEFORE_FIXING.getFirst(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.EUR);
    definitions = definitionsForDiscountingAndLibors.get(CURVE_NAME_FWD6_EUR);
    curveConstructionTest(definitions, DSC_LIBOR_SIMULTANEOUS_BEFORE_FIXING.getFirst(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.EUR);
    // after fixing
    definitionsForDiscountingAndLibors = DISCOUNTING_AND_LIBORS_BUILDER.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().getDefinitionsForCurves(NOW);
    definitions = definitionsForDiscountingAndLibors.get(CURVE_NAME_DSC_EUR);
    curveConstructionTest(definitions, DSC_LIBOR_SIMULTANEOUS_AFTER_FIXING.getFirst(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.EUR);
    definitions = definitionsForDiscountingAndLibors.get(CURVE_NAME_FWD3_EUR);
    curveConstructionTest(definitions, DSC_LIBOR_SIMULTANEOUS_AFTER_FIXING.getFirst(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.EUR);
    definitions = definitionsForDiscountingAndLibors.get(CURVE_NAME_FWD6_EUR);
    curveConstructionTest(definitions, DSC_LIBOR_SIMULTANEOUS_AFTER_FIXING.getFirst(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.EUR);
  }

  // discounting curve has no sensitivity to 3m or 6m libor
  @Test
  public void blockBundleDscFiniteDifferenceTest1() {
    final int discountingCurveSize = DSC_EUR_MARKET_QUOTES.length;
    final CurveBuildingBlockBundle fullInverseJacobian = DSC_THEN_LIBOR_BEFORE_FIXING.getSecond();
    final double bump = 1e-6;
    for (int i = 0; i < discountingCurveSize; i++) {
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults = DISCOUNTING_THEN_LIBORS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_DSC_EUR, DSC_EUR_GENERATORS[i], DSC_EUR_ATTR[i], DSC_EUR_MARKET_QUOTES[i] + bump)
          .buildCurves(NOW);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults = DISCOUNTING_THEN_LIBORS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_DSC_EUR, DSC_EUR_GENERATORS[i], DSC_EUR_ATTR[i], DSC_EUR_MARKET_QUOTES[i] - bump)
          .buildCurves(NOW);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_DSC_EUR)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_DSC_EUR)).getCurve().getYData();
      for (int j = 0; j < discountingCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        // note columns then rows tested
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_DSC_EUR).getSecond().getData()[j][i];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_DSC_EUR + ": column=" + j + " row=" + i,
            expectedSensitivity, dYielddQuote, bump);
      }
    }
  }

  @Test
  public void blockBundleDscFiniteDifferenceTest2() {
    final int discountingCurveSize = DSC_EUR_MARKET_QUOTES.length;
    final int libor3mCurveSize = FWD3_EUR_MARKET_QUOTES.length;
    final int libor6mCurveSize = FWD6_EUR_MARKET_QUOTES.length;
    final CurveBuildingBlockBundle fullInverseJacobian = DSC_LIBOR_SIMULTANEOUS_BEFORE_FIXING.getSecond();
    final double bump = 1e-6;
    for (int i = 0; i < discountingCurveSize; i++) {
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults = DISCOUNTING_AND_LIBORS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_DSC_EUR, DSC_EUR_GENERATORS[i], DSC_EUR_ATTR[i], DSC_EUR_MARKET_QUOTES[i] + bump)
          .buildCurves(NOW);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults = DISCOUNTING_AND_LIBORS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_DSC_EUR, DSC_EUR_GENERATORS[i], DSC_EUR_ATTR[i], DSC_EUR_MARKET_QUOTES[i] - bump)
          .buildCurves(NOW);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_DSC_EUR)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_DSC_EUR)).getCurve().getYData();
      for (int j = 0; j < discountingCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        // note columns then rows tested
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_DSC_EUR).getSecond().getData()[j][i];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_DSC_EUR + ": column=" + i + " row=" + j,
            expectedSensitivity, dYielddQuote, bump);
      }
    }
    for (int i = 0; i < libor3mCurveSize; i++) {
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults = DISCOUNTING_AND_LIBORS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_FWD3_EUR, FWD3_EUR_GENERATORS[i], FWD3_EUR_ATTR[i], FWD3_EUR_MARKET_QUOTES[i] + bump)
          .buildCurves(NOW);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults = DISCOUNTING_AND_LIBORS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_FWD3_EUR, FWD3_EUR_GENERATORS[i], FWD3_EUR_ATTR[i], FWD3_EUR_MARKET_QUOTES[i] - bump)
          .buildCurves(NOW);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_DSC_EUR)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_DSC_EUR)).getCurve().getYData();
      final int offset = i + discountingCurveSize;
      for (int j = 0; j < discountingCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_DSC_EUR).getSecond().getData()[j][offset];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_DSC_EUR + ": column=" + offset + " row=" + j,
            expectedSensitivity, dYielddQuote, bump);
        assertEquals(expectedSensitivity, 0, 1e-12);
      }
    }
    for (int i = 0; i < libor6mCurveSize; i++) {
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults = DISCOUNTING_AND_LIBORS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_FWD6_EUR, FWD6_EUR_GENERATORS[i], FWD6_EUR_ATTR[i], FWD6_EUR_MARKET_QUOTES[i] + bump)
          .buildCurves(NOW);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults = DISCOUNTING_AND_LIBORS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_FWD6_EUR, FWD6_EUR_GENERATORS[i], FWD6_EUR_ATTR[i], FWD6_EUR_MARKET_QUOTES[i] - bump)
          .buildCurves(NOW);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_DSC_EUR)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_DSC_EUR)).getCurve().getYData();
      final int offset = i + libor3mCurveSize + discountingCurveSize;
      for (int j = 0; j < discountingCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_DSC_EUR).getSecond().getData()[j][offset];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_DSC_EUR + ": column=" + offset + " row=" + j,
            expectedSensitivity, dYielddQuote, bump);
        assertEquals(expectedSensitivity, 0, bump);
      }
    }
  }

  // 3m curve has no sensitivity to 6m
  @Test
  public void blockBundleFwd3MFiniteDifferenceTest1() {
    final int discountingCurveSize = DSC_EUR_MARKET_QUOTES.length;
    final int libor3mCurveSize = FWD3_EUR_MARKET_QUOTES.length;
    final CurveBuildingBlockBundle fullInverseJacobian = DSC_THEN_LIBOR_BEFORE_FIXING.getSecond();
    final double bump = 1e-6;

    for (int i = 0; i < discountingCurveSize; i++) {
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults = DISCOUNTING_THEN_LIBORS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_DSC_EUR, DSC_EUR_GENERATORS[i], DSC_EUR_ATTR[i], DSC_EUR_MARKET_QUOTES[i] + bump)
          .buildCurves(NOW);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults = DISCOUNTING_THEN_LIBORS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_DSC_EUR, DSC_EUR_GENERATORS[i], DSC_EUR_ATTR[i], DSC_EUR_MARKET_QUOTES[i] - bump)
          .buildCurves(NOW);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_FWD3_EUR)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_FWD3_EUR)).getCurve().getYData();
      for (int j = 0; j < libor3mCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_FWD3_EUR).getSecond().getData()[j][i];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_FWD3_EUR + ": column=" + i + " row=" + j,
            expectedSensitivity, dYielddQuote, bump);
      }
    }
    for (int i = 0; i < libor3mCurveSize; i++) {
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults = DISCOUNTING_THEN_LIBORS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_FWD3_EUR, FWD3_EUR_GENERATORS[i], FWD3_EUR_ATTR[i], FWD3_EUR_MARKET_QUOTES[i] + bump)
          .buildCurves(NOW);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults = DISCOUNTING_THEN_LIBORS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_FWD3_EUR, FWD3_EUR_GENERATORS[i], FWD3_EUR_ATTR[i], FWD3_EUR_MARKET_QUOTES[i] - bump)
          .buildCurves(NOW);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_FWD3_EUR)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_FWD3_EUR)).getCurve().getYData();
      final int offset = i + discountingCurveSize;
      for (int j = 0; j < libor3mCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_FWD3_EUR).getSecond().getData()[j][offset];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_FWD3_EUR + ": column=" + offset + " row=" + j,
            expectedSensitivity, dYielddQuote, bump);
      }
    }
  }

  @Test
  public void blockBundleFwd3MFiniteDifferenceTest2() {
    final int discountingCurveSize = DSC_EUR_MARKET_QUOTES.length;
    final int libor3mCurveSize = FWD3_EUR_MARKET_QUOTES.length;
    final int libor6mCurveSize = FWD6_EUR_MARKET_QUOTES.length;
    final CurveBuildingBlockBundle fullInverseJacobian = DSC_LIBOR_SIMULTANEOUS_BEFORE_FIXING.getSecond();
    final double bump = 1e-6;

    for (int i = 0; i < discountingCurveSize; i++) {
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults = DISCOUNTING_AND_LIBORS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_DSC_EUR, DSC_EUR_GENERATORS[i], DSC_EUR_ATTR[i], DSC_EUR_MARKET_QUOTES[i] + bump)
          .buildCurves(NOW);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults = DISCOUNTING_AND_LIBORS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_DSC_EUR, DSC_EUR_GENERATORS[i], DSC_EUR_ATTR[i], DSC_EUR_MARKET_QUOTES[i] - bump)
          .buildCurves(NOW);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_FWD3_EUR)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_FWD3_EUR)).getCurve().getYData();
      for (int j = 0; j < libor3mCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_FWD3_EUR).getSecond().getData()[j][i];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_FWD3_EUR + ": column=" + i + " row=" + j,
            expectedSensitivity, dYielddQuote, bump);
      }
    }
    for (int i = 0; i < libor3mCurveSize; i++) {
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults = DISCOUNTING_AND_LIBORS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_FWD3_EUR, FWD3_EUR_GENERATORS[i], FWD3_EUR_ATTR[i], FWD3_EUR_MARKET_QUOTES[i] + bump)
          .buildCurves(NOW);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults = DISCOUNTING_AND_LIBORS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_FWD3_EUR, FWD3_EUR_GENERATORS[i], FWD3_EUR_ATTR[i], FWD3_EUR_MARKET_QUOTES[i] - bump)
          .buildCurves(NOW);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_FWD3_EUR)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_FWD3_EUR)).getCurve().getYData();
      final int offset = i + discountingCurveSize;
      for (int j = 0; j < libor3mCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_FWD3_EUR).getSecond().getData()[j][offset];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_FWD3_EUR + ": column=" + offset + " row=" + j,
            expectedSensitivity, dYielddQuote, bump);
      }
    }
    for (int i = 0; i < libor6mCurveSize; i++) {
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults = DISCOUNTING_AND_LIBORS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_FWD6_EUR, FWD6_EUR_GENERATORS[i], FWD6_EUR_ATTR[i], FWD6_EUR_MARKET_QUOTES[i] + bump)
          .buildCurves(NOW);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults = DISCOUNTING_AND_LIBORS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_FWD6_EUR, FWD6_EUR_GENERATORS[i], FWD6_EUR_ATTR[i], FWD6_EUR_MARKET_QUOTES[i] - bump)
          .buildCurves(NOW);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_FWD3_EUR)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_FWD3_EUR)).getCurve().getYData();
      final int offset = i + libor3mCurveSize + discountingCurveSize;
      for (int j = 0; j < libor3mCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_FWD3_EUR).getSecond().getData()[j][offset];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_FWD3_EUR + ": column=" + offset + " row=" + j,
            expectedSensitivity, dYielddQuote, bump);
      }
    }
  }

  @Test
  public void blockBundleFwd6MFiniteDifferenceTest1() {
    final int discountingCurveSize = DSC_EUR_MARKET_QUOTES.length;
    final int libor3mCurveSize = FWD3_EUR_MARKET_QUOTES.length;
    final int libor6mCurveSize = FWD6_EUR_MARKET_QUOTES.length;
    final CurveBuildingBlockBundle fullInverseJacobian = DSC_THEN_LIBOR_BEFORE_FIXING.getSecond();
    final double bump = 1e-6;

    for (int i = 0; i < discountingCurveSize; i++) {
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults = DISCOUNTING_THEN_LIBORS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_DSC_EUR, DSC_EUR_GENERATORS[i], DSC_EUR_ATTR[i], DSC_EUR_MARKET_QUOTES[i] + bump)
          .buildCurves(NOW);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults = DISCOUNTING_THEN_LIBORS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_DSC_EUR, DSC_EUR_GENERATORS[i], DSC_EUR_ATTR[i], DSC_EUR_MARKET_QUOTES[i] - bump)
          .buildCurves(NOW);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_FWD6_EUR)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_FWD6_EUR)).getCurve().getYData();
      for (int j = 0; j < libor6mCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_FWD6_EUR).getSecond().getData()[j][i];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_FWD6_EUR + ": column=" + i + " row=" + j,
            expectedSensitivity, dYielddQuote, bump);
      }
    }
    for (int i = 0; i < libor3mCurveSize; i++) {
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults = DISCOUNTING_THEN_LIBORS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_FWD3_EUR, FWD3_EUR_GENERATORS[i], FWD3_EUR_ATTR[i], FWD3_EUR_MARKET_QUOTES[i] + bump)
          .buildCurves(NOW);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults = DISCOUNTING_THEN_LIBORS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_FWD3_EUR, FWD3_EUR_GENERATORS[i], FWD3_EUR_ATTR[i], FWD3_EUR_MARKET_QUOTES[i] - bump)
          .buildCurves(NOW);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_FWD6_EUR)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_FWD6_EUR)).getCurve().getYData();
      final int offset = i + discountingCurveSize;
      for (int j = 0; j < libor6mCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_FWD6_EUR).getSecond().getData()[j][offset];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_FWD6_EUR + ": column=" + offset + " row=" + j,
            expectedSensitivity, dYielddQuote, bump);
      }
    }
    for (int i = 0; i < libor6mCurveSize; i++) {
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults = DISCOUNTING_AND_LIBORS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_FWD6_EUR, FWD6_EUR_GENERATORS[i], FWD6_EUR_ATTR[i], FWD6_EUR_MARKET_QUOTES[i] + bump)
          .buildCurves(NOW);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults = DISCOUNTING_AND_LIBORS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_FWD6_EUR, FWD6_EUR_GENERATORS[i], FWD6_EUR_ATTR[i], FWD6_EUR_MARKET_QUOTES[i] - bump)
          .buildCurves(NOW);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_FWD6_EUR)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_FWD6_EUR)).getCurve().getYData();
      final int offset = i + libor3mCurveSize + discountingCurveSize;
      for (int j = 0; j < libor6mCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_FWD6_EUR).getSecond().getData()[j][offset];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_FWD6_EUR + ": column=" + offset + " row=" + j,
            expectedSensitivity, dYielddQuote, bump);
      }
    }
  }

  @Test
  public void blockBundleFwd6MFiniteDifferenceTest2() {
    final int discountingCurveSize = DSC_EUR_MARKET_QUOTES.length;
    final int libor3mCurveSize = FWD3_EUR_MARKET_QUOTES.length;
    final int libor6mCurveSize = FWD6_EUR_MARKET_QUOTES.length;
    final CurveBuildingBlockBundle fullInverseJacobian = DSC_LIBOR_SIMULTANEOUS_BEFORE_FIXING.getSecond();
    final double bump = 1e-6;

    for (int i = 0; i < discountingCurveSize; i++) {
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults = DISCOUNTING_AND_LIBORS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_DSC_EUR, DSC_EUR_GENERATORS[i], DSC_EUR_ATTR[i], DSC_EUR_MARKET_QUOTES[i] + bump)
          .buildCurves(NOW);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults = DISCOUNTING_AND_LIBORS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_DSC_EUR, DSC_EUR_GENERATORS[i], DSC_EUR_ATTR[i], DSC_EUR_MARKET_QUOTES[i] - bump)
          .buildCurves(NOW);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_FWD6_EUR)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_FWD6_EUR)).getCurve().getYData();
      for (int j = 0; j < libor6mCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_FWD6_EUR).getSecond().getData()[j][i];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_FWD6_EUR + ": column=" + i + " row=" + j,
            expectedSensitivity, dYielddQuote, bump);
      }
    }
    for (int i = 0; i < libor3mCurveSize; i++) {
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults = DISCOUNTING_AND_LIBORS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_FWD3_EUR, FWD3_EUR_GENERATORS[i], FWD3_EUR_ATTR[i], FWD3_EUR_MARKET_QUOTES[i] + bump)
          .buildCurves(NOW);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults = DISCOUNTING_AND_LIBORS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_FWD3_EUR, FWD3_EUR_GENERATORS[i], FWD3_EUR_ATTR[i], FWD3_EUR_MARKET_QUOTES[i] - bump)
          .buildCurves(NOW);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_FWD6_EUR)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_FWD6_EUR)).getCurve().getYData();
      final int offset = i + discountingCurveSize;
      for (int j = 0; j < libor6mCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_FWD6_EUR).getSecond().getData()[j][offset];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_FWD6_EUR + ": column=" + offset + " row=" + j,
            expectedSensitivity, dYielddQuote, bump);
      }
    }
    for (int i = 0; i < libor6mCurveSize; i++) {
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults = DISCOUNTING_AND_LIBORS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_FWD6_EUR, FWD6_EUR_GENERATORS[i], FWD6_EUR_ATTR[i], FWD6_EUR_MARKET_QUOTES[i] + bump)
          .buildCurves(NOW);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults = DISCOUNTING_AND_LIBORS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_FWD6_EUR, FWD6_EUR_GENERATORS[i], FWD6_EUR_ATTR[i], FWD6_EUR_MARKET_QUOTES[i] - bump)
          .buildCurves(NOW);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_FWD6_EUR)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_FWD6_EUR)).getCurve().getYData();
      final int offset = i + libor3mCurveSize + discountingCurveSize;
      for (int j = 0; j < libor6mCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_FWD6_EUR).getSecond().getData()[j][offset];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_FWD6_EUR + ": column=" + offset + " row=" + j,
            expectedSensitivity, dYielddQuote, bump);
      }
    }
  }

  @Test
  public void comparison1Unit3Units() {
    final MulticurveProviderDiscount[] units =
        new MulticurveProviderDiscount[] { DSC_THEN_LIBOR_BEFORE_FIXING.getFirst(), DSC_LIBOR_SIMULTANEOUS_BEFORE_FIXING.getFirst() };
    final CurveBuildingBlockBundle[] bb =
        new CurveBuildingBlockBundle[] { DSC_THEN_LIBOR_BEFORE_FIXING.getSecond(), DSC_LIBOR_SIMULTANEOUS_BEFORE_FIXING.getSecond() };
    final YieldAndDiscountCurve[] curveDsc = new YieldAndDiscountCurve[] { units[0].getCurve(Currency.EUR), units[1].getCurve(Currency.EUR) };
    final YieldAndDiscountCurve[] curveFwd3 = new YieldAndDiscountCurve[] { units[0].getCurve(EUR_3M_LIBOR_INDEX), units[1].getCurve(EUR_3M_LIBOR_INDEX) };
    final YieldAndDiscountCurve[] curveFwd6 = new YieldAndDiscountCurve[] { units[0].getCurve(EUR_6M_LIBOR_INDEX), units[1].getCurve(EUR_6M_LIBOR_INDEX) };
    assertEquals("Curve construction: 1 unit / 3 units ", curveDsc[0].getNumberOfParameters(), curveDsc[1].getNumberOfParameters());
    assertEquals("Curve construction: 1 unit / 3 units ", curveFwd3[0].getNumberOfParameters(), curveFwd3[1].getNumberOfParameters());
    assertEquals("Curve construction: 1 unit / 3 units ", curveFwd6[0].getNumberOfParameters(), curveFwd6[1].getNumberOfParameters());
    assertArrayEquals("Curve construction: 1 unit / 3 units ", ArrayUtils.toPrimitive(((YieldCurve) curveDsc[0]).getCurve().getXData()),
        ArrayUtils.toPrimitive(((YieldCurve) curveDsc[1]).getCurve().getXData()), TOLERANCE_CAL);
    assertArrayEquals("Curve construction: 1 unit / 3 units ", ArrayUtils.toPrimitive(((YieldCurve) curveDsc[0]).getCurve().getYData()),
        ArrayUtils.toPrimitive(((YieldCurve) curveDsc[1]).getCurve().getYData()), TOLERANCE_CAL);
    assertArrayEquals("Curve construction: 1 unit / 3 units ", ArrayUtils.toPrimitive(((YieldCurve) curveFwd3[0]).getCurve().getXData()),
        ArrayUtils.toPrimitive(((YieldCurve) curveFwd3[1]).getCurve().getXData()), TOLERANCE_CAL);
    assertArrayEquals("Curve construction: 1 unit / 3 units ", ArrayUtils.toPrimitive(((YieldCurve) curveFwd3[0]).getCurve().getYData()),
        ArrayUtils.toPrimitive(((YieldCurve) curveFwd3[1]).getCurve().getYData()), TOLERANCE_CAL);
    assertArrayEquals("Curve construction: 1 unit / 3 units ", ArrayUtils.toPrimitive(((YieldCurve) curveFwd6[0]).getCurve().getXData()),
        ArrayUtils.toPrimitive(((YieldCurve) curveFwd6[1]).getCurve().getXData()), TOLERANCE_CAL);
    assertArrayEquals("Curve construction: 1 unit / 3 units ", ArrayUtils.toPrimitive(((YieldCurve) curveFwd6[0]).getCurve().getYData()),
        ArrayUtils.toPrimitive(((YieldCurve) curveFwd6[1]).getCurve().getYData()), TOLERANCE_CAL);

    assertEquals("Curve construction: 1 unit / 3 units ", bb[0].getBlock(CURVE_NAME_FWD6_EUR).getFirst(), bb[1].getBlock(CURVE_NAME_FWD6_EUR).getFirst());
  }

  @Test(enabled = false)
  public void performance() {
    long startTime, endTime;
    final int nbTest = 100;

    startTime = System.currentTimeMillis();
    DiscountingMethodCurveBuilder builder = DISCOUNTING_THEN_LIBORS_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder();
    for (int i = 0; i < nbTest; i++) {
      builder.buildCurves(NOW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction / 3 units: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 3 units: 07-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 655 ms for 100 sets.

    startTime = System.currentTimeMillis();
    builder = DISCOUNTING_AND_LIBORS_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder();
    for (int i = 0; i < nbTest; i++) {
      builder.buildCurves(NOW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction / 1 unit: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 1 unit: 07-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 635 ms for 100 sets.

  }

  /**
   * Analyzes the shape of the forward curve.
   */
  @Test(enabled = false)
  public void forwardAnalysis() {
    final MulticurveProviderInterface marketDsc = DSC_THEN_LIBOR_BEFORE_FIXING.getFirst();
    final int jump = 1;
    final int startIndex = 0;
    final int nbDate = 2750;
    ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(NOW, EUR_3M_EURIBOR_INDEX.getSpotLag() + startIndex * jump, TARGET);
    final double[] rateDsc = new double[nbDate];
    final double[] startTime = new double[nbDate];
    try (final FileWriter writer = new FileWriter("fwd-dsc.csv")) {
      for (int i = 0; i < nbDate; i++) {
        startTime[i] = TimeCalculator.getTimeBetween(NOW, startDate);
        final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, EUR_3M_EURIBOR_INDEX, TARGET);
        final double endTime = TimeCalculator.getTimeBetween(NOW, endDate);
        final double accrualFactor = EUR_3M_EURIBOR_INDEX.getDayCount().getDayCountFraction(startDate, endDate);
        rateDsc[i] = marketDsc.getSimplyCompoundForwardRate(EUR_3M_EURIBOR_INDEX, startTime[i], endTime, accrualFactor);
        startDate = ScheduleCalculator.getAdjustedDate(startDate, jump, TARGET);
        writer.append(0.0 + "," + startTime[i] + "," + rateDsc[i] + "\n");
      }
      writer.flush();
      writer.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

}
