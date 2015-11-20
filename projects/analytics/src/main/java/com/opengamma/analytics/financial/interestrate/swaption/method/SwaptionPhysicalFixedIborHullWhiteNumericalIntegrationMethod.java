/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

import com.opengamma.analytics.financial.interestrate.CashFlowEquivalentCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Method to compute the present value of physical delivery European swaptions with the Hull-White one factor model by numerical integration.
 * @deprecated Use {@link com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionPhysicalFixedIborHullWhiteNumericalIntegrationMethod}
 */
@Deprecated
public class SwaptionPhysicalFixedIborHullWhiteNumericalIntegrationMethod implements PricingMethod {

  /**
   * The model used in computations.
   */
  private static final HullWhiteOneFactorPiecewiseConstantInterestRateModel MODEL = new HullWhiteOneFactorPiecewiseConstantInterestRateModel();
  /**
   * The cash flow equivalent calculator used in computations.
   */
  private static final CashFlowEquivalentCalculator CFEC = CashFlowEquivalentCalculator.getInstance();
  /**
   * Minimal number of integration steps in the replication.
   */
  private static final int NB_INTEGRATION = 6;

  /**
   * Computes the present value of the Physical delivery swaption.
   * @param swaption The swaption.
   * @param hwData The Hull-White parameters and the curves.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final SwaptionPhysicalFixedIbor swaption, final HullWhiteOneFactorPiecewiseConstantDataBundle hwData) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(hwData, "Hull-White data");
    final double expiryTime = swaption.getTimeToExpiry();
    final AnnuityPaymentFixed cfe = swaption.getUnderlyingSwap().accept(CFEC, hwData);
    final double[] alpha = new double[cfe.getNumberOfPayments()];
    final double[] df = new double[cfe.getNumberOfPayments()];
    final double[] discountedCashFlow = new double[cfe.getNumberOfPayments()];
    for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
      alpha[loopcf] = MODEL.alpha(hwData.getHullWhiteParameter(), 0.0, expiryTime, expiryTime, cfe.getNthPayment(loopcf).getPaymentTime());
      df[loopcf] = hwData.getCurve(cfe.getDiscountCurve()).getDiscountFactor(cfe.getNthPayment(loopcf).getPaymentTime());
      discountedCashFlow[loopcf] = df[loopcf] * cfe.getNthPayment(loopcf).getAmount();
    }
    // Integration
    final SwaptionIntegrant integrant = new SwaptionIntegrant(discountedCashFlow, alpha);
    final double limit = 10.0;
    final double absoluteTolerance = 1.0E-2;
    final double relativeTolerance = 1.0E-6;
    final RungeKuttaIntegrator1D integrator = new RungeKuttaIntegrator1D(absoluteTolerance, relativeTolerance, NB_INTEGRATION);
    double pv = 0.0;
    try {
      pv = 1.0 / Math.sqrt(2.0 * Math.PI) * integrator.integrate(integrant, -limit, limit) * (swaption.isLong() ? 1.0 : -1.0);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
    return CurrencyAmount.of(swaption.getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(instrument instanceof SwaptionPhysicalFixedIbor, "Physical delivery swaption");
    ArgumentChecker.isTrue(curves instanceof HullWhiteOneFactorPiecewiseConstantDataBundle, "Bundle should contain Hull-White data");
    return presentValue((SwaptionPhysicalFixedIbor) instrument, (HullWhiteOneFactorPiecewiseConstantDataBundle) curves);
  }

  /**
   * Inner class to implement the integration used in price replication.
   */
  private static final class SwaptionIntegrant extends Function1D<Double, Double> {

    private final double[] _discountedCashFlow;
    private final double[] _alpha;

    /**
     * Constructor to the integrant function.
     * @param discountedCashFlow The discounted cash flows.
     * @param alpha The bond volatilities.
     */
    public SwaptionIntegrant(final double[] discountedCashFlow, final double[] alpha) {
      _discountedCashFlow = discountedCashFlow;
      _alpha = alpha;
    }

    @Override
    public Double evaluate(final Double x) {
      double result = 0.0;
      for (int loopcf = 0; loopcf < _discountedCashFlow.length; loopcf++) {
        result += _discountedCashFlow[loopcf] * Math.exp(-(x + _alpha[loopcf]) * (x + _alpha[loopcf]) / 2.0);
      }
      return Math.max(result, 0.0);
    }
  }

}
