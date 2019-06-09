/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

/**
 * Visitor that gives the name of an Exercise type.
 */
public class ExerciseTypeNameVisitor implements ExerciseTypeVisitor<String> {
  /** American type. */
  public static final String AMERICAN = "American";
  /** Asian type. */
  public static final String ASIAN = "Asian";
  /** Bermudan type. */
  public static final String BERMUDAN = "Bermudan";
  /** European type. */
  public static final String EUROPEAN = "European";

  @Override
  public String visitAmericanExerciseType(final AmericanExerciseType exerciseType) {
    return AMERICAN;
  }

  @Override
  public String visitAsianExerciseType(final AsianExerciseType exerciseType) {
    return ASIAN;
  }

  @Override
  public String visitBermudanExerciseType(final BermudanExerciseType exerciseType) {
    return BERMUDAN;
  }

  @Override
  public String visitEuropeanExerciseType(final EuropeanExerciseType exerciseType) {
    return EUROPEAN;
  }

}
