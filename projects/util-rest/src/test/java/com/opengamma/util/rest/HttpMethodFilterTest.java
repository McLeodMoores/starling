/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertSame;

import java.util.Arrays;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.spi.container.ContainerRequest;

/**
 * Test HttpMethodFilter.
 */
@Test(groups = TestGroup.UNIT)
public class HttpMethodFilterTest {

  /**
   * Tests a filter on GET.
   */
  public void testFilterNoActionOnGet() {
    final ContainerRequest mock = Mockito.mock(ContainerRequest.class);
    when(mock.getMethod()).thenReturn("GET");

    final HttpMethodFilter test = new HttpMethodFilter();
    final ContainerRequest result = test.filter(mock);

    assertSame(mock, result);
    verify(mock).getMethod();
    verifyNoMoreInteractions(mock);
  }

  /**
   * Tests a filter on POST.
   */
  public void testFilterNoActionOnPostNoForm() {
    final ContainerRequest mock = Mockito.mock(ContainerRequest.class);
    when(mock.getMethod()).thenReturn("POST");
    when(mock.getFormParameters()).thenReturn(new Form());

    final HttpMethodFilter test = new HttpMethodFilter();
    final ContainerRequest result = test.filter(mock);

    assertSame(mock, result);
    verify(mock).getMethod();
    verify(mock).getFormParameters();
    verifyNoMoreInteractions(mock);
  }

  /**
   * Tests a filter on PUT.
   */
  public void testFilterNoActionOnPostFormPut() {
    final ContainerRequest mock = Mockito.mock(ContainerRequest.class);
    when(mock.getMethod()).thenReturn("POST");
    final Form form = new Form();
    form.put("method", Arrays.asList("PUT"));
    when(mock.getFormParameters()).thenReturn(form);

    final HttpMethodFilter test = new HttpMethodFilter();
    final ContainerRequest result = test.filter(mock);

    assertSame(mock, result);
    verify(mock).getMethod();
    verify(mock).getFormParameters();
    verify(mock).setMethod("PUT");
    verifyNoMoreInteractions(mock);
  }

  /**
   * Tests a filter on DELETE.
   */
  public void testFilterNoActionOnPostFormDelete() {
    final ContainerRequest mock = Mockito.mock(ContainerRequest.class);
    when(mock.getMethod()).thenReturn("POST");
    final Form form = new Form();
    form.put("method", Arrays.asList("DELETE"));
    when(mock.getFormParameters()).thenReturn(form);

    final HttpMethodFilter test = new HttpMethodFilter();
    final ContainerRequest result = test.filter(mock);

    assertSame(mock, result);
    verify(mock).getMethod();
    verify(mock).getFormParameters();
    verify(mock).setMethod("DELETE");
    verifyNoMoreInteractions(mock);
  }

  /**
   * Tests a filter on POSTing options.
   */
  public void testFilterNoActionOnPostFormOptions() {
    final ContainerRequest mock = Mockito.mock(ContainerRequest.class);
    when(mock.getMethod()).thenReturn("POST");
    final Form form = new Form();
    form.put("method", Arrays.asList("OPTIONS"));
    when(mock.getFormParameters()).thenReturn(form);

    final HttpMethodFilter test = new HttpMethodFilter();
    final ContainerRequest result = test.filter(mock);

    assertSame(mock, result);
    verify(mock).getMethod();
    verify(mock).getFormParameters();
    verify(mock).setMethod("OPTIONS");
    verifyNoMoreInteractions(mock);
  }

  /**
   * Tests a filter on POSTing head.
   */
  public void testFilterNoActionOnPostFormHead() {
    final ContainerRequest mock = Mockito.mock(ContainerRequest.class);
    when(mock.getMethod()).thenReturn("POST");
    final Form form = new Form();
    form.put("method", Arrays.asList("HEAD"));
    when(mock.getFormParameters()).thenReturn(form);

    final HttpMethodFilter test = new HttpMethodFilter();
    final ContainerRequest result = test.filter(mock);

    assertSame(mock, result);
    verify(mock).getMethod();
    verify(mock).getFormParameters();
    verify(mock).setMethod("HEAD");
    verifyNoMoreInteractions(mock);
  }

  /**
   * Tests a filter on POST.
   */
  public void testFilterNoActionOnPostFormPost() {
    final ContainerRequest mock = Mockito.mock(ContainerRequest.class);
    when(mock.getMethod()).thenReturn("POST");
    final Form form = new Form();
    form.put("method", Arrays.asList("POST"));
    when(mock.getFormParameters()).thenReturn(form);

    final HttpMethodFilter test = new HttpMethodFilter();
    final ContainerRequest result = test.filter(mock);

    assertSame(mock, result);
    verify(mock).getMethod();
    verify(mock).getFormParameters();
    verify(mock).setMethod("POST");
    verifyNoMoreInteractions(mock);
  }

  /**
   * Tests a filter on POST.
   */
  public void testFilterNoActionOnPostFormGet() {
    final ContainerRequest mock = Mockito.mock(ContainerRequest.class);
    when(mock.getMethod()).thenReturn("POST");
    final Form form = new Form();
    form.put("method", Arrays.asList("GET"));
    when(mock.getFormParameters()).thenReturn(form);

    final HttpMethodFilter test = new HttpMethodFilter();
    final ContainerRequest result = test.filter(mock);

    assertSame(mock, result);
    verify(mock).getMethod();
    verify(mock).getFormParameters();
    verify(mock).setMethod("GET");
    verifyNoMoreInteractions(mock);
  }

  /**
   * Tests a filter on POST.
   */
  public void testFilterNoActionOnPostFormNoMatch() {
    final ContainerRequest mock = Mockito.mock(ContainerRequest.class);
    when(mock.getMethod()).thenReturn("POST");
    final Form form = new Form();
    form.put("method", Arrays.asList("FOOBAR"));
    when(mock.getFormParameters()).thenReturn(form);

    final HttpMethodFilter test = new HttpMethodFilter();
    final ContainerRequest result = test.filter(mock);

    assertSame(mock, result);
    verify(mock).getMethod();
    verify(mock).getFormParameters();
    verifyNoMoreInteractions(mock);
  }

}
