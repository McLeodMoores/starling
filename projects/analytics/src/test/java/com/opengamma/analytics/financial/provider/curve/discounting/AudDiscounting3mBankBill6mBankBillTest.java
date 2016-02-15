/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve.discounting;

import static com.opengamma.analytics.financial.provider.curve.discounting.DiscountingMethodCurveUtils.curveConstructionTest;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.date.CalendarAdapter;
import com.opengamma.analytics.date.WeekendWorkingDayCalendar;
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
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.discounting.DiscountingMethodCurveUtils.DiscountingMethodCurveBuilder;
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

/**
 * Tests with relevant Jacobian matrices.
 */
@Test(groups = TestGroup.UNIT)
public class AudDiscounting3mBankBill6mBankBillTest {
  private static final Interpolator1D INTERPOLATOR = NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME);
  private static final CalendarAdapter SYD = new CalendarAdapter(WeekendWorkingDayCalendar.SATURDAY_SUNDAY);
  private static final FXMatrix FX_MATRIX = new FXMatrix(Currency.AUD);
  private static final GeneratorSwapFixedON GENERATOR_OIS_AUD = GeneratorSwapFixedONMaster.getInstance().getGenerator("AUD1YRBAON", SYD);
  /** An overnight AUD index */
  private static final IndexON AUD_OVERNIGHT_INDEX = GENERATOR_OIS_AUD.getIndex();
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_AUD = new GeneratorDepositON("AUD Deposit ON", Currency.AUD, SYD, AUD_OVERNIGHT_INDEX.getDayCount());
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
  private static final ZonedDateTime PREVIOUS_DATE = NOW.minusDays(1);
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_AUD_WITH_TODAY =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] { PREVIOUS_DATE, NOW }, new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_AUD_WITHOUT_TODAY =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] { PREVIOUS_DATE }, new double[] {0.07 });
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_AUD3M_WITH_TODAY =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] { PREVIOUS_DATE, NOW }, new double[] {0.0035, 0.0036 });
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_AUD3M_WITHOUT_TODAY =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] { PREVIOUS_DATE }, new double[] {0.0035 });
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_AUD6M_WITH_TODAY =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] { PREVIOUS_DATE, NOW }, new double[] {0.0035, 0.0036 });
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_AUD6M_WITHOUT_TODAY =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] { PREVIOUS_DATE }, new double[] {0.0035 });

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
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_AUD_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_DEPOSIT_ON_AUD, GENERATOR_OIS_AUD, GENERATOR_OIS_AUD,
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

  private static final MulticurveProviderDiscount MULTICURVE_KNOWN_DATA = new MulticurveProviderDiscount(FX_MATRIX);
  private static final DiscountingMethodCurveBuilder.ConfigBuilder DISCOUNTING_THEN_BANK_BILLS_BUILDER = DiscountingMethodCurveBuilder.setUp()
      .buildingFirst(CURVE_NAME_DSC_AUD)
      .using(CURVE_NAME_DSC_AUD).forDiscounting(Currency.AUD).forOvernightIndex(AUD_OVERNIGHT_INDEX).withInterpolator(INTERPOLATOR)
      .thenBuilding(CURVE_NAME_FWD3_AUD, CURVE_NAME_FWD6_AUD)
      .using(CURVE_NAME_FWD3_AUD).forIborIndex(AUD_3M_BANK_BILL_INDEX).withInterpolator(INTERPOLATOR)
      .using(CURVE_NAME_FWD6_AUD).forIborIndex(AUD_6M_BANK_BILL_INDEX).withInterpolator(INTERPOLATOR)
      .withKnownData(MULTICURVE_KNOWN_DATA);
  private static final DiscountingMethodCurveBuilder.ConfigBuilder DISCOUNTING_AND_BANK_BILLS_BUILDER = DiscountingMethodCurveBuilder.setUp()
      .building(CURVE_NAME_DSC_AUD, CURVE_NAME_FWD3_AUD, CURVE_NAME_FWD6_AUD)
      .using(CURVE_NAME_DSC_AUD).forDiscounting(Currency.AUD).forOvernightIndex(AUD_OVERNIGHT_INDEX).withInterpolator(INTERPOLATOR)
      .using(CURVE_NAME_FWD3_AUD).forIborIndex(AUD_3M_BANK_BILL_INDEX).withInterpolator(INTERPOLATOR)
      .using(CURVE_NAME_FWD6_AUD).forIborIndex(AUD_6M_BANK_BILL_INDEX).withInterpolator(INTERPOLATOR)
      .withKnownData(MULTICURVE_KNOWN_DATA);
  static {
    for (int i = 0; i < DSC_AUD_MARKET_QUOTES.length; i++) {
      DISCOUNTING_THEN_BANK_BILLS_BUILDER.withNode(CURVE_NAME_DSC_AUD, DSC_AUD_GENERATORS[i], DSC_AUD_ATTR[i], DSC_AUD_MARKET_QUOTES[i]);
      DISCOUNTING_AND_BANK_BILLS_BUILDER.withNode(CURVE_NAME_DSC_AUD, DSC_AUD_GENERATORS[i], DSC_AUD_ATTR[i], DSC_AUD_MARKET_QUOTES[i]);
    }
    for (int i = 0; i < FWD3_AUD_MARKET_QUOTES.length; i++) {
      DISCOUNTING_THEN_BANK_BILLS_BUILDER.withNode(CURVE_NAME_FWD3_AUD, FWD3_AUD_GENERATORS[i], FWD3_AUD_ATTR[i], FWD3_AUD_MARKET_QUOTES[i]);
      DISCOUNTING_AND_BANK_BILLS_BUILDER.withNode(CURVE_NAME_FWD3_AUD, FWD3_AUD_GENERATORS[i], FWD3_AUD_ATTR[i], FWD3_AUD_MARKET_QUOTES[i]);
    }
    for (int i = 0; i < FWD6_AUD_MARKET_QUOTES.length; i++) {
      DISCOUNTING_THEN_BANK_BILLS_BUILDER.withNode(CURVE_NAME_FWD6_AUD, FWD6_AUD_GENERATORS[i], FWD6_AUD_ATTR[i], FWD6_AUD_MARKET_QUOTES[i]);
      DISCOUNTING_AND_BANK_BILLS_BUILDER.withNode(CURVE_NAME_FWD6_AUD, FWD6_AUD_GENERATORS[i], FWD6_AUD_ATTR[i], FWD6_AUD_MARKET_QUOTES[i]);
    }
    DISCOUNTING_THEN_BANK_BILLS_BUILDER.withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().buildCurves(NOW);
    DISCOUNTING_AND_BANK_BILLS_BUILDER.withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().buildCurves(NOW);
  }

  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> DSC_BANK_BILLS_SIMULTANEOUS_BEFORE_FIXING;
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> DSC_THEN_BANK_BILLS_BEFORE_FIXING;
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> DSC_BANK_BILLS_SIMULTANEOUS_AFTER_FIXING;
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> DSC_THEN_BANK_BILLS_AFTER_FIXING;

  static {
    DSC_BANK_BILLS_SIMULTANEOUS_BEFORE_FIXING = DISCOUNTING_AND_BANK_BILLS_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().buildCurves(NOW);
    DSC_BANK_BILLS_SIMULTANEOUS_AFTER_FIXING = DISCOUNTING_AND_BANK_BILLS_BUILDER.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().buildCurves(NOW);
    DSC_THEN_BANK_BILLS_BEFORE_FIXING = DISCOUNTING_THEN_BANK_BILLS_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().buildCurves(NOW);
    DSC_THEN_BANK_BILLS_AFTER_FIXING = DISCOUNTING_THEN_BANK_BILLS_BUILDER.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().buildCurves(NOW);
  }

  private static final double TOLERANCE_CAL = 1.0E-9;

  @Test
  public void testJacobianSizes() {
    final int allQuotes = DSC_AUD_MARKET_QUOTES.length + FWD3_AUD_MARKET_QUOTES.length + FWD6_AUD_MARKET_QUOTES.length;
    // discounting curve first, then two coupled bank bill curves
    CurveBuildingBlockBundle fullJacobian = DSC_THEN_BANK_BILLS_BEFORE_FIXING.getSecond();
    Map<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> fullJacobianData = fullJacobian.getData();
    assertEquals(fullJacobianData.size(), 3);
    DoubleMatrix2D discountingJacobianMatrix = fullJacobianData.get(CURVE_NAME_DSC_AUD).getSecond();
    assertEquals(discountingJacobianMatrix.getNumberOfRows(), DSC_AUD_MARKET_QUOTES.length);
    assertEquals(discountingJacobianMatrix.getNumberOfColumns(), DSC_AUD_MARKET_QUOTES.length);
    DoubleMatrix2D bankBill3mJacobianMatrix = fullJacobianData.get(CURVE_NAME_FWD3_AUD).getSecond();
    assertEquals(bankBill3mJacobianMatrix.getNumberOfRows(), FWD3_AUD_MARKET_QUOTES.length);
    assertEquals(bankBill3mJacobianMatrix.getNumberOfColumns(), allQuotes);
    DoubleMatrix2D libor6mJacobianMatrix = fullJacobianData.get(CURVE_NAME_FWD6_AUD).getSecond();
    assertEquals(libor6mJacobianMatrix.getNumberOfRows(), FWD6_AUD_MARKET_QUOTES.length);
    assertEquals(libor6mJacobianMatrix.getNumberOfColumns(), allQuotes);
    // three curves fitted at the same time
    fullJacobian = DSC_BANK_BILLS_SIMULTANEOUS_BEFORE_FIXING.getSecond();
    fullJacobianData = fullJacobian.getData();
    assertEquals(fullJacobianData.size(), 3);
    discountingJacobianMatrix = fullJacobianData.get(CURVE_NAME_DSC_AUD).getSecond();
    assertEquals(discountingJacobianMatrix.getNumberOfRows(), DSC_AUD_MARKET_QUOTES.length);
    assertEquals(discountingJacobianMatrix.getNumberOfColumns(), allQuotes);
    bankBill3mJacobianMatrix = fullJacobianData.get(CURVE_NAME_FWD3_AUD).getSecond();
    assertEquals(bankBill3mJacobianMatrix.getNumberOfRows(), FWD3_AUD_MARKET_QUOTES.length);
    assertEquals(bankBill3mJacobianMatrix.getNumberOfColumns(), allQuotes);
    libor6mJacobianMatrix = fullJacobianData.get(CURVE_NAME_FWD6_AUD).getSecond();
    assertEquals(libor6mJacobianMatrix.getNumberOfRows(), FWD6_AUD_MARKET_QUOTES.length);
    assertEquals(libor6mJacobianMatrix.getNumberOfColumns(), allQuotes);
  }

  @Test
  public void testInstrumentsInCurvePriceToZero() {
    InstrumentDefinition<?>[] definitions;
    // discounting then 3m then 6m
    Map<String, InstrumentDefinition<?>[]> definitionsForDiscountingThenLibors;
    // before fixing
    definitionsForDiscountingThenLibors = DISCOUNTING_THEN_BANK_BILLS_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().getDefinitionsForCurves(NOW);
    definitions = definitionsForDiscountingThenLibors.get(CURVE_NAME_DSC_AUD);
    curveConstructionTest(definitions, DSC_THEN_BANK_BILLS_BEFORE_FIXING.getFirst(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.AUD);
    definitions = definitionsForDiscountingThenLibors.get(CURVE_NAME_FWD3_AUD);
    curveConstructionTest(definitions, DSC_THEN_BANK_BILLS_BEFORE_FIXING.getFirst(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.AUD);
    definitions = definitionsForDiscountingThenLibors.get(CURVE_NAME_FWD6_AUD);
    curveConstructionTest(definitions, DSC_THEN_BANK_BILLS_BEFORE_FIXING.getFirst(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.AUD);
    // after fixing
    definitionsForDiscountingThenLibors = DISCOUNTING_THEN_BANK_BILLS_BUILDER.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().getDefinitionsForCurves(NOW);
    definitions = definitionsForDiscountingThenLibors.get(CURVE_NAME_DSC_AUD);
    curveConstructionTest(definitions, DSC_THEN_BANK_BILLS_AFTER_FIXING.getFirst(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.AUD);
    definitions = definitionsForDiscountingThenLibors.get(CURVE_NAME_FWD3_AUD);
    curveConstructionTest(definitions, DSC_THEN_BANK_BILLS_AFTER_FIXING.getFirst(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.AUD);
    definitions = definitionsForDiscountingThenLibors.get(CURVE_NAME_FWD6_AUD);
    curveConstructionTest(definitions, DSC_THEN_BANK_BILLS_AFTER_FIXING.getFirst(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.AUD);
    // discounting and libors
    Map<String, InstrumentDefinition<?>[]> definitionsForDiscountingAndLibors;
    // before fixing
    definitionsForDiscountingAndLibors = DISCOUNTING_AND_BANK_BILLS_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().getDefinitionsForCurves(NOW);
    definitions = definitionsForDiscountingAndLibors.get(CURVE_NAME_DSC_AUD);
    curveConstructionTest(definitions, DSC_BANK_BILLS_SIMULTANEOUS_BEFORE_FIXING.getFirst(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.AUD);
    definitions = definitionsForDiscountingAndLibors.get(CURVE_NAME_FWD3_AUD);
    curveConstructionTest(definitions, DSC_BANK_BILLS_SIMULTANEOUS_BEFORE_FIXING.getFirst(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.AUD);
    definitions = definitionsForDiscountingAndLibors.get(CURVE_NAME_FWD6_AUD);
    curveConstructionTest(definitions, DSC_BANK_BILLS_SIMULTANEOUS_BEFORE_FIXING.getFirst(), FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.AUD);
    // after fixing
    definitionsForDiscountingAndLibors = DISCOUNTING_AND_BANK_BILLS_BUILDER.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().getDefinitionsForCurves(NOW);
    definitions = definitionsForDiscountingAndLibors.get(CURVE_NAME_DSC_AUD);
    curveConstructionTest(definitions, DSC_BANK_BILLS_SIMULTANEOUS_AFTER_FIXING.getFirst(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.AUD);
    definitions = definitionsForDiscountingAndLibors.get(CURVE_NAME_FWD3_AUD);
    curveConstructionTest(definitions, DSC_BANK_BILLS_SIMULTANEOUS_AFTER_FIXING.getFirst(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.AUD);
    definitions = definitionsForDiscountingAndLibors.get(CURVE_NAME_FWD6_AUD);
    curveConstructionTest(definitions, DSC_BANK_BILLS_SIMULTANEOUS_AFTER_FIXING.getFirst(), FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.AUD);
  }

  // discounting curve has no sensitivity to 3m or 6m bank bills
  @Test
  public void blockBundleDscFiniteDifferenceTest1() {
    final int discountingCurveSize = DSC_AUD_MARKET_QUOTES.length;
    final CurveBuildingBlockBundle fullInverseJacobian = DSC_THEN_BANK_BILLS_BEFORE_FIXING.getSecond();
    final double bump = 1e-6;
    for (int i = 0; i < discountingCurveSize; i++) {
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults = DISCOUNTING_THEN_BANK_BILLS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_DSC_AUD, DSC_AUD_GENERATORS[i], DSC_AUD_ATTR[i], DSC_AUD_MARKET_QUOTES[i] + bump)
          .buildCurves(NOW);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults = DISCOUNTING_THEN_BANK_BILLS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_DSC_AUD, DSC_AUD_GENERATORS[i], DSC_AUD_ATTR[i], DSC_AUD_MARKET_QUOTES[i] - bump)
          .buildCurves(NOW);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_DSC_AUD)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_DSC_AUD)).getCurve().getYData();
      for (int j = 0; j < discountingCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        // note columns then rows tested
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_DSC_AUD).getSecond().getData()[j][i];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_DSC_AUD + ": column=" + j + " row=" + i,
            expectedSensitivity, dYielddQuote, bump);
      }
    }
  }

  @Test
  public void blockBundleDscFiniteDifferenceTest2() {
    final int discountingCurveSize = DSC_AUD_MARKET_QUOTES.length;
    final int libor3mCurveSize = FWD3_AUD_MARKET_QUOTES.length;
    final int libor6mCurveSize = FWD6_AUD_MARKET_QUOTES.length;
    final CurveBuildingBlockBundle fullInverseJacobian = DSC_BANK_BILLS_SIMULTANEOUS_BEFORE_FIXING.getSecond();
    final double bump = 1e-6;
    for (int i = 0; i < discountingCurveSize; i++) {
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults = DISCOUNTING_AND_BANK_BILLS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_DSC_AUD, DSC_AUD_GENERATORS[i], DSC_AUD_ATTR[i], DSC_AUD_MARKET_QUOTES[i] + bump)
          .buildCurves(NOW);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults = DISCOUNTING_AND_BANK_BILLS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_DSC_AUD, DSC_AUD_GENERATORS[i], DSC_AUD_ATTR[i], DSC_AUD_MARKET_QUOTES[i] - bump)
          .buildCurves(NOW);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_DSC_AUD)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_DSC_AUD)).getCurve().getYData();
      for (int j = 0; j < discountingCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        // note columns then rows tested
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_DSC_AUD).getSecond().getData()[j][i];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_DSC_AUD + ": column=" + i + " row=" + j,
            expectedSensitivity, dYielddQuote, bump);
      }
    }
    for (int i = 0; i < libor3mCurveSize; i++) {
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults = DISCOUNTING_AND_BANK_BILLS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_FWD3_AUD, FWD3_AUD_GENERATORS[i], FWD3_AUD_ATTR[i], FWD3_AUD_MARKET_QUOTES[i] + bump)
          .buildCurves(NOW);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults = DISCOUNTING_AND_BANK_BILLS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_FWD3_AUD, FWD3_AUD_GENERATORS[i], FWD3_AUD_ATTR[i], FWD3_AUD_MARKET_QUOTES[i] - bump)
          .buildCurves(NOW);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_DSC_AUD)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_DSC_AUD)).getCurve().getYData();
      final int offset = i + discountingCurveSize;
      for (int j = 0; j < discountingCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_DSC_AUD).getSecond().getData()[j][offset];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_DSC_AUD + ": column=" + offset + " row=" + j,
            expectedSensitivity, dYielddQuote, bump);
        assertEquals(expectedSensitivity, 0, 1e-12);
      }
    }
    for (int i = 0; i < libor6mCurveSize; i++) {
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults = DISCOUNTING_AND_BANK_BILLS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_FWD6_AUD, FWD6_AUD_GENERATORS[i], FWD6_AUD_ATTR[i], FWD6_AUD_MARKET_QUOTES[i] + bump)
          .buildCurves(NOW);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults = DISCOUNTING_AND_BANK_BILLS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_FWD6_AUD, FWD6_AUD_GENERATORS[i], FWD6_AUD_ATTR[i], FWD6_AUD_MARKET_QUOTES[i] - bump)
          .buildCurves(NOW);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_DSC_AUD)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_DSC_AUD)).getCurve().getYData();
      final int offset = i + libor3mCurveSize + discountingCurveSize;
      for (int j = 0; j < discountingCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_DSC_AUD).getSecond().getData()[j][offset];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_DSC_AUD + ": column=" + offset + " row=" + j,
            expectedSensitivity, dYielddQuote, bump);
        assertEquals(expectedSensitivity, 0, bump);
      }
    }
  }

  @Test
  public void blockBundleFwd3MFiniteDifferenceTest2() {
    final int discountingCurveSize = DSC_AUD_MARKET_QUOTES.length;
    final int libor3mCurveSize = FWD3_AUD_MARKET_QUOTES.length;
    final int libor6mCurveSize = FWD6_AUD_MARKET_QUOTES.length;
    final CurveBuildingBlockBundle fullInverseJacobian = DSC_BANK_BILLS_SIMULTANEOUS_BEFORE_FIXING.getSecond();
    final double bump = 1e-6;

    for (int i = 0; i < discountingCurveSize; i++) {
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults = DISCOUNTING_AND_BANK_BILLS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_DSC_AUD, DSC_AUD_GENERATORS[i], DSC_AUD_ATTR[i], DSC_AUD_MARKET_QUOTES[i] + bump)
          .buildCurves(NOW);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults = DISCOUNTING_AND_BANK_BILLS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_DSC_AUD, DSC_AUD_GENERATORS[i], DSC_AUD_ATTR[i], DSC_AUD_MARKET_QUOTES[i] - bump)
          .buildCurves(NOW);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_FWD3_AUD)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_FWD3_AUD)).getCurve().getYData();
      for (int j = 0; j < libor3mCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_FWD3_AUD).getSecond().getData()[j][i];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_FWD3_AUD + ": column=" + i + " row=" + j,
            expectedSensitivity, dYielddQuote, bump);
      }
    }
    for (int i = 0; i < libor3mCurveSize; i++) {
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults = DISCOUNTING_AND_BANK_BILLS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_FWD3_AUD, FWD3_AUD_GENERATORS[i], FWD3_AUD_ATTR[i], FWD3_AUD_MARKET_QUOTES[i] + bump)
          .buildCurves(NOW);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults = DISCOUNTING_AND_BANK_BILLS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_FWD3_AUD, FWD3_AUD_GENERATORS[i], FWD3_AUD_ATTR[i], FWD3_AUD_MARKET_QUOTES[i] - bump)
          .buildCurves(NOW);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_FWD3_AUD)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_FWD3_AUD)).getCurve().getYData();
      final int offset = i + discountingCurveSize;
      for (int j = 0; j < libor3mCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_FWD3_AUD).getSecond().getData()[j][offset];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_FWD3_AUD + ": column=" + offset + " row=" + j,
            expectedSensitivity, dYielddQuote, bump);
      }
    }
    for (int i = 0; i < libor6mCurveSize; i++) {
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults = DISCOUNTING_AND_BANK_BILLS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_FWD6_AUD, FWD6_AUD_GENERATORS[i], FWD6_AUD_ATTR[i], FWD6_AUD_MARKET_QUOTES[i] + bump)
          .buildCurves(NOW);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults = DISCOUNTING_AND_BANK_BILLS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_FWD6_AUD, FWD6_AUD_GENERATORS[i], FWD6_AUD_ATTR[i], FWD6_AUD_MARKET_QUOTES[i] - bump)
          .buildCurves(NOW);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_FWD3_AUD)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_FWD3_AUD)).getCurve().getYData();
      final int offset = i + libor3mCurveSize + discountingCurveSize;
      for (int j = 0; j < libor3mCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_FWD3_AUD).getSecond().getData()[j][offset];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_FWD3_AUD + ": column=" + offset + " row=" + j,
            expectedSensitivity, dYielddQuote, bump);
      }
    }
  }

  @Test
  public void blockBundleFwd6MFiniteDifferenceTest1() {
    final int discountingCurveSize = DSC_AUD_MARKET_QUOTES.length;
    final int libor3mCurveSize = FWD3_AUD_MARKET_QUOTES.length;
    final int libor6mCurveSize = FWD6_AUD_MARKET_QUOTES.length;
    final CurveBuildingBlockBundle fullInverseJacobian = DSC_THEN_BANK_BILLS_BEFORE_FIXING.getSecond();
    final double bump = 1e-6;

    for (int i = 0; i < discountingCurveSize; i++) {
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults = DISCOUNTING_THEN_BANK_BILLS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_DSC_AUD, DSC_AUD_GENERATORS[i], DSC_AUD_ATTR[i], DSC_AUD_MARKET_QUOTES[i] + bump)
          .buildCurves(NOW);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults = DISCOUNTING_THEN_BANK_BILLS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_DSC_AUD, DSC_AUD_GENERATORS[i], DSC_AUD_ATTR[i], DSC_AUD_MARKET_QUOTES[i] - bump)
          .buildCurves(NOW);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_FWD6_AUD)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_FWD6_AUD)).getCurve().getYData();
      for (int j = 0; j < libor6mCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_FWD6_AUD).getSecond().getData()[j][i];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_FWD6_AUD + ": column=" + i + " row=" + j,
            expectedSensitivity, dYielddQuote, bump);
      }
    }
    for (int i = 0; i < libor3mCurveSize; i++) {
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults = DISCOUNTING_THEN_BANK_BILLS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_FWD3_AUD, FWD3_AUD_GENERATORS[i], FWD3_AUD_ATTR[i], FWD3_AUD_MARKET_QUOTES[i] + bump)
          .buildCurves(NOW);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults = DISCOUNTING_THEN_BANK_BILLS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_FWD3_AUD, FWD3_AUD_GENERATORS[i], FWD3_AUD_ATTR[i], FWD3_AUD_MARKET_QUOTES[i] - bump)
          .buildCurves(NOW);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_FWD6_AUD)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_FWD6_AUD)).getCurve().getYData();
      final int offset = i + discountingCurveSize;
      for (int j = 0; j < libor6mCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_FWD6_AUD).getSecond().getData()[j][offset];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_FWD6_AUD + ": column=" + offset + " row=" + j,
            expectedSensitivity, dYielddQuote, bump);
      }
    }
    for (int i = 0; i < libor6mCurveSize; i++) {
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> upResults = DISCOUNTING_AND_BANK_BILLS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_FWD6_AUD, FWD6_AUD_GENERATORS[i], FWD6_AUD_ATTR[i], FWD6_AUD_MARKET_QUOTES[i] + bump)
          .buildCurves(NOW);
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> downResults = DISCOUNTING_AND_BANK_BILLS_BUILDER.copy()
          .withFixingTs(FIXING_TS_WITHOUT_TODAY)
          .getBuilder()
          .replaceMarketQuote(CURVE_NAME_FWD6_AUD, FWD6_AUD_GENERATORS[i], FWD6_AUD_ATTR[i], FWD6_AUD_MARKET_QUOTES[i] - bump)
          .buildCurves(NOW);
      final Double[] upYields = ((YieldCurve) upResults.getFirst().getCurve(CURVE_NAME_FWD6_AUD)).getCurve().getYData();
      final Double[] downYields = ((YieldCurve) downResults.getFirst().getCurve(CURVE_NAME_FWD6_AUD)).getCurve().getYData();
      final int offset = i + libor3mCurveSize + discountingCurveSize;
      for (int j = 0; j < libor6mCurveSize; j++) {
        final double dYielddQuote = (upYields[j] - downYields[j]) / (2 * bump);
        final double expectedSensitivity = fullInverseJacobian.getBlock(CURVE_NAME_FWD6_AUD).getSecond().getData()[j][offset];
        assertEquals("Finite difference sensitivities for " + CURVE_NAME_FWD6_AUD + ": column=" + offset + " row=" + j,
            expectedSensitivity, dYielddQuote, bump);
      }
    }
  }

  @Test
  public void comparison1Unit2Units() {
    final MulticurveProviderDiscount[] units =
        new MulticurveProviderDiscount[] { DSC_THEN_BANK_BILLS_BEFORE_FIXING.getFirst(), DSC_BANK_BILLS_SIMULTANEOUS_BEFORE_FIXING.getFirst() };
    final CurveBuildingBlockBundle[] bb =
        new CurveBuildingBlockBundle[] { DSC_THEN_BANK_BILLS_BEFORE_FIXING.getSecond(), DSC_BANK_BILLS_SIMULTANEOUS_BEFORE_FIXING.getSecond() };
    final YieldAndDiscountCurve[] curveDsc = new YieldAndDiscountCurve[] { units[0].getCurve(Currency.AUD), units[1].getCurve(Currency.AUD) };
    final YieldAndDiscountCurve[] curveFwd3 = new YieldAndDiscountCurve[] { units[0].getCurve(AUD_3M_BANK_BILL_INDEX), units[1].getCurve(AUD_3M_BANK_BILL_INDEX) };
    final YieldAndDiscountCurve[] curveFwd6 = new YieldAndDiscountCurve[] { units[0].getCurve(AUD_6M_BANK_BILL_INDEX), units[1].getCurve(AUD_6M_BANK_BILL_INDEX) };
    assertEquals("Curve construction: 1 unit / 2 units ", curveDsc[0].getNumberOfParameters(), curveDsc[1].getNumberOfParameters());
    assertEquals("Curve construction: 1 unit / 2 units ", curveFwd3[0].getNumberOfParameters(), curveFwd3[1].getNumberOfParameters());
    assertEquals("Curve construction: 1 unit / 2 units ", curveFwd6[0].getNumberOfParameters(), curveFwd6[1].getNumberOfParameters());
    assertArrayEquals("Curve construction: 1 unit / 2 units ", ArrayUtils.toPrimitive(((YieldCurve) curveDsc[0]).getCurve().getXData()),
        ArrayUtils.toPrimitive(((YieldCurve) curveDsc[1]).getCurve().getXData()), TOLERANCE_CAL);
    assertArrayEquals("Curve construction: 1 unit / 2 units ", ArrayUtils.toPrimitive(((YieldCurve) curveDsc[0]).getCurve().getYData()),
        ArrayUtils.toPrimitive(((YieldCurve) curveDsc[1]).getCurve().getYData()), TOLERANCE_CAL);
    assertArrayEquals("Curve construction: 1 unit / 2 units ", ArrayUtils.toPrimitive(((YieldCurve) curveFwd3[0]).getCurve().getXData()),
        ArrayUtils.toPrimitive(((YieldCurve) curveFwd3[1]).getCurve().getXData()), TOLERANCE_CAL);
    assertArrayEquals("Curve construction: 1 unit / 2 units ", ArrayUtils.toPrimitive(((YieldCurve) curveFwd3[0]).getCurve().getYData()),
        ArrayUtils.toPrimitive(((YieldCurve) curveFwd3[1]).getCurve().getYData()), TOLERANCE_CAL);
    assertArrayEquals("Curve construction: 1 unit / 2 units ", ArrayUtils.toPrimitive(((YieldCurve) curveFwd6[0]).getCurve().getXData()),
        ArrayUtils.toPrimitive(((YieldCurve) curveFwd6[1]).getCurve().getXData()), TOLERANCE_CAL);
    assertArrayEquals("Curve construction: 1 unit / 2 units ", ArrayUtils.toPrimitive(((YieldCurve) curveFwd6[0]).getCurve().getYData()),
        ArrayUtils.toPrimitive(((YieldCurve) curveFwd6[1]).getCurve().getYData()), TOLERANCE_CAL);

    assertEquals("Curve construction: 1 unit / 2 units ", bb[0].getBlock(CURVE_NAME_FWD3_AUD).getFirst(), bb[1].getBlock(CURVE_NAME_FWD3_AUD).getFirst());
    // Test note: the discounting curve building blocks are not the same; in one case both curves are build together in the other one after the other.
    final int nbLineDsc = bb[0].getBlock(CURVE_NAME_DSC_AUD).getSecond().getNumberOfRows();
    final int nbLineFwd3 = bb[0].getBlock(CURVE_NAME_FWD3_AUD).getSecond().getNumberOfRows();
    final int nbLineFwd6 = bb[0].getBlock(CURVE_NAME_FWD6_AUD).getSecond().getNumberOfRows();
    assertEquals("Curve construction: 1 unit / 2 units ", bb[1].getBlock(CURVE_NAME_DSC_AUD).getSecond().getNumberOfRows(), nbLineDsc);
    assertEquals("Curve construction: 1 unit / 2 units ", bb[1].getBlock(CURVE_NAME_FWD3_AUD).getSecond().getNumberOfRows(), nbLineFwd3);
    assertEquals("Curve construction: 1 unit / 2 units ", bb[1].getBlock(CURVE_NAME_FWD6_AUD).getSecond().getNumberOfRows(), nbLineFwd6);
  }

  @Test(enabled = false)
  public void performance() {
    long startTime, endTime;
    final int nbTest = 100;

    startTime = System.currentTimeMillis();
    DiscountingMethodCurveBuilder builder = DISCOUNTING_THEN_BANK_BILLS_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder();
    for (int i = 0; i < nbTest; i++) {
      builder.buildCurves(NOW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " x 3 curves construction / 2 units: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 2 units: 08-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 810 ms for 100 sets.

    startTime = System.currentTimeMillis();
    builder = DISCOUNTING_AND_BANK_BILLS_BUILDER.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      builder.buildCurves(NOW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " x 3 curves construction / 1 unit: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 1 unit: 08-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 995 ms for 100 sets.

  }

  /**
   * Analyzes the shape of the forward curve.
   */
  @Test(enabled = false)
  public void forwardAnalysis() {
    final MulticurveProviderInterface marketDsc = DSC_THEN_BANK_BILLS_BEFORE_FIXING.getFirst();
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

}
