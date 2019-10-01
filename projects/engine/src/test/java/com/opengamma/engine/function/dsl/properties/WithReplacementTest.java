/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.engine.function.dsl.properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link WithReplacement}.
 */
@Test(groups = TestGroup.UNIT)
public class WithReplacementTest {
  private static final String PROPERTY_NAME = "name";
  private static final String[] PROPERTY_VALUES = { "value1", "value2", "value3" };

  /**
   * Tests that the name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName() {
    new WithReplacement(null, PROPERTY_VALUES);
  }

  /**
   * Tests that the values can be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullValues() {
    new WithReplacement(PROPERTY_NAME, null);
  }

  /**
   * Tests that the values can be empty.
   */
  @Test
  public void testEmptyValues() {
    new WithReplacement(PROPERTY_NAME, new String[0]);
  }

  /**
   * Tests that the builder cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBuilder() {
    new WithReplacement(PROPERTY_NAME, PROPERTY_VALUES).modify(null);
  }

  /**
   * Tests the modifier.
   */
  @Test
  public void testModifyEmptyBuilder() {
    final ValueProperties.Builder builder = ValueProperties.builder();
    assertNull(builder.get().getProperties());
    new WithReplacement(PROPERTY_NAME, PROPERTY_VALUES).modify(builder);
    assertEquals(builder.get().getProperties().size(), 1);
    assertValuesEqual(builder.get().getProperties(), new String[] { PROPERTY_NAME });
    assertEquals(builder.get().getProperties().size(), 1);
    assertValuesEqual(builder.get().getValues(PROPERTY_NAME), PROPERTY_VALUES);
  }

  /**
   * Tests the modifier.
   */
  @Test
  public void testModifyWithNewProperty() {
    final ValueProperties.Builder builder = ValueProperties.builder().with("other", Collections.singleton("othervalue"));
    assertEquals(builder.get().getProperties().size(), 1);
    new WithReplacement(PROPERTY_NAME, PROPERTY_VALUES).modify(builder);
    assertEquals(builder.get().getProperties().size(), 2);
    assertValuesEqual(builder.get().getProperties(), new String[] { PROPERTY_NAME, "other" });
    assertEquals(builder.get().getProperties().size(), 2);
    assertEquals(builder.get().getValues(PROPERTY_NAME).toArray(new String[0]), PROPERTY_VALUES);
    assertValuesEqual(builder.get().getValues("other"), new String[] { "othervalue" });
  }

  /**
   * Tests the modifier.
   */
  @Test
  public void testModifyWithExistingProperty() {
    final ValueProperties.Builder builder = ValueProperties.builder().with(PROPERTY_NAME, Collections.singleton("othervalue"));
    assertEquals(builder.get().getProperties().size(), 1);
    new WithReplacement(PROPERTY_NAME, PROPERTY_VALUES).modify(builder);
    assertEquals(builder.get().getProperties().size(), 1);
    assertValuesEqual(builder.get().getValues(PROPERTY_NAME), PROPERTY_VALUES);
  }

  /**
   * Tests the modifier.
   */
  @Test
  public void testModifyWithOptionalProperty() {
    final ValueProperties.Builder builder = ValueProperties.builder().withOptional(PROPERTY_NAME);
    assertEquals(builder.get().getProperties().size(), 1);
    new WithReplacement(PROPERTY_NAME, PROPERTY_VALUES).modify(builder);
    assertEquals(builder.get().getProperties().size(), 1);
    assertValuesEqual(builder.get().getProperties(), new String[] { PROPERTY_NAME });
    assertFalse(builder.get().isOptional(PROPERTY_NAME));
    assertValuesEqual(builder.get().getValues(PROPERTY_NAME), PROPERTY_VALUES);
  }

  private static void assertValuesEqual(final Set<String> actual, final String[] expected) {
    assertEquals(actual.size(), expected.length);
    for (final String e : expected) {
      assertTrue(actual.contains(e));
    }
  }
}
