/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import static org.testng.AssertJUnit.assertEquals;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class StandardRulesTest {


  /**
   * Tests the no normalization rules.
   */
  @Test
  public void noNormalization() {
    final NormalizationRuleSet ruleSet = StandardRules.getNoNormalization();

    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add("Foo", "1");
    msg.add("Bar", 2.0);
    msg.add("Baz", 500);

    final FieldHistoryStore store = new FieldHistoryStore();
    final FudgeMsg normalizedMsg = ruleSet.getNormalizedMessage(msg, "123", store);
    assertEquals(msg, normalizedMsg);
  }

}
