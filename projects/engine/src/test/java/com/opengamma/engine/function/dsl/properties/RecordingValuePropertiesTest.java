/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.engine.function.dsl.properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.annotations.Test;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link RecordingValueProperties}.
 */
@Test(groups = TestGroup.UNIT)
public class RecordingValuePropertiesTest {
  private static final String REQ_NAME = "reqName";
  private static final String PROP_NAME_1 = "propName1";
  private static final String PROP_NAME_2 = "propName2";
  private static final String PROP_NAME_3 = "propName3";
  private static final String[] PROP_VALUES_1 = new String[] { "propValue1" };
  private static final String[] PROP_VALUES_2 = new String[] { "propValue2", "propValue3" };

  private static RecordingValueProperties createPopulated() {
    return RecordingValueProperties.copyFrom(REQ_NAME).with(PROP_NAME_1, PROP_VALUES_1).with(PROP_NAME_2, PROP_VALUES_2).withOptional(PROP_NAME_2)
        .withAny(PROP_NAME_3);
  }

  private static RecordingValueProperties createEmpty() {
    return RecordingValueProperties.copyFrom(REQ_NAME);
  }

  /**
   * Tests that the requirement name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullReqName() {
    RecordingValueProperties.copyFrom(null);
  }

  /**
   * Tests that the property cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRemoveProperty() {
    RecordingValueProperties.desiredValue().withoutAny(null);
  }

  /**
   * Tests that the property cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAddProperty() {
    RecordingValueProperties.desiredValue().with(null);
  }

  /**
   * Tests that the property value cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAddPropertyValue() {
    RecordingValueProperties.desiredValue().with(PROP_NAME_1, (String[]) null);
  }

  /**
   * Tests that the property cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullReplaceProperty() {
    RecordingValueProperties.desiredValue().withReplacement(null);
  }

  /**
   * Tests that the property value cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullReplacePropertyValue() {
    RecordingValueProperties.desiredValue().withReplacement(PROP_NAME_1, (String[]) null);
  }

  /**
   * Tests that the property cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAddAnyProperty() {
    RecordingValueProperties.desiredValue().withAny(null);
  }

  /**
   * Tests that the property cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAddOptionalProperty() {
    RecordingValueProperties.desiredValue().withOptional(null);
  }

  /**
   * Tests empty properties.
   */
  @Test
  public void testEmptyProperties() {
    final RecordingValueProperties empty = RecordingValueProperties.desiredValue();
    assertNull(empty.getCopiedFrom());
    assertTrue(empty.getRecordedValueProperties().collect(Collectors.toList()).isEmpty());
  }

  /**
   * Tests removing a property.
   */
  @Test
  public void testRemoveProperty() {
    RecordingValueProperties properties = createPopulated();
    ValueProperties expected = ValueProperties.builder()
        .with(PROP_NAME_1, Arrays.asList(PROP_VALUES_1))
        .with(PROP_NAME_2, Arrays.asList(PROP_VALUES_2))
        .withOptional(PROP_NAME_2)
        .withAny(PROP_NAME_3)
        .get();
    assertProperties(expected, properties);
    properties = createPopulated();
    properties.withoutAny(PROP_NAME_3);
    expected = ValueProperties.builder()
        .with(PROP_NAME_1, Arrays.asList(PROP_VALUES_1))
        .with(PROP_NAME_2, Arrays.asList(PROP_VALUES_2))
        .withOptional(PROP_NAME_2)
        .get();
    assertProperties(expected, properties);
    properties = createPopulated();
    properties.withoutAny(PROP_NAME_2);
    properties.withoutAny(PROP_NAME_3);
    expected = ValueProperties.builder()
        .with(PROP_NAME_1, Arrays.asList(PROP_VALUES_1))
        .get();
    assertProperties(expected, properties);
    properties = createPopulated();
    properties.withoutAny(PROP_NAME_1);
    properties.withoutAny(PROP_NAME_2);
    properties.withoutAny(PROP_NAME_3);
    expected = ValueProperties.builder()
        .get();
    assertProperties(expected, properties);
  }

