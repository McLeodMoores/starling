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
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapIborIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapXCcyIborIbor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.Index;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingTests;
import com.opengamma.analytics.financial.provider.curve.builder.CurveBuilder;
import com.opengamma.analytics.financial.provider.curve.builder.DiscountingMethodCurveBuilder;
import com.opengamma.analytics.financial.provider.curve.builder.DiscountingMethodCurveSetUp;
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

@Test(groups = TestGroup.UNIT)
public class UsdJpyDiscountingLiborXCcyTest extends CurveBuildingTests {
  /** The interpolator used for all curves */
  private static final Interpolator1D INTERPOLATOR = NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME);
  /** NYC holidays */
  private static final CalendarAdapter NYC = new CalendarAdapter(WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
  /** Tokyo holidays */
  private static final CalendarAdapter TOKYO = new CalendarAdapter(WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
  /** Spot USD/JPY */
  private static final double FX_USDJPY = 80.0;
  /** The FX matrix */
  private static final FXMatrix FX_MATRIX = new FXMatrix(Currency.USD);
  static {
    FX_MATRIX.addCurrency(Currency.JPY, Currency.USD, 1 / FX_USDJPY);
  }
  /** Generates JPY OIS */
  private static final GeneratorSwapFixedON GENERATOR_OIS_USD = GeneratorSwapFixedONMaster.getInstance().getGenerator("USD1YFEDFUND", NYC);
  /** Generates USD OIS */
  private static final GeneratorSwapFixedON GENERATOR_OIS_JPY = GeneratorSwapFixedONMaster.getInstance().getGenerator("JPY1YTONAR", TOKYO);
  /** Fed funds index */
  private static final IndexON FED_FUNDS_INDEX = GENERATOR_OIS_USD.getIndex();
  /** TONAR index */
  private static final IndexON TONAR_INDEX = GENERATOR_OIS_JPY.getIndex();
  /** Generates the overnight deposit */
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_USD = new GeneratorDepositON("USD Deposit ON", Currency.USD, NYC, FED_FUNDS_INDEX.getDayCount());
  /** Generates the overnight deposit */
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_JPY = new GeneratorDepositON("JPY Deposit ON", Currency.JPY, TOKYO, TONAR_INDEX.getDayCount());
  /** Generates 3m LIBOR / 6m fixed USD swaps */
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("USD6MLIBOR3M", NYC);
  /** Generates 6m LIBOR / 6m fixed JPY swaps */
  private static final GeneratorSwapFixedIbor JPY6MLIBOR6M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("JPY6MLIBOR6M", TOKYO);
  /** The 3M USD LIBOR index */
  private static final IborIndex USD_3M_LIBOR_INDEX = USD6MLIBOR3M.getIborIndex();
  /** The 6M JPY LIBOR index */
  private static final IborIndex JPY_6M_LIBOR_INDEX = JPY6MLIBOR6M.getIborIndex();
  /** The 3M JPY LIBOR index */
  private static final IborIndex JPY_3M_LIBOR_INDEX = IndexIborMaster.getInstance().getIndex("JPYLIBOR3M");
  /** Generates 3m USD FRAs */
  private static final GeneratorFRA GENERATOR_USD_FRA_3M = new GeneratorFRA("GENERATOR USD FRA 3M", USD_3M_LIBOR_INDEX, NYC);
  /** Generates a 3m USD LIBOR deposit rate */
  private static final GeneratorDepositIbor GENERATOR_USDLIBOR3M = new GeneratorDepositIbor("GENERATOR_USDLIBOR3M", USD_3M_LIBOR_INDEX, NYC);
  /** Generates a 3m JPY LIBOR deposit rate */
  private static final GeneratorDepositIbor GENERATOR_JPYLIBOR3M = new GeneratorDepositIbor("GENERATOR_JPYLIBOR3M", JPY_3M_LIBOR_INDEX, TOKYO);
  /** Generates a 6m JPY LIBOR deposit rate */
  private static final GeneratorDepositIbor GENERATOR_JPYLIBOR6M = new GeneratorDepositIbor("GENERATOR_JPYLIBOR3M", JPY_6M_LIBOR_INDEX, TOKYO);
  /** Generates 3m USD LIBOR / 3m JPY LIBOR cross-currency swaps with the spread on the JPY leg */
  private static final GeneratorSwapXCcyIborIbor JPYLIBOR3MUSDLIBOR3M =
      new GeneratorSwapXCcyIborIbor("JPYLIBOR3MUSDLIBOR3M", JPY_3M_LIBOR_INDEX, USD_3M_LIBOR_INDEX, TOKYO, NYC);
  /** Generates 3m LIBOR / 6m LIBOR JPY basis swaps */
  private static final GeneratorSwapIborIbor JPYLIBOR6MLIBOR3M =
      new GeneratorSwapIborIbor("JPYLIBOR6MLIBOR3M", JPY_3M_LIBOR_INDEX, JPY_6M_LIBOR_INDEX, TOKYO, TOKYO);
  /** Generates USD/JPY FX swaps */
  private static final GeneratorForexSwap GENERATOR_FX_USDJPY =
      new GeneratorForexSwap("USDJPY", Currency.USD, Currency.JPY, TOKYO, JPY_3M_LIBOR_INDEX.getSpotLag(), JPY_3M_LIBOR_INDEX.getBusinessDayConvention(), true);
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
  /** USD 3m LIBOR fixing series after today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries USD_3M_LIBOR_TS_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS, NOW}, new double[] {0.0035, 0.0036});
  /** USD 3m LIBOR fixing series before today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries USD_3M_LIBOR_TS_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS}, new double[] {0.0035});
  /** JPY 3m LIBOR fixing series after today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries JPY_3M_LIBOR_TS_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS, NOW}, new double[] {0.0060, 0.0061});
  /** JPY 3m LIBOR fixing series before today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries JPY_3M_LIBOR_TS_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS}, new double[] {0.0060});
  /** JPY 6m LIBOR fixing series after today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries JPY_6M_LIBOR_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS, NOW}, new double[] {0.0060, 0.0061});
  /** JPY 6m LIBOR fixing series before today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries JPY_6M_LIBOR_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS}, new double[] {0.0060});
  /** Fixing time series created before the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITHOUT_TODAY = new HashMap<>();
  /** Fixing time series created after the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITH_TODAY = new HashMap<>();
  static {
    FIXING_TS_WITHOUT_TODAY.put(FED_FUNDS_INDEX, FF_TS_WITHOUT_TODAY);
    FIXING_TS_WITHOUT_TODAY.put(USD_3M_LIBOR_INDEX, USD_3M_LIBOR_TS_WITHOUT_TODAY);
    FIXING_TS_WITHOUT_TODAY.put(JPY_3M_LIBOR_INDEX, JPY_3M_LIBOR_TS_WITHOUT_TODAY);
    FIXING_TS_WITHOUT_TODAY.put(JPY_6M_LIBOR_INDEX, JPY_6M_LIBOR_WITHOUT_TODAY);
    FIXING_TS_WITH_TODAY.put(FED_FUNDS_INDEX, FF_TS_WITH_TODAY);
    FIXING_TS_WITH_TODAY.put(USD_3M_LIBOR_INDEX, USD_3M_LIBOR_TS_WITH_TODAY);
    FIXING_TS_WITH_TODAY.put(JPY_3M_LIBOR_INDEX, JPY_3M_LIBOR_TS_WITH_TODAY);
    FIXING_TS_WITH_TODAY.put(JPY_6M_LIBOR_INDEX, JPY_6M_LIBOR_WITH_TODAY);
  }
  /** USD discounting curve name */
  private static final String CURVE_NAME_DSC_USD = "USD Dsc";
  /** USD 3m LIBOR curve name */
  private static final String CURVE_NAME_FWD3_USD = "USD Fwd 3M";
  /** JPY discounting curve name */
  private static final String CURVE_NAME_DSC_JPY = "JPY Dsc";
  /** JPY 3m LIBOR curve name */
  private static final String CURVE_NAME_FWD3_JPY = "JPY Fwd 3M";
  /** JPY 6m LIBOR curve name */
  private static final String CURVE_NAME_FWD6_JPY = "JPY Fwd 6M";
  /** Already known data - contains only the FX matrix */
  private static final MulticurveProviderDiscount KNOWN_DATA = new MulticurveProviderDiscount(FX_MATRIX);
  /** Builds USD discounting, then USD LIBOR, then three JPY curves */
  private static final DiscountingMethodCurveSetUp BUILDER_1 = DiscountingMethodCurveBuilder.setUp()
      .buildingFirst(CURVE_NAME_DSC_USD)
      .thenBuilding(CURVE_NAME_FWD3_USD)
      .thenBuilding(CURVE_NAME_DSC_JPY, CURVE_NAME_FWD3_JPY, CURVE_NAME_FWD6_JPY)
      .using(CURVE_NAME_DSC_USD).forDiscounting(Currency.USD).forOvernightIndex(FED_FUNDS_INDEX).withInterpolator(INTERPOLATOR)
      .using(CURVE_NAME_FWD3_USD).forIborIndex(USD_3M_LIBOR_INDEX).withInterpolator(INTERPOLATOR)
      .using(CURVE_NAME_DSC_JPY).forDiscounting(Currency.JPY).forOvernightIndex(TONAR_INDEX).withInterpolator(INTERPOLATOR)
      .using(CURVE_NAME_FWD3_JPY).forIborIndex(JPY_3M_LIBOR_INDEX).withInterpolator(INTERPOLATOR)
      .using(CURVE_NAME_FWD6_JPY).forIborIndex(JPY_6M_LIBOR_INDEX).withInterpolator(INTERPOLATOR)
      .withKnownData(KNOWN_DATA);
  /** Builds USD discounting, USD LIBOR and three JPY curves simultaneously */
  private static final DiscountingMethodCurveSetUp BUILDER_2 = DiscountingMethodCurveBuilder.setUp()
      .building(CURVE_NAME_DSC_USD, CURVE_NAME_FWD3_USD, CURVE_NAME_DSC_JPY, CURVE_NAME_FWD3_JPY, CURVE_NAME_FWD6_JPY)
      .using(CURVE_NAME_DSC_USD).forDiscounting(Currency.USD).forOvernightIndex(FED_FUNDS_INDEX).withInterpolator(INTERPOLATOR)
      .using(CURVE_NAME_FWD3_USD).forIborIndex(USD_3M_LIBOR_INDEX).withInterpolator(INTERPOLATOR)
      .using(CURVE_NAME_DSC_JPY).forDiscounting(Currency.JPY).forOvernightIndex(TONAR_INDEX).withInterpolator(INTERPOLATOR)
      .using(CURVE_NAME_FWD3_JPY).forIborIndex(JPY_3M_LIBOR_INDEX).withInterpolator(INTERPOLATOR)
      .using(CURVE_NAME_FWD6_JPY).forIborIndex(JPY_6M_LIBOR_INDEX).withInterpolator(INTERPOLATOR)
      .withKnownData(KNOWN_DATA);
  /** Market values for the USD discounting curve */
  private static final double[] DSC_USD_MARKET_QUOTES =
      new double[] {0.0010, 0.0010, 0.0010, 0.0010, 0.0010, 0.0010, 0.0010, 0.0010, 0.0015, 0.0020, 0.0035, 0.0050, 0.0130 };
  /** Vanilla instrument generators for the USD discounting curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_USD_GENERATORS =
      new GeneratorInstrument<?>[] {GENERATOR_DEPOSIT_ON_USD, GENERATOR_DEPOSIT_ON_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD,
    GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD };
  /** USD discounting curve attributes */
  private static final GeneratorAttributeIR[] DSC_USD_ATTR;
  static {
    final Period[] tenors = new Period[] {Period.ofDays(0), Period.ofDays(1), Period.ofMonths(1), Period.ofMonths(2),
        Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3),
        Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
    DSC_USD_ATTR = new GeneratorAttributeIR[tenors.length];
    for (int i = 0; i < 2; i++) {
      DSC_USD_ATTR[i] = new GeneratorAttributeIR(tenors[i], Period.ZERO);
      BUILDER_1.withNode(CURVE_NAME_DSC_USD, DSC_USD_GENERATORS[i], DSC_USD_ATTR[i], DSC_USD_MARKET_QUOTES[i]);
      BUILDER_2.withNode(CURVE_NAME_DSC_USD, DSC_USD_GENERATORS[i], DSC_USD_ATTR[i], DSC_USD_MARKET_QUOTES[i]);
    }
    for (int i = 2; i < tenors.length; i++) {
      DSC_USD_ATTR[i] = new GeneratorAttributeIR(tenors[i]);
      BUILDER_1.withNode(CURVE_NAME_DSC_USD, DSC_USD_GENERATORS[i], DSC_USD_ATTR[i], DSC_USD_MARKET_QUOTES[i]);
      BUILDER_2.withNode(CURVE_NAME_DSC_USD, DSC_USD_GENERATORS[i], DSC_USD_ATTR[i], DSC_USD_MARKET_QUOTES[i]);
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
    }
  }
  /** Market values for the JPY discounting curve */
  private static final double[] DSC_JPY_MARKET_QUOTES =
      new double[] {0.0005, 0.0005, -0.0004, -0.0008, -0.0012, -0.0024, -0.0036, -0.0048, -0.0030, -0.0040, -0.0040, -0.0045, -0.0050 };
  /** Vanilla instrument generators for the JPY discounting curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_JPY_GENERATORS = new GeneratorInstrument<?>[] {
    GENERATOR_DEPOSIT_ON_JPY, GENERATOR_DEPOSIT_ON_JPY, GENERATOR_FX_USDJPY, GENERATOR_FX_USDJPY, GENERATOR_FX_USDJPY, GENERATOR_FX_USDJPY,
    GENERATOR_FX_USDJPY, GENERATOR_FX_USDJPY, JPYLIBOR3MUSDLIBOR3M, JPYLIBOR3MUSDLIBOR3M, JPYLIBOR3MUSDLIBOR3M, JPYLIBOR3MUSDLIBOR3M,
    JPYLIBOR3MUSDLIBOR3M };
  /** JPY discounting curve attributes */
  private static final GeneratorAttribute[] DSC_JPY_ATTR;
  static {
    final Period[] tenors = new Period[] {Period.ofDays(0), Period.ofDays(1), Period.ofMonths(1), Period.ofMonths(2),
      Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9),
      Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
    DSC_JPY_ATTR = new GeneratorAttribute[tenors.length];
    for (int i = 0; i < 2; i++) {
      DSC_JPY_ATTR[i] = new GeneratorAttributeIR(tenors[i], Period.ZERO);
      BUILDER_1.withNode(CURVE_NAME_DSC_JPY, DSC_JPY_GENERATORS[i], DSC_JPY_ATTR[i], DSC_JPY_MARKET_QUOTES[i]);
      BUILDER_2.withNode(CURVE_NAME_DSC_JPY, DSC_JPY_GENERATORS[i], DSC_JPY_ATTR[i], DSC_JPY_MARKET_QUOTES[i]);
    }
    for (int i = 2; i < tenors.length; i++) {
      DSC_JPY_ATTR[i] = new GeneratorAttributeFX(tenors[i], FX_MATRIX);
      BUILDER_1.withNode(CURVE_NAME_DSC_JPY, DSC_JPY_GENERATORS[i], DSC_JPY_ATTR[i], DSC_JPY_MARKET_QUOTES[i]);
      BUILDER_2.withNode(CURVE_NAME_DSC_JPY, DSC_JPY_GENERATORS[i], DSC_JPY_ATTR[i], DSC_JPY_MARKET_QUOTES[i]);
    }
  }
  /** Market values for the JPY 3m LIBOR curve */
  private static final double[] FWD3_JPY_MARKET_QUOTES = new double[] {0.0020, 0.0010, 0.0010, 0.0010, 0.0010, 0.0015, 0.0015, 0.0015 };
  /** Vanilla instrument generators for the JPY 3m LIBOR curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD3_JPY_GENERATORS = new GeneratorInstrument<?>[] {
    GENERATOR_JPYLIBOR3M, JPYLIBOR6MLIBOR3M, JPYLIBOR6MLIBOR3M, JPYLIBOR6MLIBOR3M, JPYLIBOR6MLIBOR3M, JPYLIBOR6MLIBOR3M, JPYLIBOR6MLIBOR3M,
    JPYLIBOR6MLIBOR3M };
  /** Attributes for the JPY 3m LIBOR curve */
  private static final GeneratorAttributeIR[] FWD3_JPY_ATTR;
  static {
    final Period[] tenors = new Period[] {Period.ofMonths(0), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2),
      Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
      Period.ofYears(10) };
    FWD3_JPY_ATTR = new GeneratorAttributeIR[tenors.length];
    for (int i = 0; i < tenors.length; i++) {
      FWD3_JPY_ATTR[i] = new GeneratorAttributeIR(tenors[i]);
      BUILDER_1.withNode(CURVE_NAME_FWD3_JPY, FWD3_JPY_GENERATORS[i], FWD3_JPY_ATTR[i], FWD3_JPY_MARKET_QUOTES[i]);
      BUILDER_2.withNode(CURVE_NAME_FWD3_JPY, FWD3_JPY_GENERATORS[i], FWD3_JPY_ATTR[i], FWD3_JPY_MARKET_QUOTES[i]);
    }
  }
  /** Market values for the JPY 6m LIBOR curve */
  private static final double[] FWD6_JPY_MARKET_QUOTES = new double[] {0.0035, 0.0035, 0.0035, 0.0040, 0.0040, 0.0040, 0.0075 };
  /** Vanilla instrument generators for the JPY 6m LIBOR curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD6_JPY_GENERATORS = new GeneratorInstrument<?>[] {
    GENERATOR_JPYLIBOR6M, JPY6MLIBOR6M, JPY6MLIBOR6M, JPY6MLIBOR6M, JPY6MLIBOR6M, JPY6MLIBOR6M, JPY6MLIBOR6M};
  /** Attributes for the JPY 6m LIBOR curve */
  private static final GeneratorAttributeIR[] FWD6_JPY_ATTR;
  static {
    final Period[] tenors = new Period[] {Period.ofMonths(0), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3),
      Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
    FWD6_JPY_ATTR = new GeneratorAttributeIR[tenors.length];
    for (int i = 0; i < tenors.length; i++) {
      FWD6_JPY_ATTR[i] = new GeneratorAttributeIR(tenors[i]);
      BUILDER_1.withNode(CURVE_NAME_FWD6_JPY, FWD6_JPY_GENERATORS[i], FWD6_JPY_ATTR[i], FWD6_JPY_MARKET_QUOTES[i]);
      BUILDER_2.withNode(CURVE_NAME_FWD6_JPY, FWD6_JPY_GENERATORS[i], FWD6_JPY_ATTR[i], FWD6_JPY_MARKET_QUOTES[i]);
    }
  }
  /** First set of curves constructed before today's fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> BEFORE_FIXING_1;
  /** Second set of curves constructed before today's fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> BEFORE_FIXING_2;
  /** First set of curves constructed after today's fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> AFTER_FIXING_1;
  /** Second set of curves constructed after today's fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> AFTER_FIXING_2;
  static {
    BEFORE_FIXING_1 = BUILDER_1.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().buildCurves(NOW);
    BEFORE_FIXING_2 = BUILDER_2.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().buildCurves(NOW);
    AFTER_FIXING_1 = BUILDER_1.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().buildCurves(NOW);
    AFTER_FIXING_2 = BUILDER_2.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().buildCurves(NOW);
  }
  /** Calculation tolerance */
  private static final double EPS = 1.0e-9;

  @Override
  @Test
  public void testJacobianSize() {
    // USD + JPY curves
    Map<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> jacobian = BEFORE_FIXING_1.getSecond().getData();
    assertEquals(jacobian.size(), 5);
    // USD discounting constructed first
    assertEquals(jacobian.get(CURVE_NAME_DSC_USD).getSecond().getNumberOfRows(), DSC_USD_MARKET_QUOTES.length);
    assertEquals(jacobian.get(CURVE_NAME_DSC_USD).getSecond().getNumberOfColumns(),
        DSC_USD_MARKET_QUOTES.length);
    // USD LIBOR curve next
    assertEquals(jacobian.get(CURVE_NAME_FWD3_USD).getSecond().getNumberOfRows(), FWD3_USD_MARKET_QUOTES.length);
    assertEquals(jacobian.get(CURVE_NAME_FWD3_USD).getSecond().getNumberOfColumns(),
        DSC_USD_MARKET_QUOTES.length + FWD3_USD_MARKET_QUOTES.length);
    // JPY discounting and 2 LIBOR curves next
    assertEquals(jacobian.get(CURVE_NAME_DSC_JPY).getSecond().getNumberOfRows(), DSC_JPY_MARKET_QUOTES.length);
    assertEquals(jacobian.get(CURVE_NAME_DSC_JPY).getSecond().getNumberOfColumns(),
        DSC_USD_MARKET_QUOTES.length + FWD3_USD_MARKET_QUOTES.length + DSC_JPY_MARKET_QUOTES.length + FWD3_JPY_MARKET_QUOTES.length
          + FWD6_JPY_MARKET_QUOTES.length);
    assertEquals(jacobian.get(CURVE_NAME_FWD3_JPY).getSecond().getNumberOfRows(), FWD3_JPY_MARKET_QUOTES.length);
    assertEquals(jacobian.get(CURVE_NAME_FWD3_JPY).getSecond().getNumberOfColumns(),
        DSC_USD_MARKET_QUOTES.length + FWD3_USD_MARKET_QUOTES.length + DSC_JPY_MARKET_QUOTES.length + FWD3_JPY_MARKET_QUOTES.length
          + FWD6_JPY_MARKET_QUOTES.length);
    assertEquals(jacobian.get(CURVE_NAME_FWD6_JPY).getSecond().getNumberOfRows(), FWD6_JPY_MARKET_QUOTES.length);
    assertEquals(jacobian.get(CURVE_NAME_FWD6_JPY).getSecond().getNumberOfColumns(),
        DSC_USD_MARKET_QUOTES.length + FWD3_USD_MARKET_QUOTES.length + DSC_JPY_MARKET_QUOTES.length + FWD3_JPY_MARKET_QUOTES.length
          + FWD6_JPY_MARKET_QUOTES.length);

    // USD + JPY curves
    jacobian = BEFORE_FIXING_2.getSecond().getData();
    assertEquals(jacobian.size(), 5);
    // all curves constructed at the same time
    assertEquals(jacobian.get(CURVE_NAME_DSC_USD).getSecond().getNumberOfRows(), DSC_USD_MARKET_QUOTES.length);
    assertEquals(jacobian.get(CURVE_NAME_DSC_USD).getSecond().getNumberOfColumns(),
        DSC_USD_MARKET_QUOTES.length + FWD3_USD_MARKET_QUOTES.length + DSC_JPY_MARKET_QUOTES.length + FWD3_JPY_MARKET_QUOTES.length
        + FWD6_JPY_MARKET_QUOTES.length);
    assertEquals(jacobian.get(CURVE_NAME_FWD3_USD).getSecond().getNumberOfRows(), FWD3_USD_MARKET_QUOTES.length);
    assertEquals(jacobian.get(CURVE_NAME_FWD3_USD).getSecond().getNumberOfColumns(),
        DSC_USD_MARKET_QUOTES.length + FWD3_USD_MARKET_QUOTES.length + DSC_JPY_MARKET_QUOTES.length + FWD3_JPY_MARKET_QUOTES.length
        + FWD6_JPY_MARKET_QUOTES.length);
    assertEquals(jacobian.get(CURVE_NAME_DSC_JPY).getSecond().getNumberOfRows(), DSC_JPY_MARKET_QUOTES.length);
    assertEquals(jacobian.get(CURVE_NAME_DSC_JPY).getSecond().getNumberOfColumns(),
        DSC_USD_MARKET_QUOTES.length + FWD3_USD_MARKET_QUOTES.length + DSC_JPY_MARKET_QUOTES.length + FWD3_JPY_MARKET_QUOTES.length
        + FWD6_JPY_MARKET_QUOTES.length);
    assertEquals(jacobian.get(CURVE_NAME_FWD3_JPY).getSecond().getNumberOfRows(), FWD3_JPY_MARKET_QUOTES.length);
    assertEquals(jacobian.get(CURVE_NAME_FWD3_JPY).getSecond().getNumberOfColumns(),
        DSC_USD_MARKET_QUOTES.length + FWD3_USD_MARKET_QUOTES.length + DSC_JPY_MARKET_QUOTES.length + FWD3_JPY_MARKET_QUOTES.length
        + FWD6_JPY_MARKET_QUOTES.length);
    assertEquals(jacobian.get(CURVE_NAME_FWD6_JPY).getSecond().getNumberOfRows(), FWD6_JPY_MARKET_QUOTES.length);
    assertEquals(jacobian.get(CURVE_NAME_FWD6_JPY).getSecond().getNumberOfColumns(),
        DSC_USD_MARKET_QUOTES.length + FWD3_USD_MARKET_QUOTES.length + DSC_JPY_MARKET_QUOTES.length + FWD3_JPY_MARKET_QUOTES.length
        + FWD6_JPY_MARKET_QUOTES.length);
  }

  @Override
  @Test
  public void testInstrumentsInCurvePriceToZero() {
    testInstrumentsInCurvePriceToZero(BUILDER_1.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder(), BEFORE_FIXING_1.getFirst(), true);
    testInstrumentsInCurvePriceToZero(BUILDER_1.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder(), AFTER_FIXING_1.getFirst(), false);
    testInstrumentsInCurvePriceToZero(BUILDER_2.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder(), BEFORE_FIXING_2.getFirst(), true);
    testInstrumentsInCurvePriceToZero(BUILDER_2.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder(), AFTER_FIXING_2.getFirst(), false);
  }

  /**
   * Tests that each curve in the bundle prices the instruments used to construct it to zero.
   * @param builder  the builder
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
    testUsdJpySensitivities1();
    testUsdJpySensitivities2();
  }

  /**
   * Tests the sensitivities when the USD curves are constructed before the JPY curves.
   */
  private static void testUsdJpySensitivities1() {
    // USD discounting sensitivities to USD discounting
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_1.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_1, CURVE_NAME_DSC_USD,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_1.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_1, CURVE_NAME_DSC_USD,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    // No calculation of USD discounting curve sensitivities to any other curves
    assertNoSensitivities(BEFORE_FIXING_1.getSecond(), CURVE_NAME_DSC_USD, CURVE_NAME_FWD3_USD);
    assertNoSensitivities(AFTER_FIXING_1.getSecond(), CURVE_NAME_DSC_USD, CURVE_NAME_FWD3_USD);
    assertNoSensitivities(BEFORE_FIXING_1.getSecond(), CURVE_NAME_DSC_USD, CURVE_NAME_DSC_JPY);
    assertNoSensitivities(AFTER_FIXING_1.getSecond(), CURVE_NAME_DSC_USD, CURVE_NAME_DSC_JPY);
    assertNoSensitivities(BEFORE_FIXING_1.getSecond(), CURVE_NAME_DSC_USD, CURVE_NAME_FWD3_JPY);
    assertNoSensitivities(AFTER_FIXING_1.getSecond(), CURVE_NAME_DSC_USD, CURVE_NAME_FWD3_JPY);
    assertNoSensitivities(BEFORE_FIXING_1.getSecond(), CURVE_NAME_DSC_USD, CURVE_NAME_FWD6_JPY);
    assertNoSensitivities(AFTER_FIXING_1.getSecond(), CURVE_NAME_DSC_USD, CURVE_NAME_FWD6_JPY);

    // USD LIBOR sensitivities to USD curves
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_1.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_1, CURVE_NAME_FWD3_USD,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_1.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_1, CURVE_NAME_FWD3_USD,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_1.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_1, CURVE_NAME_FWD3_USD,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_1.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_1, CURVE_NAME_FWD3_USD,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
    // No calculation of sensitivities to JPY curves
    assertNoSensitivities(BEFORE_FIXING_1.getSecond(), CURVE_NAME_FWD3_USD, CURVE_NAME_DSC_JPY);
    assertNoSensitivities(AFTER_FIXING_1.getSecond(), CURVE_NAME_FWD3_USD, CURVE_NAME_DSC_JPY);
    assertNoSensitivities(BEFORE_FIXING_1.getSecond(), CURVE_NAME_FWD3_USD, CURVE_NAME_FWD3_JPY);
    assertNoSensitivities(AFTER_FIXING_1.getSecond(), CURVE_NAME_FWD3_USD, CURVE_NAME_FWD3_JPY);
    assertNoSensitivities(BEFORE_FIXING_1.getSecond(), CURVE_NAME_FWD3_USD, CURVE_NAME_FWD6_JPY);
    assertNoSensitivities(AFTER_FIXING_1.getSecond(), CURVE_NAME_FWD3_USD, CURVE_NAME_FWD6_JPY);

    // JPY discounting sensitivities to USD curves are non-zero because of cross-currency instruments
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_1.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_1, CURVE_NAME_DSC_JPY,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_1.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_1, CURVE_NAME_DSC_JPY,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_1.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_1, CURVE_NAME_DSC_JPY,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_1.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_1, CURVE_NAME_DSC_JPY,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
    // JPY discounting sensitivities to JPY discounting
//    assertFiniteDifferenceSensitivities(BEFORE_FIXING_2.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_2, CURVE_NAME_DSC_JPY,
//        CURVE_NAME_DSC_JPY, NOW, DSC_JPY_GENERATORS, DSC_JPY_ATTR, DSC_JPY_MARKET_QUOTES, false);
//    assertFiniteDifferenceSensitivities(AFTER_FIXING_2.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_2, CURVE_NAME_DSC_JPY,
//        CURVE_NAME_DSC_JPY, NOW, DSC_JPY_GENERATORS, DSC_JPY_ATTR, DSC_JPY_MARKET_QUOTES, false);
    // JPY discounting sensitivities to JPY 3m LIBOR are non-zero because of XCCY LIBOR / LIBOR swaps used in JPY discounting
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_1.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_1, CURVE_NAME_DSC_JPY,
        CURVE_NAME_FWD3_JPY, NOW, FWD3_JPY_GENERATORS, FWD3_JPY_ATTR, FWD3_JPY_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_1.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_1, CURVE_NAME_DSC_JPY,
        CURVE_NAME_FWD3_JPY, NOW, FWD3_JPY_GENERATORS, FWD3_JPY_ATTR, FWD3_JPY_MARKET_QUOTES, false);
    // JPY discounting sensitivities to JPY 6m LIBOR are non-zero because 3m / 6m basis swaps are used in the 3m LIBOR curve
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_1.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_1, CURVE_NAME_DSC_JPY,
        CURVE_NAME_FWD6_JPY, NOW, FWD6_JPY_GENERATORS, FWD6_JPY_ATTR, FWD6_JPY_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_1.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_1, CURVE_NAME_DSC_JPY,
        CURVE_NAME_FWD6_JPY, NOW, FWD6_JPY_GENERATORS, FWD6_JPY_ATTR, FWD6_JPY_MARKET_QUOTES, false);

    // JPY 3m LIBOR sensitivities are non-zero because of cross-currency instruments
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_1.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_1, CURVE_NAME_FWD3_JPY,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_1.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_1, CURVE_NAME_FWD3_JPY,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_1.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_1, CURVE_NAME_FWD3_JPY,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_1.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_1, CURVE_NAME_FWD3_JPY,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
    // JPY 3m LIBOR to JPY discounting
