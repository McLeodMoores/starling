/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class MulticurveBuildingDiscountingSimpleWithoutDiscountTest {

//  private static final Interpolator1D INTERPOLATOR_LINEAR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
//      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
//
//  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();
//  private static final double TOLERANCE_ROOT = 1.0E-10;
//  private static final int STEP_MAX = 100;
//
//  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
//  private static final Currency USD = Currency.USD;
//
//  private static final double NOTIONAL = 1.0;
//
//  private static final GeneratorSwapFixedON GENERATOR_OIS_USD = GeneratorSwapFixedONMaster.getInstance().getGenerator("USD1YFEDFUND", NYC);
//  private static final IndexON INDEX_ON_USD = GENERATOR_OIS_USD.getIndex();
//  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_USD = new GeneratorDepositON("USD Deposit ON", USD, NYC, INDEX_ON_USD.getDayCount());
//  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
//  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_SWAP_MASTER.getGenerator("USD6MLIBOR3M", NYC);
//  private static final IborIndex USDLIBOR3M = USD6MLIBOR3M.getIborIndex();
//  private static final GeneratorDepositIbor GENERATOR_USDLIBOR3M = new GeneratorDepositIbor("GENERATOR_USDLIBOR3M", USDLIBOR3M, NYC);
//
//  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2011, 9, 28);
//
//  private static final ZonedDateTimeDoubleTimeSeries TS_EMPTY = ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC();
//  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
//    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.07, 0.08 });
//  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
//    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.07, 0.08 });
//  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_USD_WITH_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_USD_WITH_TODAY };
//  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_USD_WITHOUT_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_USD_WITHOUT_TODAY };
//
//  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_USD3M_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
//    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.0035, 0.0036 });
//  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_USD3M_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27) },
//      new double[] {0.0035 });
//
//  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_USD3M_WITH_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_USD3M_WITH_TODAY };
//  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_USD3M_WITHOUT_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_USD3M_WITHOUT_TODAY };
//
//  private static final String CURVE_NAME_FWD3_USD = "USD Fwd 3M";
//  private static final String[] CURVE_NAMES = {CURVE_NAME_FWD3_USD };
//
//  /** Market values for the dsc USD curve */
//  private static final double[] DSC_USD_MARKET_QUOTES = new double[] {0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400 };
//  /** Generators for the dsc USD curve */
//  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_USD_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_DEPOSIT_ON_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD,
//    GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD };
//  /** Tenors for the dsc USD curve */
//  private static final Period[] DSC_USD_TENOR = new Period[] {Period.ofDays(0), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3),
//    Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1),
//    Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
//  private static final GeneratorAttributeIR[] DSC_USD_ATTR = new GeneratorAttributeIR[DSC_USD_TENOR.length];
//  static {
//    for (int loopins = 0; loopins < DSC_USD_TENOR.length; loopins++) {
//      DSC_USD_ATTR[loopins] = new GeneratorAttributeIR(DSC_USD_TENOR[loopins]);
//    }
//  }
//  /** Market values for the Fwd 3M USD curve */
//  private static final double[] FWD3_USD_MARKET_QUOTES = new double[] {0.0420, 0.0420, 0.0420, 0.0430, 0.0470, 0.0540, 0.0570, 0.0600 };
//  /** Generators for the Fwd 3M USD curve */
//  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD3_USD_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_USDLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M,
//    USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M };
//  /** Tenors for the Fwd 3M USD curve */
//  private static final Period[] FWD3_USD_TENOR = new Period[] {Period.ofMonths(0), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2),
//    Period.ofYears(3), Period.ofYears(5), Period.ofYears(7),
//    Period.ofYears(10) };
//  private static final GeneratorAttributeIR[] FWD3_USD_ATTR = new GeneratorAttributeIR[FWD3_USD_TENOR.length];
//  static {
//    for (int loopins = 0; loopins < FWD3_USD_TENOR.length; loopins++) {
//      FWD3_USD_ATTR[loopins] = new GeneratorAttributeIR(FWD3_USD_TENOR[loopins]);
//    }
//  }
//
//  /** Standard USD Forward 3M curve instrument definitions */
//  private static final InstrumentDefinition<?>[] DEFINITIONS_FWD3_USD;
//
//  /** Units of curves */
//  private static final int[] NB_UNITS = new int[] {1 };
//  private static final int NB_BLOCKS = NB_UNITS.length;
//  private static final InstrumentDefinition<?>[][][][] DEFINITIONS_UNITS = new InstrumentDefinition<?>[NB_BLOCKS][][][];
//  private static final GeneratorYDCurve[][][] GENERATORS_UNITS = new GeneratorYDCurve[NB_BLOCKS][][];
//  private static final String[][][] NAMES_UNITS = new String[NB_BLOCKS][][];
//
//  private static final MulticurveBuildingDiscountingDiscountSimpleTest BUILDER = new MulticurveBuildingDiscountingDiscountSimpleTest();
//  private static final MulticurveProviderDiscount KNOWN_DATA = BUILDER.getCurvesWithOnlyDiscount();
//  private static final CurveBuildingBlockBundle KNOWN_BUNDLE = BUILDER.getBundleWithOnlyDiscount();
//  private static final LinkedHashMap<String, Currency> DSC_MAP = new LinkedHashMap<>();
//  private static final LinkedHashMap<String, IndexON[]> FWD_ON_MAP = new LinkedHashMap<>();
//  private static final LinkedHashMap<String, IborIndex[]> FWD_IBOR_MAP = new LinkedHashMap<>();
//
//  static {
//    getDefinitions(DSC_USD_MARKET_QUOTES, DSC_USD_GENERATORS, DSC_USD_ATTR);
//    DEFINITIONS_FWD3_USD = getDefinitions(FWD3_USD_MARKET_QUOTES, FWD3_USD_GENERATORS, FWD3_USD_ATTR);
//    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
//      DEFINITIONS_UNITS[loopblock] = new InstrumentDefinition<?>[NB_UNITS[loopblock]][][];
//      GENERATORS_UNITS[loopblock] = new GeneratorYDCurve[NB_UNITS[loopblock]][];
//      NAMES_UNITS[loopblock] = new String[NB_UNITS[loopblock]][];
//    }
//    DEFINITIONS_UNITS[0][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_USD };
//    final GeneratorYDCurve genIntLin = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR_LINEAR);
//    GENERATORS_UNITS[0][0] = new GeneratorYDCurve[] {genIntLin };
//    NAMES_UNITS[0][0] = new String[] {CURVE_NAME_FWD3_USD };
//    FWD_IBOR_MAP.put(CURVE_NAME_FWD3_USD, new IborIndex[] {USDLIBOR3M });
//  }
//
//  @SuppressWarnings({"rawtypes", "unchecked" })
//  private static InstrumentDefinition<?>[] getDefinitions(final double[] marketQuotes, final GeneratorInstrument[] generators, final GeneratorAttribute[] attribute) {
//    final InstrumentDefinition<?>[] definitions = new InstrumentDefinition<?>[marketQuotes.length];
//    for (int loopmv = 0; loopmv < marketQuotes.length; loopmv++) {
//      definitions[loopmv] = generators[loopmv].generateInstrument(NOW, marketQuotes[loopmv], NOTIONAL, attribute[loopmv]);
//    }
//    return definitions;
//  }
//
//  private static final List<Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle>> CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK = new ArrayList<>();
//
//  // Calculator
//  private static final PresentValueDiscountingCalculator PVC = PresentValueDiscountingCalculator.getInstance();
//  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
//  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PSMQCSC = ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();
//
//  private static final MulticurveDiscountBuildingRepository CURVE_BUILDING_REPOSITORY = new MulticurveDiscountBuildingRepository(TOLERANCE_ROOT, TOLERANCE_ROOT, STEP_MAX);
//
//  private static final double TOLERANCE_CAL = 1.0E-9;
//
//  @BeforeSuite
//  static void initClass() {
//    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
//      CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.add(makeCurvesFromDefinitions(DEFINITIONS_UNITS[loopblock], GENERATORS_UNITS[loopblock], NAMES_UNITS[loopblock], KNOWN_DATA, KNOWN_BUNDLE, PSMQC,
//          PSMQCSC, false));
//    }
//  }
//
//  private static List<Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle>> getCurvesWithBlock() {
//    return CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK;
//  }
//
//  private static MulticurveProviderDiscount getCurvesWithOnlyDiscount() {
//    final MulticurveProviderDiscount curves = CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(0).getFirst().copy();
//    final Map<IborIndex, YieldAndDiscountCurve> iborCurves = new LinkedHashMap<>();
//    final MulticurveProviderDiscount curve = new MulticurveProviderDiscount(curves.getDiscountingCurves(), iborCurves, curves.getForwardONCurves(), curves.getFxRates());
//    return curve;
//  }
//
//  private static CurveBuildingBlockBundle getBundleWithOnlyDiscount() {
//    final Map<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> bundle = CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(0).getSecond().getData();
//    final Map<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> bundleWithoutFwd3M = new LinkedHashMap<>();
//    final Set<String> keySet = bundle.keySet();
//    for (final String name : keySet) {
//      if (name.equals(CURVE_NAME_DSC_USD)) {
//        bundleWithoutFwd3M.put(name, bundle.get(name));
//      }
//    }
//    final LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> bundleToLinkedMap = new LinkedHashMap<>(bundleWithoutFwd3M);
//    return new CurveBuildingBlockBundle(bundleToLinkedMap);
//  }
//
//  @Test
//  public void curveConstructionGeneratorOtherBlocks() {
//    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
//      curveConstructionTest(DEFINITIONS_UNITS[loopblock], CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(loopblock).getFirst(), false, loopblock);
//    }
//  }
//
//  private void curveConstructionTest(final InstrumentDefinition<?>[][][] definitions, final MulticurveProviderDiscount curves, final boolean withToday, final int block) {
//    final int nbBlocks = definitions.length;
//    for (int loopblock = 0; loopblock < nbBlocks; loopblock++) {
//      final InstrumentDerivative[][] instruments = convert(definitions[loopblock], loopblock, withToday);
//      final double[][] pv = new double[instruments.length][];
//      for (int loopcurve = 0; loopcurve < instruments.length; loopcurve++) {
//        pv[loopcurve] = new double[instruments[loopcurve].length];
//        for (int loopins = 0; loopins < instruments[loopcurve].length; loopins++) {
//          pv[loopcurve][loopins] = curves.getFxRates().convert(instruments[loopcurve][loopins].accept(PVC, curves), USD).getAmount();
//          assertEquals("Curve construction: block " + block + ", unit " + loopblock + " - instrument " + loopins, 0, pv[loopcurve][loopins], TOLERANCE_CAL);
//        }
//      }
//    }
//  }
//
//  @Test(enabled = true)
//  public void blockBundle() {
//    final CurveBuildingBlockBundle blockBundleFromOneCurveTest = CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(0).getSecond();
//    final CurveBuildingBlockBundle blockBundleFromTwoCurveTest = BUILDER.getCurvesWithBlock().get(0).getSecond();
//    for (final String element : CURVE_NAMES) {
//      for (int j = 0; j < blockBundleFromOneCurveTest.getBlock(element).getSecond().getData().length; j++) {
//        for (int k = 0; k < blockBundleFromOneCurveTest.getBlock(element).getSecond().getData()[j].length; k++) {
//          assertEquals("Curve construction: block " + element + ", column " + j + " - line " + k, blockBundleFromOneCurveTest.getBlock(element).getSecond().getData()[j][k],
//              blockBundleFromTwoCurveTest.getBlock(element).getSecond().getData()[j][k], TOLERANCE_CAL);
//        }
//      }
//    }
//  }
//
//  /**
//   * Analyzes the shape of the forward curve.
//   */
//  @Test(enabled = false)
//  public void forwardAnalysis() {
//    final MulticurveProviderInterface marketDsc = CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(0).getFirst();
//    final int jump = 1;
//    final int startIndex = 0;
//    final int nbDate = 2750;
//    ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(NOW, USDLIBOR3M.getSpotLag() + startIndex * jump, NYC);
//    final double[] rateDsc = new double[nbDate];
//    final double[] startTime = new double[nbDate];
//    try {
//      final FileWriter writer = new FileWriter("fwd-dsc.csv");
//      for (int loopdate = 0; loopdate < nbDate; loopdate++) {
//        startTime[loopdate] = TimeCalculator.getTimeBetween(NOW, startDate);
//        final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, USDLIBOR3M, NYC);
//        final double endTime = TimeCalculator.getTimeBetween(NOW, endDate);
//        final double accrualFactor = USDLIBOR3M.getDayCount().getDayCountFraction(startDate, endDate, NYC);
//        rateDsc[loopdate] = marketDsc.getSimplyCompoundForwardRate(USDLIBOR3M, startTime[loopdate], endTime, accrualFactor);
//        startDate = ScheduleCalculator.getAdjustedDate(startDate, jump, NYC);
//        writer.append(0.0 + "," + startTime[loopdate] + "," + rateDsc[loopdate] + "\n");
//      }
//      writer.flush();
//      writer.close();
//    } catch (final IOException e) {
//      e.printStackTrace();
//    }
//  }
//
//  @SuppressWarnings("unchecked")
//  private static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDefinitions(final InstrumentDefinition<?>[][][] definitions, final GeneratorYDCurve[][] curveGenerators,
//      final String[][] curveNames, final MulticurveProviderDiscount knownData, final CurveBuildingBlockBundle knownBundle,
//      final InstrumentDerivativeVisitor<MulticurveProviderInterface, Double> calculator,
//      final InstrumentDerivativeVisitor<MulticurveProviderInterface, MulticurveSensitivity> sensitivityCalculator, final boolean withToday) {
//    final int nUnits = definitions.length;
//    final MultiCurveBundle<GeneratorYDCurve>[] curveBundles = new MultiCurveBundle[nUnits];
//    for (int i = 0; i < nUnits; i++) {
//      final int nCurves = definitions[i].length;
//      final SingleCurveBundle<GeneratorYDCurve>[] singleCurves = new SingleCurveBundle[nCurves];
//      for (int j = 0; j < nCurves; j++) {
//        final int nInstruments = definitions[i][j].length;
//        final InstrumentDerivative[] derivatives = new InstrumentDerivative[nInstruments];
//        final double[] initialGuess = new double[nInstruments];
//        for (int k = 0; k < nInstruments; k++) {
//          derivatives[k] = convert(definitions[i][j][k], i, withToday);
//          initialGuess[k] = initialGuess(definitions[i][j][k]);
//        }
//        final GeneratorYDCurve generator = curveGenerators[i][j].finalGenerator(derivatives);
//        singleCurves[j] = new SingleCurveBundle<>(curveNames[i][j], derivatives, initialGuess, generator);
//      }
//      curveBundles[i] = new MultiCurveBundle<>(singleCurves);
//    }
//    return CURVE_BUILDING_REPOSITORY.makeCurvesFromDerivatives(curveBundles, knownData, knownBundle, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP, calculator,
//        sensitivityCalculator);
//  }
//
//  private static InstrumentDerivative convert(final InstrumentDefinition<?> instrument, final int unit, final boolean withToday) {
//    InstrumentDerivative ird;
//    if (instrument instanceof SwapFixedONDefinition) {
//      ird = ((SwapFixedONDefinition) instrument).toDerivative(NOW, getTSSwapFixedON(withToday, unit));
//    } else {
//      if (instrument instanceof SwapFixedIborDefinition) {
//        ird = ((SwapFixedIborDefinition) instrument).toDerivative(NOW, getTSSwapFixedIbor(withToday, unit));
//      } else {
//        ird = instrument.toDerivative(NOW);
//      }
//    }
//    return ird;
//  }
//
//  private static InstrumentDerivative[][] convert(final InstrumentDefinition<?>[][] definitions, final int unit, final boolean withToday) {
//    final InstrumentDerivative[][] instruments = new InstrumentDerivative[definitions.length][];
//    for (int loopcurve = 0; loopcurve < definitions.length; loopcurve++) {
//      instruments[loopcurve] = new InstrumentDerivative[definitions[loopcurve].length];
//      int loopins = 0;
//      for (final InstrumentDefinition<?> instrument : definitions[loopcurve]) {
//        InstrumentDerivative ird;
//        if (instrument instanceof SwapFixedONDefinition) {
//          ird = ((SwapFixedONDefinition) instrument).toDerivative(NOW, getTSSwapFixedON(withToday, unit));
//        } else {
//          if (instrument instanceof SwapFixedIborDefinition) {
//            ird = ((SwapFixedIborDefinition) instrument).toDerivative(NOW, getTSSwapFixedIbor(withToday, unit));
//          } else {
//            ird = instrument.toDerivative(NOW);
//          }
//        }
//        instruments[loopcurve][loopins++] = ird;
//      }
//    }
//    return instruments;
//  }
//
//  private static ZonedDateTimeDoubleTimeSeries[] getTSSwapFixedON(final Boolean withToday, final Integer unit) {
//    switch (unit) {
//      case 0:
//        return withToday ? TS_FIXED_OIS_USD_WITH_TODAY : TS_FIXED_OIS_USD_WITHOUT_TODAY;
//      default:
//        throw new IllegalArgumentException(unit.toString());
//    }
//  }
//
//  private static ZonedDateTimeDoubleTimeSeries[] getTSSwapFixedIbor(final Boolean withToday, final Integer unit) {
//    switch (unit) {
//      case 0:
//        return withToday ? TS_FIXED_IBOR_USD3M_WITH_TODAY : TS_FIXED_IBOR_USD3M_WITHOUT_TODAY;
//      case 1:
//        return withToday ? TS_FIXED_IBOR_USD3M_WITH_TODAY : TS_FIXED_IBOR_USD3M_WITHOUT_TODAY;
//      default:
//        throw new IllegalArgumentException(unit.toString());
//    }
//  }
//
//  private static double initialGuess(final InstrumentDefinition<?> instrument) {
//    if (instrument instanceof SwapFixedONDefinition) {
//      return ((SwapFixedONDefinition) instrument).getFixedLeg().getNthPayment(0).getRate();
//    }
//    if (instrument instanceof SwapFixedIborDefinition) {
//      return ((SwapFixedIborDefinition) instrument).getFixedLeg().getNthPayment(0).getRate();
//    }
//    if (instrument instanceof ForwardRateAgreementDefinition) {
//      return ((ForwardRateAgreementDefinition) instrument).getRate();
//    }
//    if (instrument instanceof CashDefinition) {
//      return ((CashDefinition) instrument).getRate();
//    }
//    return 0.01;
//  }

}
