/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.util.rest;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.google.common.net.HttpHeaders;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.spi.container.ContainerRequest;

/**
 * Tests for {@link UrlSuffixFilter}.
 */
@Test(groups = TestGroup.UNIT)
public class UrlSuffixFilterTest {
  private static final UrlSuffixFilter FILTER = new UrlSuffixFilter();

  /**
   * Tests a filter on PUT has no effect.
   */
  public void testFilterNoActionOnPut() {
    final ContainerRequest request = Mockito.mock(ContainerRequest.class);
    when(request.getMethod()).thenReturn("PUT");
    final ContainerRequest filtered = FILTER.filter(request);
    assertSame(filtered, request);
    verify(request, never()).getRequestUri();
  }

  /**
   * Tests a filter on a type that is not supported.
   *
   * @throws URISyntaxException
   *           if there is a problem with the URI path
   */
  public void testFilterUnsupportedType() throws URISyntaxException {
    final ContainerRequest request = Mockito.mock(ContainerRequest.class);
    when(request.getMethod()).thenReturn("GET");
    final String path = "/test/path/type.notsupported";
    final URI requestUri = new URI(path);
    when(request.getRequestUri()).thenReturn(requestUri);
    final ContainerRequest filtered = FILTER.filter(request);
    assertSame(filtered, request);
    assertEquals(filtered.getRequestUri().getPath(), path);
    verify(request, never()).getRequestHeaders();
  }

  /**
   * Tests a filter on a path to .csv.
   *
   * @throws URISyntaxException
   *           if there is a problem with the URI path
   */
  public void testFilterCsv() throws URISyntaxException {
    testHeaders("/path/to/test.csv", "text/csv");
  }

  /**
   * Tests a filter on a path to .json.
   *
   * @throws URISyntaxException
   *           if there is a problem with the URI path
   */
  public void testFilterJson() throws URISyntaxException {
    testHeaders("/path/to/test.json", "application/json");
  }

  /**
   * Tests a filter on a path to .xml.
   *
   * @throws URISyntaxException
   *           if there is a problem with the URI path
   */
  public void testFilterXml() throws URISyntaxException {
    testHeaders("/path/to/test.xml", "application/xml");
  }

  /**
   * Tests a filter on a path to .fudge.
   *
   * @throws URISyntaxException
   *           if there is a problem with the URI path
   */
  public void testFilterFudge() throws URISyntaxException {
    testHeaders("/path/to/test.fudge", "application/vnd.fudgemsg");
  }

  /**
   * Tests a filter on a path to .html.
   *
   * @throws URISyntaxException
   *           if there is a problem with the URI path
   */
  public void testFilterHtml() throws URISyntaxException {
    testHeaders("/path/to/test.html", "text/html");
  }

  /**
   * Tests a filter on a path to .jbxml.
   *
   * @throws URISyntaxException
   *           if there is a problem with the URI path
   */
  public void testFilterJbxml() throws URISyntaxException {
    testHeaders("/path/to/test.jbxml", "application/vnd.org.joda.bean+xml");
  }

  /**
   * Tests a filter on a path to .jbbin.
   *
   * @throws URISyntaxException
   *           if there is a problem with the URI path
   */
  public void testFilterJbbin() throws URISyntaxException {
    testHeaders("/path/to/test.jbbin", "application/vnd.org.joda.bean");
  }

  private static void testHeaders(final String path, final String expectedMimeType) throws URISyntaxException {
    final InBoundHeaders headers = new InBoundHeaders();
    final ContainerRequest request = Mockito.mock(ContainerRequest.class);
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestHeaders()).thenReturn(headers);
    final URI requestUri = new URI(path);
    when(request.getRequestUri()).thenReturn(requestUri);
    final ContainerRequest filtered = FILTER.filter(request);
    assertSame(filtered, request);
    assertEquals(filtered.getRequestUri().getPath(), path);
    verify(request, times(1)).getRequestHeaders();
    assertEquals(headers.size(), 1);
    assertEquals(headers.get(HttpHeaders.ACCEPT), Arrays.asList(expectedMimeType));
  }
}

