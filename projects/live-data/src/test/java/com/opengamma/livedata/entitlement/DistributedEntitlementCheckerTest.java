/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.entitlement;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.client.DistributedEntitlementChecker;
import com.opengamma.transport.ByteArrayFudgeRequestSender;
import com.opengamma.transport.FudgeRequestDispatcher;
import com.opengamma.transport.InMemoryByteArrayRequestConduit;
import com.opengamma.util.test.TestGroup;

/**
 * Integration test between {@link DistributedEntitlementChecker} and {@link EntitlementServer}.
 */
@Test(groups = TestGroup.UNIT)
public class DistributedEntitlementCheckerTest {

  /**
   * Tests that an empty map is returned if the user is null.
   */
  @Test
  public void testNullUser() {
    final TestLiveDataEntitlementChecker delegate = new TestLiveDataEntitlementChecker();
    final EntitlementServer server = new EntitlementServer(delegate);
    final FudgeRequestDispatcher fudgeRequestDispatcher = new FudgeRequestDispatcher(server);
    final InMemoryByteArrayRequestConduit inMemoryByteArrayRequestConduit = new InMemoryByteArrayRequestConduit(fudgeRequestDispatcher);
    final ByteArrayFudgeRequestSender fudgeRequestSender = new ByteArrayFudgeRequestSender(inMemoryByteArrayRequestConduit);
    final DistributedEntitlementChecker client = new DistributedEntitlementChecker(fudgeRequestSender);
    final LiveDataSpecification spec1 = new LiveDataSpecification("TestNormalization", ExternalId.of("test1", "test1"));
    final LiveDataSpecification spec2 = new LiveDataSpecification("TestNormalization", ExternalId.of("test2", "test1"));
    assertTrue(client.isEntitled(null, Arrays.asList(spec1, spec2)).isEmpty());
  }

  /**
   * Tests that an empty map is returned if the specifications are null.
   */
  @Test
  public void testEmpty() {
    final TestLiveDataEntitlementChecker delegate = new TestLiveDataEntitlementChecker();
    final EntitlementServer server = new EntitlementServer(delegate);
    final FudgeRequestDispatcher fudgeRequestDispatcher = new FudgeRequestDispatcher(server);
    final InMemoryByteArrayRequestConduit inMemoryByteArrayRequestConduit = new InMemoryByteArrayRequestConduit(fudgeRequestDispatcher);
    final ByteArrayFudgeRequestSender fudgeRequestSender = new ByteArrayFudgeRequestSender(inMemoryByteArrayRequestConduit);
    final DistributedEntitlementChecker client = new DistributedEntitlementChecker(fudgeRequestSender);
    assertTrue(client.isEntitled(null, Collections.<LiveDataSpecification> emptySet()).isEmpty());
  }

  /**
   * Tests a request and response.
   */
  @Test
  public void testRequestResponse() {
    final TestLiveDataEntitlementChecker delegate = new TestLiveDataEntitlementChecker();
    final EntitlementServer server = new EntitlementServer(delegate);

    final FudgeRequestDispatcher fudgeRequestDispatcher = new FudgeRequestDispatcher(server);
    final InMemoryByteArrayRequestConduit inMemoryByteArrayRequestConduit = new InMemoryByteArrayRequestConduit(fudgeRequestDispatcher);
    final ByteArrayFudgeRequestSender fudgeRequestSender = new ByteArrayFudgeRequestSender(inMemoryByteArrayRequestConduit);

    final DistributedEntitlementChecker client = new DistributedEntitlementChecker(fudgeRequestSender);

    final LiveDataSpecification testSpecEntitled = new LiveDataSpecification("TestNormalization", ExternalId.of("test1", "test1"));
    final LiveDataSpecification testSpecNotEntitled = new LiveDataSpecification("TestNormalization", ExternalId.of("test2", "test1"));

    final UserPrincipal megan = new UserPrincipal("megan", "127.0.0.1");

    assertTrue(client.isEntitled(megan, testSpecEntitled));
    assertFalse(client.isEntitled(megan, testSpecNotEntitled));
  }

  private static class TestLiveDataEntitlementChecker extends AbstractEntitlementChecker {

    @Override
    public boolean isEntitled(final UserPrincipal user, final LiveDataSpecification requestedSpecification) {
      return requestedSpecification.getIdentifier(ExternalScheme.of("test1")) != null;
    }

  }
}
