/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.position;

import static com.opengamma.web.WebResourceTestUtils.assertJSONObjectEquals;
import static com.opengamma.web.WebResourceTestUtils.loadJson;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.impl.flexi.FlexiBean;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.util.JodaBeanSerialization;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.TestGroup;
import com.opengamma.web.MockUriInfo;
import com.opengamma.web.user.WebUser;

/**
 * Test {@link WebPositionsResource}.
 */
@Test(groups = TestGroup.UNIT)
public class WebPositionsResourceTest extends AbstractWebPositionResourceTestCase {

  /**
   * Tests adding a position using JSON.
   *
   * @throws Exception
   *           if there is a problem
   */
  @Test
  public void testAddPositionWithTrades() throws Exception {
    final String tradesJson = getTradesJson();
    final Response response = _webPositionsResource.postJSON("10", SEC_ID.getScheme().getName(), SEC_ID.getValue(), tradesJson, null, null, null);
    assertNotNull(response);
    assertEquals(201, response.getStatus());
    assertEquals("/positions/MemPos~1", getActualURL(response));
    assertPositionAndTrades();
  }

  /**
   * Tests adding a position using HTML.
   *
   * @throws Exception
   *           if there is a problem
   */
  @Test
  public void testAddPositionWithTradesHtml() throws Exception {
    final Response response = _webPositionsResource.postHTML("10", SEC_ID.getScheme().getName(), SEC_ID.getValue(), null, null, null);
    assertNotNull(response);
    assertEquals(303, response.getStatus());
    assertEquals("/positions/MemPos~1", getActualURL(response));
    // trades aren't added
    assertPositionWithNoTrades();
  }

  /**
   * Tests adding a position using JSON.
   *
   * @throws Exception
   *           if there is a problem
   */
  @Test
  public void testAddXmlPositionWithTrades() throws Exception {
    final UniqueId positionId = addPosition();
    final String tradesJson = getTradesJson();
    final ManageablePosition position = _positionMaster.get(positionId).getPosition();
    final String positionXml = JodaBeanSerialization.serializer(false).xmlWriter().write(position);
    final Response response = _webPositionsResource.postJSON("10", SEC_ID.getScheme().getName(), SEC_ID.getValue(), tradesJson, "xml", positionXml,
        positionId.getScheme());
    assertNotNull(response);
    assertEquals(201, response.getStatus());
    final String actualURL = getActualURL(response);
    // uid will be changed after adding
    assertTrue(actualURL.startsWith("/positions/MemPos~"));
    final UniqueId newPositionId = UniqueId.parse(actualURL.split("/")[2]);
    final ManageablePosition stored = _positionMaster.get(newPositionId).getPosition();
    assertTrue(JodaBeanUtils.equalIgnoring(position, stored, ManageablePosition.meta().uniqueId(), ManageablePosition.meta().trades()));
  }

  /**
   * Tests adding a position using HTML.
   *
   * @throws Exception
   *           if there is a problem
   */
  @Test
  public void testAddXmlPositionWithTradesHtml() throws Exception {
    final UniqueId positionId = addPosition();
    final ManageablePosition position = _positionMaster.get(positionId).getPosition();
    final String positionXml = JodaBeanSerialization.serializer(false).xmlWriter().write(position);
    final Response response = _webPositionsResource.postHTML("10", SEC_ID.getScheme().getName(), SEC_ID.getValue(), "xml", positionXml, positionId.getScheme());
    assertNotNull(response);
    assertEquals(303, response.getStatus());
    final String actualURL = getActualURL(response);
    // uid will be changed after adding
    assertTrue(actualURL.startsWith("/positions/MemPos~"));
    final UniqueId newPositionId = UniqueId.parse(actualURL.split("/")[2]);
    final ManageablePosition stored = _positionMaster.get(newPositionId).getPosition();
    assertTrue(JodaBeanUtils.equalIgnoring(position, stored, ManageablePosition.meta().uniqueId(), ManageablePosition.meta().trades()));
  }

  /**
   * Tests the response when the position XML is empty.
   *
   * @throws Exception
   *           if there is a problem
   */
  @Test
  public void testAddPositionWithEmptyXml() throws Exception {
    final String tradesJson = getTradesJson();
    final Response response = _webPositionsResource.postJSON("10", SEC_ID.getScheme().getName(), SEC_ID.getValue(), tradesJson, "xml", "", null);
    assertEquals(400, response.getStatus());
  }

