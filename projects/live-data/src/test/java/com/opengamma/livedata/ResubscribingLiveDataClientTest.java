/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.LiveDataSubscriptionResult;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ResubscribingLiveDataClient}.
 */
@Test(groups = TestGroup.UNIT)
public class ResubscribingLiveDataClientTest {
  private static final UserPrincipal USER = UserPrincipal.getTestUser();
  private static final ExternalId ID1 = ExternalId.of("scheme", "1");
  private static final ExternalId ID2 = ExternalId.of("scheme", "2");
  private static final LiveDataSpecification SPEC1 = new LiveDataSpecification("rules", ID1);
  private static final LiveDataSpecification SPEC2 = new LiveDataSpecification("rules", ID2);

  /**
   * Tests that the delegate cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDelegate() {
    new ResubscribingLiveDataClient(null);
  }

  /**
   * Tests a subscription by a user for a single specification.
   */
  @Test
  public void subscribeSingleSpecs() {
    final LiveDataClient delegate = mock(LiveDataClient.class);
    final ResubscribingLiveDataClient client = new ResubscribingLiveDataClient(delegate);
    final Listener listener = new Listener();
    client.subscribe(USER, SPEC1, listener);
    client.subscribe(USER, SPEC2, listener);
    verify(delegate).subscribe(USER, SPEC1, listener);
    verify(delegate).subscribe(USER, SPEC2, listener);
    reset(delegate);
    client.resubscribe();
    verify(delegate).subscribe(USER, SPEC1, listener);
    verify(delegate).subscribe(USER, SPEC2, listener);
  }

  /**
   * Tests a subscription by a user for multiple specifications.
   */
  @Test
  public void subscribeMultipleSpecs() {
    final Collection<LiveDataSpecification> specs = Arrays.asList(SPEC1, SPEC2);
    final LiveDataClient delegate = mock(LiveDataClient.class);
    final ResubscribingLiveDataClient client = new ResubscribingLiveDataClient(delegate);
    final Listener listener = new Listener();
    client.subscribe(USER, specs, listener);
    verify(delegate).subscribe(USER, specs, listener);
    verify(delegate, never()).subscribe(USER, SPEC1, listener);
    verify(delegate, never()).subscribe(USER, SPEC2, listener);
    reset(delegate);
    client.resubscribe();
    // specs are resubscribed one at a time
    verify(delegate, never()).subscribe(USER, specs, listener);
    verify(delegate).subscribe(USER, SPEC1, listener);
    verify(delegate).subscribe(USER, SPEC2, listener);
  }

  /**
   * Tests an unsubscription by a user for a single specification.
   */
  @Test
  public void unsubscribeSingleSpecs() {
    final LiveDataClient delegate = mock(LiveDataClient.class);
    final ResubscribingLiveDataClient client = new ResubscribingLiveDataClient(delegate);
    final Listener listener = new Listener();
    client.subscribe(USER, SPEC1, listener);
    client.subscribe(USER, SPEC2, listener);
    client.unsubscribe(USER, SPEC1, listener);
    verify(delegate).subscribe(USER, SPEC1, listener);
    verify(delegate).subscribe(USER, SPEC2, listener);
    verify(delegate).unsubscribe(USER, SPEC1, listener);
    reset(delegate);
    client.resubscribe();
    verify(delegate).subscribe(USER, SPEC2, listener);
  }

  /**
   * Tests an unsubscription by a user for multiple specifications.
   */
  @Test
  public void unsubscribeMultipleSpecs() {
    final Collection<LiveDataSpecification> specs = Arrays.asList(SPEC1, SPEC2);
    final LiveDataClient delegate = mock(LiveDataClient.class);
    final ResubscribingLiveDataClient client = new ResubscribingLiveDataClient(delegate);
    final Listener listener = new Listener();
    client.subscribe(USER, specs, listener);
    client.unsubscribe(USER, specs, listener);
    verify(delegate).subscribe(USER, specs, listener);
    verify(delegate).unsubscribe(USER, specs, listener);
    verify(delegate, never()).subscribe(USER, SPEC1, listener);
    verify(delegate, never()).subscribe(USER, SPEC2, listener);
    verify(delegate, never()).unsubscribe(USER, SPEC1, listener);
    verify(delegate, never()).unsubscribe(USER, SPEC2, listener);
    reset(delegate);
    client.resubscribe();
    verify(delegate, never()).subscribe(USER, specs, listener);
    verify(delegate, never()).subscribe(USER, SPEC1, listener);
    verify(delegate, never()).subscribe(USER, SPEC2, listener);
  }

