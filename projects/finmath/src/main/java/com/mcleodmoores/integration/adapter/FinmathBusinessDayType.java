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
 * An annotation to indicate that a class is an adapter for a Finmath business day.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Inherited
public @interface FinmathBusinessDayType {

  /**
   * The name of the convention. If present, this field is used in the factory.
   */
  String name() default "";

  /**
   * Alternative names of the convention. If present, these fields are used in the factory.
   */
  String[] aliases() default "";
}
