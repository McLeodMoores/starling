/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve.discounting;

import static com.opengamma.analytics.financial.provider.curve.discounting.DiscountingMethodCurveUtils.curveConstructionTest;
import static org.testng.AssertJUnit.assertEquals;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.date.CalendarAdapter;
import com.opengamma.analytics.date.SimpleWorkingDayCalendar;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositON;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.Index;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.discounting.DiscountingMethodCurveUtils.DiscountingMethodCurveBuilder;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
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
 */
@Test(groups = TestGroup.UNIT)
public class UsdDiscounting3mLibor1Test {
  private static final CalendarAdapter NYC =
      new CalendarAdapter(new SimpleWorkingDayCalendar("NYC", Collections.<LocalDate>emptySet(), DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
  private static final FXMatrix FX_MATRIX = new FXMatrix(Currency.USD);
  private static final double NOTIONAL = 1.0;
  private static final GeneratorSwapFixedON GENERATOR_OIS_USD = GeneratorSwapFixedONMaster.getInstance().getGenerator("USD1YFEDFUND", NYC);
  /** An overnight USD index */
  private static final IndexON USD_OVERNIGHT_INDEX = GENERATOR_OIS_USD.getIndex();
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_USD = new GeneratorDepositON("USD Deposit ON", Currency.USD, NYC, USD_OVERNIGHT_INDEX.getDayCount());
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_SWAP_MASTER.getGenerator("USD6MLIBOR3M", NYC);
  /** A 3M USD LIBOR index */
  private static final IborIndex USD_3M_LIBOR_INDEX = USD6MLIBOR3M.getIborIndex();
  private static final GeneratorDepositIbor GENERATOR_USDLIBOR3M = new GeneratorDepositIbor("GENERATOR_USDLIBOR3M", USD_3M_LIBOR_INDEX, NYC);
  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2011, 9, 28);
  private static final ZonedDateTime PREVIOUS_DATE = NOW.minusDays(1);
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS_DATE, NOW}, new double[] {0.07, 0.08});
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS_DATE}, new double[] {0.07});
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_USD3M_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS_DATE, NOW}, new double[] {0.0035, 0.0036});
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_USD3M_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS_DATE}, new double[] {0.0035});
  /** Fixing time series created before the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITHOUT_TODAY = new HashMap<>();
  /** Fixing time series created after the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITH_TODAY = new HashMap<>();
  static {
    FIXING_TS_WITHOUT_TODAY.put(USD_OVERNIGHT_INDEX, TS_ON_USD_WITHOUT_TODAY);
    FIXING_TS_WITHOUT_TODAY.put(USD_3M_LIBOR_INDEX, TS_IBOR_USD3M_WITHOUT_TODAY);
    FIXING_TS_WITH_TODAY.put(USD_OVERNIGHT_INDEX, TS_ON_USD_WITH_TODAY);
    FIXING_TS_WITH_TODAY.put(USD_3M_LIBOR_INDEX, TS_IBOR_USD3M_WITH_TODAY);
  }
  private static final String CURVE_NAME_DSC_USD = "USD Dsc";
  private static final String CURVE_NAME_FWD3_USD = "USD Fwd 3M";
  /** Market values for the dsc USD curve */
  private static final double[] DSC_USD_MARKET_QUOTES = new double[] {0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400 };
  /** Generators for the dsc USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_USD_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_DEPOSIT_ON_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD,
    GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD };
  /** Tenors for the dsc USD curve */
  private static final GeneratorAttributeIR[] DSC_USD_ATTR;
  static {
    final Period[] dscUsdTenor = new Period[] {Period.ofDays(0), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3),
        Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
        Period.ofYears(10) };
    DSC_USD_ATTR = new GeneratorAttributeIR[dscUsdTenor.length];
    for (int i = 0; i < dscUsdTenor.length; i++) {
      DSC_USD_ATTR[i] = new GeneratorAttributeIR(dscUsdTenor[i]);
    }
  }
  /** Market values for the Fwd 3M USD curve */
  private static final double[] FWD3_USD_MARKET_QUOTES = new double[] {0.0420, 0.0420, 0.0420, 0.0430, 0.0470, 0.0540, 0.0570, 0.0600 };
  /** Generators for the Fwd 3M USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD3_USD_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_USDLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M,
    USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M };
  /** Tenors for the Fwd 3M USD curve */
  private static final GeneratorAttributeIR[] FWD3_USD_ATTR;
  static {
    final Period[] fwd3UsdTenor = new Period[] {Period.ofMonths(0), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2),
        Period.ofYears(3), Period.ofYears(5), Period.ofYears(7), Period.ofYears(10) };
    FWD3_USD_ATTR = new GeneratorAttributeIR[fwd3UsdTenor.length];
    for (int i = 0; i < fwd3UsdTenor.length; i++) {
      FWD3_USD_ATTR[i] = new GeneratorAttributeIR(fwd3UsdTenor[i]);
    }
  }
  private static final MulticurveProviderDiscount KNOWN_DATA = new MulticurveProviderDiscount(FX_MATRIX);

  private static final DiscountingMethodCurveBuilder.ConfigBuilder BUILDER_FOR_TEST = DiscountingMethodCurveBuilder.setUp()
      .buildingFirst(CURVE_NAME_DSC_USD)
      .using(CURVE_NAME_DSC_USD).forDiscounting(Currency.USD).forOvernightIndex(USD_OVERNIGHT_INDEX)
      .thenBuilding(CURVE_NAME_FWD3_USD)
      .using(CURVE_NAME_FWD3_USD).forIborIndex(USD_3M_LIBOR_INDEX)
      .withKnownData(KNOWN_DATA);
  static {
    for (int i = 0; i < DSC_USD_MARKET_QUOTES.length; i++) {
      BUILDER_FOR_TEST.withNode(CURVE_NAME_DSC_USD, DSC_USD_GENERATORS[i], DSC_USD_ATTR[i], DSC_USD_MARKET_QUOTES[i]);
    }
    for (int i = 0; i < FWD3_USD_MARKET_QUOTES.length; i++) {
      BUILDER_FOR_TEST.withNode(CURVE_NAME_FWD3_USD, FWD3_USD_GENERATORS[i], FWD3_USD_ATTR[i], FWD3_USD_MARKET_QUOTES[i]);
    }
  }
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> BEFORE_TODAYS_FIXING;
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> AFTER_TODAYS_FIXING;

  private static final double TOLERANCE_CAL = 1.0E-9;

  static {
    BEFORE_TODAYS_FIXING = BUILDER_FOR_TEST.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().buildCurves(NOW);
    AFTER_TODAYS_FIXING = BUILDER_FOR_TEST.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().buildCurves(NOW);
  }

  @Test
  public void testJacobianSizes() {
    final CurveBuildingBlockBundle fullJacobian = BEFORE_TODAYS_FIXING.getSecond();
    final Map<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> fullJacobianData = fullJacobian.getData();
    assertEquals(fullJacobianData.size(), 2);
    final DoubleMatrix2D discountingJacobianMatrix = fullJacobianData.get(CURVE_NAME_DSC_USD).getSecond();
    assertEquals(discountingJacobianMatrix.getNumberOfRows(), DSC_USD_MARKET_QUOTES.length);
    assertEquals(discountingJacobianMatrix.getNumberOfColumns(), DSC_USD_MARKET_QUOTES.length);
    final DoubleMatrix2D liborJacobianMatrix = fullJacobianData.get(CURVE_NAME_FWD3_USD).getSecond();
    assertEquals(liborJacobianMatrix.getNumberOfRows(), FWD3_USD_MARKET_QUOTES.length);
    assertEquals(liborJacobianMatrix.getNumberOfColumns(), FWD3_USD_MARKET_QUOTES.length + DSC_USD_MARKET_QUOTES.length);
  }

  @Test
  public void testInstrumentsInCurvePriceToZero() {
    final Map<String, InstrumentDefinition<?>[]> definitionsForCurvesBeforeFixing = BUILDER_FOR_TEST.copy()
        .withFixingTs(FIXING_TS_WITHOUT_TODAY)
        .getBuilder()
        .getDefinitionsForCurves(NOW);
    final Map<String, InstrumentDefinition<?>[]> definitionsForCurvesAfterFixing = BUILDER_FOR_TEST.copy()
        .withFixingTs(FIXING_TS_WITH_TODAY)
        .getBuilder()
        .getDefinitionsForCurves(NOW);
    curveConstructionTest(definitionsForCurvesBeforeFixing.get(CURVE_NAME_DSC_USD), BEFORE_TODAYS_FIXING.getFirst(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW);
    curveConstructionTest(definitionsForCurvesAfterFixing.get(CURVE_NAME_DSC_USD), AFTER_TODAYS_FIXING.getFirst(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW);
    curveConstructionTest(definitionsForCurvesBeforeFixing.get(CURVE_NAME_FWD3_USD), BEFORE_TODAYS_FIXING.getFirst(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW);
    curveConstructionTest(definitionsForCurvesAfterFixing.get(CURVE_NAME_FWD3_USD), AFTER_TODAYS_FIXING.getFirst(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW);
  }

  @Test
  public void blockBundleDscFiniteDifferenceTest() {
    final int discountingCurveSize = DSC_USD_MARKET_QUOTES.length;
    final CurveBuildingBlockBundle fullInverseJacobian = BEFORE_TODAYS_FIXING.getSecond();
    final double bump = 1e-6;
    for (int i = 0; i < discountingCurveSize; i++) {
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults = BUILDER_FOR_TEST.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_DSC_USD, DSC_USD_GENERATORS[i], DSC_USD_ATTR[i], DSC_USD_MARKET_QUOTES[i] + bump)
          .buildCurves(NOW);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults = BUILDER_FOR_TEST.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_DSC_USD, DSC_USD_GENERATORS[i], DSC_USD_ATTR[i], DSC_USD_MARKET_QUOTES[i] - bump)
          .buildCurves(NOW);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_DSC_USD)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_DSC_USD)).getCurve().getYData();
      for (int j = 0; j < discountingCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        // note columns then rows tested
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_DSC_USD).getSecond().getData()[j][i];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_DSC_USD + ": column=" + i + " row=" + j,
            expectedSensitivity, dYielddQuote, bump);
      }
    }
  }

  @Test
  public void blockBundleFwd3MFiniteDifferenceTest() {
    final int discountingCurveSize = DSC_USD_MARKET_QUOTES.length;
    final int liborCurveSize = FWD3_USD_MARKET_QUOTES.length;
    final CurveBuildingBlockBundle fullInverseJacobian = BEFORE_TODAYS_FIXING.getSecond();
    final double bump = 1e-6;

    for (int i = 0; i < discountingCurveSize; i++) {
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults = BUILDER_FOR_TEST.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_DSC_USD, DSC_USD_GENERATORS[i], DSC_USD_ATTR[i], DSC_USD_MARKET_QUOTES[i] + bump)
          .buildCurves(NOW);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults = BUILDER_FOR_TEST.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_DSC_USD, DSC_USD_GENERATORS[i], DSC_USD_ATTR[i], DSC_USD_MARKET_QUOTES[i] - bump)
          .buildCurves(NOW);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_FWD3_USD)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_FWD3_USD)).getCurve().getYData();
      for (int j = 0; j < liborCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_FWD3_USD).getSecond().getData()[j][i];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_FWD3_USD + ": column=" + i + " row=" + j,
            expectedSensitivity, dYielddQuote, bump);
      }
    }
    for (int i = 0; i < liborCurveSize; i++) {
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults = BUILDER_FOR_TEST.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_FWD3_USD, FWD3_USD_GENERATORS[i], FWD3_USD_ATTR[i], FWD3_USD_MARKET_QUOTES[i] + bump)
          .buildCurves(NOW);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults = BUILDER_FOR_TEST.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_FWD3_USD, FWD3_USD_GENERATORS[i], FWD3_USD_ATTR[i], FWD3_USD_MARKET_QUOTES[i] - bump)
          .buildCurves(NOW);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_FWD3_USD)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_FWD3_USD)).getCurve().getYData();
      final int offset = i + discountingCurveSize;
      for (int j = 0; j < liborCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_FWD3_USD).getSecond().getData()[j][offset];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_FWD3_USD + ": column=" + offset + " row=" + j,
            expectedSensitivity, dYielddQuote, bump);
      }
    }
  }

  /**
   * Analyzes the shape of the forward curve.
   */
  @Test(enabled = false)
  public void forwardAnalysis() {
    final MulticurveProviderInterface marketDsc = BEFORE_TODAYS_FIXING.getFirst();
    final int jump = 1;
    final int startIndex = 0;
    final int nbDate = 2750;
    ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(NOW, USD_3M_LIBOR_INDEX.getSpotLag() + startIndex * jump, NYC);
    final double[] rateDsc = new double[nbDate];
    final double[] startTime = new double[nbDate];
    try (final FileWriter writer = new FileWriter("fwd-dsc.csv")) {
      for (int i = 0; i < nbDate; i++) {
        startTime[i] = TimeCalculator.getTimeBetween(NOW, startDate);
        final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, USD_3M_LIBOR_INDEX, NYC);
        final double endTime = TimeCalculator.getTimeBetween(NOW, endDate);
        final double accrualFactor = USD_3M_LIBOR_INDEX.getDayCount().getDayCountFraction(startDate, endDate, NYC);
        rateDsc[i] = marketDsc.getSimplyCompoundForwardRate(USD_3M_LIBOR_INDEX, startTime[i], endTime, accrualFactor);
        startDate = ScheduleCalculator.getAdjustedDate(startDate, jump, NYC);
        writer.append(0.0 + "," + startTime[i] + "," + rateDsc[i] + "\n");
      }
      writer.flush();
      writer.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

}
