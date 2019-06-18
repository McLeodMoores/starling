/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.region;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriInfo;

import org.eclipse.jetty.util.ajax.JSON;
import org.joda.beans.impl.flexi.FlexiBean;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockServletContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.region.RegionClassification;
import com.opengamma.core.region.impl.SimpleRegion;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionSearchRequest;
import com.opengamma.master.region.impl.InMemoryRegionMaster;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.TestGroup;
import com.opengamma.web.FreemarkerOutputter;
import com.opengamma.web.MockUriInfo;
import com.opengamma.web.user.WebUser;

import freemarker.template.Configuration;

/**
 * Tests for {@link WebRegionsResource}.
 */
@Test(groups = TestGroup.UNIT)
public class WebRegionsResourceTest {
  private WebRegionsResource _regionsResource;

  /**
   * Sets up an empty master and the web resource.
   */
  @BeforeMethod
  public void setUp() {
    final InMemoryRegionMaster master = new InMemoryRegionMaster();
    final MockUriInfo uriInfo = new MockUriInfo(true);
    _regionsResource = setUpResource(master, uriInfo);
  }

  /**
   * Cleans up the master and web resource.
   */
  @AfterMethod
  public void cleanUp() {
    _regionsResource = null;
  }

  /**
   * Tests the HTML GET response.
   */
  public void testHtmlGet() {
    final Integer index = 2;
    final Integer number = 1;
    final Integer size = 4;
    final String name = "region name";
    final RegionClassification classification = RegionClassification.INDEPENDENT_STATE;
    final List<String> ids = Arrays.asList("eid~id1", "eid~id2");
    final UriInfo uriInfo = new MockUriInfo(true);
    final String response = _regionsResource.getHTML(index, number, size, name, classification, ids, uriInfo);
    assertNotNull(response);
  }

  /**
   * Tests the JSON GET response.
   */
  @SuppressWarnings("unchecked")
  public void testJsonGetNoRegionAvailable() {
    final Integer index = 2;
    final Integer number = 1;
    final Integer size = 4;
    final String name = "region name";
    final RegionClassification classification = RegionClassification.INDEPENDENT_STATE;
    final List<String> ids = Arrays.asList("eid~id1", "eid~id2");
    final UriInfo uriInfo = new MockUriInfo(true);
    final String response = _regionsResource.getJSON(index, number, size, name, classification, ids, uriInfo);
    final Map<String, Object> json = (Map<String, Object>) JSON.parse(response);
    assertEquals(json.get("data"), new Object[0]);
  }

  /**
   * Tests the data in the root bean.
   */
  public void testRootData() {
    final FlexiBean root = _regionsResource.createRootData();
    assertEquals(root.propertyNames().size(), 11);
    final String baseUri = (String) root.get("baseUri");
    assertEquals(baseUri, "http://localhost:8080/");
    final WebUser security = (WebUser) root.get("userSecurity");
    assertEquals(security.getSubject().getPrincipal(), "permissive");
    final WebRegionUris uris = (WebRegionUris) root.get("uris");
    assertEquals(uris.region().getPath(), "/regions/null");
    assertEquals(uris.regionVersion().getPath(), "/regions/null/versions/");
    final RegionSearchRequest searchRequest = (RegionSearchRequest) root.get("searchRequest");
    assertNull(searchRequest.getChildrenOfId());
    assertNull(searchRequest.getClassification());
    assertNull(searchRequest.getExternalIdSearch());
    assertNull(searchRequest.getName());
    assertNull(searchRequest.getObjectIds());
    assertEquals(searchRequest.getPagingRequest(), PagingRequest.ALL);
    assertNull(searchRequest.getProviderId());
    assertNull(searchRequest.getUniqueIdScheme());
    assertEquals(searchRequest.getVersionCorrection(), VersionCorrection.LATEST);
  }

  /**
   * Tests the search result data.
   */
  @SuppressWarnings("unchecked")
  public void testSearchResult() {
    final String name = "region name";
    final RegionClassification classification = RegionClassification.INDEPENDENT_STATE;
    final ExternalIdBundle eids = ExternalIdBundle.of(ExternalId.of("eid1", "1"), ExternalId.of("eid2", "a"));
    final SimpleRegion region = new SimpleRegion();
    region.setExternalIdBundle(eids);
    region.setName(name);
    region.setClassification(classification);
    final RegionDocument document = new RegionDocument(region);
    final InMemoryRegionMaster master = new InMemoryRegionMaster();
    master.add(document);
    final MockUriInfo uriInfo = new MockUriInfo(true);
    uriInfo.setQueryParameter("idscheme.0", "eid1");
    uriInfo.setQueryParameter("idvalue.0", "1");
    uriInfo.setQueryParameter("idscheme.1", "eid2");
    uriInfo.setQueryParameter("idvalue.1", "a");
    final WebRegionsResource resource = setUpResource(master, uriInfo);
    final String response = resource.getJSON(null, null, null, name, classification, Arrays.asList(document.getObjectId().toString()), uriInfo);
    final Map<String, Object> json = (Map<String, Object>) JSON.parse(response);
    final Map<String, Object> header = (Map<String, Object>) json.get("header");
    assertEquals(header.get("type"), "Regions");
    assertArrayEquals((Object[]) header.get("dataFields"), new Object[] { "id", "name", "validFrom" });
    final String data = (String) ((Object[]) json.get("data"))[0];
    assertTrue(data.contains(document.getUniqueId().toString()));
    assertTrue(data.contains(name));
  }