  /**
   * Tests adding a property.
   */
  @Test
  public void testAddProperty() {
    RecordingValueProperties properties = createEmpty();
    ValueProperties expected = ValueProperties.builder()
        .get();
    assertProperties(expected, properties);
    properties = createEmpty();
    properties.with(PROP_NAME_1, PROP_VALUES_1);
    expected = ValueProperties.builder()
        .with(PROP_NAME_1, Arrays.asList(PROP_VALUES_1))
        .get();
    assertProperties(expected, properties);
    properties = createEmpty();
    properties.with(PROP_NAME_1, PROP_VALUES_1);
    properties.with(PROP_NAME_2, PROP_VALUES_2);
    expected = ValueProperties.builder()
        .with(PROP_NAME_1, Arrays.asList(PROP_VALUES_1))
        .with(PROP_NAME_2, Arrays.asList(PROP_VALUES_2))
        .get();
    assertProperties(expected, properties);
  }

  /**
   * Tests replacing a property.
   */
  @Test
  public void testReplaceProperty() {
    RecordingValueProperties properties = createPopulated();
    ValueProperties expected = ValueProperties.builder()
        .with(PROP_NAME_1, Arrays.asList(PROP_VALUES_1))
        .with(PROP_NAME_2, Arrays.asList(PROP_VALUES_2))
        .withOptional(PROP_NAME_2)
        .withAny(PROP_NAME_3)
        .get();
    assertProperties(expected, properties);
    properties = createPopulated();
    properties.withReplacement(PROP_NAME_1, PROP_VALUES_2);
    expected = ValueProperties.builder()
        .with(PROP_NAME_1, Arrays.asList(PROP_VALUES_2))
        .with(PROP_NAME_2, Arrays.asList(PROP_VALUES_2))
        .withOptional(PROP_NAME_2)
        .withAny(PROP_NAME_3)
        .get();
    assertProperties(expected, properties);
  }

  /**
   * Tests adding a property with no value.
   */
  @Test
  public void testAddAnyProperty() {
    RecordingValueProperties properties = createEmpty();
    properties.withAny(PROP_NAME_1);
    ValueProperties expected = ValueProperties.builder()
        .withAny(PROP_NAME_1)
        .get();
    assertProperties(expected, properties);
    properties = RecordingValueProperties.copyFrom(REQ_NAME).with(PROP_NAME_1, PROP_VALUES_1);
    properties.withAny(PROP_NAME_1);
    expected = ValueProperties.builder()
        .withAny(PROP_NAME_1)
        .get();
    assertProperties(expected, properties);
  }

  /**
   * Tests replacing an optional property.
   */
  @Test
  public void testAddOptionalProperty() {
    RecordingValueProperties properties = RecordingValueProperties.copyFrom(REQ_NAME).with(PROP_NAME_1, PROP_VALUES_1).withOptional(PROP_NAME_1);
    ValueProperties expected = ValueProperties.builder()
        .with(PROP_NAME_1, Arrays.asList(PROP_VALUES_1))
        .withOptional(PROP_NAME_1)
        .get();
    assertProperties(expected, properties);
    properties = RecordingValueProperties.copyFrom(REQ_NAME).with(PROP_NAME_1, PROP_VALUES_1).withOptional(PROP_NAME_1).withAny(PROP_NAME_2)
        .withOptional(PROP_NAME_2);
    expected = ValueProperties.builder()
        .with(PROP_NAME_1, Arrays.asList(PROP_VALUES_1))
        .withOptional(PROP_NAME_1)
        .withAny(PROP_NAME_2)
        .withOptional(PROP_NAME_2)
        .get();
    assertProperties(expected, properties);
  }

  private static void assertProperties(final ValueProperties expectedInitial, final RecordingValueProperties properties) {
    final ValueProperties.Builder builder = ValueProperties.builder();
    final List<ValuePropertiesModifier> stream = properties.getRecordedValueProperties().collect(Collectors.toList());
    for (final ValuePropertiesModifier vpm : stream) {
      vpm.modify(builder);
    }
    assertEquals(expectedInitial, builder.get());
  }
}
