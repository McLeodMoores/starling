/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.util.rest;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import java.util.Collections;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;
import com.sun.jersey.core.header.OutBoundHeaders;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;

/**
 * Tests for {@link NoCachingFilter}.
 */
@Test(groups = TestGroup.UNIT)
public class NoCachingFilterTest {
  private static final NoCachingFilter FILTER = new NoCachingFilter();

  /**
   * Tests a filter on PUT has no effect.
   */
  public void testFilterNoActionOnPut() {
    final ContainerRequest request = Mockito.mock(ContainerRequest.class);
    final ContainerResponse response = Mockito.mock(ContainerResponse.class);
    when(request.getMethod()).thenReturn("PUT");
    final ContainerResponse filtered = FILTER.filter(request, response);
    assertSame(filtered, response);
    verify(response, never()).getHttpHeaders();
  }

  /**
   * Tests that the response is unchanged if the ETAG, CACHE_CONTROL and EXPIRES
   * headers are set.
   */
  public void testNoEtagCacheControlExpiresHeader() {
    final MultivaluedMap<String, Object> headers = new OutBoundHeaders();
    headers.add(HttpHeaders.ETAG, "abc");
    headers.add(HttpHeaders.CACHE_CONTROL, "directive");
    headers.add(HttpHeaders.EXPIRES, "2020-12-01");
    final ContainerRequest request = Mockito.mock(ContainerRequest.class);
    final ContainerResponse response = Mockito.mock(ContainerResponse.class);
    when(request.getMethod()).thenReturn("GET");
    when(response.getHttpHeaders()).thenReturn(headers);
    final ContainerResponse filtered = FILTER.filter(request, response);
    assertSame(filtered, response);
    verify(response).getHttpHeaders();
  }

  /**
   * Tests that the response is unchanged if the CACHE_CONTROL and EXPIRES
   * headers are set.
   */
  public void testNoEtagHeader() {
    final MultivaluedMap<String, Object> headers = new OutBoundHeaders();
    headers.add(HttpHeaders.CACHE_CONTROL, "directive");
    headers.add(HttpHeaders.EXPIRES, "2020-12-01");
    final ContainerRequest request = Mockito.mock(ContainerRequest.class);
    final ContainerResponse response = Mockito.mock(ContainerResponse.class);
    when(request.getMethod()).thenReturn("GET");
    when(response.getHttpHeaders()).thenReturn(headers);
    final ContainerResponse filtered = FILTER.filter(request, response);
    assertSame(filtered, response);
    verify(response).getHttpHeaders();
  }

  /**
   * Tests that the response is unchanged if the ETAG and EXPIRES headers are
   * set.
   */
  public void testNoCacheControlHeader() {
    final MultivaluedMap<String, Object> headers = new OutBoundHeaders();
    headers.add(HttpHeaders.ETAG, "abc");
    headers.add(HttpHeaders.EXPIRES, "2020-12-01");
    final ContainerRequest request = Mockito.mock(ContainerRequest.class);
    final ContainerResponse response = Mockito.mock(ContainerResponse.class);
    when(request.getMethod()).thenReturn("GET");
    when(response.getHttpHeaders()).thenReturn(headers);
    final ContainerResponse filtered = FILTER.filter(request, response);
    assertSame(filtered, response);
    verify(response).getHttpHeaders();
  }

  /**
   * Tests that the response is unchanged if the ETAG and CACHE_CONTROL headers
   * are set.
   */
  public void testNoExpiresHeader() {
    final MultivaluedMap<String, Object> headers = new OutBoundHeaders();
    headers.add(HttpHeaders.ETAG, "abc");
    headers.add(HttpHeaders.CACHE_CONTROL, "directive");
    final ContainerRequest request = Mockito.mock(ContainerRequest.class);
    final ContainerResponse response = Mockito.mock(ContainerResponse.class);
    when(request.getMethod()).thenReturn("GET");
    when(response.getHttpHeaders()).thenReturn(headers);
    final ContainerResponse filtered = FILTER.filter(request, response);
    assertSame(filtered, response);
    verify(response).getHttpHeaders();
  }

  /**
   * Tests that CACHE_CONTROL and EXPIRES headers are added if ETAG,
   * CACHE_CONTROL and EXPIRES are not set in the response.
   */
  public void testHeadersAdded() {
    final MultivaluedMap<String, Object> headers = new OutBoundHeaders();
    final ContainerRequest request = Mockito.mock(ContainerRequest.class);
    final ContainerResponse response = Mockito.mock(ContainerResponse.class);
    when(request.getMethod()).thenReturn("GET");
    when(response.getHttpHeaders()).thenReturn(headers);
    final ContainerResponse filtered = FILTER.filter(request, response);
    assertSame(filtered, response);
    verify(response).getHttpHeaders();
    assertEquals(filtered.getHttpHeaders().size(), 2);
    assertEquals(filtered.getHttpHeaders().get(HttpHeaders.CACHE_CONTROL), Collections.singletonList("no-cache, no-store, max-age=0, must-revalidate"));
    assertEquals(filtered.getHttpHeaders().get(HttpHeaders.EXPIRES), Collections.singletonList("Mon, 26 Jul 1997 05:00:00 GMT"));
  }
}
