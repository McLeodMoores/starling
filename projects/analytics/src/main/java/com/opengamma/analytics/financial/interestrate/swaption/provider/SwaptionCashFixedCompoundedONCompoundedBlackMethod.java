/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.provider;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedAccruedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONCompounded;
import com.opengamma.analytics.financial.interestrate.sensitivity.PresentValueBlackSwaptionSensitivity;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.provider.SwapFixedCompoundingONCompoundingDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSwaptionFlatProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Class used to compute the price and sensitivity of a cash-settled swaption with the Black model.
 */
// TODO: Complete the code when the definition of cash settlement is clear for those swaptions.
public final class SwaptionCashFixedCompoundedONCompoundedBlackMethod {

  /**
   * The method unique instance.
   */
  private static final SwaptionCashFixedCompoundedONCompoundedBlackMethod INSTANCE = new SwaptionCashFixedCompoundedONCompoundedBlackMethod();

  /**
   * Return the unique instance of the class.
   *
   * @return The instance.
   */
  public static SwaptionCashFixedCompoundedONCompoundedBlackMethod getInstance() {
    return INSTANCE;
  }

  // /**
  // * The present value curve sensitivity calculator.
  // */
  // private static final PresentValueCurveSensitivityCalculator PV_SENSITIVITY_CALCULATOR = PresentValueCurveSensitivityCalculator
  // .getInstance();
  // /**
  // * The present value calculator.
  // */
  // private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();

  /**
   * Private constructor.
   */
  private SwaptionCashFixedCompoundedONCompoundedBlackMethod() {
  }

  /**
   * The swap method.
   */
  private static final SwapFixedCompoundingONCompoundingDiscountingMethod METHOD_SWAP = SwapFixedCompoundingONCompoundingDiscountingMethod
      .getInstance();
  /**
   * The par rate calculator.
   */
  private static final ParRateDiscountingCalculator PRC = ParRateDiscountingCalculator.getInstance();

