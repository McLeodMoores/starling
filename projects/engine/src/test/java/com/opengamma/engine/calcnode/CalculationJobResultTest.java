/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.google.common.collect.Lists;
import com.opengamma.engine.cache.AbstractIdentifierMap;
import com.opengamma.engine.cache.IdentifierMap;
import com.opengamma.engine.cache.InMemoryIdentifierMap;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.id.UniqueId;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.log.LogLevel;
import com.opengamma.util.test.TestGroup;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class CalculationJobResultTest {
  private static final FudgeContext FUDGE_CONTEXT = OpenGammaFudgeContext.getInstance();

  public void fudge() {
    final IdentifierMap identifierMap = new InMemoryIdentifierMap();
    final CalculationJobSpecification spec = new CalculationJobSpecification(UniqueId.of("Test", "ViewCycle"), "config", Instant.now(), 1L);

    final CalculationJobResultItem item1 = CalculationJobResultItem.success();

    final MutableExecutionLog executionLog = new MutableExecutionLog(ExecutionLogMode.INDICATORS);
    final CalculationJobResultItem item2 = CalculationJobResultItemBuilder.of(executionLog).withException(new RuntimeException("failure!")).toResultItem();

    final CalculationJobResult result = new CalculationJobResult(spec,
        500,
        Lists.newArrayList(item1, item2),
        "localhost");
    AbstractIdentifierMap.convertIdentifiers(identifierMap, result);
    final FudgeSerializer serializationContext = new FudgeSerializer(FUDGE_CONTEXT);
    final MutableFudgeMsg inputMsg = serializationContext.objectToFudgeMsg(result);
    final FudgeMsg outputMsg = FUDGE_CONTEXT.deserialize(FUDGE_CONTEXT.toByteArray(inputMsg)).getMessage();

    final FudgeDeserializer deserializationContext = new FudgeDeserializer(FUDGE_CONTEXT);
    final CalculationJobResult outputJob = deserializationContext.fudgeMsgToObject(CalculationJobResult.class, outputMsg);

    assertNotNull(outputJob);
    AbstractIdentifierMap.resolveIdentifiers(identifierMap, outputJob);
    assertEquals(spec, outputJob.getSpecification());
    assertEquals(500, outputJob.getDuration());
    assertEquals("localhost", outputJob.getComputeNodeId());
    assertNotNull(outputJob.getResultItems());
    assertEquals(2, outputJob.getResultItems().size());
    final CalculationJobResultItem outputItem1 = outputJob.getResultItems().get(0);
    assertNotNull(outputItem1);
    assertEquals(InvocationResult.SUCCESS, outputItem1.getResult());
    assertFalse(outputItem1.getExecutionLog().getLogLevels().contains(LogLevel.ERROR));
    assertTrue(outputItem1.getMissingInputs().isEmpty());

    final CalculationJobResultItem outputItem2 = outputJob.getResultItems().get(1);
    assertNotNull(outputItem2);
    assertEquals(InvocationResult.FUNCTION_THREW_EXCEPTION, outputItem2.getResult());
    assertEquals("java.lang.RuntimeException", outputItem2.getExecutionLog().getExceptionClass());
    assertEquals("failure!", outputItem2.getExecutionLog().getExceptionMessage());
    assertNotNull(outputItem2.getExecutionLog().getExceptionStackTrace());
    assertTrue(outputItem2.getMissingInputs().isEmpty());
  }


}