  /**
   * Tests the response when the position XML is empty.
   */
  @Test
  public void testAddPositionWithEmptyXmlHtml() {
    final Response response = _webPositionsResource.postHTML("10", SEC_ID.getScheme().getName(), SEC_ID.getValue(), "xml", "", null);
    assertEquals(200, response.getStatus());
  }

  /**
   * Tests the response when the quantity is empty.
   *
   * @throws Exception
   *           if there is a problem
   */
  @Test
  public void testAddPositionWithEmptyQuantity() throws Exception {
    final String tradesJson = getTradesJson();
    final Response response = _webPositionsResource.postJSON("", SEC_ID.getScheme().getName(), SEC_ID.getValue(), tradesJson, null, null, null);
    assertEquals(400, response.getStatus());
  }

  /**
   * Tests the response when the quantity is empty.
   */
  @Test
  public void testAddPositionWithEmptyQuantityHtml() {
    final Response response = _webPositionsResource.postHTML("", SEC_ID.getScheme().getName(), SEC_ID.getValue(), null, null, null);
    assertEquals(200, response.getStatus());
    assertTrue(((String) response.getEntity()).contains("err"));
  }

  /**
   * Tests the response when the quantity is not numeric.
   *
   * @throws Exception
   *           if there is a problem
   */
  @Test
  public void testAddPositionWithNonNumericQuantity() throws Exception {
    final String tradesJson = getTradesJson();
    final Response response = _webPositionsResource.postJSON("A", SEC_ID.getScheme().getName(), SEC_ID.getValue(), tradesJson, null, null, null);
    assertEquals(400, response.getStatus());
  }

  /**
   * Tests the response when the quantity is not numeric.
   */
  @Test
  public void testAddPositionWithNonNumericQuantityHtml() {
    final Response response = _webPositionsResource.postHTML("A", SEC_ID.getScheme().getName(), SEC_ID.getValue(), null, null, null);
    assertEquals(200, response.getStatus());
    assertTrue(((String) response.getEntity()).contains("err"));
  }

  /**
   * Tests the response when the id scheme is empty.
   *
   * @throws Exception
   *           if there is a problem
   */
  @Test
  public void testAddPositionWithEmptyIdScheme() throws Exception {
    final String tradesJson = getTradesJson();
    final Response response = _webPositionsResource.postJSON("10", "", SEC_ID.getValue(), tradesJson, null, null, null);
    assertEquals(400, response.getStatus());
  }

  /**
   * Tests the response when the id scheme is empty.
   */
  @Test
  public void testAddPositionWithEmptyIdSchemeHtml() {
    final Response response = _webPositionsResource.postHTML("10", "", SEC_ID.getValue(), null, null, null);
    assertEquals(200, response.getStatus());
    assertTrue(((String) response.getEntity()).contains("err"));
  }

  /**
   * Tests the response when the id value is empty.
   *
   * @throws Exception
   *           if there is a problem
   */
  @Test
  public void testAddPositionWithEmptyIdValue() throws Exception {
    final String tradesJson = getTradesJson();
    final Response response = _webPositionsResource.postJSON("10", SEC_ID.getScheme().getName(), "", tradesJson, "", null, null);
    assertEquals(400, response.getStatus());
  }

  /**
   * Tests the response when the id value is empty.
   */
  @Test
  public void testAddPositionWithEmptyIdValueHtml() {
    final Response response = _webPositionsResource.postHTML("10", SEC_ID.getScheme().getName(), "", null, null, null);
    assertEquals(200, response.getStatus());
    assertTrue(((String) response.getEntity()).contains("err"));
  }

  /**
   * Tests the response when there is no security for an id.
   *
   * @throws Exception
   *           if there is a problem
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testAddPositionWithNoSecurity() throws Exception {
    final String tradesJson = getTradesJson();
    _webPositionsResource.postJSON("10", SEC_ID.getScheme().getName(), "XYZ", tradesJson, "", null, null);
  }

  /**
   * Tests the response when there is no security for an id.
   */
  @Test
  public void testAddPositionWithNoSecurityHtml() {
    final Response response = _webPositionsResource.postHTML("10", SEC_ID.getScheme().getName(), "XYZ", "", null, null);
    assertEquals(200, response.getStatus());
    assertTrue(((String) response.getEntity()).contains("err"));
  }

