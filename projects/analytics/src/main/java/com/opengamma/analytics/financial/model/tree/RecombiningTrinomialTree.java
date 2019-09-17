/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.tree;

import com.opengamma.analytics.math.function.Function1D;

/**
 *
 * @param <T>
 *          The type of the data
 */
public class RecombiningTrinomialTree<T> extends RecombiningTree<T> {
  /**
   *
   */
  public static final Function1D<Integer, Integer> NODES = new Function1D<Integer, Integer>() {

    @Override
    public Integer apply(final Integer i) {
      return 2 * i + 1;
    }

  };

  public RecombiningTrinomialTree(final T[][] data) {
    super(data);
  }

  @Override
  protected int getMaxNodesForStep(final int step) {
    return NODES.apply(step);
  }

  //  @SuppressWarnings("unchecked")
  //  @Override
  //  public T[][] getEmptyNodes(int size) {
  //    Object[][] res = new Object[size+1][];
  //    for (int i = 0; i <= size; i++) {
  //      res[i] = new Object[NODES.evaluate(i)];
  //    }
  //    return (T[][]) res;
  //  }
}
