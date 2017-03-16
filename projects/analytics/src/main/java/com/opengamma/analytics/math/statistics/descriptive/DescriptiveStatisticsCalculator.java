/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.NamedInstance;

/**
 * Parent class for descriptive statistics calculator e.g. mean, standard deviation, etc.
 */
public abstract class DescriptiveStatisticsCalculator extends Function1D<double[], Double> implements NamedInstance {

}
