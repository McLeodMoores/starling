/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve.discounting;

import static com.opengamma.analytics.financial.provider.curve.CurveBuildingTestUtils.assertFiniteDifferenceSensitivities;
import static com.opengamma.analytics.financial.provider.curve.CurveBuildingTestUtils.assertNoSensitivities;
import static com.opengamma.analytics.financial.provider.curve.CurveBuildingTestUtils.assertYieldCurvesEqual;
import static com.opengamma.analytics.financial.provider.curve.CurveBuildingTestUtils.curveConstructionTest;
import static org.testng.Assert.assertEquals;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.financial.curve.interestrate.CurveBuilder;
import com.mcleodmoores.analytics.financial.curve.interestrate.DiscountingMethodCurveBuilder;
import com.mcleodmoores.analytics.financial.curve.interestrate.DiscountingMethodCurveSetUp;
import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeFX;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositON;
import com.opengamma.analytics.financial.instrument.index.GeneratorFRA;
import com.opengamma.analytics.financial.instrument.index.GeneratorForexSwap;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapXCcyIborIbor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.Index;
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

@Test(groups = TestGroup.UNIT)
public class UsdEurDiscountingLiborXCcyTest extends CurveBuildingTests {
  /** The interpolator used for all curves */
  private static final Interpolator1D INTERPOLATOR = NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME);
  /** TARGET holidays */
  private static final CalendarAdapter TARGET = new CalendarAdapter(WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
  /** NYC holidays */
  private static final CalendarAdapter NYC = new CalendarAdapter(WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
  /** Spot USD/EUR */
  private static final double FX_EURUSD = 1.40;
  /** The FX matrix */
  private static final FXMatrix FX_MATRIX = new FXMatrix(Currency.USD);
  static {
    FX_MATRIX.addCurrency(Currency.EUR, Currency.USD, FX_EURUSD);
  }
  /** Generates EUR OIS */
  private static final GeneratorSwapFixedON GENERATOR_OIS_EUR = GeneratorSwapFixedONMaster.getInstance().getGenerator("EUR1YEONIA", TARGET);
  /** Generates USD OIS */
  private static final GeneratorSwapFixedON GENERATOR_OIS_USD = GeneratorSwapFixedONMaster.getInstance().getGenerator("USD1YFEDFUND", TARGET);
  /** EONIA index */
  private static final IndexON EONIA_INDEX = GENERATOR_OIS_EUR.getIndex();
  /** Fed funds index */
  private static final IndexON FED_FUNDS_INDEX = GENERATOR_OIS_USD.getIndex();
  /** Generates the overnight deposit */
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_EUR = new GeneratorDepositON("EUR Deposit ON", Currency.EUR, TARGET, EONIA_INDEX.getDayCount());
  /** Generates the overnight deposit */
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_USD = new GeneratorDepositON("USD Deposit ON", Currency.USD, NYC, FED_FUNDS_INDEX.getDayCount());
  /** Generates 1y fixed / 3m EURIBOR swaps */
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR3M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("EUR1YEURIBOR3M", TARGET);
  /** Generates 6m fixed / 3m LIBOR swaps */
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("USD6MLIBOR3M", NYC);
  /** A 3M EURIBOR index */
  private static final IborIndex EUR_3M_EURIBOR_INDEX = EUR1YEURIBOR3M.getIborIndex();
  /** A 3M USD LIBOR index */
  private static final IborIndex USD_3M_LIBOR_INDEX = USD6MLIBOR3M.getIborIndex();
  /** Generates 3m LIBOR FRAs */
  private static final GeneratorFRA GENERATOR_USD_FRA_3M = new GeneratorFRA("GENERATOR USD FRA 3M", USD_3M_LIBOR_INDEX, NYC);
  /** Generates the 3m EURIBOR deposit */
  private static final GeneratorDepositIbor GENERATOR_EURIBOR3M = new GeneratorDepositIbor("GENERATOR_EURIBOR3M", EUR_3M_EURIBOR_INDEX, TARGET);
  /** Generates the 3m LIBOR deposit */
  private static final GeneratorDepositIbor GENERATOR_USDLIBOR3M = new GeneratorDepositIbor("GENERATOR_USDLIBOR3M", USD_3M_LIBOR_INDEX, NYC);
  /** Generates 3m LIBOR / 3m EURIBOR cross-currency swaps with the spread on the EUR leg */
  private static final GeneratorSwapXCcyIborIbor EURIBOR3MUSDLIBOR3M =
      new GeneratorSwapXCcyIborIbor("EURIBOR3MUSDLIBOR3M", EUR_3M_EURIBOR_INDEX, USD_3M_LIBOR_INDEX, TARGET, NYC);
  /** Generates USD/EUR FX swaps */
  private static final GeneratorForexSwap GENERATOR_FX_EURUSD = new GeneratorForexSwap("EURUSD", Currency.EUR, Currency.USD, TARGET,
      EUR_3M_EURIBOR_INDEX.getSpotLag(), EUR_3M_EURIBOR_INDEX.getBusinessDayConvention(), true);
  /** The curve construction date */
  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2011, 9, 28);
  /** The previous day */
  private static final ZonedDateTime PREVIOUS = NOW.minusDays(1);
  /** Fed funds fixing series after today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries FF_TS_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS, NOW}, new double[] {0.07, 0.08});
  /** Fed funds fixing series before today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries FF_TS_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS}, new double[] {0.07});
  /** LIBOR fixing series after today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries LIBOR_TS_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS, NOW}, new double[] {0.0035, 0.0036});
  /** LIBOR fixing series before today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries LIBOR_TS_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS}, new double[] {0.0035});
  /** EURIBOR fixing series after today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries EURIBOR_TS_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS, NOW}, new double[] {0.0060, 0.0061});
  /** EURIBOR fixing series before today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries EURIBOR_TS_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS}, new double[] {0.0060});
  /** Fixing time series created before the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITHOUT_TODAY = new HashMap<>();
  /** Fixing time series created after the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITH_TODAY = new HashMap<>();
  static {
    FIXING_TS_WITHOUT_TODAY.put(FED_FUNDS_INDEX, FF_TS_WITHOUT_TODAY);
    FIXING_TS_WITHOUT_TODAY.put(USD_3M_LIBOR_INDEX, LIBOR_TS_WITHOUT_TODAY);
    FIXING_TS_WITHOUT_TODAY.put(EUR_3M_EURIBOR_INDEX, EURIBOR_TS_WITHOUT_TODAY);
    FIXING_TS_WITH_TODAY.put(FED_FUNDS_INDEX, FF_TS_WITH_TODAY);
    FIXING_TS_WITH_TODAY.put(USD_3M_LIBOR_INDEX, LIBOR_TS_WITH_TODAY);
    FIXING_TS_WITH_TODAY.put(EUR_3M_EURIBOR_INDEX, EURIBOR_TS_WITH_TODAY);
  }
  /** EUR discounting curve name */
  private static final String CURVE_NAME_DSC_EUR = "EUR Dsc";
  /** 3m EURIBOR curve name */
  private static final String CURVE_NAME_FWD3_EUR = "EUR Fwd 3M";
  /** USD discounting curve name */
  private static final String CURVE_NAME_DSC_USD = "USD Dsc";
  /** 3m LIBOR curve name */
  private static final String CURVE_NAME_FWD3_USD = "USD Fwd 3M";
  /** Already known data - contains only the FX matrix */
  private static final MulticurveProviderDiscount KNOWN_DATA = new MulticurveProviderDiscount(FX_MATRIX);
  /** Builds USD discounting, then LIBOR, then two EUR curves using the first set of data */
  private static final DiscountingMethodCurveSetUp BUILDER_1 = DiscountingMethodCurveBuilder.setUp()
      .buildingFirst(CURVE_NAME_DSC_USD)
      .thenBuilding(CURVE_NAME_FWD3_USD)
      .thenBuilding(CURVE_NAME_DSC_EUR, CURVE_NAME_FWD3_EUR)
      .using(CURVE_NAME_DSC_USD).forDiscounting(Currency.USD).forOvernightIndex(FED_FUNDS_INDEX).withInterpolator(INTERPOLATOR)
      .using(CURVE_NAME_FWD3_USD).forIborIndex(USD_3M_LIBOR_INDEX).withInterpolator(INTERPOLATOR)
      .using(CURVE_NAME_DSC_EUR).forDiscounting(Currency.EUR).forOvernightIndex(EONIA_INDEX).withInterpolator(INTERPOLATOR)
      .using(CURVE_NAME_FWD3_EUR).forIborIndex(EUR_3M_EURIBOR_INDEX).withInterpolator(INTERPOLATOR)
      .withKnownData(KNOWN_DATA);
  /** Builds USD discounting, then LIBOR, then two EUR curves using the second set of data */
  private static final DiscountingMethodCurveSetUp BUILDER_2 = DiscountingMethodCurveBuilder.setUp()
      .buildingFirst(CURVE_NAME_DSC_USD)
      .thenBuilding(CURVE_NAME_FWD3_USD)
      .thenBuilding(CURVE_NAME_DSC_EUR, CURVE_NAME_FWD3_EUR)
      .using(CURVE_NAME_DSC_USD).forDiscounting(Currency.USD).forOvernightIndex(FED_FUNDS_INDEX).withInterpolator(INTERPOLATOR)
      .using(CURVE_NAME_FWD3_USD).forIborIndex(USD_3M_LIBOR_INDEX).withInterpolator(INTERPOLATOR)
      .using(CURVE_NAME_DSC_EUR).forDiscounting(Currency.EUR).forOvernightIndex(EONIA_INDEX).withInterpolator(INTERPOLATOR)
      .using(CURVE_NAME_FWD3_EUR).forIborIndex(EUR_3M_EURIBOR_INDEX).withInterpolator(INTERPOLATOR)
      .withKnownData(KNOWN_DATA);
  /** Builds USD discounting, then LIBOR, then two EUR curves using the third set of data */
  private static final DiscountingMethodCurveSetUp BUILDER_3 = DiscountingMethodCurveBuilder.setUp()
      .buildingFirst(CURVE_NAME_DSC_USD)
      .thenBuilding(CURVE_NAME_FWD3_USD)
      .thenBuilding(CURVE_NAME_DSC_EUR, CURVE_NAME_FWD3_EUR)
      .using(CURVE_NAME_DSC_USD).forDiscounting(Currency.USD).forOvernightIndex(FED_FUNDS_INDEX).withInterpolator(INTERPOLATOR)
      .using(CURVE_NAME_FWD3_USD).forIborIndex(USD_3M_LIBOR_INDEX).withInterpolator(INTERPOLATOR)
      .using(CURVE_NAME_DSC_EUR).forDiscounting(Currency.EUR).forOvernightIndex(EONIA_INDEX).withInterpolator(INTERPOLATOR)
      .using(CURVE_NAME_FWD3_EUR).forIborIndex(EUR_3M_EURIBOR_INDEX).withInterpolator(INTERPOLATOR)
      .withKnownData(KNOWN_DATA);
  /** Market values for the USD discounting curve */
  private static final double[] DSC_USD_MARKET_QUOTES =
      new double[] {0.0010, 0.0010, 0.0010, 0.0010, 0.0010, 0.0010, 0.0010, 0.0010, 0.0015, 0.0020, 0.0035, 0.0050, 0.0130};
  /** Vanilla instrument generators for the USD discounting curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_USD_GENERATORS = new GeneratorInstrument<?>[] {
    GENERATOR_DEPOSIT_ON_USD, GENERATOR_DEPOSIT_ON_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD,
    GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD};
    /** USD discounting curve attributes */
  private static final GeneratorAttributeIR[] DSC_USD_ATTR;
  static {
    final Period[] tenors = new Period[] {Period.ofDays(0), Period.ofDays(1), Period.ofMonths(1), Period.ofMonths(2),
      Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9),
      Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
    DSC_USD_ATTR = new GeneratorAttributeIR[tenors.length];
    for (int i = 0; i < 2; i++) {
      DSC_USD_ATTR[i] = new GeneratorAttributeIR(tenors[i], Period.ZERO);
      BUILDER_1.withNode(CURVE_NAME_DSC_USD, DSC_USD_GENERATORS[i], DSC_USD_ATTR[i], DSC_USD_MARKET_QUOTES[i]);
      BUILDER_2.withNode(CURVE_NAME_DSC_USD, DSC_USD_GENERATORS[i], DSC_USD_ATTR[i], DSC_USD_MARKET_QUOTES[i]);
      BUILDER_3.withNode(CURVE_NAME_DSC_USD, DSC_USD_GENERATORS[i], DSC_USD_ATTR[i], DSC_USD_MARKET_QUOTES[i]);
    }
    for (int i = 2; i < tenors.length; i++) {
      DSC_USD_ATTR[i] = new GeneratorAttributeIR(tenors[i]);
      BUILDER_1.withNode(CURVE_NAME_DSC_USD, DSC_USD_GENERATORS[i], DSC_USD_ATTR[i], DSC_USD_MARKET_QUOTES[i]);
      BUILDER_2.withNode(CURVE_NAME_DSC_USD, DSC_USD_GENERATORS[i], DSC_USD_ATTR[i], DSC_USD_MARKET_QUOTES[i]);
      BUILDER_3.withNode(CURVE_NAME_DSC_USD, DSC_USD_GENERATORS[i], DSC_USD_ATTR[i], DSC_USD_MARKET_QUOTES[i]);
    }
  }
  /** Market values for the USD LIBOR curve */
  private static final double[] FWD3_USD_MARKET_QUOTES = new double[] {0.0045, 0.0045, 0.0045, 0.0045, 0.0060, 0.0070, 0.0080, 0.0160 };
  /** Vanilla instrument generators for the USD LIBOR curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD3_USD_GENERATORS = new GeneratorInstrument<?>[] {
    GENERATOR_USDLIBOR3M, GENERATOR_USD_FRA_3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M };
    /** USD LIBOR curve attributes */
  private static final GeneratorAttributeIR[] FWD3_USD_ATTR;
  static {
    final Period[] tenors = new Period[] {Period.ofMonths(0), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2),
        Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
        Period.ofYears(10) };
    FWD3_USD_ATTR = new GeneratorAttributeIR[tenors.length];
    for (int i = 0; i < tenors.length; i++) {
      FWD3_USD_ATTR[i] = new GeneratorAttributeIR(tenors[i]);
      BUILDER_1.withNode(CURVE_NAME_FWD3_USD, FWD3_USD_GENERATORS[i], FWD3_USD_ATTR[i], FWD3_USD_MARKET_QUOTES[i]);
      BUILDER_2.withNode(CURVE_NAME_FWD3_USD, FWD3_USD_GENERATORS[i], FWD3_USD_ATTR[i], FWD3_USD_MARKET_QUOTES[i]);
      BUILDER_3.withNode(CURVE_NAME_FWD3_USD, FWD3_USD_GENERATORS[i], FWD3_USD_ATTR[i], FWD3_USD_MARKET_QUOTES[i]);
    }
  }
  /** First set of market values for the EUR discounting curve */
  private static final double[] DSC_EUR_MARKET_QUOTES_1 =
      new double[] {0.0010, 0.0010, 0.0004, 0.0009, 0.0015, 0.0035, 0.0050, 0.0060, -0.0050, -0.0050, -0.0050, -0.0045, -0.0040 };
  /** First set of vanilla instrument generators for the EUR discounting curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_EUR_GENERATORS_1 = new GeneratorInstrument<?>[] {
    GENERATOR_DEPOSIT_ON_EUR, GENERATOR_DEPOSIT_ON_EUR, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD,
    GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M,
    EURIBOR3MUSDLIBOR3M };
  /** First set of attributes for the EUR discounting curve */
  private static final GeneratorAttribute[] DSC_EUR_ATTR_1;
  static {
    final Period[] tenors = new Period[] {Period.ofDays(0), Period.ofDays(1), Period.ofMonths(1), Period.ofMonths(2),
      Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9),
      Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
    DSC_EUR_ATTR_1 = new GeneratorAttribute[tenors.length];
    for (int i = 0; i < 2; i++) {
      DSC_EUR_ATTR_1[i] = new GeneratorAttributeIR(tenors[i], Period.ZERO);
      BUILDER_1.withNode(CURVE_NAME_DSC_EUR, DSC_EUR_GENERATORS_1[i], DSC_EUR_ATTR_1[i], DSC_EUR_MARKET_QUOTES_1[i]);
    }
    for (int i = 2; i < tenors.length; i++) {
      DSC_EUR_ATTR_1[i] = new GeneratorAttributeFX(tenors[i], FX_MATRIX);
      BUILDER_1.withNode(CURVE_NAME_DSC_EUR, DSC_EUR_GENERATORS_1[i], DSC_EUR_ATTR_1[i], DSC_EUR_MARKET_QUOTES_1[i]);
    }
  }
  /** First set of market values for the EURIBOR curve */
  private static final double[] FWD3_EUR_MARKET_QUOTES_1 = new double[] {0.0045, 0.0045, 0.0045, 0.0045, 0.0050, 0.0060, 0.0085, 0.0160 };
  /** First set of vanilla instrument generators for the EURIBOR curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD3_EUR_GENERATORS_1 = new GeneratorInstrument<?>[] {
    GENERATOR_EURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M };
  /** First set of attributes for the EURIBOR curve */
  private static final GeneratorAttributeIR[] FWD3_EUR_ATTR_1;
  static {
    final Period[] tenors = new Period[] {Period.ofMonths(0), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2),
      Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
      Period.ofYears(10) };
    FWD3_EUR_ATTR_1 = new GeneratorAttributeIR[tenors.length];
    for (int i = 0; i < tenors.length; i++) {
      FWD3_EUR_ATTR_1[i] = new GeneratorAttributeIR(tenors[i]);
      BUILDER_1.withNode(CURVE_NAME_FWD3_EUR, FWD3_EUR_GENERATORS_1[i], FWD3_EUR_ATTR_1[i], FWD3_EUR_MARKET_QUOTES_1[i]);
    }
  }
  /** Second set of market values for the EUR discounting curve */
  private static final double[] DSC_EUR_MARKET_QUOTES_2 =
      new double[] {0.0010, 0.0010, 0.0004, 0.0009, 0.0015, 0.0035, 0.0050, 0.0060, 0.0045, 0.0050, 0.0060, 0.0085, 0.0160 };
  /** Second set of vanilla instrument generators for the EUR discounting curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_EUR_GENERATORS_2 = new GeneratorInstrument<?>[] {
    GENERATOR_DEPOSIT_ON_EUR, GENERATOR_DEPOSIT_ON_EUR, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD,
    GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M };
  /** Second set of attributes for the EUR discounting curve */
  private static final GeneratorAttribute[] DSC_EUR_ATTR_2;
  static {
    final Period[] tenors = new Period[] {Period.ofDays(0), Period.ofDays(1), Period.ofMonths(1), Period.ofMonths(2),
        Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3),
        Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
    DSC_EUR_ATTR_2 = new GeneratorAttribute[tenors.length];
    for (int i = 0; i < 2; i++) {
      DSC_EUR_ATTR_2[i] = new GeneratorAttributeIR(tenors[i], Period.ZERO);
      BUILDER_2.withNode(CURVE_NAME_DSC_EUR, DSC_EUR_GENERATORS_2[i], DSC_EUR_ATTR_2[i], DSC_EUR_MARKET_QUOTES_2[i]);
    }
    for (int i = 2; i < 8; i++) {
      DSC_EUR_ATTR_2[i] = new GeneratorAttributeFX(tenors[i], FX_MATRIX);
      BUILDER_2.withNode(CURVE_NAME_DSC_EUR, DSC_EUR_GENERATORS_2[i], DSC_EUR_ATTR_2[i], DSC_EUR_MARKET_QUOTES_2[i]);
    }
    for (int i = 8; i < tenors.length; i++) {
      DSC_EUR_ATTR_2[i] = new GeneratorAttributeIR(tenors[i]);
      BUILDER_2.withNode(CURVE_NAME_DSC_EUR, DSC_EUR_GENERATORS_2[i], DSC_EUR_ATTR_2[i], DSC_EUR_MARKET_QUOTES_2[i]);
    }
  }
  /** Second set of market values for the EURIBOR curve */
  private static final double[] FWD3_EUR_MARKET_QUOTES_2 = new double[] {0.0045, 0.0045, 0.0045, -0.0050, -0.0050, -0.0050, -0.0045, -0.0040 };
  /** Second set of vanilla instrument generators for the EURIBOR curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD3_EUR_GENERATORS_2 = new GeneratorInstrument<?>[] {
    GENERATOR_EURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M,
    EURIBOR3MUSDLIBOR3M };
  /** Second set of attributes for the EURIBOR curve */
  private static final GeneratorAttribute[] FWD3_EUR_ATTR_2;
  static {
    final Period[] tenors = new Period[] {Period.ofMonths(0), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2),
      Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
      Period.ofYears(10) };
    FWD3_EUR_ATTR_2 = new GeneratorAttribute[tenors.length];
    for (int i = 0; i < 3; i++) {
      FWD3_EUR_ATTR_2[i] = new GeneratorAttributeIR(tenors[i]);
      BUILDER_2.withNode(CURVE_NAME_FWD3_EUR, FWD3_EUR_GENERATORS_2[i], FWD3_EUR_ATTR_2[i], FWD3_EUR_MARKET_QUOTES_2[i]);
    }
    for (int i = 3; i < tenors.length; i++) {
      FWD3_EUR_ATTR_2[i] = new GeneratorAttributeFX(tenors[i], FX_MATRIX);
      BUILDER_2.withNode(CURVE_NAME_FWD3_EUR, FWD3_EUR_GENERATORS_2[i], FWD3_EUR_ATTR_2[i], FWD3_EUR_MARKET_QUOTES_2[i]);
    }
  }
  /** Third set of market values for the EUR discounting curve */
  private static final double[] DSC_EUR_MARKET_QUOTES_3 =
      new double[] {0.0010, 0.0010, 0.0004, 0.0009, 0.0015, 0.0035, 0.0050, 0.0060, 0.0045, 0.0050, 0.0060, 0.0085, -0.0040 };
  /** Third set of vanilla instrument generators for the EUR discounting curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_EUR_GENERATORS_3 = new GeneratorInstrument<?>[] {
    GENERATOR_DEPOSIT_ON_EUR, GENERATOR_DEPOSIT_ON_EUR, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD,
    GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EURIBOR3MUSDLIBOR3M };
  /** Third set of attributes for the EUR discounting curve */
  private static final GeneratorAttribute[] DSC_EUR_ATTR_3;
  static {
    final Period[] tenors = new Period[] {Period.ofDays(0), Period.ofDays(1), Period.ofMonths(1), Period.ofMonths(2),
      Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3),
      Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
    DSC_EUR_ATTR_3 = new GeneratorAttribute[tenors.length];
    for (int i = 0; i < 2; i++) {
      DSC_EUR_ATTR_3[i] = new GeneratorAttributeIR(tenors[i], Period.ZERO);
      BUILDER_3.withNode(CURVE_NAME_DSC_EUR, DSC_EUR_GENERATORS_3[i], DSC_EUR_ATTR_3[i], DSC_EUR_MARKET_QUOTES_3[i]);
    }
    for (int i = 2; i < 8; i++) {
      DSC_EUR_ATTR_3[i] = new GeneratorAttributeFX(tenors[i], FX_MATRIX);
      BUILDER_3.withNode(CURVE_NAME_DSC_EUR, DSC_EUR_GENERATORS_3[i], DSC_EUR_ATTR_3[i], DSC_EUR_MARKET_QUOTES_3[i]);
    }
    for (int i = 8; i < tenors.length - 1; i++) {
      DSC_EUR_ATTR_3[i] = new GeneratorAttributeIR(tenors[i]);
      BUILDER_3.withNode(CURVE_NAME_DSC_EUR, DSC_EUR_GENERATORS_3[i], DSC_EUR_ATTR_3[i], DSC_EUR_MARKET_QUOTES_3[i]);
    }
    DSC_EUR_ATTR_3[tenors.length - 1] = new GeneratorAttributeFX(tenors[tenors.length - 1], FX_MATRIX);
    BUILDER_3.withNode(CURVE_NAME_DSC_EUR, DSC_EUR_GENERATORS_3[tenors.length - 1], DSC_EUR_ATTR_3[tenors.length - 1],
        DSC_EUR_MARKET_QUOTES_3[tenors.length - 1]);
  }
  /** Third set of market values for the EURIBOR curve */
  private static final double[] FWD3_EUR_MARKET_QUOTES_3 = new double[] {0.0045, 0.0045, 0.0045, -0.0050, -0.0050, -0.0050, -0.0045, 0.0160 };
  /** Third set of vanilla instrument generators for the EURIBOR curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD3_EUR_GENERATORS_3 = new GeneratorInstrument<?>[] {
    GENERATOR_EURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M,
    EUR1YEURIBOR3M };
  /** Third set of attributes for the EURIBOR curve */
  private static final GeneratorAttribute[] FWD3_EUR_ATTR_3;
  static {
    final Period[] tenors = new Period[] {Period.ofMonths(0), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2),
      Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
    FWD3_EUR_ATTR_3 = new GeneratorAttribute[tenors.length];
    for (int i = 0; i < 3; i++) {
      FWD3_EUR_ATTR_3[i] = new GeneratorAttributeIR(tenors[i]);
      BUILDER_3.withNode(CURVE_NAME_FWD3_EUR, FWD3_EUR_GENERATORS_3[i], FWD3_EUR_ATTR_3[i], FWD3_EUR_MARKET_QUOTES_3[i]);
    }
    for (int i = 3; i < tenors.length - 1; i++) {
      FWD3_EUR_ATTR_3[i] = new GeneratorAttributeFX(tenors[i], FX_MATRIX);
      BUILDER_3.withNode(CURVE_NAME_FWD3_EUR, FWD3_EUR_GENERATORS_3[i], FWD3_EUR_ATTR_3[i], FWD3_EUR_MARKET_QUOTES_3[i]);
    }
    FWD3_EUR_ATTR_3[tenors.length - 1] = new GeneratorAttributeIR(tenors[tenors.length - 1]);
    BUILDER_3.withNode(CURVE_NAME_FWD3_EUR, FWD3_EUR_GENERATORS_3[tenors.length - 1], FWD3_EUR_ATTR_3[tenors.length - 1],
        FWD3_EUR_MARKET_QUOTES_3[tenors.length - 1]);
  }
  /** First set of curves built before today's fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> BEFORE_FIXING_1;
  /** Second set of curves built before today's fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> BEFORE_FIXING_2;
  /** Third set of curves built before today's fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> BEFORE_FIXING_3;
  /** First set of curves built after today's fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> AFTER_FIXING_1;
  /** Second set of curves built after today's fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> AFTER_FIXING_2;
  /** Third set of curves built after today's fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> AFTER_FIXING_3;
  static {
    BEFORE_FIXING_1 = BUILDER_1.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().buildCurves(NOW);
    BEFORE_FIXING_2 = BUILDER_2.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().buildCurves(NOW);
    BEFORE_FIXING_3 = BUILDER_3.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().buildCurves(NOW);
    AFTER_FIXING_1 = BUILDER_1.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().buildCurves(NOW);
    AFTER_FIXING_2 = BUILDER_2.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().buildCurves(NOW);
    AFTER_FIXING_3 = BUILDER_3.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().buildCurves(NOW);
  }
  /** Calculation tolerance */
  private static final double EPS = 1.0e-9;

  @Override
  @Test
  public void testJacobianSize() {
    // USD + EUR curves using FX and XCCY swaps
    Map<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> jacobian = BEFORE_FIXING_1.getSecond().getData();
    assertEquals(jacobian.size(), 4);
    // USD discounting constructed first
    assertEquals(jacobian.get(CURVE_NAME_DSC_USD).getSecond().getNumberOfRows(), DSC_USD_MARKET_QUOTES.length);
    assertEquals(jacobian.get(CURVE_NAME_DSC_USD).getSecond().getNumberOfColumns(),
        DSC_USD_MARKET_QUOTES.length);
    // USD LIBOR curve next
    assertEquals(jacobian.get(CURVE_NAME_FWD3_USD).getSecond().getNumberOfRows(), FWD3_USD_MARKET_QUOTES.length);
    assertEquals(jacobian.get(CURVE_NAME_FWD3_USD).getSecond().getNumberOfColumns(),
        DSC_USD_MARKET_QUOTES.length + FWD3_USD_MARKET_QUOTES.length);
    // EUR discounting and EURIBOR next
    assertEquals(jacobian.get(CURVE_NAME_DSC_EUR).getSecond().getNumberOfRows(), DSC_EUR_MARKET_QUOTES_1.length);
    assertEquals(jacobian.get(CURVE_NAME_DSC_EUR).getSecond().getNumberOfColumns(),
        DSC_USD_MARKET_QUOTES.length + FWD3_USD_MARKET_QUOTES.length + DSC_EUR_MARKET_QUOTES_1.length + FWD3_EUR_MARKET_QUOTES_1.length);
    assertEquals(jacobian.get(CURVE_NAME_FWD3_EUR).getSecond().getNumberOfRows(), FWD3_EUR_MARKET_QUOTES_1.length);
    assertEquals(jacobian.get(CURVE_NAME_FWD3_EUR).getSecond().getNumberOfColumns(),
        DSC_USD_MARKET_QUOTES.length + FWD3_USD_MARKET_QUOTES.length + DSC_EUR_MARKET_QUOTES_1.length + FWD3_EUR_MARKET_QUOTES_1.length);

    // USD + EUR curves using FX swaps
    jacobian = BEFORE_FIXING_2.getSecond().getData();
    assertEquals(jacobian.size(), 4);
    // USD discounting constructed first
    assertEquals(jacobian.get(CURVE_NAME_DSC_USD).getSecond().getNumberOfRows(), DSC_USD_MARKET_QUOTES.length);
    assertEquals(jacobian.get(CURVE_NAME_DSC_USD).getSecond().getNumberOfColumns(),
        DSC_USD_MARKET_QUOTES.length);
    // USD LIBOR curve next
    assertEquals(jacobian.get(CURVE_NAME_FWD3_USD).getSecond().getNumberOfRows(), FWD3_USD_MARKET_QUOTES.length);
    assertEquals(jacobian.get(CURVE_NAME_FWD3_USD).getSecond().getNumberOfColumns(),
        DSC_USD_MARKET_QUOTES.length + FWD3_USD_MARKET_QUOTES.length);
    // EUR discounting and EURIBOR next
    assertEquals(jacobian.get(CURVE_NAME_DSC_EUR).getSecond().getNumberOfRows(), DSC_EUR_MARKET_QUOTES_2.length);
    assertEquals(jacobian.get(CURVE_NAME_DSC_EUR).getSecond().getNumberOfColumns(),
        DSC_USD_MARKET_QUOTES.length + FWD3_USD_MARKET_QUOTES.length + DSC_EUR_MARKET_QUOTES_2.length + FWD3_EUR_MARKET_QUOTES_2.length);
    assertEquals(jacobian.get(CURVE_NAME_FWD3_EUR).getSecond().getNumberOfRows(), FWD3_EUR_MARKET_QUOTES_2.length);
    assertEquals(jacobian.get(CURVE_NAME_FWD3_EUR).getSecond().getNumberOfColumns(),
        DSC_USD_MARKET_QUOTES.length + FWD3_USD_MARKET_QUOTES.length + DSC_EUR_MARKET_QUOTES_2.length + FWD3_EUR_MARKET_QUOTES_2.length);

    // USD + EUR curves using XCCY swaps
    jacobian = BEFORE_FIXING_3.getSecond().getData();
    assertEquals(jacobian.size(), 4);
    // USD discounting constructed first
    assertEquals(jacobian.get(CURVE_NAME_DSC_USD).getSecond().getNumberOfRows(), DSC_USD_MARKET_QUOTES.length);
    assertEquals(jacobian.get(CURVE_NAME_DSC_USD).getSecond().getNumberOfColumns(),
        DSC_USD_MARKET_QUOTES.length);
    // USD LIBOR curve next
    assertEquals(jacobian.get(CURVE_NAME_FWD3_USD).getSecond().getNumberOfRows(), FWD3_USD_MARKET_QUOTES.length);
    assertEquals(jacobian.get(CURVE_NAME_FWD3_USD).getSecond().getNumberOfColumns(),
        DSC_USD_MARKET_QUOTES.length + FWD3_USD_MARKET_QUOTES.length);
    // EUR discounting and EURIBOR next
    assertEquals(jacobian.get(CURVE_NAME_DSC_EUR).getSecond().getNumberOfRows(), DSC_EUR_MARKET_QUOTES_3.length);
    assertEquals(jacobian.get(CURVE_NAME_DSC_EUR).getSecond().getNumberOfColumns(),
        DSC_USD_MARKET_QUOTES.length + FWD3_USD_MARKET_QUOTES.length + DSC_EUR_MARKET_QUOTES_3.length + FWD3_EUR_MARKET_QUOTES_3.length);
    assertEquals(jacobian.get(CURVE_NAME_FWD3_EUR).getSecond().getNumberOfRows(), FWD3_EUR_MARKET_QUOTES_3.length);
    assertEquals(jacobian.get(CURVE_NAME_FWD3_EUR).getSecond().getNumberOfColumns(),
        DSC_USD_MARKET_QUOTES.length + FWD3_USD_MARKET_QUOTES.length + DSC_EUR_MARKET_QUOTES_3.length + FWD3_EUR_MARKET_QUOTES_3.length);
  }

  @Override
  @Test
  public void testInstrumentsInCurvePriceToZero() {
    testInstrumentsInCurvePriceToZero(BUILDER_1.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder(), BEFORE_FIXING_1.getFirst(), true);
    testInstrumentsInCurvePriceToZero(BUILDER_1.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder(), AFTER_FIXING_1.getFirst(), false);
    testInstrumentsInCurvePriceToZero(BUILDER_2.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder(), BEFORE_FIXING_2.getFirst(), true);
    testInstrumentsInCurvePriceToZero(BUILDER_2.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder(), AFTER_FIXING_2.getFirst(), false);
    testInstrumentsInCurvePriceToZero(BUILDER_3.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder(), BEFORE_FIXING_3.getFirst(), true);
    testInstrumentsInCurvePriceToZero(BUILDER_3.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder(), AFTER_FIXING_3.getFirst(), false);
  }

  /**
   * Tests that all of the instruments used to construct curves price to zero.
   * @param builder  the builder, used to create the instruments
   * @param curves  the curves
   * @param beforeFixing  true if the curves were constructed before today's fixing
   */
  private static void testInstrumentsInCurvePriceToZero(final CurveBuilder<MulticurveProviderDiscount> builder, final MulticurveProviderDiscount curves,
      final boolean beforeFixing) {
    final Map<String, InstrumentDefinition<?>[]> definitions = builder.getDefinitionsForCurves(NOW);
    final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs = beforeFixing ? FIXING_TS_WITHOUT_TODAY : FIXING_TS_WITH_TODAY;
    for (final String curveName : curves.getAllCurveNames()) {
      curveConstructionTest(definitions.get(curveName), curves, PresentValueDiscountingCalculator.getInstance(),
          fixingTs, FX_MATRIX, NOW, Currency.USD);
    }
  }

  @Override
  @Test
  public void testFiniteDifferenceSensitivities() {
    testUsdEurSensitivities1();
    testUsdEurSensitivities2();
    testUsdEurSensitivities3();
  }

  /**
   * Tests the curve sensitivities for the first set of curves.
   */
  private static void testUsdEurSensitivities1() {
    // USD discounting sensitivities to USD discounting
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_1.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_1, CURVE_NAME_DSC_USD,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_1.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_1, CURVE_NAME_DSC_USD,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    // No calculation of USD discounting curve sensitivities to any other curves
    assertNoSensitivities(BEFORE_FIXING_1.getSecond(), CURVE_NAME_DSC_USD, CURVE_NAME_FWD3_USD);
    assertNoSensitivities(AFTER_FIXING_1.getSecond(), CURVE_NAME_DSC_USD, CURVE_NAME_FWD3_USD);
    assertNoSensitivities(BEFORE_FIXING_1.getSecond(), CURVE_NAME_DSC_USD, CURVE_NAME_DSC_EUR);
    assertNoSensitivities(AFTER_FIXING_1.getSecond(), CURVE_NAME_DSC_USD, CURVE_NAME_DSC_EUR);
    assertNoSensitivities(BEFORE_FIXING_1.getSecond(), CURVE_NAME_DSC_USD, CURVE_NAME_FWD3_EUR);
    assertNoSensitivities(AFTER_FIXING_1.getSecond(), CURVE_NAME_DSC_USD, CURVE_NAME_FWD3_EUR);

    // USD LIBOR sensitivities to USD discounting
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_1.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_1, CURVE_NAME_FWD3_USD,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_1.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_1, CURVE_NAME_FWD3_USD,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    // USD LIBOR sensitivities to USD LIBOR
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_1.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_1, CURVE_NAME_FWD3_USD,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_1.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_1, CURVE_NAME_FWD3_USD,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
    // No calculation of USD LIBOR sensitivities to EUR curves
    assertNoSensitivities(BEFORE_FIXING_1.getSecond(), CURVE_NAME_FWD3_USD, CURVE_NAME_DSC_EUR);
    assertNoSensitivities(AFTER_FIXING_1.getSecond(), CURVE_NAME_FWD3_USD, CURVE_NAME_DSC_EUR);
    assertNoSensitivities(BEFORE_FIXING_1.getSecond(), CURVE_NAME_FWD3_USD, CURVE_NAME_FWD3_EUR);
    assertNoSensitivities(AFTER_FIXING_1.getSecond(), CURVE_NAME_FWD3_USD, CURVE_NAME_FWD3_EUR);

    // EUR discounting sensitivities to USD curves are non-zero because of the cross-currency instruments
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_1.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_1, CURVE_NAME_DSC_EUR,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_1.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_1, CURVE_NAME_DSC_EUR,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_1.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_1, CURVE_NAME_DSC_EUR,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_1.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_1, CURVE_NAME_DSC_EUR,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
    // EUR discounting sensitivities to EUR discounting
//    assertFiniteDifferenceSensitivities(BEFORE_FIXING_1.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_1, CURVE_NAME_DSC_EUR,
//        CURVE_NAME_DSC_EUR, NOW, DSC_EUR_GENERATORS, DSC_EUR_ATTR, DSC_EUR_MARKET_QUOTES, false);
//    assertFiniteDifferenceSensitivities(AFTER_FIXING_1.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_1, CURVE_NAME_DSC_EUR,
//        CURVE_NAME_DSC_EUR, NOW, DSC_EUR_GENERATORS, DSC_EUR_ATTR, DSC_EUR_MARKET_QUOTES, false);
    // EUR discounting sensitivities to EURIBOR are non-zero because of XCCY LIBOR / EURIBOR swaps used in EUR discounting
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_1.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_1, CURVE_NAME_DSC_EUR,
        CURVE_NAME_FWD3_EUR, NOW, FWD3_EUR_GENERATORS_1, FWD3_EUR_ATTR_1, FWD3_EUR_MARKET_QUOTES_1, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_1.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_1, CURVE_NAME_DSC_EUR,
        CURVE_NAME_FWD3_EUR, NOW, FWD3_EUR_GENERATORS_1, FWD3_EUR_ATTR_1, FWD3_EUR_MARKET_QUOTES_1, false);

    // EURIBOR sensitivities to USD curves are non-zero because of the cross-currency instruments in the discounting curve
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_1.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_1, CURVE_NAME_FWD3_EUR,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_1.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_1, CURVE_NAME_FWD3_EUR,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_1.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_1, CURVE_NAME_FWD3_EUR,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_1.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_1, CURVE_NAME_FWD3_EUR,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
    // EURIBOR sensitivities to EUR discounting
