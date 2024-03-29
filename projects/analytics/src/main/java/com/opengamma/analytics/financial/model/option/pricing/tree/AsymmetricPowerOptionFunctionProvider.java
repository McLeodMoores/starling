/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import com.google.common.primitives.Doubles;
import com.opengamma.util.ArgumentChecker;

/**
 * Payoff of asymmetric power option is max( S^i - K , 0 ) for call and max( K - S^i , 0 ) for put with i &gt; 0.
 */
public class AsymmetricPowerOptionFunctionProvider extends OptionFunctionProvider1D {

  private final double _power;

  /**
   * @param strike
   *          Strike price, K
   * @param timeToExpiry
   *          Time to expiry
   * @param steps
   *          Number of steps
   * @param isCall
   *          True if call, false if put
   * @param power
   *          Power, i
   */
  public AsymmetricPowerOptionFunctionProvider(final double strike, final double timeToExpiry, final int steps, final boolean isCall, final double power) {
    super(strike, timeToExpiry, steps, isCall);
    ArgumentChecker.isTrue(power > 0., "power should be positive");
    ArgumentChecker.isTrue(Doubles.isFinite(power), "power should be finite");
    _power = power;
  }

  @Override
  public double[] getPayoffAtExpiry(final double assetPrice, final double downFactor, final double upOverDown) {
    final double strike = getStrike();
    final int nSteps = getNumberOfSteps();
    final int nStepsP = nSteps + 1;
    final double sign = getSign();

    final double[] values = new double[nStepsP];
    double priceTmp = assetPrice * Math.pow(downFactor, nSteps);
    for (int i = 0; i < nStepsP; ++i) {
      values[i] = Math.max(sign * (Math.pow(priceTmp, _power) - strike), 0.);
      priceTmp *= upOverDown;
    }
    return values;
  }

  @Override
  public double[] getPayoffAtExpiryTrinomial(final double assetPrice, final double downFactor, final double middleOverDown) {
    final double strike = getStrike();
    final int nSteps = getNumberOfSteps();
    final int nNodes = 2 * getNumberOfSteps() + 1;
    final double sign = getSign();

    final double[] values = new double[nNodes];
    double priceTmp = assetPrice * Math.pow(downFactor, nSteps);
    for (int i = 0; i < nNodes; ++i) {
      values[i] = Math.max(sign * (Math.pow(priceTmp, _power) - strike), 0.);
      priceTmp *= middleOverDown;
    }
    return values;
  }

  /**
   * Access power.
   * 
   * @return _power
   */
  public double getPower() {
    return _power;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_power);
    result = prime * result + (int) (temp ^ temp >>> 32);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof AsymmetricPowerOptionFunctionProvider)) {
      return false;
    }
    final AsymmetricPowerOptionFunctionProvider other = (AsymmetricPowerOptionFunctionProvider) obj;
    if (Double.doubleToLongBits(_power) != Double.doubleToLongBits(other._power)) {
      return false;
    }
    return true;
  }

}
