/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.results;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.MissingInputsFunction;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link ResultKey}.
 */
@Test(groups = TestGroup.UNIT)
public class ResultKeyTest {

  /**
   * Tests the behaviour when the column set name is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullColumnSetName() {
    ResultKey.of(null, ResultType.FX_PRESENT_VALUE);
  }

  /**
   * Tests the behaviour when the result type is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullResultType1() {
    ResultKey.of(null);
  }

  /**
   * Tests the behaviour when the result type is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullResultType2() {
    ResultKey.of(null, ResultType.FX_PRESENT_VALUE);
  }

  /**
   * Test getters.
   */
  @Test
  public void testGetters() {
    final String columnSetName = "Test";
    final ResultKey key = ResultKey.of(columnSetName, ResultType.FX_PRESENT_VALUE);
    assertEquals(key.getColumnSet(), columnSetName);
    assertEquals(key.getResultType(), ResultType.FX_PRESENT_VALUE);
    assertFalse(key.isDefaultColumnSet());
    assertTrue(ResultKey.of(ResultType.FX_PRESENT_VALUE).isDefaultColumnSet());
  }

  /**
   * Tests the hash code.
   */
  @Test
  public void testHashCode() {
    final ResultKey key = ResultKey.of("Test", ResultType.FX_PRESENT_VALUE);
    assertEquals(key.hashCode(), ResultKey.of("Test", ResultType.FX_PRESENT_VALUE).hashCode());
    assertNotEquals(key.hashCode(), ResultKey.of(ResultType.FX_PRESENT_VALUE).hashCode());
    assertNotEquals(key.hashCode(), ResultKey.of("Test", ResultType.BUCKETED_PV01).hashCode());
  }

  /**
   * Tests the equals method.
   */
  @Test
  public void testEquals() {
    final ResultKey key = ResultKey.of("Test", ResultType.FX_PRESENT_VALUE);
    assertEquals(key, key);
    assertNotEquals(null, key);
    assertNotEquals(new Object(), key);
    assertEquals(key, ResultKey.of("Test", ResultType.FX_PRESENT_VALUE));
    // column set names don't match
    assertNotEquals(key, ResultKey.of(ResultType.FX_PRESENT_VALUE));
    // value requirement names don't match
    assertNotEquals(key, ResultKey.of("Test", ResultType.BUCKETED_PV01));
    final ValueProperties missingInputProperty = ValueProperties.builder()
        .with(ValuePropertyNames.AGGREGATION, MissingInputsFunction.AGGREGATION_STYLE_MISSING).get();
    final ValueProperties anyCurrencyProperty = ValueProperties.builder().withAny(ValuePropertyNames.CURRENCY).get();
    final ValueProperties optionalProperties = ValueProperties.builder()
        .with(ValuePropertyNames.AGGREGATION, MissingInputsFunction.AGGREGATION_STYLE_MISSING).withOptional(ValuePropertyNames.AGGREGATION)
        .with(ValuePropertyNames.CURRENCY, Currency.USD.getCode()).get();
    final ResultKey keyWithMissingInputProperty = ResultKey.of("Test", ResultType.builder()
        .valueRequirementName(ValueRequirementNames.FX_PRESENT_VALUE)
        .properties(missingInputProperty)
        .build());
    final ResultKey keyWithCurrencyProperty = ResultKey.of("Test", ResultType.builder()
        .valueRequirementName(ValueRequirementNames.FX_PRESENT_VALUE)
        .properties(anyCurrencyProperty)
        .build());
    final ResultKey keyWithOptionalProperties = ResultKey.of("Test", ResultType.builder()
        .valueRequirementName(ValueRequirementNames.FX_PRESENT_VALUE)
        .properties(optionalProperties)
        .build());
    // loose matching means key without properties will always satisfy properties of other key
    assertEquals(key, keyWithMissingInputProperty);
    assertEquals(keyWithMissingInputProperty, key);
    assertEquals(key, keyWithCurrencyProperty);
    assertEquals(keyWithCurrencyProperty, key);
    assertEquals(key, keyWithOptionalProperties);
    assertEquals(keyWithOptionalProperties, key);
    // missing input and currency properties cannot match
    assertNotEquals(keyWithMissingInputProperty, keyWithCurrencyProperty);
    assertNotEquals(keyWithCurrencyProperty, keyWithMissingInputProperty);
    // optional match on missing inputs
    assertEquals(keyWithMissingInputProperty, keyWithOptionalProperties);
    // any currency property matches
    assertEquals(keyWithCurrencyProperty, keyWithOptionalProperties);
  }

  /**
   * Tests the toString method.
   */
  @Test
  public void testToString() {
    assertEquals(ResultKey.of("Test", ResultType.FX_PRESENT_VALUE).toString(), 
        "ResultKey[columnSet=Test, resultType=ResultType{valueRequirementName=FX Present Value, properties=EMPTY}]");
  }
}
