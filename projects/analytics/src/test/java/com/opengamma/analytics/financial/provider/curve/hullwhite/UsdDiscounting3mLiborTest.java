/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve.hullwhite;

import static com.opengamma.analytics.financial.provider.curve.CurveBuildingTestUtils.assertFiniteDifferenceSensitivities;
import static com.opengamma.analytics.financial.provider.curve.CurveBuildingTestUtils.assertMatrixEquals;
import static com.opengamma.analytics.financial.provider.curve.CurveBuildingTestUtils.assertNoSensitivities;
import static com.opengamma.analytics.financial.provider.curve.CurveBuildingTestUtils.assertYieldCurvesEqual;
import static com.opengamma.analytics.financial.provider.curve.CurveBuildingTestUtils.curveConstructionTest;
import static org.testng.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.date.CalendarAdapter;
import com.opengamma.analytics.date.WeekendWorkingDayCalendar;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.SwapFuturesPriceDeliverableSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositON;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorInterestRateFutures;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFuturesDeliverable;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.Index;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.PresentValueHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingTests;
import com.opengamma.analytics.financial.provider.curve.builder.HullWhiteMethodCurveBuilder;
import com.opengamma.analytics.financial.provider.curve.builder.HullWhiteMethodCurveSetUp;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.factory.FlatExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LinearInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.NamedInterpolator1dFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Builds and tests discounting and 3m USD LIBOR curves using a one-factor Hull-White model. The discounting curve is built
 * first and then used when constructing the LIBOR curve. This means that the LIBOR curve has sensitivities to both discounting and LIBOR
 * market data, but the discounting curve only has sensitivities to the discounting curve.
 * <p>
 * The discounting curve contains the overnight deposit rate and OIS swaps. The LIBOR curve contains the 3m LIBOR rate,
 * LIBOR IMM futures and deliverable swap futures.
 */
