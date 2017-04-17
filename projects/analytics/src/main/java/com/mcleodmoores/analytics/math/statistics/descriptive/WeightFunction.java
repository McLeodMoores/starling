/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.math.statistics.descriptive;

/**
 * An interface for functions that provide a stream of weights. This stream could be finite or
 * infinite.
 * @param <T>  the type of the weight that is returned
 */
public interface WeightFunction<T> {

  /**
   * Gets the next weight.
   * @return  the weight
   */
  T get();
}
