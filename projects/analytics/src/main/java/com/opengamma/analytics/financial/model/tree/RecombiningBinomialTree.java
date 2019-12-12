/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.tree;

import java.util.function.Function;

/**
 *
 * @param <T>
 *          The type of the data
 */
public class RecombiningBinomialTree<T> extends RecombiningTree<T> {
  /** Number of nodes at each level. */
  public static final Function<Integer, Integer> NODES = i -> i + 1;

  public RecombiningBinomialTree(final T[][] data) {
    super(data);
  }

  @Override
  protected int getMaxNodesForStep(final int step) {
    return NODES.apply(step);
  }

}
