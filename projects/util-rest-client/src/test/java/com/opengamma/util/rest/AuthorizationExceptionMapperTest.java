/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.AuthorizationException;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Test AuthorizationExceptionMapper.
 */
@Test(groups = TestGroup.UNIT)
public class AuthorizationExceptionMapperTest extends AbstractExceptionMapperTestHelper {

  @Test(dataProvider = "mediaTypes")
  public void test_mapping(final MediaType mediaType) throws Exception {
    final AuthorizationException ex = new AuthorizationException("Test message");
    final AuthorizationExceptionMapper mapper = new AuthorizationExceptionMapper();
    init(mapper, mediaType);

    final Response test = mapper.toResponse(ex);
    testResult(test, Status.FORBIDDEN, ex);
  }

}
