/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Test DataNotFoundExceptionMapper.
 */
@Test(groups = TestGroup.UNIT)
public class DataNotFoundExceptionMapperTest extends AbstractExceptionMapperTestHelper {

  @Test(dataProvider = "mediaTypes")
  public void test_mapping(final MediaType mediaType) throws Exception {
    final DataNotFoundException ex = new DataNotFoundException("Test message");
    final DataNotFoundExceptionMapper mapper = new DataNotFoundExceptionMapper();
    init(mapper, mediaType);

    final Response test = mapper.toResponse(ex);
    testResult(test, Status.NOT_FOUND, ex);
  }

}
