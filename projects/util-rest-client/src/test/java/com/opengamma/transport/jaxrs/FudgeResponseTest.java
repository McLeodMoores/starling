/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.transport.jaxrs;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link FudgeResponse}.
 */
@Test(groups = TestGroup.UNIT)
public class FudgeResponseTest {
  private static final Object VALUE = "value";
  private static final FudgeResponse RESPONSE = FudgeResponse.of(VALUE);

  /**
   * Tests the unwrap method.
   */
  @Test
  public void testUnwrap() {
    assertEquals(FudgeResponse.unwrap(VALUE), VALUE);
    assertEquals(FudgeResponse.unwrap(RESPONSE), VALUE);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    assertEquals(RESPONSE, RESPONSE);
    assertNotEquals(null, RESPONSE);
    assertNotEquals(VALUE, RESPONSE);
    assertEquals(RESPONSE.getValue(), VALUE);
    assertEquals(RESPONSE.toString(), "FudgeResponse[value]");
    FudgeResponse other = FudgeResponse.of(VALUE);
    assertEquals(RESPONSE, other);
    assertEquals(RESPONSE.hashCode(), other.hashCode());
    other = FudgeResponse.of("other");
    assertNotEquals(RESPONSE, other);
  }
}
