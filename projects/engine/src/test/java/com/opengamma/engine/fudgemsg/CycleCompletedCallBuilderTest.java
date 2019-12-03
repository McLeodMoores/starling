/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.testng.annotations.Test;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import com.google.common.base.Function;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.impl.InMemoryViewComputationResultModel;
import com.opengamma.engine.view.impl.InMemoryViewDeltaResultModel;
import com.opengamma.engine.view.listener.CycleCompletedCall;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test the {@link FixedHistoricalMarketDataSpecificationFudgeBuilder} class.
 */
@Test(groups = TestGroup.UNIT)
public class CycleCompletedCallBuilderTest extends AbstractFudgeBuilderTestCase {

  public void testNormalConstructor() {
    final InMemoryViewComputationResultModel resultModel = new InMemoryViewComputationResultModel();
    resultModel.setViewCycleExecutionOptions(ViewCycleExecutionOptions.builder().create());
    resultModel.setCalculationDuration(Duration.ZERO);
    resultModel.setCalculationTime(Instant.now());
    resultModel.setVersionCorrection(VersionCorrection.LATEST);
    resultModel.setViewCycleId(UniqueId.of("A", "B"));
    resultModel.setViewProcessId(UniqueId.of("A", "C"));
    final InMemoryViewDeltaResultModel deltaResultModel = new InMemoryViewDeltaResultModel();
    deltaResultModel.setViewCycleExecutionOptions(ViewCycleExecutionOptions.builder().create());
    deltaResultModel.setCalculationDuration(Duration.ZERO);
    deltaResultModel.setCalculationTime(Instant.now());
    deltaResultModel.setVersionCorrection(VersionCorrection.LATEST);
    deltaResultModel.setViewCycleId(UniqueId.of("A", "B"));
    deltaResultModel.setViewProcessId(UniqueId.of("A", "C"));
    deltaResultModel.setPreviousCalculationTime(Instant.now().minusSeconds(20));
    final CycleCompletedCall cycleCompletedCall = new CycleCompletedCall(resultModel, deltaResultModel);
    cycleObject(CycleCompletedCall.class, cycleCompletedCall);
  }

  public void testSuperClass() {
    final InMemoryViewComputationResultModel resultModel = new InMemoryViewComputationResultModel();
    resultModel.setViewCycleExecutionOptions(ViewCycleExecutionOptions.builder().create());
    resultModel.setCalculationDuration(Duration.ZERO);
    resultModel.setCalculationTime(Instant.now());
    resultModel.setVersionCorrection(VersionCorrection.LATEST);
    resultModel.setViewCycleId(UniqueId.of("A", "B"));
    resultModel.setViewProcessId(UniqueId.of("A", "C"));
    final InMemoryViewDeltaResultModel deltaResultModel = new InMemoryViewDeltaResultModel();
    deltaResultModel.setViewCycleExecutionOptions(ViewCycleExecutionOptions.builder().create());
    deltaResultModel.setCalculationDuration(Duration.ZERO);
    deltaResultModel.setCalculationTime(Instant.now());
    deltaResultModel.setVersionCorrection(VersionCorrection.LATEST);
    deltaResultModel.setViewCycleId(UniqueId.of("A", "B"));
    deltaResultModel.setViewProcessId(UniqueId.of("A", "C"));
    deltaResultModel.setPreviousCalculationTime(Instant.now().minusSeconds(20));
    final CycleCompletedCall cycleCompletedCall = new CycleCompletedCall(resultModel, deltaResultModel);
    cycleObject(Function.class, cycleCompletedCall);
  }

}
