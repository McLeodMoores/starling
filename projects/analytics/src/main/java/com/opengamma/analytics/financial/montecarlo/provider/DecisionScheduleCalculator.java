/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.montecarlo.provider;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFloating;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.montecarlo.DecisionSchedule;
import com.opengamma.analytics.financial.provider.calculator.discounting.CashFlowEquivalentCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;

/**
 * Calculator of decision schedule for different instruments. Used in particular for Monte Carlo pricing.
 */
public class DecisionScheduleCalculator extends InstrumentDerivativeVisitorAdapter<MulticurveProviderInterface, DecisionSchedule> {
  private static final DecisionScheduleCalculator INSTANCE = new DecisionScheduleCalculator();

  /**
   * Constructor.
   */
  DecisionScheduleCalculator() {
  }

  /**
   * Gets the calculator instance.
   *
   * @return The calculator.
   */
  public static DecisionScheduleCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * The cash-flow equivalent calculator.
   */
  private static final CashFlowEquivalentCalculator CFEC = CashFlowEquivalentCalculator.getInstance();

  @Override
  public DecisionSchedule visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption,
      final MulticurveProviderInterface multicurves) {
    final double[] decisionTime = new double[] { swaption.getTimeToExpiry() };
    final AnnuityPaymentFixed cfe = swaption.getUnderlyingSwap().accept(CFEC, multicurves);
    final double[][] impactTime = new double[1][cfe.getNumberOfPayments()];
    final double[][] impactAmount = new double[1][cfe.getNumberOfPayments()];
    for (int i = 0; i < cfe.getNumberOfPayments(); i++) {
      impactTime[0][i] = cfe.getNthPayment(i).getPaymentTime();
      impactAmount[0][i] = cfe.getNthPayment(i).getAmount();
    }
    final DecisionSchedule decision = new DecisionSchedule(decisionTime, impactTime, impactAmount);
    return decision;
  }

  @Override
  public DecisionSchedule visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final MulticurveProviderInterface multicurves) {
    final double[] decisionTime = new double[] { swaption.getTimeToExpiry() };
    final AnnuityPaymentFixed cfeIbor = swaption.getUnderlyingSwap().getSecondLeg().accept(CFEC, multicurves);
    final int nbCfeIbor = cfeIbor.getNumberOfPayments();
    final int nbCpnFixed = swaption.getUnderlyingSwap().getFixedLeg().getNumberOfPayments();
    final double[][] impactTime = new double[1][nbCpnFixed + nbCfeIbor];
    final double[][] impactAmount = new double[1][nbCpnFixed + nbCfeIbor];
    // Fixed leg
    for (int i = 0; i < nbCpnFixed; i++) {
      impactTime[0][i] = swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(i).getPaymentTime();
      impactAmount[0][i] = swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(i).getPaymentYearFraction()
          * swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(i).getNotional();
    }
    // Ibor leg
    for (int i = 0; i < nbCfeIbor; i++) {
      impactTime[0][nbCpnFixed + i] = cfeIbor.getNthPayment(i).getPaymentTime();
      impactAmount[0][nbCpnFixed + i] = cfeIbor.getNthPayment(i).getAmount();
    }
    final DecisionSchedule decision = new DecisionSchedule(decisionTime, impactTime, impactAmount);
    return decision;
  }

  @Override
  public DecisionSchedule visitCapFloorIbor(final CapFloorIbor payment, final MulticurveProviderInterface multicurves) {
    final double[] decisionTime = new double[] { payment.getFixingTime() };
    final double fixingStartTime = payment.getFixingPeriodStartTime();
    final double fixingEndTime = payment.getFixingPeriodEndTime();
    final double paymentTime = payment.getPaymentTime();
    final double[][] impactTime = new double[1][];
    impactTime[0] = new double[] { fixingStartTime, fixingEndTime, paymentTime };
    final double[][] impactAmount = new double[1][];
    final double forward = multicurves.getSimplyCompoundForwardRate(payment.getIndex(), payment.getFixingPeriodStartTime(),
        payment.getFixingPeriodEndTime(),
        payment.getFixingAccrualFactor());
    final double beta = (1.0 + payment.getFixingAccrualFactor() * forward)
        * multicurves.getDiscountFactor(payment.getCurrency(), payment.getFixingPeriodEndTime())
        / multicurves.getDiscountFactor(payment.getCurrency(), payment.getFixingPeriodStartTime());
    impactAmount[0] = new double[] { beta, -1.0, 1.0 };
    final DecisionSchedule decision = new DecisionSchedule(decisionTime, impactTime, impactAmount);
    return decision;
  }

  @Override
  public DecisionSchedule visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity,
      final MulticurveProviderInterface multicurves) {
    final int nbCpn = annuity.getNumberOfPayments();
    final double[] decisionTime = new double[nbCpn];
    final double[][] impactTime = new double[nbCpn][];
    final double[][] impactAmount = new double[nbCpn][];
    for (int i = 0; i < nbCpn; i++) {
      final AnnuityPaymentFixed cfe = annuity.getNthPayment(i).accept(CFEC, multicurves);
      decisionTime[i] = annuity.isFixed()[i] ? 0.0 : ((CouponFloating) annuity.getNthPayment(i)).getFixingTime();
      impactTime[i] = new double[cfe.getNumberOfPayments()];
      impactAmount[i] = new double[cfe.getNumberOfPayments()];
      for (int j = 0; j < cfe.getNumberOfPayments(); j++) {
        impactTime[i][j] = cfe.getNthPayment(j).getPaymentTime();
        impactAmount[i][j] = cfe.getNthPayment(j).getAmount();
      }
    }
    final DecisionSchedule decision = new DecisionSchedule(decisionTime, impactTime, impactAmount);
    return decision;
  }

}
