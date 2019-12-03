/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.user;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.impl.flexi.FlexiBean;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockServletContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.user.UserAccountStatus;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.user.ManageableUser;
import com.opengamma.master.user.UserSearchRequest;
import com.opengamma.master.user.UserSearchSortOrder;
import com.opengamma.master.user.impl.InMemoryUserMaster;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.TestGroup;
import com.opengamma.web.FreemarkerOutputter;
import com.opengamma.web.MockUriInfo;

import freemarker.template.Configuration;

/**
 * Tests for {@link WebUsersResource}.
 */
@Test(groups = TestGroup.UNIT)
public class WebUsersResourceTest {
  private WebUsersResource _usersResource;

  /**
   * Sets up an empty master and the web resource.
   */
  @BeforeMethod
  public void setUp() {
    final InMemoryUserMaster master = new InMemoryUserMaster();
    final MockUriInfo uriInfo = new MockUriInfo(true);
    _usersResource = setUpResource(master, uriInfo);
  }

  /**
   * Cleans up the master and web resource.
   */
  @AfterMethod
  public void cleanUp() {
    _usersResource = null;
  }

  /**
   * Tests the HTML GET response.
   */
  public void testHtmlGet() {
    final Integer index = 2;
    final Integer number = 1;
    final Integer size = 4;
    final String sort = "NAME ASC";
    final String userName = "userName";
    final String name = "name";
    final String email = "user@test.com";
    final String idScheme = "idScheme";
    final String idValue = "idValue";
    final List<String> ids = Arrays.asList("eid~id1", "eid~id2");
    final UriInfo uriInfo = new MockUriInfo(true);
    final String response = _usersResource.getHTML(index, number, size, sort, userName, name, email, idScheme, idValue, ids, uriInfo);
    assertNotNull(response);
  }

  /**
   * Tests the HTML POST response.
   */
  public void testHtmlPost() {
    final InMemoryUserMaster master = new InMemoryUserMaster();
    final WebUsersResource usersResource = setUpResource(master, new MockUriInfo(true));
    final String userName = "userName";
    final String password = "diagiuIUH-36-58";
    final String email = "user@test.com";
    final String displayName = "display name";
    final String locale = Locale.ENGLISH.toString();
    final String zone = "Europe/London";
    try {
      assertNull(master.getByName(userName));
    } catch (final DataNotFoundException e) {
      // expected
    }
    final Response response = usersResource.postHTML(userName, password, email, displayName, locale, zone, null, null);
    final ManageableUser stored = master.getByName(userName);
    assertEquals(stored.getEmailAddress(), email);
    assertEquals(response.getStatus(), Response.Status.SEE_OTHER.getStatusCode());
    assertEquals(response.getMetadata().get("Location").size(), 1);
    assertEquals(((URI) ((List<?>) response.getMetadata().get("Location")).get(0)).getPath(), "/users/name/" + userName);
  }

  /**
   * Tests the HTML POST response with bad data (a weak password).
   */
  public void testHtmlPostBadData() {
    final InMemoryUserMaster master = new InMemoryUserMaster();
    final WebUsersResource usersResource = setUpResource(master, new MockUriInfo(true));
    final String userName = "userName";
    final String password = "password";
    final String email = "user@test.com";
    final String displayName = "display name";
    final String locale = Locale.ENGLISH.toString();
    final String zone = "Europe/London";
    try {
      assertNull(master.getByName(userName));
    } catch (final DataNotFoundException e) {
      // expected
    }
    final Response response = usersResource.postHTML(userName, password, email, displayName, locale, zone, null, null);
    try {
      assertNull(master.getByName(userName));
    } catch (final DataNotFoundException e) {
      // expected
    }
    assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
  }

