/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import com.opengamma.analytics.financial.model.tree.RecombiningBinomialTree;

/**
 *
 * @param <T>
 *          The type of the option
 * @param <U>
 *          The type of the market data
 */
public abstract class BinomialOptionModelDefinition<T extends OptionDefinition, U extends StandardOptionDataBundle> {

  public abstract double getUpFactor(T option, U data, int n, int j);

  public abstract double getDownFactor(T option, U data, int n, int j);

  public abstract RecombiningBinomialTree<Double> getUpProbabilityTree(T option, U data, int n, int j);

}
