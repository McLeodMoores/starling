/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.results;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link ResultType}.
 */
@Test(groups = TestGroup.UNIT)
public class ResultTypeTest {

  /**
   * Tests the behaviour when the value requirement name is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullValueRequirementName() {
    ResultType.builder().valueRequirementName(null);
  }

  /**
   * Tests the equals method. In this type of result, the properties must match exactly.
   */
  @Test
  public void testEquals() {
    final ResultType referenceType = ResultType.builder().valueRequirementName(ValueRequirementNames.PRESENT_VALUE).build();
    assertEquals(referenceType, referenceType);
    assertNotEquals(null, referenceType);
    assertNotEquals(new Object(), referenceType);
    assertEquals(referenceType, ResultType.builder().valueRequirementName(ValueRequirementNames.PRESENT_VALUE).build());
    // empty properties supplied
    assertEquals(ResultType.builder().valueRequirementName(ValueRequirementNames.PRESENT_VALUE).properties(ValueProperties.none()).build(), referenceType);
    // value requirement names don't match
    assertNotEquals(ResultType.builder().valueRequirementName(ValueRequirementNames.FX_PRESENT_VALUE).build(), referenceType);
    // withAny() properties don't match
    assertNotEquals(ResultType.builder().valueRequirementName(ValueRequirementNames.PRESENT_VALUE).properties(ValueProperties.withAny(ValuePropertyNames.CURRENCY).get()).build(), referenceType);
    // with() properties don't match
    assertNotEquals(ResultType.builder().valueRequirementName(ValueRequirementNames.PRESENT_VALUE).properties(ValueProperties.with(ValuePropertyNames.CURRENCY, "USD").get()).build(), referenceType);
    //TODO should this be equals? it is according to the way properties are used throughout the rest of the system
    assertNotEquals(ResultType.builder().valueRequirementName(ValueRequirementNames.PRESENT_VALUE).properties(ValueProperties.with(ValuePropertyNames.CURRENCY, "USD").withOptional(ValuePropertyNames.CURRENCY).get()).build(), referenceType);
  }
}