  /**
   * Tests the error when the type is not supported.
   *
   * @throws Exception
   *           if there is a problem
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddPositionUnsupportedType() throws Exception {
    final String tradesJson = getTradesJson();
    _webPositionsResource.postJSON("10", SEC_ID.getScheme().getName(), SEC_ID.getValue(), tradesJson, "json", null, null);
  }

  /**
   * Tests the error when the type is not supported.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddPositionUnsupportedTypeHtml() {
    _webPositionsResource.postHTML("10", SEC_ID.getScheme().getName(), SEC_ID.getValue(), "json", null, null);
  }
  /**
   * Tests adding a position using JSON.
   *
   * @throws Exception
   *           if there is a problem
   */
  @Test
  public void testAddPositionWithEmptyTrades() throws Exception {
    final String tradesJson = EMPTY_TRADES;
    final Response response = _webPositionsResource.postJSON("10", SEC_ID.getScheme().getName(), SEC_ID.getValue(), tradesJson, null, null, null);
    assertNotNull(response);
    assertEquals(201, response.getStatus());
    assertEquals("/positions/MemPos~1", getActualURL(response));
    assertPositionWithNoTrades();
  }

  /**
   * Tests adding a position using HTML.
   *
   * @throws Exception
   *           if there is a problem
   */
  @Test
  public void testAddPositionWithEmptyTradesHtml() throws Exception {
    final Response response = _webPositionsResource.postHTML("10", SEC_ID.getScheme().getName(), SEC_ID.getValue(), null, null, null);
    assertNotNull(response);
    assertEquals(303, response.getStatus());
    assertEquals("/positions/MemPos~1", getActualURL(response));
    assertPositionWithNoTrades();
  }

  /**
   * Tests adding a position using JSON.
   *
   * @throws Exception
   *           if there is a problem
   */
  @Test
  public void testAddPositionWithNoTrades() throws Exception {
    final Response response = _webPositionsResource.postJSON("10", SEC_ID.getScheme().getName(), SEC_ID.getValue(), null, null, null, null);
    assertNotNull(response);
    assertEquals(201, response.getStatus());
    assertEquals("/positions/MemPos~1", getActualURL(response));
    assertPositionWithNoTrades();
  }

  /**
   * Tests adding a position using HTML.
   *
   * @throws Exception
   *           if there is a problem
   */
  @Test
  public void testAddPositionWithNoTradesHtml() throws Exception {
    final Response response = _webPositionsResource.postHTML("10", SEC_ID.getScheme().getName(), SEC_ID.getValue(), null, null, null);
    assertNotNull(response);
    assertEquals(303, response.getStatus());
    assertEquals("/positions/MemPos~1", getActualURL(response));
    assertPositionWithNoTrades();
  }

  /**
   * Tests getting all positions using JSON.
   *
   * @throws Exception
   *           if there is a problem
   */
  @Test
  public void testGetAllPositions() throws Exception {
    populatePositionMaster();
    final MultivaluedMap<String, String> queryParameters = _uriInfo.getQueryParameters();
    queryParameters.putSingle("identifier", StringUtils.EMPTY);
    queryParameters.putSingle("minquantity", StringUtils.EMPTY);
    queryParameters.putSingle("maxquantity", StringUtils.EMPTY);
    queryParameters.put("tradeId", Collections.<String>emptyList());
    queryParameters.put("positionId", Collections.<String>emptyList());

    final String allPositions = _webPositionsResource.getJSON(null, null, null, null, null, null, queryParameters.get("positionId"), queryParameters.get("tradeId"), null);
    assertNotNull(allPositions);
    assertJSONObjectEquals(loadJson("com/opengamma/web/position/allPositionsJson.txt"), new JSONObject(allPositions));
  }

