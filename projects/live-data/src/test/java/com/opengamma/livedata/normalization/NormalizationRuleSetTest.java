/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;

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
public class NormalizationRuleSetTest {

  /**
   * First filter will remove the message entirely.
   * Testing to make sure that the break condition happens, and that
   * no NPEs happen.
   */
  public void filterRemovingMessageEntirely() {
    final NormalizationRuleSet ruleSet = new NormalizationRuleSet(
        "Testing",
        new RequiredFieldFilter("Foo"),
        new FieldFilter("Bar"));

    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add("Bar", 2.0);
    msg.add("Baz", 500);

    final FudgeMsg normalizedMsg = ruleSet.getNormalizedMessage(msg, "123", new FieldHistoryStore());
    assertNull(normalizedMsg);
  }

  /**
   * Tests that the topic suffix is created from the separator and topic suffix.
   */
  public void testTopicSuffix() {
    final NormalizationRuleSet ruleSet = new NormalizationRuleSet("Test", new RequiredFieldFilter("x"), new FieldFilter("y"));
    assertEquals(ruleSet.getJmsTopicSuffix(), ".Test");
  }

  /**
   * Tests the topic suffix when the input is empty.
   */
  public void testEmptyTopicSuffix() {
    final NormalizationRuleSet ruleSet = new NormalizationRuleSet("", new RequiredFieldFilter("x"), new FieldFilter("y"));
    assertEquals(ruleSet.getJmsTopicSuffix(), "");
  }

  /**
   * Tests the topic suffix when the input already starts with the separator.
   */
  public void testTopicSuffixWithSeparator() {
    final NormalizationRuleSet ruleSet = new NormalizationRuleSet(".Test", new RequiredFieldFilter("x"), new FieldFilter("y"));
    assertEquals(ruleSet.getJmsTopicSuffix(), ".Test");
  }

  /**
   * Tests the object.
   */
  public void testObject() {
    final NormalizationRuleSet ruleSet = new NormalizationRuleSet("Test", new RequiredFieldFilter("x"), new FieldFilter("y"));
    assertEquals(ruleSet.getJmsTopicSuffix(), ".Test");
    assertEquals(ruleSet.getId(), "Test");
    assertEquals(ruleSet, ruleSet);
    assertNotEquals(".Test", ruleSet);
    assertNotEquals(null, ruleSet);
    NormalizationRuleSet other = new NormalizationRuleSet("Test", new RequiredFieldFilter("x"), new FieldFilter("y"));
    assertEquals(ruleSet, other);
    assertEquals(ruleSet.hashCode(), other.hashCode());
    other = new NormalizationRuleSet("Other", new RequiredFieldFilter("x"), new FieldFilter("y"));
    assertNotEquals(ruleSet, other);
    other = new NormalizationRuleSet("Other", new RequiredFieldFilter("y"), new FieldFilter("y"));
    assertNotEquals(ruleSet, other);
    other = new NormalizationRuleSet("Other", new RequiredFieldFilter("x"), new FieldFilter("x"));
    assertNotEquals(ruleSet, other);
  }
}
