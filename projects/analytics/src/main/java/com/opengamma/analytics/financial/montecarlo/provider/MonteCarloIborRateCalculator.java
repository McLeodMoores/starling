/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.montecarlo.provider;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.swap.provider.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.montecarlo.MonteCarloIborRateDataBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Computes the total instrument price over different paths (the sum of prices over the different paths, not its average). The data bundle
 * contains the different Ibor rates paths and the instrument reference amounts. The numeraire is the last time in the LMM description.
 */
public class MonteCarloIborRateCalculator extends InstrumentDerivativeVisitorAdapter<MonteCarloIborRateDataBundle, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final MonteCarloIborRateCalculator INSTANCE = new MonteCarloIborRateCalculator();

  /**
   * Gets the calculator instance.
   *
   * @return The calculator.
   */
  public static MonteCarloIborRateCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  MonteCarloIborRateCalculator() {
  }

  /**
   * The swap method.
   */
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();

  @Override
  public Double visitCapFloorIbor(final CapFloorIbor payment, final MonteCarloIborRateDataBundle mcResults) {
    ArgumentChecker.isTrue(mcResults.getPathIborRate().length == 1, "Only one decision date for cap/floor.");
    final double[][] pathIborRate = mcResults.getPathIborRate()[0];
    final double[] impactAmount = mcResults.getImpactAmount()[0];
    final int[] impactIndex = mcResults.getImpactIndex()[0];
    double price = 0;
    final int nbPath = pathIborRate[0].length;
    final int nbPeriod = pathIborRate.length;
    final double[] ibor = new double[nbPath];
    double payoff;
    final double[] discounting = new double[nbPath];
    final double omega = payment.isCap() ? 1.0 : -1.0;
    for (int i = 0; i < nbPath; i++) {
      ibor[i] = impactAmount[0] * pathIborRate[impactIndex[0]][i] + (impactAmount[0] - 1.0) / payment.getFixingAccrualFactor();
      // Ibor in the right convention; path in Dsc curve
      payoff = Math.max(omega * (ibor[i] - payment.getStrike()), 0);
      discounting[i] = 1.0;
      for (int j = impactIndex[2]; j < nbPeriod; j++) {
        discounting[i] *= 1.0 + pathIborRate[j][i] * mcResults.getDelta()[j];
      }
      price += payoff * discounting[i];
    }
    price *= payment.getNotional() * payment.getPaymentYearFraction();
    return price;
  }

  @Override
  public Double visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final MonteCarloIborRateDataBundle mcResults) {
    final double[][] pathIborRate = mcResults.getPathIborRate()[0];
    final double[] impactAmount = mcResults.getImpactAmount()[0];
    final int[] impactIndex = mcResults.getImpactIndex()[0];
    final int nbImpact = impactIndex.length;
    final int nbPath = pathIborRate[0].length;
    final int nbPeriod = pathIborRate.length;
    final double[][] discounting = new double[nbPath][nbPeriod + 1];
    double price = 0.0;
    final double[] pricePath = new double[nbPath];
    for (int i = 0; i < nbPath; i++) {
      discounting[i][nbPeriod] = 1.0;
      for (int j = nbPeriod - 1; j >= 0; j--) {
        discounting[i][j] = discounting[i][j + 1] * (1.0 + pathIborRate[j][i] * mcResults.getDelta()[j]);
      }
      for (int j = 0; j < nbImpact; j++) {
        pricePath[i] += impactAmount[impactIndex[j]] * discounting[i][impactIndex[j]];
      }
      price += Math.max(pricePath[i], 0);
    }
    return price * (swaption.isLong() ? 1.0 : -1.0);
  }

  @Override
  public Double visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final MonteCarloIborRateDataBundle mcResults) {
    final double strike = swaption.getStrike();
    final double[][] pathIborRate = mcResults.getPathIborRate()[0];
    final double[] impactAmount = mcResults.getImpactAmount()[0];
    final int[] impactIndex = mcResults.getImpactIndex()[0];
    int nbFixed = 0; // The number of fixed coupons.
    while (impactIndex[nbFixed] < impactIndex[nbFixed + 1]) {
      nbFixed++;
    }
    nbFixed++;
    final int nbImpact = impactIndex.length;
    final int nbPath = pathIborRate[0].length;
    final int nbPeriod = pathIborRate.length;
    final double[][] discounting = new double[nbPath][nbPeriod + 1];
    double price = 0.0;
    final double omega = swaption.getUnderlyingSwap().getFixedLeg().getPayments()[0].getNotional() < 0 ? 1.0 : -1.0;
    for (int i = 0; i < nbPath; i++) {
      double fixedPath = 0.0;
      double floatPath = 0.0;
      double swapRatePath = 0.0;
      discounting[i][nbPeriod] = 1.0;
      for (int j = nbPeriod - 1; j >= 0; j--) {
        discounting[i][j] = discounting[i][j + 1]
            * (1.0 + pathIborRate[j][i] * mcResults.getDelta()[j]);
      }
      for (int j = 0; j < nbFixed; j++) {
        fixedPath += impactAmount[j] * discounting[i][impactIndex[j]];
      }
      for (int j = nbFixed; j < nbImpact; j++) {
        floatPath += impactAmount[j] * discounting[i][impactIndex[j]];
      }
      swapRatePath = -floatPath / fixedPath;
      final double annuityCashPath = METHOD_SWAP.getAnnuityCash(swaption.getUnderlyingSwap(), swapRatePath);
      price += annuityCashPath * Math.max(omega * (swapRatePath - strike), 0.0) * discounting[i][impactIndex[nbFixed]];
    }
    return price * (swaption.isLong() ? 1.0 : -1.0);
  }

  @Override
  public Double visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity, final MonteCarloIborRateDataBundle mcResults) {
    final int nbCpn = annuity.getNumberOfPayments();
    final double[][][] pathIborRate = mcResults.getPathIborRate(); // Size: nbJump x nbPeriodLMM x nbPath
    final int nbPath = pathIborRate[0][0].length;
    final int nbPeriod = pathIborRate[0].length;
    final double[][] impactAmount = mcResults.getImpactAmount(); // impact - amount
    final int[][] impactIndex = mcResults.getImpactIndex(); // impact - index
    final double[] delta = mcResults.getDelta();
    // Discount factors
    final double[][][] discounting = new double[nbCpn][nbPeriod + 1][nbPath];
    for (int i = 0; i < nbCpn; i++) { // nbCpn
      for (int j = 0; j < nbPath; j++) {
        discounting[i][nbPeriod][j] = 1.0;
        for (int k = nbPeriod - 1; k >= 0; k--) {
          discounting[i][k][j] = discounting[i][k + 1][j]
              * (1.0 + pathIborRate[i][k][j] * delta[k]);
        }
      }
    }
    // Coupons and annuity value
    final double[][] cpnRate = new double[nbCpn][nbPath];
    final double[] annuityPathValue = new double[nbPath];
    double ibor;
    for (int i = 0; i < nbCpn; i++) { // nbCpn
      if (annuity.isFixed()[i]) { // Coupon already fixed: only one cash flow
        final CouponFixed cpn = (CouponFixed) annuity.getNthPayment(i);
        for (int j = 0; j < nbPath; j++) {
          cpnRate[i][j] = cpn.getFixedRate();
          annuityPathValue[j] += impactAmount[i][0] * discounting[i][impactIndex[i][0]][j];
        }
      } else {
        if (annuity.getNthPayment(i) instanceof CouponIborRatchet) {
          final CouponIborRatchet cpn = (CouponIborRatchet) annuity.getNthPayment(i);
          for (int j = 0; j < nbPath; j++) {
            ibor = (-impactAmount[i][0] * discounting[i][impactIndex[i][0]][j]
                / (impactAmount[i][1] * discounting[i][impactIndex[i][1]][j]) - 1.0)
                / cpn.getFixingAccrualFactor();
            final double cpnMain = cpn.getMainCoefficients()[0] * cpnRate[i - 1][j] + cpn.getMainCoefficients()[1] * ibor
                + cpn.getMainCoefficients()[2];
            final double cpnFloor = cpn.getFloorCoefficients()[0] * cpnRate[i - 1][j] + cpn.getFloorCoefficients()[1] * ibor
                + cpn.getFloorCoefficients()[2];
            final double cpnCap = cpn.getCapCoefficients()[0] * cpnRate[i - 1][j] + cpn.getCapCoefficients()[1] * ibor
                + cpn.getCapCoefficients()[2];
            cpnRate[i][j] = Math.min(Math.max(cpnFloor, cpnMain), cpnCap);
            annuityPathValue[j] += cpnRate[i][j] * cpn.getPaymentYearFraction() * cpn.getNotional()
                * discounting[i][impactIndex[i][1]][j];
          }
        } else {
          final CouponIborGearing cpn = (CouponIborGearing) annuity.getNthPayment(i); // Only possible for the first coupon
          for (int j = 0; j < nbPath; j++) {
            ibor = (-impactAmount[0][0] * discounting[i][impactIndex[i][0]][j]
                / (impactAmount[0][1] * discounting[i][impactIndex[i][1]][j]) - 1.0)
                / cpn.getFixingAccrualFactor();
            cpnRate[i][j] = cpn.getFactor() * ibor + cpn.getSpread();
            annuityPathValue[j] += cpnRate[i][j] * cpn.getPaymentYearFraction() * cpn.getNotional()
                * discounting[i][impactIndex[i][1]][j];
          }
        }
      }
    }
    double price = 0.0;
    for (int i = 0; i < nbPath; i++) {
      price += annuityPathValue[i];
    }
    return price;
  }

}
