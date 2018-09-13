/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.transport.jaxrs;

import static org.testng.Assert.assertEquals;

import org.fudgemsg.FudgeContext;
import org.testng.annotations.Test;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link FudgeBase}.
 */
@Test(groups = TestGroup.UNIT)
public class FudgeBaseTest {

  /**
   * Tests that the Fudge context cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullContext1() {
    new DummyInstance(null);
  }

  /**
   * Tests that the Fudge context cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullContext2() {
    new DummyInstance().setFudgeContext(null);
  }

  /**
   * Tests that the taxonomy id cannot be less than -2e15.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMinTaxonomyId() {
    final DummyInstance base = new DummyInstance();
    base.setFudgeTaxonomyId(Short.MIN_VALUE - 1);
  }

  /**
   * Tests that the taxonomy id cannot be less than 2e15.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMaxTaxonomyId() {
    final DummyInstance base = new DummyInstance();
    base.setFudgeTaxonomyId(Short.MAX_VALUE + 1);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final DummyInstance base = new DummyInstance();
    base.setFudgeContext(OpenGammaFudgeContext.getInstance());
    base.setFudgeTaxonomyId(8);
    assertEquals(base.getFudgeTaxonomyId(), 8);
  }

  /**
   * Dummy implementation.
   */
  private static class DummyInstance extends FudgeBase {

    protected DummyInstance() {
      super();
    }

    protected DummyInstance(final FudgeContext context) {
      super(context);
    }
  }
}
