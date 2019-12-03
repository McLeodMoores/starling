/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;


/**
 * Default Exercise type visitor that returns a new instance of the exercise
 * type.
 */
public final class ExerciseTypeVisitorImpl implements ExerciseTypeVisitor<ExerciseType> {

  @Override
  public ExerciseType visitAmericanExerciseType(final AmericanExerciseType exerciseType) {
    return new AmericanExerciseType();
  }

  @Override
  public ExerciseType visitAsianExerciseType(final AsianExerciseType exerciseType) {
    return new AsianExerciseType();
  }

  @Override
  public ExerciseType visitBermudanExerciseType(final BermudanExerciseType exerciseType) {
    return new BermudanExerciseType();
  }

  @Override
  public ExerciseType visitEuropeanExerciseType(final EuropeanExerciseType exerciseType) {
    return new EuropeanExerciseType();
  }

}
