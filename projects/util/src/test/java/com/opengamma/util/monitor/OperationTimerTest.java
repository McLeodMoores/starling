/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.monitor;

import static org.testng.AssertJUnit.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test OperationTimer.
 */
@Test(groups = TestGroup.INTEGRATION)
public class OperationTimerTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(OperationTimerTest.class);

  /**
   * @throws InterruptedException
   *           if there is a problem
   */
  public void simpleTest() throws InterruptedException {
    final OperationTimer timer = new OperationTimer(LOGGER, "Testing");
    Thread.sleep(100);
    final long result = timer.finished();
    // We're not guaranteed that sleeping for 100ms will be exactly 100ms.
    assertTrue(result > 90);
  }

}
