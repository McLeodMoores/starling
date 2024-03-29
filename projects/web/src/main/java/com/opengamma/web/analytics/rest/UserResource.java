/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.shiro.session.SessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.auth.AuthUtils;

/**
 * REST resource for the user sessions. This resource class specifies the endpoints for user requests.
 */
@Path("user")
public class UserResource {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(UserResource.class);

  @GET
  @Path("logout")
  public Response get(@Context final HttpServletRequest hsr) {
    try {
      AuthUtils.getSubject().logout();
      hsr.getSession().invalidate();
    } catch (final SessionException ex) {
      LOGGER.debug("Ignoring session exception during logout", ex);
    } catch (final RuntimeException ex) {
      LOGGER.debug("Ignoring unexpected exception during logout", ex);
    }
    return Response.ok().build();
  }

}
