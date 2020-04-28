/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve.discounting;

import static com.opengamma.analytics.financial.provider.curve.CurveBuildingTestUtils.curveConstructionTest;
import static org.testng.Assert.assertEquals;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder.DiscountingMethodCurveBuilder;
import com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder.DiscountingMethodCurveSetUp;
import com.mcleodmoores.analytics.financial.index.Index;
import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositON;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedCompoundedONCompoundedMaster;
import com.opengamma.analytics.financial.instrument.index.IndexON;
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
 * Builds and tests a discounting BRL curve containing an overnight rate and OIS.
 */
@Test(groups = TestGroup.UNIT)
public class BrlDiscountingOvernight1Test extends CurveBuildingTests {
  /** The interpolator used for the curve */
  private static final Interpolator1D INTERPOLATOR = NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME,
      FlatExtrapolator1dAdapter.NAME);
  /** A calendar containing only Saturday and Sunday holidays */
  private static final WorkingDayCalendar RIO = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;
  /** The base FX matrix */
  private static final FXMatrix FX_MATRIX = new FXMatrix(Currency.BRL);
  /** Generates OIS swaps */
  private static final GeneratorSwapFixedCompoundedONCompounded GENERATOR_OIS_BRL = GeneratorSwapFixedCompoundedONCompoundedMaster
      .getInstance()
      .getGenerator("BRLCDI", RIO);
  /** The CDI index */
  private static final IndexON CDI_INDEX = GENERATOR_OIS_BRL.getIndex();
  /** Generates the overnight deposit */
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_BRL = new GeneratorDepositON("BRL Deposit ON", Currency.BRL, RIO,
      CDI_INDEX.getDayCount());
  /** The curve construction date */
  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2011, 9, 28);
  /** The previous day */
  private static final ZonedDateTime PREVIOUS_DATE = NOW.minusDays(1);
  /** Fixing time series of overnight rates after today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_BRL_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries
      .ofUTC(new ZonedDateTime[] { PREVIOUS_DATE, NOW }, new double[] { 0.07, 0.08 });
  /** Fixing time series of overnight rates before today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_BRL_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries
      .ofUTC(new ZonedDateTime[] { PREVIOUS_DATE }, new double[] { 0.07 });
  /** Fixing time series created before the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITHOUT_TODAY = new HashMap<>();
  /** Fixing time series created after the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITH_TODAY = new HashMap<>();
  static {
    FIXING_TS_WITHOUT_TODAY.put(CDI_INDEX, TS_ON_BRL_WITHOUT_TODAY);
    FIXING_TS_WITH_TODAY.put(CDI_INDEX, TS_ON_BRL_WITH_TODAY);
  }
  /** The curve name */
  private static final String CURVE_NAME_DSC_BRL = "BRL Dsc";
  /** Market values for the discounting curve */
  private static final double[] DSC_BRL_MARKET_QUOTES = new double[] { 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400,
      0.0400, 0.0400, 0.0400,
      0.0400 };
  /** Vanilla instrument generators */
  private static final GeneratorInstrument[] DSC_BRL_GENERATORS = new GeneratorInstrument[] { GENERATOR_DEPOSIT_ON_BRL, GENERATOR_OIS_BRL,
      GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL,
      GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL };
  /** Attribute generators */
  private static final GeneratorAttributeIR[] DSC_BRL_ATTR;
  static {
    final Period[] tenors = new Period[] { Period.ofDays(0), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6),
        Period.ofMonths(9),
        Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
    DSC_BRL_ATTR = new GeneratorAttributeIR[tenors.length];
    for (int i = 0; i < tenors.length; i++) {
      DSC_BRL_ATTR[i] = new GeneratorAttributeIR(tenors[i]);
    }
  }
  /** The curve builder */
  private static final DiscountingMethodCurveSetUp BUILDER_FOR_TEST = DiscountingMethodCurveBuilder.setUp()
      .building(CURVE_NAME_DSC_BRL)
      .using(CURVE_NAME_DSC_BRL).forDiscounting(Currency.BRL).forIndex(CDI_INDEX.toOvernightIndex()).withInterpolator(INTERPOLATOR);
  // initialize the curve builder with market data
  static {
    for (int i = 0; i < DSC_BRL_MARKET_QUOTES.length; i++) {
      BUILDER_FOR_TEST.addNode(CURVE_NAME_DSC_BRL,
          DSC_BRL_GENERATORS[i].generateInstrument(NOW, DSC_BRL_MARKET_QUOTES[i], 1, DSC_BRL_ATTR[i]));
    }
  }
  /** Curves constructed before today's fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> BEFORE_TODAYS_FIXING;
  /** Curves constructed after today's fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> AFTER_TODAYS_FIXING;
  static {
    BEFORE_TODAYS_FIXING = BUILDER_FOR_TEST.copy().getBuilder().buildCurves(NOW, FIXING_TS_WITHOUT_TODAY);
    AFTER_TODAYS_FIXING = BUILDER_FOR_TEST.copy().getBuilder().buildCurves(NOW, FIXING_TS_WITH_TODAY);
  }

  @Override
  @Test
  public void testJacobianSize() {
    final CurveBuildingBlockBundle fullJacobian = BEFORE_TODAYS_FIXING.getSecond();
    final Map<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> fullJacobianData = fullJacobian.getData();
    assertEquals(fullJacobianData.size(), 1);
    final DoubleMatrix2D discountingJacobianMatrix = fullJacobianData.get(CURVE_NAME_DSC_BRL).getSecond();
    assertEquals(discountingJacobianMatrix.getNumberOfRows(), DSC_BRL_MARKET_QUOTES.length);
    assertEquals(discountingJacobianMatrix.getNumberOfColumns(), DSC_BRL_MARKET_QUOTES.length);
  }

  @Override
  @Test
  public void testInstrumentsInCurvePriceToZero() {
    final Map<String, List<InstrumentDefinition<?>>> definitionsForCurvesBeforeFixing = BUILDER_FOR_TEST.copy()
        .getBuilder()
        .getNodes();
    final Map<String, List<InstrumentDefinition<?>>> definitionsForCurvesAfterFixing = BUILDER_FOR_TEST.copy()
        .getBuilder()
        .getNodes();
    curveConstructionTest(definitionsForCurvesBeforeFixing.get(CURVE_NAME_DSC_BRL),
        BEFORE_TODAYS_FIXING.getFirst(), PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW,
        Currency.BRL);
    curveConstructionTest(definitionsForCurvesAfterFixing.get(CURVE_NAME_DSC_BRL),
        AFTER_TODAYS_FIXING.getFirst(), PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW,
        Currency.BRL);
  }

  /**
   * Tests the sensitivities of the discounting curve to changes in the market data points used in the discounting curve.
   */
  @Override
  @Test
  public void testFiniteDifferenceSensitivities() {
    // TODO
    // assertFiniteDifferenceSensitivities(BEFORE_TODAYS_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_FOR_TEST, CURVE_NAME_DSC_BRL,
    // CURVE_NAME_DSC_BRL, NOW, DSC_BRL_GENERATORS, DSC_BRL_ATTR, DSC_BRL_MARKET_QUOTES, false);
    // assertFiniteDifferenceSensitivities(AFTER_TODAYS_FIXING.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_FOR_TEST, CURVE_NAME_DSC_BRL,
    // CURVE_NAME_DSC_BRL, NOW, DSC_BRL_GENERATORS, DSC_BRL_ATTR, DSC_BRL_MARKET_QUOTES, false);
  }

  /**
   * Only one set of curves is constructed, so no tests are possible.
   */
  @Override
  @Test
  public void testSameCurvesDifferentMethods() {
    return;
  }

  /**
   * Performance test.
   */
  @Test(enabled = false)
  public void performance() {
    long startTime, endTime;
    final int nbTest = 100;

    startTime = System.currentTimeMillis();
    final DiscountingMethodCurveBuilder builder = BUILDER_FOR_TEST.copy().getBuilder();
    for (int i = 0; i < nbTest; i++) {
      builder.buildCurves(NOW, FIXING_TS_WITHOUT_TODAY);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction Brazilian CDI EUR 1 units: " + (endTime - startTime) + " ms");
    // Performance note: curve construction: On Dell Precision T1850 3.5 GHz Quad-Core Intel Xeon: 3094 ms for 100 sets.
  }

  /**
   * Analyzes the shape of the forward curve.
   */
  @Test(enabled = false)
  public void forwardAnalysis() {
    final MulticurveProviderInterface marketDsc = BEFORE_TODAYS_FIXING.getFirst();
    final int jump = 1;
    final int startIndex = 0;
    final int nbDate = 1000;
    ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(NOW, CDI_INDEX.getPublicationLag() + startIndex * jump, RIO);
    final double[] dscstart = new double[nbDate];
    final double[] dscend = new double[nbDate];
    final double[] rateDsc = new double[nbDate];
    final double[] rateDsc2 = new double[nbDate];
    final double[] rateDscNormal = new double[nbDate];
    final double[] startTime = new double[nbDate];
    final double[] startTime2 = new double[nbDate];
    final double[] accrualFactor = new double[nbDate];
    final double[] accrualFactorActAct = new double[nbDate];
    try (FileWriter writer = new FileWriter("fwd-dsc.csv")) {
      for (int i = 0; i < nbDate; i++) {
        startTime[i] = TimeCalculator.getTimeBetween(NOW, startDate);
        startTime2[i] = CDI_INDEX.getDayCount().getDayCountFraction(NOW, startDate, CalendarAdapter.of(RIO));
        final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, CDI_INDEX.getPublicationLag(), RIO);
        final double endTime = TimeCalculator.getTimeBetween(NOW, endDate);
        final double endTime2 = CDI_INDEX.getDayCount().getDayCountFraction(NOW, endDate, CalendarAdapter.of(RIO));
        accrualFactor[i] = CDI_INDEX.getDayCount().getDayCountFraction(startDate, endDate, CalendarAdapter.of(RIO));
        accrualFactorActAct[i] = TimeCalculator.getTimeBetween(startDate, endDate);
        dscstart[i] = marketDsc.getDiscountFactor(Currency.BRL, startTime2[i]);
        dscend[i] = marketDsc.getDiscountFactor(Currency.BRL, endTime2);
        rateDsc[i] = marketDsc.getSimplyCompoundForwardRate(CDI_INDEX, startTime2[i], endTime2, accrualFactor[i]);
        rateDsc2[i] = marketDsc.getSimplyCompoundForwardRate(CDI_INDEX, startTime[i], endTime, accrualFactor[i]);
        rateDscNormal[i] = marketDsc.getSimplyCompoundForwardRate(CDI_INDEX, startTime[i], endTime, accrualFactorActAct[i]);
        startDate = ScheduleCalculator.getAdjustedDate(startDate, jump, RIO);
        writer.append(0.0 + "," + startTime[i] + "," + dscstart[i] + "," + dscend[i] + "," + rateDsc[i] + "," + rateDsc2[i] + ","
            + rateDscNormal[i] + "\n");
      }
      writer.flush();
      writer.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }
}
