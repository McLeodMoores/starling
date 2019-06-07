/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

/**
 * @param <S>
 *          Type of the arguments
 * @param <T>
 *          Return type of the function
 */
public interface BlackVolatilitySurfaceVisitor<S, T> {

  T visitDelta(BlackVolatilitySurfaceDelta surface, S data);

  T visitStrike(BlackVolatilitySurfaceStrike surface, S data);

  T visitMoneyness(BlackVolatilitySurfaceMoneyness surface, S data);

  T visitLogMoneyness(BlackVolatilitySurfaceLogMoneyness surface, S data);

  T visitDelta(BlackVolatilitySurfaceDelta surface);

  T visitStrike(BlackVolatilitySurfaceStrike surface);

  T visitMoneyness(BlackVolatilitySurfaceMoneyness surface);

  T visitLogMoneyness(BlackVolatilitySurfaceLogMoneyness surface);
}
