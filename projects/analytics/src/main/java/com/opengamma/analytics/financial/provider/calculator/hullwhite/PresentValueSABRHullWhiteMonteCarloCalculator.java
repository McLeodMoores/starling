/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.hullwhite;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet.RatchetIborCalibrationType;
import com.opengamma.analytics.financial.interestrate.annuity.provider.IborRatchetBasketCalibrator;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.montecarlo.provider.HullWhiteMonteCarloMethod;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSwaptionCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderInterface;
import com.opengamma.analytics.financial.provider.method.SuccessiveRootFinderHullWhiteCalibrationEngine;
import com.opengamma.analytics.financial.provider.method.SuccessiveRootFinderHullWhiteCalibrationObjective;
import com.opengamma.analytics.math.random.NormalRandomNumberGenerator;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

import cern.jet.random.engine.MersenneTwister;

/**
 * Present value calculator for interest rate instruments using a Hull-White one factor model calibrated to SABR prices.
 * <p>
 * <b>The calculator is for test purposes only! It calibrates a Hull-White on an instrument priced with SABR and then price the same
 * instrument in the Hull-White model by Monte Carlo. Do not use this calculator in production.</b>
 */
public final class PresentValueSABRHullWhiteMonteCarloCalculator
    extends InstrumentDerivativeVisitorAdapter<SABRSwaptionProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueSABRHullWhiteMonteCarloCalculator INSTANCE = new PresentValueSABRHullWhiteMonteCarloCalculator();

  /**
   * Constructor.
   */
  private PresentValueSABRHullWhiteMonteCarloCalculator() {
  }

  /**
   * Gets the calculator instance.
   *
   * @return The calculator.
   */
  public static PresentValueSABRHullWhiteMonteCarloCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * The default mean reversion parameter for the Hull-White one factor model.
   */
  private static final double DEFAULT_MEAN_REVERSION = 0.01;
  /**
   * The default number of path in the Monte Carlo simulation.
   */
  private static final int DEFAULT_NB_PATH = 50000; // 12500;
  /**
   * The SABR method used for European swaptions with physical delivery.
   */
  private static final PresentValueSABRSwaptionCalculator PVSSC = PresentValueSABRSwaptionCalculator.getInstance();

  @Override
  public MultipleCurrencyAmount visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption,
      final SABRSwaptionProviderInterface sabrData) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(sabrData, "sabrData");
    final Currency ccy = swaption.getCurrency();
    final HullWhiteOneFactorPiecewiseConstantParameters hwParameters = new HullWhiteOneFactorPiecewiseConstantParameters(
        DEFAULT_MEAN_REVERSION,
        new double[] { 0.01 }, new double[0]);
    final SuccessiveRootFinderHullWhiteCalibrationObjective objective = new SuccessiveRootFinderHullWhiteCalibrationObjective(hwParameters,
        ccy);
    final SuccessiveRootFinderHullWhiteCalibrationEngine<SABRSwaptionProviderInterface> calibrationEngine = new SuccessiveRootFinderHullWhiteCalibrationEngine<>(
        objective);
    // Calibration instruments
    calibrationEngine.addInstrument(swaption, PVSSC);
    // Calibration
    calibrationEngine.calibrate(sabrData);
    final HullWhiteOneFactorProvider hwMulticurves = new HullWhiteOneFactorProvider(sabrData.getMulticurveProvider(), hwParameters, ccy);
    // Pricing
    final HullWhiteMonteCarloMethod methodMC = new HullWhiteMonteCarloMethod(
        new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), DEFAULT_NB_PATH);
    return methodMC.presentValue(swaption, ccy, hwMulticurves);
  }

  @Override
  public MultipleCurrencyAmount visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity,
      final SABRSwaptionProviderInterface sabrData) {
    ArgumentChecker.notNull(annuity, "annuity");
    ArgumentChecker.notNull(sabrData, "sabrData");
    final Currency ccy = annuity.getCurrency();
    final HullWhiteOneFactorPiecewiseConstantParameters hwParameters = new HullWhiteOneFactorPiecewiseConstantParameters(
        DEFAULT_MEAN_REVERSION,
        new double[] { 0.01 }, new double[0]);
    final SuccessiveRootFinderHullWhiteCalibrationObjective objective = new SuccessiveRootFinderHullWhiteCalibrationObjective(hwParameters,
        ccy);
    final SuccessiveRootFinderHullWhiteCalibrationEngine<SABRSwaptionProviderInterface> calibrationEngine = new SuccessiveRootFinderHullWhiteCalibrationEngine<>(
        objective);
    // Calibration instruments
    final InstrumentDerivative[] calibrationBasket = IborRatchetBasketCalibrator.INSTANCE
        .calibrationBasket(annuity, RatchetIborCalibrationType.FORWARD_COUPON, sabrData.getMulticurveProvider());
    // TODO: set a way to chose the calibration type.
    calibrationEngine.addInstrument(calibrationBasket, PVSSC);
    // Calibration
    calibrationEngine.calibrate(sabrData);
    final HullWhiteOneFactorProvider hwMulticurves = new HullWhiteOneFactorProvider(sabrData.getMulticurveProvider(), hwParameters, ccy);
    // Pricing
    final HullWhiteMonteCarloMethod methodMC = new HullWhiteMonteCarloMethod(
        new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), DEFAULT_NB_PATH);
    return methodMC.presentValue(annuity, ccy, hwMulticurves);
  }

}