//    assertFiniteDifferenceSensitivities(BEFORE_FIXING_1.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_1, CURVE_NAME_FWD3_EUR,
//        CURVE_NAME_DSC_EUR, NOW, DSC_EUR_GENERATORS, DSC_EUR_ATTR, DSC_EUR_MARKET_QUOTES, false);
//    assertFiniteDifferenceSensitivities(AFTER_FIXING_1.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_1, CURVE_NAME_FWD3_EUR,
//        CURVE_NAME_DSC_EUR, NOW, DSC_EUR_GENERATORS, DSC_EUR_ATTR, DSC_EUR_MARKET_QUOTES, false);
    // EURIBOR sensitivities to EURIBOR
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_1.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_1, CURVE_NAME_FWD3_EUR,
        CURVE_NAME_FWD3_EUR, NOW, FWD3_EUR_GENERATORS_1, FWD3_EUR_ATTR_1, FWD3_EUR_MARKET_QUOTES_1, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_1.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_1, CURVE_NAME_FWD3_EUR,
        CURVE_NAME_FWD3_EUR, NOW, FWD3_EUR_GENERATORS_1, FWD3_EUR_ATTR_1, FWD3_EUR_MARKET_QUOTES_1, false);
  }

  /**
   * Tests the curve sensitivities for the second set of curves.
   */
  private static void testUsdEurSensitivities2() {
    // USD discounting sensitivities to USD discounting
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_2.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_2, CURVE_NAME_DSC_USD,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_2.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_2, CURVE_NAME_DSC_USD,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    // No calculation of USD discounting curve sensitivities to any other curves
    assertNoSensitivities(BEFORE_FIXING_2.getSecond(), CURVE_NAME_DSC_USD, CURVE_NAME_FWD3_USD);
    assertNoSensitivities(AFTER_FIXING_2.getSecond(), CURVE_NAME_DSC_USD, CURVE_NAME_FWD3_USD);
    assertNoSensitivities(BEFORE_FIXING_2.getSecond(), CURVE_NAME_DSC_USD, CURVE_NAME_DSC_EUR);
    assertNoSensitivities(AFTER_FIXING_2.getSecond(), CURVE_NAME_DSC_USD, CURVE_NAME_DSC_EUR);
    assertNoSensitivities(BEFORE_FIXING_2.getSecond(), CURVE_NAME_DSC_USD, CURVE_NAME_FWD3_EUR);
    assertNoSensitivities(AFTER_FIXING_2.getSecond(), CURVE_NAME_DSC_USD, CURVE_NAME_FWD3_EUR);

    // USD LIBOR sensitivities to USD discounting
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_2.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_2, CURVE_NAME_FWD3_USD,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_2.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_2, CURVE_NAME_FWD3_USD,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    // USD LIBOR sensitivities to USD LIBOR
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_2.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_2, CURVE_NAME_FWD3_USD,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_2.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_2, CURVE_NAME_FWD3_USD,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
    // No calculation of USD LIBOR sensitivities to EUR curves
    assertNoSensitivities(BEFORE_FIXING_2.getSecond(), CURVE_NAME_FWD3_USD, CURVE_NAME_DSC_EUR);
    assertNoSensitivities(AFTER_FIXING_2.getSecond(), CURVE_NAME_FWD3_USD, CURVE_NAME_DSC_EUR);
    assertNoSensitivities(BEFORE_FIXING_2.getSecond(), CURVE_NAME_FWD3_USD, CURVE_NAME_FWD3_EUR);
    assertNoSensitivities(AFTER_FIXING_2.getSecond(), CURVE_NAME_FWD3_USD, CURVE_NAME_FWD3_EUR);

    // EUR discounting sensitivities to USD curves are non-zero because of the cross-currency instruments
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_2.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_2, CURVE_NAME_DSC_EUR,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_2.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_2, CURVE_NAME_DSC_EUR,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_2.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_2, CURVE_NAME_DSC_EUR,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_2.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_2, CURVE_NAME_DSC_EUR,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
    // EUR discounting sensitivities to EUR discounting
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_2.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_2, CURVE_NAME_DSC_EUR,
        CURVE_NAME_DSC_EUR, NOW, DSC_EUR_GENERATORS_2, DSC_EUR_ATTR_2, DSC_EUR_MARKET_QUOTES_2, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_2.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_2, CURVE_NAME_DSC_EUR,
        CURVE_NAME_DSC_EUR, NOW, DSC_EUR_GENERATORS_2, DSC_EUR_ATTR_2, DSC_EUR_MARKET_QUOTES_2, false);
    // EUR discounting sensitivities to EURIBOR are non-zero because of XCCY LIBOR / EURIBOR swaps used in EUR discounting
