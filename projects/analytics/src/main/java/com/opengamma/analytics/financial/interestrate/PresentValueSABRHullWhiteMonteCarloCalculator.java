/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.jet.random.engine.MersenneTwister;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet.RatchetIborCalibrationType;
import com.opengamma.analytics.financial.interestrate.method.SuccessiveRootFinderCalibrationEngine;
import com.opengamma.analytics.financial.interestrate.payments.method.CapFloorHullWhiteCalibrationObjective;
import com.opengamma.analytics.financial.interestrate.payments.method.CapFloorHullWhiteSuccessiveRootFinderCalibrationEngine;
import com.opengamma.analytics.financial.interestrate.payments.method.CapFloorIborSABRMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborSABRMethod;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionPhysicalHullWhiteCalibrationObjective;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionPhysicalHullWhiteSuccessiveRootFinderCalibrationEngine;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.montecarlo.HullWhiteMonteCarloMethod;
import com.opengamma.analytics.math.random.NormalRandomNumberGenerator;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Present value calculator for interest rate instruments using a Hull-White one factor model calibrated to SABR prices.
 * @deprecated {@link PresentValueCalculator} is deprecated
 */
@Deprecated
public class PresentValueSABRHullWhiteMonteCarloCalculator extends PresentValueCalculator {

  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(PresentValueSABRHullWhiteMonteCarloCalculator.class);
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
  private static final SwaptionPhysicalFixedIborSABRMethod METHOD_SWAPTION_SABR = SwaptionPhysicalFixedIborSABRMethod.getInstance();
  /**
   * The SABR method used for cap/floor.
   */
  private static final CapFloorIborSABRMethod METHOD_CAP_SABR = CapFloorIborSABRMethod.getInstance();

  /**
   * The calculator unique instance.
   */
  private static final PresentValueSABRHullWhiteMonteCarloCalculator INSTANCE = new PresentValueSABRHullWhiteMonteCarloCalculator();

  public static PresentValueSABRHullWhiteMonteCarloCalculator getInstance() {
    return INSTANCE;
  }

  PresentValueSABRHullWhiteMonteCarloCalculator() {
  }

  @Override
  /**
   * The calculator is for test purposes only! It calibrates a Hull-White on a swaption priced with SABR and then price the same swaption in the Hull-White model by Monte Carlo.
   * Do not use this calculator in production.
   */
  public Double visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    LOGGER.warn("This calculator should be used for test purposes only, not in production!");
    Validate.notNull(swaption);
    Validate.notNull(curves);
    if (!(curves instanceof SABRInterestRateDataBundle)) {
      throw new UnsupportedOperationException("The PresentValueSABRHullWhiteMonteCarloCalculator visitor visitSwaptionPhysicalFixedIbor requires a SABRInterestRateDataBundle as data.");
    }
    final HullWhiteOneFactorPiecewiseConstantParameters hwParameters = new HullWhiteOneFactorPiecewiseConstantParameters(DEFAULT_MEAN_REVERSION, new double[] {0.01}, new double[0]);
    final SwaptionPhysicalHullWhiteCalibrationObjective objective = new SwaptionPhysicalHullWhiteCalibrationObjective(hwParameters);
    final SuccessiveRootFinderCalibrationEngine calibrationEngine = new SwaptionPhysicalHullWhiteSuccessiveRootFinderCalibrationEngine(objective);
    // Calibration instruments
    calibrationEngine.addInstrument(swaption, METHOD_SWAPTION_SABR);
    // Calibration
    calibrationEngine.calibrate(curves);
    final HullWhiteOneFactorPiecewiseConstantDataBundle hwBundle = new HullWhiteOneFactorPiecewiseConstantDataBundle(hwParameters, curves);
    // Pricing
    final HullWhiteMonteCarloMethod methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), DEFAULT_NB_PATH);
    final CurrencyAmount pvMC = methodMC.presentValue(swaption, swaption.getCurrency(), swaption.getUnderlyingSwap().getFirstLeg().getDiscountCurve(), hwBundle);
    return pvMC.getAmount();
  }

  @Override
  public Double visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity, final YieldCurveBundle curves) {
    Validate.notNull(annuity);
    Validate.notNull(curves);
    if (!(curves instanceof SABRInterestRateDataBundle)) {
      throw new UnsupportedOperationException("The PresentValueSABRHullWhiteMonteCarloCalculator visitor visitSwaptionPhysicalFixedIbor requires a SABRInterestRateDataBundle as data.");
    }
    final HullWhiteOneFactorPiecewiseConstantParameters hwParameters = new HullWhiteOneFactorPiecewiseConstantParameters(DEFAULT_MEAN_REVERSION, new double[] {0.01}, new double[0]);
    final CapFloorHullWhiteCalibrationObjective objective = new CapFloorHullWhiteCalibrationObjective(hwParameters);
    final SuccessiveRootFinderCalibrationEngine calibrationEngine = new CapFloorHullWhiteSuccessiveRootFinderCalibrationEngine(objective);
    // Calibration instruments
    final InstrumentDerivative[] calibrationBasket = annuity.calibrationBasket(RatchetIborCalibrationType.FORWARD_COUPON, curves);
    //TODO: set a way to chose the calibration type.
    calibrationEngine.addInstrument(calibrationBasket, METHOD_CAP_SABR);
    // Calibration
    calibrationEngine.calibrate(curves);
    final HullWhiteOneFactorPiecewiseConstantDataBundle hwBundle = new HullWhiteOneFactorPiecewiseConstantDataBundle(hwParameters, curves);
    // Pricing
    final HullWhiteMonteCarloMethod methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), DEFAULT_NB_PATH);
    final CurrencyAmount pvMC = methodMC.presentValue(annuity, annuity.getCurrency(), annuity.getDiscountCurve(), hwBundle);
    return pvMC.getAmount();
  }

}