//    assertFiniteDifferenceSensitivities(BEFORE_FIXING_2.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_2, CURVE_NAME_FWD3_JPY,
//        CURVE_NAME_DSC_JPY, NOW, DSC_JPY_GENERATORS, DSC_JPY_ATTR, DSC_JPY_MARKET_QUOTES, false);
//    assertFiniteDifferenceSensitivities(AFTER_FIXING_2.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_2, CURVE_NAME_FWD3_JPY,
//        CURVE_NAME_DSC_JPY, NOW, DSC_JPY_GENERATORS, DSC_JPY_ATTR, DSC_JPY_MARKET_QUOTES, false);
    // JPY 3m LIBOR sensitivities to JPY 3m LIBOR
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_1.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_1, CURVE_NAME_FWD3_JPY,
        CURVE_NAME_FWD3_JPY, NOW, FWD3_JPY_GENERATORS, FWD3_JPY_ATTR, FWD3_JPY_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_1.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_1, CURVE_NAME_FWD3_JPY,
        CURVE_NAME_FWD3_JPY, NOW, FWD3_JPY_GENERATORS, FWD3_JPY_ATTR, FWD3_JPY_MARKET_QUOTES, false);
    // JPY 3m LIBOR sensitivities to JPY 6m LIBOR are non-zero because 3m / 6m basis swaps are used in the 3m LIBOR curve
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_1.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_1, CURVE_NAME_FWD3_JPY,
        CURVE_NAME_FWD6_JPY, NOW, FWD6_JPY_GENERATORS, FWD6_JPY_ATTR, FWD6_JPY_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_1.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_1, CURVE_NAME_FWD3_JPY,
        CURVE_NAME_FWD6_JPY, NOW, FWD6_JPY_GENERATORS, FWD6_JPY_ATTR, FWD6_JPY_MARKET_QUOTES, false);
  }

  /**
   * Tests the sensitivities when the USD curves and JPY curves are constructed at the same time.
   */
  private static void testUsdJpySensitivities2() {
    // USD discounting sensitivities to USD discounting
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_2.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_2, CURVE_NAME_DSC_USD,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_2.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_2, CURVE_NAME_DSC_USD,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    // USD discounting sensitivities to all other curves should be zero
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_2.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_2, CURVE_NAME_DSC_USD,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, true);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_2.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_2, CURVE_NAME_DSC_USD,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, true);
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_2.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_2, CURVE_NAME_DSC_USD,
        CURVE_NAME_DSC_JPY, NOW, DSC_JPY_GENERATORS, DSC_JPY_ATTR, DSC_JPY_MARKET_QUOTES, true);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_2.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_2, CURVE_NAME_DSC_USD,
        CURVE_NAME_DSC_JPY, NOW, DSC_JPY_GENERATORS, DSC_JPY_ATTR, DSC_JPY_MARKET_QUOTES, true);
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_2.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_2, CURVE_NAME_DSC_USD,
        CURVE_NAME_FWD3_JPY, NOW, FWD3_JPY_GENERATORS, FWD3_JPY_ATTR, FWD3_JPY_MARKET_QUOTES, true);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_2.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_2, CURVE_NAME_DSC_USD,
        CURVE_NAME_FWD3_JPY, NOW, FWD3_JPY_GENERATORS, FWD3_JPY_ATTR, FWD3_JPY_MARKET_QUOTES, true);
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_2.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_2, CURVE_NAME_DSC_USD,
        CURVE_NAME_FWD6_JPY, NOW, FWD6_JPY_GENERATORS, FWD6_JPY_ATTR, FWD6_JPY_MARKET_QUOTES, true);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_2.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_2, CURVE_NAME_DSC_USD,
        CURVE_NAME_FWD6_JPY, NOW, FWD6_JPY_GENERATORS, FWD6_JPY_ATTR, FWD6_JPY_MARKET_QUOTES, true);

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
    // USD LIBOR sensitivities to JPY curves should be zero
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_2.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_2, CURVE_NAME_FWD3_USD,
        CURVE_NAME_DSC_JPY, NOW, DSC_JPY_GENERATORS, DSC_JPY_ATTR, DSC_JPY_MARKET_QUOTES, true);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_2.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_2, CURVE_NAME_FWD3_USD,
        CURVE_NAME_DSC_JPY, NOW, DSC_JPY_GENERATORS, DSC_JPY_ATTR, DSC_JPY_MARKET_QUOTES, true);
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_2.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_2, CURVE_NAME_FWD3_USD,
        CURVE_NAME_FWD3_JPY, NOW, FWD3_JPY_GENERATORS, FWD3_JPY_ATTR, FWD3_JPY_MARKET_QUOTES, true);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_2.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_2, CURVE_NAME_FWD3_USD,
        CURVE_NAME_FWD3_JPY, NOW, FWD3_JPY_GENERATORS, FWD3_JPY_ATTR, FWD3_JPY_MARKET_QUOTES, true);
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_2.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_2, CURVE_NAME_FWD3_USD,
        CURVE_NAME_FWD6_JPY, NOW, FWD6_JPY_GENERATORS, FWD6_JPY_ATTR, FWD6_JPY_MARKET_QUOTES, true);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_2.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_2, CURVE_NAME_FWD3_USD,
        CURVE_NAME_FWD6_JPY, NOW, FWD6_JPY_GENERATORS, FWD6_JPY_ATTR, FWD6_JPY_MARKET_QUOTES, true);

    // JPY discounting has sensitivities to all curves
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_2.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_2, CURVE_NAME_DSC_JPY,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_2.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_2, CURVE_NAME_DSC_JPY,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_2.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_2, CURVE_NAME_DSC_JPY,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_2.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_2, CURVE_NAME_DSC_JPY,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
//    assertFiniteDifferenceSensitivities(BEFORE_FIXING_3.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_3, CURVE_NAME_DSC_JPY,
//        CURVE_NAME_DSC_JPY, NOW, DSC_JPY_GENERATORS, DSC_JPY_ATTR, DSC_JPY_MARKET_QUOTES, false);
//    assertFiniteDifferenceSensitivities(AFTER_FIXING_3.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_3, CURVE_NAME_DSC_JPY,
//        CURVE_NAME_DSC_JPY, NOW, DSC_JPY_GENERATORS, DSC_JPY_ATTR, DSC_JPY_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_2.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_2, CURVE_NAME_DSC_JPY,
        CURVE_NAME_FWD3_JPY, NOW, FWD3_JPY_GENERATORS, FWD3_JPY_ATTR, FWD3_JPY_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_2.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_2, CURVE_NAME_DSC_JPY,
        CURVE_NAME_FWD3_JPY, NOW, FWD3_JPY_GENERATORS, FWD3_JPY_ATTR, FWD3_JPY_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_2.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_2, CURVE_NAME_DSC_JPY,
        CURVE_NAME_FWD6_JPY, NOW, FWD6_JPY_GENERATORS, FWD6_JPY_ATTR, FWD6_JPY_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_2.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_2, CURVE_NAME_DSC_JPY,
        CURVE_NAME_FWD6_JPY, NOW, FWD6_JPY_GENERATORS, FWD6_JPY_ATTR, FWD6_JPY_MARKET_QUOTES, false);

    // JPY 3m LIBOR has sensitivities to all curves
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_2.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_2, CURVE_NAME_FWD3_JPY,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_2.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_2, CURVE_NAME_FWD3_JPY,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_2.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_2, CURVE_NAME_FWD3_JPY,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_2.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_2, CURVE_NAME_FWD3_JPY,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
//    assertFiniteDifferenceSensitivities(BEFORE_FIXING_3.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_3, CURVE_NAME_FWD3_JPY,
//        CURVE_NAME_DSC_JPY, NOW, DSC_JPY_GENERATORS, DSC_JPY_ATTR, DSC_JPY_MARKET_QUOTES, false);
//    assertFiniteDifferenceSensitivities(AFTER_FIXING_3.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_3, CURVE_NAME_FWD3_JPY,
//        CURVE_NAME_DSC_JPY, NOW, DSC_JPY_GENERATORS, DSC_JPY_ATTR, DSC_JPY_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_2.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_2, CURVE_NAME_FWD3_JPY,
        CURVE_NAME_FWD3_JPY, NOW, FWD3_JPY_GENERATORS, FWD3_JPY_ATTR, FWD3_JPY_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_2.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_2, CURVE_NAME_FWD3_JPY,
        CURVE_NAME_FWD3_JPY, NOW, FWD3_JPY_GENERATORS, FWD3_JPY_ATTR, FWD3_JPY_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_2.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_2, CURVE_NAME_FWD3_JPY,
        CURVE_NAME_FWD6_JPY, NOW, FWD6_JPY_GENERATORS, FWD6_JPY_ATTR, FWD6_JPY_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_2.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_2, CURVE_NAME_FWD3_JPY,
        CURVE_NAME_FWD6_JPY, NOW, FWD6_JPY_GENERATORS, FWD6_JPY_ATTR, FWD6_JPY_MARKET_QUOTES, false);

    // JPY 6m LIBOR has sensitivities to all curves
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_2.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_2, CURVE_NAME_FWD6_JPY,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_2.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_2, CURVE_NAME_FWD6_JPY,
        CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS, DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_2.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_2, CURVE_NAME_FWD6_JPY,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_2.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_2, CURVE_NAME_FWD6_JPY,
        CURVE_NAME_FWD3_USD, NOW, FWD3_USD_GENERATORS, FWD3_USD_ATTR, FWD3_USD_MARKET_QUOTES, false);
