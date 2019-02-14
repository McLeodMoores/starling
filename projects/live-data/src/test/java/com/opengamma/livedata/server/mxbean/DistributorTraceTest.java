/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.livedata.server.mxbean;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link DistributorTrace}.
 */
@Test(groups = TestGroup.UNIT)
public class DistributorTraceTest {

  /**
   * Tests the object.
   */
  public void test() {
    final String jmsTopic = "topic";
    final String expiry = "expiry";
    final boolean hasExpired = false;
    final boolean isPersistent = true;
    final long messagesSent = 4857;
    final DistributorTrace trace = new DistributorTrace(jmsTopic, expiry, hasExpired, isPersistent, messagesSent);
    assertEquals(trace.getJmsTopic(), jmsTopic);
    assertEquals(trace.getExpiry(), expiry);
    assertEquals(trace.isHasExpired(), hasExpired);
    assertEquals(trace.isPersistent(), isPersistent);
    assertEquals(trace.getMessagesSent(), messagesSent);
    assertNotEquals(trace, new DistributorTrace(jmsTopic, expiry, hasExpired, isPersistent, messagesSent));
  }
}