//    assertFiniteDifferenceSensitivities(BEFORE_FIXING_4.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_4, CURVE_NAME_DSC_EUR,
//        CURVE_NAME_FWD3_EUR, NOW, FWD3_EUR_GENERATORS_2, FWD3_EUR_ATTR_2, FWD3_EUR_MARKET_QUOTES_2, false);
//    assertFiniteDifferenceSensitivities(AFTER_FIXING_4.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_4, CURVE_NAME_DSC_EUR,
//        CURVE_NAME_FWD3_EUR, NOW, FWD3_EUR_GENERATORS_2, FWD3_EUR_ATTR_2, FWD3_EUR_MARKET_QUOTES_2, false);

    // EURIBOR sensitivities to USD curves are non-zero because of the cross-currency instruments in the discounting curve
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_2.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_2, CURVE_NAME_FWD3_EUR,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_2.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_2, CURVE_NAME_FWD3_EUR,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_2.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_2, CURVE_NAME_FWD3_EUR,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_2.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_2, CURVE_NAME_FWD3_EUR,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
    // EURIBOR sensitivities to EUR discounting
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_2.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_2, CURVE_NAME_FWD3_EUR,
        CURVE_NAME_DSC_EUR, NOW, DSC_EUR_GENERATORS_2, DSC_EUR_ATTR_2, DSC_EUR_MARKET_QUOTES_2, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_2.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_2, CURVE_NAME_FWD3_EUR,
        CURVE_NAME_DSC_EUR, NOW, DSC_EUR_GENERATORS_2, DSC_EUR_ATTR_2, DSC_EUR_MARKET_QUOTES_2, false);
    // EURIBOR sensitivities to EURIBOR
