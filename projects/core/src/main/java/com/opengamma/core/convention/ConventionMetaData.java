/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.convention;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for classes that represent a convention item intended to be stored in a <code>ConventionMaster</code>.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface ConventionMetaData {

  /**
   * A short description of the convention to be used in GUIs.
   * @return  the description
   */
  String description() default "";

  /**
   * A categorization for conventions.
   * @return  the category
   */
  String group() default ConventionGroups.MISC;

  /**
   * A longer information string about how the convention is to be used.
   * @return  information about usage
   */
  String info() default "";
}
