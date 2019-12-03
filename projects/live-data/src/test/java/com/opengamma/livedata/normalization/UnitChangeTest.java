/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class UnitChangeTest {

  /**
   * Tests multiplication of the value of a single field.
   */
  @Test
  public void unitChangeSingleField() {
    final UnitChange unitChange = new UnitChange("Foo", 10);

    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add("Foo", 2.0);
    msg.add("Bar", 2);

    final MutableFudgeMsg normalized = unitChange.apply(msg, "123", new FieldHistoryStore());
    assertEquals(2, normalized.getAllFields().size());
    assertEquals(20.0, normalized.getDouble("Foo"), 0.0001);
  }

  /**
   * Tests the error when the value is not a number.
   */
  @Test(expectedExceptions = NumberFormatException.class)
  public void unitChangeSingleFieldWrongType() {
    final UnitChange unitChange = new UnitChange("Foo", 10);
    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add("Foo", "value");
    unitChange.apply(msg, "123", new FieldHistoryStore());
  }

  /**
   * Tests multiplication of the values of multiple fields.
   */
  public void testUnitChangeFieldSet() {
    final Set<String> fields = new HashSet<>();
    fields.add("a");
    fields.add("b");
    fields.add("z");
    final UnitChange change = new UnitChange(fields, 123);
    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add("a", 1);
    msg.add("b", 2);
    msg.add("c", 3);
    final MutableFudgeMsg multiplied = change.apply(msg, "uid", new FieldHistoryStore());
    assertEquals(multiplied.getAllFields().size(), 3);
    assertEquals(multiplied.getDouble("a"), 123.);
    assertEquals(multiplied.getDouble("b"), 246.);
    assertEquals(multiplied.getDouble("c"), 3.);
  }

  /**
   * Tests multiplication of the values of multiple fields.
   */
  public void testUnitChangeFieldVarArgs() {
    final UnitChange change = new UnitChange(123, "a", "b", "z");
    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add("a", 1);
    msg.add("b", 2);
    msg.add("c", 3);
    final MutableFudgeMsg multiplied = change.apply(msg, "uid", new FieldHistoryStore());
    assertEquals(multiplied.getAllFields().size(), 3);
    assertEquals(multiplied.getDouble("a"), 123.);
    assertEquals(multiplied.getDouble("b"), 246.);
    assertEquals(multiplied.getDouble("c"), 3.);
  }
}
