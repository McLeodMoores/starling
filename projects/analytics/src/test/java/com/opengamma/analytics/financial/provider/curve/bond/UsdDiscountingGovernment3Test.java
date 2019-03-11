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

import static com.opengamma.analytics.financial.provider.curve.CurveBuildingTestUtils.assertFiniteDifferenceSensitivities;
import static com.opengamma.analytics.financial.provider.curve.CurveBuildingTestUtils.curveConstructionTest;
import static org.testng.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.financial.curve.interestrate.CurveBuilder;
import com.mcleodmoores.analytics.financial.curve.interestrate.DiscountingMethodBondCurveBuilder;
import com.mcleodmoores.analytics.financial.curve.interestrate.DiscountingMethodBondCurveSetUp;
import com.mcleodmoores.analytics.financial.index.Index;
import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorBill;
import com.opengamma.analytics.financial.instrument.index.GeneratorBondFixed;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositON;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositONCounterpart;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityShortName;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingTests;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.factory.FlatExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LinearInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.NamedInterpolator1dFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
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
 * Builds and tests discounting and US government curves. The discounting curve and government curves are constructed simultaneously.
 * <p>
 * The discounting curve contains the overnight deposit rate and OIS swaps. The government curve contains the overnight deposit rate and bills, notes and bonds.
 */
