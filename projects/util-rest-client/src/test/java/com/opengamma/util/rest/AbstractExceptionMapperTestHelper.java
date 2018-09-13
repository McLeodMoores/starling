/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.lang.reflect.Field;
import java.util.Arrays;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.testng.annotations.DataProvider;

import com.opengamma.transport.jaxrs.FudgeRest;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Abstract helper for mapper tests.
 */
public abstract class AbstractExceptionMapperTestHelper {

  /**
   * Initialises the test.
   *
   * @param mapper  the mapper
   * @param mediaType  the media type
   * @throws Exception  an exception if the test cannot be initialized
   */
  protected void init(final ExceptionMapper<?> mapper, final MediaType mediaType) throws Exception {
    final HttpHeaders headers = mock(HttpHeaders.class);
    when(headers.getAcceptableMediaTypes()).thenReturn(Arrays.asList(mediaType));

    final Field field = AbstractExceptionMapper.class.getDeclaredField("_headers");
    field.setAccessible(true);
    field.set(mapper, headers);
  }

  /**
   * Tests the result.
   *
   * @param test  the response
   * @param status  the status
   * @param th  the exception
   */
  protected void testResult(final Response test, final Status status, final Throwable th) {
    assertEquals("Status: " + status.getStatusCode() + " " + status.getReasonPhrase() + "; Message: " + th.getMessage(), test.getEntity());
    assertEquals(status.getStatusCode(), test.getStatus());
    assertEquals(1, test.getMetadata().get(ExceptionThrowingClientFilter.EXCEPTION_TYPE).size());
    assertEquals(th.getClass().getName(), test.getMetadata().get(ExceptionThrowingClientFilter.EXCEPTION_TYPE).get(0));
    assertEquals(1, test.getMetadata().get(ExceptionThrowingClientFilter.EXCEPTION_MESSAGE).size());
    assertEquals(th.getMessage(), test.getMetadata().get(ExceptionThrowingClientFilter.EXCEPTION_MESSAGE).get(0));
    assertEquals(1, test.getMetadata().get(ExceptionThrowingClientFilter.EXCEPTION_POINT).size());
    assertEquals(true, test.getMetadata().get(ExceptionThrowingClientFilter.EXCEPTION_POINT).get(0).toString().contains(getClass().getName()));
    assertEquals(true, test.getMetadata().get(ExceptionThrowingClientFilter.EXCEPTION_POINT).get(0).toString().contains(".java"));
  }

  /**
   * Provides data types for test cases.
   *
   * @return  the types
   */
  @DataProvider(name = "mediaTypes")
  public Object[][] dataMediaTypes() {
    return new Object[][] {
        {MediaType.APPLICATION_JSON_TYPE},
        {FudgeRest.MEDIA_TYPE},
    };
  }

}
