/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.financial.ExerciseDecisionType;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.AsianExerciseType;
import com.opengamma.financial.security.option.BermudanExerciseType;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.ExerciseTypeVisitor;

/**
 *
 */
public final class ExerciseTypeAnalyticsVisitorAdapter implements ExerciseTypeVisitor<ExerciseDecisionType> {
  private static final ExerciseTypeAnalyticsVisitorAdapter INSTANCE = new ExerciseTypeAnalyticsVisitorAdapter();

  public static ExerciseTypeAnalyticsVisitorAdapter getInstance() {
    return INSTANCE;
  }
  /**
   *
   */
  private ExerciseTypeAnalyticsVisitorAdapter() {
  }

  @Override
  public ExerciseDecisionType visitAmericanExerciseType(final AmericanExerciseType exerciseType) {
    return ExerciseDecisionType.AMERICAN;
  }

  @Override
  public ExerciseDecisionType visitAsianExerciseType(final AsianExerciseType exerciseType) {
    throw new NotImplementedException();
  }

  @Override
  public ExerciseDecisionType visitBermudanExerciseType(final BermudanExerciseType exerciseType) {
    throw new NotImplementedException();
  }

  @Override
  public ExerciseDecisionType visitEuropeanExerciseType(final EuropeanExerciseType exerciseType) {
    return ExerciseDecisionType.EUROPEAN;
  }

}
