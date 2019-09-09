/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.engine.exec;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import com.opengamma.engine.calcnode.JobDispatcher;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link PlanExecutor} when an operation is cancelled during a get.
 */
@Test(groups = TestGroup.UNIT)
public class PlanExecutorCancelDuringGetTest extends PlanExecutorTest {

  /**
   * Timeout is set just in case "get" decides to block.
   * 
   * @throws Throwable
   *           if there is an unexpected exception
   */
  @Test(timeOut = 5000, expectedExceptions = CancellationException.class)
  public void testCancelDuringGet() throws Throwable {
    final ExecutorService threads = Executors.newSingleThreadExecutor();
    try {
      final PlanExecutor executor = new PlanExecutor(createCycle(new JobDispatcher()), createPlan());
      executor.start();
      threads.submit(() -> executor.cancel(true));
      executor.get();
    } finally {
      threads.shutdownNow();
      threads.awaitTermination(3, TimeUnit.SECONDS);
    }
  }

}
