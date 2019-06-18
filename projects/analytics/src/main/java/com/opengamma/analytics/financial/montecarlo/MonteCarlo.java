/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.montecarlo;

import com.opengamma.analytics.financial.model.stochastic.StochasticProcess;

/**
 *
 * @param <U>
 *          The type of the process
 * @param <S>
 *          The type of the option
 * @param <T>
 *          The type of the market data
 */
public interface MonteCarlo<U extends StochasticProcess<S, T>, S, T> {

  double[] getPath(U process, int n);
}