  /**
   * Tests the data in the root bean.
   */
  public void testRootData() {
    final FlexiBean root = _usersResource.createRootData();
    assertEquals(root.propertyNames().size(), 11);
    final String baseUri = (String) root.get("baseUri");
    assertEquals(baseUri, "http://localhost:8080/");
    final WebUser user = (WebUser) root.get("userSecurity");
    assertEquals(user.getSubject().getPrincipal(), "permissive");
    final WebUserUris uris = (WebUserUris) root.get("uris");
    assertEquals(uris.user().getPath(), "/users/name/null");
    final UserSearchRequest searchRequest = (UserSearchRequest) root.get("searchRequest");
    assertNull(searchRequest.getAssociatedPermission());
    assertNull(searchRequest.getDisplayName());
    assertNull(searchRequest.getEmailAddress());
    assertNull(searchRequest.getObjectIds());
    assertEquals(searchRequest.getPagingRequest(), PagingRequest.ALL);
    assertEquals(searchRequest.getSortOrder(), UserSearchSortOrder.NAME_ASC);
    assertNull(searchRequest.getUserName());
  }

  /**
   * Tests the search result data.
   */
  public void testSearchResult() {
    final String userName = "userName";
    final String email = "user@test.com";
    final ExternalIdBundle eids = ExternalIdBundle.of(ExternalId.of("eid1", "1"), ExternalId.of("eid2", "a"));
    final ManageableUser user = new ManageableUser(userName);
    user.setEmailAddress(email);
    user.setStatus(UserAccountStatus.LOCKED);
    final InMemoryUserMaster master = new InMemoryUserMaster();
    master.add(user);
    final MockUriInfo uriInfo = new MockUriInfo(true);
    uriInfo.setQueryParameter("idscheme.0", "eid1");
    uriInfo.setQueryParameter("idvalue.0", "1");
    uriInfo.setQueryParameter("idscheme.1", "eid2");
    uriInfo.setQueryParameter("idvalue.1", "a");
    final WebUsersResource resource = setUpResource(master, uriInfo);
    final String response = resource.getHTML(null, null, null, null, userName, null, email, null, null, eids.toStringList(), uriInfo);
    assertTrue(response.contains(userName));
    assertTrue(response.contains(email));
  }

  /**
   * Tests the result of a search.
   */
  public void testFindUser() {
    final String userName = "userName";
    final String email = "user@test.com";
    final ManageableUser user = new ManageableUser(userName);
    user.setEmailAddress(email);
    user.setStatus(UserAccountStatus.LOCKED);
    final InMemoryUserMaster master = new InMemoryUserMaster();
    master.add(user);
    final MockUriInfo uriInfo = new MockUriInfo(true);
    final WebUsersResource resource = setUpResource(master, uriInfo);
    final WebUserResource userResource = resource.findUser(userName);
    // uid is set on add to master
    assertTrue(JodaBeanUtils.equalIgnoring(userResource.data().getUser(), user, ManageableUser.meta().uniqueId()));
  }

  /**
   * Tests the result of a search.
   */
  public void testFindUserHistoryOnly() {
    final String userName = "userName";
    final String email = "user@test.com";
    final ManageableUser user = new ManageableUser(userName);
    user.setEmailAddress(email);
    user.setStatus(UserAccountStatus.LOCKED);
    final InMemoryUserMaster master = new InMemoryUserMaster();
    master.add(user);
    master.removeByName(userName);
    final MockUriInfo uriInfo = new MockUriInfo(true);
    final WebUsersResource resource = setUpResource(master, uriInfo);
    final WebUserResource userResource = resource.findUser(userName);
    // TODO user returned by history is a generic user
    assertNotNull(userResource.data().getUser());
  }

  /**
   * Tests the result of a search.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testFindUserNoResultFromMaster() {
    final InMemoryUserMaster master = new InMemoryUserMaster();
    final MockUriInfo uriInfo = new MockUriInfo(true);
    final WebUsersResource resource = setUpResource(master, uriInfo);
    resource.findUser("userName");
  }

  private static WebUsersResource setUpResource(final InMemoryUserMaster master, final MockUriInfo uriInfo) {
    final WebUsersResource usersResource = new WebUsersResource(master, new DefaultPasswordService());
    usersResource.setUriInfo(uriInfo);
    final MockServletContext servletContext = new MockServletContext("/web-engine", new FileSystemResourceLoader());
    final Configuration configuration = FreemarkerOutputter.createConfiguration();
    configuration.setServletContextForTemplateLoading(servletContext, "WEB-INF/pages");
    FreemarkerOutputter.init(servletContext, configuration);
    servletContext.setAttribute(FreemarkerOutputter.class.getName() + ".FreemarkerConfiguration", configuration);
    usersResource.setServletContext(servletContext);
    return usersResource;
  }

}
