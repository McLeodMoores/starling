/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.fourier;

import static com.opengamma.analytics.math.ComplexMathUtils.add;
import static com.opengamma.analytics.math.ComplexMathUtils.multiply;
import static com.opengamma.analytics.math.number.ComplexNumber.MINUS_I;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.number.ComplexNumber;

/**
 *
 */
public abstract class MeanCorrection implements MartingaleCharacteristicExponent {
  private final CharacteristicExponent _base;

  public MeanCorrection(final CharacteristicExponent base) {
    Validate.notNull(base, "null base ce");
    _base = base;
  }

  /**
   * Returns the mean (drift) corrected characteristic exponent.
   *
   * @param t
   *          The time
   * @return A function to calculate the characteristic exponent
   */
  @Override
  public Function1D<ComplexNumber, ComplexNumber> getFunction(final double t) {

    final Function1D<ComplexNumber, ComplexNumber> func = _base.getFunction(t);
    final ComplexNumber temp = func.apply(MINUS_I);
    Validate.isTrue(Math.abs(temp.getImaginary()) < 1e-12, "problem with CharacteristicExponent");
    final ComplexNumber w = new ComplexNumber(0, -temp.getReal());

    return new Function1D<ComplexNumber, ComplexNumber>() {
      @Override
      public ComplexNumber evaluate(final ComplexNumber u) {
        return add(func.apply(u), multiply(w, u));
      }
    };
  }

  @Override
  public ComplexNumber getValue(final ComplexNumber u, final double t) {
    final ComplexNumber temp = _base.getValue(MINUS_I, t);
    Validate.isTrue(Math.abs(temp.getImaginary()) < 1e-12, "problem with CharacteristicExponent");
    final ComplexNumber w = new ComplexNumber(0, -temp.getReal());
    return add(_base.getValue(u, t), multiply(w, u));
  }

  @Override
  public Function1D<ComplexNumber, ComplexNumber[]> getAdjointFunction(final double t) {
    final Function1D<ComplexNumber, ComplexNumber[]> func = _base.getAdjointFunction(t);
    final ComplexNumber[] temp = func.apply(MINUS_I);
    return new Function1D<ComplexNumber, ComplexNumber[]>() {

      @Override
      public ComplexNumber[] evaluate(final ComplexNumber u) {
        final ComplexNumber[] uncorrected = func.apply(u);
        final ComplexNumber minusUi = multiply(MINUS_I, u);
        final ComplexNumber[] res = new ComplexNumber[temp.length];
        for (int i = 0; i < temp.length; i++) {
          res[i] = add(uncorrected[i], multiply(minusUi, temp[i]));
        }
        return res;
      }
    };
  }

  @Override
  public ComplexNumber[] getCharacteristicExponentAdjoint(final ComplexNumber u, final double t) {
    final ComplexNumber[] temp = _base.getCharacteristicExponentAdjoint(MINUS_I, t);
    final ComplexNumber[] uncorrected = _base.getCharacteristicExponentAdjoint(u, t);
    final ComplexNumber minusUi = multiply(MINUS_I, u);
    final ComplexNumber[] res = new ComplexNumber[temp.length];
    for (int i = 0; i < temp.length; i++) {
      res[i] = add(uncorrected[i], multiply(minusUi, temp[i]));
    }
    return res;
  }

  @Override
  public double getLargestAlpha() {
    return _base.getLargestAlpha();
  }

  @Override
  public double getSmallestAlpha() {
    return _base.getSmallestAlpha();
  }

  public CharacteristicExponent getBase() {
    return _base;
  }

}
