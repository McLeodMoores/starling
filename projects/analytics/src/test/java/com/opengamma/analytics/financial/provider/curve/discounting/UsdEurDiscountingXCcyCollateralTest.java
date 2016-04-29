/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve.discounting;

import static com.opengamma.analytics.financial.provider.curve.CurveBuildingTestUtils.assertFiniteDifferenceSensitivities;
import static com.opengamma.analytics.financial.provider.curve.CurveBuildingTestUtils.assertNoSensitivities;
import static com.opengamma.analytics.financial.provider.curve.CurveBuildingTestUtils.curveConstructionTest;
import static org.testng.Assert.assertEquals;

import java.util.HashMap;
import java.util.LinkedHashMap;
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
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapXCcyIborIbor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.Index;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingTests;
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
public class UsdEurDiscountingXCcyCollateralTest extends CurveBuildingTests {
  /** The interpolator used for all curves. */
  private static final Interpolator1D INTERPOLATOR_LINEAR = NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME);
  /** A calendar containing only Saturday and Sunday holidays */
  private static final CalendarAdapter TARGET = new CalendarAdapter(WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
  /** A calendar containing only Saturday and Sunday holidays */
  private static final CalendarAdapter NYC = new CalendarAdapter(WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
  /** The EUR/USD rate */
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
  /** Generates the EONIA deposit */
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_EUR = new GeneratorDepositON("EONIA", Currency.EUR, TARGET, EONIA_INDEX.getDayCount());
  /** Generates the Fed funds deposit */
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_USD = new GeneratorDepositON("Fed Funds", Currency.USD, TARGET, FED_FUNDS_INDEX.getDayCount());
  /** Generates 3m EURIBOR / 1Y fixed EUR swaps */
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR3M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("EUR1YEURIBOR3M", TARGET);
  /** Generates 3m LIBOR / 6m fixed USD swaps */
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("USD6MLIBOR3M", TARGET);
  /** 3m EURIBOR index */
  private static final IborIndex EURIBOR_INDEX = EUR1YEURIBOR3M.getIborIndex();
  /** 3m LIBOR index */
  private static final IborIndex LIBOR_INDEX = USD6MLIBOR3M.getIborIndex();
  /** Generates 3m USD FRAs */
  private static final GeneratorFRA GENERATOR_USD_FRA_3M = new GeneratorFRA("GENERATOR USD FRA 3M", LIBOR_INDEX, NYC);
  /** Generates the EURIBOR index */
  private static final GeneratorDepositIbor GENERATOR_EURIBOR3M = new GeneratorDepositIbor("GENERATOR_EURIBOR3M", EURIBOR_INDEX, TARGET);
  /** Generates the LIBOR index */
  private static final GeneratorDepositIbor GENERATOR_USDLIBOR3M = new GeneratorDepositIbor("GENERATOR_USDLIBOR3M", LIBOR_INDEX, NYC);
  /** Generates EUR/USD cross-currency swaps */
  private static final GeneratorSwapXCcyIborIbor EURIBOR3MUSDLIBOR3M =
      new GeneratorSwapXCcyIborIbor("EURIBOR3MUSDLIBOR3M", EURIBOR_INDEX, LIBOR_INDEX, TARGET, NYC); // Spread on EUR leg
  /** Generates EUR/USD FX swaps */
  private static final GeneratorForexSwap GENERATOR_FX_EURUSD =
      new GeneratorForexSwap("EURUSD", Currency.EUR, Currency.USD, TARGET, EURIBOR_INDEX.getSpotLag(), EURIBOR_INDEX.getBusinessDayConvention(), true);
  /** The curve construction time */
  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2011, 9, 28);
  /** The previous day */
  private static final ZonedDateTime PREVIOUS = NOW.minusDays(1);
  /** Fed funds fixing series after today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS, NOW}, new double[] {0.07, 0.08});
  /** Fed funds fixing series before today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS}, new double[] {0.07});
  /** EONIA fixing series after today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_EUR_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS, NOW}, new double[] {0.06, 0.07});
  /** EONIA fixing series before today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_EUR_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS}, new double[] {0.06});
  /** LIBOR fixing series after today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_USD3M_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS, NOW}, new double[] {0.0035, 0.0036});
  /** LIBOR fixing series before today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_USD3M_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS}, new double[] {0.0035});
  /** EONIA fixing series after today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_EUR3M_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS, NOW}, new double[] {0.0060, 0.0061});
  /** EONIA fixing series before today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_EUR3M_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {PREVIOUS}, new double[] {0.0060});
  /** Fixing time series created before the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITHOUT_TODAY = new HashMap<>();
  /** Fixing time series created after the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITH_TODAY = new HashMap<>();
  static {
    FIXING_TS_WITHOUT_TODAY.put(FED_FUNDS_INDEX, TS_ON_USD_WITHOUT_TODAY);
    FIXING_TS_WITHOUT_TODAY.put(EONIA_INDEX, TS_ON_EUR_WITHOUT_TODAY);
    FIXING_TS_WITHOUT_TODAY.put(LIBOR_INDEX, TS_IBOR_USD3M_WITHOUT_TODAY);
    FIXING_TS_WITHOUT_TODAY.put(EURIBOR_INDEX, TS_IBOR_EUR3M_WITHOUT_TODAY);
    FIXING_TS_WITH_TODAY.put(FED_FUNDS_INDEX, TS_ON_USD_WITH_TODAY);
    FIXING_TS_WITH_TODAY.put(EONIA_INDEX, TS_ON_EUR_WITH_TODAY);
    FIXING_TS_WITH_TODAY.put(LIBOR_INDEX, TS_IBOR_USD3M_WITH_TODAY);
    FIXING_TS_WITH_TODAY.put(EURIBOR_INDEX, TS_IBOR_EUR3M_WITH_TODAY);
  }
  /** The USD discounting curve name */
  private static final String CURVE_NAME_USD_DSC_FF = "USD Dsc FedFund";
  /** The USD LIBOR curve name */
  private static final String CURVE_NAME_USD_FWD_L3 = "USD Fwd Libor3M";
  /** The EUR discounting curve name */
  private static final String CURVE_NAME_EUR_DSC_EO = "EUR Dsc Eonia";
  /** The EURIBOR curve name */
  private static final String CURVE_NAME_EUR_FWD_E3 = "EUR Fwd Euribor3M";
  /** The USD collateral curve name */
  private static final String CURVE_NAME_EUR_DSC_USDFF = "EUR Dsc USD FedFund";
  /** Market values for the USD discounting curve */
  /** Known market data */
  private static final MulticurveProviderDiscount MULTICURVE_KNOWN_DATA = new MulticurveProviderDiscount(FX_MATRIX);
  /** Builder that constructs EUR and USD discounting, EURIBOR and LIBOR curves */
  private static final DiscountingMethodCurveSetUp BUILDER = DiscountingMethodCurveBuilder.setUp()
      .buildingFirst(CURVE_NAME_USD_DSC_FF)
      .using(CURVE_NAME_USD_DSC_FF).forDiscounting(Currency.USD).forOvernightIndex(FED_FUNDS_INDEX).withInterpolator(INTERPOLATOR_LINEAR)
      .thenBuilding(CURVE_NAME_USD_FWD_L3)
      .using(CURVE_NAME_USD_FWD_L3).forIborIndex(LIBOR_INDEX).withInterpolator(INTERPOLATOR_LINEAR)
      .thenBuilding(CURVE_NAME_EUR_DSC_EO)
      .using(CURVE_NAME_EUR_DSC_EO).forDiscounting(Currency.EUR).forOvernightIndex(EONIA_INDEX).withInterpolator(INTERPOLATOR_LINEAR)
      .thenBuilding(CURVE_NAME_EUR_FWD_E3)
      .using(CURVE_NAME_EUR_FWD_E3).forIborIndex(EURIBOR_INDEX).withInterpolator(INTERPOLATOR_LINEAR)
      .withKnownData(MULTICURVE_KNOWN_DATA);
  /** Market quotes for the USD discounting curve */
  private static final double[] USD_DSC_FF_MARKET_QUOTES = new double[] {
      0.0015, 0.0015, 0.0010, 0.0010, 0.0010, 0.0010, 0.0010, 0.0010, 0.0015, 0.0020, 0.0035, 0.0050, 0.0130 };
  /** Vanilla instrument generators for the USD discounting curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] USD_DSC_FF_GENERATORS = new GeneratorInstrument<?>[] {
    GENERATOR_DEPOSIT_ON_USD, GENERATOR_DEPOSIT_ON_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD,
    GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD };
  /** Attributes for the USD discounting curve */
  private static final GeneratorAttributeIR[] USD_DSC_FF_ATTR;
  static {
    final Period[] tenors = new Period[] {Period.ofDays(0), Period.ofDays(1), Period.ofMonths(1), Period.ofMonths(2),
        Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3),
        Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
    USD_DSC_FF_ATTR = new GeneratorAttributeIR[tenors.length];
    for (int i = 0; i < 2; i++) {
      USD_DSC_FF_ATTR[i] = new GeneratorAttributeIR(tenors[i], Period.ZERO);
      BUILDER.withNode(CURVE_NAME_USD_DSC_FF, USD_DSC_FF_GENERATORS[i], USD_DSC_FF_ATTR[i], USD_DSC_FF_MARKET_QUOTES[i]);
    }
    for (int i = 2; i < tenors.length; i++) {
      USD_DSC_FF_ATTR[i] = new GeneratorAttributeIR(tenors[i]);
      BUILDER.withNode(CURVE_NAME_USD_DSC_FF, USD_DSC_FF_GENERATORS[i], USD_DSC_FF_ATTR[i], USD_DSC_FF_MARKET_QUOTES[i]);
    }
  }
  /** Market values for the USD LIBOR curve */
  private static final double[] USD_FWD_L3_MARKET_QUOTES = new double[] {0.0045, 0.0045, 0.0045, 0.0045, 0.0060, 0.0070, 0.0080, 0.0160 };
  /** Vanilla instrument generators for the USD LIBOR curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] USD_FWD_L3_GENERATORS = new GeneratorInstrument<?>[] {
    GENERATOR_USDLIBOR3M, GENERATOR_USD_FRA_3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M };
  /** Attributes for the USD LIBOR curve */
  private static final GeneratorAttributeIR[] USD_FWD_L3_ATTR;
  static {
    final Period[] tenors = new Period[] {Period.ofMonths(0), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2),
      Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
      Period.ofYears(10) };
    USD_FWD_L3_ATTR = new GeneratorAttributeIR[tenors.length];
    for (int i = 0; i < tenors.length; i++) {
      USD_FWD_L3_ATTR[i] = new GeneratorAttributeIR(tenors[i]);
      BUILDER.withNode(CURVE_NAME_USD_FWD_L3, USD_FWD_L3_GENERATORS[i], USD_FWD_L3_ATTR[i], USD_FWD_L3_MARKET_QUOTES[i]);
    }
  }
  /** Market values for the EUR discounting curve */
  private static final double[] EUR_DSC_EO_MARKET_QUOTES = new double[] {
      0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400 };
  /** Vanilla instrument generators for the EUR discounting curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] EUR_DSC_EO_GENERATORS = new GeneratorInstrument<?>[] {
    GENERATOR_DEPOSIT_ON_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR,
    GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR };
  /** Attributes for the EUR discounting curve */
  private static final GeneratorAttributeIR[] EUR_DSC_EO_ATTR;
  static {
    final Period[] tenors = new Period[] {Period.ofDays(0), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3),
        Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1),
        Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
    EUR_DSC_EO_ATTR = new GeneratorAttributeIR[tenors.length];
    for (int i = 0; i < tenors.length; i++) {
      EUR_DSC_EO_ATTR[i] = new GeneratorAttributeIR(tenors[i]);
      BUILDER.withNode(CURVE_NAME_EUR_DSC_EO, EUR_DSC_EO_GENERATORS[i], EUR_DSC_EO_ATTR[i], EUR_DSC_EO_MARKET_QUOTES[i]);
    }
  }
  /** Market values for the EURIBOR curve */
  private static final double[] EUR_FWD_E3_MARKET_QUOTES = new double[] {0.0045, 0.0045, 0.0045, 0.0045, 0.0050, 0.0060, 0.0085, 0.0160 };
  /** Vanilla instrument generators for the EURIBOR curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] EUR_FWD_E3_GENERATORS = new GeneratorInstrument<?>[] {
    GENERATOR_EURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M };
  /** Attributes for the EURIBOR curve */
  private static final GeneratorAttributeIR[] EUR_FWD_E3_ATTR;
  static {
    final Period[] tenors = new Period[] {Period.ofMonths(0), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2),
      Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
      Period.ofYears(10) };
    EUR_FWD_E3_ATTR = new GeneratorAttributeIR[tenors.length];
    for (int i = 0; i < tenors.length; i++) {
      EUR_FWD_E3_ATTR[i] = new GeneratorAttributeIR(tenors[i]);
      BUILDER.withNode(CURVE_NAME_EUR_FWD_E3, EUR_FWD_E3_GENERATORS[i], EUR_FWD_E3_ATTR[i], EUR_FWD_E3_MARKET_QUOTES[i]);
    }
  }
  /** Builds the EUR discounting with USD collateral curve */
  private static final DiscountingMethodCurveSetUp COLLATERAL_BUILDER;
  /** Curves before fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> BEFORE_FIXING;
  /** Curves after fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> AFTER_FIXING;
  /** Collateralised curve before fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> COLLATERAL_BEFORE_FIXING;
  /** Collateralised curve after fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> COLLATERAL_AFTER_FIXING;
  static {
    BEFORE_FIXING = BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().buildCurves(NOW);
    AFTER_FIXING = BUILDER.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().buildCurves(NOW);
  }
  static {
    COLLATERAL_BUILDER =  DiscountingMethodCurveBuilder.setUp()
        .building(CURVE_NAME_EUR_DSC_USDFF)
        .using(CURVE_NAME_EUR_DSC_USDFF).forDiscounting(Currency.EUR).withInterpolator(INTERPOLATOR_LINEAR);
  }
  /** Market values for the EUR discounting with USD collateral curve */
  private static final double[] EUR_DSC_USDFF_MARKET_QUOTES = new double[] {
      0.0010, 0.0010, 0.00025, 0.0005, 0.00075, 0.0020, 0.0030, 0.0038, -0.0050, -0.0050, -0.0050, -0.0045, -0.0040 };
  /** Vanilla instrument generators for the EUR discounting with USD collateral curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] EUR_DSC_USDFF_GENERATORS = new GeneratorInstrument<?>[] {
    GENERATOR_DEPOSIT_ON_EUR, GENERATOR_DEPOSIT_ON_EUR, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD,
    GENERATOR_FX_EURUSD, EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M };
  /** Attributes for the EUR discounting with USD collateral curve */
  private static final GeneratorAttribute[] EUR_DSC_USDFF_ATTR;
  static {
    final Period[] tenors = new Period[] {Period.ofDays(0), Period.ofDays(1), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6),
        Period.ofMonths(9), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
    EUR_DSC_USDFF_ATTR = new GeneratorAttribute[tenors.length];
    for (int i = 0; i < 2; i++) {
      EUR_DSC_USDFF_ATTR[i] = new GeneratorAttributeIR(tenors[i], Period.ZERO);
      COLLATERAL_BUILDER.withNode(CURVE_NAME_EUR_DSC_USDFF, EUR_DSC_USDFF_GENERATORS[i], EUR_DSC_USDFF_ATTR[i], EUR_DSC_USDFF_MARKET_QUOTES[i]);
    }
    for (int i = 2; i < tenors.length; i++) {
      EUR_DSC_USDFF_ATTR[i] = new GeneratorAttributeFX(tenors[i], FX_MATRIX);
      COLLATERAL_BUILDER.withNode(CURVE_NAME_EUR_DSC_USDFF, EUR_DSC_USDFF_GENERATORS[i], EUR_DSC_USDFF_ATTR[i], EUR_DSC_USDFF_MARKET_QUOTES[i]);
    }
    final MulticurveProviderDiscount knownDataBeforeFixing = BEFORE_FIXING.getFirst().copy();
    knownDataBeforeFixing.removeCurve(Currency.EUR);
    final MulticurveProviderDiscount knownDataAfterFixing = AFTER_FIXING.getFirst().copy();
    knownDataAfterFixing.removeCurve(Currency.EUR);
    COLLATERAL_BEFORE_FIXING = COLLATERAL_BUILDER.copy()
        .withKnownData(knownDataBeforeFixing)
        .withKnownBundle(new CurveBuildingBlockBundle(new LinkedHashMap<>(BEFORE_FIXING.getSecond().getData())))
        .withFixingTs(FIXING_TS_WITHOUT_TODAY)
        .getBuilder().buildCurves(NOW);
    COLLATERAL_AFTER_FIXING = COLLATERAL_BUILDER.copy()
        .withKnownData(knownDataAfterFixing)
        .withFixingTs(FIXING_TS_WITH_TODAY)
        .withKnownBundle(new CurveBuildingBlockBundle(new LinkedHashMap<>(AFTER_FIXING.getSecond().getData())))
        .getBuilder().buildCurves(NOW);
  }

  @Override
  @Test
  public void testJacobianSize() {
    final int fedFundsSize = USD_DSC_FF_MARKET_QUOTES.length;
    final int liborSize = USD_FWD_L3_MARKET_QUOTES.length;
    final int eoniaSize = EUR_DSC_EO_MARKET_QUOTES.length;
    final int euriborSize = EUR_FWD_E3_MARKET_QUOTES.length;
    final int collateralSize = EUR_DSC_USDFF_MARKET_QUOTES.length;
    // curves constructed without collateral
    CurveBuildingBlockBundle fullJacobian = BEFORE_FIXING.getSecond();
    Map<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> fullJacobianData = fullJacobian.getData();
    assertEquals(fullJacobianData.size(), 4);
    // Fed funds
    DoubleMatrix2D matrix = fullJacobianData.get(CURVE_NAME_USD_DSC_FF).getSecond();
    assertEquals(matrix.getNumberOfRows(), fedFundsSize);
    assertEquals(matrix.getNumberOfColumns(), fedFundsSize);
    // LIBOR
    matrix = fullJacobianData.get(CURVE_NAME_USD_FWD_L3).getSecond();
    assertEquals(matrix.getNumberOfRows(), liborSize);
    assertEquals(matrix.getNumberOfColumns(), fedFundsSize + liborSize);
    // EONIA
    matrix = fullJacobianData.get(CURVE_NAME_EUR_DSC_EO).getSecond();
    assertEquals(matrix.getNumberOfRows(), eoniaSize);
    assertEquals(matrix.getNumberOfColumns(), fedFundsSize + liborSize + eoniaSize);
    // EURIBOR
    matrix = fullJacobianData.get(CURVE_NAME_EUR_FWD_E3).getSecond();
    assertEquals(matrix.getNumberOfRows(), euriborSize);
    assertEquals(matrix.getNumberOfColumns(), fedFundsSize + liborSize + eoniaSize + euriborSize);
    // collateral curve + previous
    fullJacobian = COLLATERAL_BEFORE_FIXING.getSecond();
    fullJacobianData = fullJacobian.getData();
    assertEquals(fullJacobianData.size(), 5);
    // Fed funds
    matrix = fullJacobianData.get(CURVE_NAME_USD_DSC_FF).getSecond();
    assertEquals(matrix.getNumberOfRows(), fedFundsSize);
    assertEquals(matrix.getNumberOfColumns(), fedFundsSize);
    // LIBOR
    matrix = fullJacobianData.get(CURVE_NAME_USD_FWD_L3).getSecond();
    assertEquals(matrix.getNumberOfRows(), liborSize);
    assertEquals(matrix.getNumberOfColumns(), fedFundsSize + liborSize);
    // EONIA
    matrix = fullJacobianData.get(CURVE_NAME_EUR_DSC_EO).getSecond();
    assertEquals(matrix.getNumberOfRows(), eoniaSize);
    assertEquals(matrix.getNumberOfColumns(), fedFundsSize + liborSize + eoniaSize);
    // EURIBOR
    matrix = fullJacobianData.get(CURVE_NAME_EUR_FWD_E3).getSecond();
    assertEquals(matrix.getNumberOfRows(), euriborSize);
    assertEquals(matrix.getNumberOfColumns(), fedFundsSize + liborSize + eoniaSize + euriborSize);
    // EUR discounting with USD collateral
    matrix = fullJacobianData.get(CURVE_NAME_EUR_DSC_USDFF).getSecond();
    assertEquals(matrix.getNumberOfRows(), collateralSize);
    assertEquals(matrix.getNumberOfColumns(), fedFundsSize + liborSize + eoniaSize + euriborSize + collateralSize);
  }

  @Override
  @Test
  public void testInstrumentsInCurvePriceToZero() {
    Map<String, InstrumentDefinition<?>[]> definitions;
    // before fixing
    definitions = BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().getDefinitionsForCurves(NOW);
    curveConstructionTest(definitions.get(CURVE_NAME_USD_DSC_FF), BEFORE_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.USD);
    curveConstructionTest(definitions.get(CURVE_NAME_USD_FWD_L3), BEFORE_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.USD);
    curveConstructionTest(definitions.get(CURVE_NAME_EUR_DSC_EO), BEFORE_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.EUR);
    curveConstructionTest(definitions.get(CURVE_NAME_EUR_FWD_E3), BEFORE_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.EUR);
    // after fixing
    definitions = BUILDER.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().getDefinitionsForCurves(NOW);
    curveConstructionTest(definitions.get(CURVE_NAME_USD_DSC_FF), AFTER_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.USD);
    curveConstructionTest(definitions.get(CURVE_NAME_USD_FWD_L3), AFTER_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.USD);
    curveConstructionTest(definitions.get(CURVE_NAME_EUR_DSC_EO), AFTER_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.EUR);
    curveConstructionTest(definitions.get(CURVE_NAME_EUR_FWD_E3), AFTER_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.EUR);
    // before fixing
    definitions = COLLATERAL_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().getDefinitionsForCurves(NOW);
    curveConstructionTest(definitions.get(CURVE_NAME_EUR_DSC_USDFF), COLLATERAL_BEFORE_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW,
        Currency.EUR);
    // after fixing
    definitions = COLLATERAL_BUILDER.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().getDefinitionsForCurves(NOW);
    curveConstructionTest(definitions.get(CURVE_NAME_EUR_DSC_USDFF), COLLATERAL_AFTER_FIXING.getFirst(),
        PresentValueDiscountingCalculator.getInstance(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW,
        Currency.EUR);
  }

  @Override
  @Test
  public void testFiniteDifferenceSensitivities() {
    // Fed funds sensitivities to Fed funds
    assertFiniteDifferenceSensitivities(BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER, CURVE_NAME_USD_DSC_FF,
        CURVE_NAME_USD_DSC_FF, NOW, USD_DSC_FF_GENERATORS, USD_DSC_FF_ATTR, USD_DSC_FF_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY, BUILDER, CURVE_NAME_USD_DSC_FF,
        CURVE_NAME_USD_DSC_FF, NOW, USD_DSC_FF_GENERATORS, USD_DSC_FF_ATTR, USD_DSC_FF_MARKET_QUOTES, false);
    // Fed funds sensitivities to LIBOR - expect sensitivities not to be calculated
    assertNoSensitivities(BEFORE_FIXING.getSecond(), CURVE_NAME_USD_DSC_FF, CURVE_NAME_USD_FWD_L3);
    assertNoSensitivities(AFTER_FIXING.getSecond(), CURVE_NAME_USD_DSC_FF, CURVE_NAME_USD_FWD_L3);
    // Fed funds sensitivities to EONIA - expect sensitivities not to be calculated
    assertNoSensitivities(BEFORE_FIXING.getSecond(), CURVE_NAME_USD_DSC_FF, CURVE_NAME_EUR_DSC_EO);
    assertNoSensitivities(AFTER_FIXING.getSecond(), CURVE_NAME_USD_DSC_FF, CURVE_NAME_EUR_DSC_EO);
    // Fed funds sensitivities to EURIBOR - expect sensitivities not to be calculated
    assertNoSensitivities(BEFORE_FIXING.getSecond(), CURVE_NAME_USD_DSC_FF, CURVE_NAME_EUR_FWD_E3);
    assertNoSensitivities(AFTER_FIXING.getSecond(), CURVE_NAME_USD_DSC_FF, CURVE_NAME_EUR_FWD_E3);

    // LIBOR sensitivities to Fed funds
    assertFiniteDifferenceSensitivities(BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER, CURVE_NAME_USD_FWD_L3,
        CURVE_NAME_USD_DSC_FF, NOW, USD_DSC_FF_GENERATORS, USD_DSC_FF_ATTR, USD_DSC_FF_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY, BUILDER, CURVE_NAME_USD_FWD_L3,
        CURVE_NAME_USD_DSC_FF, NOW, USD_DSC_FF_GENERATORS, USD_DSC_FF_ATTR, USD_DSC_FF_MARKET_QUOTES, false);
    // LIBOR sensitivities to LIBOR
    assertFiniteDifferenceSensitivities(BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER, CURVE_NAME_USD_FWD_L3,
        CURVE_NAME_USD_FWD_L3, NOW, USD_FWD_L3_GENERATORS, USD_FWD_L3_ATTR, USD_FWD_L3_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY, BUILDER, CURVE_NAME_USD_FWD_L3,
        CURVE_NAME_USD_FWD_L3, NOW, USD_FWD_L3_GENERATORS, USD_FWD_L3_ATTR, USD_FWD_L3_MARKET_QUOTES, false);
    // LIBOR sensitivities to EONIA - expect sensitivities not to be calculated
    assertNoSensitivities(BEFORE_FIXING.getSecond(), CURVE_NAME_USD_FWD_L3, CURVE_NAME_EUR_DSC_EO);
    assertNoSensitivities(AFTER_FIXING.getSecond(), CURVE_NAME_USD_FWD_L3, CURVE_NAME_EUR_DSC_EO);
    // LIBOR sensitivities to EURIBOR - expect sensitivities not to be calculated
    assertNoSensitivities(BEFORE_FIXING.getSecond(), CURVE_NAME_USD_FWD_L3, CURVE_NAME_EUR_FWD_E3);
    assertNoSensitivities(AFTER_FIXING.getSecond(), CURVE_NAME_USD_FWD_L3, CURVE_NAME_EUR_FWD_E3);

    // EONIA sensitivities to Fed funds - expect sensitivities to be zero
    assertFiniteDifferenceSensitivities(BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER, CURVE_NAME_EUR_DSC_EO,
        CURVE_NAME_USD_DSC_FF, NOW, USD_DSC_FF_GENERATORS, USD_DSC_FF_ATTR, USD_DSC_FF_MARKET_QUOTES, true);
    assertFiniteDifferenceSensitivities(AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY, BUILDER, CURVE_NAME_EUR_DSC_EO,
        CURVE_NAME_USD_DSC_FF, NOW, USD_DSC_FF_GENERATORS, USD_DSC_FF_ATTR, USD_DSC_FF_MARKET_QUOTES, true);
    // EONIA sensitivities to LIBOR - expect sensitivities to be zero
    assertFiniteDifferenceSensitivities(BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER, CURVE_NAME_EUR_DSC_EO,
        CURVE_NAME_USD_FWD_L3, NOW, USD_FWD_L3_GENERATORS, USD_FWD_L3_ATTR, USD_FWD_L3_MARKET_QUOTES, true);
    assertFiniteDifferenceSensitivities(AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY, BUILDER, CURVE_NAME_EUR_DSC_EO,
        CURVE_NAME_USD_FWD_L3, NOW, USD_FWD_L3_GENERATORS, USD_FWD_L3_ATTR, USD_FWD_L3_MARKET_QUOTES, true);
    // EONIA sensitivities to EONIA
    assertFiniteDifferenceSensitivities(BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER, CURVE_NAME_EUR_DSC_EO,
        CURVE_NAME_EUR_DSC_EO, NOW, EUR_DSC_EO_GENERATORS, EUR_DSC_EO_ATTR, EUR_DSC_EO_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY, BUILDER, CURVE_NAME_EUR_DSC_EO,
        CURVE_NAME_EUR_DSC_EO, NOW, EUR_DSC_EO_GENERATORS, EUR_DSC_EO_ATTR, EUR_DSC_EO_MARKET_QUOTES, false);
    // EONIA sensitivities to EURIBOR - expect sensitivities not to be calculated
    assertNoSensitivities(BEFORE_FIXING.getSecond(), CURVE_NAME_EUR_DSC_EO, CURVE_NAME_EUR_FWD_E3);
    assertNoSensitivities(AFTER_FIXING.getSecond(), CURVE_NAME_EUR_DSC_EO, CURVE_NAME_EUR_FWD_E3);

    // EURIBOR sensitivities to Fed funds - expect sensitivities to be zero
    assertFiniteDifferenceSensitivities(BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER, CURVE_NAME_EUR_FWD_E3,
        CURVE_NAME_USD_DSC_FF, NOW, USD_DSC_FF_GENERATORS, USD_DSC_FF_ATTR, USD_DSC_FF_MARKET_QUOTES, true);
    assertFiniteDifferenceSensitivities(AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY, BUILDER, CURVE_NAME_EUR_FWD_E3,
        CURVE_NAME_USD_DSC_FF, NOW, USD_DSC_FF_GENERATORS, USD_DSC_FF_ATTR, USD_DSC_FF_MARKET_QUOTES, true);
    // EURIBOR sensitivities to LIBOR - expect sensitivities to be zero
    assertFiniteDifferenceSensitivities(BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER, CURVE_NAME_EUR_FWD_E3,
        CURVE_NAME_USD_FWD_L3, NOW, USD_FWD_L3_GENERATORS, USD_FWD_L3_ATTR, USD_FWD_L3_MARKET_QUOTES, true);
    assertFiniteDifferenceSensitivities(AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY, BUILDER, CURVE_NAME_EUR_FWD_E3,
        CURVE_NAME_USD_FWD_L3, NOW, USD_FWD_L3_GENERATORS, USD_FWD_L3_ATTR, USD_FWD_L3_MARKET_QUOTES, true);
    // EURIBOR sensitivities to EONIA
    assertFiniteDifferenceSensitivities(BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER, CURVE_NAME_EUR_FWD_E3,
        CURVE_NAME_EUR_DSC_EO, NOW, EUR_DSC_EO_GENERATORS, EUR_DSC_EO_ATTR, EUR_DSC_EO_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY, BUILDER, CURVE_NAME_EUR_FWD_E3,
        CURVE_NAME_EUR_DSC_EO, NOW, EUR_DSC_EO_GENERATORS, EUR_DSC_EO_ATTR, EUR_DSC_EO_MARKET_QUOTES, false);
    // EURIBOR sensitivities to EURIBOR
    assertFiniteDifferenceSensitivities(BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, BUILDER, CURVE_NAME_EUR_FWD_E3,
        CURVE_NAME_EUR_FWD_E3, NOW, EUR_FWD_E3_GENERATORS, EUR_FWD_E3_ATTR, EUR_FWD_E3_MARKET_QUOTES, false);
    assertFiniteDifferenceSensitivities(AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY, BUILDER, CURVE_NAME_EUR_FWD_E3,
        CURVE_NAME_EUR_FWD_E3, NOW, EUR_FWD_E3_GENERATORS, EUR_FWD_E3_ATTR, EUR_FWD_E3_MARKET_QUOTES, false);

    //TODO
//    // Collateralized EUR sensitivities to Fed funds
//    assertFiniteDifferenceSensitivities(COLLATERAL_BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, COLLATERAL_BUILDER, CURVE_NAME_EUR_DSC_USDFF,
//        CURVE_NAME_USD_DSC_FF, NOW, USD_DSC_FF_GENERATORS, USD_DSC_FF_ATTR, USD_DSC_FF_MARKET_QUOTES, true);
//    assertFiniteDifferenceSensitivities(COLLATERAL_AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY, COLLATERAL_BUILDER, CURVE_NAME_EUR_DSC_USDFF,
//        CURVE_NAME_USD_DSC_FF, NOW, USD_DSC_FF_GENERATORS, USD_DSC_FF_ATTR, USD_DSC_FF_MARKET_QUOTES, true);
//    // Collateralized EUR sensitivities to LIBOR
//    assertFiniteDifferenceSensitivities(COLLATERAL_BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, COLLATERAL_BUILDER, CURVE_NAME_EUR_DSC_USDFF,
//        CURVE_NAME_USD_FWD_L3, NOW, USD_FWD_L3_GENERATORS, USD_FWD_L3_ATTR, USD_FWD_L3_MARKET_QUOTES, true);
//    assertFiniteDifferenceSensitivities(COLLATERAL_AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY, COLLATERAL_BUILDER, CURVE_NAME_EUR_DSC_USDFF,
//        CURVE_NAME_USD_FWD_L3, NOW, USD_FWD_L3_GENERATORS, USD_FWD_L3_ATTR, USD_FWD_L3_MARKET_QUOTES, true);
//    // Collateralized EUR sensitivities to EONIA
//    assertFiniteDifferenceSensitivities(COLLATERAL_BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, COLLATERAL_BUILDER, CURVE_NAME_EUR_DSC_USDFF,
//        CURVE_NAME_EUR_DSC_EO, NOW, EUR_DSC_EO_GENERATORS, EUR_DSC_EO_ATTR, EUR_DSC_EO_MARKET_QUOTES, false);
//    assertFiniteDifferenceSensitivities(COLLATERAL_AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY, COLLATERAL_BUILDER, CURVE_NAME_EUR_DSC_USDFF,
//        CURVE_NAME_EUR_DSC_EO, NOW, EUR_DSC_EO_GENERATORS, EUR_DSC_EO_ATTR, EUR_DSC_EO_MARKET_QUOTES, false);
//    // Collateralized EUR sensitivities to EURIBOR
//    final MulticurveProviderDiscount knownDataBeforeFixing = BEFORE_FIXING.getFirst().copy();
//    knownDataBeforeFixing.removeCurve(Currency.EUR);
//    final MulticurveProviderDiscount knownDataAfterFixing = AFTER_FIXING.getFirst().copy();
//    knownDataAfterFixing.removeCurve(Currency.EUR);
//    DiscountingMethodCurveSetUp builder = COLLATERAL_BUILDER
//        .withKnownData(knownDataBeforeFixing)
//        .withKnownBundle(new CurveBuildingBlockBundle(new LinkedHashMap<>(BEFORE_FIXING.getSecond().getData())));
//    assertFiniteDifferenceSensitivities(COLLATERAL_BEFORE_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY, builder, CURVE_NAME_EUR_DSC_USDFF,
//        CURVE_NAME_EUR_DSC_USDFF, NOW, EUR_DSC_USDFF_GENERATORS, EUR_DSC_USDFF_ATTR, EUR_DSC_USDFF_MARKET_QUOTES, false);
//    builder = COLLATERAL_BUILDER
//        .withKnownData(knownDataAfterFixing)
//        .withKnownBundle(new CurveBuildingBlockBundle(new LinkedHashMap<>(AFTER_FIXING.getSecond().getData())));
//    assertFiniteDifferenceSensitivities(COLLATERAL_AFTER_FIXING.getSecond(), FIXING_TS_WITH_TODAY, builder, CURVE_NAME_EUR_DSC_USDFF,
//        CURVE_NAME_EUR_DSC_USDFF, NOW, EUR_DSC_USDFF_GENERATORS, EUR_DSC_USDFF_ATTR, EUR_DSC_USDFF_MARKET_QUOTES, false);
  }

  @Override
  @Test
  public void testSameCurvesDifferentMethods() {
    return;
  }
}
