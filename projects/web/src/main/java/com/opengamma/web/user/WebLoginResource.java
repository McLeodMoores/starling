/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.user;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;
import org.joda.beans.impl.flexi.FlexiBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.auth.AuthUtils;
import com.opengamma.web.AbstractSingletonWebResource;
import com.opengamma.web.WebHomeResource;

/**
 * RESTful resource for the login page.
 */
@Path("/login")
public class WebLoginResource extends AbstractSingletonWebResource {
  // take control of logout from Shiro to enable ftl files

  /**
   * OpenGamma specific header for client IP address.
   */
  private static final String HEADER_X_OPENGAMMA_CLIENT_IP = "X-OPENGAMMA-CLIENT-IP";
  /**
   * General forwarded header for finding IP address.
   */
  private static final String HEADER_X_FORWARDED_FOR = "X-FORWARDED-FOR";

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(WebLoginResource.class);

  // one resource class handles two ftl files
  private static final String LOGIN_GREEN = "users/html/login.ftl";
  private static final String LOGIN_STYLISH = "users/html/login-og.ftl";
  // Key for the login user name
  static final Object LOGIN_USERNAME = WebLoginResource.class.getName() + ".LoginUserName";

  /**
   * Creates the resource.
   */
  public WebLoginResource() {
  }

