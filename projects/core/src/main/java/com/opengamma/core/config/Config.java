/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for classes that represents a configuration item intended to be stored in a <code>ConfigMaster</code>.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Config {

  /**
   * The class type to use for searching for the configuration item in a ConfigMaster.
   * <p>
   * Optional search type when it is different from base class
   *
   * @return  the class of the search type
   */
  Class<?> searchType() default Object.class;

  /**
   * A short description of the configuration to be used in GUIs.
   * <p>
   * This is typically an expansion of the class name.
   *
   * @return  the description
   */
  String description() default "";

  /**
   * A way to categorize configurations to enable meaningful grouping used in GUIs.
   *
   * @return  the configuration group
   */
  String group() default ConfigGroups.MISC;

}
