/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

/**
 * Visitor for the {@code ExerciseType} subclasses.
 *
 * @param <T> visitor method return type
 */
public interface ExerciseTypeVisitor<T> {

  /**
   * Visits an American exercise type.
   *
   * @param exerciseType
   *          the exercise type
   * @return the result
   */
  T visitAmericanExerciseType(AmericanExerciseType exerciseType);

  /**
   * Visits an Asian exercise type.
   *
   * @param exerciseType
   *          the exercise type
   * @return the result
   */
  T visitAsianExerciseType(AsianExerciseType exerciseType);

  /**
   * Visits a Bermudan exercise type.
   *
   * @param exerciseType
   *          the exercise type
   * @return the result
   */
  T visitBermudanExerciseType(BermudanExerciseType exerciseType);

  /**
   * Visits a European exercise type.
   *
   * @param exerciseType
   *          the exercise type
   * @return the result
   */
  T visitEuropeanExerciseType(EuropeanExerciseType exerciseType);

}