  /**
   * Tests the result of a search.
   */
  public void testFindRegion() {
    final String name = "region name";
    final RegionClassification classification = RegionClassification.INDEPENDENT_STATE;
    final ExternalIdBundle eids = ExternalIdBundle.of(ExternalId.of("eid1", "1"), ExternalId.of("eid2", "a"));
    final SimpleRegion region = new SimpleRegion();
    region.setExternalIdBundle(eids);
    region.setName(name);
    region.setClassification(classification);
    final RegionDocument document = new RegionDocument(region);
    final InMemoryRegionMaster master = new InMemoryRegionMaster();
    master.add(document);
    final MockUriInfo uriInfo = new MockUriInfo(true);
    final WebRegionsResource resource = setUpResource(master, uriInfo);
    final WebRegionResource regionResource = resource.findRegion(document.getUniqueId().toString());
    assertEquals(regionResource.data().getRegion(), document);
  }

  /**
   * Tests the result of a search.
   */
  public void testFindRegionHistoryOnly() {
    final String name = "region name";
    final RegionClassification classification = RegionClassification.INDEPENDENT_STATE;
    final ExternalIdBundle eids = ExternalIdBundle.of(ExternalId.of("eid1", "1"), ExternalId.of("eid2", "a"));
    final SimpleRegion region = new SimpleRegion();
    region.setExternalIdBundle(eids);
    region.setName(name);
    region.setClassification(classification);
    final RegionDocument document = new RegionDocument(region);
    final InMemoryRegionMaster master = new InMemoryRegionMaster();
    master.add(document);
    final UniqueId originalId = document.getUniqueId();
    // update the document id so that the history is used
    document.setUniqueId(UniqueId.of(InMemoryRegionMaster.DEFAULT_OID_SCHEME, "new"));
    final MockUriInfo uriInfo = new MockUriInfo(true);
    final WebRegionsResource resource = setUpResource(master, uriInfo);
    final WebRegionResource regionResource = resource.findRegion(originalId.toString());
    assertEquals(regionResource.data().getRegion(), document);
  }

  /**
   * Tests the result of a search.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testFindRegionNoResultFromMaster() {
    final InMemoryRegionMaster master = new InMemoryRegionMaster();
    final MockUriInfo uriInfo = new MockUriInfo(true);
    final WebRegionsResource resource = setUpResource(master, uriInfo);
    resource.findRegion(UniqueId.of("uid", "100").toString());
  }

  /**
   * Tests the URI built when the identifiers are null.
   */
  public void testUriNullIdentifiers() {
    final MockUriInfo uriInfo = new MockUriInfo(true);
    final WebRegionData data = new WebRegionData(uriInfo);
    final URI uri = WebRegionsResource.uri(data);
    assertEquals(uri.getPath(), "/regions");
    assertNull(uri.getQuery());
  }

  /**
   * Tests the URI built.
   */
  public void testUri() {
    final ExternalIdBundle eids = ExternalIdBundle.of(ExternalId.of("eid1", "1"), ExternalId.of("eid2", "a"));
    final MockUriInfo uriInfo = new MockUriInfo(true);
    final WebRegionData data = new WebRegionData(uriInfo);
    final URI uri = WebRegionsResource.uri(data, eids);
    assertEquals(uri.getPath(), "/regions");
    assertEquals(uri.getQuery(), "&idscheme.0=eid1&idvalue.0=1&idscheme.1=eid2&idvalue.1=a");
  }

  private static WebRegionsResource setUpResource(final InMemoryRegionMaster master, final MockUriInfo uriInfo) {
    final WebRegionsResource regionsResource = new WebRegionsResource(master);
    regionsResource.setUriInfo(uriInfo);
    final MockServletContext servletContext = new MockServletContext("/web-engine", new FileSystemResourceLoader());
    final Configuration configuration = FreemarkerOutputter.createConfiguration();
    configuration.setServletContextForTemplateLoading(servletContext, "WEB-INF/pages");
    FreemarkerOutputter.init(servletContext, configuration);
    servletContext.setAttribute(FreemarkerOutputter.class.getName() + ".FreemarkerConfiguration", configuration);
    regionsResource.setServletContext(servletContext);
    return regionsResource;
  }
}
