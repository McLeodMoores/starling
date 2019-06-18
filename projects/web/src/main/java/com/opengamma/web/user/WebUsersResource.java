/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.user;

import java.net.URI;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.credential.PasswordService;
import org.joda.beans.impl.flexi.FlexiBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ObjectId;
import com.opengamma.master.user.ManageableUser;
import com.opengamma.master.user.UserEventHistoryRequest;
import com.opengamma.master.user.UserForm;
import com.opengamma.master.user.UserFormError;
import com.opengamma.master.user.UserFormException;
import com.opengamma.master.user.UserMaster;
import com.opengamma.master.user.UserSearchRequest;
import com.opengamma.master.user.UserSearchResult;
import com.opengamma.master.user.UserSearchSortOrder;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;

/**
 * RESTful resource for all users.
 * <p>
 * The users resource represents the whole of a user master.
 */
@Path("/users")
public class WebUsersResource extends AbstractWebUserResource {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(WebUsersResource.class);
  /**
   * The ftl file.
   */
  private static final String USERS_PAGE = HTML_DIR + "users.ftl";
  /**
   * The ftl file.
   */
  private static final String USER_ADD_PAGE = HTML_DIR + "user-add.ftl";

  /**
   * Creates the resource.
   * @param userMaster  the user master, not null
   * @param passwordService  the password service, not null
   */
  public WebUsersResource(final UserMaster userMaster, final PasswordService passwordService) {
    super(userMaster, passwordService);
  }

  //-------------------------------------------------------------------------
  /**
   * Produces an HTML GET request.
   * 
   * @param pgIdx
   *          the paging first-item index, can be null
   * @param pgNum
   *          the paging page, can be null
   * @param pgSze
   *          the page size, can be null
   * @param sort
   *          the sorting type, can be null
   * @param username
   *          the user name, can be null
   * @param name
   *          the name, can be null
   * @param email
   *          the email, can be null
   * @param idScheme
   *          the id scheme, can be null
   * @param idValue
   *          the id value, can be null
   * @param userIdStrs
   *          the identifiers of the user, not null
   * @param uriInfo
   *          the URI info, not null
   * @return the Freemarker output
   */
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML(
      @QueryParam("pgIdx") final Integer pgIdx,
      @QueryParam("pgNum") final Integer pgNum,
      @QueryParam("pgSze") final Integer pgSze,
      @QueryParam("sort") final String sort,
      @QueryParam("username") final String username,
      @QueryParam("name") final String name,
      @QueryParam("email") final String email,
      @QueryParam("idscheme") final String idScheme,
      @QueryParam("idvalue") final String idValue,
      @QueryParam("userId") final List<String> userIdStrs,
      @Context final UriInfo uriInfo) {
    final String trimmedSort = StringUtils.trimToNull(sort);
    final String trimmedUsername = StringUtils.trimToNull(username);
    final String trimmedName = StringUtils.trimToNull(name);
    final String trimmedEmail = StringUtils.trimToNull(email);
    final String trimmedIdScheme = StringUtils.trimToNull(idScheme);
    final String trimmedIdValue = StringUtils.trimToNull(idValue);
    final PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    final UserSearchSortOrder so = buildSortOrder(trimmedSort, UserSearchSortOrder.NAME_ASC);
    final FlexiBean out = createSearchResultData(pr, so, trimmedUsername, trimmedName, trimmedEmail, trimmedIdScheme, trimmedIdValue, userIdStrs, uriInfo);
    return getFreemarker().build(USERS_PAGE, out);
  }

  private FlexiBean createSearchResultData(
      final PagingRequest pr, final UserSearchSortOrder so,
      final String username, final String name, final String email, final String idScheme, final String idValue,
      final List<String> userIdStrs, final UriInfo uriInfo) {
    final FlexiBean out = createRootData();

    final UserSearchRequest searchRequest = new UserSearchRequest();
    searchRequest.setPagingRequest(pr);
    searchRequest.setSortOrder(so);
    searchRequest.setUserName(username);
    searchRequest.setDisplayName(name);
    searchRequest.setEmailAddress(email);
    searchRequest.setAlternateIdScheme(StringUtils.trimToNull(idScheme));
    searchRequest.setAlternateIdValue(StringUtils.trimToNull(idValue));
    for (final String userIdStr : userIdStrs) {
      searchRequest.addObjectId(ObjectId.parse(userIdStr));
    }
    out.put("searchRequest", searchRequest);

    if (data().getUriInfo().getQueryParameters().size() > 0) {
      final UserSearchResult searchResult = data().getUserMaster().search(searchRequest);
      out.put("searchResult", searchResult);
      out.put("paging", new WebPaging(searchResult.getPaging(), uriInfo));
    }
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an HTML POST response.
   * 
   * @param userName
   *          the user name, can be null
   * @param password
   *          the password, can be null
   * @param email
   *          the email, can be null
   * @param displayName
   *          the display name, can be null
   * @param locale
   *          the locale, can be null
   * @param zone
   *          the time zone, can be null
   * @param dateStyle
   *          the date style, can be null
   * @param timeStyle
   *          the time style, can be null
   * @return the Freemarker output
   */
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response postHTML(
      @FormParam("username") final String userName,
      @FormParam("password") final String password,
      @FormParam("email") final String email,
      @FormParam("displayname") final String displayName,
      @FormParam("locale") final String locale,
      @FormParam("timezone") final String zone,
      @FormParam("datestyle") final String dateStyle,
      @FormParam("timestyle") final String timeStyle) {
    try {
      final UserForm form = new UserForm(userName, password, email, displayName, locale, zone, dateStyle, timeStyle);
      final ManageableUser added = form.add(data().getUserMaster(), data().getPasswordService());
      final URI uri = WebUserResource.uri(data(), added.getUserName());
      return Response.seeOther(uri).build();

    } catch (final UserFormException ex) {
      ex.logUnexpected(LOGGER);
      final FlexiBean out = createRootData();
      out.put("username", userName);
      out.put("displayname", displayName);
      out.put("timezone", zone);
      out.put("email", email);
      for (final UserFormError error : ex.getErrors()) {
        out.put("err_" + error.toLowerCamel(), true);
      }
      return Response.ok(getFreemarker().build(USER_ADD_PAGE, out)).build();
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Finds a user by user name. If the user is not present in the master, the
   * latest version in the history is returned. If this is not available, an
   * exception is thrown.
   *
   * @param userName
   *          the user name
   * @return the user resource
   */
  @Path("name/{userName}")
  public WebUserResource findUser(@PathParam("userName") final String userName) {
    data().setUriUserName(userName);
    try {
      final ManageableUser user = data().getUserMaster().getByName(userName);
      data().setUser(user);
    } catch (final DataNotFoundException ex) {
      final UserEventHistoryRequest request = new UserEventHistoryRequest(userName);
      try {
        data().getUserMaster().eventHistory(request);
        final ManageableUser user = new ManageableUser(userName);
        data().setUser(user);
      } catch (final DataNotFoundException ex2) {
        throw ex;
      }
    }
    return new WebUserResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    final FlexiBean out = super.createRootData();
    final UserSearchRequest searchRequest = new UserSearchRequest();
    out.put("searchRequest", searchRequest);
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for users.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebUserData data) {
    final UriBuilder builder = data.getUriInfo().getBaseUriBuilder().path(WebUsersResource.class);
    return builder.build();
  }

}
