/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve.discounting;

import static com.opengamma.analytics.financial.provider.curve.discounting.DiscountingMethodCurveUtils.curveConstructionTest;
import static org.testng.AssertJUnit.assertEquals;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.date.CalendarAdapter;
import com.opengamma.analytics.date.WeekendWorkingDayCalendar;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositON;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedCompoundedONCompoundedMaster;
import com.opengamma.analytics.financial.instrument.index.Index;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.discounting.DiscountingMethodCurveUtils.DiscountingMethodCurveBuilder;
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
 *  this example provide an example of curve construction using Brazilian swap.
 */
@Test(groups = TestGroup.UNIT)
public class BrlDiscountingOvernight1Test {
  private static final Interpolator1D INTERPOLATOR = NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME);
  private static final CalendarAdapter RIO = new CalendarAdapter(WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
  private static final FXMatrix FX_MATRIX = new FXMatrix(Currency.BRL);

  private static final double NOTIONAL = 1.0;

  private static final GeneratorSwapFixedCompoundedONCompounded GENERATOR_OIS_BRL = GeneratorSwapFixedCompoundedONCompoundedMaster.getInstance().getGenerator("BRLCDI", RIO);
  /** A CDI index */
  private static final IndexON CDI_INDEX = GENERATOR_OIS_BRL.getIndex();
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_BRL = new GeneratorDepositON("BRL Deposit ON", Currency.BRL, RIO, CDI_INDEX.getDayCount());

  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2011, 9, 28);
  private static final ZonedDateTime PREVIOUS_DATE = NOW.minusDays(1);
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_BRL_WITH_TODAY =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] { PREVIOUS_DATE, NOW }, new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_BRL_WITHOUT_TODAY =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] { PREVIOUS_DATE }, new double[] {0.07 });

  /** Fixing time series created before the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITHOUT_TODAY = new HashMap<>();
  /** Fixing time series created after the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITH_TODAY = new HashMap<>();
  static {
    FIXING_TS_WITHOUT_TODAY.put(CDI_INDEX, TS_ON_BRL_WITHOUT_TODAY);
    FIXING_TS_WITH_TODAY.put(CDI_INDEX, TS_ON_BRL_WITH_TODAY);
  }

  private static final String CURVE_NAME_DSC_BRL = "BRL Dsc";

  /** Market values for the dsc BRL curve */
  private static final double[] DSC_BRL_MARKET_QUOTES = new double[] {0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400 };
  /** Generators for the dsc BRL curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_BRL_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_DEPOSIT_ON_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL,
    GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL };
  /** Tenors for the dsc BRL curve */
  private static final Period[] DSC_BRL_TENOR = new Period[] {Period.ofDays(0), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3),
    Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1),
    Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
  private static final GeneratorAttributeIR[] DSC_BRL_ATTR = new GeneratorAttributeIR[DSC_BRL_TENOR.length];
  static {
    for (int i = 0; i < DSC_BRL_TENOR.length; i++) {
      DSC_BRL_ATTR[i] = new GeneratorAttributeIR(DSC_BRL_TENOR[i]);
    }
  }

  private static final MulticurveProviderDiscount KNOWN_DATA = new MulticurveProviderDiscount(FX_MATRIX);
  private static final DiscountingMethodCurveBuilder.ConfigBuilder BUILDER_FOR_TEST = DiscountingMethodCurveBuilder.setUp()
      .building(CURVE_NAME_DSC_BRL)
      .using(CURVE_NAME_DSC_BRL).forDiscounting(Currency.BRL).forOvernightIndex(CDI_INDEX).withInterpolator(INTERPOLATOR)
      .withKnownData(KNOWN_DATA);
  static {
    for (int i = 0; i < DSC_BRL_MARKET_QUOTES.length; i++) {
      BUILDER_FOR_TEST.withNode(CURVE_NAME_DSC_BRL, DSC_BRL_GENERATORS[i], DSC_BRL_ATTR[i], DSC_BRL_MARKET_QUOTES[i]);
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
    assertEquals(fullJacobianData.size(), 1);
    final DoubleMatrix2D discountingJacobianMatrix = fullJacobianData.get(CURVE_NAME_DSC_BRL).getSecond();
    assertEquals(discountingJacobianMatrix.getNumberOfRows(), DSC_BRL_MARKET_QUOTES.length);
    assertEquals(discountingJacobianMatrix.getNumberOfColumns(), DSC_BRL_MARKET_QUOTES.length);
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
    curveConstructionTest(definitionsForCurvesBeforeFixing.get(CURVE_NAME_DSC_BRL),
        BEFORE_TODAYS_FIXING.getFirst(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.BRL);
    curveConstructionTest(definitionsForCurvesAfterFixing.get(CURVE_NAME_DSC_BRL),
        AFTER_TODAYS_FIXING.getFirst(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.BRL);
  }

//  @Test
//  public void blockBundleDscFiniteDifferenceTest() {
//    final int discountingCurveSize = DSC_BRL_MARKET_QUOTES.length;
//    final CurveBuildingBlockBundle fullInverseJacobian = BEFORE_TODAYS_FIXING.getSecond();
//    final double bump = 1e-6;
//    for (int i = 0; i < discountingCurveSize; i++) {
//      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults = BUILDER_FOR_TEST.copy()
//          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
//          .getBuilder()
//          .replaceMarketQuote(CURVE_NAME_DSC_BRL, DSC_BRL_GENERATORS[i], DSC_BRL_ATTR[i], DSC_BRL_MARKET_QUOTES[i] + bump)
//          .buildCurves(NOW);
//      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults = BUILDER_FOR_TEST.copy()
//          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
//          .getBuilder()
//          .replaceMarketQuote(CURVE_NAME_DSC_BRL, DSC_BRL_GENERATORS[i], DSC_BRL_ATTR[i], DSC_BRL_MARKET_QUOTES[i] - bump)
//          .buildCurves(NOW);
//      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_DSC_BRL)).getCurve().getYData();
//      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_DSC_BRL)).getCurve().getYData();
//      for (int j = 0; j < discountingCurveSize; j++) {
//        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
//        // note columns then rows tested
//        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_DSC_BRL).getSecond().getData()[j][i];
//        assertEquals("Finite difference sensitivities for " + CURVE_NAME_DSC_BRL + ": column=" + i + " row=" + j,
//            expectedSensitivity, dYielddQuote, bump);
//      }
//    }
//  }

  @Test(enabled = false)
  public void performance() {
    long startTime, endTime;
    final int nbTest = 100;

    startTime = System.currentTimeMillis();
    final DiscountingMethodCurveBuilder builder = BUILDER_FOR_TEST.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder();
    for (int i = 0; i < nbTest; i++) {
      builder.buildCurves(NOW);
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
   try (final FileWriter writer = new FileWriter("fwd-dsc.csv")) {
     for (int i = 0; i < nbDate; i++) {
       startTime[i] = TimeCalculator.getTimeBetween(NOW, startDate);
       startTime2[i] = CDI_INDEX.getDayCount().getDayCountFraction(NOW, startDate, RIO);
       final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, CDI_INDEX.getPublicationLag(), RIO);
       final double endTime = TimeCalculator.getTimeBetween(NOW, endDate);
       final double endTime2 = CDI_INDEX.getDayCount().getDayCountFraction(NOW, endDate, RIO);
       accrualFactor[i] = CDI_INDEX.getDayCount().getDayCountFraction(startDate, endDate, RIO);
       accrualFactorActAct[i] = TimeCalculator.getTimeBetween(startDate, endDate);
       dscstart[i] = marketDsc.getDiscountFactor(Currency.BRL, startTime2[i]);
       dscend[i] = marketDsc.getDiscountFactor(Currency.BRL, endTime2);
       rateDsc[i] = marketDsc.getSimplyCompoundForwardRate(CDI_INDEX, startTime2[i], endTime2, accrualFactor[i]);
       rateDsc2[i] = marketDsc.getSimplyCompoundForwardRate(CDI_INDEX, startTime[i], endTime, accrualFactor[i]);
       rateDscNormal[i] = marketDsc.getSimplyCompoundForwardRate(CDI_INDEX, startTime[i], endTime, accrualFactorActAct[i]);
       startDate = ScheduleCalculator.getAdjustedDate(startDate, jump, RIO);
       writer.append(0.0 + "," + startTime[i] + "," + dscstart[i] + "," + dscend[i] + "," + rateDsc[i] + "," + rateDsc2[i] + "," + rateDscNormal[i] + "\n");
     }
     writer.flush();
     writer.close();
   } catch (final IOException e) {
     e.printStackTrace();
   }
 }
}
