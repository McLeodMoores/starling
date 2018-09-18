/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.util.result;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.google.common.base.Throwables;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link Failure}.
 */
@Test(groups = TestGroup.UNIT)
public class FailureTest extends AbstractFudgeBuilderTestCase {
  private static final FailureStatus STATUS = FailureStatus.CALCULATION_FAILED;
  private static final String MESSAGE = "message";
  private static final IllegalArgumentException EXCEPTION = new IllegalArgumentException("exception");

  /**
   * Tests constructor equivalence.
   */
  @Test
  public void testConstructors() {
    assertEquals(new Failure(EXCEPTION), new Failure(FailureStatus.ERROR, EXCEPTION.getMessage(), EXCEPTION));
    assertEquals(new Failure(EXCEPTION, MESSAGE), new Failure(FailureStatus.ERROR, MESSAGE, EXCEPTION));
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final Failure failure = new Failure(STATUS, MESSAGE, EXCEPTION);
    assertEquals(failure, failure);
    assertNotEquals(null, failure);
    assertNotEquals(STATUS, failure);
    Failure other = new Failure(STATUS, MESSAGE, EXCEPTION);
    assertEquals(failure, other);
    assertEquals(failure.hashCode(), other.hashCode());
    other = new Failure(FailureStatus.ERROR, MESSAGE, EXCEPTION);
    assertNotEquals(failure, other);
    other = new Failure(STATUS, "other", EXCEPTION);
    assertNotEquals(failure, other);
    other = new Failure(STATUS, MESSAGE, new UnsupportedOperationException());
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final Failure failure = new Failure(STATUS, MESSAGE, EXCEPTION);
    assertEquals(failure.metaBean().status().get(failure), STATUS);
    assertEquals(failure.metaBean().message().get(failure), MESSAGE);
    assertEquals(failure.metaBean().stackTrace().get(failure), Throwables.getStackTraceAsString(EXCEPTION));
    assertEquals(failure.property("status").get(), STATUS);
    assertEquals(failure.property("message").get(), MESSAGE);
    assertEquals(failure.property("stackTrace").get(), Throwables.getStackTraceAsString(EXCEPTION));
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final Failure failure = new Failure(STATUS, MESSAGE, EXCEPTION);
    assertEquals(cycleObjectJodaXml(Failure.class, failure), failure);
  }
}
