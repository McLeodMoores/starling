/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link FieldFilter}.
 */
@Test(groups = TestGroup.UNIT)
public class FieldFilterTest {

  /**
   * Tests the removal of fields.
   */
  public void normalCase() {
    final Set<String> fieldsToAccept = new HashSet<>();
    fieldsToAccept.add("Foo");
    fieldsToAccept.add("Bar");
    final FieldFilter filter = new FieldFilter(fieldsToAccept);

    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add("Foo", "1");
    msg.add("Bar", 2.0);
    msg.add("Baz", 500);

    final MutableFudgeMsg normalized = filter.apply(msg, "123", new FieldHistoryStore());
    assertEquals("1", normalized.getString("Foo"));
    assertEquals(2.0, normalized.getDouble("Bar"), 0.0001);
    assertNull(normalized.getByName("Baz"));
  }

  /**
   * Tests removing all fields.
   */
  public void extinguishmentWithNonEmptyFieldsToAccept() {
    final Set<String> fieldsToAccept = new HashSet<>();
    fieldsToAccept.add("Foo");
    fieldsToAccept.add("Bar");
    final FieldFilter filter = new FieldFilter(fieldsToAccept);

    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add("Foo2", "1");

    final MutableFudgeMsg normalized = filter.apply(msg, "123", new FieldHistoryStore());
    assertNull(normalized);
  }

  /**
   * Tests removing no fields.
   */
  public void extinguishmentWithEmptyFieldsToAccept() {
    final Set<String> fieldsToAccept = new HashSet<>();
    final FieldFilter filter = new FieldFilter(fieldsToAccept);

    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add("Foo", "1");

    final MutableFudgeMsg normalized = filter.apply(msg, "123", new FieldHistoryStore());
    assertNull(normalized);
  }

  /**
   * Tests that fields without a name are allowed.
   */
  public void testNoNameInField() {
    final String[] fieldsToAccept = { "x", "y" };
    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add("x", "value1");
    msg.add("y", "value2");
    msg.add("z", "value3");
    msg.add(4, "value4");
    final MutableFudgeMsg expected = OpenGammaFudgeContext.getInstance().newMessage();
    expected.add("x", "value1");
    expected.add("y", "value2");
    assertEquals(expected, new FieldFilter(fieldsToAccept).apply(msg, "123", new FieldHistoryStore()));
  }

  /**
   * Tests the equivalence of constructors.
   */
  public void testConstructors() {
    final Set<String> fieldsToAcceptSet = new HashSet<>(Arrays.asList("x", "y"));
    final String[] fieldsToAcceptArray = fieldsToAcceptSet.toArray(new String[0]);
    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add("x", "value1");
    msg.add("y", "value2");
    msg.add("z", "value3");
    final MutableFudgeMsg expected = OpenGammaFudgeContext.getInstance().newMessage();
    expected.add("x", "value1");
    expected.add("y", "value2");
    assertEquals(expected, new FieldFilter(fieldsToAcceptArray).apply(msg, "123", new FieldHistoryStore()));
    assertEquals(expected, new FieldFilter(OpenGammaFudgeContext.getInstance(), fieldsToAcceptArray).apply(msg, "123", new FieldHistoryStore()));
    assertEquals(expected, new FieldFilter(fieldsToAcceptSet).apply(msg, "123", new FieldHistoryStore()));
    assertEquals(expected, new FieldFilter(fieldsToAcceptSet, OpenGammaFudgeContext.getInstance()).apply(msg, "123", new FieldHistoryStore()));
  }
}
