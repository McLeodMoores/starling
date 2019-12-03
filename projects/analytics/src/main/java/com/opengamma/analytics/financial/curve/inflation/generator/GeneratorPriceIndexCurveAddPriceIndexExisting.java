/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.inflation.generator;

import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurveAddPriceIndexSpreadCurve;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Store the details and generate the required curve. The curve is the sum (or difference) of two curves
 * (operation on the continuously-compounded zero-coupon rates): an existing curve referenced by its name and a new curve.
 * The generated curve is a YieldAndDiscountAddZeroSpreadCurve.
 */

public class GeneratorPriceIndexCurveAddPriceIndexExisting extends GeneratorPriceIndexCurve {

  /**
   * The generator for the new curve.
   */
  private final GeneratorPriceIndexCurve _generator;
  /**
   * If true the rate of the new curve will be subtracted from the first one. If false the rates are added.
   */
  private final boolean _substract;
  /**
   * The name of the existing curve.
   */
  private final String _existingCurveName;

  /**
   * The constructor.
   * @param generator The generator for the new curve.
   * @param substract If true the rate of the new curve will be subtracted from the first one. If false the rates are added.
   * @param existingCurveName The name of the existing curve.
   */
  public GeneratorPriceIndexCurveAddPriceIndexExisting(final GeneratorPriceIndexCurve generator, final boolean substract, final String existingCurveName) {
    ArgumentChecker.notNull(generator, "Generator");
    ArgumentChecker.notNull(existingCurveName, "Exisitng curve name");
    _generator = generator;
    _substract = substract;
    _existingCurveName = existingCurveName;
  }

  @Override
  public int getNumberOfParameter() {
    return _generator.getNumberOfParameter();
  }

  @Override
  public PriceIndexCurve generateCurve(final String name, final double[] parameters) {
    throw new UnsupportedOperationException("Cannot create the curve form the generator without an existing curve");
  }

  @Override
  public PriceIndexCurve generateCurve(final String name, final InflationProviderInterface inflation, final double[] parameters) {
    if (inflation instanceof InflationProviderDiscount) { // TODO: improve the way the curves are generated
      final PriceIndexCurve existingCurve = ((InflationProviderDiscount) inflation).getCurve(_existingCurveName);
      final PriceIndexCurve newCurve = _generator.generateCurve(name + "-0", inflation, parameters);
      return new PriceIndexCurveAddPriceIndexSpreadCurve(name, _substract, existingCurve, newCurve);

    }
    throw new UnsupportedOperationException("Cannot generate curves for a GeneratorCurveAddYieldExisiting");
  }

  @Override
  public GeneratorPriceIndexCurve finalGenerator(final Object data) {
    return new GeneratorPriceIndexCurveAddPriceIndexExisting(_generator.finalGenerator(data), _substract, _existingCurveName);
  }

  @Override
  public double[] initialGuess(final double[] rates) {
    ArgumentChecker.isTrue(rates.length == _generator.getNumberOfParameter(), "Rates of incorrect length.");
    final double[] spread = new double[rates.length];
    // Implementation note: The AddYieldExisting generator is used for spread. The initial guess is a spread of 0.
    return spread;
  }

}
