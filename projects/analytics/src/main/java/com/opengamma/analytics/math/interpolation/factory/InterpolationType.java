/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to indicate that a class is an implementation of an {@link com.opengamma.analytics.math.interpolation.Interpolator}
 * that is used by factories.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Inherited
public @interface InterpolationType {

  /**
   * The name of the interpolator.
   */
  String name();

  /**
   * Any aliases of the interpolator.
   */
  String[] aliases() default { };

}
