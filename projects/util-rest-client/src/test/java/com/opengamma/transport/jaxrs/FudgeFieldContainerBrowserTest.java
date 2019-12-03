/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.transport.jaxrs;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link FudgeFieldContainerBrowser}.
 */
@Test(groups = TestGroup.UNIT)
public class FudgeFieldContainerBrowserTest {
  private static final MutableFudgeMsg MESSAGE = OpenGammaFudgeContext.getInstance().newMessage();
  private static final MutableFudgeMsg SUB_MESSAGE = OpenGammaFudgeContext.getInstance().newMessage();
  private static final FudgeFieldContainerBrowser CONTAINER = new FudgeFieldContainerBrowser(MESSAGE);
  static {
    MESSAGE.add("scalar", 3.);
    SUB_MESSAGE.add("scalar", "2");
    MESSAGE.add("subMsg", SUB_MESSAGE);
  }

  /**
   * Tests the field name path.
   */
  @Test
  public void testNullMessage() {
    assertNull(CONTAINER.get("field"));
  }

  /**
   * Tests a value.
   */
  @Test
  public void testScalarValue() {
    assertNull(CONTAINER.get("scalar"));
  }

  /**
   * Tests a sub-message.
   */
  @Test
  public void testSubMessage() {
    assertEquals(CONTAINER.get("subMsg").get().getMessage(), SUB_MESSAGE);
  }
}
