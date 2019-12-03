/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.util.result;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests.
 */
@SuppressWarnings("deprecation")
@Test(groups = TestGroup.UNIT)
public class MultipleFailureResultTest extends AbstractFudgeBuilderTestCase {
  private static final Failure FAILURE_1 = new Failure(FailureStatus.CALCULATION_FAILED, "message1");
  private static final Failure FAILURE_2 = new Failure(FailureStatus.ERROR, "message2");
  private static final Failure FAILURE_3 = new Failure(FailureStatus.INVALID_INPUT, "message3");

  /**
   * Tests that multiple failures are required.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMultiple() {
    MultipleFailureResult.of(Arrays.asList(FAILURE_1));
  }

  /**
   * Tests construction.
   */
  public void testConstruction() {
    final Result<Object> result = MultipleFailureResult.of(Arrays.asList(FAILURE_1, FAILURE_2));
    assertEquals(MultipleFailureResult.of(Arrays.asList(FAILURE_1, FAILURE_2)), result);
  }

  /**
   * Tests the object.
   */
  public void testObject() {
    final Result<Object> result = MultipleFailureResult.of(Arrays.asList(FAILURE_1, FAILURE_2, FAILURE_3));
    assertEquals(result, result);
    assertNotEquals(null, result);
    assertNotEquals(FAILURE_1, result);
    Result<Object> other = MultipleFailureResult.of(Arrays.asList(FAILURE_1, FAILURE_2, FAILURE_3));
    assertEquals(result, other);
    assertEquals(result.hashCode(), other.hashCode());
    other = MultipleFailureResult.of(Arrays.asList(FAILURE_1, FAILURE_2));
    assertNotEquals(result, other);
  }

  /**
   * Tests the bean.
   */
  public void testBean() {
    final List<Failure> failures = Arrays.asList(FAILURE_1, FAILURE_2, FAILURE_3);
    final MultipleFailureResult<Object> result = (MultipleFailureResult<Object>) MultipleFailureResult.of(failures);
    assertEquals(result.metaBean().failures().get(result), failures);
    assertEquals(result.metaBean().status().get(result), FailureStatus.MULTIPLE);
    assertEquals(result.metaBean().message().get(result), "message1\nmessage2\nmessage3");
    assertEquals(result.property("status").get(), FailureStatus.MULTIPLE);
    assertEquals(result.property("message").get(), "message1\nmessage2\nmessage3");
  }

  /**
   * Tests a cycle.
   */
  public void testCycle() {
    final List<Failure> failures = Arrays.asList(FAILURE_1, FAILURE_2, FAILURE_3);
    final Result<Object> result = MultipleFailureResult.of(failures);
    assertEquals(cycleObjectJodaXml(Result.class, result), result);
  }
}
