/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.util.result;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests.
 */
@SuppressWarnings("deprecation")
@Test(groups = TestGroup.UNIT)
public class ThrowableDetailsTest extends AbstractFudgeBuilderTestCase {

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final Exception ex = new IllegalArgumentException("message");
    final ThrowableDetails td = ThrowableDetails.of(ex);
    assertEquals(td, td);
    assertNotEquals(null, td);
    assertNotEquals(ex, td);
    ThrowableDetails other = ThrowableDetails.of(ex);
    assertEquals(td, other);
    assertEquals(td.hashCode(), other.hashCode());
    other = ThrowableDetails.of(new IllegalArgumentException("other"));
    assertNotEquals(td, other);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final Exception ex = new IllegalArgumentException("message");
    final ThrowableDetails td = ThrowableDetails.of(ex);
    assertEquals(cycleObjectJodaXml(ThrowableDetails.class, td), td);
  }
}
