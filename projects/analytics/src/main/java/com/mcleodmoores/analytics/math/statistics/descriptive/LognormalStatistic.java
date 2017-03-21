/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.math.statistics.descriptive;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to indicate that a class is a lognormal statistics calculator. This allows these classes to
 * be used in a {@link com.opengamma.util.NamedInstanceFactory}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface LognormalStatistic {

  /**
   * Any aliases of the calculator.
   */
  String[] aliases() default { };
}
