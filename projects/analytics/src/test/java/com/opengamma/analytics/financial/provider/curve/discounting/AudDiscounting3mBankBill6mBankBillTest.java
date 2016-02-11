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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
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
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositON;
import com.opengamma.analytics.financial.instrument.index.GeneratorFRA;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapIborIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapIborIborMaster;
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
 * Tests with relevant Jacobian matrices.
 */
@Test(groups = TestGroup.UNIT)
public class AudDiscounting3mBankBill6mBankBillTest {
  private static final Interpolator1D INTERPOLATOR_LINEAR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();
  private static final double TOLERANCE_ROOT = 1.0E-10;
  private static final int STEP_MAX = 100;

  private static final Calendar SYD = new MondayToFridayCalendar("SYD");
  private static final Currency AUD = Currency.AUD;
  private static final FXMatrix FX_MATRIX = new FXMatrix(AUD);

  private static final double NOTIONAL = 1.0;

  private static final GeneratorSwapFixedON GENERATOR_OIS_AUD = GeneratorSwapFixedONMaster.getInstance().getGenerator("AUD1YRBAON", SYD);
  /** An overnight AUD index */
  private static final IndexON AUD_OVERNIGHT_INDEX = GENERATOR_OIS_AUD.getIndex();
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_AUD = new GeneratorDepositON("AUD Deposit ON", AUD, SYD, AUD_OVERNIGHT_INDEX.getDayCount());
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapIborIborMaster GENERATOR_BASIS_MASTER = GeneratorSwapIborIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor AUD3MBBSW3M = GENERATOR_SWAP_MASTER.getGenerator("AUD3MBBSW3M", SYD);
  private static final GeneratorSwapFixedIbor AUD6MBBSW6M = GENERATOR_SWAP_MASTER.getGenerator("AUD6MBBSW6M", SYD);
  private static final GeneratorSwapIborIbor AUDBBSW3MBBSW6M = GENERATOR_BASIS_MASTER.getGenerator("AUDBBSW3MBBSW6M", SYD);
  /** A 3M AUD bank bill index */
  private static final IborIndex AUD_3M_BANK_BILL_INDEX = AUD3MBBSW3M.getIborIndex();
  /** A 6M AUD bank bill index */
  private static final IborIndex AUD_6M_BANK_BILL_INDEX = AUD6MBBSW6M.getIborIndex();
  private static final GeneratorFRA GENERATOR_FRA_3M = new GeneratorFRA("GENERATOR_FRA_3M", AUD_3M_BANK_BILL_INDEX, SYD);
  private static final GeneratorDepositIbor GENERATOR_AUDBB3M = new GeneratorDepositIbor("GENERATOR_AUDBB3M", AUD_3M_BANK_BILL_INDEX, SYD);
  private static final GeneratorDepositIbor GENERATOR_AUDBB6M = new GeneratorDepositIbor("GENERATOR_AUDBB6M", AUD_6M_BANK_BILL_INDEX, SYD);

  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2011, 9, 28);

  private static final ZonedDateTimeDoubleTimeSeries TS_EMPTY = ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC();
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_AUD_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_AUD_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.07, 0.08 });

  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_AUD3M_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.0035, 0.0036 });
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_AUD3M_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27) },
      new double[] {0.0035 });
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_AUD6M_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.0035, 0.0036 });
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_AUD6M_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27) },
      new double[] {0.0035 });

  /** Fixing time series created before the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITHOUT_TODAY = new HashMap<>();
  /** Fixing time series created after the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITH_TODAY = new HashMap<>();

  static {
    FIXING_TS_WITH_TODAY.put(AUD_OVERNIGHT_INDEX, TS_ON_AUD_WITH_TODAY);
    FIXING_TS_WITH_TODAY.put(AUD_3M_BANK_BILL_INDEX, TS_IBOR_AUD3M_WITH_TODAY);
    FIXING_TS_WITH_TODAY.put(AUD_6M_BANK_BILL_INDEX, TS_IBOR_AUD6M_WITH_TODAY);
    FIXING_TS_WITHOUT_TODAY.put(AUD_OVERNIGHT_INDEX, TS_ON_AUD_WITHOUT_TODAY);
    FIXING_TS_WITHOUT_TODAY.put(AUD_3M_BANK_BILL_INDEX, TS_IBOR_AUD3M_WITHOUT_TODAY);
    FIXING_TS_WITHOUT_TODAY.put(AUD_6M_BANK_BILL_INDEX, TS_IBOR_AUD6M_WITHOUT_TODAY);
  }
  private static final String CURVE_NAME_DSC_AUD = "AUD Dsc";
  private static final String CURVE_NAME_FWD3_AUD = "AUD Fwd 3M";
  private static final String CURVE_NAME_FWD6_AUD = "AUD Fwd 6M";

  /** Market values for the dsc USD curve */
  private static final double[] DSC_AUD_MARKET_QUOTES = new double[] {0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400 };
  /** Generators for the dsc USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_USD_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_DEPOSIT_ON_AUD, GENERATOR_OIS_AUD, GENERATOR_OIS_AUD,
    GENERATOR_OIS_AUD, GENERATOR_OIS_AUD, GENERATOR_OIS_AUD, GENERATOR_OIS_AUD, GENERATOR_OIS_AUD, GENERATOR_OIS_AUD, GENERATOR_OIS_AUD, GENERATOR_OIS_AUD, GENERATOR_OIS_AUD };
  /** Tenors for the dsc USD curve */
  private static final Period[] DSC_AUD_TENOR = new Period[] {Period.ofDays(0), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1),
    Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
  private static final GeneratorAttributeIR[] DSC_AUD_ATTR = new GeneratorAttributeIR[DSC_AUD_TENOR.length];
  static {
    for (int i = 0; i < DSC_AUD_TENOR.length; i++) {
      DSC_AUD_ATTR[i] = new GeneratorAttributeIR(DSC_AUD_TENOR[i]);
    }
  }

  /** Market values for the Fwd 3M USD curve */
  private static final double[] FWD3_AUD_MARKET_QUOTES = new double[] {0.0420, 0.0420, 0.0420, 0.0420, 0.0430, 0.0470, 0.0020, 0.0020, 0.0020 };
  /** Generators for the Fwd 3M USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD3_AUD_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_AUDBB3M, GENERATOR_FRA_3M, GENERATOR_FRA_3M, AUD3MBBSW3M,
    AUD3MBBSW3M, AUD3MBBSW3M, AUDBBSW3MBBSW6M, AUDBBSW3MBBSW6M, AUDBBSW3MBBSW6M };
  /** Tenors for the Fwd 3M USD curve */
  private static final Period[] FWD3_AUD_TENOR = new Period[] {Period.ofMonths(0), Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(5),
    Period.ofYears(7), Period.ofYears(10) };
  private static final GeneratorAttributeIR[] FWD3_AUD_ATTR = new GeneratorAttributeIR[FWD3_AUD_TENOR.length];
  static {
    for (int i = 0; i < FWD3_AUD_TENOR.length; i++) {
      FWD3_AUD_ATTR[i] = new GeneratorAttributeIR(FWD3_AUD_TENOR[i]);
    }
  }

  /** Market values for the Fwd 3M USD curve */
  private static final double[] FWD6_AUD_MARKET_QUOTES = new double[] {0.0440, 0.0020, 0.0020, 0.0020, 0.0560, 0.0610, 0.0620 };
  /** Generators for the Fwd 3M USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD6_AUD_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_AUDBB6M, AUDBBSW3MBBSW6M, AUDBBSW3MBBSW6M, AUDBBSW3MBBSW6M,
    AUD6MBBSW6M, AUD6MBBSW6M, AUD6MBBSW6M };
  /** Tenors for the Fwd 3M USD curve */
  private static final Period[] FWD6_AUD_TENOR = new Period[] {Period.ofMonths(0), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(5), Period.ofYears(7), Period.ofYears(10) };
  private static final GeneratorAttributeIR[] FWD6_AUD_ATTR = new GeneratorAttributeIR[FWD6_AUD_TENOR.length];
  static {
    for (int i = 0; i < FWD6_AUD_TENOR.length; i++) {
      FWD6_AUD_ATTR[i] = new GeneratorAttributeIR(FWD6_AUD_TENOR[i]);
    }
  }

  /** Standard USD discounting curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_DSC_AUD;
  /** Standard USD Forward 3M curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_FWD3_AUD;
  /** Standard USD Forward 3M curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_FWD6_AUD;

  /** Units of curves */
  private static final int[] NB_UNITS = new int[] {2, 1 };
  private static final int NB_BLOCKS = NB_UNITS.length;
  private static final InstrumentDefinition<?>[][][][] DEFINITIONS_UNITS = new InstrumentDefinition<?>[NB_BLOCKS][][][];
  private static final GeneratorYDCurve[][][] GENERATORS_UNITS = new GeneratorYDCurve[NB_BLOCKS][][];
  private static final String[][][] NAMES_UNITS = new String[NB_BLOCKS][][];
  private static final MulticurveProviderDiscount KNOWN_DATA = new MulticurveProviderDiscount(FX_MATRIX);
  private static final LinkedHashMap<String, Currency> DSC_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IndexON[]> FWD_ON_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IborIndex[]> FWD_IBOR_MAP = new LinkedHashMap<>();

  static {
    DEFINITIONS_DSC_AUD = getDefinitions(DSC_AUD_MARKET_QUOTES, DSC_USD_GENERATORS, DSC_AUD_ATTR);
    DEFINITIONS_FWD3_AUD = getDefinitions(FWD3_AUD_MARKET_QUOTES, FWD3_AUD_GENERATORS, FWD3_AUD_ATTR);
    DEFINITIONS_FWD6_AUD = getDefinitions(FWD6_AUD_MARKET_QUOTES, FWD6_AUD_GENERATORS, FWD6_AUD_ATTR);
    for (int i = 0; i < NB_BLOCKS; i++) {
      DEFINITIONS_UNITS[i] = new InstrumentDefinition<?>[NB_UNITS[i]][][];
      GENERATORS_UNITS[i] = new GeneratorYDCurve[NB_UNITS[i]][];
      NAMES_UNITS[i] = new String[NB_UNITS[i]][];
    }
    DEFINITIONS_UNITS[0][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_AUD };
    DEFINITIONS_UNITS[0][1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_AUD, DEFINITIONS_FWD6_AUD };
    DEFINITIONS_UNITS[1][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_AUD, DEFINITIONS_FWD3_AUD, DEFINITIONS_FWD6_AUD };
    final GeneratorYDCurve genIntLin = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR_LINEAR);
    GENERATORS_UNITS[0][0] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[0][1] = new GeneratorYDCurve[] {genIntLin, genIntLin };
    GENERATORS_UNITS[1][0] = new GeneratorYDCurve[] {genIntLin, genIntLin, genIntLin };
    NAMES_UNITS[0][0] = new String[] {CURVE_NAME_DSC_AUD };
    NAMES_UNITS[0][1] = new String[] {CURVE_NAME_FWD3_AUD, CURVE_NAME_FWD6_AUD };
    NAMES_UNITS[1][0] = new String[] {CURVE_NAME_DSC_AUD, CURVE_NAME_FWD3_AUD, CURVE_NAME_FWD6_AUD };
    DSC_MAP.put(CURVE_NAME_DSC_AUD, AUD);
    FWD_ON_MAP.put(CURVE_NAME_DSC_AUD, new IndexON[] {AUD_OVERNIGHT_INDEX });
    FWD_IBOR_MAP.put(CURVE_NAME_FWD3_AUD, new IborIndex[] {AUD_3M_BANK_BILL_INDEX });
    FWD_IBOR_MAP.put(CURVE_NAME_FWD6_AUD, new IborIndex[] {AUD_6M_BANK_BILL_INDEX });
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

  @Test
  public void curveConstruction() {
    for (int i = 0; i < NB_BLOCKS; i++) {
      curveConstructionTest(DEFINITIONS_UNITS[i], CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(i).getFirst(), false, i);
    }
  }

  @Test
  public void comparison1Unit2Units() {
    final MulticurveProviderDiscount[] units = new MulticurveProviderDiscount[2];
    final CurveBuildingBlockBundle[] bb = new CurveBuildingBlockBundle[2];
    final YieldAndDiscountCurve[] curveDsc = new YieldAndDiscountCurve[2];
    final YieldAndDiscountCurve[] curveFwd = new YieldAndDiscountCurve[2];
    for (int i = 0; i < 2; i++) {
      units[i] = CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(i).getFirst();
      bb[i] = CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(i).getSecond();
      curveDsc[i] = units[i].getCurve(AUD);
      curveFwd[i] = units[i].getCurve(AUD_3M_BANK_BILL_INDEX);
    }
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

    assertEquals("Curve construction: 1 unit / 2 units ", bb[0].getBlock(CURVE_NAME_FWD3_AUD).getFirst(), bb[1].getBlock(CURVE_NAME_FWD3_AUD).getFirst());
    // Test note: the discounting curve building blocks are not the same; in one case both curves are build together in the other one after the other.
    final int nbLineDsc = bb[0].getBlock(CURVE_NAME_DSC_AUD).getSecond().getNumberOfRows();
    final int nbLineFwd3 = bb[0].getBlock(CURVE_NAME_FWD3_AUD).getSecond().getNumberOfRows();
    final int nbLineFwd6 = bb[0].getBlock(CURVE_NAME_FWD6_AUD).getSecond().getNumberOfRows();
    assertEquals("Curve construction: 1 unit / 2 units ", bb[1].getBlock(CURVE_NAME_DSC_AUD).getSecond().getNumberOfRows(), nbLineDsc);
    assertEquals("Curve construction: 1 unit / 2 units ", bb[1].getBlock(CURVE_NAME_FWD3_AUD).getSecond().getNumberOfRows(), nbLineFwd3);
    assertEquals("Curve construction: 1 unit / 2 units ", bb[1].getBlock(CURVE_NAME_FWD6_AUD).getSecond().getNumberOfRows(), nbLineFwd6);
  }

  //TODO: test on the correctness of the Jacobian matrix in the CurveBuildingBlock's.

  @Test(enabled = false)
  public void performance() {
    long startTime, endTime;
    final int nbTest = 100;

    startTime = System.currentTimeMillis();
    for (int i = 0; i < nbTest; i++) {
      makeCurvesFromDefinitions(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQC, PSMQCSC, false);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " x 3 curves construction / 2 units: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 2 units: 08-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 810 ms for 100 sets.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      makeCurvesFromDefinitions(DEFINITIONS_UNITS[1], GENERATORS_UNITS[1], NAMES_UNITS[1], KNOWN_DATA, PSMQC, PSMQCSC, false);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " x 3 curves construction / 1 unit: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 1 unit: 08-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 995 ms for 100 sets.

  }

  private void curveConstructionTest(final InstrumentDefinition<?>[][][] definitions, final MulticurveProviderDiscount curves, final boolean withToday, final int block) {
    final int nbBlocks = definitions.length;
    for (int i = 0; i < nbBlocks; i++) {
      final InstrumentDerivative[][] instruments = CurveTestUtils.convert(definitions[i], withToday ? FIXING_TS_WITH_TODAY : FIXING_TS_WITHOUT_TODAY, NOW);
      final double[][] pv = new double[instruments.length][];
      for (int j = 0; j < instruments.length; j++) {
        pv[j] = new double[instruments[j].length];
        for (int k = 0; k < instruments[j].length; k++) {
          pv[j][k] = curves.getFxRates().convert(instruments[j][k].accept(PVC, curves), AUD).getAmount();
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
    final int nbDate = 2750;
    ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(NOW, AUD_3M_BANK_BILL_INDEX.getSpotLag() + startIndex * jump, SYD);
    final double[] rateDsc = new double[nbDate];
    final double[] startTime = new double[nbDate];
    try (final FileWriter writer = new FileWriter("fwd-dsc.csv")) {
      for (int i = 0; i < nbDate; i++) {
        startTime[i] = TimeCalculator.getTimeBetween(NOW, startDate);
        final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, AUD_3M_BANK_BILL_INDEX, SYD);
        final double endTime = TimeCalculator.getTimeBetween(NOW, endDate);
        final double accrualFactor = AUD_3M_BANK_BILL_INDEX.getDayCount().getDayCountFraction(startDate, endDate);
        rateDsc[i] = marketDsc.getSimplyCompoundForwardRate(AUD_3M_BANK_BILL_INDEX, startTime[i], endTime, accrualFactor);
        startDate = ScheduleCalculator.getAdjustedDate(startDate, jump, SYD);
        writer.append(0.0 + "," + startTime[i] + "," + rateDsc[i] + "\n");
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
    final int nUnits = curveGenerators.length;
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
    return CURVE_BUILDING_REPOSITORY.makeCurvesFromDerivatives(curveBundles, knownData, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP, calculator,
        sensitivityCalculator);
  }

}
