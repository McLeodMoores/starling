/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web;

import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.ExerciseTypeNameVisitor;
import com.opengamma.util.ArgumentChecker;

/**
 * Utility class for custom object rendering
 */
public final class FreemarkerCustomRenderer {

  /**
   * Restrictive constructor
   */
  private FreemarkerCustomRenderer() {
  }

  /**
   * Singleton
   */
  public static final Object INSTANCE = new FreemarkerCustomRenderer();

  public String printExerciseType(final ExerciseType exerciseType) {
    ArgumentChecker.notNull(exerciseType, "exerciseType");
    final String result = exerciseType.accept(new ExerciseTypeNameVisitor());
    return result;
  }
}
