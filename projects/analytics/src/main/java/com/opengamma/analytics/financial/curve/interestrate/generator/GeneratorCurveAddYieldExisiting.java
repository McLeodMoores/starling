/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.interestrate.generator;

/**
 * Store the details and generate the required curve. The curve is the sum (or difference) of two curves (operation on the
 * continuously-compounded zero-coupon rates): an existing curve referenced by its name and a new curve. The generated curve is a
 * YieldAndDiscountAddZeroSpreadCurve.
 *
 * @deprecated Use {@link GeneratorCurveAddYieldExisting}, which doesn't have a typo in the name.
 */
@Deprecated
public class GeneratorCurveAddYieldExisiting extends GeneratorCurveAddYieldExisting {

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
  public GeneratorCurveAddYieldExisiting(final GeneratorYDCurve generator, final boolean subtract, final String existingCurveName) {
    super(generator, subtract, existingCurveName);
  }
}
