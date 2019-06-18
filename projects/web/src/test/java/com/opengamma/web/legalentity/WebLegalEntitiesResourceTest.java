/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.legalentity;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriInfo;

import org.eclipse.jetty.util.ajax.JSON;
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
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.master.legalentity.LegalEntitySearchRequest;
import com.opengamma.master.legalentity.LegalEntitySearchSortOrder;
import com.opengamma.master.legalentity.ManageableLegalEntity;
import com.opengamma.master.legalentity.impl.InMemoryLegalEntityMaster;
import com.opengamma.master.security.impl.InMemorySecurityMaster;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.TestGroup;
import com.opengamma.web.FreemarkerOutputter;
import com.opengamma.web.MockUriInfo;
import com.opengamma.web.user.WebUser;

import freemarker.template.Configuration;

/**
 * Tests for {@link WebLegalEntitiesResource}.
 */
@Test(groups = TestGroup.UNIT)
public class WebLegalEntitiesResourceTest {
  private WebLegalEntitiesResource _legalEntityResource;

  /**
   * Sets up an empty master and the web resource.
   */
  @BeforeMethod
  public void setUp() {
    final InMemoryLegalEntityMaster master = new InMemoryLegalEntityMaster();
    final MockUriInfo uriInfo = new MockUriInfo(true);
    _legalEntityResource = setUpResource(master, uriInfo);
  }

  /**
   * Cleans up the master and web resource.
   */
  @AfterMethod
  public void cleanUp() {
    _legalEntityResource = null;
  }

  /**
   * Tests the HTML GET response.
   */
  public void testHtmlGet() {
    final Integer index = 2;
    final Integer number = 1;
    final Integer size = 4;
    final String name = "legal entity name";
    final String type = "type";
    final List<String> ids = Collections.singletonList("len~1");
    final UriInfo uriInfo = new MockUriInfo(true);
    final String response = _legalEntityResource.getHTML(index, number, size, LegalEntitySearchSortOrder.NAME_ASC.name(), name, type, ids, uriInfo);
    assertNotNull(response);
  }

  /**
   * Tests the JSON GET response.
   */
  @SuppressWarnings("unchecked")
  public void testJsonGetNoLegalEntityAvailable() {
    final Integer index = 2;
    final Integer number = 1;
    final Integer size = 4;
    final String name = "legal entity name";
    final String type = "type";
    final List<String> ids = Collections.singletonList("len~1");
    final UriInfo uriInfo = new MockUriInfo(true);
    final String response = _legalEntityResource.getJSON(index, number, size, LegalEntitySearchSortOrder.NAME_DESC.name(), name, type, ids, uriInfo);
    final Map<String, Object> json = (Map<String, Object>) JSON.parse(response);
    assertEquals(json.get("data"), new Object[0]);
  }

  /**
   * Tests the data in the root bean.
   */
  public void testRootData() {
    final FlexiBean root = _legalEntityResource.createRootData();
    assertEquals(root.propertyNames().size(), 11);
    final String baseUri = (String) root.get("baseUri");
    assertEquals(baseUri, "http://localhost:8080/");
    final WebUser security = (WebUser) root.get("userSecurity");
    assertEquals(security.getSubject().getPrincipal(), "permissive");
    final WebLegalEntityUris uris = (WebLegalEntityUris) root.get("uris");
    assertEquals(uris.legalEntity().getPath(), "/legalentities/null");
    assertEquals(uris.legalEntityVersion().getPath(), "/legalentities/null/versions/");
    final LegalEntitySearchRequest searchRequest = (LegalEntitySearchRequest) root.get("searchRequest");
    assertNull(searchRequest.getExternalIdScheme());
    assertNull(searchRequest.getExternalIdSearch());
    assertNull(searchRequest.getExternalIdValue());
    assertNull(searchRequest.getName());
    assertNull(searchRequest.getObjectIds());
    assertEquals(searchRequest.getPagingRequest(), PagingRequest.ALL);
    assertNull(searchRequest.getUniqueIdScheme());
    assertEquals(searchRequest.getVersionCorrection(), VersionCorrection.LATEST);
  }

