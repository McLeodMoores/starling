/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.testng.annotations.Test;

import com.opengamma.DataVersionException;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Test DataVersionExceptionMapper.
 */
@Test(groups = TestGroup.UNIT)
public class DataVersionExceptionMapperTest extends AbstractExceptionMapperTestHelper {

  @Test(dataProvider = "mediaTypes")
  public void test_mapping(final MediaType mediaType) throws Exception {
    final DataVersionException ex = new DataVersionException("Test message");
    final DataVersionExceptionMapper mapper = new DataVersionExceptionMapper();
    init(mapper, mediaType);

    final Response test = mapper.toResponse(ex);
    testResult(test, Status.CONFLICT, ex);
  }

}