@Test(groups = TestGroup.UNIT)
public class UsdDiscounting3mLiborTest extends CurveBuildingTests {
  /** The interpolator used for both curves */
  private static final Interpolator1D INTERPOLATOR = NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME);
  /** A calendar containing only Saturday and Sunday holidays */
  private static final CalendarAdapter NYC = new CalendarAdapter(WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
  /** The base FX matrix */
  private static final FXMatrix FX_MATRIX = new FXMatrix(Currency.USD);
  /** The notional of the futures */
  private static final double FUTURE_NOTIONAL = 1000000.0;
  /** Generates OIS swaps for the discounting curve */
  private static final GeneratorSwapFixedON GENERATOR_OIS_USD = GeneratorSwapFixedONMaster.getInstance().getGenerator("USD1YFEDFUND", NYC);
  /** An overnight USD index */
  private static final IndexON FED_FUNDS_INDEX = GENERATOR_OIS_USD.getIndex();
  /** Generates the overnight deposit */
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_USD =
      new GeneratorDepositON("Overnight USD Deposit", Currency.USD, NYC, FED_FUNDS_INDEX.getDayCount());
  /** Generates the 3m LIBOR / 6m fixed swaps */
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M =  GeneratorSwapFixedIborMaster.getInstance().getGenerator("USD6MLIBOR3M", NYC);
  /** A 3m LIBOR index */
  private static final IborIndex USD_3M_LIBOR_INDEX = USD6MLIBOR3M.getIborIndex();
  /** Generates the 3m LIBOR instrument */
  private static final GeneratorDepositIbor GENERATOR_USDLIBOR3M = new GeneratorDepositIbor("GENERATOR_USDLIBOR3M", USD_3M_LIBOR_INDEX, NYC);
  /** *M3 future start date */
  private static final ZonedDateTime M3_START = DateUtils.getUTCDate(2013, 6, 19);
  /** *U3 future start date */
  private static final ZonedDateTime U3_START = DateUtils.getUTCDate(2013, 9, 18);
  /** *Z3 future start date */
  private static final ZonedDateTime Z3_START = DateUtils.getUTCDate(2013, 12, 18);
  /** *H4 future start date */
  private static final ZonedDateTime H4_START = DateUtils.getUTCDate(2014, 3, 19);
  /** EDM3 */
  private static final InterestRateFutureSecurityDefinition EDM3_DEFINITION = InterestRateFutureSecurityDefinition
      .fromFixingPeriodStartDate(M3_START, USD_3M_LIBOR_INDEX, FUTURE_NOTIONAL, 0.25, "EDM3", NYC);
  /** EDU3 */
  private static final InterestRateFutureSecurityDefinition EDU3_DEFINITION = InterestRateFutureSecurityDefinition
      .fromFixingPeriodStartDate(U3_START, USD_3M_LIBOR_INDEX, FUTURE_NOTIONAL, 0.25, "EDU3", NYC);
  /** EDZ3 */
  private static final InterestRateFutureSecurityDefinition EDZ3_DEFINITION = InterestRateFutureSecurityDefinition
      .fromFixingPeriodStartDate(Z3_START, USD_3M_LIBOR_INDEX, FUTURE_NOTIONAL, 0.25, "EDZ3", NYC);
  /** EDH4 */
  private static final InterestRateFutureSecurityDefinition EDH4_DEFINITION = InterestRateFutureSecurityDefinition
      .fromFixingPeriodStartDate(H4_START, USD_3M_LIBOR_INDEX, FUTURE_NOTIONAL, 0.25, "EDH4", NYC);
  /** CTPM3 */
  private static final SwapFuturesPriceDeliverableSecurityDefinition CTPM3_DEFINITION =
      SwapFuturesPriceDeliverableSecurityDefinition.from(M3_START, USD6MLIBOR3M, Period.ofYears(2), FUTURE_NOTIONAL, 0.0050);
  /** CFPM3 */
  private static final SwapFuturesPriceDeliverableSecurityDefinition CFPM3_DEFINITION =
      SwapFuturesPriceDeliverableSecurityDefinition.from(M3_START, USD6MLIBOR3M, Period.ofYears(5), FUTURE_NOTIONAL, 0.0100);
  /** CNPM3 */
  private static final SwapFuturesPriceDeliverableSecurityDefinition CNPM3_DEFINITION =
      SwapFuturesPriceDeliverableSecurityDefinition.from(M3_START, USD6MLIBOR3M, Period.ofYears(10), FUTURE_NOTIONAL, 0.0200);
  /** CBPM3 */
  private static final SwapFuturesPriceDeliverableSecurityDefinition CBPM3_DEFINITION =
      SwapFuturesPriceDeliverableSecurityDefinition.from(M3_START, USD6MLIBOR3M, Period.ofYears(30), FUTURE_NOTIONAL, 0.0275);
  /** Generates EDM3 */
  private static final GeneratorInterestRateFutures GENERATOR_EDM3 = new GeneratorInterestRateFutures("EDM3", EDM3_DEFINITION);
  /** Generates EDU3 */
  private static final GeneratorInterestRateFutures GENERATOR_EDU3 = new GeneratorInterestRateFutures("EDU3", EDU3_DEFINITION);
  /** Generates EDZ3 */
  private static final GeneratorInterestRateFutures GENERATOR_EDZ3 = new GeneratorInterestRateFutures("EDZ3", EDZ3_DEFINITION);
  /** Generates EDH4 */
  private static final GeneratorInterestRateFutures GENERATOR_EDH4 = new GeneratorInterestRateFutures("EDH4", EDH4_DEFINITION);
  /** Generates CTPM3 */
  private static final GeneratorSwapFuturesDeliverable GENERATOR_CTPM3 = new GeneratorSwapFuturesDeliverable("CTPM3", CTPM3_DEFINITION);
  /** Generates CFPM3 */
  private static final GeneratorSwapFuturesDeliverable GENERATOR_CFPM3 = new GeneratorSwapFuturesDeliverable("CFPM3", CFPM3_DEFINITION);
  /** Generates CNPM3 */
  private static final GeneratorSwapFuturesDeliverable GENERATOR_CNPM3 = new GeneratorSwapFuturesDeliverable("CNPM3", CNPM3_DEFINITION);
  /** Generates CBPM3 */
  private static final GeneratorSwapFuturesDeliverable GENERATOR_CBPM3 = new GeneratorSwapFuturesDeliverable("CBPM3", CBPM3_DEFINITION);
  /** The curve construction date */
  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2013, 4, 26);
  /** The previous day */
  private static final ZonedDateTime PREVIOUS = NOW.minusDays(1);
  /** Overnight rates after today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS, NOW}, new double[] {0.0007, 0.0008});
  /** Overnight rates before today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {NOW}, new double[] {0.0007 });
  /** Fixing time series created before the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITHOUT_TODAY = new HashMap<>();
  /** Fixing time series created after the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITH_TODAY = new HashMap<>();
  static {
    FIXING_TS_WITHOUT_TODAY.put(FED_FUNDS_INDEX, TS_ON_USD_WITHOUT_TODAY);
    FIXING_TS_WITH_TODAY.put(FED_FUNDS_INDEX, TS_ON_USD_WITH_TODAY);
  }
  /** Mean reversion parameter */
  private static final double MEAN_REVERSION = 0.01;
  /** Volatility levels */
  private static final double[] VOLATILITY = new double[] {0.01, 0.011, 0.012, 0.013, 0.014 };
  /** Volatility times */
  private static final double[] VOLATILITY_TIME = new double[] {0.5, 1.0, 2.0, 5.0 };
  /** The parameters for the Hull-White model */
  private static final HullWhiteOneFactorPiecewiseConstantParameters MODEL_PARAMETERS =
      new HullWhiteOneFactorPiecewiseConstantParameters(MEAN_REVERSION, VOLATILITY, VOLATILITY_TIME);
  /** Already known market data - contains only an empty FX matrix */
  private static final MulticurveProviderDiscount KNOWN_DATA = new MulticurveProviderDiscount(FX_MATRIX);
  /** Already known data plus Hull-White parameters */
  private static final HullWhiteOneFactorProviderDiscount HW_KNOWN_DATA =
      new HullWhiteOneFactorProviderDiscount(KNOWN_DATA, MODEL_PARAMETERS, Currency.USD);
  /** Discounting curve name */
  private static final String CURVE_NAME_DSC_USD = "USD OIS";
  /** 3M LIBOR curve name */
  private static final String CURVE_NAME_FWD3_USD = "USD Fwd 3M";
  /** Builds the curves one after the other */
  private static final HullWhiteMethodCurveSetUp CONSECUTIVE_BUILDER = HullWhiteMethodCurveBuilder.setUp()
      .buildingFirst(CURVE_NAME_DSC_USD)
      .using(CURVE_NAME_DSC_USD).forDiscounting(Currency.USD).forOvernightIndex(FED_FUNDS_INDEX).withInterpolator(INTERPOLATOR)
      .thenBuilding(CURVE_NAME_FWD3_USD)
      .using(CURVE_NAME_FWD3_USD).forIborIndex(USD_3M_LIBOR_INDEX).withInterpolator(INTERPOLATOR)
      .withKnownData(HW_KNOWN_DATA);
  /** Builds the curves simultaneously */
  private static final HullWhiteMethodCurveSetUp SIMULTANEOUS_BUILDER = HullWhiteMethodCurveBuilder.setUp()
      .building(CURVE_NAME_DSC_USD, CURVE_NAME_FWD3_USD)
      .using(CURVE_NAME_DSC_USD).forDiscounting(Currency.USD).forOvernightIndex(FED_FUNDS_INDEX).withInterpolator(INTERPOLATOR)
      .using(CURVE_NAME_FWD3_USD).forIborIndex(USD_3M_LIBOR_INDEX).withInterpolator(INTERPOLATOR)
      .withKnownData(HW_KNOWN_DATA);
  /** Market values for the discounting curve */
  private static final double[] DSC_USD_MARKET_QUOTES =
      new double[] {0.0022, 0.00127, 0.00125, 0.00126, 0.00126, 0.00125, 0.001315, 0.001615, 0.00243, 0.00393, 0.00594, 0.01586 };
  /** Vanilla instrument generators for the discounting curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_USD_GENERATORS =
      new GeneratorInstrument<?>[] {GENERATOR_DEPOSIT_ON_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD,
    GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD };
    /** Attribute generators for the discounting curve */
    private static final GeneratorAttributeIR[] DSC_USD_ATTR;
    static {
      final Period[] discountingTenors = new Period[] {Period.ofDays(0), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3),
          Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
          Period.ofYears(10) };
      DSC_USD_ATTR = new GeneratorAttributeIR[discountingTenors.length];
      for (int i = 0; i < discountingTenors.length; i++) {
        DSC_USD_ATTR[i] = new GeneratorAttributeIR(discountingTenors[i]);
        CONSECUTIVE_BUILDER.withNode(CURVE_NAME_DSC_USD, DSC_USD_GENERATORS[i], DSC_USD_ATTR[i], DSC_USD_MARKET_QUOTES[i]);
        SIMULTANEOUS_BUILDER.withNode(CURVE_NAME_DSC_USD, DSC_USD_GENERATORS[i], DSC_USD_ATTR[i], DSC_USD_MARKET_QUOTES[i]);
      }
    }
    /** Market values for the 3m LIBOR curve */
  private static final double[] FWD3_USD_MARKET_QUOTES =
      new double[] {0.0027560, 0.99715, 0.99700, 0.99680, 0.99660, (100 + 7.0 / 32.0 + 3.0 / (32.0 * 4.0)) / 100.0, (100 + 17.0 / 32.0) / 100.0,
    (101 + 2.0 / 32.0) / 100.0, (98 + 21.0 / 32.0) / 100.0 };
  // Quoted in 32nd (by 1/4): 100-07 3/4, 100-17 +, 101-02, 98-21 };
  /** Vanilla instrument generators for the 3m LIBOR curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD3_USD_GENERATORS =
      new GeneratorInstrument<?>[] {GENERATOR_USDLIBOR3M, GENERATOR_EDM3, GENERATOR_EDU3, GENERATOR_EDZ3, GENERATOR_EDH4, GENERATOR_CTPM3,
    GENERATOR_CFPM3, GENERATOR_CNPM3, GENERATOR_CBPM3 };
  /** Attributes for the 3m LIBOR curve */
  private static final GeneratorAttribute[] FWD3_USD_ATTR;
  static {
    final Period[] startTenors = new Period[] {Period.ofMonths(0), Period.ofMonths(0), Period.ofMonths(0), Period.ofMonths(0), Period.ofMonths(0),
        Period.ofMonths(0), Period.ofMonths(0), Period.ofMonths(0), Period.ofMonths(0)};
    FWD3_USD_ATTR = new GeneratorAttribute[startTenors.length];
    FWD3_USD_ATTR[0] = new GeneratorAttributeIR(startTenors[0], startTenors[0]);
    CONSECUTIVE_BUILDER.withNode(CURVE_NAME_FWD3_USD, FWD3_USD_GENERATORS[0], FWD3_USD_ATTR[0], FWD3_USD_MARKET_QUOTES[0]);
    SIMULTANEOUS_BUILDER.withNode(CURVE_NAME_FWD3_USD, FWD3_USD_GENERATORS[0], FWD3_USD_ATTR[0], FWD3_USD_MARKET_QUOTES[0]);
    for (int i = 1; i < startTenors.length; i++) {
      FWD3_USD_ATTR[i] = new GeneratorAttribute();
      CONSECUTIVE_BUILDER.withNode(CURVE_NAME_FWD3_USD, FWD3_USD_GENERATORS[i], FWD3_USD_ATTR[i], FWD3_USD_MARKET_QUOTES[i]);
      SIMULTANEOUS_BUILDER.withNode(CURVE_NAME_FWD3_USD, FWD3_USD_GENERATORS[i], FWD3_USD_ATTR[i], FWD3_USD_MARKET_QUOTES[i]);
    }
  }
  /** Simultaneous curves constructed before today's fixing */
  private static final Pair<HullWhiteOneFactorProviderDiscount, CurveBuildingBlockBundle> SIMULTANEOUS_BEFORE_FIXING;
  /** Consecutive curves constructed before today's fixing */
  private static final Pair<HullWhiteOneFactorProviderDiscount, CurveBuildingBlockBundle> CONSECUTIVE_BEFORE_FIXING;
  /** Simultaneous curves constructed after today's fixing */
  private static final Pair<HullWhiteOneFactorProviderDiscount, CurveBuildingBlockBundle> SIMULTANEOUS_AFTER_FIXING;
  /** Consecutive curves constructed after today's fixing */
  private static final Pair<HullWhiteOneFactorProviderDiscount, CurveBuildingBlockBundle> CONSECUTIVE_AFTER_FIXING;
  /** Calculation tolerance */
  private static final double EPS = 1.0e-7;

  static {
    SIMULTANEOUS_BEFORE_FIXING = SIMULTANEOUS_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().buildCurves(NOW);
    SIMULTANEOUS_AFTER_FIXING = SIMULTANEOUS_BUILDER.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().buildCurves(NOW);
    CONSECUTIVE_BEFORE_FIXING = CONSECUTIVE_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().buildCurves(NOW);
    CONSECUTIVE_AFTER_FIXING = CONSECUTIVE_BUILDER.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().buildCurves(NOW);
  }

  @Override
  @Test
  public void testJacobianSize() {
    // discounting then LIBOR
    CurveBuildingBlockBundle fullJacobian = CONSECUTIVE_BEFORE_FIXING.getSecond();
    Map<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> fullJacobianData = fullJacobian.getData();
    assertEquals(fullJacobianData.size(), 2);
    DoubleMatrix2D discountingJacobianMatrix = fullJacobianData.get(CURVE_NAME_DSC_USD).getSecond();
    assertEquals(discountingJacobianMatrix.getNumberOfRows(), DSC_USD_MARKET_QUOTES.length);
    assertEquals(discountingJacobianMatrix.getNumberOfColumns(), DSC_USD_MARKET_QUOTES.length);
    DoubleMatrix2D liborJacobianMatrix = fullJacobianData.get(CURVE_NAME_FWD3_USD).getSecond();
    assertEquals(liborJacobianMatrix.getNumberOfRows(), FWD3_USD_MARKET_QUOTES.length);
    assertEquals(liborJacobianMatrix.getNumberOfColumns(), DSC_USD_MARKET_QUOTES.length + FWD3_USD_MARKET_QUOTES.length);
    // discounting and LIBOR
    fullJacobian = SIMULTANEOUS_BEFORE_FIXING.getSecond();
    fullJacobianData = fullJacobian.getData();
    assertEquals(fullJacobianData.size(), 2);
    discountingJacobianMatrix = fullJacobianData.get(CURVE_NAME_DSC_USD).getSecond();
    assertEquals(discountingJacobianMatrix.getNumberOfRows(), DSC_USD_MARKET_QUOTES.length);
    assertEquals(discountingJacobianMatrix.getNumberOfColumns(), DSC_USD_MARKET_QUOTES.length + FWD3_USD_MARKET_QUOTES.length);
    liborJacobianMatrix = fullJacobianData.get(CURVE_NAME_FWD3_USD).getSecond();
    assertEquals(liborJacobianMatrix.getNumberOfRows(), FWD3_USD_MARKET_QUOTES.length);
    assertEquals(liborJacobianMatrix.getNumberOfColumns(), DSC_USD_MARKET_QUOTES.length + FWD3_USD_MARKET_QUOTES.length);
  }

  @Override
  @Test
  public void testInstrumentsInCurvePriceToZero() {
    // discounting then LIBOR
    Map<String, InstrumentDefinition<?>[]> definitions;
    // before fixing
    definitions = CONSECUTIVE_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().getDefinitionsForCurves(NOW);
    curveConstructionTest(definitions.get(CURVE_NAME_DSC_USD), CONSECUTIVE_BEFORE_FIXING.getFirst(),
        PresentValueHullWhiteCalculator.getInstance(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.USD, EPS);
    curveConstructionTest(definitions.get(CURVE_NAME_FWD3_USD), CONSECUTIVE_BEFORE_FIXING.getFirst(),
        PresentValueHullWhiteCalculator.getInstance(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.USD, EPS);
    // after fixing
    definitions = CONSECUTIVE_BUILDER.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().getDefinitionsForCurves(NOW);
    curveConstructionTest(definitions.get(CURVE_NAME_DSC_USD), CONSECUTIVE_AFTER_FIXING.getFirst(),
        PresentValueHullWhiteCalculator.getInstance(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.USD, EPS);
    curveConstructionTest(definitions.get(CURVE_NAME_FWD3_USD), CONSECUTIVE_AFTER_FIXING.getFirst(),
        PresentValueHullWhiteCalculator.getInstance(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.USD, EPS);
    // discounting and LIBOR
    // before fixing
    definitions = SIMULTANEOUS_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().getDefinitionsForCurves(NOW);
    curveConstructionTest(definitions.get(CURVE_NAME_DSC_USD), SIMULTANEOUS_BEFORE_FIXING.getFirst(),
        PresentValueHullWhiteCalculator.getInstance(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX,
        NOW, Currency.USD, EPS);
    curveConstructionTest(definitions.get(CURVE_NAME_FWD3_USD), SIMULTANEOUS_BEFORE_FIXING.getFirst(),
        PresentValueHullWhiteCalculator.getInstance(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX,
        NOW, Currency.USD, EPS);
    // after fixing
    definitions = SIMULTANEOUS_BUILDER.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().getDefinitionsForCurves(NOW);
    curveConstructionTest(definitions.get(CURVE_NAME_DSC_USD), SIMULTANEOUS_AFTER_FIXING.getFirst(),
        PresentValueHullWhiteCalculator.getInstance(), FIXING_TS_WITH_TODAY, FX_MATRIX,
        NOW, Currency.USD, EPS);
    curveConstructionTest(definitions.get(CURVE_NAME_FWD3_USD), SIMULTANEOUS_AFTER_FIXING.getFirst(),
        PresentValueHullWhiteCalculator.getInstance(), FIXING_TS_WITH_TODAY, FX_MATRIX,
        NOW, Currency.USD, EPS);
  }

  @Override
  @Test
  public void testFiniteDifferenceSensitivities() {
    testDiscountingCurveSensitivities1(CONSECUTIVE_BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY);
    testDiscountingCurveSensitivities1(CONSECUTIVE_AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY);
    testDiscountingCurveSensitivities2(SIMULTANEOUS_BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY);
    testDiscountingCurveSensitivities2(SIMULTANEOUS_AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY);
    testLiborCurveSensitivities(CONSECUTIVE_BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, CONSECUTIVE_BUILDER);
    testLiborCurveSensitivities(CONSECUTIVE_BEFORE_FIXING.getSecond(), FIXING_TS_WITH_TODAY, CONSECUTIVE_BUILDER);
    testLiborCurveSensitivities(SIMULTANEOUS_BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, SIMULTANEOUS_BUILDER);
    testLiborCurveSensitivities(SIMULTANEOUS_AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY, SIMULTANEOUS_BUILDER);
  }

  /**
   * Tests the sensitivities of the discounting curve to changes in the market data points used in the
   * curves when the discounting curve has no sensitivity to the LIBOR curve.
   * @param fullInverseJacobian  analytic sensitivities
   * @param fixingTs  the fixing time series
   */
  private static void testDiscountingCurveSensitivities1(final CurveBuildingBlockBundle fullInverseJacobian,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs) {
    // sensitivities to discounting
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, CONSECUTIVE_BUILDER, CURVE_NAME_DSC_USD,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    // sensitivities to 3m LIBOR should not have been calculated
    assertNoSensitivities(fullInverseJacobian, CURVE_NAME_DSC_USD, CURVE_NAME_FWD3_USD);
  }

  /**
   * Tests the sensitivities of the discounting curve to changes in the market data points used in the
   * curves when the discounting curve is constructed before the LIBOR curve. Sensitivities to
   * LIBOR market data are calculated, but they should be equal to zero.
   * @param fullInverseJacobian  analytic sensitivities
   * @param fixingTs  the fixing time series
   */
  private static void testDiscountingCurveSensitivities2(final CurveBuildingBlockBundle fullInverseJacobian,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs) {
    // sensitivities to discounting
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, SIMULTANEOUS_BUILDER, CURVE_NAME_DSC_USD,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    // sensitivities to 3m LIBOR should be zero
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, SIMULTANEOUS_BUILDER, CURVE_NAME_DSC_USD,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, true);
  }

  /**
   * Tests the sensitivities of the LIBOR curve to changes in the discounting and LIBOR curve market data.
   * @param fullInverseJacobian  analytic sensitivities
   * @param fixingTs  the fixing time series
   * @param builder  the curve builder
   */
  private static void testLiborCurveSensitivities(final CurveBuildingBlockBundle fullInverseJacobian,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs, final HullWhiteMethodCurveSetUp builder) {
    // sensitivities to discounting
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, builder, CURVE_NAME_FWD3_USD,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    // sensitivities to 3m LIBOR
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, builder, CURVE_NAME_FWD3_USD,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
  }

  @Override
  @Test
  public void testSameCurvesDifferentMethods() {
    // discounting curves
    YieldAndDiscountCurve curveBefore1 = CONSECUTIVE_BEFORE_FIXING.getFirst().getCurve(Currency.USD);
    YieldAndDiscountCurve curveBefore2 = SIMULTANEOUS_BEFORE_FIXING.getFirst().getCurve(Currency.USD);
    assertYieldCurvesEqual(curveBefore1, curveBefore2, EPS);
    YieldAndDiscountCurve curveAfter1 = CONSECUTIVE_AFTER_FIXING.getFirst().getCurve(Currency.USD);
    YieldAndDiscountCurve curveAfter2 = SIMULTANEOUS_AFTER_FIXING.getFirst().getCurve(Currency.USD);
    assertYieldCurvesEqual(curveAfter1, curveAfter2, EPS);
    // LIBOR curves
    curveBefore1 = CONSECUTIVE_BEFORE_FIXING.getFirst().getCurve(USD_3M_LIBOR_INDEX);
    curveBefore2 = SIMULTANEOUS_BEFORE_FIXING.getFirst().getCurve(USD_3M_LIBOR_INDEX);
    assertYieldCurvesEqual(curveBefore1, curveBefore2, EPS);
    curveAfter1 = CONSECUTIVE_AFTER_FIXING.getFirst().getCurve(USD_3M_LIBOR_INDEX);
    curveAfter2 = SIMULTANEOUS_AFTER_FIXING.getFirst().getCurve(USD_3M_LIBOR_INDEX);
    assertYieldCurvesEqual(curveAfter1, curveAfter2, EPS);
    // discounting sensitivities are not the same, but the LIBOR matrices should be the same for both construction methods
    final DoubleMatrix2D matrixBefore1 = CONSECUTIVE_BEFORE_FIXING.getSecond().getBlock(CURVE_NAME_FWD3_USD).getSecond();
    final DoubleMatrix2D matrixBefore2 = SIMULTANEOUS_BEFORE_FIXING.getSecond().getBlock(CURVE_NAME_FWD3_USD).getSecond();
    assertMatrixEquals(matrixBefore1, matrixBefore2, EPS);
    final DoubleMatrix2D matrixAfter1 = CONSECUTIVE_AFTER_FIXING.getSecond().getBlock(CURVE_NAME_FWD3_USD).getSecond();
    final DoubleMatrix2D matrixAfter2 = SIMULTANEOUS_AFTER_FIXING.getSecond().getBlock(CURVE_NAME_FWD3_USD).getSecond();
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
    HullWhiteMethodCurveBuilder builder = CONSECUTIVE_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder();
    for (int i = 0; i < nbTest; i++) {
      builder.buildCurves(NOW);
    }
    endTime = System.currentTimeMillis();
    System.out.println("MulticurveBuildingHullWhiteDiscountFuturesUSDTest:" + nbTest + " curve construction / 2 units: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 2 units: 06-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 810 ms for 100 sets.

    startTime = System.currentTimeMillis();
    builder = SIMULTANEOUS_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder();
    for (int i = 0; i < nbTest; i++) {
      builder.buildCurves(NOW);
    }
    endTime = System.currentTimeMillis();
    System.out.println("MulticurveBuildingHullWhiteDiscountFuturesUSDTest:" + nbTest + " curve construction / 1 unit: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 1 unit: 06-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 920 ms for 100 sets.

  }

}
