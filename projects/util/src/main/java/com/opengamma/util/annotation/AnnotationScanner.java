/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.annotation;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Scans Opengamma archive for a given annotation.
 */
public interface AnnotationScanner {

  /**
   * Scans the archive for a particular annotation.
   *
   * @param annotationClass  the annotation class
   * @return  the set of annotated classes
   */
  Set<Class<?>> scan(Class<? extends Annotation> annotationClass);

}