  /**
   * Computes the present value of a cash-settled European swaption in the Black model.
   *
   * @param swaption
   *          The swaption.
   * @param marketData
   *          The curves with Black volatility data.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final SwaptionCashFixedCompoundedONCompounded swaption,
      final BlackSwaptionFlatProviderInterface marketData) {
    ArgumentChecker.notNull(marketData, "curves");
    ArgumentChecker.notNull(swaption, "swaption");
    final GeneratorInstrument<GeneratorAttributeIR> generatorSwap = marketData.getBlackParameters().getGeneratorSwap();
    final GeneratorSwapFixedCompoundedONCompounded fixedCompoundedON = (GeneratorSwapFixedCompoundedONCompounded) generatorSwap;
    final Calendar calendar = fixedCompoundedON.getOvernightCalendar();
    final double tenor = swaption.getMaturityTime();
    final double forward = presentValueBasisPoint(swaption.getUnderlyingSwap(),
        fixedCompoundedON.getFixedLegDayCount(), calendar, marketData.getMulticurveProvider());
    // final double forward = swaption.getUnderlyingSwap().accept(PRC, marketData.getMulticurveProvider());
    // final double forward = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), fixedCompoundedON.getFixedLegDayCount(),
    // calendar, curveBlack);
    final double pvbp = METHOD_SWAP.getAnnuityCash(swaption.getUnderlyingSwap(), forward);
    // Implementation comment: cash-settled swaptions make sense only for constant strike, the computation of coupon equivalent is not
    // required.
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double volatility = marketData.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), tenor);
    final double discountFactorSettle = marketData.getMulticurveProvider().getDiscountFactor(swaption.getCurrency(),
        swaption.getSettlementTime());
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, discountFactorSettle * pvbp, volatility);
    final Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(swaption);
    final double price = func.evaluate(dataBlack) * (swaption.isLong() ? 1.0 : -1.0);
    return MultipleCurrencyAmount.of(swaption.getCurrency(), price);
  }

  /**
   * Computes the present value rate sensitivity to rates of a cash-settled European swaption in the Black model.
   *
   * @param swaption
   *          The swaption.
   * @param marketData
   *          The curves with Black volatility data.
   * @return The present value curve sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final SwaptionCashFixedCompoundedONCompounded swaption,
      final BlackSwaptionFlatProviderInterface marketData) {
    ArgumentChecker.notNull(marketData, "curves");
    ArgumentChecker.notNull(swaption, "swaption");
    // ArgumentChecker.notNull(swaption, "Swaption");
    // ArgumentChecker.notNull(curveBlack, "Curves with Black volatility");
    // final Annuity<? extends Payment> annuityFixed = swaption.getUnderlyingSwap().getFirstLeg();
    // final double tenor = swaption.getMaturityTime();
    // final GeneratorInstrument<GeneratorAttributeIR> generatorSwap = curveBlack.getBlackParameters().getGeneratorSwap();
    // final GeneratorSwapFixedCompoundedONCompounded fixedCompoundedON = (GeneratorSwapFixedCompoundedONCompounded) generatorSwap;
    // final Calendar calendar = fixedCompoundedON.getOvernightCalendar();
    // final DayCount dayCount = fixedCompoundedON.getFixedLegDayCount();
    // final double forward = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), dayCount, calendar, curveBlack);
    // final Swap<CouponFixedAccruedCompounding, CouponONCompounded> swap = swaption.getUnderlyingSwap();
    // // Derivative of the forward with respect to the rates.
    // final double pvSecond = swap.getSecondLeg().accept(PVC, curveBlack) *
    // Math.signum(swap.getSecondLeg().getNthPayment(0).getNotional());
    // final InterestRateCurveSensitivity pvbpDr = METHOD_SWAP.presentValueBasisPointCurveSensitivity(swap, dayCount, curveBlack);
    // final InterestRateCurveSensitivity pvSecondDr = new
    // InterestRateCurveSensitivity(swap.getSecondLeg().accept(PV_SENSITIVITY_CALCULATOR,
    // curveBlack)).multipliedBy(Math
    // .signum(swap.getSecondLeg().getNthPayment(0).getNotional()));
    // final double pvbp = METHOD_SWAP.getAnnuityCash(swap, forward);
    // final InterestRateCurveSensitivity forwardDr = pvSecondDr.multipliedBy(1.0 / pvbp).plus(pvbpDr.multipliedBy(-pvSecond / (pvbp *
    // pvbp)));
    // // Derivative of the cash annuity with respect to the forward.
    // final double pvbpDf = METHOD_SWAP.getAnnuityCashDerivative(swaption.getUnderlyingSwap(), forward);
    // // Implementation note: strictly speaking, the strike equivalent is curve dependent; that dependency is ignored.
    // final BlackPriceFunction blackFunction = new BlackPriceFunction();
    // final double volatility = curveBlack.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), tenor);
    // final double discountFactorSettle =
    // curveBlack.getCurve(annuityFixed.getNthPayment(0).getFundingCurveName()).getDiscountFactor(swaption.getSettlementTime());
    // final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
    // final double[] bsAdjoint = blackFunction.getPriceAdjoint(swaption, dataBlack);
    // final double sensiDF = -swaption.getSettlementTime() * discountFactorSettle * pvbp * bsAdjoint[0];
    // final List<DoublesPair> list = new ArrayList<>();
    // list.add(DoublesPair.of(swaption.getSettlementTime(), sensiDF));
    // final Map<String, List<DoublesPair>> resultMap = new HashMap<>();
    // resultMap.put(annuityFixed.getNthPayment(0).getFundingCurveName(), list);
    // InterestRateCurveSensitivity result = new InterestRateCurveSensitivity(resultMap);
    // result = result.plus(forwardDr.multipliedBy(discountFactorSettle * (pvbpDf * bsAdjoint[0] + pvbp * bsAdjoint[1])));
    // if (!swaption.isLong()) {
    // result = result.multipliedBy(-1);
    // }
    // return result;
    throw new NotImplementedException();
  }

  /**
   * Computes the present value sensitivity to the Black volatility (also called vega) of a cash-settled European swaption in the Black
   * swaption model.
   *
   * @param swaption
   *          The swaption.
   * @param marketData
   *          The curves with Black volatility data.
   * @return The present value Black sensitivity.
   */
  public PresentValueBlackSwaptionSensitivity presentValueBlackSensitivity(final SwaptionCashFixedCompoundedONCompounded swaption,
      final BlackSwaptionFlatProviderInterface marketData) {
    ArgumentChecker.notNull(marketData, "curves");
    ArgumentChecker.notNull(swaption, "swaption");
    // ArgumentChecker.notNull(swaption, "Swaption");
    // ArgumentChecker.notNull(curveBlack, "Curves with Black volatility");
    // final Annuity<? extends Payment> annuityFixed = swaption.getUnderlyingSwap().getFirstLeg();
    // final GeneratorInstrument<GeneratorAttributeIR> generatorSwap = curveBlack.getBlackParameters().getGeneratorSwap();
    // final GeneratorSwapFixedCompoundedONCompounded fixedCompoundedON = (GeneratorSwapFixedCompoundedONCompounded) generatorSwap;
    // final Calendar calendar = fixedCompoundedON.getOvernightCalendar();
    // final double forward = METHOD_SWAP.presentValueBasisPoint(swaption.getUnderlyingSwap(), fixedCompoundedON.getFixedLegDayCount(),
    // calendar, curveBlack);
    // final double pvbp = METHOD_SWAP.getAnnuityCash(swaption.getUnderlyingSwap(), forward);
    // final double discountFactorSettle =
    // curveBlack.getCurve(annuityFixed.getNthPayment(0).getFundingCurveName()).getDiscountFactor(swaption.getSettlementTime());
    // final DoublesPair point = DoublesPair.of(swaption.getTimeToExpiry(), swaption.getMaturityTime());
    // final BlackPriceFunction blackFunction = new BlackPriceFunction();
    // final double volatility = curveBlack.getBlackParameters().getVolatility(point);
    // final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
    // final double[] bsAdjoint = blackFunction.getPriceAdjoint(swaption, dataBlack);
    // final Map<DoublesPair, Double> sensitivity = new HashMap<>();
    // sensitivity.put(point, bsAdjoint[2] * pvbp * discountFactorSettle * (swaption.isLong() ? 1.0 : -1.0));
    // return new PresentValueBlackSwaptionSensitivity(sensitivity, curveBlack.getBlackParameters().getGeneratorSwap());
    throw new NotImplementedException();
  }