  // -------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getGreen(
      @Context final HttpServletRequest request,
      @Context final ServletContext servletContext,
      @Context final UriInfo uriInfo) {

    final SavedRequest savedRequest = WebUtils.getSavedRequest(request);
    if (savedRequest != null && savedRequest.getMethod().equalsIgnoreCase(AccessControlFilter.GET_METHOD)) {
      if (savedRequest.getRequestUrl() != null && savedRequest.getRequestUrl().contains("/bundles/fm/prototype/")) {
        return getStylish(servletContext, uriInfo);
      }
    }
    return get(servletContext, uriInfo, LOGIN_GREEN);
  }

  @GET
  @Path("og")
  @Produces(MediaType.TEXT_HTML)
  public String getStylish(
      @Context final ServletContext servletContext,
      @Context final UriInfo uriInfo) {
    return get(servletContext, uriInfo, LOGIN_STYLISH);
  }

  private String get(final ServletContext servletContext, final UriInfo uriInfo, final String ftlFile) {
    final FlexiBean out = createRootData(uriInfo);
    final Subject subject = AuthUtils.getSubject();
    final Session session = subject.getSession(false);
    if (session != null && session.getAttribute(LOGIN_USERNAME) != null) {
      out.put("username", session.getAttribute(LOGIN_USERNAME));
    } else {
      out.put("username", "");
    }
    return getFreemarker(servletContext).build(ftlFile, out);
  }

  // -------------------------------------------------------------------------
  @POST
  @Produces(MediaType.TEXT_HTML)
  public Response loginGreen(
      @Context final HttpServletRequest request,
      @Context final ServletContext servletContext,
      @Context final UriInfo uriInfo,
      @FormParam("username") final String username,
      @FormParam("password") final String password) {
    return login(request, servletContext, uriInfo, username, password, LOGIN_GREEN);
  }

  @POST
  @Path("og")
  @Produces(MediaType.TEXT_HTML)
  public Response loginStylish(
      @Context final HttpServletRequest request,
      @Context final ServletContext servletContext,
      @Context final UriInfo uriInfo,
      @FormParam("username") final String username,
      @FormParam("password") final String password) {
    return login(request, servletContext, uriInfo, username, password, LOGIN_STYLISH);
  }

  private Response login(
      final HttpServletRequest request,
      final ServletContext servletContext,
      final UriInfo uriInfo,
      final String username, final String password,
      final String ftlFile) {
    final String trimmedUsername = StringUtils.trimToNull(username);
    final String trimmedPassword = StringUtils.trimToNull(password);
    if (trimmedUsername == null) {
      return displayError(servletContext, uriInfo, trimmedUsername, ftlFile, "UserNameMissing");
    }
    if (trimmedPassword == null) {
      return displayError(servletContext, uriInfo, trimmedUsername, ftlFile, "PasswordMissing");
    }
    final String ipAddress = findIpAddress(request);
    final UsernamePasswordToken token = new UsernamePasswordToken(trimmedUsername, trimmedPassword, false, ipAddress);
    try {
      final Subject subject = AuthUtils.getSubject();
      subject.login(token);
      token.clear();

      URI successUrl = null;
      final SavedRequest savedRequest = WebUtils.getAndClearSavedRequest(request);
      if (savedRequest != null && savedRequest.getMethod().equalsIgnoreCase(AccessControlFilter.GET_METHOD)) {
        successUrl = uriInfo.getBaseUri().resolve(savedRequest.getRequestUrl());
      } else {
        if (ftlFile.equals(LOGIN_GREEN)) {
          successUrl = WebHomeResource.uri(uriInfo);
        } else {
          successUrl = uriInfo.getBaseUri().resolve("/");
        }
      }
      return Response.seeOther(successUrl).build();

    } catch (final AuthenticationException ex) {
      final String errorCode = StringUtils.substringBeforeLast(ex.getClass().getSimpleName(), "Exception");
      return displayError(servletContext, uriInfo, trimmedUsername, ftlFile, errorCode);
    }
  }

  /**
   * Finds the IP address of the user.
   * <p>
   * This is a generally impossible task. We prefer a specific 'X_OPENGAMMA_CLIENT_IP' header containing a single IP address. If not found, we rely on the
   * generic 'X-FORWARDED-FOR' header. If not found, we rely on the remote host of the servlet request.
   *
   * @param request
   *          the servlet request, not null
   * @return the IP address, not null
   */
  private static String findIpAddress(final HttpServletRequest request) {
    String remoteIp = StringUtils.stripToNull(request.getHeader(HEADER_X_OPENGAMMA_CLIENT_IP));
    if (remoteIp == null) {
      remoteIp = StringUtils.stripToNull(request.getHeader(HEADER_X_FORWARDED_FOR));
      if (remoteIp != null) {
        remoteIp = StringUtils.stripToNull(StringUtils.split(remoteIp, ',')[0]);
        if ("unknown".equalsIgnoreCase(remoteIp)) {
          remoteIp = null;
        }
      }
    }
    if (remoteIp == null) {
      remoteIp = request.getRemoteHost();
    }
    return ensureIpAddressNonLoopback(remoteIp);
  }

  /**
   * Ensures that the IP address is non-local.
   *
   * @param remoteIp
   *          the remote IP address, may be null
   * @return the non-local IP address, not null
   */
  private static String ensureIpAddressNonLoopback(final String remoteIp) {
    try {
      final InetAddress ia = remoteIp != null ? InetAddress.getByName(remoteIp) : null;
      if (ia != null && !ia.isLoopbackAddress()) {
        return remoteIp;
      }
      // search through network interfaces to find reasonable non-loopback IP address
      InetAddress possible = null;
      for (final Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
        final NetworkInterface iface = ifaces.nextElement();
        for (final Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
          final InetAddress inetAddr = inetAddrs.nextElement();
          if (!inetAddr.isLoopbackAddress()) {
            if (inetAddr.isSiteLocalAddress()) {
              return inetAddr.getHostAddress();
            } else if (possible == null) {
              possible = inetAddr;
            }
          }
        }
      }
      if (possible != null) {
        return possible.getHostAddress();
      }
      final InetAddress localHost = InetAddress.getLocalHost();
      if (localHost == null) {
        throw new UnknownHostException("Unknown local host");
      }
      return localHost.getHostAddress();
    } catch (final Exception ex) {
      LOGGER.warn("Unable to obtain suitable local host address", ex);
      return remoteIp != null ? remoteIp : "0:0:0:0:0:0:0:1";
    }
  }

  private Response displayError(final ServletContext servletContext, final UriInfo uriInfo, final String username, final String ftlFile,
      final String errorCode) {
    final FlexiBean out = createRootData(uriInfo);
    out.put("username", username);
    out.put("err_invalidLogin", errorCode);
    return Response.ok(getFreemarker(servletContext).build(ftlFile, out)).build();
  }

  // -------------------------------------------------------------------------
  /**
   * Builds a URI for this page.
   *
   * @param uriInfo
   *          the uriInfo, not null
   * @return the URI, not null
   */
  public static URI uri(final UriInfo uriInfo) {
    return uriInfo.getBaseUriBuilder().path(WebLoginResource.class).build();
  }

}
