/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.provider.curve.bond;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.LinkedListMultimap;
import com.opengamma.analytics.date.CalendarAdapter;
import com.opengamma.analytics.date.WeekendWorkingDayCalendar;
import com.opengamma.analytics.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorBill;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositON;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositONCounterpart;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.Index;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityShortName;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.ParSpreadMarketQuoteIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveTestUtils;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.SingleCurveBundle;
import com.opengamma.analytics.financial.provider.curve.issuer.IssuerDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class UsdDiscountingGovernment1Test {

  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2012, 8, 22);

  private static final Interpolator1D INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();
  private static final double TOLERANCE_ROOT = 1.0E-10;
  private static final int STEP_MAX = 100;
  private static final WorkingDayCalendar NYC = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;
  private static final Calendar NYC_OLD = new CalendarAdapter(NYC);
  private static final Currency USD = Currency.USD;
  private static final FXMatrix FX_MATRIX = new FXMatrix(USD);

  private static final double NOTIONAL = 1.0;

  private static final GeneratorSwapFixedON GENERATOR_OIS_USD = GeneratorSwapFixedONMaster.getInstance().getGenerator("USD1YFEDFUND", NYC_OLD);
  /** A Fed funds index */
  private static final IndexON FED_FUNDS_INDEX = GENERATOR_OIS_USD.getIndex();
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_USD = new GeneratorDepositON("USD Deposit ON", USD, NYC_OLD, FED_FUNDS_INDEX.getDayCount());
  private static final String NAME_COUNTERPART = "US GOVT";
  private static final DayCount DAY_COUNT_ON = DayCounts.ACT_360;
  private static final GeneratorDepositONCounterpart GENERATOR_DEPOSIT_ON_USGOVT = new GeneratorDepositONCounterpart("US GOVT Deposit ON", USD, NYC_OLD, DAY_COUNT_ON, NAME_COUNTERPART);

  private static final YieldConvention YIELD_BILL_USGOVT = YieldConventionFactory.INSTANCE.getYieldConvention("INTEREST@MTY");
  private static final DayCount DAY_COUNT_BILL_USGOVT = DayCounts.ACT_360;
  private static final int SPOT_LAG_BILL = 1;
  private static final ZonedDateTime[] BILL_MATURITY = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 9, 28), DateUtils.getUTCDate(2012, 11, 30), DateUtils.getUTCDate(2013, 2, 28) };
  private static final int NB_BILL = BILL_MATURITY.length;
  private static final BillSecurityDefinition[] BILL_SECURITY = new BillSecurityDefinition[NB_BILL];
  private static final GeneratorBill[] GENERATOR_BILL = new GeneratorBill[NB_BILL];
  static {
    for (int i = 0; i < BILL_MATURITY.length; i++) {
      BILL_SECURITY[i] = new BillSecurityDefinition(USD, BILL_MATURITY[i], NOTIONAL, SPOT_LAG_BILL, NYC, YIELD_BILL_USGOVT, DAY_COUNT_BILL_USGOVT, NAME_COUNTERPART);
      GENERATOR_BILL[i] = new GeneratorBill("GeneratorBill" + i, BILL_SECURITY[i]);
    }
  }

  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.07, 0.08 });

  /** Fixing time series created before the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITHOUT_TODAY = new HashMap<>();
  /** Fixing time series created after the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITH_TODAY = new HashMap<>();
  static {
    FIXING_TS_WITHOUT_TODAY.put(FED_FUNDS_INDEX, TS_ON_USD_WITHOUT_TODAY);
    FIXING_TS_WITH_TODAY.put(FED_FUNDS_INDEX, TS_ON_USD_WITH_TODAY);
  }

  private static final String CURVE_NAME_DSC_USD = "USD Dsc";
  private static final String CURVE_NAME_GOVTUS_USD = "USD GOVT US";

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

  /** Market values for the govt USD curve */
  private static final double[] GOVTUS_USD_MARKET_QUOTES = new double[] {0.0010, 0.0015, 0.0020, 0.0015 };
  /** Generators for the govt USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] GOVTUS_USD_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_DEPOSIT_ON_USGOVT, GENERATOR_BILL[0], GENERATOR_BILL[1],
    GENERATOR_BILL[2] };
  /** Tenors for the govt USD curve */
  private static final Period[] GOVTUS_USD_TENOR = new Period[] {Period.ofDays(0), Period.ofDays(0), Period.ofDays(0), Period.ofDays(0) };
  private static final GeneratorAttributeIR[] GOVTUS_USD_ATTR = new GeneratorAttributeIR[GOVTUS_USD_TENOR.length];
  static {
    for (int i = 0; i < GOVTUS_USD_TENOR.length; i++) {
      GOVTUS_USD_ATTR[i] = new GeneratorAttributeIR(GOVTUS_USD_TENOR[i]);
    }
  }

  /** Standard USD discounting curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_DSC_USD;
  /** Standard USD Forward 3M curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_GOVTUS_USD;

  /** Units of curves */
  /** Units of curves */
  private static final int[] NB_UNITS = new int[] {2 };
  private static final int NB_BLOCKS = NB_UNITS.length;
  private static final InstrumentDefinition<?>[][][][] DEFINITIONS_UNITS = new InstrumentDefinition<?>[NB_BLOCKS][][][];
  private static final GeneratorYDCurve[][][] GENERATORS_UNITS = new GeneratorYDCurve[NB_BLOCKS][][];
  private static final String[][][] NAMES_UNITS = new String[NB_BLOCKS][][];
  private static final MulticurveProviderDiscount KNOWN_MULTICURVES = new MulticurveProviderDiscount(FX_MATRIX);
  private static final IssuerProviderDiscount KNOWN_DATA = new IssuerProviderDiscount(KNOWN_MULTICURVES, new HashMap<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve>());
  private static final LinkedHashMap<String, Currency> DSC_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IndexON[]> FWD_ON_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IborIndex[]> FWD_IBOR_MAP = new LinkedHashMap<>();
  private static final LinkedListMultimap<String, Pair<Object, LegalEntityFilter<LegalEntity>>> DSC_ISS_MAP = LinkedListMultimap.create();

  static {
    DEFINITIONS_DSC_USD = getDefinitions(DSC_USD_MARKET_QUOTES, DSC_USD_GENERATORS, DSC_USD_ATTR);
    DEFINITIONS_GOVTUS_USD = getDefinitions(GOVTUS_USD_MARKET_QUOTES, GOVTUS_USD_GENERATORS, GOVTUS_USD_ATTR);
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      DEFINITIONS_UNITS[loopblock] = new InstrumentDefinition<?>[NB_UNITS[loopblock]][][];
      GENERATORS_UNITS[loopblock] = new GeneratorYDCurve[NB_UNITS[loopblock]][];
      NAMES_UNITS[loopblock] = new String[NB_UNITS[loopblock]][];
    }
    DEFINITIONS_UNITS[0][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD };
    DEFINITIONS_UNITS[0][1] = new InstrumentDefinition<?>[][] {DEFINITIONS_GOVTUS_USD };
    final GeneratorYDCurve genIntLin = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR);
    GENERATORS_UNITS[0][0] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[0][1] = new GeneratorYDCurve[] {genIntLin };
    NAMES_UNITS[0][0] = new String[] {CURVE_NAME_DSC_USD };
    NAMES_UNITS[0][1] = new String[] {CURVE_NAME_GOVTUS_USD };
    DSC_MAP.put(CURVE_NAME_DSC_USD, USD);
    FWD_ON_MAP.put(CURVE_NAME_DSC_USD, new IndexON[] {FED_FUNDS_INDEX });
    DSC_ISS_MAP.put(CURVE_NAME_GOVTUS_USD, Pairs.of((Object) NAME_COUNTERPART, (LegalEntityFilter<LegalEntity>) new LegalEntityShortName()));
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  private static InstrumentDefinition<?>[] getDefinitions(final double[] marketQuotes, final GeneratorInstrument[] generators, final GeneratorAttribute[] attribute) {
    final InstrumentDefinition<?>[] definitions = new InstrumentDefinition<?>[marketQuotes.length];
    for (int i = 0; i < marketQuotes.length; i++) {
      definitions[i] = generators[i].generateInstrument(NOW, marketQuotes[i], NOTIONAL, attribute[i]);
    }
    return definitions;
  }

  private static final List<Pair<IssuerProviderDiscount, CurveBuildingBlockBundle>> CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK = new ArrayList<>();

  // Calculator
  private static final PresentValueIssuerCalculator PVIC = PresentValueIssuerCalculator.getInstance();
  private static final ParSpreadMarketQuoteIssuerDiscountingCalculator PSMQIC = ParSpreadMarketQuoteIssuerDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator PSMQCSIC = ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator.getInstance();

  private static final IssuerDiscountBuildingRepository CURVE_BUILDING_REPOSITORY = new IssuerDiscountBuildingRepository(TOLERANCE_ROOT, TOLERANCE_ROOT, STEP_MAX);

  private static final double TOLERANCE_CAL = 1.0E-9;

  @BeforeSuite
  static void initClass() {
    for (int i = 0; i < NB_BLOCKS; i++) {
      CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.add(makeCurvesFromDefinitions(DEFINITIONS_UNITS[i], GENERATORS_UNITS[i], NAMES_UNITS[i], KNOWN_DATA, PSMQIC, PSMQCSIC, false));
    }
  }

  @Test
  public void curveConstruction() {
    for (int i = 0; i < NB_BLOCKS; i++) {
      curveConstructionTest(DEFINITIONS_UNITS[i], CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(i).getFirst(), false, i);
    }
  }

  private static List<Pair<IssuerProviderDiscount, CurveBuildingBlockBundle>> getCurvesWithBlock() {
    return CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK;
  }

  private static IssuerProviderDiscount getCurvesWithOnlyDiscount() {
    final IssuerProviderDiscount curves = CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(0).getFirst().copy();
    final IssuerProviderDiscount curve = new IssuerProviderDiscount(curves.getMulticurveProvider().getDiscountingCurves(), curves.getMulticurveProvider().getForwardIborCurves(), curves
        .getMulticurveProvider().getForwardONCurves(), curves.getMulticurveProvider().getFxRates());
    return curve;
  }

  private static CurveBuildingBlockBundle getBundleWithOnlyDiscount() {
    final Map<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> bundle = CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(0).getSecond().getData();
    final Map<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> bundleWithoutIssuer = new LinkedHashMap<>();
    final Set<String> keySet = bundle.keySet();
    for (final String name : keySet) {
      if (name.equals(CURVE_NAME_DSC_USD)) {
        bundleWithoutIssuer.put(name, bundle.get(name));
      }
    }
    final LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> bundleToLinkedMap = new LinkedHashMap<>(bundleWithoutIssuer);
    return new CurveBuildingBlockBundle(bundleToLinkedMap);
  }

  @Test(enabled = false)
  public void performance() {
    long startTime, endTime;
    final int nbTest = 100;

    startTime = System.currentTimeMillis();
    for (int i = 0; i < nbTest; i++) {
      makeCurvesFromDefinitions(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQIC, PSMQCSIC, false);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction / 2 units: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 2 units: 02-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 270 (no Jac)/430 ms for 100 sets.

    startTime = System.currentTimeMillis();
    for (int i = 0; i < nbTest; i++) {
      makeCurvesFromDefinitions(DEFINITIONS_UNITS[1], GENERATORS_UNITS[1], NAMES_UNITS[1], KNOWN_DATA, PSMQIC, PSMQCSIC, false);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction / 1 unit: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 1 unit: 02-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 315 (no Jac)/440 ms for 10 sets.

  }

  private void curveConstructionTest(final InstrumentDefinition<?>[][][] definitions, final IssuerProviderDiscount curves, final boolean withToday, final int block) {
    final int nbBlocks = definitions.length;
    for (int i = 0; i < nbBlocks; i++) {
      final InstrumentDerivative[][] instruments = CurveTestUtils.convert(definitions[i], withToday ? FIXING_TS_WITH_TODAY : FIXING_TS_WITHOUT_TODAY, NOW);
      final double[][] pv = new double[instruments.length][];
      for (int j = 0; j < instruments.length; j++) {
        pv[j] = new double[instruments[j].length];
        for (int k = 0; k < instruments[j].length; k++) {
          pv[j][k] = curves.getMulticurveProvider().getFxRates().convert(instruments[j][k].accept(PVIC, curves), USD).getAmount();
          assertEquals("Curve construction: block " + block + ", unit " + i + " - instrument " + k, 0, pv[j][k], TOLERANCE_CAL);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static Pair<IssuerProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDefinitions(final InstrumentDefinition<?>[][][] definitions, final GeneratorYDCurve[][] curveGenerators,
      final String[][] curveNames, final IssuerProviderDiscount knownData, final InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, MulticurveSensitivity> sensitivityCalculator, final boolean withToday) {
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
          initialGuess[k] = definitions[i][j][k].accept(CurveTestUtils.RATES_INITIALIZATION);
        }
        final GeneratorYDCurve generator = curveGenerators[i][j].finalGenerator(derivatives);
        singleCurves[j] = new SingleCurveBundle<>(curveNames[i][j], derivatives, initialGuess, generator);
      }
      curveBundles[i] = new MultiCurveBundle<>(singleCurves);
    }
    return CURVE_BUILDING_REPOSITORY.makeCurvesFromDerivatives(curveBundles, knownData, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP, DSC_ISS_MAP, calculator,
        sensitivityCalculator);
  }

}
