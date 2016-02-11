/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve.hullwhite;

import static org.testng.AssertJUnit.assertEquals;

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
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.ParSpreadMarketQuoteCurveSensitivityHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.ParSpreadMarketQuoteHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.PresentValueHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveTestUtils;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.SingleCurveBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Build of curve in several blocks with relevant Jacobian matrices.
 * Two curves in USD; 3M curve build with STIR futures and swap futures priced with Hull-White (HW parameters exogeneous).
 */
@Test(groups = TestGroup.UNIT)
public class UsdDiscounting3mLiborTest {

  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2013, 4, 26);

  private static final Interpolator1D INTERPOLATOR_LINEAR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final Currency USD = Currency.USD;
  private static final FXMatrix FX_MATRIX = new FXMatrix(USD);

  private static final double NOTIONAL = 1000000.0;

  private static final GeneratorSwapFixedON GENERATOR_OIS_USD = GeneratorSwapFixedONMaster.getInstance().getGenerator("USD1YFEDFUND", NYC);
  /** An overnight USD index */
  private static final IndexON USD_OVERNIGHT_INDEX = GENERATOR_OIS_USD.getIndex();
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_USD = new GeneratorDepositON("USD Deposit ON", USD, NYC, USD_OVERNIGHT_INDEX.getDayCount());
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_SWAP_MASTER.getGenerator("USD6MLIBOR3M", NYC);
  /** A 3M USD LIBOR index */
  private static final IborIndex USD_3M_LIBOR_INDEX = USD6MLIBOR3M.getIborIndex();
  private static final ZonedDateTime EDM3_START_PERIOD = DateUtils.getUTCDate(2013, 6, 19);
  private static final InterestRateFutureSecurityDefinition EDM3_DEFINITION = InterestRateFutureSecurityDefinition
      .fromFixingPeriodStartDate(EDM3_START_PERIOD, USD_3M_LIBOR_INDEX, NOTIONAL, 0.25, "EDM3", NYC);
  private static final ZonedDateTime EDU3_START_PERIOD = DateUtils.getUTCDate(2013, 9, 18);
  private static final InterestRateFutureSecurityDefinition EDU3_DEFINITION = InterestRateFutureSecurityDefinition
      .fromFixingPeriodStartDate(EDU3_START_PERIOD, USD_3M_LIBOR_INDEX, NOTIONAL, 0.25, "EDU3", NYC);
  private static final ZonedDateTime EDZ3_START_PERIOD = DateUtils.getUTCDate(2013, 12, 18);
  private static final InterestRateFutureSecurityDefinition EDZ3_DEFINITION = InterestRateFutureSecurityDefinition
      .fromFixingPeriodStartDate(EDZ3_START_PERIOD, USD_3M_LIBOR_INDEX, NOTIONAL, 0.25, "EDZ3", NYC);
  private static final ZonedDateTime EDH4_START_PERIOD = DateUtils.getUTCDate(2014, 3, 19);
  private static final InterestRateFutureSecurityDefinition EDH4_DEFINITION = InterestRateFutureSecurityDefinition
      .fromFixingPeriodStartDate(EDH4_START_PERIOD, USD_3M_LIBOR_INDEX, NOTIONAL, 0.25, "EDH4", NYC);
  private static final Period CTPM3_TENOR = Period.ofYears(2);
  private static final double CTPM3_RATE = 0.0050;
  private static final SwapFuturesPriceDeliverableSecurityDefinition CTPM3_DEFINITION = SwapFuturesPriceDeliverableSecurityDefinition.from(EDM3_START_PERIOD, USD6MLIBOR3M, CTPM3_TENOR, NOTIONAL,
      CTPM3_RATE);
  private static final Period CFPM3_TENOR = Period.ofYears(5);
  private static final double CFPM3_RATE = 0.0100;
  private static final SwapFuturesPriceDeliverableSecurityDefinition CFPM3_DEFINITION = SwapFuturesPriceDeliverableSecurityDefinition.from(EDM3_START_PERIOD, USD6MLIBOR3M, CFPM3_TENOR, NOTIONAL,
      CFPM3_RATE);
  private static final Period CNPM3_TENOR = Period.ofYears(10);
  private static final double CNPM3_RATE = 0.0200;
  private static final SwapFuturesPriceDeliverableSecurityDefinition CNPM3_DEFINITION = SwapFuturesPriceDeliverableSecurityDefinition.from(EDM3_START_PERIOD, USD6MLIBOR3M, CNPM3_TENOR, NOTIONAL,
      CNPM3_RATE);
  private static final Period CBPM3_TENOR = Period.ofYears(30);
  private static final double CBPM3_RATE = 0.0275;
  private static final SwapFuturesPriceDeliverableSecurityDefinition CBPM3_DEFINITION = SwapFuturesPriceDeliverableSecurityDefinition.from(EDM3_START_PERIOD, USD6MLIBOR3M, CBPM3_TENOR, NOTIONAL,
      CBPM3_RATE);
  private static final GeneratorInterestRateFutures GENERATOR_EDM3 = new GeneratorInterestRateFutures("EDM3", EDM3_DEFINITION);
  private static final GeneratorInterestRateFutures GENERATOR_EDU3 = new GeneratorInterestRateFutures("EDU3", EDU3_DEFINITION);
  private static final GeneratorInterestRateFutures GENERATOR_EDZ3 = new GeneratorInterestRateFutures("EDZ3", EDZ3_DEFINITION);
  private static final GeneratorInterestRateFutures GENERATOR_EDH4 = new GeneratorInterestRateFutures("EDH4", EDH4_DEFINITION);
  private static final GeneratorSwapFuturesDeliverable GENERATOR_CTPM3 = new GeneratorSwapFuturesDeliverable("CTPM3", CTPM3_DEFINITION);
  private static final GeneratorSwapFuturesDeliverable GENERATOR_CFPM3 = new GeneratorSwapFuturesDeliverable("CFPM3", CFPM3_DEFINITION);
  private static final GeneratorSwapFuturesDeliverable GENERATOR_CNPM3 = new GeneratorSwapFuturesDeliverable("CNPM3", CNPM3_DEFINITION);
  private static final GeneratorSwapFuturesDeliverable GENERATOR_CBPM3 = new GeneratorSwapFuturesDeliverable("CBPM3", CBPM3_DEFINITION);
  private static final GeneratorDepositIbor GENERATOR_USDLIBOR3M = new GeneratorDepositIbor("GENERATOR_USDLIBOR3M", USD_3M_LIBOR_INDEX, NYC);

  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2013, 4, 25),
    DateUtils.getUTCDate(2013, 4, 26) }, new double[] {0.0007, 0.0008 });
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2013, 4, 25) },
      new double[] {0.0007 });

  /** Fixing time series created before the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITHOUT_TODAY = new HashMap<>();
  /** Fixing time series created after the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITH_TODAY = new HashMap<>();
  static {
    FIXING_TS_WITHOUT_TODAY.put(USD_OVERNIGHT_INDEX, TS_ON_USD_WITHOUT_TODAY);
    FIXING_TS_WITH_TODAY.put(USD_OVERNIGHT_INDEX, TS_ON_USD_WITH_TODAY);
  }

  private static final String CURVE_NAME_DSC_USD = "USD OIS";
  private static final String CURVE_NAME_FWD3_USD = "USD Fwd 3M";

  /** Market values for the dsc USD curve */
  private static final double[] DSC_USD_MARKET_QUOTES = new double[] {0.0022, 0.00127, 0.00125, 0.00126, 0.00126, 0.00125, 0.001315, 0.001615, 0.00243, 0.00393, 0.00594, 0.01586 };
  /** Generators for the dsc USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_USD_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_DEPOSIT_ON_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD,
    GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD };
  /** Tenors for the dsc USD curve */
  private static final Period[] DSC_USD_TENOR = new Period[] {Period.ofDays(0), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1),
    Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
  private static final GeneratorAttributeIR[] DSC_USD_ATTR = new GeneratorAttributeIR[DSC_USD_TENOR.length];
  static {
    for (int i = 0; i < 1; i++) {
      DSC_USD_ATTR[i] = new GeneratorAttributeIR(DSC_USD_TENOR[i], Period.ZERO);
    }
    for (int i = 1; i < DSC_USD_TENOR.length; i++) {
      DSC_USD_ATTR[i] = new GeneratorAttributeIR(DSC_USD_TENOR[i]);
    }
  }
  private static final double[] FWD3_USD_MARKET_QUOTES = new double[] {0.0027560, 0.99715, 0.99700, 0.99680, 0.99660, (100 + 7.0 / 32.0 + 3.0 / (32.0 * 4.0)) / 100.0, (100 + 17.0 / 32.0) / 100.0,
    (101 + 2.0 / 32.0) / 100.0, (98 + 21.0 / 32.0) / 100.0 };
  // Quoted in 32nd (by 1/4): 100-07 3/4, 100-17 +, 101-02, 98-21 };
  /** Generators for the Fwd 3M USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD3_EUR_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_USDLIBOR3M, GENERATOR_EDM3, GENERATOR_EDU3, GENERATOR_EDZ3,
    GENERATOR_EDH4, GENERATOR_CTPM3, GENERATOR_CFPM3, GENERATOR_CNPM3, GENERATOR_CBPM3 };
  /** Tenors for the Fwd 3M USD curve */
  private static final Period[] FWD3_USD_TENOR = new Period[] {Period.ofMonths(0), Period.ofMonths(0), Period.ofMonths(0), Period.ofMonths(0), Period.ofMonths(0), Period.ofMonths(0),
    Period.ofMonths(0), Period.ofMonths(0), Period.ofMonths(0) };
  private static final GeneratorAttribute[] FWD3_USD_ATTR = new GeneratorAttribute[FWD3_USD_TENOR.length];
  static {
    FWD3_USD_ATTR[0] = new GeneratorAttributeIR(FWD3_USD_TENOR[0], FWD3_USD_TENOR[0]);
    for (int i = 1; i < FWD3_USD_TENOR.length; i++) {
      FWD3_USD_ATTR[i] = new GeneratorAttribute();
    }
  }

  /** Standard USD discounting curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_DSC_USD;
  /** Standard USD Forward 3M curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_FWD3_USD;

  /** Units of curves */
  private static final int[] NB_UNITS = new int[] {2, 1 };
  private static final int NB_BLOCKS = NB_UNITS.length;
  private static final InstrumentDefinition<?>[][][][] DEFINITIONS_UNITS = new InstrumentDefinition<?>[NB_BLOCKS][][][];
  private static final GeneratorYDCurve[][][] GENERATORS_UNITS = new GeneratorYDCurve[NB_BLOCKS][][];
  private static final String[][][] NAMES_UNITS = new String[NB_BLOCKS][][];
  // Hull-White (for futures)
  private static final double MEAN_REVERSION = 0.01;
  private static final double[] VOLATILITY = new double[] {0.01, 0.011, 0.012, 0.013, 0.014 };
  private static final double[] VOLATILITY_TIME = new double[] {0.5, 1.0, 2.0, 5.0 };
  private static final HullWhiteOneFactorPiecewiseConstantParameters MODEL_PARAMETERS = new HullWhiteOneFactorPiecewiseConstantParameters(MEAN_REVERSION, VOLATILITY, VOLATILITY_TIME);

  private static final MulticurveProviderDiscount MULTICURVE_KNOWN_DATA = new MulticurveProviderDiscount(FX_MATRIX);
  private static final HullWhiteOneFactorProviderDiscount HW_KNOWN_DATA = new HullWhiteOneFactorProviderDiscount(MULTICURVE_KNOWN_DATA, MODEL_PARAMETERS, USD);

  private static final LinkedHashMap<String, Currency> DSC_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IndexON[]> FWD_ON_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IborIndex[]> FWD_IBOR_MAP = new LinkedHashMap<>();

  static {
    DEFINITIONS_DSC_USD = getDefinitions(DSC_USD_MARKET_QUOTES, DSC_USD_GENERATORS, DSC_USD_ATTR);
    DEFINITIONS_FWD3_USD = getDefinitions(FWD3_USD_MARKET_QUOTES, FWD3_EUR_GENERATORS, FWD3_USD_ATTR);
    for (int i = 0; i < NB_BLOCKS; i++) {
      DEFINITIONS_UNITS[i] = new InstrumentDefinition<?>[NB_UNITS[i]][][];
      GENERATORS_UNITS[i] = new GeneratorYDCurve[NB_UNITS[i]][];
      NAMES_UNITS[i] = new String[NB_UNITS[i]][];
    }
    DEFINITIONS_UNITS[0][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD };
    DEFINITIONS_UNITS[0][1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_USD };
    DEFINITIONS_UNITS[1][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD, DEFINITIONS_FWD3_USD };
    final GeneratorYDCurve genIntLin = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR_LINEAR);
    GENERATORS_UNITS[0][0] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[0][1] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[1][0] = new GeneratorYDCurve[] {genIntLin, genIntLin };
    NAMES_UNITS[0][0] = new String[] {CURVE_NAME_DSC_USD };
    NAMES_UNITS[0][1] = new String[] {CURVE_NAME_FWD3_USD };
    NAMES_UNITS[1][0] = new String[] {CURVE_NAME_DSC_USD, CURVE_NAME_FWD3_USD };
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

  private static final List<Pair<HullWhiteOneFactorProviderDiscount, CurveBuildingBlockBundle>> CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK = new ArrayList<>();

  // Calculator
  private static final PresentValueHullWhiteCalculator PVHWC = PresentValueHullWhiteCalculator.getInstance();
  private static final ParSpreadMarketQuoteHullWhiteCalculator PSMQHWC = ParSpreadMarketQuoteHullWhiteCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityHullWhiteCalculator PSMQCSHWC = ParSpreadMarketQuoteCurveSensitivityHullWhiteCalculator.getInstance();

  private static final double TOLERANCE_ROOT = 1.0E-10;
  private static final int STEP_MAX = 100;
  private static final HullWhiteProviderDiscountBuildingRepository CURVE_BUILDING_REPOSITORY = new HullWhiteProviderDiscountBuildingRepository(TOLERANCE_ROOT, TOLERANCE_ROOT, STEP_MAX);

  private static final double TOLERANCE_CAL = 1.0E-10 * NOTIONAL; // 0.01 currency unit for 100m

  @BeforeSuite
  static void initClass() {
    for (int i = 0; i < NB_BLOCKS; i++) {
      CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.add(makeCurvesFromDefinitions(DEFINITIONS_UNITS[i], GENERATORS_UNITS[i], NAMES_UNITS[i], HW_KNOWN_DATA, PSMQHWC, PSMQCSHWC,
          false));
    }
  }

  @Test
  public void curveConstruction() {
    for (int i = 0; i < NB_BLOCKS; i++) {
      curveConstructionTest(DEFINITIONS_UNITS[i], CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(i).getFirst(), false, i);
    }
  }

  @Test(enabled = false)
  public void performance() {
    long startTime, endTime;
    final int nbTest = 100;

    startTime = System.currentTimeMillis();
    for (int i = 0; i < nbTest; i++) {
      makeCurvesFromDefinitions(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0], HW_KNOWN_DATA, PSMQHWC, PSMQCSHWC, false);
    }
    endTime = System.currentTimeMillis();
    System.out.println("MulticurveBuildingHullWhiteDiscountFuturesUSDTest:" + nbTest + " curve construction / 2 units: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 2 units: 06-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 810 ms for 100 sets.

    startTime = System.currentTimeMillis();
    for (int i = 0; i < nbTest; i++) {
      makeCurvesFromDefinitions(DEFINITIONS_UNITS[1], GENERATORS_UNITS[1], NAMES_UNITS[1], HW_KNOWN_DATA, PSMQHWC, PSMQCSHWC, false);
    }
    endTime = System.currentTimeMillis();
    System.out.println("MulticurveBuildingHullWhiteDiscountFuturesUSDTest:" + nbTest + " curve construction / 1 unit: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 1 unit: 06-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 920 ms for 100 sets.

  }

  private void curveConstructionTest(final InstrumentDefinition<?>[][][] definitions, final HullWhiteOneFactorProviderDiscount curves, final boolean withToday, final int block) {
    final int nbBlocks = definitions.length;
    for (int i = 0; i < nbBlocks; i++) {
      final InstrumentDerivative[][] instruments = CurveTestUtils.convert(definitions[i], withToday ? FIXING_TS_WITH_TODAY : FIXING_TS_WITHOUT_TODAY, NOW);
      final double[][] pv = new double[instruments.length][];
      for (int j = 0; j < instruments.length; j++) {
        pv[j] = new double[instruments[j].length];
        for (int k = 0; k < instruments[j].length; k++) {
          pv[j][k] = curves.getMulticurveProvider().getFxRates().convert(instruments[j][k].accept(PVHWC, curves), USD).getAmount();
          assertEquals("Curve construction: block " + block + ", unit " + i + " - instrument " + k, 0, pv[j][k], TOLERANCE_CAL);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static Pair<HullWhiteOneFactorProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDefinitions(final InstrumentDefinition<?>[][][] definitions,
      final GeneratorYDCurve[][] curveGenerators, final String[][] curveNames, final HullWhiteOneFactorProviderDiscount knownData,
      final InstrumentDerivativeVisitor<HullWhiteOneFactorProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<HullWhiteOneFactorProviderInterface, MulticurveSensitivity> sensitivityCalculator, final boolean withToday) {
    final int nUnits = definitions.length;
    final MultiCurveBundle<GeneratorYDCurve>[] curveBundles = new MultiCurveBundle[nUnits];
    for (int i = 0; i < nUnits; i++) {
      final int nCurves = definitions[i].length;
      final SingleCurveBundle<GeneratorYDCurve>[] singleCurves = new SingleCurveBundle[nCurves];
      for (int j = 0; j < nCurves; j++) {
        final int nInstruments = definitions[i][j].length;
        final InstrumentDerivative[] derivatives = new InstrumentDerivative[nInstruments];
        final double[] rates = new double[nInstruments];
        for (int k = 0; k < nInstruments; k++) {
          derivatives[k] = CurveTestUtils.convert(definitions[i][j][k], withToday ? FIXING_TS_WITH_TODAY : FIXING_TS_WITHOUT_TODAY, NOW);
          rates[k] = definitions[i][j][k].accept(CurveTestUtils.RATES_INITIALIZATION);
        }
        final GeneratorYDCurve generator = curveGenerators[i][j].finalGenerator(derivatives);
        final double[] initialGuess = generator.initialGuess(rates);
        singleCurves[j] = new SingleCurveBundle<>(curveNames[i][j], derivatives, initialGuess, generator);
      }
      curveBundles[i] = new MultiCurveBundle<>(singleCurves);
    }
    return CURVE_BUILDING_REPOSITORY.makeCurvesFromDerivatives(curveBundles, knownData, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP, calculator, sensitivityCalculator);
  }

}
