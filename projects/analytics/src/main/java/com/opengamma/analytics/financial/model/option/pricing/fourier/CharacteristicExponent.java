/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.fourier;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.number.ComplexNumber;

/**
 * The characteristic exponent for use in pricing using fast Fourier transforms.
 */
public interface CharacteristicExponent {

  /**
   * Returns the characteristic exponent function.
   * 
   * @param t
   *          The time
   * @return A function to calculate the characteristic exponent
   */
  Function1D<ComplexNumber, ComplexNumber> getFunction(double t);

  /**
   * Evaluates the characteristic exponent at a particular (complex) u, and time t.
   * 
   * @param u
   *          complex value
   * @param t
   *          time
   * @return the characteristic exponent
   */
  ComplexNumber getValue(ComplexNumber u, double t);

  /**
   * Evaluates the characteristic exponent at a particular (complex) u, and time t, and the first derivative of the characteristic exponent wrt each of its
   * parameters (also at u and t).
   * 
   * @param u
   *          The complex value that the characteristic exponent is evaluated at
   * @param t
   *          The time that the characteristic exponent is evaluated at
   * @return Array of ComplexNumbers with the first entry being the value of the characteristic exponent, and subsequent entries being derivatives WRT each
   *         parameter
   */
  ComplexNumber[] getCharacteristicExponentAdjoint(ComplexNumber u, double t);

  /**
   * Returns the characteristic exponent adjoint function.
   * 
   * @param t
   *          The time
   * @return A function to calculate the characteristic exponent
   */
  Function1D<ComplexNumber, ComplexNumber[]> getAdjointFunction(double t);

  /**
   * Returns the largest allowable value of $\alpha$, the contour along which the characteristic function is integrated.
   * 
   * @return Returns the largest allowable value of $\alpha$
   */
  double getLargestAlpha();

  /**
   * Returns the largest allowable value of $\alpha$, the contour along which the characteristic function is integrated.
   * 
   * @return Returns the smallest allowable value of $\alpha$
   */
  double getSmallestAlpha();

}
