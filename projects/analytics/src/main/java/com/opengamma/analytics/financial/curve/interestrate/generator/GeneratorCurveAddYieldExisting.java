/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.interestrate.generator;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountAddZeroSpreadCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Store the details and generate the required curve. The curve is the sum (or difference) of two curves (operation on the
 * continuously-compounded zero-coupon rates): an existing curve referenced by its name and a new curve. The generated curve is a
 * YieldAndDiscountAddZeroSpreadCurve.
 */
public class GeneratorCurveAddYieldExisting extends GeneratorYDCurve {

  /**
   * The generator for the new curve.
   */
  private final GeneratorYDCurve _generator;
  /**
   * If true the rate of the new curve will be subtracted from the first one. If false the rates are added.
   */
  private final boolean _subtract;
  /**
   * The name of the existing curve.
   */
  private final String _existingCurveName;

  /**
   * The constructor.
   *
   * @param generator
   *          The generator for the new curve.
   * @param subtract
   *          If true the rate of the new curve will be subtracted from the first one. If false the rates are added.
   * @param existingCurveName
   *          The name of the existing curve.
   */
  public GeneratorCurveAddYieldExisting(final GeneratorYDCurve generator, final boolean subtract, final String existingCurveName) {
    ArgumentChecker.notNull(generator, "Generator");
    ArgumentChecker.notNull(existingCurveName, "Exisitng curve name");
    _generator = generator;
    _subtract = subtract;
    _existingCurveName = existingCurveName;
  }

  @Override
  public int getNumberOfParameter() {
    return _generator.getNumberOfParameter();
  }

  @Override
  public YieldAndDiscountCurve generateCurve(final String name, final double[] parameters) {
    throw new UnsupportedOperationException("Cannot create the curve form the generator without an existing curve");
  }

  @Override
  public YieldAndDiscountCurve generateCurve(final String name, final MulticurveProviderInterface multicurve, final double[] parameters) {
    if (multicurve instanceof MulticurveProviderDiscount) { // TODO: improve the way the curves are generated
      final YieldAndDiscountCurve existingCurve = ((MulticurveProviderDiscount) multicurve).getCurve(_existingCurveName);
      final YieldAndDiscountCurve newCurve = _generator.generateCurve(name + "-0", multicurve, parameters);
      return new YieldAndDiscountAddZeroSpreadCurve(name, _subtract, existingCurve, newCurve);
    }
    throw new UnsupportedOperationException("Cannot generate curves for a GeneratorCurveAddYieldExisting");
  }

  @Override
  public GeneratorYDCurve finalGenerator(final Object data) {
    return new GeneratorCurveAddYieldExisting(_generator.finalGenerator(data), _subtract, _existingCurveName);
  }

  @Override
  public double[] initialGuess(final double[] rates) {
    ArgumentChecker.isTrue(rates.length == _generator.getNumberOfParameter(), "Rates of incorrect length.");
    final double[] spread = new double[rates.length];
    // Implementation note: The AddYieldExisting generator is used for spread. The initial guess is a spread of 0.
    return spread;
  }

}
