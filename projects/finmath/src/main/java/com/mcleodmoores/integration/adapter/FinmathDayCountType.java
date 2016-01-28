/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to indicate that a class is an adapter for a Finmath day count.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Inherited
public @interface FinmathDayCountType {

  /**
   * The name of the day-count convention.
   */
  String name() default "";

  /**
   * Aliases for the day-count convention.
   */
  String[] aliases() default "";
}