  /**
   * Tests the HTML GET response.
   */
  public void testHtmlGet() {
    final Integer index = 2;
    final Integer number = 1;
    final Integer size = 4;
    final String id = "pos~1";
    final String minQuantity = "10";
    final String maxQuantity = "100";
    final List<String> positionIds = Arrays.asList("eid~id1", "eid~id2");
    final List<String> tradeIds = Arrays.asList("eid~id3", "eid~id4");
    final String scheme = "pos";
    final String response = _webPositionsResource.getHTML(index, number, size, id, minQuantity, maxQuantity, positionIds, tradeIds, scheme);
    assertNotNull(response);
  }

  /**
   * Tests the JSON GET response.
   */
  public void testJsonGet() {
    final Integer index = 2;
    final Integer number = 1;
    final Integer size = 4;
    final String id = "pos~1";
    final String minQuantity = "10";
    final String maxQuantity = "100";
    final List<String> positionIds = Arrays.asList("eid~id1", "eid~id2");
    final List<String> tradeIds = Arrays.asList("eid~id3", "eid~id4");
    final String scheme = "pos";
    final String response = _webPositionsResource.getJSON(index, number, size, id, minQuantity, maxQuantity, positionIds, tradeIds, scheme);
    assertNotNull(response);
  }

  /**
   * Tests the data in the root bean.
   */
  public void testRootData() {
    final FlexiBean root = _webPositionsResource.createRootData();
    assertEquals(root.propertyNames().size(), 13);
    final String baseUri = (String) root.get("baseUri");
    assertEquals(baseUri, "http://localhost:8080/");
    final WebUser user = (WebUser) root.get("userSecurity");
    assertEquals(user.getSubject().getPrincipal(), "permissive");
    final WebPositionsUris uris = (WebPositionsUris) root.get("uris");
    assertEquals(uris.positions().getPath(), "/positions");
    final PositionSearchRequest searchRequest = (PositionSearchRequest) root.get("searchRequest");
    assertNull(searchRequest.getMaxQuantity());
    assertNull(searchRequest.getMinQuantity());
    assertEquals(searchRequest.getPagingRequest(), PagingRequest.ALL);
    assertNull(searchRequest.getPositionObjectIds());
    assertNull(searchRequest.getSecurityIdSearch());
    assertNull(searchRequest.getSecurityIdValue());
    assertNull(searchRequest.getTradeObjectIds());
    assertNull(searchRequest.getTradeProviderId());
    assertNull(searchRequest.getUniqueIdScheme());
    assertEquals(searchRequest.getVersionCorrection(), VersionCorrection.LATEST);
  }

  /**
   * Tests the search result data.
   *
   * @throws JSONException
   *           if there is a problem with the JSON
   */
  public void testSearchResult() throws JSONException {
    final Integer index = 2;
    final Integer number = 1;
    final Integer size = 4;
    final String id = "pos~1";
    final String minQuantity = "10";
    final String maxQuantity = "100";
    final List<String> positionIds = Arrays.asList("eid~id1", "eid~id2");
    final List<String> tradeIds = Arrays.asList("eid~id3", "eid~id4");
    final String scheme = "pos";
    final String response = _webPositionsResource.getJSON(index, number, size, id, minQuantity, maxQuantity, positionIds, tradeIds, scheme);
    final JSONObject result = new JSONObject(response);
    assertEquals(((JSONArray) result.get("data")).length(), 0);
    final JSONObject headers = (JSONObject) result.get("header");
    assertEquals(headers.get("type"), "Positions");
    final JSONArray fields = (JSONArray) headers.get("dataFields");
    assertEquals(fields.length(), 4);
    assertEquals(fields.get(0), "id");
    assertEquals(fields.get(1), "name");
    assertEquals(fields.get(2), "quantity");
    assertEquals(fields.get(3), "trades");
  }

  /**
   * Tests the result of a search.
   */
  public void testFindPosition() {
    final UniqueId positionId = addPosition();
    final WebPositionResource resource = _webPositionsResource.findPosition(positionId.toString());
    assertEquals(resource.data().getPosition().getPosition(), _positionMaster.get(positionId).getPosition());
  }

  /**
   * Tests the URI built when the identifiers are null.
   */
  public void testUriNullIdentifiers() {
    final MockUriInfo uriInfo = new MockUriInfo(true);
    final WebPositionsData data = new WebPositionsData(uriInfo);
    final URI uri = WebPositionsResource.uri(data);
    assertEquals(uri.getPath(), "/positions");
    assertNull(uri.getQuery());
  }

}
