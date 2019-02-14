/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Test for {@link FieldNameChange}.
 */
@Test(groups = TestGroup.UNIT)
public class FieldNameChangeTest {

  /**
   * Tests that the name to change from cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNameFrom() {
    new FieldNameChange(null, "to");
  }

  /**
   * Tests that the name to change to cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNameTo() {
    new FieldNameChange("from", null);
  }

  /**
   * Tests a field name change.
   */
  @Test
  public void fieldNameChange() {
    final FieldNameChange nameChange = new FieldNameChange("Foo", "Bar");

    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add("Foo", "1");
    msg.add("Bar", 2.0);
    msg.add("Baz", 500);

    final MutableFudgeMsg normalized = nameChange.apply(msg, "123", new FieldHistoryStore());
    assertEquals(3, normalized.getAllFields().size());
    assertNull(normalized.getByName("Foo"));
    assertEquals(2.0, (Double) normalized.getAllByName("Bar").get(0).getValue(), 0.0001);
    assertEquals("1", (String) normalized.getAllByName("Bar").get(1).getValue());
    assertEquals(500, normalized.getInt("Baz").intValue());
  }

  /**
   * Tests a field name change.
   */
  @Test
  public void fieldNameChangeNoMatch() {
    final FieldNameChange nameChange = new FieldNameChange("Foob", "Bar");

    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add("Foo", "1");
    msg.add("Bar", 2.0);
    msg.add("Baz", 500);

    final MutableFudgeMsg normalized = nameChange.apply(msg, "123", new FieldHistoryStore());
    assertEquals(3, normalized.getAllFields().size());
    assertNull(normalized.getByName("Foob"));
    assertEquals(2.0, (Double) normalized.getAllByName("Bar").get(0).getValue(), 0.0001);
    assertEquals("1", (String) normalized.getAllByName("Foo").get(0).getValue());
    assertEquals(500, normalized.getInt("Baz").intValue());
  }

}