@Test(groups = TestGroup.UNIT)
public class UsdDiscountingGovernment3Test extends CurveBuildingTests {
  /** The interpolator used for both curves */
  private static final Interpolator1D INTERPOLATOR = NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME);
  /** A calendar containing only Saturday and Sunday holidays */
  private static final WorkingDayCalendar NYC = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;
  /** A calendar containing only Saturday and Sunday holidays */
  private static final Calendar NYC_OLD = CalendarAdapter.of(NYC);
  /** The base FX matrix */
  private static final FXMatrix FX_MATRIX = new FXMatrix(Currency.USD);
  /** Generates OIS swaps for the discounting curve */
  private static final GeneratorSwapFixedON GENERATOR_OIS_USD = GeneratorSwapFixedONMaster.getInstance().getGenerator("USD1YFEDFUND", NYC_OLD);
  /** A Fed funds index */
  private static final IndexON FED_FUNDS_INDEX = GENERATOR_OIS_USD.getIndex();
  /** Generates the overnight deposit */
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_USD = new GeneratorDepositON("USD Deposit ON", Currency.USD, NYC_OLD,
      FED_FUNDS_INDEX.getDayCount());
  /** The issuer */
  private static final String NAME_COUNTERPART = "US GOVT";
  /** Generates a deposit with the counterparty (issuer) */
  private static final GeneratorDepositONCounterpart GENERATOR_DEPOSIT_ON_USGOVT = new GeneratorDepositONCounterpart("US GOVT Deposit ON", Currency.USD,
      NYC_OLD, DayCounts.ACT_360, NAME_COUNTERPART);
  /** Yield convention for bills */
  private static final YieldConvention YIELD_BILL_USGOVT = YieldConventionFactory.INSTANCE.getYieldConvention("INTEREST@MTY");
  /** The bill maturities */
  private static final ZonedDateTime[] BILL_MATURITY = new ZonedDateTime[] { DateUtils.getUTCDate(2012, 9, 28), DateUtils.getUTCDate(2012, 11, 30),
      DateUtils.getUTCDate(2013, 2, 28) };
  /** The bill securities */
  private static final BillSecurityDefinition[] BILL_SECURITY = new BillSecurityDefinition[BILL_MATURITY.length];
  /** Generates the bills */
  private static final GeneratorBill[] GENERATOR_BILL = new GeneratorBill[BILL_MATURITY.length];
  static {
    for (int i = 0; i < BILL_MATURITY.length; i++) {
      BILL_SECURITY[i] = new BillSecurityDefinition(Currency.USD, BILL_MATURITY[i], 1, 1, NYC, YIELD_BILL_USGOVT, DayCounts.ACT_360, NAME_COUNTERPART);
      GENERATOR_BILL[i] = new GeneratorBill("GeneratorBill" + i, BILL_SECURITY[i]);
    }
  }
  // Here we define US NOTES and US BONDS. We make no distinction between notes and bonds because the instrument is exactly the same.
  // typically US NOTES are short maturity interest rate bonds(ie under 10Y) and US BONDS are long maturity interest rate bonds
  // (i.e. more than 10y but mostly 30y in practice).
  // To build the curve we choose six bonds, the most recent 2y, 3y, 5y, 7y, 10yand 30y bond:
  // USA, Note 0.125 31jul2014 2Y (ISIN US912828TF73)
  // USA, Note 0.25 15aug2015 3Y (ISIN US912828TK68)
  // USA, Note 0.5 31jul2017 5Y (ISIN US912828TG56)
  // USA, Note 0.875 31jul2019 7Y (ISIN US912828TH30)
  // USA, Note 1.625 15aug2022 10Y (ISIN US912828TJ95)
  // USA, Bond 2.75 15aug2042 30Y (ISIN US912810QX90)
  /** Yield convention for bonds */
  private static final YieldConvention YIELD_BOND_USGOVT = YieldConventionFactory.INSTANCE.getYieldConvention("INTEREST@MTY");
  /** Bond accrual start dates */
  private static final ZonedDateTime[] BOND_START_ACCRUAL_DATE = new ZonedDateTime[] { DateUtils.getUTCDate(2012, 07, 31), DateUtils.getUTCDate(2012, 8, 15),
      DateUtils.getUTCDate(2012, 07, 31), DateUtils.getUTCDate(2012, 07, 31), DateUtils.getUTCDate(2012, 8, 15), DateUtils.getUTCDate(2012, 8, 15) };
  /** Bond maturities */
  private static final ZonedDateTime[] BOND_MATURITY = new ZonedDateTime[] { DateUtils.getUTCDate(2014, 07, 31), DateUtils.getUTCDate(2015, 05, 15),
      DateUtils.getUTCDate(2017, 07, 31), DateUtils.getUTCDate(2019, 07, 31), DateUtils.getUTCDate(2022, 8, 15), DateUtils.getUTCDate(2042, 8, 15) };
  /** Coupons */
  private static final double[] RATE_FIXED = new double[] { 0.00125, 0.00250, 0.00500, 0.00875, 0.01625, 0.02750 };
  /** The bond securities */
  private static final BondFixedSecurityDefinition[] BOND_SECURITY = new BondFixedSecurityDefinition[BOND_MATURITY.length];
  /** Generates the bonds */
  private static final GeneratorBondFixed[] GENERATOR_BOND = new GeneratorBondFixed[BOND_MATURITY.length];
  static {
    for (int i = 0; i < BOND_MATURITY.length; i++) {
      BOND_SECURITY[i] = BondFixedSecurityDefinition.from(Currency.USD, BOND_MATURITY[i], BOND_START_ACCRUAL_DATE[i], Period.ofMonths(6), RATE_FIXED[i], 3, 1,
          NYC_OLD, DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, YIELD_BOND_USGOVT, false, NAME_COUNTERPART, "General collateral");
      GENERATOR_BOND[i] = new GeneratorBondFixed("GeneratorBond" + i, BOND_SECURITY[i]);
    }
  }
  /** The curve construction date */
  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2012, 8, 22);
  /** Fixing time series of overnight rates after today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries
      .ofUTC(new ZonedDateTime[] { NOW.minusDays(1), NOW }, new double[] { 0.07, 0.08 });
  /** Fixing time series of overnight rates before today's fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries
      .ofUTC(new ZonedDateTime[] { NOW.minusDays(1) }, new double[] { 0.07 });
  /** Fixing time series created before the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITHOUT_TODAY = new HashMap<>();
  /** Fixing time series created after the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITH_TODAY = new HashMap<>();
  static {
    FIXING_TS_WITHOUT_TODAY.put(FED_FUNDS_INDEX, TS_ON_USD_WITHOUT_TODAY);
    FIXING_TS_WITH_TODAY.put(FED_FUNDS_INDEX, TS_ON_USD_WITH_TODAY);
  }
  /** The discounting curve name */
  private static final String CURVE_NAME_DSC_USD = "USD Dsc";
  /** The government curve name */
  private static final String CURVE_NAME_GOVTUS_USD = "USD GOVT US";
  /** Already known market data - contains only an empty FX matrix */
  private static final IssuerProviderDiscount KNOWN_DATA = new IssuerProviderDiscount(new MulticurveProviderDiscount(FX_MATRIX),
      new HashMap<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve>());
  /** The curve builder */
  private static final DiscountingMethodBondCurveSetUp BUILDER_FOR_TEST = DiscountingMethodBondCurveBuilder.setUp()
      .building(CURVE_NAME_DSC_USD, CURVE_NAME_GOVTUS_USD).using(CURVE_NAME_DSC_USD).forDiscounting(Currency.USD)
      .forOvernightIndex(FED_FUNDS_INDEX.toOvernightIndex()).withInterpolator(INTERPOLATOR).using(CURVE_NAME_GOVTUS_USD)
      .forIssuer(Pairs.<Object, LegalEntityFilter<LegalEntity>> of(NAME_COUNTERPART, new LegalEntityShortName())).withInterpolator(INTERPOLATOR)
      .withKnownData(KNOWN_DATA);
  /** Market values for the discounting curve */
  private static final double[] DSC_USD_MARKET_QUOTES = new double[] { 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400,
      0.0400 };
  /** Vanilla instrument generators for the discounting curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_USD_GENERATORS = new GeneratorInstrument<?>[] { GENERATOR_DEPOSIT_ON_USD,
      GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD,
      GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD };
  /** Attribute generators for the discounting curve */
  private static final GeneratorAttributeIR[] DSC_USD_ATTR;
  static {
    final Period[] tenors = new Period[] { Period.ofDays(0), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9),
        Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
    DSC_USD_ATTR = new GeneratorAttributeIR[tenors.length];
    for (int i = 0; i < tenors.length; i++) {
      DSC_USD_ATTR[i] = new GeneratorAttributeIR(tenors[i]);
      BUILDER_FOR_TEST.withNode(CURVE_NAME_DSC_USD, DSC_USD_GENERATORS[i], DSC_USD_ATTR[i], DSC_USD_MARKET_QUOTES[i]);
    }
  }
  /** Market values for the government curve */
  private static final double[] GOVTUS_USD_MARKET_QUOTES = new double[] { 0.0010, 0.0015, 0.0020, 0.0015, 0.99642, 0.9981, 0.99587, 0.99466, 0.99496, 0.98489 };
  /** Vanilla instrument generators for the government curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] GOVTUS_USD_GENERATORS = new GeneratorInstrument<?>[] { GENERATOR_DEPOSIT_ON_USGOVT,
      GENERATOR_BILL[0], GENERATOR_BILL[1], GENERATOR_BILL[2], GENERATOR_BOND[0], GENERATOR_BOND[1], GENERATOR_BOND[2], GENERATOR_BOND[3], GENERATOR_BOND[4],
      GENERATOR_BOND[5] };
  /** Attribute generators for the government curve */
  private static final GeneratorAttributeIR[] GOVTUS_USD_ATTR;
  static {
    final Period[] tenors = new Period[] { Period.ofDays(0), Period.ofDays(0), Period.ofDays(0), Period.ofDays(0), Period.ofDays(0), Period.ofDays(0),
        Period.ofDays(0), Period.ofDays(0), Period.ofDays(0), Period.ofDays(0) };
    GOVTUS_USD_ATTR = new GeneratorAttributeIR[tenors.length];
    for (int i = 0; i < tenors.length; i++) {
      GOVTUS_USD_ATTR[i] = new GeneratorAttributeIR(tenors[i]);
      BUILDER_FOR_TEST.withNode(CURVE_NAME_GOVTUS_USD, GOVTUS_USD_GENERATORS[i], GOVTUS_USD_ATTR[i], GOVTUS_USD_MARKET_QUOTES[i]);
    }
  }
  /** Curves constructed before today's fixing */
  private static final Pair<IssuerProviderDiscount, CurveBuildingBlockBundle> BEFORE_TODAYS_FIXING;
  /** Curves constructed after today's fixing */
  private static final Pair<IssuerProviderDiscount, CurveBuildingBlockBundle> AFTER_TODAYS_FIXING;
  // build curves before and after today's fixing
  static {
    BEFORE_TODAYS_FIXING = BUILDER_FOR_TEST.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder().buildCurves(NOW);
    AFTER_TODAYS_FIXING = BUILDER_FOR_TEST.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().buildCurves(NOW);
  }

  @Override
  @Test
  public void testJacobianSize() {
    final CurveBuildingBlockBundle fullJacobian = BEFORE_TODAYS_FIXING.getSecond();
    final Map<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> fullJacobianData = fullJacobian.getData();
    assertEquals(fullJacobianData.size(), 2);
    final DoubleMatrix2D discountingJacobianMatrix = fullJacobianData.get(CURVE_NAME_DSC_USD).getSecond();
    assertEquals(discountingJacobianMatrix.getNumberOfRows(), DSC_USD_MARKET_QUOTES.length);
    assertEquals(discountingJacobianMatrix.getNumberOfColumns(), GOVTUS_USD_MARKET_QUOTES.length + DSC_USD_MARKET_QUOTES.length);
    final DoubleMatrix2D governmentJacobianMatrix = fullJacobianData.get(CURVE_NAME_GOVTUS_USD).getSecond();
    assertEquals(governmentJacobianMatrix.getNumberOfRows(), GOVTUS_USD_MARKET_QUOTES.length);
    assertEquals(governmentJacobianMatrix.getNumberOfColumns(), GOVTUS_USD_MARKET_QUOTES.length + DSC_USD_MARKET_QUOTES.length);
  }

  @Override
  @Test
  public void testInstrumentsInCurvePriceToZero() {
    Map<String, InstrumentDefinition<?>[]> definitions = BUILDER_FOR_TEST.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder()
        .getDefinitionsForCurves(NOW);
    curveConstructionTest(definitions.get(CURVE_NAME_DSC_USD), BEFORE_TODAYS_FIXING.getFirst(), PresentValueIssuerCalculator.getInstance(),
        FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.USD);
    curveConstructionTest(definitions.get(CURVE_NAME_GOVTUS_USD), BEFORE_TODAYS_FIXING.getFirst(), PresentValueIssuerCalculator.getInstance(),
        FIXING_TS_WITHOUT_TODAY, FX_MATRIX, NOW, Currency.USD);
    definitions = BUILDER_FOR_TEST.copy().withFixingTs(FIXING_TS_WITH_TODAY).getBuilder().getDefinitionsForCurves(NOW);
    curveConstructionTest(definitions.get(CURVE_NAME_DSC_USD), AFTER_TODAYS_FIXING.getFirst(), PresentValueIssuerCalculator.getInstance(), FIXING_TS_WITH_TODAY,
        FX_MATRIX, NOW, Currency.USD);
    curveConstructionTest(definitions.get(CURVE_NAME_GOVTUS_USD), AFTER_TODAYS_FIXING.getFirst(), PresentValueIssuerCalculator.getInstance(),
        FIXING_TS_WITH_TODAY, FX_MATRIX, NOW, Currency.USD);
  }

  @Override
  @Test
  public void testFiniteDifferenceSensitivities() {
    testDiscountingCurveSensitivities(BEFORE_TODAYS_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY);
    testDiscountingCurveSensitivities(AFTER_TODAYS_FIXING.getSecond(), FIXING_TS_WITH_TODAY);
    testGovernmentCurveSensitivities(BEFORE_TODAYS_FIXING.getSecond(), FIXING_TS_WITHOUT_TODAY);
    testGovernmentCurveSensitivities(AFTER_TODAYS_FIXING.getSecond(), FIXING_TS_WITH_TODAY);
  }

  /**
   * Tests the sensitivities of the discounting curve to changes in the market data points used in the discounting curve and government curve. The sensitivities
   * to the government curve should be zero.
   *
   * @param fullInverseJacobian
   *          analytic sensitivities
   * @param fixingTs
   *          the fixing time series
   */
  private static void testDiscountingCurveSensitivities(final CurveBuildingBlockBundle fullInverseJacobian,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs) {
    // sensitivities to discounting
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, BUILDER_FOR_TEST, CURVE_NAME_DSC_USD, CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS,
        DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    // sensitivities to the government curve should be zero
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, BUILDER_FOR_TEST, CURVE_NAME_DSC_USD, CURVE_NAME_GOVTUS_USD, NOW, GOVTUS_USD_GENERATORS,
        GOVTUS_USD_ATTR, GOVTUS_USD_MARKET_QUOTES, true);
  }

  /**
   * Tests the sensitivities of the government curve to changes in the market data points used in the discounting and government curves.
   *
   * @param fullInverseJacobian
   *          analytic sensitivities
   * @param fixingTs
   *          the fixing time series
   */
  private static void testGovernmentCurveSensitivities(final CurveBuildingBlockBundle fullInverseJacobian,
      final Map<Index, ZonedDateTimeDoubleTimeSeries> fixingTs) {
    // sensitivities to discounting
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, BUILDER_FOR_TEST, CURVE_NAME_GOVTUS_USD, CURVE_NAME_DSC_USD, NOW, DSC_USD_GENERATORS,
        DSC_USD_ATTR, DSC_USD_MARKET_QUOTES, false);
    // sensitivities to the government curve
    assertFiniteDifferenceSensitivities(fullInverseJacobian, fixingTs, BUILDER_FOR_TEST, CURVE_NAME_GOVTUS_USD, CURVE_NAME_GOVTUS_USD, NOW,
        GOVTUS_USD_GENERATORS, GOVTUS_USD_ATTR, GOVTUS_USD_MARKET_QUOTES, false);
  }

  /**
   * Only one set of curves is constructed, so no tests are possible.
   */
  @Override
  @Test
  public void testSameCurvesDifferentMethods() {
    return;
  }

  /**
   * Performance tests.
   */
  @Test(enabled = false)
  public void performance() {
    long startTime, endTime;
    final int nbTest = 100;

    startTime = System.currentTimeMillis();
    final CurveBuilder<IssuerProviderDiscount> builder = BUILDER_FOR_TEST.copy().withFixingTs(FIXING_TS_WITHOUT_TODAY).getBuilder();
    for (int i = 0; i < nbTest; i++) {
      builder.buildCurves(NOW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction / 2 units: " + (endTime - startTime) + " ms");
  }
}
