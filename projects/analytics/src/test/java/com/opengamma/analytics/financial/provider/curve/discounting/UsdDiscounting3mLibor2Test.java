/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve.discounting;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.date.CalendarAdapter;
import com.opengamma.analytics.date.SimpleWorkingDayCalendar;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
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
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveTestUtils;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.SingleCurveBundle;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
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
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Build of curve in several blocks with relevant Jacobian matrices.
 */
@Test(groups = TestGroup.UNIT)
public class UsdDiscounting3mLibor2Test {
  private static final Interpolator1D INTERPOLATOR_LINEAR =
      NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME);
  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();
  private static final double TOLERANCE_ROOT = 1.0E-10;
  private static final int STEP_MAX = 100;
  private static final CalendarAdapter NYC =
      new CalendarAdapter(new SimpleWorkingDayCalendar("NYC", Collections.<LocalDate>emptySet(), DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
  private static final Currency USD = Currency.USD;
  private static final FXMatrix FX_MATRIX = new FXMatrix(USD);
  private static final double NOTIONAL = 1.0;
  private static final GeneratorSwapFixedON GENERATOR_OIS_USD = GeneratorSwapFixedONMaster.getInstance().getGenerator("USD1YFEDFUND", NYC);
  /** An overnight USD index */
  private static final IndexON USD_OVERNIGHT_INDEX = GENERATOR_OIS_USD.getIndex();
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_USD = new GeneratorDepositON("USD Deposit ON", USD, NYC, USD_OVERNIGHT_INDEX.getDayCount());
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_SWAP_MASTER.getGenerator("USD6MLIBOR3M", NYC);
  /** A 3M USD LIBOR index */
  private static final IborIndex USD_3M_LIBOR_INDEX = USD6MLIBOR3M.getIborIndex();
  private static final GeneratorFRA GENERATOR_FRA = new GeneratorFRA("GENERATOR_FRA", USD_3M_LIBOR_INDEX, NYC);
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
  private static final Period[] DSC_USD_TENOR = new Period[] {Period.ofDays(0), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1),
    Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
  private static final GeneratorAttributeIR[] DSC_USD_ATTR = new GeneratorAttributeIR[DSC_USD_TENOR.length];
  static {
    for (int i = 0; i < DSC_USD_TENOR.length; i++) {
      DSC_USD_ATTR[i] = new GeneratorAttributeIR(DSC_USD_TENOR[i]);
    }
  }
  /** Market values for the Fwd 3M USD curve */
  private static final double[] FWD3_USD_MARKET_QUOTES = new double[] {0.0420, 0.0420, 0.0420, 0.0420, 0.0430, 0.0470, 0.0540, 0.0570, 0.0600 };
  /** Generators for the Fwd 3M USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD3_USD_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_USDLIBOR3M, GENERATOR_FRA, GENERATOR_FRA, USD6MLIBOR3M,
    USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M };
  /** Tenors for the Fwd 3M USD curve */
  private static final Period[] FWD3_USD_TENOR = new Period[] {Period.ofMonths(0), Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(5),
    Period.ofYears(7), Period.ofYears(10) };
  private static final GeneratorAttributeIR[] FWD3_USD_ATTR = new GeneratorAttributeIR[FWD3_USD_TENOR.length];
  static {
    for (int i = 0; i < FWD3_USD_TENOR.length; i++) {
      FWD3_USD_ATTR[i] = new GeneratorAttributeIR(FWD3_USD_TENOR[i]);
    }
  }
  /** Standard USD discounting curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_DSC_USD;
  /** Standard USD Forward 3M curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_FWD3_USD;
  /** Units of curves */
  private static final int[] NB_UNITS = new int[] {2, 1, 1 };
  private static final int NB_BLOCKS = NB_UNITS.length;
  private static final InstrumentDefinition<?>[][][][] DEFINITIONS_UNITS = new InstrumentDefinition<?>[NB_BLOCKS][][][];
  private static final GeneratorYDCurve[][][] GENERATORS_UNITS = new GeneratorYDCurve[NB_BLOCKS][][];
  private static final String[][][] NAMES_UNITS = new String[NB_BLOCKS][][];
  private static final MulticurveProviderDiscount KNOWN_DATA = new MulticurveProviderDiscount(FX_MATRIX);
  private static final LinkedHashMap<String, Currency> DSC_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IndexON[]> FWD_ON_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IborIndex[]> FWD_IBOR_MAP = new LinkedHashMap<>();

  static {
    DEFINITIONS_DSC_USD = getDefinitions(DSC_USD_MARKET_QUOTES, DSC_USD_GENERATORS, DSC_USD_ATTR);
    DEFINITIONS_FWD3_USD = getDefinitions(FWD3_USD_MARKET_QUOTES, FWD3_USD_GENERATORS, FWD3_USD_ATTR);
    for (int i = 0; i < NB_BLOCKS; i++) {
      DEFINITIONS_UNITS[i] = new InstrumentDefinition<?>[NB_UNITS[i]][][];
      GENERATORS_UNITS[i] = new GeneratorYDCurve[NB_UNITS[i]][];
      NAMES_UNITS[i] = new String[NB_UNITS[i]][];
    }
    DEFINITIONS_UNITS[0][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD };
    DEFINITIONS_UNITS[0][1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_USD };
    DEFINITIONS_UNITS[1][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD, DEFINITIONS_FWD3_USD };
    DEFINITIONS_UNITS[2][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD };
    final GeneratorYDCurve genIntLin = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR_LINEAR);
    GENERATORS_UNITS[0][0] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[0][1] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[1][0] = new GeneratorYDCurve[] {genIntLin, genIntLin };
    GENERATORS_UNITS[2][0] = new GeneratorYDCurve[] {genIntLin };
    NAMES_UNITS[0][0] = new String[] {CURVE_NAME_DSC_USD };
    NAMES_UNITS[0][1] = new String[] {CURVE_NAME_FWD3_USD };
    NAMES_UNITS[1][0] = new String[] {CURVE_NAME_DSC_USD, CURVE_NAME_FWD3_USD };
    NAMES_UNITS[2][0] = new String[] {CURVE_NAME_DSC_USD };
    DSC_MAP.put(CURVE_NAME_DSC_USD, USD);
    FWD_ON_MAP.put(CURVE_NAME_DSC_USD, new IndexON[] {USD_OVERNIGHT_INDEX });
    FWD_IBOR_MAP.put(CURVE_NAME_FWD3_USD, new IborIndex[] {USD_3M_LIBOR_INDEX });
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  private static InstrumentDefinition<?>[] getDefinitions(final double[] marketQuotes, final GeneratorInstrument[] generators, final GeneratorAttribute[] attribute) {
    final InstrumentDefinition<?>[] definitions = new InstrumentDefinition<?>[marketQuotes.length];
    for (int i = 0; i < marketQuotes.length; i++) {
      definitions[i] = generators[i].generateInstrument(NOW, marketQuotes[i], NOTIONAL, attribute[i]);
    }
    return definitions;
  }

  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> DSC_LIBOR_SIMULTANEOUS_BEFORE_FIXING;
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> DSC_THEN_LIBOR_BEFORE_FIXING;
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> DSC_BEFORE_FIXING;
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> DSC_LIBOR_SIMULTANEOUS_AFTER_FIXING;
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> DSC_THEN_LIBOR_AFTER_FIXING;
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> DSC_AFTER_FIXING;

  // Calculator
  private static final PresentValueDiscountingCalculator PVC = PresentValueDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PSMQCSC = ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();

  private static final MulticurveDiscountBuildingRepository CURVE_BUILDING_REPOSITORY = new MulticurveDiscountBuildingRepository(TOLERANCE_ROOT, TOLERANCE_ROOT, STEP_MAX);

  private static final double TOLERANCE_CAL = 1.0E-9;

  static {
    DSC_THEN_LIBOR_BEFORE_FIXING = makeCurvesFromDefinitions(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0],
        KNOWN_DATA, PSMQC, PSMQCSC, FIXING_TS_WITHOUT_TODAY, NOW, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP);
    DSC_LIBOR_SIMULTANEOUS_BEFORE_FIXING = makeCurvesFromDefinitions(DEFINITIONS_UNITS[1], GENERATORS_UNITS[1], NAMES_UNITS[1],
        KNOWN_DATA, PSMQC, PSMQCSC, FIXING_TS_WITHOUT_TODAY, NOW, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP);
    DSC_BEFORE_FIXING = makeCurvesFromDefinitions(DEFINITIONS_UNITS[2], GENERATORS_UNITS[2], NAMES_UNITS[2],
        KNOWN_DATA, PSMQC, PSMQCSC, FIXING_TS_WITHOUT_TODAY, NOW, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP);
    DSC_THEN_LIBOR_AFTER_FIXING = makeCurvesFromDefinitions(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0],
        KNOWN_DATA, PSMQC, PSMQCSC, FIXING_TS_WITH_TODAY, NOW, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP);
    DSC_LIBOR_SIMULTANEOUS_AFTER_FIXING = makeCurvesFromDefinitions(DEFINITIONS_UNITS[1], GENERATORS_UNITS[1], NAMES_UNITS[1],
        KNOWN_DATA, PSMQC, PSMQCSC, FIXING_TS_WITH_TODAY, NOW, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP);
    DSC_AFTER_FIXING = makeCurvesFromDefinitions(DEFINITIONS_UNITS[2], GENERATORS_UNITS[2], NAMES_UNITS[2],
        KNOWN_DATA, PSMQC, PSMQCSC, FIXING_TS_WITH_TODAY, NOW, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP);
  }

  @Test
  public void testJacobianSizes() {
    CurveBuildingBlockBundle fullJacobian = DSC_THEN_LIBOR_BEFORE_FIXING.getSecond();
    Map<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> fullJacobianData = fullJacobian.getData();
    assertEquals(fullJacobianData.size(), 2);
    DoubleMatrix2D discountingJacobianMatrix = fullJacobianData.get(CURVE_NAME_DSC_USD).getSecond();
    assertEquals(discountingJacobianMatrix.getNumberOfRows(), DSC_USD_MARKET_QUOTES.length);
    assertEquals(discountingJacobianMatrix.getNumberOfColumns(), DSC_USD_MARKET_QUOTES.length);
    DoubleMatrix2D liborJacobianMatrix = fullJacobianData.get(CURVE_NAME_FWD3_USD).getSecond();
    assertEquals(liborJacobianMatrix.getNumberOfRows(), FWD3_USD_MARKET_QUOTES.length);
    assertEquals(liborJacobianMatrix.getNumberOfColumns(), DSC_USD_MARKET_QUOTES.length + FWD3_USD_MARKET_QUOTES.length);
    fullJacobian = DSC_LIBOR_SIMULTANEOUS_BEFORE_FIXING.getSecond();
    fullJacobianData = fullJacobian.getData();
    assertEquals(fullJacobianData.size(), 2);
    discountingJacobianMatrix = fullJacobianData.get(CURVE_NAME_DSC_USD).getSecond();
    assertEquals(discountingJacobianMatrix.getNumberOfRows(), DSC_USD_MARKET_QUOTES.length);
    assertEquals(discountingJacobianMatrix.getNumberOfColumns(), DSC_USD_MARKET_QUOTES.length + FWD3_USD_MARKET_QUOTES.length);
    liborJacobianMatrix = fullJacobianData.get(CURVE_NAME_FWD3_USD).getSecond();
    assertEquals(liborJacobianMatrix.getNumberOfRows(), FWD3_USD_MARKET_QUOTES.length);
    assertEquals(liborJacobianMatrix.getNumberOfColumns(), DSC_USD_MARKET_QUOTES.length + FWD3_USD_MARKET_QUOTES.length);
    fullJacobian = DSC_BEFORE_FIXING.getSecond();
    fullJacobianData = fullJacobian.getData();
    assertEquals(fullJacobianData.size(), 1);
    discountingJacobianMatrix = fullJacobianData.get(CURVE_NAME_DSC_USD).getSecond();
    assertEquals(discountingJacobianMatrix.getNumberOfRows(), DSC_USD_MARKET_QUOTES.length);
    assertEquals(discountingJacobianMatrix.getNumberOfColumns(), DSC_USD_MARKET_QUOTES.length);
  }

  @Test
  public void testInstrumentsInCurvePriceToZero() {
    curveConstructionTest(DEFINITIONS_UNITS[0], DSC_THEN_LIBOR_BEFORE_FIXING.getFirst(), FIXING_TS_WITHOUT_TODAY);
    curveConstructionTest(DEFINITIONS_UNITS[1], DSC_LIBOR_SIMULTANEOUS_BEFORE_FIXING.getFirst(), FIXING_TS_WITHOUT_TODAY);
    curveConstructionTest(DEFINITIONS_UNITS[2], DSC_BEFORE_FIXING.getFirst(), FIXING_TS_WITHOUT_TODAY);
    curveConstructionTest(DEFINITIONS_UNITS[0], DSC_THEN_LIBOR_AFTER_FIXING.getFirst(), FIXING_TS_WITH_TODAY);
    curveConstructionTest(DEFINITIONS_UNITS[1], DSC_LIBOR_SIMULTANEOUS_AFTER_FIXING.getFirst(), FIXING_TS_WITH_TODAY);
    curveConstructionTest(DEFINITIONS_UNITS[2], DSC_AFTER_FIXING.getFirst(), FIXING_TS_WITH_TODAY);
  }

  private static void curveConstructionTest(final InstrumentDefinition<?>[][][] definitions, final MulticurveProviderDiscount curves,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs) {
    final int nbBlocks = definitions.length;
    for (int i = 0; i < nbBlocks; i++) {
      final InstrumentDerivative[][] instruments = CurveTestUtils.convert(definitions[i], fixingTs, NOW);
      for (final InstrumentDerivative[] instrumentsForCurve : instruments) {
        for (final InstrumentDerivative instrument : instrumentsForCurve) {
          final MultipleCurrencyAmount pv = instrument.accept(PVC, curves);
          final double usdPv = FX_MATRIX.convert(pv, USD).getAmount();
          assertEquals(0, usdPv, TOLERANCE_CAL);
        }
      }
    }
  }

  @Test
  public void blockBundleDscFiniteDifferenceTest1() {
    final int discountingCurveSize = DSC_USD_MARKET_QUOTES.length;
    final CurveBuildingBlockBundle fullInverseJacobian = DSC_THEN_LIBOR_BEFORE_FIXING.getSecond();
    final double[] upDiscountingCurveQuotes = new double[] {0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400 };
    final double[] downDiscountingCurveQuotes = new double[] {0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400 };
    final double bump = 1e-6;
    for (int i = 0; i < discountingCurveSize; i++) {
      upDiscountingCurveQuotes[i] += bump;
      downDiscountingCurveQuotes[i] -= bump;
      final InstrumentDefinition<?>[] upDefinitions = getDefinitions(upDiscountingCurveQuotes, DSC_USD_GENERATORS, DSC_USD_ATTR);
      final InstrumentDefinition<?>[] downDefinitions = getDefinitions(downDiscountingCurveQuotes, DSC_USD_GENERATORS, DSC_USD_ATTR);
      final InstrumentDefinition<?>[][][] upUnits = new InstrumentDefinition<?>[2][][];
      final InstrumentDefinition<?>[][][] downUnits = new InstrumentDefinition<?>[2][][];
      upUnits[0] = new InstrumentDefinition<?>[][] { upDefinitions };
      downUnits[0] = new InstrumentDefinition<?>[][] { downDefinitions };
      upUnits[1] = new InstrumentDefinition<?>[][] { DEFINITIONS_FWD3_USD };
      downUnits[1] = new InstrumentDefinition<?>[][] { DEFINITIONS_FWD3_USD };
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults =
          makeCurvesFromDefinitions(upUnits, GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQC, PSMQCSC, FIXING_TS_WITHOUT_TODAY,
              NOW, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults =
          makeCurvesFromDefinitions(downUnits, GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQC, PSMQCSC, FIXING_TS_WITHOUT_TODAY,
              NOW, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_DSC_USD)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_DSC_USD)).getCurve().getYData();
      for (int j = 0; j < discountingCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        // note columns then rows tested
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_DSC_USD).getSecond().getData()[j][i];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_DSC_USD + ": column=" + j + " row=" + i,
            expectedSensitivity, dYielddQuote, bump);
      }
      upDiscountingCurveQuotes[i] -= bump;
      downDiscountingCurveQuotes[i] += bump;
    }
  }

  @Test
  public void blockBundleDscFiniteDifferenceTest2() {
    final int discountingCurveSize = DSC_USD_MARKET_QUOTES.length;
    final int liborCurveSize = FWD3_USD_MARKET_QUOTES.length;
    final CurveBuildingBlockBundle fullInverseJacobian = DSC_LIBOR_SIMULTANEOUS_BEFORE_FIXING.getSecond();
    final double[] upDiscountingCurveQuotes = new double[] {0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400 };
    final double[] downDiscountingCurveQuotes = new double[] {0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400 };
    final double[] upLiborCurveQuotes = new double[] {0.0420, 0.0420, 0.0420, 0.0420, 0.0430, 0.0470, 0.0540, 0.0570, 0.0600 };
    final double[] downLiborCurveQuotes = new double[] {0.0420, 0.0420, 0.0420, 0.0420, 0.0430, 0.0470, 0.0540, 0.0570, 0.0600 };
    final double bump = 1e-6;
    for (int i = 0; i < discountingCurveSize; i++) {
      upDiscountingCurveQuotes[i] += bump;
      downDiscountingCurveQuotes[i] -= bump;
      final InstrumentDefinition<?>[] upDefinitions = getDefinitions(upDiscountingCurveQuotes, DSC_USD_GENERATORS, DSC_USD_ATTR);
      final InstrumentDefinition<?>[] downDefinitions = getDefinitions(downDiscountingCurveQuotes, DSC_USD_GENERATORS, DSC_USD_ATTR);
      final InstrumentDefinition<?>[][][] upUnits = new InstrumentDefinition<?>[1][][];
      final InstrumentDefinition<?>[][][] downUnits = new InstrumentDefinition<?>[1][][];
      upUnits[0] = new InstrumentDefinition<?>[][] { upDefinitions, DEFINITIONS_FWD3_USD };
      downUnits[0] = new InstrumentDefinition<?>[][] { downDefinitions, DEFINITIONS_FWD3_USD };
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults =
          makeCurvesFromDefinitions(upUnits, GENERATORS_UNITS[1], NAMES_UNITS[1], KNOWN_DATA, PSMQC, PSMQCSC, FIXING_TS_WITHOUT_TODAY,
              NOW, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults =
          makeCurvesFromDefinitions(downUnits, GENERATORS_UNITS[1], NAMES_UNITS[1], KNOWN_DATA, PSMQC, PSMQCSC, FIXING_TS_WITHOUT_TODAY,
              NOW, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_DSC_USD)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_DSC_USD)).getCurve().getYData();
      for (int j = 0; j < discountingCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        // note columns then rows tested
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_DSC_USD).getSecond().getData()[j][i];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_DSC_USD + ": column=" + i + " row=" + j,
            expectedSensitivity, dYielddQuote, bump);
      }
      upDiscountingCurveQuotes[i] -= bump;
      downDiscountingCurveQuotes[i] += bump;
    }
    for (int i = 0; i < liborCurveSize; i++) {
      upLiborCurveQuotes[i] += bump;
      downLiborCurveQuotes[i] -= bump;
      final InstrumentDefinition<?>[] upLiborCurveDefinitions = getDefinitions(upLiborCurveQuotes, FWD3_USD_GENERATORS, FWD3_USD_ATTR);
      final InstrumentDefinition<?>[] downLiborCurveDefinitions = getDefinitions(downLiborCurveQuotes, FWD3_USD_GENERATORS, FWD3_USD_ATTR);
      final InstrumentDefinition<?>[][][] upUnits = new InstrumentDefinition<?>[1][][];
      final InstrumentDefinition<?>[][][] downUnits = new InstrumentDefinition<?>[1][][];
      upUnits[0] = new InstrumentDefinition<?>[][] { DEFINITIONS_DSC_USD, upLiborCurveDefinitions };
      downUnits[0] = new InstrumentDefinition<?>[][] { DEFINITIONS_DSC_USD, downLiborCurveDefinitions };
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults =
          makeCurvesFromDefinitions(upUnits, GENERATORS_UNITS[1], NAMES_UNITS[1], KNOWN_DATA, PSMQC, PSMQCSC, FIXING_TS_WITHOUT_TODAY,
              NOW, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults =
          makeCurvesFromDefinitions(downUnits, GENERATORS_UNITS[1], NAMES_UNITS[1], KNOWN_DATA, PSMQC, PSMQCSC, FIXING_TS_WITHOUT_TODAY,
              NOW, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_DSC_USD)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_DSC_USD)).getCurve().getYData();
      final int offset = discountingCurveSize - 1;
      for (int j = 0; j < liborCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_DSC_USD).getSecond().getData()[j][offset];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_DSC_USD + ": column=" + offset + " row=" + j,
            expectedSensitivity, dYielddQuote, bump);
        assertEquals(expectedSensitivity, 0, 1e-12);
      }
      upLiborCurveQuotes[i] -= bump;
      downLiborCurveQuotes[i] += bump;
    }
  }

  @Test
  public void blockBundleDscFiniteDifferenceTest3() {
    final int discountingCurveSize = DSC_USD_MARKET_QUOTES.length;
    final CurveBuildingBlockBundle fullInverseJacobian = DSC_BEFORE_FIXING.getSecond();
    final double[] upDiscountingCurveQuotes = new double[] {0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400 };
    final double[] downDiscountingCurveQuotes = new double[] {0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400 };
    final double bump = 1e-6;
    for (int i = 0; i < discountingCurveSize; i++) {
      upDiscountingCurveQuotes[i] += bump;
      downDiscountingCurveQuotes[i] -= bump;
      final InstrumentDefinition<?>[] upDefinitions = getDefinitions(upDiscountingCurveQuotes, DSC_USD_GENERATORS, DSC_USD_ATTR);
      final InstrumentDefinition<?>[] downDefinitions = getDefinitions(downDiscountingCurveQuotes, DSC_USD_GENERATORS, DSC_USD_ATTR);
      final InstrumentDefinition<?>[][][] upUnits = new InstrumentDefinition<?>[2][][];
      final InstrumentDefinition<?>[][][] downUnits = new InstrumentDefinition<?>[2][][];
      upUnits[0] = new InstrumentDefinition<?>[][] { upDefinitions };
      downUnits[0] = new InstrumentDefinition<?>[][] { downDefinitions };
      upUnits[1] = new InstrumentDefinition<?>[][] { DEFINITIONS_FWD3_USD };
      downUnits[1] = new InstrumentDefinition<?>[][] { DEFINITIONS_FWD3_USD };
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults =
          makeCurvesFromDefinitions(upUnits, GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQC, PSMQCSC, FIXING_TS_WITHOUT_TODAY,
              NOW, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults =
          makeCurvesFromDefinitions(downUnits, GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQC, PSMQCSC, FIXING_TS_WITHOUT_TODAY,
              NOW, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_DSC_USD)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_DSC_USD)).getCurve().getYData();
      for (int j = 0; j < discountingCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        // note columns then rows tested
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_DSC_USD).getSecond().getData()[j][i];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_DSC_USD + ": column=" + j + " row=" + i,
            expectedSensitivity, dYielddQuote, bump);
      }
      upDiscountingCurveQuotes[i] -= bump;
      downDiscountingCurveQuotes[i] += bump;
    }
  }

  @Test
  public void blockBundleFwd3MFiniteDifferenceTest1() {
    final int discountingCurveSize = DSC_USD_MARKET_QUOTES.length;
    final int liborCurveSize = FWD3_USD_MARKET_QUOTES.length;
    final CurveBuildingBlockBundle fullInverseJacobian = DSC_THEN_LIBOR_BEFORE_FIXING.getSecond();
    final double[] upDiscountingCurveQuotes = new double[] {0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400 };
    final double[] downDiscountingCurveQuotes = new double[] {0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400 };
    final double[] upLiborCurveQuotes = new double[] {0.0420, 0.0420, 0.0420, 0.0420, 0.0430, 0.0470, 0.0540, 0.0570, 0.0600 };
    final double[] downLiborCurveQuotes = new double[] {0.0420, 0.0420, 0.0420, 0.0420, 0.0430, 0.0470, 0.0540, 0.0570, 0.0600 };
    final double bump = 1e-6;

    for (int i = 0; i < discountingCurveSize; i++) {
      upDiscountingCurveQuotes[i] += bump;
      downDiscountingCurveQuotes[i] -= bump;
      final InstrumentDefinition<?>[] upDiscountingCurveDefinitions = getDefinitions(upDiscountingCurveQuotes, DSC_USD_GENERATORS, DSC_USD_ATTR);
      final InstrumentDefinition<?>[] downDiscountingCurveDefinitions = getDefinitions(downDiscountingCurveQuotes, DSC_USD_GENERATORS, DSC_USD_ATTR);
      final InstrumentDefinition<?>[][][] upUnits = new InstrumentDefinition<?>[2][][];
      final InstrumentDefinition<?>[][][] downUnits = new InstrumentDefinition<?>[2][][];
      upUnits[0] = new InstrumentDefinition<?>[][] {upDiscountingCurveDefinitions };
      downUnits[0] = new InstrumentDefinition<?>[][] {downDiscountingCurveDefinitions };
      upUnits[1] = new InstrumentDefinition<?>[][] { DEFINITIONS_FWD3_USD };
      downUnits[1] = new InstrumentDefinition<?>[][] { DEFINITIONS_FWD3_USD };
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults =
          makeCurvesFromDefinitions(upUnits, GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQC, PSMQCSC, FIXING_TS_WITHOUT_TODAY,
              NOW, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults =
          makeCurvesFromDefinitions(downUnits, GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQC, PSMQCSC, FIXING_TS_WITHOUT_TODAY,
              NOW, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_FWD3_USD)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_FWD3_USD)).getCurve().getYData();
      for (int j = 0; j < liborCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_FWD3_USD).getSecond().getData()[j][i];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_FWD3_USD + ": column=" + i + " row=" + j,
            expectedSensitivity, dYielddQuote, bump);
      }
      upDiscountingCurveQuotes[i] -= bump;
      downDiscountingCurveQuotes[i] += bump;
    }
    for (int i = 0; i < liborCurveSize; i++) {
      upLiborCurveQuotes[i] += bump;
      downLiborCurveQuotes[i] -= bump;
      final InstrumentDefinition<?>[] upLiborCurveDefinitions = getDefinitions(upLiborCurveQuotes, FWD3_USD_GENERATORS, FWD3_USD_ATTR);
      final InstrumentDefinition<?>[] downLiborCurveDefinitions = getDefinitions(downLiborCurveQuotes, FWD3_USD_GENERATORS, FWD3_USD_ATTR);
      final InstrumentDefinition<?>[][][] upUnits = new InstrumentDefinition<?>[2][][];
      final InstrumentDefinition<?>[][][] downUnits = new InstrumentDefinition<?>[2][][];
      upUnits[0] = new InstrumentDefinition<?>[][] { DEFINITIONS_DSC_USD };
      downUnits[0] = new InstrumentDefinition<?>[][] { DEFINITIONS_DSC_USD };
      upUnits[1] = new InstrumentDefinition<?>[][] { upLiborCurveDefinitions };
      downUnits[1] = new InstrumentDefinition<?>[][] {downLiborCurveDefinitions };
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults =
          makeCurvesFromDefinitions(upUnits, GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQC, PSMQCSC, FIXING_TS_WITHOUT_TODAY,
              NOW, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults =
          makeCurvesFromDefinitions(downUnits, GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQC, PSMQCSC, FIXING_TS_WITHOUT_TODAY,
              NOW, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_FWD3_USD)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_FWD3_USD)).getCurve().getYData();
      final int offset = i + discountingCurveSize;
      for (int j = 0; j < liborCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_FWD3_USD).getSecond().getData()[j][offset];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_FWD3_USD + ": column=" + offset + " row=" + j,
            expectedSensitivity, dYielddQuote, bump);
      }
      upLiborCurveQuotes[i] -= bump;
      downLiborCurveQuotes[i] += bump;
    }
  }

  @Test
  public void blockBundleFwd3MFiniteDifferenceTest2() {
    final int discountingCurveSize = DSC_USD_MARKET_QUOTES.length;
    final int liborCurveSize = FWD3_USD_MARKET_QUOTES.length;
    final CurveBuildingBlockBundle fullInverseJacobian = DSC_LIBOR_SIMULTANEOUS_BEFORE_FIXING.getSecond();
    final double[] upDiscountingCurveQuotes = new double[] {0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400 };
    final double[] downDiscountingCurveQuotes = new double[] {0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400 };
    final double[] upLiborCurveQuotes = new double[] {0.0420, 0.0420, 0.0420, 0.0420, 0.0430, 0.0470, 0.0540, 0.0570, 0.0600 };
    final double[] downLiborCurveQuotes = new double[] {0.0420, 0.0420, 0.0420, 0.0420, 0.0430, 0.0470, 0.0540, 0.0570, 0.0600 };
    final double bump = 1e-6;

    for (int i = 0; i < discountingCurveSize; i++) {
      upDiscountingCurveQuotes[i] += bump;
      downDiscountingCurveQuotes[i] -= bump;
      final InstrumentDefinition<?>[] upDiscountingCurveDefinitions = getDefinitions(upDiscountingCurveQuotes, DSC_USD_GENERATORS, DSC_USD_ATTR);
      final InstrumentDefinition<?>[] downDiscountingCurveDefinitions = getDefinitions(downDiscountingCurveQuotes, DSC_USD_GENERATORS, DSC_USD_ATTR);
      final InstrumentDefinition<?>[][][] upUnits = new InstrumentDefinition<?>[1][][];
      final InstrumentDefinition<?>[][][] downUnits = new InstrumentDefinition<?>[1][][];
      upUnits[0] = new InstrumentDefinition<?>[][] {upDiscountingCurveDefinitions, DEFINITIONS_FWD3_USD };
      downUnits[0] = new InstrumentDefinition<?>[][] {downDiscountingCurveDefinitions, DEFINITIONS_FWD3_USD };
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults =
          makeCurvesFromDefinitions(upUnits, GENERATORS_UNITS[1], NAMES_UNITS[1], KNOWN_DATA, PSMQC, PSMQCSC, FIXING_TS_WITHOUT_TODAY,
              NOW, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults =
          makeCurvesFromDefinitions(downUnits, GENERATORS_UNITS[1], NAMES_UNITS[1], KNOWN_DATA, PSMQC, PSMQCSC, FIXING_TS_WITHOUT_TODAY,
              NOW, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_FWD3_USD)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_FWD3_USD)).getCurve().getYData();
      for (int j = 0; j < liborCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_FWD3_USD).getSecond().getData()[j][i];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_FWD3_USD + ": column=" + i + " row=" + j,
            expectedSensitivity, dYielddQuote, bump);
      }
      upDiscountingCurveQuotes[i] -= bump;
      downDiscountingCurveQuotes[i] += bump;
    }
    for (int i = 0; i < liborCurveSize; i++) {
      upLiborCurveQuotes[i] += bump;
      downLiborCurveQuotes[i] -= bump;
      final InstrumentDefinition<?>[] upLiborCurveDefinitions = getDefinitions(upLiborCurveQuotes, FWD3_USD_GENERATORS, FWD3_USD_ATTR);
      final InstrumentDefinition<?>[] downLiborCurveDefinitions = getDefinitions(downLiborCurveQuotes, FWD3_USD_GENERATORS, FWD3_USD_ATTR);
      final InstrumentDefinition<?>[][][] upUnits = new InstrumentDefinition<?>[1][][];
      final InstrumentDefinition<?>[][][] downUnits = new InstrumentDefinition<?>[1][][];
      upUnits[0] = new InstrumentDefinition<?>[][] { DEFINITIONS_DSC_USD, upLiborCurveDefinitions };
      downUnits[0] = new InstrumentDefinition<?>[][] { DEFINITIONS_DSC_USD, downLiborCurveDefinitions };
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults =
          makeCurvesFromDefinitions(upUnits, GENERATORS_UNITS[1], NAMES_UNITS[1], KNOWN_DATA, PSMQC, PSMQCSC, FIXING_TS_WITHOUT_TODAY,
              NOW, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults =
          makeCurvesFromDefinitions(downUnits, GENERATORS_UNITS[1], NAMES_UNITS[1], KNOWN_DATA, PSMQC, PSMQCSC, FIXING_TS_WITHOUT_TODAY,
              NOW, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_FWD3_USD)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_FWD3_USD)).getCurve().getYData();
      final int offset = i + discountingCurveSize;
      for (int j = 0; j < liborCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_FWD3_USD).getSecond().getData()[j][offset];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_FWD3_USD + ": column=" + offset + " row=" + j,
            expectedSensitivity, dYielddQuote, bump);
      }
      upLiborCurveQuotes[i] -= bump;
      downLiborCurveQuotes[i] += bump;
    }
  }

  @Test
  public void comparison1Unit2Units() {
    final MulticurveProviderDiscount[] units =
        new MulticurveProviderDiscount[] { DSC_THEN_LIBOR_BEFORE_FIXING.getFirst(), DSC_LIBOR_SIMULTANEOUS_BEFORE_FIXING.getFirst() };
    final CurveBuildingBlockBundle[] bb =
        new CurveBuildingBlockBundle[] { DSC_THEN_LIBOR_BEFORE_FIXING.getSecond(), DSC_LIBOR_SIMULTANEOUS_BEFORE_FIXING.getSecond() };
    final YieldAndDiscountCurve[] curveDsc = new YieldAndDiscountCurve[] { units[0].getCurve(USD), units[1].getCurve(USD) };
    final YieldAndDiscountCurve[] curveFwd = new YieldAndDiscountCurve[] { units[0].getCurve(USD_3M_LIBOR_INDEX), units[1].getCurve(USD_3M_LIBOR_INDEX) };
    assertEquals("Curve construction: 1 unit / 2 units ", curveDsc[0].getNumberOfParameters(), curveDsc[1].getNumberOfParameters());
    assertEquals("Curve construction: 1 unit / 2 units ", curveFwd[0].getNumberOfParameters(), curveFwd[1].getNumberOfParameters());
    assertArrayEquals("Curve construction: 1 unit / 2 units ", ArrayUtils.toPrimitive(((YieldCurve) curveDsc[0]).getCurve().getXData()),
        ArrayUtils.toPrimitive(((YieldCurve) curveDsc[1]).getCurve().getXData()), TOLERANCE_CAL);
    assertArrayEquals("Curve construction: 1 unit / 2 units ", ArrayUtils.toPrimitive(((YieldCurve) curveDsc[0]).getCurve().getYData()),
        ArrayUtils.toPrimitive(((YieldCurve) curveDsc[1]).getCurve().getYData()), TOLERANCE_CAL);
    assertArrayEquals("Curve construction: 1 unit / 2 units ", ArrayUtils.toPrimitive(((YieldCurve) curveFwd[0]).getCurve().getXData()),
        ArrayUtils.toPrimitive(((YieldCurve) curveFwd[1]).getCurve().getXData()), TOLERANCE_CAL);
    assertArrayEquals("Curve construction: 1 unit / 2 units ", ArrayUtils.toPrimitive(((YieldCurve) curveFwd[0]).getCurve().getYData()),
        ArrayUtils.toPrimitive(((YieldCurve) curveFwd[1]).getCurve().getYData()), TOLERANCE_CAL);

    assertEquals("Curve construction: 1 unit / 2 units ", bb[0].getBlock(CURVE_NAME_FWD3_USD).getFirst(), bb[1].getBlock(CURVE_NAME_FWD3_USD).getFirst());
    // Test note: the discounting curve building blocks are not the same; in one case both curves are build together in the other one after the other.
    final int nbLineDsc = bb[0].getBlock(CURVE_NAME_DSC_USD).getSecond().getNumberOfRows();
    final int nbLineFwd = bb[0].getBlock(CURVE_NAME_FWD3_USD).getSecond().getNumberOfRows();
    assertEquals("Curve construction: 1 unit / 2 units ", bb[1].getBlock(CURVE_NAME_DSC_USD).getSecond().getNumberOfRows(), nbLineDsc);
    assertEquals("Curve construction: 1 unit / 2 units ", bb[1].getBlock(CURVE_NAME_FWD3_USD).getSecond().getNumberOfRows(), nbLineFwd);
    for (int i = 0; i < nbLineFwd; i++) {
      assertArrayEquals("Curve construction: 1 unit / 2 units ", bb[0].getBlock(CURVE_NAME_FWD3_USD).getSecond().getRowVector(i).getData(), bb[1].getBlock(CURVE_NAME_FWD3_USD).getSecond()
          .getRowVector(i).getData(), TOLERANCE_CAL);
      for (int j = 0; j < nbLineDsc; j++) { // Test rely on dsc being first
        assertEquals("Curve construction: 1 unit / 2 units ", bb[0].getBlock(CURVE_NAME_FWD3_USD).getSecond().getRowVector(i).getData()[j], bb[1].getBlock(CURVE_NAME_FWD3_USD)
            .getSecond().getRowVector(i).getData()[j], TOLERANCE_CAL);
      }
      for (int j = 0; j < nbLineFwd - nbLineDsc; j++) { // Test rely on dsc being first
        assertEquals("Curve construction: 1 unit / 2 units ", 0, bb[1].getBlock(CURVE_NAME_FWD3_USD).getSecond().getRowVector(i).getData()[j + nbLineDsc], TOLERANCE_CAL);
      }
    }
  }

  @Test(enabled = false)
  public void performance() {
    long startTime, endTime;
    final int nbTest = 1000;

    startTime = System.currentTimeMillis();
    for (int i = 0; i < nbTest; i++) {
      makeCurvesFromDefinitions(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQC, PSMQCSC, FIXING_TS_WITHOUT_TODAY,
          NOW, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction / 2 units: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 2 units: 02-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 430 ms for 100 sets.

    startTime = System.currentTimeMillis();
    for (int i = 0; i < nbTest; i++) {
      makeCurvesFromDefinitions(DEFINITIONS_UNITS[1], GENERATORS_UNITS[1], NAMES_UNITS[1], KNOWN_DATA, PSMQC, PSMQCSC, FIXING_TS_WITHOUT_TODAY,
          NOW, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction / 1 unit: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 1 unit: 02-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 440 ms for 100 sets.

    startTime = System.currentTimeMillis();
    for (int i = 0; i < nbTest; i++) {
      makeCurvesFromDefinitions(DEFINITIONS_UNITS[2], GENERATORS_UNITS[2], NAMES_UNITS[2], KNOWN_DATA, PSMQC, PSMQCSC, FIXING_TS_WITHOUT_TODAY,
          NOW, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction / 1 curve: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 1 curve: 20-May-2013: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 125 ms for 100 sets.

    startTime = System.currentTimeMillis();
    for (int i = 0; i < nbTest; i++) {
      makeCurvesFromDefinitions(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQC, PSMQCSC, FIXING_TS_WITHOUT_TODAY,
          NOW, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction / 2 units: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 2 units: 02-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 420 ms for 100 sets.

    startTime = System.currentTimeMillis();
    for (int i = 0; i < nbTest; i++) {
      makeCurvesFromDefinitions(DEFINITIONS_UNITS[1], GENERATORS_UNITS[1], NAMES_UNITS[1], KNOWN_DATA, PSMQC, PSMQCSC, FIXING_TS_WITHOUT_TODAY,
          NOW, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction / 1 unit: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 1 unit: 02-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 402 ms for 100 sets.

    startTime = System.currentTimeMillis();
    for (int i = 0; i < nbTest; i++) {
      makeCurvesFromDefinitions(DEFINITIONS_UNITS[2], GENERATORS_UNITS[2], NAMES_UNITS[2], KNOWN_DATA, PSMQC, PSMQCSC, FIXING_TS_WITHOUT_TODAY,
          NOW, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction / 1 curve: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 1 curve: 20-May-2013: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 125 ms for 100 sets.

  }

  private void curveConstructionTest(final InstrumentDefinition<?>[][][] definitions, final MulticurveProviderDiscount curves, final boolean withToday, final int block) {
    final int nbBlocks = definitions.length;
    for (int i = 0; i < nbBlocks; i++) {
      final InstrumentDerivative[][] instruments = CurveTestUtils.convert(definitions[i], withToday ? FIXING_TS_WITH_TODAY : FIXING_TS_WITHOUT_TODAY, NOW);
      final double[][] pv = new double[instruments.length][];
      for (int j = 0; j < instruments.length; j++) {
        pv[j] = new double[instruments[j].length];
        for (int k = 0; k < instruments[j].length; k++) {
          pv[j][k] = curves.getFxRates().convert(instruments[j][k].accept(PVC, curves), USD).getAmount();
          assertEquals("Curve construction: block " + block + ", unit " + i + " - instrument " + k, 0, pv[j][k], TOLERANCE_CAL);
        }
      }
    }
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
    ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(NOW, USD_3M_LIBOR_INDEX.getSpotLag() + startIndex * jump, NYC);
    final double[] rateDsc = new double[nbDate];
    final double[] startTime = new double[nbDate];
    try (final FileWriter writer = new FileWriter("fwd-dsc.csv")) {
      for (int i = 0; i < nbDate; i++) {
        startTime[i] = TimeCalculator.getTimeBetween(NOW, startDate);
        final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, USD_3M_LIBOR_INDEX, NYC);
        final double endTime = TimeCalculator.getTimeBetween(NOW, endDate);
        final double accrualFactor = USD_3M_LIBOR_INDEX.getDayCount().getDayCountFraction(startDate, endDate);
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

  @SuppressWarnings("unchecked")
  private static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDefinitions(
      final InstrumentDefinition<?>[][][] definitions, final GeneratorYDCurve[][] curveGenerators, final String[][] curveNames,
      final MulticurveProviderDiscount knownData, final InstrumentDerivativeVisitor<MulticurveProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<MulticurveProviderInterface, MulticurveSensitivity> sensitivityCalculator,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs, final ZonedDateTime valuationDate, final LinkedHashMap<String, Currency> discountingCurves,
      final LinkedHashMap<String, IborIndex[]> iborCurves, final LinkedHashMap<String, IndexON[]> overnightCurves) {
    final int nUnits = definitions.length;
    final MultiCurveBundle<GeneratorYDCurve>[] curveBundles = new MultiCurveBundle[nUnits];
    for (int i = 0; i < nUnits; i++) {
      final int nCurves = definitions[i].length;
      final SingleCurveBundle<GeneratorYDCurve>[] singleCurves = new SingleCurveBundle[nCurves];
      for (int j = 0; j < nCurves; j++) {
        final int nInstruments = definitions[i][j].length;
        final InstrumentDerivative[] derivatives = new InstrumentDerivative[nInstruments];
        final double[] initialGuess = new double[nInstruments];
        for (int k = 0; k < nInstruments; k++) {
          derivatives[k] = CurveTestUtils.convert(definitions[i][j][k], fixingTs, valuationDate);
          initialGuess[k] = definitions[i][j][k].accept(CurveTestUtils.RATES_INITIALIZATION);
        }
        final GeneratorYDCurve generator = curveGenerators[i][j].finalGenerator(derivatives);
        singleCurves[j] = new SingleCurveBundle<>(curveNames[i][j], derivatives, initialGuess, generator);
      }
      curveBundles[i] = new MultiCurveBundle<>(singleCurves);
    }
    return CURVE_BUILDING_REPOSITORY.makeCurvesFromDerivatives(curveBundles, knownData, discountingCurves, iborCurves, overnightCurves, calculator,
        sensitivityCalculator);
  }

}