  /**
   * Tests the search result data.
   */
  @SuppressWarnings("unchecked")
  public void testSearchResult() {
    final String name = "legal entity name";
    final ExternalIdBundle eids = ExternalIdBundle.of(ExternalId.of("eid1", "1"), ExternalId.of("eid2", "a"));
    final ManageableLegalEntity legalEntity = new ManageableLegalEntity(name, eids);
    final LegalEntityDocument document = new LegalEntityDocument(legalEntity);
    final InMemoryLegalEntityMaster master = new InMemoryLegalEntityMaster();
    master.add(document);
    final MockUriInfo uriInfo = new MockUriInfo(true);
    uriInfo.setQueryParameter("idscheme.0", "eid1");
    uriInfo.setQueryParameter("idvalue.0", "1");
    uriInfo.setQueryParameter("idscheme.1", "eid2");
    uriInfo.setQueryParameter("idvalue.1", "a");
    final WebLegalEntitiesResource resource = setUpResource(master, uriInfo);
    final String response = resource.getJSON(null, null, null, LegalEntitySearchSortOrder.NAME_ASC.name(), name, null,
        Arrays.asList(document.getObjectId().toString()), uriInfo);
    final Map<String, Object> json = (Map<String, Object>) JSON.parse(response);
    final Map<String, Object> header = (Map<String, Object>) json.get("header");
    assertEquals(header.get("type"), "LegalEntities");
    assertArrayEquals((Object[]) header.get("dataFields"), new Object[] { "id", "name" });
    final String data = (String) ((Object[]) json.get("data"))[0];
    assertTrue(data.contains(document.getUniqueId().toString()));
    assertTrue(data.contains(name));
  }

  /**
   * Tests the result of a search.
   */
  public void testFindLegalEntity() {
    final String name = "legal entity name";
    final ExternalIdBundle eids = ExternalIdBundle.of(ExternalId.of("eid1", "1"), ExternalId.of("eid2", "a"));
    final ManageableLegalEntity legalEntity = new ManageableLegalEntity(name, eids);
    final LegalEntityDocument document = new LegalEntityDocument(legalEntity);
    final InMemoryLegalEntityMaster master = new InMemoryLegalEntityMaster();
    master.add(document);
    final MockUriInfo uriInfo = new MockUriInfo(true);
    final WebLegalEntitiesResource resource = setUpResource(master, uriInfo);
    final WebLegalEntityResource legalEntityResource = (WebLegalEntityResource) resource.findLegalEntityHTML(document.getUniqueId().toString());
    assertTrue(JodaBeanUtils.equalIgnoring(legalEntityResource.data().getLegalEntity().getLegalEntity(), document.getLegalEntity(),
        ManageableLegalEntity.meta().uniqueId()));
  }

  /**
   * Tests the result of a search.
   */
  public void testFindLegalEntityHistoryOnly() {
    final String name = "legal entity name";
    final ExternalIdBundle eids = ExternalIdBundle.of(ExternalId.of("eid1", "1"), ExternalId.of("eid2", "a"));
    final ManageableLegalEntity legalEntity = new ManageableLegalEntity(name, eids);
    final LegalEntityDocument document = new LegalEntityDocument(legalEntity);
    final InMemoryLegalEntityMaster master = new InMemoryLegalEntityMaster();
    master.add(document);
    final UniqueId originalId = document.getUniqueId();
    // update the document id so that the history is used
    document.setUniqueId(UniqueId.of(InMemoryLegalEntityMaster.DEFAULT_OID_SCHEME, "new"));
    final MockUriInfo uriInfo = new MockUriInfo(true);
    final WebLegalEntitiesResource resource = setUpResource(master, uriInfo);
    final WebLegalEntityResource legalEntityResource = (WebLegalEntityResource) resource.findLegalEntityHTML(originalId.toString());
    assertTrue(JodaBeanUtils.equalIgnoring(legalEntityResource.data().getLegalEntity().getLegalEntity(), document.getLegalEntity(),
        ManageableLegalEntity.meta().uniqueId()));
  }

  /**
   * Tests the result of a search.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testFindLegalEntityNoResultFromMaster() {
    final InMemoryLegalEntityMaster master = new InMemoryLegalEntityMaster();
    final MockUriInfo uriInfo = new MockUriInfo(true);
    final WebLegalEntitiesResource resource = setUpResource(master, uriInfo);
    resource.findLegalEntityHTML(UniqueId.of("uid", "100").toString());
  }

  /**
   * Tests the URI built when the identifiers are null.
   */
  public void testUriNullIdentifiers() {
    final MockUriInfo uriInfo = new MockUriInfo(true);
    final WebLegalEntityData data = new WebLegalEntityData(uriInfo);
    final URI uri = WebLegalEntitiesResource.uri(data);
    assertEquals(uri.getPath(), "/legalentities");
    assertNull(uri.getQuery());
  }

  private static WebLegalEntitiesResource setUpResource(final InMemoryLegalEntityMaster master, final MockUriInfo uriInfo) {
    final WebLegalEntitiesResource legalEntitiesResource = new WebLegalEntitiesResource(master, new InMemorySecurityMaster());
    legalEntitiesResource.setUriInfo(uriInfo);
    final MockServletContext servletContext = new MockServletContext("/web-engine", new FileSystemResourceLoader());
    final Configuration configuration = FreemarkerOutputter.createConfiguration();
    configuration.setServletContextForTemplateLoading(servletContext, "WEB-INF/pages");
    FreemarkerOutputter.init(servletContext, configuration);
    servletContext.setAttribute(FreemarkerOutputter.class.getName() + ".FreemarkerConfiguration", configuration);
    legalEntitiesResource.setServletContext(servletContext);
    return legalEntitiesResource;
  }
}