//    assertFiniteDifferenceSensitivities(BEFORE_FIXING_4.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_4, CURVE_NAME_FWD3_EUR,
//        CURVE_NAME_FWD3_EUR, NOW, FWD3_EUR_GENERATORS_2, FWD3_EUR_ATTR_2, FWD3_EUR_MARKET_QUOTES_2, false);
//    assertFiniteDifferenceSensitivities(AFTER_FIXING_4.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_4, CURVE_NAME_FWD3_EUR,
//        CURVE_NAME_FWD3_EUR, NOW, FWD3_EUR_GENERATORS_2, FWD3_EUR_ATTR_2, FWD3_EUR_MARKET_QUOTES_2, false);
  }

  /**
   * Tests the curve sensitivities for the third set of curves.
   */
  private static void testUsdEurSensitivities3() {
    // USD discounting sensitivities to USD discounting
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_3.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_3, CURVE_NAME_DSC_USD,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_3.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_3, CURVE_NAME_DSC_USD,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    // No calculation of USD discounting curve sensitivities to any other curves
    assertNoSensitivities(BEFORE_FIXING_3.getSecond(), CURVE_NAME_DSC_USD, CURVE_NAME_FWD3_USD);
    assertNoSensitivities(AFTER_FIXING_3.getSecond(), CURVE_NAME_DSC_USD, CURVE_NAME_FWD3_USD);
    assertNoSensitivities(BEFORE_FIXING_3.getSecond(), CURVE_NAME_DSC_USD, CURVE_NAME_DSC_EUR);
    assertNoSensitivities(AFTER_FIXING_3.getSecond(), CURVE_NAME_DSC_USD, CURVE_NAME_DSC_EUR);
    assertNoSensitivities(BEFORE_FIXING_3.getSecond(), CURVE_NAME_DSC_USD, CURVE_NAME_FWD3_EUR);
    assertNoSensitivities(AFTER_FIXING_3.getSecond(), CURVE_NAME_DSC_USD, CURVE_NAME_FWD3_EUR);

    // USD LIBOR sensitivities to USD discounting
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_3.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_3, CURVE_NAME_FWD3_USD,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_3.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_3, CURVE_NAME_FWD3_USD,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    // USD LIBOR sensitivities to USD LIBOR
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_3.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_3, CURVE_NAME_FWD3_USD,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_3.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_3, CURVE_NAME_FWD3_USD,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
    // No calculation of USD LIBOR sensitivities to EUR curves
    assertNoSensitivities(BEFORE_FIXING_3.getSecond(), CURVE_NAME_FWD3_USD, CURVE_NAME_DSC_EUR);
    assertNoSensitivities(AFTER_FIXING_3.getSecond(), CURVE_NAME_FWD3_USD, CURVE_NAME_DSC_EUR);
    assertNoSensitivities(BEFORE_FIXING_3.getSecond(), CURVE_NAME_FWD3_USD, CURVE_NAME_FWD3_EUR);
    assertNoSensitivities(AFTER_FIXING_3.getSecond(), CURVE_NAME_FWD3_USD, CURVE_NAME_FWD3_EUR);

    // EUR discounting sensitivities to USD curves are non-zero because of the cross-currency instruments
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_3.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_3, CURVE_NAME_DSC_EUR,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_3.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_3, CURVE_NAME_DSC_EUR,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_3.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_3, CURVE_NAME_DSC_EUR,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_3.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_3, CURVE_NAME_DSC_EUR,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
    // EUR discounting sensitivities to EUR discounting
//    assertFiniteDifferenceSensitivities(BEFORE_FIXING_5.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_5, CURVE_NAME_DSC_EUR,
//        CURVE_NAME_DSC_EUR, NOW, DSC_EUR_GENERATORS_3, DSC_EUR_ATTR_3, DSC_EUR_MARKET_QUOTES_3, false);
//    assertFiniteDifferenceSensitivities(AFTER_FIXING_5.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_5, CURVE_NAME_DSC_EUR,
//        CURVE_NAME_DSC_EUR, NOW, DSC_EUR_GENERATORS_3, DSC_EUR_ATTR_3, DSC_EUR_MARKET_QUOTES_3, false);
    // EUR discounting sensitivities to EURIBOR are non-zero because of XCCY LIBOR / EURIBOR swaps used in EUR discounting
//    assertFiniteDifferenceSensitivities(BEFORE_FIXING_5.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_5, CURVE_NAME_DSC_EUR,
//        CURVE_NAME_FWD3_EUR, NOW, FWD3_EUR_GENERATORS_3, FWD3_EUR_ATTR_3, FWD3_EUR_MARKET_QUOTES_3, false);
//    assertFiniteDifferenceSensitivities(AFTER_FIXING_5.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_5, CURVE_NAME_DSC_EUR,
//        CURVE_NAME_FWD3_EUR, NOW, FWD3_EUR_GENERATORS_3, FWD3_EUR_ATTR_3, FWD3_EUR_MARKET_QUOTES_3, false);

    // EURIBOR sensitivities to USD curves are non-zero because of the cross-currency instruments in the discounting curve
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_3.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_3, CURVE_NAME_FWD3_EUR,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_3.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_3, CURVE_NAME_FWD3_EUR,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_3.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_3, CURVE_NAME_FWD3_EUR,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_3.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_3, CURVE_NAME_FWD3_EUR,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
    // EURIBOR sensitivities to EUR discounting
//    assertFiniteDifferenceSensitivities(BEFORE_FIXING_5.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_5, CURVE_NAME_FWD3_EUR,
//        CURVE_NAME_DSC_EUR, NOW, DSC_EUR_GENERATORS_3, DSC_EUR_ATTR_3, DSC_EUR_MARKET_QUOTES_3, false);
//    assertFiniteDifferenceSensitivities(AFTER_FIXING_5.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_5, CURVE_NAME_FWD3_EUR,
//        CURVE_NAME_DSC_EUR, NOW, DSC_EUR_GENERATORS_3, DSC_EUR_ATTR_3, DSC_EUR_MARKET_QUOTES_3, false);
//    // EURIBOR sensitivities to EURIBOR
//    assertFiniteDifferenceSensitivities(BEFORE_FIXING_5.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_5, CURVE_NAME_FWD3_EUR,
//        CURVE_NAME_FWD3_EUR, NOW, FWD3_EUR_GENERATORS_3, FWD3_EUR_ATTR_3, FWD3_EUR_MARKET_QUOTES_3, false);
//    assertFiniteDifferenceSensitivities(AFTER_FIXING_5.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_5, CURVE_NAME_FWD3_EUR,
//        CURVE_NAME_FWD3_EUR, NOW, FWD3_EUR_GENERATORS_3, FWD3_EUR_ATTR_3, FWD3_EUR_MARKET_QUOTES_3, false);
  }

  @Override
  @Test
  public void testSameCurvesDifferentMethods() {
    // USD discounting curves all use the same data and nodes
    assertYieldCurvesEqual(BEFORE_FIXING_1.getFirst().getCurve(Currency.USD), BEFORE_FIXING_2.getFirst().getCurve(Currency.USD), EPS);
    assertYieldCurvesEqual(BEFORE_FIXING_1.getFirst().getCurve(Currency.USD), BEFORE_FIXING_3.getFirst().getCurve(Currency.USD), EPS);
    assertYieldCurvesEqual(AFTER_FIXING_1.getFirst().getCurve(Currency.USD), AFTER_FIXING_2.getFirst().getCurve(Currency.USD), EPS);
    assertYieldCurvesEqual(AFTER_FIXING_1.getFirst().getCurve(Currency.USD), AFTER_FIXING_3.getFirst().getCurve(Currency.USD), EPS);
    // USD LIBOR curves all use the same data and nodes
    assertYieldCurvesEqual(BEFORE_FIXING_1.getFirst().getCurve(USD_3M_LIBOR_INDEX), BEFORE_FIXING_2.getFirst().getCurve(USD_3M_LIBOR_INDEX), EPS);
    assertYieldCurvesEqual(BEFORE_FIXING_1.getFirst().getCurve(USD_3M_LIBOR_INDEX), BEFORE_FIXING_3.getFirst().getCurve(USD_3M_LIBOR_INDEX), EPS);
    assertYieldCurvesEqual(AFTER_FIXING_1.getFirst().getCurve(USD_3M_LIBOR_INDEX), AFTER_FIXING_2.getFirst().getCurve(USD_3M_LIBOR_INDEX), EPS);
    assertYieldCurvesEqual(AFTER_FIXING_1.getFirst().getCurve(USD_3M_LIBOR_INDEX), AFTER_FIXING_3.getFirst().getCurve(USD_3M_LIBOR_INDEX), EPS);
  }

