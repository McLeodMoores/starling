/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueCurveSensitivitySABRSwaptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSensitivitySABRSwaptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSwaptionCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.SABRDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests for the pricing of CMS coupon in the SABR model and pricing by replication.
 */
@Test(groups = TestGroup.UNIT)
public class CouponCMSSABRReplicationMethodTest {
  // Swap 5Y
  private static final Currency CUR = Currency.EUR;
  private static final WorkingDayCalendar CALENDAR = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final IborIndex IBOR_INDEX = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[0];
  private static final boolean IS_EOM = true;
  private static final int ANNUITY_TENOR_YEAR = 5;
  private static final Period ANNUITY_TENOR = Period.ofYears(ANNUITY_TENOR_YEAR);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2014, 3, 17);
  // Fixed leg: Semi-annual bond
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final AnnuityCouponFixedDefinition FIXED_ANNUITY = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR,
      FIXED_PAYMENT_PERIOD, CalendarAdapter.of(CALENDAR), FIXED_DAY_COUNT,
      BUSINESS_DAY, IS_EOM, 1.0, RATE, FIXED_IS_PAYER);
  // Ibor leg: quarterly money
  private static final int SETTLEMENT_DAYS = 2;
  private static final AnnuityCouponIborDefinition IBOR_ANNUITY = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, 1.0,
      IBOR_INDEX, !FIXED_IS_PAYER, CalendarAdapter.of(CALENDAR));
  // CMS coupon construction
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, ANNUITY_TENOR,
      CalendarAdapter.of(CALENDAR));
  private static final SwapFixedIborDefinition SWAP_DEFINITION = new SwapFixedIborDefinition(FIXED_ANNUITY, IBOR_ANNUITY);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2014, 6, 17); // Prefixed
  private static final ZonedDateTime FIXING_DATE = ScheduleCalculator.getAdjustedDate(SETTLEMENT_DATE, -SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime ACCRUAL_START_DATE = SETTLEMENT_DATE;
  private static final ZonedDateTime ACCRUAL_END_DATE = PAYMENT_DATE;
  private static final DayCount PAYMENT_DAY_COUNT = DayCounts.ACT_360;
  private static final double ACCRUAL_FACTOR = PAYMENT_DAY_COUNT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 1000000; // 1m
  private static final CouponCMSDefinition CMS_COUPON_RECEIVER_DEFINITION = CouponCMSDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE,
      ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE,
      SWAP_DEFINITION, CMS_INDEX);
  private static final CouponCMSDefinition CMS_COUPON_PAYER_DEFINITION = CouponCMSDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE,
      ACCRUAL_END_DATE, ACCRUAL_FACTOR, -NOTIONAL, FIXING_DATE,
      SWAP_DEFINITION, CMS_INDEX);
  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final SABRInterestRateParameters SABR_PARAMETER = SABRDataSets.createSABR1();
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR6M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("EUR1YEURIBOR6M",
      CALENDAR);
  private static final SABRSwaptionProviderDiscount SABR_MULTICURVES = new SABRSwaptionProviderDiscount(MULTICURVES, SABR_PARAMETER,
      EUR1YEURIBOR6M);
  // to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 8, 18);
  private static final CouponCMS CMS_COUPON_RECEIVER = (CouponCMS) CMS_COUPON_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CouponCMS CMS_COUPON_PAYER = (CouponCMS) CMS_COUPON_PAYER_DEFINITION.toDerivative(REFERENCE_DATE);
  // Calculators
  private static final PresentValueSABRSwaptionCalculator PVSSC = PresentValueSABRSwaptionCalculator.getInstance();
  private static final PresentValueCurveSensitivitySABRSwaptionCalculator PVCSSSC = PresentValueCurveSensitivitySABRSwaptionCalculator
      .getInstance();
  private static final PresentValueSABRSensitivitySABRSwaptionCalculator PVSSSSC = PresentValueSABRSensitivitySABRSwaptionCalculator
      .getInstance();
  private static final CouponCMSSABRReplicationMethod METHOD = CouponCMSSABRReplicationMethod.getInstance();
  private static final CapFloorCMSSABRReplicationMethod METHOD_CAP = CapFloorCMSSABRReplicationMethod.getDefaultInstance();
  private static final CouponCMSSABRReplicationGenericMethod METHOD_GENERIC = new CouponCMSSABRReplicationGenericMethod(METHOD_CAP);

  /**
   *
   */
  @Test
  public void presentValueSABRReplicationPayerReceiver() {
    final double priceReceiver = CMS_COUPON_RECEIVER.accept(PVSSC, SABR_MULTICURVES).getAmount(CUR);
    final double pricePayer = CMS_COUPON_PAYER.accept(PVSSC, SABR_MULTICURVES).getAmount(CUR);
    assertEquals("Payer/receiver", priceReceiver, -pricePayer);
  }

  /**
   * Tests the method against the present value calculator.
   */
  @Test
  public void presentValueSABRReplicationMethodVsCalculator() {
    final double pvMethod = METHOD.presentValue(CMS_COUPON_PAYER, SABR_MULTICURVES).getAmount(CUR);
    final double pvCalculator = CMS_COUPON_PAYER.accept(PVSSC, SABR_MULTICURVES).getAmount(CUR);
    assertEquals("Coupon CMS SABR: method and calculator", pvMethod, pvCalculator);
  }

  /**
   * Tests the method against the present value calculator.
   */
  @Test
  public void presentValueSABRReplicationMethodSpecificVsGeneric() {
    final double pvSpecific = METHOD.presentValue(CMS_COUPON_PAYER, SABR_MULTICURVES).getAmount(CUR);
    final double pvGeneric = METHOD_GENERIC.presentValue(CMS_COUPON_PAYER, SABR_MULTICURVES).getAmount(CUR);
    assertEquals("Coupon CMS SABR: method : Specific vs Generic", pvSpecific, pvGeneric);
  }

  /**
   * Tests the method against the present value curve sensitivity calculator.
   */
  @Test
  public void presentValueSABRReplicationCurveSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvsMethod = METHOD.presentValueCurveSensitivity(CMS_COUPON_PAYER, SABR_MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvsCalculator = CMS_COUPON_PAYER.accept(PVCSSSC, SABR_MULTICURVES);
    AssertSensitivityObjects.assertEquals("", pvsMethod, pvsCalculator, 1e-9);
  }

  // TODO
  //
  // /**
  // * Test the present value sensitivity to the rates.
  // */
  // @Test
  // public void presentValueSABRReplicationCurveSensitivity() {
  // // Swaption sensitivity
  // InterestRateCurveSensitivity pvsReceiver = METHOD.presentValueCurveSensitivity(CMS_COUPON_RECEIVER, SABR_MULTICURVES);
  // // Present value sensitivity comparison with finite difference.
  // final double deltaTolerance = 1E+2; // Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.
  // final double deltaShift = 1e-9;
  // pvsReceiver = pvsReceiver.cleaned();
  // final double pv = METHOD.presentValue(CMS_COUPON_RECEIVER, SABR_MULTICURVES).getAmount();
  // // 1. Forward curve sensitivity
  // final String bumpedCurveName = "Bumped Curve";
  // final String[] bumpedCurvesForwardName = { FUNDING_CURVE_NAME, bumpedCurveName };
  // final CouponCMS cmsBumpedForward = (CouponCMS) CMS_COUPON_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE, bumpedCurvesForwardName);
  // final YieldAndDiscountCurve curveForward = curves.getCurve(FORWARD_CURVE_NAME);
  // final Set<Double> timeForwardSet = new TreeSet<>();
  // for (final Payment pay : CMS_COUPON_RECEIVER.getUnderlyingSwap().getSecondLeg().getPayments()) {
  // final CouponIbor coupon = (CouponIbor) pay;
  // timeForwardSet.add(coupon.getFixingPeriodStartTime());
  // timeForwardSet.add(coupon.getFixingPeriodEndTime());
  // }
  // final int nbForwardDate = timeForwardSet.size();
  // final List<Double> timeForwardList = new ArrayList<>(timeForwardSet);
  // Double[] timeForwardArray = new Double[nbForwardDate];
  // timeForwardArray = timeForwardList.toArray(timeForwardArray);
  // final double[] yieldsForward = new double[nbForwardDate + 1];
  // final double[] nodeTimesForward = new double[nbForwardDate + 1];
  // yieldsForward[0] = curveForward.getInterestRate(0.0);
  // for (int i = 0; i < nbForwardDate; i++) {
  // nodeTimesForward[i + 1] = timeForwardArray[i];
  // yieldsForward[i + 1] = curveForward.getInterestRate(nodeTimesForward[i + 1]);
  // }
  // final YieldAndDiscountCurve tempCurveForward = YieldCurve
  // .from(InterpolatedDoublesCurve.fromSorted(nodeTimesForward, yieldsForward, new LinearInterpolator1D()));
  // final List<DoublesPair> tempForward = pvsReceiver.getSensitivities().get(FORWARD_CURVE_NAME);
  // for (int i = 0; i < nbForwardDate; i++) {
  // final YieldAndDiscountCurve bumpedCurveForward = tempCurveForward.withSingleShift(nodeTimesForward[i + 1], deltaShift);
  // final YieldCurveBundle curvesBumpedForward = new YieldCurveBundle();
  // curvesBumpedForward.addAll(curves);
  // curvesBumpedForward.setCurve("Bumped Curve", bumpedCurveForward);
  // final SABRInterestRateDataBundle sabrBundleBumped = new SABRInterestRateDataBundle(sabrParameter, curvesBumpedForward);
  // final double bumpedpv = cmsBumpedForward.accept(PVC_SABR, sabrBundleBumped);
  // final double res = (bumpedpv - pv) / deltaShift;
  // final DoublesPair pair = tempForward.get(i);
  // assertEquals("Node " + i, nodeTimesForward[i + 1], pair.getFirst(), 1E-8);
  // assertEquals("Node " + i, res, pair.getSecond(), deltaTolerance);
  // }
  // // 2. Funding curve sensitivity
  // final String[] bumpedCurvesFundingName = { bumpedCurveName, FORWARD_CURVE_NAME };
  // final CouponCMS cmsBumpedFunding = (CouponCMS) CMS_COUPON_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE, bumpedCurvesFundingName);
  // final int nbPayDate = CMS_COUPON_RECEIVER_DEFINITION.getUnderlyingSwap().getIborLeg().getPayments().length;
  // final YieldAndDiscountCurve curveFunding = curves.getCurve(FUNDING_CURVE_NAME);
  // final double[] yieldsFunding = new double[nbPayDate + 1];
  // final double[] nodeTimesFunding = new double[nbPayDate + 1];
  // yieldsFunding[0] = curveFunding.getInterestRate(0.0);
  // for (int i = 0; i < nbPayDate; i++) {
  // nodeTimesFunding[i + 1] = CMS_COUPON_RECEIVER.getUnderlyingSwap().getSecondLeg().getNthPayment(i).getPaymentTime();
  // yieldsFunding[i + 1] = curveFunding.getInterestRate(nodeTimesFunding[i + 1]);
  // }
  // final YieldAndDiscountCurve tempCurveFunding = YieldCurve
  // .from(InterpolatedDoublesCurve.fromSorted(nodeTimesFunding, yieldsFunding, new LinearInterpolator1D()));
  // final List<DoublesPair> tempFunding = pvsReceiver.getSensitivities().get(FUNDING_CURVE_NAME);
  // final double[] res = new double[nbPayDate];
  // for (int i = 0; i < nbPayDate; i++) {
  // final YieldAndDiscountCurve bumpedCurve = tempCurveFunding.withSingleShift(nodeTimesFunding[i + 1], deltaShift);
  // final YieldCurveBundle curvesBumped = new YieldCurveBundle();
  // curvesBumped.addAll(curves);
  // curvesBumped.setCurve("Bumped Curve", bumpedCurve);
  // final SABRInterestRateDataBundle sabrBundleBumped = new SABRInterestRateDataBundle(sabrParameter, curvesBumped);
  // final double bumpedpv = METHOD.presentValue(cmsBumpedFunding, sabrBundleBumped).getAmount();
  // res[i] = (bumpedpv - pv) / deltaShift;
  // final DoublesPair pair = tempFunding.get(i);
  // assertEquals("Node " + i, nodeTimesFunding[i + 1], pair.getFirst(), 1E-8);
  // assertEquals("Node " + i, res[i], pair.getSecond(), deltaTolerance);
  // }
  // }

  /**
   * Test the present value sensitivity to the SABR parameters.
   */
  @Test
  public void presentValueSABRReplicationSABRSensitivity() {
    // Swaption sensitivity
    final PresentValueSABRSensitivityDataBundle pvsReceiver = METHOD.presentValueSABRSensitivity(CMS_COUPON_RECEIVER, SABR_MULTICURVES);
    PresentValueSABRSensitivityDataBundle pvsPayer = METHOD.presentValueSABRSensitivity(CMS_COUPON_PAYER, SABR_MULTICURVES);
    // Long/short parity
    pvsPayer = pvsPayer.multiplyBy(-1.0);
    assertEquals(pvsPayer.getAlpha(), pvsReceiver.getAlpha());
    // SABR sensitivity vs finite difference
    final double pvLongPayer = METHOD.presentValue(CMS_COUPON_RECEIVER, SABR_MULTICURVES).getAmount(CUR);
    final double shift = 0.0001;
    final double shiftAlpha = 0.00001;
    final DoublesPair expectedExpiryTenor = DoublesPair.of(CMS_COUPON_RECEIVER.getFixingTime(), ANNUITY_TENOR_YEAR + 1.0 / 365.0);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterAlphaBumped = SABRDataSets.createSABR1AlphaBumped(shiftAlpha);
    final SABRSwaptionProviderDiscount sabrBundleAlphaBumped = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterAlphaBumped,
        EUR1YEURIBOR6M);
    final double pvLongPayerAlphaBumped = METHOD.presentValue(CMS_COUPON_RECEIVER, sabrBundleAlphaBumped).getAmount(CUR);
    final double expectedAlphaSensi = (pvLongPayerAlphaBumped - pvLongPayer) / shiftAlpha;
    assertEquals("Number of alpha sensitivity", pvsReceiver.getAlpha().getMap().keySet().size(), 1);
    assertEquals("Alpha sensitivity expiry/tenor", pvsReceiver.getAlpha().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Alpha sensitivity value", expectedAlphaSensi, pvsReceiver.getAlpha().getMap().get(expectedExpiryTenor), 150.0);
    // Rho sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterRhoBumped = SABRDataSets.createSABR1RhoBumped();
    final SABRSwaptionProviderDiscount sabrBundleRhoBumped = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterRhoBumped,
        EUR1YEURIBOR6M);
    final double pvLongPayerRhoBumped = METHOD.presentValue(CMS_COUPON_RECEIVER, sabrBundleRhoBumped).getAmount(CUR);
    final double expectedRhoSensi = (pvLongPayerRhoBumped - pvLongPayer) / shift;
    assertEquals("Number of rho sensitivity", pvsReceiver.getRho().getMap().keySet().size(), 1);
    assertEquals("Rho sensitivity expiry/tenor", pvsReceiver.getRho().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Rho sensitivity value", pvsReceiver.getRho().getMap().get(expectedExpiryTenor), expectedRhoSensi, 2.0);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterNuBumped = SABRDataSets.createSABR1NuBumped();
    final SABRSwaptionProviderDiscount sabrBundleNuBumped = new SABRSwaptionProviderDiscount(MULTICURVES, sabrParameterNuBumped,
        EUR1YEURIBOR6M);
    final double pvLongPayerNuBumped = METHOD.presentValue(CMS_COUPON_RECEIVER, sabrBundleNuBumped).getAmount(CUR);
    final double expectedNuSensi = (pvLongPayerNuBumped - pvLongPayer) / shift;
    assertEquals("Number of nu sensitivity", pvsReceiver.getNu().getMap().keySet().size(), 1);
    assertEquals("Nu sensitivity expiry/tenor", pvsReceiver.getNu().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Nu sensitivity value", pvsReceiver.getNu().getMap().get(expectedExpiryTenor), expectedNuSensi, 15.0);
  }

  /**
   * Tests the present value SABR parameters sensitivity: Method vs Calculator.
   */
  @Test
  public void presentValueSABRReplicationSABRSensitivityMethodVsCalculator() {
    final PresentValueSABRSensitivityDataBundle pvssMethod = METHOD.presentValueSABRSensitivity(CMS_COUPON_RECEIVER, SABR_MULTICURVES);
    final PresentValueSABRSensitivityDataBundle pvssCalculator = CMS_COUPON_RECEIVER.accept(PVSSSSC, SABR_MULTICURVES);
    assertEquals("CMS cap/floor SABR: Present value SABR sensitivity: method vs calculator", pvssMethod, pvssCalculator);
  }

}
