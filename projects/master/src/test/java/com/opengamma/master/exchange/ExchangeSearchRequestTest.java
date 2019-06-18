/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.exchange;

import static com.opengamma.test.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.exchange.ExchangeSearchRequest.Meta;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ExchangeSearchRequest}.
 */
@Test(groups = TestGroup.UNIT)
public class ExchangeSearchRequestTest extends AbstractFudgeBuilderTestCase {
  private static final List<ObjectId> OIDS = Arrays.asList(ObjectId.of("oid", "1"), ObjectId.of("oid", "2"), ObjectId.of("oid", "3"));
  private static final ExternalIdSearch EIDS = ExternalIdSearch.of(ExternalIdSearchType.EXACT, ExternalId.of("eid", "1"), ExternalId.of("eid", "2"),
      ExternalId.of("eid", "3"));
  private static final String NAME = "name";
  private static final ExchangeSearchSortOrder SEARCH_ORDER = ExchangeSearchSortOrder.OBJECT_ID_ASC;
  private static final String UID_SCHEME = "uid";
  private static final VersionCorrection VC = VersionCorrection.of(Instant.ofEpochSecond(1000), Instant.ofEpochSecond(2000));


  /**
   * Tests that the external id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExternalId() {
    new ExchangeSearchRequest((ExternalId) null);
  }

  /**
   * Tests that the external id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExternalIdBUndle() {
    new ExchangeSearchRequest((ExternalIdBundle) null);
  }

  /**
   * Tests that the id to add cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExchangeOid() {
    new ExchangeSearchRequest().addObjectId(null);
  }

  /**
   * Tests that the id to add cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExchangeId() {
    new ExchangeSearchRequest().addExternalId(null);
  }

  /**
   * Tests that the ids to add cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExchangeIdArray() {
    new ExchangeSearchRequest().addExternalIds((ExternalId[]) null);
  }

  /**
   * Tests that the ids to add cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExchangeIdIterable() {
    new ExchangeSearchRequest().addExternalIds((Iterable<ExternalId>) null);
  }

  /**
   * Tests that the search type cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExchangeIdSearchType() {
    new ExchangeSearchRequest().setExternalIdSearchType(null);
  }

  /**
   * Tests the addition of exchange ids.
   */
  public void testAddExternalId() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    assertNull(request.getExternalIdSearch());
    final Set<ExternalId> expected = new HashSet<>(EIDS.getExternalIds());
    final Iterator<ExternalId> iter = expected.iterator();
    for (int i = 0; i < EIDS.size(); i++) {
      final ExternalId next = iter.next();
      request.addExternalId(next);
      assertEquals(request.getExternalIdSearch().getExternalIds().size(), i + 1);
    }
    assertEquals(request.getExternalIdSearch().getSearchType(), ExternalIdSearchType.ANY);
    assertEqualsNoOrder(request.getExternalIdSearch().getExternalIds(), expected);
    final ExternalId eid = ExternalId.of("eid", "4");
    request.addExternalIds(eid);
    assertEquals(request.getExternalIdSearch().getSearchType(), ExternalIdSearchType.ANY);
    expected.add(eid);
    assertEqualsNoOrder(request.getExternalIdSearch().getExternalIds(), expected);
  }

  /**
   * Tests the addition of exchange ids.
   */
  public void testAddExternalIds() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    assertNull(request.getExternalIdSearch());
    final Set<ExternalId> expected = new HashSet<>(EIDS.getExternalIds());
    request.addExternalIds(expected.toArray(new ExternalId[0]));
    assertEquals(request.getExternalIdSearch().getSearchType(), ExternalIdSearchType.ANY);
    assertEqualsNoOrder(request.getExternalIdSearch().getExternalIds(), expected);
    final ExternalId eid = ExternalId.of("eid", "4");
    request.addExternalIds(Arrays.asList(eid));
    assertEquals(request.getExternalIdSearch().getSearchType(), ExternalIdSearchType.ANY);
    expected.add(eid);
    assertEqualsNoOrder(request.getExternalIdSearch().getExternalIds(), expected);
  }

  /**
   * Tests adding null object ids.
   */
  public void testNullExchangeOids() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setObjectIds(null);
    assertNull(request.getObjectIds());
  }

  /**
   * Tests that adding a new object id appends it to the list.
   */
  public void testAddObjectIdAppends() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    setFields(request);
    assertEquals(request.getObjectIds(), OIDS);
    request.addObjectId(OIDS.get(0));
    request.addObjectId(OIDS.get(1));
    final List<ObjectIdentifiable> expected = new ArrayList<ObjectIdentifiable>(OIDS);
    expected.add(OIDS.get(0));
    expected.add(OIDS.get(1));
    assertEquals(request.getObjectIds(), expected);
    request.setObjectIds(null);
    request.addObjectId(OIDS.get(0));
    request.addObjectId(OIDS.get(1));
    assertEquals(request.getObjectIds(), expected.subList(3, 5));
  }

  /**
   * Tests that adding new object ids replaces any previously set.
   */
  public void testSetObjectIdsReplaces() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    setFields(request);
    assertEquals(request.getObjectIds(), OIDS);
    request.setObjectIds(OIDS.subList(0, 1));
    assertEquals(request.getObjectIds(), OIDS.subList(0, 1));
  }

  /**
   * Tests the addition of a search type.
   */
  public void testAddSearchType() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    assertNull(request.getExternalIdSearch());
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    assertEquals(request.getExternalIdSearch(), ExternalIdSearch.of(ExternalIdSearchType.ALL, new ExternalId[0]));
    request.setExternalIdSearchType(ExternalIdSearchType.NONE);
    assertEquals(request.getExternalIdSearch(), ExternalIdSearch.of(ExternalIdSearchType.NONE, new ExternalId[0]));
    request.setExternalIdSearch(null);
    request.addExternalIds(EIDS.getExternalIds());
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    assertEquals(request.getExternalIdSearch(), ExternalIdSearch.of(ExternalIdSearchType.ALL, EIDS.getExternalIds()));
    request.setExternalIdSearchType(ExternalIdSearchType.NONE);
    assertEquals(request.getExternalIdSearch(), ExternalIdSearch.of(ExternalIdSearchType.NONE, EIDS.getExternalIds()));
  }

  /**
   * Tests that documents of the wrong type do not match.
   */
  public void testNoMatchWrongDocumentType() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    setFields(request);
    assertFalse(request.matches(new ConfigDocument(ConfigItem.of(EIDS.getExternalIds().iterator().next()))));
  }

  /**
   * Tests that an empty request matches any document.
   */
  public void testEmptyRequestMatches() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    final ManageableExchange exchange = new ManageableExchange();
    final ExchangeDocument document = new ExchangeDocument(exchange);
    assertTrue(request.matches(document));
  }

  /**
   * Tests matching on object ids.
   */
  public void testObjectIdMatching() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setObjectIds(OIDS);
    final ManageableExchange exchange = new ManageableExchange();
    final ExchangeDocument document = new ExchangeDocument(exchange);
    document.setUniqueId(UniqueId.of(OIDS.get(0).getScheme(), OIDS.get(0).getValue()));
    assertTrue(request.matches(document));
    document.setUniqueId(UniqueId.of("other", "1"));
    assertFalse(request.matches(document));
  }

  /**
   * Tests matching on external ids.
   */
  public void testExternalIdMatching() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setExternalIdSearch(EIDS);
    final ManageableExchange exchange = new ManageableExchange();
    exchange.setExternalIdBundle(ExternalIdBundle.of(EIDS.getExternalIds()));
    final ExchangeDocument document = new ExchangeDocument(exchange);
    assertTrue(request.matches(document));
    exchange.setExternalIdBundle(ExternalIdBundle.of("other", "1"));
    assertFalse(request.matches(document));
  }

  /**
   * Tests matching on name.
   */
  public void testNameMatching() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setName(NAME);
    final ManageableExchange exchange = new ManageableExchange();
    exchange.setName(NAME);
    final ExchangeDocument document = new ExchangeDocument(exchange);
    assertTrue(request.matches(document));
    request.setName("*" + NAME.charAt(0) + "*");
    assertTrue(request.matches(document));
    exchange.setName("other");
    assertFalse(request.matches(document));
  }

  /**
   * Tests the default search order.
   */
  public void testDefaultSearchOrder() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    assertEquals(request.getSortOrder(), ExchangeSearchSortOrder.OBJECT_ID_ASC);
  }

  /**
   * Tests the object.
   */
  public void testObject() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    setFields(request);
    ExchangeSearchRequest other = new ExchangeSearchRequest();
    setFields(other);
    assertEquals(request, request);
    assertEquals(request.toString(),
        "ExchangeSearchRequest{uniqueIdScheme=uid, pagingRequest=PagingRequest[first=0, size=0], "
            + "versionCorrection=V1970-01-01T00:16:40Z.C1970-01-01T00:33:20Z, objectIds=[oid~1, oid~2, oid~3], "
            + "externalIdSearch=ExternalIdSearch{externalIds=[eid~1, eid~2, eid~3], searchType=EXACT}, " + "name=name, sortOrder=OBJECT_ID_ASC}");
    assertEquals(request, other);
    assertEquals(request.hashCode(), other.hashCode());
    other.setExternalIdSearch(ExternalIdSearch.of());
    assertNotEquals(request, other);
    setFields(other);
    other.setName(UID_SCHEME);
    assertNotEquals(request, other);
    setFields(other);
    other.setObjectIds(Arrays.asList(OIDS.get(0)));
    assertNotEquals(request, other);
    setFields(other);
    other.setPagingRequest(PagingRequest.FIRST_PAGE);
    assertNotEquals(request, other);
    setFields(other);
    other.setSortOrder(ExchangeSearchSortOrder.NAME_ASC);
    assertNotEquals(request, other);
    other = new ExchangeSearchRequest();
    other.setUniqueIdScheme(NAME);
    assertNotEquals(request, other);
    setFields(other);
    other.setVersionCorrection(VersionCorrection.LATEST);
    assertNotEquals(request, other);
  }

  /**
   * Tests the bean.
   */
  public void testBean() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    setFields(request);
    assertEquals(request.propertyNames().size(), 7);
    final Meta bean = request.metaBean();
    assertEquals(bean.externalIdSearch().get(request), EIDS);
    assertEquals(bean.name().get(request), NAME);
    assertEquals(bean.objectIds().get(request), OIDS);
    assertEquals(bean.pagingRequest().get(request), PagingRequest.NONE);
    assertEquals(bean.sortOrder().get(request), SEARCH_ORDER);
    assertEquals(bean.uniqueIdScheme().get(request), UID_SCHEME);
    assertEquals(bean.versionCorrection().get(request), VC);
    assertEquals(request.property("externalIdSearch").get(), EIDS);
    assertEquals(request.property("name").get(), NAME);
    assertEquals(request.property("objectIds").get(), OIDS);
    assertEquals(request.property("pagingRequest").get(), PagingRequest.NONE);
    assertEquals(request.property("sortOrder").get(), SEARCH_ORDER);
    assertEquals(request.property("uniqueIdScheme").get(), UID_SCHEME);
    assertEquals(request.property("versionCorrection").get(), VC);
  }

  /**
   * Tests a cycle.
   */
  public void testCycle() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    setFields(request);
    assertEncodeDecodeCycle(ExchangeSearchRequest.class, request);
  }

  private static void setFields(final ExchangeSearchRequest request) {
    request.setExternalIdSearch(EIDS);
    request.setName(NAME);
    request.setObjectIds(OIDS);
    request.setPagingRequest(PagingRequest.NONE);
    request.setSortOrder(SEARCH_ORDER);
    request.setUniqueIdScheme(UID_SCHEME);
    request.setVersionCorrection(VC);
  }
}
