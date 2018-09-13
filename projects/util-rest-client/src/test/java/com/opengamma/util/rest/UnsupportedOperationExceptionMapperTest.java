/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Test {@link UnsupportedOperationExceptionMapper}.
 */
@Test(groups = TestGroup.UNIT)
public class UnsupportedOperationExceptionMapperTest extends AbstractExceptionMapperTestHelper {

  /**
   * Tests the mapping for the data types.
   *
   * @param mediaType  the media type
   * @throws Exception  if there is a problem
   */
  @Test(dataProvider = "mediaTypes")
  public void testMapping(final MediaType mediaType) throws Exception {
    final UnsupportedOperationException ex = new UnsupportedOperationException("Test message");
    final UnsupportedOperationExceptionMapper mapper = new UnsupportedOperationExceptionMapper();
    init(mapper, mediaType);

    final Response test = mapper.toResponse(ex);
    testResult(test, Status.SERVICE_UNAVAILABLE, ex);
  }

  /**
   * Tests the output message when the exception is null.
   *
   * @param mediaType  the media type
   * @throws Exception  if there is a problem
   */
  @Test(dataProvider = "mediaTypes")
  public void testNoErrorMessage(final MediaType mediaType) throws Exception {
    final UnsupportedOperationExceptionMapper mapper = new UnsupportedOperationExceptionMapper();
    init(mapper, mediaType);

    final Map<String, String> data = mapper.getMessage();
    mapper.buildOutputMessage(null, data);
    assertEquals(data.size(), 1);
    assertEquals(data.get("message"), "");
  }

  /**
   * Tests the output message when the exception has no message.
   *
   * @param mediaType  the media type
   * @throws Exception  if there is a problem
   */
  @Test(dataProvider = "mediaTypes")
  public void testNoOutputMessage(final MediaType mediaType) throws Exception {
    final UnsupportedOperationExceptionMapper mapper = new UnsupportedOperationExceptionMapper();
    init(mapper, mediaType);

    final Map<String, String> data = mapper.getMessage();
    mapper.buildOutputMessage(new UnsupportedOperationException(), data);
    assertEquals(data.size(), 2);
    assertEquals(data.get("message"), "");
    assertTrue(data.get("locator").startsWith(
        "<p>UnsupportedOperationException<br />&nbsp;&nbsp;at com.opengamma.util.rest.UnsupportedOperationExceptionMapperTest.testNoOutputMessage()"));
    assertTrue(data.get("locator").endsWith("</p>"));
  }

  /**
   * Tests the output message when the exception has no message.
   *
   * @param mediaType  the media type
   * @throws Exception  if there is a problem
   */
  @Test(dataProvider = "mediaTypes")
  public void testOutputMessage(final MediaType mediaType) throws Exception {
    final UnsupportedOperationExceptionMapper mapper = new UnsupportedOperationExceptionMapper();
    init(mapper, mediaType);

    final Map<String, String> data = mapper.getMessage();
    final String message = "Reason for exception";
    mapper.buildOutputMessage(new UnsupportedOperationException(message), data);
    assertEquals(data.size(), 2);
    assertEquals(data.get("message"), message);
    assertTrue(data.get("locator").startsWith(
        "<p>UnsupportedOperationException<br />&nbsp;&nbsp;at com.opengamma.util.rest.UnsupportedOperationExceptionMapperTest.testOutputMessage()"));
    assertTrue(data.get("locator").endsWith("</p>"));
  }

}
