/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.livedata.normalization;

import static org.testng.Assert.assertEquals;

import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link FieldHistoryUpdater}.
 */
@Test(groups = TestGroup.UNIT)
public class FieldHistoryUpdaterTest {
  private static final MutableFudgeMsg MSG_1 = OpenGammaFudgeContext.getInstance().newMessage();
  private static final MutableFudgeMsg MSG_2 = OpenGammaFudgeContext.getInstance().newMessage();
  static {
    MSG_1.add("security", "abc");
    MSG_1.add("dividend", 0.02);
    MSG_1.add("price", 100);
    MSG_2.add("security", "abc");
    MSG_2.add("price", 99);
  }

  /**
   * Tests that the fields are updated.
   */
  public void testUpdate() {
    final FieldHistoryStore store = new FieldHistoryStore(MSG_1);
    MutableFudgeMsg updated = new FieldHistoryUpdater().apply(MSG_2, "uid", store);
    assertEquals(updated.getAllFields().size(), 2);
    assertEquals(updated.getByName("security").getValue(), "abc");
    assertEquals(((Number) updated.getByName("price").getValue()).doubleValue(), 99.);
    updated = new FieldHistoryUpdater().apply(MSG_1, "uid", store);
    assertEquals(updated.getAllFields().size(), 3);
    assertEquals(updated.getByName("security").getValue(), "abc");
    assertEquals(((Number) updated.getByName("dividend").getValue()).doubleValue(), 0.02);
    assertEquals(((Number) updated.getByName("price").getValue()).doubleValue(), 100.);
  }
}
