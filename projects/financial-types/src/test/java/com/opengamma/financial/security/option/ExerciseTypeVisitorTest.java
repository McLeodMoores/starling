/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.option;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotSame;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ExerciseTypeVisitor} and implementations.
 */
@Test(groups = TestGroup.UNIT)
public class ExerciseTypeVisitorTest {
  private static final ExerciseType AMERICAN = new AmericanExerciseType();
  private static final ExerciseType ASIAN = new AsianExerciseType();
  private static final ExerciseType BERMUDAN = new BermudanExerciseType();
  private static final ExerciseType EUROPEAN = new EuropeanExerciseType();

  /**
   * Tests the name visitor.
   */
  public void testNameVisitor() {
    final ExerciseTypeNameVisitor visitor = new ExerciseTypeNameVisitor();
    assertEquals(AMERICAN.accept(visitor), "American");
    assertEquals(ASIAN.accept(visitor), "Asian");
    assertEquals(BERMUDAN.accept(visitor), "Bermudan");
    assertEquals(EUROPEAN.accept(visitor), "European");
  }

  /**
   * Tests the default implementation.
   */
  public void testDefaultVisitor() {
    final ExerciseTypeVisitor<ExerciseType> visitor = new ExerciseTypeVisitorImpl();
    assertNotSame(AMERICAN.accept(visitor), AMERICAN);
    assertEquals(AMERICAN.accept(visitor), AMERICAN);
    assertNotSame(ASIAN.accept(visitor), ASIAN);
    assertEquals(ASIAN.accept(visitor), ASIAN);
    assertNotSame(BERMUDAN.accept(visitor), BERMUDAN);
    assertEquals(BERMUDAN.accept(visitor), BERMUDAN);
    assertNotSame(EUROPEAN.accept(visitor), EUROPEAN);
    assertEquals(EUROPEAN.accept(visitor), EUROPEAN);
  }

  /**
   * Tests the visitor.
   */
  public void testVisitor() {
    final ExerciseTypeVisitor<String> visitor = TestVisitor.INSTANCE;
    assertEquals(AMERICAN.accept(visitor), "Am");
    assertEquals(ASIAN.accept(visitor), "As");
    assertEquals(BERMUDAN.accept(visitor), "Be");
    assertEquals(EUROPEAN.accept(visitor), "Eu");
  }

  /**
   *
   */
  private static final class TestVisitor implements ExerciseTypeVisitor<String> {
    public static final TestVisitor INSTANCE = new TestVisitor();

    private TestVisitor() {
    }

    @Override
    public String visitAmericanExerciseType(final AmericanExerciseType exerciseType) {
      return exerciseType.getName().substring(0, 2);
    }

    @Override
    public String visitAsianExerciseType(final AsianExerciseType exerciseType) {
      return exerciseType.getName().substring(0, 2);
    }

    @Override
    public String visitBermudanExerciseType(final BermudanExerciseType exerciseType) {
      return exerciseType.getName().substring(0, 2);
    }

    @Override
    public String visitEuropeanExerciseType(final EuropeanExerciseType exerciseType) {
      return exerciseType.getName().substring(0, 2);
    }

  }
}