  /**
   * Computes the implied Black volatility of the swaption.
   *
   * @param swaption
   *          The swaption.
   * @param marketData
   *          The market data
   * @return The implied volatility.
   */
  public double impliedVolatility(final SwaptionCashFixedCompoundedONCompounded swaption,
      final BlackSwaptionFlatProviderInterface marketData) {
    ArgumentChecker.notNull(marketData, "curves");
    ArgumentChecker.notNull(swaption, "swaption");
    final double tenor = swaption.getMaturityTime();
    final double volatility = marketData.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), tenor);
    return volatility;
  }

  private static double presentValueBasisPoint(final Swap<CouponFixedAccruedCompounding, CouponONCompounded> swap, final DayCount dayCount,
      final Calendar calendar, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(swap, "swap");
    ArgumentChecker.notNull(dayCount, "day count");
    ArgumentChecker.notNull(multicurves, "Multi-curves provider");
    final Annuity<CouponFixedAccruedCompounding> annuityFixed = swap.getFirstLeg();
    double pvbp = 0;
    for (int i = 0; i < annuityFixed.getPayments().length; i++) {
      pvbp += dayCount.getDayCountFraction(annuityFixed.getNthPayment(i).getAccrualStartDate(),
          annuityFixed.getNthPayment(i).getAccrualEndDate(),
          calendar)
          * Math.abs(annuityFixed.getNthPayment(i).getNotional())
          * multicurves.getDiscountFactor(annuityFixed.getNthPayment(i).getCurrency(),
              annuityFixed.getNthPayment(i).getPaymentTime());
    }
    return pvbp;
  }

}