//    assertFiniteDifferenceSensitivities(BEFORE_FIXING_3.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_3, CURVE_NAME_FWD6_JPY,
//        CURVE_NAME_DSC_JPY, NOW, DSC_JPY_GENERATORS, DSC_JPY_ATTR, DSC_JPY_MARKET_QUOTES, false);
//    assertFiniteDifferenceSensitivities(AFTER_FIXING_3.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_3, CURVE_NAME_FWD6_JPY,
//        CURVE_NAME_DSC_JPY, NOW, DSC_JPY_GENERATORS, DSC_JPY_ATTR, DSC_JPY_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_2.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_2, CURVE_NAME_FWD6_JPY,
        CURVE_NAME_FWD3_JPY, NOW, FWD3_JPY_GENERATORS, FWD3_JPY_ATTR, FWD3_JPY_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_2.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_2, CURVE_NAME_FWD6_JPY,
        CURVE_NAME_FWD3_JPY, NOW, FWD3_JPY_GENERATORS, FWD3_JPY_ATTR, FWD3_JPY_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(BEFORE_FIXING_2.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER_2, CURVE_NAME_FWD6_JPY,
        CURVE_NAME_FWD6_JPY, NOW, FWD6_JPY_GENERATORS, FWD6_JPY_ATTR, FWD6_JPY_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING_2.getSecond(), FIXING_TS_WITH_TODAY, BUILDER_2, CURVE_NAME_FWD6_JPY,
        CURVE_NAME_FWD6_JPY, NOW, FWD6_JPY_GENERATORS, FWD6_JPY_ATTR, FWD6_JPY_MARKET_QUOTES, false);
  }

  @Override
  @Test
  public void testSameCurvesDifferentMethods() {
    // USD discounting curves all use the same data and nodes
    assertYieldCurvesEqual(BEFORE_FIXING_1.getFirst().getCurve(Currency.USD), BEFORE_FIXING_2.getFirst().getCurve(Currency.USD), EPS);
    assertYieldCurvesEqual(AFTER_FIXING_1.getFirst().getCurve(Currency.USD), AFTER_FIXING_2.getFirst().getCurve(Currency.USD), EPS);
    // USD LIBOR curves all use the same data and nodes
    assertYieldCurvesEqual(BEFORE_FIXING_1.getFirst().getCurve(USD_3M_LIBOR_INDEX), BEFORE_FIXING_2.getFirst().getCurve(USD_3M_LIBOR_INDEX), EPS);
    assertYieldCurvesEqual(AFTER_FIXING_1.getFirst().getCurve(USD_3M_LIBOR_INDEX), AFTER_FIXING_2.getFirst().getCurve(USD_3M_LIBOR_INDEX), EPS);
    // JPY discounting curves all use the same data and nodes
    assertYieldCurvesEqual(BEFORE_FIXING_1.getFirst().getCurve(Currency.JPY), BEFORE_FIXING_2.getFirst().getCurve(Currency.JPY), EPS);
    assertYieldCurvesEqual(AFTER_FIXING_1.getFirst().getCurve(Currency.JPY), AFTER_FIXING_2.getFirst().getCurve(Currency.JPY), EPS);
    // JPY 3m LIBOR curves all use the same data and nodes
    assertYieldCurvesEqual(BEFORE_FIXING_1.getFirst().getCurve(JPY_3M_LIBOR_INDEX), BEFORE_FIXING_2.getFirst().getCurve(JPY_3M_LIBOR_INDEX), EPS);
    assertYieldCurvesEqual(AFTER_FIXING_1.getFirst().getCurve(JPY_3M_LIBOR_INDEX), AFTER_FIXING_2.getFirst().getCurve(JPY_3M_LIBOR_INDEX), EPS);
    // JPY 6m LIBOR curves all use the same data and nodes
    assertYieldCurvesEqual(BEFORE_FIXING_1.getFirst().getCurve(JPY_6M_LIBOR_INDEX), BEFORE_FIXING_2.getFirst().getCurve(JPY_6M_LIBOR_INDEX), EPS);
    assertYieldCurvesEqual(AFTER_FIXING_1.getFirst().getCurve(JPY_6M_LIBOR_INDEX), AFTER_FIXING_2.getFirst().getCurve(JPY_6M_LIBOR_INDEX), EPS);
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
    System.out.println("MulticurveBuildingDiscountingDiscountXCcyTest - " + nbTest + " curve construction / USD/JPY 3 units: " + (endTime - startTime) + " ms");

    startTime = System.currentTimeMillis();
    builder = BUILDER_2.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      builder.buildCurves(NOW);
    }
    endTime = System.currentTimeMillis();
    System.out.println("MulticurveBuildingDiscountingDiscountXCcyTest - " + nbTest + " curve construction / USD/JPY 3 unit: " + (endTime - startTime) + " ms");
  }

}
