/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.engine.function.dsl;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.dsl.properties.RecordingValueProperties;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link FunctionGate}.
 */
@Test(groups = TestGroup.UNIT)
public class FunctionGateTest {
  private static final String NAME = "name";

  /**
   * Tests that the name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName() {
    new FunctionGate<>(null);
  }

  /**
   * Tests that the properties cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullProperties1() {
    new FunctionInput(NAME).properties((ValueProperties.Builder) null);
  }

  /**
   * Tests that the properties cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullProperties2() {
    new FunctionInput(NAME).properties((ValueProperties) null);
  }

  /**
   * Tests that the properties cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullProperties3() {
    new FunctionInput(NAME).properties((RecordingValueProperties) null);
  }

  /**
   * Tests that the unique id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUniqueId() {
    new FunctionInput(NAME).targetSpec(ComputationTargetType.CURRENCY, null);
  }

  /**
   * Tests that the computation target specification cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullComputationTargetSpecification() {
    new FunctionInput(NAME).targetSpec((ComputationTargetSpecification) null);
  }

  /**
   * Tests that the target specification reference cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTargetSpecificationReference() {
    new FunctionInput(NAME).targetSpec((TargetSpecificationReference) null);
  }

  /**
   * Tests setting and getting value properties.
   */
  @Test
  public void testValueProperties() {
    final ValueProperties.Builder builder = ValueProperties.builder()
        .with("a", "z")
        .with("b", "y").withOptional("b")
        .withAny("c");
    final ValueProperties properties = builder.get();
    assertEquals(new FunctionInput(NAME).properties(builder).getValueProperties(), properties);
    assertEquals(new FunctionInput(NAME).properties(properties).getValueProperties(), properties);
  }

  /**
   * Tests setting and getting value properties.
   */
  @Test
  public void testRecordingValueProperties() {
    final RecordingValueProperties recordingProperties = RecordingValueProperties.desiredValue()
        .with("a", "z")
        .with("b", "y")
        .withOptional("b")
        .withAny("c");
    assertEquals(new FunctionInput(NAME).properties(recordingProperties).getRecordingValueProperties(), recordingProperties);
  }

  /**
   * Tests setting the computation target specification.
   */
  @Test
  public void testComputationTargetSpecification() {
    final UniqueId uid = UniqueId.of("uid", "x");
    final ComputationTargetSpecification cts = new ComputationTargetSpecification(ComputationTargetType.CURRENCY, uid);
    assertEquals(new FunctionInput(NAME).targetSpec(ComputationTargetType.CURRENCY, uid).getComputationTargetSpecification(), cts);
    assertEquals(new FunctionInput(NAME).targetSpec(cts).getComputationTargetSpecification(), cts);
  }

  /**
   * Tests setting the target specification reference.
   */
  @Test
  public void testTargetSpecificationReference() {
    assertEquals(new FunctionInput(NAME).targetSpec(TargetSpecificationReference.originalTarget()).getTargetSpecificationReference().getClass(),
        TargetSpecificationReference.class);
  }
}
