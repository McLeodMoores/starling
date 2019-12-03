/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.engine.exec;

import static org.testng.Assert.assertEquals;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests executing a plan.
 */
@Test(groups = TestGroup.UNIT)
public class PlanExecutorGetWithTimeoutTest extends PlanExecutorTest {

  /**
   * Timeout is set just in case "get" decides to block.
   *
   * @throws Throwable
   *           if there is an unexpected exception
   */
  @Test(timeOut = 5000)
  public void testGetWithTimeout() throws Throwable {
    final ExecutorService threads = Executors.newSingleThreadExecutor();
    try {
      final NormalExecutionJobDispatcher dispatcher = new NormalExecutionJobDispatcher();
      final PlanExecutor executor = new PlanExecutor(createCycle(dispatcher), createPlan());
      threads.submit(() -> dispatcher.execute(executor));
      assertEquals(executor.get(5, TimeUnit.SECONDS), "Default");
    } finally {
      threads.shutdownNow();
      threads.awaitTermination(3, TimeUnit.SECONDS);
    }
  }

}
