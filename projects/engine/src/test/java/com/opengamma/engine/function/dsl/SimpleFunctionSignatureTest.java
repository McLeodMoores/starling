/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.engine.function.dsl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.annotations.Test;

import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link SimpleFunctionSignature}.
 */
@Test(groups = TestGroup.UNIT)
public class SimpleFunctionSignatureTest {
  private static final String NAME = "FNAME";
  private static final ComputationTargetType TYPE = ComputationTargetType.CURRENCY;
  private static final Class<?> CLASS = Currency.class;
  private static final FunctionInput[] INPUTS = new FunctionInput[] {
      new FunctionInput("i1"), new FunctionInput("i2"), new FunctionInput("i3")
  };
  private static final FunctionOutput[] OUTPUTS = new FunctionOutput[] {
      new FunctionOutput("o1"), new FunctionOutput("o2")
  };
  private static final FunctionInput NEW_INPUT_1 = new FunctionInput("i4");
  private static final FunctionInput NEW_INPUT_2 = new FunctionInput("i5");
  private static final FunctionOutput NEW_OUTPUT = new FunctionOutput("o3");

  /**
   * Tests the initialization of the class.
   */
  @Test
  public void testInitialization() {
    final SimpleFunctionSignature sfs = new SimpleFunctionSignature(NAME, TYPE);
    assertNull(sfs.getComputationTargetClass());
    assertEquals(sfs.getComputationTargetType(), TYPE);
    assertTrue(sfs.getInputs().stream().collect(Collectors.toList()).isEmpty());
    assertEquals(sfs.getName(), NAME);
    assertTrue(sfs.getOutputs().stream().collect(Collectors.toList()).isEmpty());
  }

  /**
   * Tests that adding a target class is a builder-type operation.
   */
  @Test
  public void testAddTargetClass() {
    final FunctionSignature sfs1 = new SimpleFunctionSignature(NAME, TYPE);
    assertNull(sfs1.getComputationTargetClass());
    final FunctionSignature sfs2 = sfs1.targetClass(CLASS);
    assertEquals(sfs1.getComputationTargetClass(), CLASS);
    assertEquals(sfs2.getComputationTargetClass(), CLASS);
    assertSame(sfs1, sfs2);
  }

  /**
   * Tests adding multiple inputs.
   */
  @Test
  public void testAddMultipleInputs() {
    final FunctionSignature sfs1 = new SimpleFunctionSignature(NAME, TYPE);
    final FunctionSignature sfs2 = sfs1.inputs(INPUTS);
    assertSame(sfs1, sfs2);
    assertEquals(sfs2.getInputs().stream().collect(Collectors.toList()), Arrays.asList(INPUTS));
  }

  /**
   * Tests adding multiple outputs.
   */
  @Test
  public void testAddMultipleOutputs() {
    final FunctionSignature sfs1 = new SimpleFunctionSignature(NAME, TYPE);
    final FunctionSignature sfs2 = sfs1.outputs(OUTPUTS);
    assertSame(sfs1, sfs2);
    assertEquals(sfs2.getOutputs().stream().collect(Collectors.toList()), Arrays.asList(OUTPUTS));
  }

  /**
   * Tests adding a new input.
   */
  @Test
  public void testAddNewInput() {
    final FunctionSignature sfs1 = new SimpleFunctionSignature(NAME, TYPE);
    sfs1.inputs(INPUTS);
    final FunctionSignature sfs2 = sfs1.addInput(NEW_INPUT_1);
    assertNotSame(sfs1, sfs2);
    final FunctionSignature sfs3 = sfs2.addInput(NEW_INPUT_2);
    assertNotSame(sfs2, sfs3);
    final List<FunctionInput> expected = new ArrayList<>(Arrays.asList(INPUTS));
    expected.add(0, NEW_INPUT_1);
    expected.add(0, NEW_INPUT_2);
    assertEquals(sfs3.getInputs().stream().collect(Collectors.toList()), expected);
  }

  /**
   * Tests adding a new output.
   */
  @Test
  public void testAddNewOutput() {
    final FunctionSignature sfs1 = new SimpleFunctionSignature(NAME, TYPE);
    sfs1.outputs(OUTPUTS);
    final FunctionSignature sfs2 = sfs1.addOutput(NEW_OUTPUT);
    assertNotSame(sfs1, sfs2);
    final List<FunctionOutput> expected = new ArrayList<>(Arrays.asList(OUTPUTS));
    expected.add(0, NEW_OUTPUT);
    assertEquals(sfs2.getOutputs().stream().collect(Collectors.toList()), expected);
  }
}
