/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedAccruedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONCompounded;
import com.opengamma.analytics.financial.interestrate.sensitivity.PresentValueBlackSwaptionSensitivity;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.provider.SwapFixedCompoundingONCompoundingDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSwaptionFlatProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class used to compute the price and sensitivity of a physical delivery swaption with Black model. The implied Black volatilities are
 * expiry and underlying maturity dependent. The swap underlying the swaption should be a Fixed for Ibor (without spread) swap.
 */
public final class SwaptionPhysicalFixedCompoundedONCompoundedBlackMethod {

  /**
   * The method unique instance.
   */
  private static final SwaptionPhysicalFixedCompoundedONCompoundedBlackMethod INSTANCE = new SwaptionPhysicalFixedCompoundedONCompoundedBlackMethod();

  /**
   * Return the unique instance of the class.
   *
   * @return The instance.
   */
  public static SwaptionPhysicalFixedCompoundedONCompoundedBlackMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private SwaptionPhysicalFixedCompoundedONCompoundedBlackMethod() {
  }

  /**
   * The swap method.
   */
  private static final SwapFixedCompoundingONCompoundingDiscountingMethod METHOD_SWAP = SwapFixedCompoundingONCompoundingDiscountingMethod
      .getInstance();

  /**
   * Computes the present value of a physical delivery European swaption in the Black model.
   *
   * @param swaption
   *          The swaption.
   * @param marketData
   *          The curves with Black volatility data.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final SwaptionPhysicalFixedCompoundedONCompounded swaption,
      final BlackSwaptionFlatProviderInterface marketData) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(marketData, "marketData");
    final Swap<CouponFixedAccruedCompounding, CouponONCompounded> swap = swaption.getUnderlyingSwap();
    final CouponFixedAccruedCompounding cpnFixed = swap.getFirstLeg().getNthPayment(0);
    final double numeraire = marketData.getMulticurveProvider().getDiscountFactor(cpnFixed.getCurrency(), cpnFixed.getPaymentTime())
        * Math.abs(cpnFixed.getNotional());
    final double delta = swap.getFirstLeg().getNthPayment(0).getPaymentYearFraction();
    final double forwardModified = METHOD_SWAP.forwardModified(swap, marketData.getMulticurveProvider());
    final double strikeModified = Math.pow(1.0d + swaption.getStrike(), delta) - 1.0;
    // Implementation note: Modified strike: \bar K = (1+K)^\delta-1; the swaption payoff is pvbp*(\bar F - \bar K)^+
    final double maturity = swaption.getMaturityTime();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strikeModified, swaption.getTimeToExpiry(), swaption.isCall());
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double volatility = marketData.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), maturity);
    final BlackFunctionData dataBlack = new BlackFunctionData(forwardModified, numeraire, volatility);
    final Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(option);
    final double pv = func.apply(dataBlack) * (swaption.isLong() ? 1.0 : -1.0);
    return CurrencyAmount.of(swaption.getCurrency(), pv);
  }

  /**
   * Computes the present value of a physical delivery European swaption in the Black model.
   *
   * @param swaption
   *          The swaption.
   * @param marketData
   *          The curves with Black volatility data.
   * @return The present value.
   */
  public double forward(final SwaptionPhysicalFixedCompoundedONCompounded swaption, final BlackSwaptionFlatProviderInterface marketData) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(marketData, "marketData");
    final Swap<CouponFixedAccruedCompounding, CouponONCompounded> swap = swaption.getUnderlyingSwap();
    return METHOD_SWAP.forward(swap, marketData.getMulticurveProvider());
  }

  /**
   * Computes the implied Black volatility of the vanilla swaption.
   *
   * @param swaption
   *          The swaption.
   * @param marketData
   *          The yield curve bundle.
   * @return The implied volatility.
   */
  public double impliedVolatility(final SwaptionPhysicalFixedCompoundedONCompounded swaption,
      final BlackSwaptionFlatProviderInterface marketData) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(marketData, "marketData");
    final double tenor = swaption.getMaturityTime();
    final double volatility = marketData.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), tenor);
    return volatility;
  }

  /**
   * Computes the present value rate sensitivity to rates of a physical delivery European swaption in the SABR model.
   *
   * @param swaption
   *          The swaption.
   * @param marketData
   *          The curves with Black volatility data.
   * @return The present value curve sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final SwaptionPhysicalFixedCompoundedONCompounded swaption,
      final BlackSwaptionFlatProviderInterface marketData) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(marketData, "marketData");
    final Swap<CouponFixedAccruedCompounding, CouponONCompounded> swap = swaption.getUnderlyingSwap();
    final CouponFixedAccruedCompounding cpnFixed = swap.getFirstLeg().getNthPayment(0);
    final double numeraire = marketData.getMulticurveProvider().getDiscountFactor(cpnFixed.getCurrency(), cpnFixed.getPaymentTime())
        * cpnFixed.getNotional();
    final double delta = swap.getFirstLeg().getNthPayment(0).getPaymentYearFraction();
    final double forwardModified = METHOD_SWAP.forwardModified(swap, marketData.getMulticurveProvider());
    final double strikeModified = Math.pow(1.0d + swaption.getStrike(), delta) - 1.0;
    // Implementation note: Modified strike: \bar K = (1+K)^\delta-1; the swaption payoff is pvbp*(\bar F - \bar K)^+
    final double maturity = swaption.getMaturityTime();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strikeModified, swaption.getTimeToExpiry(), swaption.isCall());
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double volatility = marketData.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), maturity);
    final BlackFunctionData dataBlack = new BlackFunctionData(forwardModified, 1, volatility);
    final double[] bsAdjoint = blackFunction.getPriceAdjoint(option, dataBlack);
    final double sign = swaption.isLong() ? 1.0 : -1.0;
    // final double pv = numeraire * bsAdjoint[0] * sign;
    // Backward sweep
    final double pvBar = 1.0;
    final double numeraireBar = bsAdjoint[0] * sign * pvBar;
    final double forwardModifiedBar = numeraire * bsAdjoint[1] * sign * pvBar;
    final MultipleCurrencyMulticurveSensitivity forwardModifiedDr = METHOD_SWAP.forwardModifiedCurveSensitivity(swap,
        marketData.getMulticurveProvider());
    final double numeraireDr = -cpnFixed.getPaymentTime() * numeraire;
    final List<DoublesPair> list = new ArrayList<>();
    list.add(DoublesPair.of(cpnFixed.getPaymentTime(), numeraireDr * numeraireBar));
    final Map<String, List<DoublesPair>> numeraireMap = new HashMap<>();
    numeraireMap.put(marketData.getMulticurveProvider().getName(swaption.getCurrency()), list);
    final MulticurveSensitivity numeraireSensitivity = MulticurveSensitivity.ofYieldDiscounting(numeraireMap);
    final MultipleCurrencyMulticurveSensitivity result = forwardModifiedDr.multipliedBy(forwardModifiedBar).plus(swaption.getCurrency(),
        numeraireSensitivity);
    return result.cleaned();
  }

  /**
   * Computes the present value sensitivity to the Black volatility (also called vega) of a physical delivery European swaption in the Black
   * swaption model.
   *
   * @param swaption
   *          The swaption.
   * @param marketData
   *          The curves with Black volatility data.
   * @return The present value Black sensitivity.
   */
  public PresentValueBlackSwaptionSensitivity presentValueBlackSensitivity(final SwaptionPhysicalFixedCompoundedONCompounded swaption,
      final BlackSwaptionFlatProviderInterface marketData) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(marketData, "marketData");
    final Swap<CouponFixedAccruedCompounding, CouponONCompounded> swap = swaption.getUnderlyingSwap();
    final CouponFixedAccruedCompounding cpnFixed = swap.getFirstLeg().getNthPayment(0);
    final double numeraire = marketData.getMulticurveProvider().getDiscountFactor(cpnFixed.getCurrency(), cpnFixed.getPaymentTime())
        * cpnFixed.getNotional();
    final double delta = swap.getFirstLeg().getNthPayment(0).getPaymentYearFraction();
    final double forwardModified = METHOD_SWAP.forwardModified(swap, marketData.getMulticurveProvider());
    final double strikeModified = Math.pow(1.0d + swaption.getStrike(), delta) - 1.0;
    // Implementation note: Modified strike: \bar K = (1+K)^\delta-1; the swaption payoff is pvbp*(\bar F - \bar K)^+
    final double maturity = swaption.getMaturityTime();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strikeModified, swaption.getTimeToExpiry(), swaption.isCall());
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final double volatility = marketData.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), maturity);
    final BlackFunctionData dataBlack = new BlackFunctionData(forwardModified, 1, volatility);
    final double[] bsAdjoint = blackFunction.getPriceAdjoint(option, dataBlack);
    final double sign = swaption.isLong() ? 1.0 : -1.0;
    // Backward sweep
    final DoublesPair point = DoublesPair.of(swaption.getTimeToExpiry(), maturity);
    final Map<DoublesPair, Double> sensitivity = new HashMap<>();
    sensitivity.put(point, bsAdjoint[2] * numeraire * sign);
    return new PresentValueBlackSwaptionSensitivity(sensitivity, marketData.getBlackParameters().getGeneratorSwap());
  }

  /**
   * Calculates the delta.
   *
   * @param swaption
   *          The swaption, not null
   * @param marketData
   *          Yield curves and swaption volatility surface, not null
   * @return The delta
   */
  public CurrencyAmount delta(final SwaptionPhysicalFixedCompoundedONCompounded swaption,
      final BlackSwaptionFlatProviderInterface marketData) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(marketData, "marketData");
    final Swap<CouponFixedAccruedCompounding, CouponONCompounded> swap = swaption.getUnderlyingSwap();
    final CouponFixedAccruedCompounding cpnFixed = swap.getFirstLeg().getNthPayment(0);
    final double numeraire = marketData.getMulticurveProvider().getDiscountFactor(cpnFixed.getCurrency(), cpnFixed.getPaymentTime())
        * cpnFixed.getNotional();
    final double forwardModified = METHOD_SWAP.forwardModified(swap, marketData.getMulticurveProvider());
    final double sign = swaption.isLong() ? 1.0 : -1.0;
    return CurrencyAmount.of(swaption.getCurrency(), forwardDeltaTheoretical(swaption, marketData) * forwardModified * numeraire * sign);
  }

  /**
   * Computes the gamma of the swaption. The gamma is the second order derivative of the option present value to the spot fx rate.
   *
   * @param swaption
   *          The Forex option.
   * @param marketData
   *          The yield curve bundle.
   * @return The gamma.
   */
  public CurrencyAmount gamma(final SwaptionPhysicalFixedCompoundedONCompounded swaption,
      final BlackSwaptionFlatProviderInterface marketData) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(marketData, "marketData");
    final double gamma = forwardGammaTheoretical(swaption, marketData);
    final Swap<CouponFixedAccruedCompounding, CouponONCompounded> swap = swaption.getUnderlyingSwap();
    final CouponFixedAccruedCompounding cpnFixed = swap.getFirstLeg().getNthPayment(0);
    final double numeraire = marketData.getMulticurveProvider().getDiscountFactor(cpnFixed.getCurrency(), cpnFixed.getPaymentTime())
        * cpnFixed.getNotional();
    final double forwardModified = METHOD_SWAP.forwardModified(swap, marketData.getMulticurveProvider());
    final double sign = swaption.isLong() ? 1.0 : -1.0;
    return CurrencyAmount.of(swaption.getCurrency(), gamma * forwardModified * forwardModified * numeraire * sign);
  }

  /**
   * Calculates the theta.
   *
   * @param swaption
   *          The swaption, not null
   * @param marketData
   *          Yield curves and swaption volatility surface, not null
   * @return The delta
   */
  public CurrencyAmount theta(final SwaptionPhysicalFixedCompoundedONCompounded swaption,
      final BlackSwaptionFlatProviderInterface marketData) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(marketData, "marketData");
    final Swap<CouponFixedAccruedCompounding, CouponONCompounded> swap = swaption.getUnderlyingSwap();
    final CouponFixedAccruedCompounding cpnFixed = swap.getFirstLeg().getNthPayment(0);
    final double numeraire = marketData.getMulticurveProvider().getDiscountFactor(cpnFixed.getCurrency(), cpnFixed.getPaymentTime())
        * cpnFixed.getNotional();
    final double sign = swaption.isLong() ? 1.0 : -1.0;
    return CurrencyAmount.of(swaption.getCurrency(), forwardThetaTheoretical(swaption, marketData) * numeraire * sign);
  }

  /**
   * Compute first derivative of present value with respect to forward rate.
   *
   * @param swaption
   *          The swaption.
   * @param marketData
   *          The curves with Black volatility data.
   * @return The forward delta
   */
  public double forwardDeltaTheoretical(final SwaptionPhysicalFixedCompoundedONCompounded swaption,
      final BlackSwaptionFlatProviderInterface marketData) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(marketData, "marketData");
    final Swap<CouponFixedAccruedCompounding, CouponONCompounded> swap = swaption.getUnderlyingSwap();
    final double delta = swap.getFirstLeg().getNthPayment(0).getPaymentYearFraction();
    final double forwardModified = METHOD_SWAP.forwardModified(swap, marketData.getMulticurveProvider());
    final double strikeModified = Math.pow(1.0d + swaption.getStrike(), delta) - 1.0;
    final double maturity = swaption.getMaturityTime();
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    final double volatility = marketData.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), maturity);

    final double expiry = swaption.getTimeToExpiry();
    return BlackFormulaRepository.delta(forwardModified, strikeModified, expiry, volatility, swaption.isCall())
        * (swaption.isLong() ? 1.0 : -1.0);
  }

  /**
   * Compute first derivative of present value with respect to volatility.
   *
   * @param swaption
   *          The swaption.
   * @param marketData
   *          The curves with Black volatility data.
   * @return The forward vega
   */
  public double forwardVegaTheoretical(final SwaptionPhysicalFixedCompoundedONCompounded swaption,
      final BlackSwaptionFlatProviderInterface marketData) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(marketData, "marketData");
    final Swap<CouponFixedAccruedCompounding, CouponONCompounded> swap = swaption.getUnderlyingSwap();
    final double forwardModified = METHOD_SWAP.forwardModified(swap, marketData.getMulticurveProvider());
    final double delta = swap.getFirstLeg().getNthPayment(0).getPaymentYearFraction();
    final double strikeModified = Math.pow(1.0d + swaption.getStrike(), delta) - 1.0;
    final double maturity = swaption.getMaturityTime();
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    final double volatility = marketData.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), maturity);

    final double expiry = swaption.getTimeToExpiry();

    return BlackFormulaRepository.vega(forwardModified, strikeModified, expiry, volatility) * (swaption.isLong() ? 1.0 : -1.0);
  }

  /**
   * Compute second derivative of present value with respect to forward rate.
   *
   * @param swaption
   *          The swaption.
   * @param marketData
   *          The curves with Black volatility data.
   * @return The forward gamma
   */
  public double forwardGammaTheoretical(final SwaptionPhysicalFixedCompoundedONCompounded swaption,
      final BlackSwaptionFlatProviderInterface marketData) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(marketData, "marketData");
    final Swap<CouponFixedAccruedCompounding, CouponONCompounded> swap = swaption.getUnderlyingSwap();
    final double delta = swap.getFirstLeg().getNthPayment(0).getPaymentYearFraction();
    final double forwardModified = METHOD_SWAP.forwardModified(swap, marketData.getMulticurveProvider());
    final double strikeModified = Math.pow(1.0d + swaption.getStrike(), delta) - 1.0;
    final double maturity = swaption.getMaturityTime();
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    final double volatility = marketData.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), maturity);

    final double expiry = swaption.getTimeToExpiry();
    return BlackFormulaRepository.gamma(forwardModified, strikeModified, expiry, volatility) * (swaption.isLong() ? 1.0 : -1.0);
  }

  /**
   * Compute minus of first derivative of present value with respect to time, setting drift term to be 0.
   *
   * @param swaption
   *          The swaption.
   * @param marketData
   *          The curves with Black volatility data.
   * @return The driftless theta
   */
  public double driftlessThetaTheoretical(final SwaptionPhysicalFixedCompoundedONCompounded swaption,
      final BlackSwaptionFlatProviderInterface marketData) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(marketData, "marketData");
    final Swap<CouponFixedAccruedCompounding, CouponONCompounded> swap = swaption.getUnderlyingSwap();
    final double delta = swap.getFirstLeg().getNthPayment(0).getPaymentYearFraction();
    final double forwardModified = METHOD_SWAP.forwardModified(swap, marketData.getMulticurveProvider());
    final double strikeModified = Math.pow(1.0d + swaption.getStrike(), delta) - 1.0;
    final double maturity = swaption.getMaturityTime();
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    final double volatility = marketData.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), maturity);

    final double expiry = swaption.getTimeToExpiry();
    return BlackFormulaRepository.driftlessTheta(forwardModified, strikeModified, expiry, volatility) * (swaption.isLong() ? 1.0 : -1.0);
  }

  /**
   * Compute minus of first derivative of present value with respect to time.
   *
   * @param swaption
   *          The swaption.
   * @param marketData
   *          The curves with Black volatility data.
   * @return The forward theta
   */
  public double forwardThetaTheoretical(final SwaptionPhysicalFixedCompoundedONCompounded swaption,
      final BlackSwaptionFlatProviderInterface marketData) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(marketData, "marketData");
    final Swap<CouponFixedAccruedCompounding, CouponONCompounded> swap = swaption.getUnderlyingSwap();
    final double delta = swap.getFirstLeg().getNthPayment(0).getPaymentYearFraction();
    final double forwardModified = METHOD_SWAP.forwardModified(swap, marketData.getMulticurveProvider());
    final double strikeModified = Math.pow(1.0d + swaption.getStrike(), delta) - 1.0;
    final double maturity = swaption.getMaturityTime();
    // Implementation note: option required to pass the strike (in case the swap has non-constant coupon).
    final double volatility = marketData.getBlackParameters().getVolatility(swaption.getTimeToExpiry(), maturity);

    final double expiry = swaption.getTimeToExpiry();
    final boolean isCall = swaption.isCall();

    return forwardModified * BlackFormulaRepository.price(forwardModified, strikeModified, expiry, volatility, isCall)
        * (swaption.isLong() ? 1.0 : -1.0)
        + BlackFormulaRepository.driftlessTheta(forwardModified, strikeModified, expiry, volatility) * (swaption.isLong() ? 1.0 : -1.0);
  }
}
