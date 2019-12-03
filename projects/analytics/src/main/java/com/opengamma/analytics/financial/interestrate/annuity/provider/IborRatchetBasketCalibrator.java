/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.interestrate.annuity.provider;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet.RatchetIborCalibrationType;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborRatchet;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public final class IborRatchetBasketCalibrator {

  /**
   * An instance of this calculator.
   */
  public static final IborRatchetBasketCalibrator INSTANCE = new IborRatchetBasketCalibrator();

  /**
   * @param annuity
   *          the annuity, not null
   * @param type
   *          The calibration type, not null
   * @param curves
   *          The curves data, not null
   * @return A list of coupons that are used in calibration
   */
  public InstrumentDerivative[] calibrationBasket(final AnnuityCouponIborRatchet annuity, final RatchetIborCalibrationType type,
      final MulticurveProviderInterface curves) {
    ArgumentChecker.notNull(annuity, "annuity");
    ArgumentChecker.notNull(type, "type");
    ArgumentChecker.notNull(curves, "curves");
    final List<InstrumentDerivative> calibration = new ArrayList<>();
    final ParRateDiscountingCalculator prc = ParRateDiscountingCalculator.getInstance();
    switch (type) {
      case FORWARD_COUPON:
        final int nbCpn = annuity.getNumberOfPayments();
        final double[] cpnRate = new double[nbCpn];
        for (int i = 0; i < nbCpn; i++) {
          if (annuity.getNthPayment(i) instanceof CouponIborRatchet) {
            final CouponIborRatchet cpn = (CouponIborRatchet) annuity.getNthPayment(i);
            final double ibor = prc.visitCouponIborSpread(cpn, curves);
            final double cpnMain = cpn.getMainCoefficients()[0] * cpnRate[i - 1] + cpn.getMainCoefficients()[1] * ibor
                + cpn.getMainCoefficients()[2];
            final double cpnFloor = cpn.getFloorCoefficients()[0] * cpnRate[i - 1] + cpn.getFloorCoefficients()[1] * ibor
                + cpn.getFloorCoefficients()[2];
            final double cpnCap = cpn.getCapCoefficients()[0] * cpnRate[i - 1] + cpn.getCapCoefficients()[1] * ibor
                + cpn.getCapCoefficients()[2];
            cpnRate[i] = Math.min(Math.max(cpnFloor, cpnMain), cpnCap);
            calibration
                .add(new CapFloorIbor(cpn.getCurrency(), cpn.getPaymentTime(), cpn.getPaymentYearFraction(),
                    cpn.getNotional(), cpn.getFixingTime(), cpn.getIndex(), cpn.getFixingPeriodStartTime(),
                    cpn.getFixingPeriodEndTime(), cpn.getFixingAccrualFactor(), cpnRate[i], true));
          } else {
            if (annuity.getNthPayment(i) instanceof CouponFixed) {
              final CouponFixed cpn = (CouponFixed) annuity.getNthPayment(i);
              cpnRate[i] = cpn.getFixedRate();
            } else {
              final CouponIborGearing cpn = (CouponIborGearing) annuity.getNthPayment(i);
              final double ibor = prc.visitCouponIborGearing(cpn, curves);
              cpnRate[i] = cpn.getFactor() * ibor + cpn.getSpread();
            }
          }
        }
        break;
      default:
        break;
    }
    return calibration.toArray(new InstrumentDerivative[calibration.size()]);
  }

  private IborRatchetBasketCalibrator() {
  }

}