//  MulticurveBuildingDiscountingDiscountXCcyTest - 10 curve construction / USD/EUR 3 units: 217 ms
//  MulticurveBuildingDiscountingDiscountXCcyTest - 10 curve construction / USD/JPY 3 unit: 361 ms
//  MulticurveBuildingDiscountingDiscountXCcyTest - 10 curve construction / USD/JPY 1 unit: 481 ms
//  MulticurveBuildingDiscountingDiscountXCcyTest - 10 curve construction / USD/EUR 3 units: 332 ms
//  MulticurveBuildingDiscountingDiscountXCcyTest - 10 curve construction / USD/JPY 3 unit: 417 ms
//  MulticurveBuildingDiscountingDiscountXCcyTest - 10 curve construction / USD/JPY 1 unit: 551 ms
  /**
   * Tests the performance.
   */
  @Test(enabled = false)
  public void performance() {
    long startTime, endTime;
    final int nbTest = 10;

    startTime = System.currentTimeMillis();
    DiscountingMethodCurveBuilder builder = BUILDER_1.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      builder.buildCurves(NOW);
    }
    endTime = System.currentTimeMillis();
    System.out.println("MulticurveBuildingDiscountingDiscountXCcyTest - " + nbTest + " curve construction / USD/EUR 3 units: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction USD/EUR 3 units: 06-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 160 ms for 10 sets.

    startTime = System.currentTimeMillis();
    builder = BUILDER_2.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      builder.buildCurves(NOW);
    }
    endTime = System.currentTimeMillis();
    System.out.println("MulticurveBuildingDiscountingDiscountXCcyTest - " + nbTest + " curve construction / USD/JPY 3 unit: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction USD/JPY 3 unit: 06-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 200 ms for 10 sets.

    startTime = System.currentTimeMillis();
    builder = BUILDER_3.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      builder.buildCurves(NOW);
    }
    endTime = System.currentTimeMillis();
    System.out.println("MulticurveBuildingDiscountingDiscountXCcyTest - " + nbTest + " curve construction / USD/JPY 1 unit: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction USD/JPY 1 unit: 06-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 265 ms for 10 sets.

  }

  /**
   * Analyzes the shape of the forward curve.
   */
  @Test(enabled = false)
  public void forwardAnalysis() {
    final MulticurveProviderInterface marketDsc = BEFORE_FIXING_1.getFirst();
    final int jump = 1;
    final int startIndex = 0;
    final int nbDate = 2750;
    ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(NOW, EUR_3M_EURIBOR_INDEX.getSpotLag() + startIndex * jump, TARGET);
    final double[] rateDsc = new double[nbDate];
    final double[] startTime = new double[nbDate];
    try (FileWriter writer = new FileWriter("fwd-dsc.csv")) {
      for (int loopdate = 0; loopdate < nbDate; loopdate++) {
        startTime[loopdate] = TimeCalculator.getTimeBetween(NOW, startDate);
        final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, EUR_3M_EURIBOR_INDEX, TARGET);
        final double endTime = TimeCalculator.getTimeBetween(NOW, endDate);
        final double accrualFactor = EUR_3M_EURIBOR_INDEX.getDayCount().getDayCountFraction(startDate, endDate);
        rateDsc[loopdate] = marketDsc.getSimplyCompoundForwardRate(EUR_3M_EURIBOR_INDEX, startTime[loopdate], endTime, accrualFactor);
        startDate = ScheduleCalculator.getAdjustedDate(startDate, jump, TARGET);
        writer.append(0.0 + "," + startTime[loopdate] + "," + rateDsc[loopdate] + "\n");
      }
      writer.flush();
      writer.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

}
