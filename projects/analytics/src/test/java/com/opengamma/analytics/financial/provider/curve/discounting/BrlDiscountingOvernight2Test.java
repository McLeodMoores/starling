/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve.discounting;

import static org.testng.AssertJUnit.assertEquals;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositON;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedCompoundedONCompoundedMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.Index;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveTestUtils;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.SingleCurveBundle;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BrlDiscountingOvernight2Test {

  private static final Interpolator1D INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LOG_NATURAL_CUBIC_MONOTONE, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();
  private static final double TOLERANCE_ROOT = 1.0E-10;
  private static final int STEP_MAX = 100;

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");

  private static final Currency BRL = Currency.BRL;
  private static final FXMatrix FX_MATRIX = new FXMatrix(BRL);

  private static final double NOTIONAL = 1.0;

  private static final GeneratorSwapFixedCompoundedONCompounded GENERATOR_OIS_BRL = GeneratorSwapFixedCompoundedONCompoundedMaster.getInstance().getGenerator("BRLCDI", NYC);
  /** A CDI index */
  private static final IndexON CDI_INDEX = GENERATOR_OIS_BRL.getIndex();
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_BRL = new GeneratorDepositON("BRL Deposit ON", BRL, NYC, CDI_INDEX.getDayCount());

  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2013, 10, 7);

  private static final ZonedDateTimeDoubleTimeSeries TS_ON_BRL_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2013, 8, 10),
    DateUtils.getUTCDate(2013, 9, 10) }, new double[] {0.0881, 0.0881 });
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_BRL_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2013, 8, 10),
    DateUtils.getUTCDate(2013, 9, 10) }, new double[] {0.0881, 0.0881 });

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
  private static final double[] DSC_BRL_MARKET_QUOTES = new double[] {0.092925, 0.09325, 0.09458, 0.09545, 0.09665, 0.09845, 0.1001, 0.10101, 0.10335, 0.10565, 0.10725, 0.10865, 0.1098, 0.11085,
    0.1113, 0.11165, 0.11205, 0.1127 };
  /** Generators for the dsc BRL curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_BRL_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_OIS_BRL, GENERATOR_OIS_BRL,
    GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL,
    GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL, GENERATOR_OIS_BRL };
  /** Tenors for the dsc BRL curve */
  private static final Period[] DSC_BRL_TENOR = new Period[] {Period.ofDays(23), Period.ofDays(54), Period.ofDays(85),
    Period.ofDays(117), Period.ofDays(174), Period.ofDays(267), Period.ofDays(357), Period.ofDays(450), Period.ofDays(539), Period.ofDays(630), Period.ofDays(722), Period.ofDays(817),
    Period.ofDays(996), Period.ofDays(1090), Period.ofDays(1181), Period.ofDays(1272), Period.ofDays(1363), Period.ofDays(1454) };
  private static final GeneratorAttributeIR[] DSC_BRL_ATTR = new GeneratorAttributeIR[DSC_BRL_TENOR.length];
  static {
    for (int i = 0; i < DSC_BRL_TENOR.length; i++) {
      DSC_BRL_ATTR[i] = new GeneratorAttributeIR(DSC_BRL_TENOR[i]);
    }
  }

  /** Standard BRL discounting curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_DSC_BRL;

  /** Units of curves */
  private static final int[] NB_UNITS = new int[] {1 };
  private static final int NB_BLOCKS = NB_UNITS.length;
  private static final InstrumentDefinition<?>[][][][] DEFINITIONS_UNITS = new InstrumentDefinition<?>[NB_BLOCKS][][][];
  private static final GeneratorYDCurve[][][] GENERATORS_UNITS = new GeneratorYDCurve[NB_BLOCKS][][];
  private static final String[][][] NAMES_UNITS = new String[NB_BLOCKS][][];
  private static final MulticurveProviderDiscount KNOWN_DATA = new MulticurveProviderDiscount(FX_MATRIX);
  private static final LinkedHashMap<String, Currency> DSC_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IndexON[]> FWD_ON_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IborIndex[]> FWD_IBOR_MAP = new LinkedHashMap<>();

  static {
    DEFINITIONS_DSC_BRL = getDefinitions(DSC_BRL_MARKET_QUOTES, DSC_BRL_GENERATORS, DSC_BRL_ATTR);
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      DEFINITIONS_UNITS[loopblock] = new InstrumentDefinition<?>[NB_UNITS[loopblock]][][];
      GENERATORS_UNITS[loopblock] = new GeneratorYDCurve[NB_UNITS[loopblock]][];
      NAMES_UNITS[loopblock] = new String[NB_UNITS[loopblock]][];
    }
    DEFINITIONS_UNITS[0][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_BRL };
    final GeneratorYDCurve genIntLin = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR);
    GENERATORS_UNITS[0][0] = new GeneratorYDCurve[] {genIntLin };
    NAMES_UNITS[0][0] = new String[] {CURVE_NAME_DSC_BRL };
    DSC_MAP.put(CURVE_NAME_DSC_BRL, BRL);
    FWD_ON_MAP.put(CURVE_NAME_DSC_BRL, new IndexON[] {CDI_INDEX });
  }

  @SuppressWarnings({"rawtypes", "unchecked" })
  private static InstrumentDefinition<?>[] getDefinitions(final double[] marketQuotes, final GeneratorInstrument[] generators, final GeneratorAttribute[] attribute) {
    final InstrumentDefinition<?>[] definitions = new InstrumentDefinition<?>[marketQuotes.length];
    for (int i = 0; i < marketQuotes.length; i++) {
      definitions[i] = generators[i].generateInstrument(NOW, marketQuotes[i], NOTIONAL, attribute[i]);
    }
    return definitions;
  }

  private static final List<Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle>> CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK = new ArrayList<>();

  // Calculator
  private static final PresentValueDiscountingCalculator PVC = PresentValueDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PSMQCSC = ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();

  private static final MulticurveDiscountBuildingRepository CURVE_BUILDING_REPOSITORY = new MulticurveDiscountBuildingRepository(TOLERANCE_ROOT, TOLERANCE_ROOT, STEP_MAX);

  private static final double TOLERANCE_CAL = 1.0E-9;

  @BeforeSuite
  static void initClass() {
    for (int i = 0; i < NB_BLOCKS; i++) {
      CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.add(makeCurvesFromDefinitions(DEFINITIONS_UNITS[i], GENERATORS_UNITS[i], NAMES_UNITS[i], KNOWN_DATA, PSMQC, PSMQCSC, false));
    }
  }

  @Test(enabled = false)
  public void performance() {
    long startTime, endTime;
    final int nbTest = 100;

    startTime = System.currentTimeMillis();
    for (int i = 0; i < nbTest; i++) {
      makeCurvesFromDefinitions(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQC, PSMQCSC, false);
    }
    endTime = System.currentTimeMillis();
    System.out.println("MulticurveBuidingDiscountingBrazilianONTest - " + nbTest + " curve construction Brazilian CDI EUR 1 units: " + (endTime - startTime) + " ms");
    // Performance note: curve construction Price index EUR 1 units: 23-Aug-13: On Dell Precision T1850 3.5 GHz Quad-Core Intel Xeon: 3094 ms for 100 sets.
  }

  @Test
  public void curveConstructionGeneratorOtherBlocks() {
    for (int i = 0; i < NB_BLOCKS; i++) {
      curveConstructionTest(DEFINITIONS_UNITS[i], CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(i).getFirst(), false, i);
    }
  }

  private void curveConstructionTest(final InstrumentDefinition<?>[][][] definitions, final MulticurveProviderDiscount curves, final boolean withToday, final int block) {
    final int nbBlocks = definitions.length;
    for (int i = 0; i < nbBlocks; i++) {
      final InstrumentDerivative[][] instruments = CurveTestUtils.convert(definitions[i], withToday ? FIXING_TS_WITH_TODAY : FIXING_TS_WITHOUT_TODAY, NOW);
      final double[][] pv = new double[instruments.length][];
      for (int j = 0; j < instruments.length; j++) {
        pv[j] = new double[instruments[j].length];
        for (int k = 0; k < instruments[j].length; k++) {
          pv[j][k] = curves.getFxRates().convert(instruments[j][k].accept(PVC, curves), BRL).getAmount();
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
    final MulticurveProviderInterface marketDsc = CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(0).getFirst();
    final int jump = 1;
    final int startIndex = 0;
    final int nbDate = 100;
    ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(NOW, CDI_INDEX.getPublicationLag() + startIndex * jump, NYC);
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
        startTime2[i] = CDI_INDEX.getDayCount().getDayCountFraction(NOW, startDate, NYC);
        final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, CDI_INDEX.getPublicationLag(), NYC);
        final double endTime = TimeCalculator.getTimeBetween(NOW, endDate);
        final double endTime2 = CDI_INDEX.getDayCount().getDayCountFraction(NOW, endDate, NYC);
        accrualFactor[i] = CDI_INDEX.getDayCount().getDayCountFraction(startDate, endDate, NYC);
        accrualFactorActAct[i] = TimeCalculator.getTimeBetween(startDate, endDate);
        dscstart[i] = marketDsc.getDiscountFactor(BRL, startTime2[i]);
        dscend[i] = marketDsc.getDiscountFactor(BRL, endTime2);
        rateDsc[i] = marketDsc.getSimplyCompoundForwardRate(CDI_INDEX, startTime2[i], endTime2, accrualFactor[i]);
        rateDsc2[i] = marketDsc.getSimplyCompoundForwardRate(CDI_INDEX, startTime[i], endTime, accrualFactor[i]);
        rateDscNormal[i] = marketDsc.getSimplyCompoundForwardRate(CDI_INDEX, startTime[i], endTime, accrualFactorActAct[i]);
        startDate = ScheduleCalculator.getAdjustedDate(startDate, jump, NYC);
        writer.append(0.0 + "," + startTime[i] + "," + dscstart[i] + "," + dscend[i] + "," + rateDsc[i] + "," + rateDsc2[i] + "," + rateDscNormal[i] + "\n");
      }
      writer.flush();
      writer.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  @SuppressWarnings("unchecked")
  private static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDefinitions(final InstrumentDefinition<?>[][][] definitions, final GeneratorYDCurve[][] curveGenerators,
      final String[][] curveNames, final MulticurveProviderDiscount knownData, final InstrumentDerivativeVisitor<MulticurveProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<MulticurveProviderInterface, MulticurveSensitivity> sensitivityCalculator, final boolean withToday) {
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
          derivatives[k] = CurveTestUtils.convert(definitions[i][j][k], withToday ? FIXING_TS_WITH_TODAY : FIXING_TS_WITHOUT_TODAY, NOW);
          initialGuess[k] = 0.01;
        }
        final GeneratorYDCurve generator = curveGenerators[i][j].finalGenerator(derivatives);
        singleCurves[j] = new SingleCurveBundle<>(curveNames[i][j], derivatives, initialGuess, generator);
      }
      curveBundles[i] = new MultiCurveBundle<>(singleCurves);
    }
    return CURVE_BUILDING_REPOSITORY.makeCurvesFromDerivatives(curveBundles, knownData, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP, calculator, sensitivityCalculator);
  }

}
