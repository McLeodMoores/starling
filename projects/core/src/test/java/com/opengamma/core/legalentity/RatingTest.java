/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.legalentity;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;

import org.testng.annotations.Test;

import com.opengamma.core.obligor.CreditRating;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link Rating}.
 */
@Test(groups = TestGroup.UNIT)
public class RatingTest extends AbstractFudgeBuilderTestCase {
  private static final String RATER = "Moodys";
  private static final CreditRating RATING = CreditRating.AA;
  private static final SeniorityLevel SENIORITY = SeniorityLevel.SECDOM;

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final Rating rating = new Rating(RATER, RATING, SENIORITY);
    assertEquals(rating, rating);
    assertNotEquals(null, rating);
    assertNotEquals(RATING, rating);
    Rating other = new Rating(RATER, RATING, SENIORITY);
    assertEquals(rating, other);
    assertEquals(rating.hashCode(), other.hashCode());
    other = new Rating("Fitch", RATING, SENIORITY);
    assertNotEquals(rating, other);
    other = new Rating(RATER, CreditRating.A, SENIORITY);
    assertNotEquals(rating, other);
    other = new Rating(RATER, RATING, SeniorityLevel.LIEN1);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    assertEncodeDecodeCycle(Rating.class, new Rating(RATER, RATING, SENIORITY));
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final Rating rating = new Rating(RATER, RATING, SENIORITY);
    assertNotNull(rating.metaBean());
    assertNotNull(rating.metaBean().rater());
    assertNotNull(rating.metaBean().score());
    assertNotNull(rating.metaBean().seniorityLevel());
    assertEquals(rating.metaBean().rater().get(rating), RATER);
    assertEquals(rating.metaBean().score().get(rating), RATING);
    assertEquals(rating.metaBean().seniorityLevel().get(rating), SENIORITY);
    assertEquals(rating.property("rater").get(), RATER);
    assertEquals(rating.property("score").get(), RATING);
    assertEquals(rating.property("seniorityLevel").get(), SENIORITY);
  }
}
