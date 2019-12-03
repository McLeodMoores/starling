/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.rootfinding.newton;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that shows that the class is a {@link com.opengamma.analytics.math.rootfinding.VectorRootFinder} that is used by factories.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE})
@Inherited
public @interface VectorRootFinderType {

  /**
   * The name of the root finder.
   * 
   * @return the name
   */
  String name();
}
