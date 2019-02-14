/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;

import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class RequiredFieldFilterTest {

  /**
   * Tests that the message is unchanged if there are no required fields.
   */
  public void noRequiredFields() {
    final RequiredFieldFilter filter = new RequiredFieldFilter();

    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add("Foo", "1");
    msg.add("Bar", 2.0);
    msg.add("Baz", 500);

    final MutableFudgeMsg normalized = filter.apply(msg, "123", new FieldHistoryStore());
    assertNotNull(normalized);
    assertSame(normalized, msg);
  }

  /**
   * Tests that null is returned if all of the required fields are not
   * available.
   */
  public void requiredFieldsNotSatisfied() {
    final RequiredFieldFilter filter = new RequiredFieldFilter("Foo", "Fibble");

    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add("Foo", "1");
    msg.add("Bar", 2.0);
    msg.add("Baz", 500);

    final MutableFudgeMsg normalized = filter.apply(msg, "123", new FieldHistoryStore());
    assertNull(normalized);
  }

  /**
   * Tests the message when all required fields are present.
   */
  public void requiredFieldsSatisfied() {
    final RequiredFieldFilter filter = new RequiredFieldFilter("Foo");

    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add("Foo", "1");
    msg.add("Bar", 2.0);
    msg.add("Baz", 500);

    final MutableFudgeMsg normalized = filter.apply(msg, "123", new FieldHistoryStore());
    assertNotNull(normalized);
    assertSame(normalized, msg);
  }

}
