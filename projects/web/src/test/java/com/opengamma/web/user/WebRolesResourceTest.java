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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.joda.beans.JodaBeanUtils;
import org.joda.beans.impl.flexi.FlexiBean;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockServletContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.user.ManageableRole;
import com.opengamma.master.user.RoleSearchRequest;
import com.opengamma.master.user.RoleSearchSortOrder;
import com.opengamma.master.user.impl.InMemoryUserMaster;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.TestGroup;
import com.opengamma.web.FreemarkerOutputter;
import com.opengamma.web.MockUriInfo;

import freemarker.template.Configuration;

/**
 * Tests for {@link WebRolesResource}.
 */
@Test(groups = TestGroup.UNIT)
public class WebRolesResourceTest {
  private WebRolesResource _rolesResource;

  /**
   * Sets up an empty master and the web resource.
   */
  @BeforeMethod
  public void setUp() {
    final InMemoryUserMaster master = new InMemoryUserMaster();
    final MockUriInfo uriInfo = new MockUriInfo(true);
    _rolesResource = setUpResource(master, uriInfo);
  }

  /**
   * Cleans up the master and web resource.
   */
  @AfterMethod
  public void cleanUp() {
    _rolesResource = null;
  }

  /**
   * Tests the HTML GET response.
   */
  public void testHtmlGet() {
    final Integer index = 2;
    final Integer number = 1;
    final Integer size = 4;
    final String sort = "NAME ASC";
    final String roleName = "roleName";
    final String name = "name";
    final List<String> ids = Arrays.asList("eid~id1", "eid~id2");
    final UriInfo uriInfo = new MockUriInfo(true);
    final String response = _rolesResource.getHTML(index, number, size, sort, roleName, name, ids, uriInfo);
    assertNotNull(response);
  }

  /**
   * Tests the HTML POST response.
   */
  public void testHtmlPost() {
    final InMemoryUserMaster master = new InMemoryUserMaster();
    final WebRolesResource roleResource = setUpResource(master, new MockUriInfo(true));
    final String roleName = "roleName";
    final String description = "description";
    final String addRoles = "r1,r2,r3";
    final String addPermissions = "p1,p2";
    final String addUsers = "u1,u2";
    try {
      assertNull(master.getByName(roleName));
    } catch (final DataNotFoundException e) {
      // expected
    }
    final Response response = roleResource.postHTML(roleName, description, addRoles, addPermissions, addUsers);
    final ManageableRole stored = master.roleMaster().getByName(roleName);
    assertEquals(stored.getDescription(), description);
    assertEquals(response.getStatus(), Response.Status.SEE_OTHER.getStatusCode());
    assertEquals(response.getMetadata().get("Location").size(), 1);
    assertEquals(((URI) ((List<?>) response.getMetadata().get("Location")).get(0)).getPath(), "/roles/name/" + roleName);
  }

  /**
   * Tests the HTML POST response with invalid role name.
   */
  public void testHtmlPostBadData() {
    final InMemoryUserMaster master = new InMemoryUserMaster();
    final WebRolesResource roleResource = setUpResource(master, new MockUriInfo(true));
    final String roleName = "role name;";
    try {
      assertNull(master.getByName(roleName));
    } catch (final DataNotFoundException e) {
      // expected
    }
    final Response response = roleResource.postHTML(roleName, null, null, null, null);
    try {
      assertNull(master.getByName(roleName));
    } catch (final DataNotFoundException e) {
      // expected
    }
    assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
  }

  /**
   * Tests the data in the root bean.
   */
  public void testRootData() {
    final FlexiBean root = _rolesResource.createRootData();
    assertEquals(root.propertyNames().size(), 11);
    final String baseUri = (String) root.get("baseUri");
    assertEquals(baseUri, "http://localhost:8080/");
    final WebUser role = (WebUser) root.get("userSecurity");
    assertEquals(role.getSubject().getPrincipal(), "permissive");
    final WebRoleUris uris = (WebRoleUris) root.get("uris");
    assertEquals(uris.roles().getPath(), "/roles");
    final RoleSearchRequest searchRequest = (RoleSearchRequest) root.get("searchRequest");
    assertNull(searchRequest.getAssociatedPermission());
    assertNull(searchRequest.getAssociatedRole());
    assertNull(searchRequest.getAssociatedUser());
    assertNull(searchRequest.getObjectIds());
    assertEquals(searchRequest.getPagingRequest(), PagingRequest.ALL);
    assertEquals(searchRequest.getSortOrder(), RoleSearchSortOrder.NAME_ASC);
    assertNull(searchRequest.getRoleName());
  }

  /**
   * Tests the search result data.
   */
  public void testSearchResult() {
    final String roleName = "roleName";
    final ExternalIdBundle eids = ExternalIdBundle.of(ExternalId.of("eid1", "1"), ExternalId.of("eid2", "a"));
    final ManageableRole role = new ManageableRole(roleName);
    final String description = "desc";
    role.setDescription(description);
    final InMemoryUserMaster master = new InMemoryUserMaster();
    master.roleMaster().add(role);
    final MockUriInfo uriInfo = new MockUriInfo(true);
    uriInfo.setQueryParameter("idscheme.0", "eid1");
    uriInfo.setQueryParameter("idvalue.0", "1");
    uriInfo.setQueryParameter("idscheme.1", "eid2");
    uriInfo.setQueryParameter("idvalue.1", "a");
    final WebRolesResource resource = setUpResource(master, uriInfo);
    final String response = resource.getHTML(null, null, null, null, roleName, null, eids.toStringList(), uriInfo);
    assertTrue(response.contains(roleName));
    assertTrue(response.contains(description));
  }

  /**
   * Tests the result of a search.
   */
  public void testFindRole() {
    final String roleName = "roleName";
    final ManageableRole role = new ManageableRole(roleName);
    final InMemoryUserMaster master = new InMemoryUserMaster();
    master.roleMaster().add(role);
    final MockUriInfo uriInfo = new MockUriInfo(true);
    final WebRolesResource resource = setUpResource(master, uriInfo);
    final WebRoleResource userResource = resource.findRole(roleName);
    // uid is set on add to master
    assertTrue(JodaBeanUtils.equalIgnoring(userResource.data().getRole(), role, ManageableRole.meta().uniqueId()));
  }

  /**
   * Tests the result of a search.
   */
  public void testFindRoleHistoryOnly() {
    final String roleName = "roleName";
    final ManageableRole user = new ManageableRole(roleName);
    final InMemoryUserMaster master = new InMemoryUserMaster();
    master.roleMaster().add(user);
    master.roleMaster().removeByName(roleName);
    final MockUriInfo uriInfo = new MockUriInfo(true);
    final WebRolesResource resource = setUpResource(master, uriInfo);
    final WebRoleResource roleResource = resource.findRole(roleName);
    assertNotNull(roleResource.data().getRole());
  }

  /**
   * Tests the result of a search.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testFindRoleNoResultFromMaster() {
    final InMemoryUserMaster master = new InMemoryUserMaster();
    final MockUriInfo uriInfo = new MockUriInfo(true);
    final WebRolesResource resource = setUpResource(master, uriInfo);
    resource.findRole("userName");
  }

  private static WebRolesResource setUpResource(final InMemoryUserMaster master, final MockUriInfo uriInfo) {
    final WebRolesResource usersResource = new WebRolesResource(master);
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
