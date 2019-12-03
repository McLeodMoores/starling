/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.sabrswaption;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorSameMethodAdapter;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMSSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.provider.CapFloorCMSSABRExtrapolationRightReplicationMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CapFloorCMSSpreadSABRBinormalMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponCMSSABRExtrapolationRightReplicationMethod;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionCashFixedIborSABRExtrapolationRightMethod;
import com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionPhysicalFixedIborSABRExtrapolationRightMethod;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateCorrelationParameters;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle.
 */
public final class PresentValueSABRSensitivitySABRSwaptionRightExtrapolationCalculator extends
    InstrumentDerivativeVisitorSameMethodAdapter<SABRSwaptionProviderInterface, PresentValueSABRSensitivityDataBundle> {

  /**
   * The cut-off strike. The smile is extrapolated above that level.
   */
  private final double _cutOffStrike;
  /**
   * The tail thickness parameter.
   */
  private final double _mu;

  /**
   * The methods.
   */
  private final CouponCMSSABRExtrapolationRightReplicationMethod _cmsCoupon;
  private final CapFloorCMSSABRExtrapolationRightReplicationMethod _capFloorCms;
  private final SwaptionCashFixedIborSABRExtrapolationRightMethod _cashSwaption;
  private final SwaptionPhysicalFixedIborSABRExtrapolationRightMethod _physicalSwaption;

  /**
   * Constructor.
   *
   * @param cutOffStrike
   *          The cut-off strike.
   * @param mu
   *          The tail thickness parameter.
   */
  public PresentValueSABRSensitivitySABRSwaptionRightExtrapolationCalculator(final double cutOffStrike, final double mu) {
    _mu = mu;
    _cutOffStrike = cutOffStrike;
    _cmsCoupon = new CouponCMSSABRExtrapolationRightReplicationMethod(_cutOffStrike, _mu);
    _capFloorCms = new CapFloorCMSSABRExtrapolationRightReplicationMethod(_cutOffStrike, _mu);
    _cashSwaption = new SwaptionCashFixedIborSABRExtrapolationRightMethod(_cutOffStrike, _mu);
    _physicalSwaption = new SwaptionPhysicalFixedIborSABRExtrapolationRightMethod(_cutOffStrike, _mu);
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visit(final InstrumentDerivative derivative, final SABRSwaptionProviderInterface data) {
    return derivative.accept(this, data);
  }

  // ----- Payment/Coupon ------

  @Override
  public PresentValueSABRSensitivityDataBundle visitCouponCMS(final CouponCMS payment, final SABRSwaptionProviderInterface data) {
    return _cmsCoupon.presentValueSABRSensitivity(payment, data);
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitCapFloorCMS(final CapFloorCMS payment, final SABRSwaptionProviderInterface data) {
    return _capFloorCms.presentValueSABRSensitivity(payment, data);
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitCapFloorCMSSpread(final CapFloorCMSSpread payment,
      final SABRSwaptionProviderInterface data) {
    if (data.getSABRParameter() instanceof SABRInterestRateCorrelationParameters) {
      // TODO: improve correlation data handling
      final SABRInterestRateCorrelationParameters sabrCorrelation = (SABRInterestRateCorrelationParameters) data.getSABRParameter();
      final CapFloorCMSSpreadSABRBinormalMethod method = new CapFloorCMSSpreadSABRBinormalMethod(sabrCorrelation.getCorrelation(),
          _capFloorCms,
          _cmsCoupon);
      return method.presentValueSABRSensitivity(payment, data);
    }
    throw new UnsupportedOperationException(
        "The PresentValueSABRSensitivitySABRSwaptionRightExtrapolationCalculator visitor visitCapFloorCMSSpread requires a "
            + "SABRInterestRateCorrelationParameters as data.");
  }

  // ----- Annuity ------

  @Override
  public PresentValueSABRSensitivityDataBundle visitGenericAnnuity(final Annuity<? extends Payment> annuity,
      final SABRSwaptionProviderInterface data) {
    ArgumentChecker.notNull(annuity, "Annuity");
    PresentValueSABRSensitivityDataBundle pvss = visit(annuity.getNthPayment(0), data);
    for (int i = 1; i < annuity.getNumberOfPayments(); i++) {
      pvss = pvss.plus(visit(annuity.getNthPayment(i), data));
    }
    return pvss;
  }

  // ----- Swaption ------

  @Override
  public PresentValueSABRSensitivityDataBundle visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption,
      final SABRSwaptionProviderInterface data) {
    return _cashSwaption.presentValueSABRSensitivity(swaption, data);
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption,
      final SABRSwaptionProviderInterface data) {
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.notNull(swaption, "swaption");
    return _physicalSwaption.presentValueSABRSensitivity(swaption, data);
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitSwap(final Swap<?, ?> swap, final SABRSwaptionProviderInterface data) {
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.notNull(swap, "swap");
    PresentValueSABRSensitivityDataBundle pvss = new PresentValueSABRSensitivityDataBundle();
    for (final Payment p : swap.getFirstLeg().getPayments()) {
      pvss = pvss.plus(p.accept(this, data));
    }
    for (final Payment p : swap.getSecondLeg().getPayments()) {
      pvss = pvss.plus(p.accept(this, data));
    }
    return pvss;
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitCouponFixed(final CouponFixed coupon, final SABRSwaptionProviderInterface curves) {
    return new PresentValueSABRSensitivityDataBundle();
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitCouponIbor(final CouponIbor coupon, final SABRSwaptionProviderInterface curves) {
    return new PresentValueSABRSensitivityDataBundle();
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitCouponIborSpread(final CouponIborSpread coupon,
      final SABRSwaptionProviderInterface curves) {
    return new PresentValueSABRSensitivityDataBundle();
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitCouponIborGearing(final CouponIborGearing coupon,
      final SABRSwaptionProviderInterface curves) {
    return new PresentValueSABRSensitivityDataBundle();
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visit(final InstrumentDerivative derivative) {
    throw new UnsupportedOperationException("Cannot calculate SABR sensitivities without data");
  }

}
