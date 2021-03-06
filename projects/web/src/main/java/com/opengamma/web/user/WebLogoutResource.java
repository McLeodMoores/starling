/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.user;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.session.SessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.auth.AuthUtils;
import com.opengamma.web.AbstractWebResource;
import com.opengamma.web.WebHomeResource;

/**
 * RESTful resource for the logout page.
 */
@Path("/logout")
public class WebLogoutResource extends AbstractWebResource {
  // take control of logout from Shiro for completeness

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(WebLogoutResource.class);

  /**
   * Creates the resource.
   */
  public WebLogoutResource() {
  }

  //-------------------------------------------------------------------------
  @GET
  public Response get(
      @Context final HttpServletRequest hsr,
      @Context final UriInfo uriInfo,
      @QueryParam("redirect") final String redirectUri) {
    final String trimmedRedirectUri = StringUtils.trimToNull(redirectUri);
    try {
      AuthUtils.getSubject().logout();
      hsr.getSession().invalidate();
    } catch (final SessionException ex) {
      LOGGER.debug("Ignoring session exception during logout", ex);
    } catch (final RuntimeException ex) {
      LOGGER.debug("Ignoring unexpected exception during logout", ex);
    }
    URI uri;
    if (trimmedRedirectUri != null) {
      uri = uriInfo.getBaseUri().resolve(trimmedRedirectUri);
    } else {
      uri = WebHomeResource.uri(uriInfo);
    }
    return Response.seeOther(uri).build();
  }

  @POST
  public Response post(
      @Context final HttpServletRequest hsr,
      @Context final UriInfo uriInfo) {
    // allow logout by POST for ease of use
    return get(hsr, uriInfo, null);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this page.
   *
   * @param uriInfo  the uriInfo, not null
   * @return the URI, not null
   */
  public static URI uri(final UriInfo uriInfo) {
    return uriInfo.getBaseUriBuilder().path(WebLogoutResource.class).build();
  }

}
