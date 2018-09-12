/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.util;

import static org.testng.Assert.assertSame;

import org.joda.beans.ser.JodaBeanSer;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link JodaBeanSerialization}.
 */
@Test(groups = TestGroup.UNIT)
public class JodaBeanSerializationTest {

  /**
   * Tests which serializer is returned.
   */
  @Test
  public void testSerializer() {
    assertSame(JodaBeanSerialization.serializer(true), JodaBeanSer.PRETTY);
    assertSame(JodaBeanSerialization.serializer(false), JodaBeanSer.COMPACT);
  }

  /**
   * Gets the default serializer.
   */
  @Test
  public void testDefault() {
    assertSame(JodaBeanSerialization.deserializer(), JodaBeanSer.PRETTY);
  }
}
