/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.engine.function.dsl.properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link WithoutAny}.
 */
@Test(groups = TestGroup.UNIT)
public class WithoutAnyTest {
  private static final String PROPERTY_NAME = "name";
  private static final String[] PROPERTY_VALUES = { "value1", "value2", "value3" };

  /**
   * Tests that the name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName() {
    new WithoutAny(null);
  }

  /**
   * Tests that the builder cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBuilder() {
    new WithoutAny(PROPERTY_NAME).modify(null);
  }

  /**
   * Tests the modifier.
   */
  @Test
  public void testModifyEmptyBuilder() {
    final ValueProperties.Builder builder = ValueProperties.builder();
    assertNull(builder.get().getProperties());
    new WithoutAny(PROPERTY_NAME).modify(builder);
    assertEmpty(builder.get());
    assertNull(builder.get().getProperties());
  }

  /**
   * @param valueProperties
   */
  private void assertEmpty(final ValueProperties valueProperties) {
  }

  /**
   * Tests the modifier.
   */
  @Test
  public void testModifyWithNewProperty() {
    final ValueProperties.Builder builder = ValueProperties.builder().with("other", Collections.singleton("othervalue"));
    assertEquals(builder.get().getProperties().size(), 1);
    new WithoutAny(PROPERTY_NAME).modify(builder);
    assertEquals(builder.get().getProperties().size(), 1);
    assertValuesEqual(builder.get().getProperties(), new String[] { "other" });
    assertEquals(builder.get().getProperties().size(), 1);
    assertNull(builder.get().getValues(PROPERTY_NAME));
  }

  /**
   * Tests the modifier.
   */
  @Test
  public void testModifyWithExistingProperty() {
    final ValueProperties.Builder builder = ValueProperties.builder().with(PROPERTY_NAME, Collections.singleton("othervalue"));
    assertEquals(builder.get().getProperties().size(), 1);
    new WithoutAny(PROPERTY_NAME).modify(builder);
    assertEmpty(builder.get());
    final String[] expectedValues = new String[PROPERTY_VALUES.length + 1];
    System.arraycopy(PROPERTY_VALUES, 0, expectedValues, 0, PROPERTY_VALUES.length);
    expectedValues[PROPERTY_VALUES.length] = "othervalue";
    assertEmpty(builder.get());
  }

  /**
   * Tests the modifier.
   */
  @Test
  public void testModifyWithOptionalProperty() {
    final ValueProperties.Builder builder = ValueProperties.builder().withOptional(PROPERTY_NAME);
    assertEquals(builder.get().getProperties().size(), 1);
    new WithoutAny(PROPERTY_NAME).modify(builder);
    assertEmpty(builder.get());
  }

  private static void assertValuesEqual(final Set<String> actual, final String[] expected) {
    assertEquals(actual.size(), expected.length);
    for (final String e : expected) {
      assertTrue(actual.contains(e));
    }
  }
}
