/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.dsl;

import static com.opengamma.engine.function.dsl.Function.function;
import static com.opengamma.engine.function.dsl.Function.input;
import static com.opengamma.engine.function.dsl.Function.output;
import static com.opengamma.engine.function.dsl.TargetSpecificationReference.originalTarget;
import static com.opengamma.engine.function.dsl.properties.RecordingValueProperties.copyFrom;
import static com.opengamma.engine.function.dsl.properties.RecordingValueProperties.desiredValue;
import static com.opengamma.engine.value.ValueRequirementNames.DV01;
import static com.opengamma.engine.value.ValueRequirementNames.PV01;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.dsl.functions.BaseNonCompiledInvoker;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BaseNonCompiledInvokerTest {

  public void getResultsTest1() {
    final FunctionCompilationContext fcctx = mock(FunctionCompilationContext.class);
    final DV01TestFun dv01 = new DV01TestFun();

    final SimplePosition position = new SimplePosition();
    position.setUniqueId(UniqueId.of("uid", "456"));
    assertTrue(dv01.canApplyTo(fcctx, new ComputationTarget(ComputationTargetType.POSITION, position)));

    final ComputationTargetSpecification cts = new ComputationTargetSpecification(ComputationTargetType.POSITION, UniqueId.of("a", "b"));
    final ComputationTarget ct = mock(ComputationTarget.class);
    when(ct.toSpecification()).thenReturn(cts);
    when(ct.getType()).thenReturn(ComputationTargetType.POSITION);
    when(ct.getUniqueId()).thenReturn(position.getUniqueId());

    final Set<ValueSpecification> specs = dv01.getResults(null, ct);
    assertEquals(specs.size(), 1);

    final ValueSpecification spec = specs.iterator().next();
    assertEquals(spec.getProperties(), ValueProperties.all());
    assertEquals(spec.getTargetSpecification(), cts);
    assertEquals(spec.getValueName(), "DV01");
  }

  public void getRequirements() {
    final FunctionCompilationContext fcctx = mock(FunctionCompilationContext.class);
    final DV01TestFun dv01 = new DV01TestFun();
    final SimplePosition position = new SimplePosition();
    position.setUniqueId(UniqueId.of("uid", "123"));
    assertTrue(dv01.canApplyTo(fcctx, new ComputationTarget(ComputationTargetType.POSITION, position)));

    final ComputationTargetSpecification cts = new ComputationTargetSpecification(ComputationTargetType.POSITION, UniqueId.of("a", "b"));
    final ComputationTarget ct = mock(ComputationTarget.class);
    when(ct.toSpecification()).thenReturn(cts);
    when(ct.getType()).thenReturn(ComputationTargetType.POSITION);
    when(ct.getUniqueId()).thenReturn(position.getUniqueId());

    final ValueProperties valueProperties = ValueProperties.builder().with("A", "1").with("B", "1").with(ValuePropertyNames.FUNCTION, "PV01_Function").get();
    final ValueRequirement desiredValue = new ValueRequirement("PV01", ct.getType(), ct.getUniqueId(), valueProperties);
    final Set<ValueRequirement> requirements = dv01.getRequirements(null, ct, desiredValue);
    assertEquals(requirements.size(), 1);

    final ValueRequirement requirement = requirements.iterator().next();
    assertEquals(requirement.getConstraints(), requirement.getConstraints());
    assertEquals(requirement.getTargetReference().getSpecification(), cts);
    assertEquals(requirement.getValueName(), "PV01");
  }

  public void getResultsTest2() {
    final FunctionCompilationContext fcctx = mock(FunctionCompilationContext.class);
    final DV01TestFun dv01 = new DV01TestFun();

    final SimplePosition position = new SimplePosition();
    position.setUniqueId(UniqueId.of("uid", "789"));
    assertTrue(dv01.canApplyTo(fcctx, new ComputationTarget(ComputationTargetType.POSITION, position)));

    final ComputationTargetSpecification cts = new ComputationTargetSpecification(ComputationTargetType.POSITION, UniqueId.of("a", "b"));
    final ComputationTarget ct = mock(ComputationTarget.class);
    when(ct.toSpecification()).thenReturn(cts);
    when(ct.getType()).thenReturn(ComputationTargetType.POSITION);
    when(ct.getUniqueId()).thenReturn(position.getUniqueId());

    final ValueProperties valueProperties = ValueProperties.builder().with("A", "1").with("B", "1").with(ValuePropertyNames.FUNCTION, "PV01_Function").get();
    final ValueRequirement desiredValue = new ValueRequirement("PV01", ct.getType(), ct.getUniqueId(), valueProperties);
    final ValueSpecification specifiedValue = new ValueSpecification(desiredValue.getValueName(),
        desiredValue.getTargetReference().getSpecification(), desiredValue.getConstraints().copy().with("X", "3").get());
    final Map<ValueSpecification, ValueRequirement> inputSpecificationsMap = new HashMap<>();
    inputSpecificationsMap.put(specifiedValue, desiredValue);
    final Set<ValueSpecification> specs = dv01.getResults(null, ct, inputSpecificationsMap);
    assertEquals(specs.size(), 1);

    final ValueSpecification spec = specs.iterator().next();
    assertEquals(spec.getProperties().getValues(ValuePropertyNames.FUNCTION), Collections.singleton(dv01.getUniqueId()));
    assertEquals(spec.getTargetSpecification(), cts);
    assertEquals(spec.getValueName(), "DV01");
  }

  class DV01TestFun extends BaseNonCompiledInvoker {
    @Override
    protected FunctionSignature functionSignature() {
      return function("DV01Function", ComputationTargetType.POSITION)
          .outputs(
              output(DV01)
                  .targetSpec(originalTarget()) // takes ComputationTargetSpecification or TargetSpecificationReference
                  .properties(copyFrom(PV01)
                      .withReplacement(ValuePropertyNames.FUNCTION, getUniqueId())
                      .withAny(ValuePropertyNames.SHIFT)))
          .inputs(
              input(PV01)
                  .targetSpec(originalTarget())
                  .properties(desiredValue()
                      .withoutAny(ValuePropertyNames.SHIFT)));
    }

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
        final ComputationTarget target, final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
      return null;
    }

    @Override
    public String getUniqueId() {
      return "DV01_Test_Function";
    }
  }

}