  /**
   * Tests that the method is delegated.
   */
  public void testDelegateIsEntitledSingle() {
    final boolean isEntitled1 = true;
    final boolean isEntitled2 = false;
    final LiveDataClient delegate = mock(LiveDataClient.class);
    when(delegate.isEntitled(USER, SPEC1)).thenReturn(isEntitled1);
    when(delegate.isEntitled(USER, SPEC2)).thenReturn(isEntitled2);
    final ResubscribingLiveDataClient client = new ResubscribingLiveDataClient(delegate);
    assertEquals(client.isEntitled(USER, SPEC1), isEntitled1);
    assertEquals(client.isEntitled(USER, SPEC2), isEntitled2);
    verify(delegate).isEntitled(USER, SPEC1);
    verify(delegate).isEntitled(USER, SPEC2);
  }

  /**
   * Tests that the method is delegated.
   */
  public void testDelegateIsEntitledMultiple() {
    final Collection<LiveDataSpecification> specs = Arrays.asList(SPEC1, SPEC2);
    final Map<LiveDataSpecification, Boolean> entitlements = new HashMap<>();
    entitlements.put(SPEC1, false);
    entitlements.put(SPEC2, true);
    final LiveDataClient delegate = mock(LiveDataClient.class);
    when(delegate.isEntitled(USER, specs)).thenReturn(entitlements);
    final ResubscribingLiveDataClient client = new ResubscribingLiveDataClient(delegate);
    assertEquals(client.isEntitled(USER, specs), entitlements);
    verify(delegate).isEntitled(USER, specs);
    verify(delegate, never()).isEntitled(USER, SPEC1);
    verify(delegate, never()).isEntitled(USER, SPEC2);
  }

  /**
   * Tests that the method is delegated.
   */
  public void testDelegateSnapshotSingle() {
    final LiveDataSubscriptionResponse response1 = new LiveDataSubscriptionResponse(SPEC1, LiveDataSubscriptionResult.NOT_AUTHORIZED);
    final LiveDataSubscriptionResponse response2 = new LiveDataSubscriptionResponse(SPEC2, LiveDataSubscriptionResult.SUCCESS);
    final long timeout = 1000L;
    final LiveDataClient delegate = mock(LiveDataClient.class);
    when(delegate.snapshot(USER, SPEC1, timeout)).thenReturn(response1);
    when(delegate.snapshot(USER, SPEC2, timeout)).thenReturn(response2);
    final ResubscribingLiveDataClient client = new ResubscribingLiveDataClient(delegate);
    assertEquals(client.snapshot(USER, SPEC1, timeout), response1);
    assertEquals(client.snapshot(USER, SPEC2, timeout), response2);
    verify(delegate).snapshot(USER, SPEC1, timeout);
    verify(delegate).snapshot(USER, SPEC2, timeout);
  }

  /**
   * Tests that the method is delegated.
   */
  public void testDelegateSnapshotMultiple() {
    final Collection<LiveDataSubscriptionResponse> responses = Arrays.asList(new LiveDataSubscriptionResponse(SPEC1, LiveDataSubscriptionResult.NOT_AUTHORIZED),
        new LiveDataSubscriptionResponse(SPEC2, LiveDataSubscriptionResult.SUCCESS));
    final Collection<LiveDataSpecification> specs = Arrays.asList(SPEC1, SPEC2);
    final long timeout = 20L;
    final LiveDataClient delegate = mock(LiveDataClient.class);
    when(delegate.snapshot(USER, specs, timeout)).thenReturn(responses);
    final ResubscribingLiveDataClient client = new ResubscribingLiveDataClient(delegate);
    assertEquals(client.snapshot(USER, specs, timeout), responses);
    verify(delegate).snapshot(USER, specs, timeout);
    verify(delegate, never()).snapshot(USER, SPEC1, timeout);
    verify(delegate, never()).snapshot(USER, SPEC2, timeout);
  }

  private static class Listener implements LiveDataListener {

    @Override
    public void subscriptionResultReceived(final LiveDataSubscriptionResponse subscriptionResult) {
    }

    @Override
    public void subscriptionResultsReceived(final Collection<LiveDataSubscriptionResponse> subscriptionResults) {
    }

    @Override
    public void subscriptionStopped(final LiveDataSpecification fullyQualifiedSpecification) {
    }

    @Override
    public void valueUpdate(final LiveDataValueUpdate valueUpdate) {
    }
  }
}
