/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.livedata.server.mxbean;

import static com.opengamma.test.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link SubscriptionTrace}.
 */
@Test(groups = TestGroup.UNIT)
public class SubscriptionTraceTest {

  /**
   * Tests the object.
   */
  public void test() {
    final String identifier = "id";
    SubscriptionTrace trace = new SubscriptionTrace(identifier);
    assertEquals(trace.getCreated(), "N/A");
    assertTrue(trace.getDistributors().isEmpty());
    assertEquals(trace.getIdentifier(), identifier);
    assertEquals(trace.getLastValues(), "N/A");
    final String created = LocalDate.now().atStartOfDay().toString();
    final DistributorTrace dist1 = new DistributorTrace("topic", LocalDate.now().toString(), false, false, 345);
    final DistributorTrace dist2 = new DistributorTrace("topic", LocalDate.now().toString(), false, false, 346);
    final Set<DistributorTrace> dist = new HashSet<>(Arrays.asList(dist1, dist2));
    final String lastValue = "1000";
    trace = new SubscriptionTrace(identifier, created, dist, lastValue);
    assertEquals(trace.getCreated(), created);
    assertEqualsNoOrder(trace.getDistributors(), dist);
    assertEquals(trace.getIdentifier(), identifier);
    assertEquals(trace.getLastValues(), lastValue);
    assertNotEquals(trace, new SubscriptionTrace(identifier, created, dist, lastValue));
  }
}
