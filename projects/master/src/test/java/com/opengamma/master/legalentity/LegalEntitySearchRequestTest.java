/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.legalentity;

import static com.opengamma.test.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.legalentity.LegalEntitySearchRequest.Meta;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link LegalEntitySearchRequest}.
 */
@Test(groups = TestGroup.UNIT)
public class LegalEntitySearchRequestTest extends AbstractFudgeBuilderTestCase {
  private static final ExternalId EID_1 = ExternalId.of("eid", "e1");
  private static final ExternalId EID_2 = ExternalId.of("eid", "e2");
  private static final ExternalIdBundle EIDS = ExternalIdBundle.of(EID_1, EID_2);
  private static final ObjectId OID_1 = ObjectId.of("oid", "o1");
  private static final ObjectId OID_2 = ObjectId.of("oid", "o2");
  private static final String EID_VALUE = "e2";
  private static final String EID_SCHEME = "eid";
  private static final Map<String, String> ATTRIBUTES = new HashMap<>();
  static {
    ATTRIBUTES.put("k1", "v1");
    ATTRIBUTES.put("k2", "v2");
  }
  private static final String NAME = "name";

  /**
   * Tests that an object id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullObjectId() {
    new LegalEntitySearchRequest().addObjectId(null);
  }

  /**
   * Tests adding an object id to the search.
   */
  public void testAddObjectId() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addObjectId(OID_1);
    assertEquals(request.getObjectIds(), Arrays.asList(OID_1));
    request.addObjectId(OID_2);
    assertEquals(request.getObjectIds(), Arrays.asList(OID_1, OID_2));
  }

  /**
   * Tests setting multiple object ids.
   */
  public void testSetObjectIds() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    assertNull(request.getObjectIds());
    final List<ObjectId> oids = Arrays.asList(OID_1, OID_2);
    request.setObjectIds(oids);
    assertEquals(request.getObjectIds(), oids);
    request.setObjectIds(null);
    assertNull(request.getObjectIds());
  }

  /**
   * Tests that an external id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExternalId() {
    new LegalEntitySearchRequest().addExternalId(null);
  }

  /**
   * Tests adding an external id to the search.
   */
  public void testAddExternalId() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    assertNull(request.getExternalIdSearch());
    request.addExternalId(EID_1);
    assertEquals(request.getExternalIdSearch().getSearchType(), ExternalIdSearchType.ANY);
    assertEqualsNoOrder(request.getExternalIdSearch().getExternalIds(), Arrays.asList(EID_1));
    request.addExternalId(EID_2);
    assertEqualsNoOrder(request.getExternalIdSearch().getExternalIds(), Arrays.asList(EID_1, EID_2));
  }

  /**
   * Tests setting multiple external ids.
   */
  public void testSetExternalIdsCollection() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    assertNull(request.getExternalIdSearch());
    final List<ExternalId> eids = Arrays.asList(EID_1, EID_2);
    request.addExternalIds(eids);
    assertEqualsNoOrder(request.getExternalIdSearch().getExternalIds(), eids);
    request.setExternalIdSearch(null);
    assertNull(request.getExternalIdSearch());
  }

  /**
   * Tests setting multiple external ids.
   */
  public void testSetExternalIdsArray() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    assertNull(request.getExternalIdSearch());
    final Set<ExternalId> eids = new HashSet<>(Arrays.asList(EID_1, EID_2));
    request.addExternalIds(eids.toArray(new ExternalId[0]));
    assertEqualsNoOrder(request.getExternalIdSearch().getExternalIds(), eids);
    request.addExternalIds(new ExternalId[] { ExternalId.of("eid", "e3") });
    eids.add(ExternalId.of("eid", "e3"));
    assertEqualsNoOrder(request.getExternalIdSearch().getExternalIds(), eids);
    request.setExternalIdSearch(null);
    assertNull(request.getExternalIdSearch());
  }

  /**
   * Tests setting the search type.
   */
  public void testExternalIdSearchType() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    assertNull(request.getExternalIdSearch());
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    assertEquals(request.getExternalIdSearch().getSearchType(), ExternalIdSearchType.ALL);
    request.setExternalIdSearchType(ExternalIdSearchType.ANY);
    assertEquals(request.getExternalIdSearch().getSearchType(), ExternalIdSearchType.ANY);
  }

  /**
   * Tests that the attribute key cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddNullAttributeKey() {
    new LegalEntitySearchRequest().addAttribute(null, "v");
  }

  /**
   * Tests that the attribute value cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAttributeValue() {
    new LegalEntitySearchRequest().addAttribute("k", null);
  }

  /**
   * Adds an attribute.
   */
  public void testAddAttribute() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    assertTrue(request.getAttributes().isEmpty());
    final Map<String, String> expected = new HashMap<>();
    request.addAttribute("k", "v");
    expected.put("k", "v");
    assertEquals(request.getAttributes(), expected);
    request.addAttribute("v", "k");
    expected.put("v", "k");
    assertEquals(request.getAttributes(), expected);
  }

  /**
   * Tests that the document type must be correct.
   */
  public void testWrongDocumentType() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.matches(new ExchangeDocument());
  }

  /**
   * Tests that an empty request will match on any document.
   */
  public void testEmptyRequest() {
    final ManageableLegalEntity entity = new ManageableLegalEntity();
    entity.setExternalIdBundle(ExternalIdBundle.of("eid", "e3"));
    entity.setName("other");
    final LegalEntityDocument doc = new LegalEntityDocument(entity);
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    assertTrue(request.matches(doc));
  }

  /**
   * Tests a non-matching object id.
   */
  public void testNonMatchingObjectId() {
    final ManageableLegalEntity entity = new ManageableLegalEntity();
    entity.setExternalIdBundle(ExternalIdBundle.of("eid", "e3"));
    entity.setName("other");
    final LegalEntityDocument doc = new LegalEntityDocument(entity);
    doc.setUniqueId(UniqueId.of("uid", "u2"));
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addObjectId(OID_1);
    assertFalse(request.matches(doc));
  }

  /**
   * Tests a matching object id.
   */
  public void testMatchingObjectId() {
    final ManageableLegalEntity entity = new ManageableLegalEntity();
    entity.setExternalIdBundle(ExternalIdBundle.of("eid", "e3"));
    entity.setName("other");
    final LegalEntityDocument doc = new LegalEntityDocument(entity);
    doc.setUniqueId(UniqueId.of(OID_1.getScheme(), OID_1.getValue()));
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setObjectIds(Arrays.asList(OID_1, OID_2));
    assertTrue(request.matches(doc));
  }

  /**
   * Tests a non-matching external id.
   */
  public void testNonMatchingExternalId() {
    final ManageableLegalEntity entity = new ManageableLegalEntity();
    entity.setExternalIdBundle(ExternalIdBundle.of("eid", "e3"));
    entity.setName("other");
    final LegalEntityDocument doc = new LegalEntityDocument(entity);
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(EID_1);
    assertFalse(request.matches(doc));
  }

  /**
   * Tests a matching external id.
   */
  public void testMatchingExternalId() {
    final ManageableLegalEntity entity = new ManageableLegalEntity();
    entity.setExternalIdBundle(EIDS);
    entity.setName("other");
    final LegalEntityDocument doc = new LegalEntityDocument(entity);
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalIds(Arrays.asList(EID_1, EID_2));
    assertTrue(request.matches(doc));
  }

  /**
   * Tests a non-matching name.
   */
  public void testNonMatchingName() {
    final ManageableLegalEntity entity = new ManageableLegalEntity();
    entity.setName("other");
    final LegalEntityDocument doc = new LegalEntityDocument(entity);
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setName(NAME);
    assertFalse(request.matches(doc));
  }

  /**
   * Tests a matching name.
   */
  public void testMatchingName() {
    final ManageableLegalEntity entity = new ManageableLegalEntity();
    entity.setName(NAME);
    final LegalEntityDocument doc = new LegalEntityDocument(entity);
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setName(NAME);
    assertTrue(request.matches(doc));
    request.setName("*" + NAME.charAt(0) + "*");
    assertTrue(request.matches(doc));
  }

  /**
   * Tests a non-matching external id value.
   */
  public void testNonMatchingEidValue() {
    final ManageableLegalEntity entity = new ManageableLegalEntity();
    entity.setExternalIdBundle(EIDS);
    final LegalEntityDocument doc = new LegalEntityDocument(entity);
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setExternalIdValue("e3");
    assertFalse(request.matches(doc));
  }

  /**
   * Tests a matching external id value.
   */
  public void testMatchingEidValue() {
    final ManageableLegalEntity entity = new ManageableLegalEntity();
    entity.setExternalIdBundle(EIDS);
    final LegalEntityDocument doc = new LegalEntityDocument(entity);
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setExternalIdValue("e1");
    assertTrue(request.matches(doc));
    request.setExternalIdValue("*e*");
    assertTrue(request.matches(doc));
  }

  /**
   * Tests a non-matching external id scheme.
   */
  public void testNonMatchingEidScheme() {
    final ManageableLegalEntity entity = new ManageableLegalEntity();
    entity.setExternalIdBundle(EIDS);
    final LegalEntityDocument doc = new LegalEntityDocument(entity);
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setExternalIdScheme("id");
    assertFalse(request.matches(doc));
  }

  /**
   * Tests a matching external id scheme.
   */
  public void testMatchingEidScheme() {
    final ManageableLegalEntity entity = new ManageableLegalEntity();
    entity.setExternalIdBundle(EIDS);
    final LegalEntityDocument doc = new LegalEntityDocument(entity);
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setExternalIdScheme("eid");
    assertTrue(request.matches(doc));
    request.setExternalIdScheme("*e*");
    assertTrue(request.matches(doc));
  }

  /**
   * Tests non-matching attributes.
   */
  public void testNonMatchingAttributes() {
    final ManageableLegalEntity entity = new ManageableLegalEntity();
    entity.setAttributes(ATTRIBUTES);
    final LegalEntityDocument doc = new LegalEntityDocument(entity);
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setAttributes(Collections.singletonMap("k3", "v3"));
    assertFalse(request.matches(doc));
    final Map<String, String> attr = new HashMap<>();
    attr.put("k1", "v1");
    attr.put("k2", "v2");
    attr.put("k3", "v3");
    request.setAttributes(attr);
    // all attributes must match
    assertFalse(request.matches(doc));
  }

  /**
   * Tests matching attributes.
   */
  public void testMatchingAttributes() {
    final ManageableLegalEntity entity = new ManageableLegalEntity();
    entity.setAttributes(ATTRIBUTES);
    final LegalEntityDocument doc = new LegalEntityDocument(entity);
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setAttributes(Collections.singletonMap("k1", "v1"));
    assertTrue(request.matches(doc));
    final Map<String, String> attr = new HashMap<>();
    attr.put("k1", "v1");
    attr.put("k2", "v2");
    request.setAttributes(attr);
    assertTrue(request.matches(doc));
  }

  /**
   * Tests constructor equivalence.
   */
  public void testConstructorEquivalence() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(EID_1);
    LegalEntitySearchRequest other = new LegalEntitySearchRequest(EID_1);
    assertEquals(request, other);
    other = new LegalEntitySearchRequest(EIDS);
    request.addExternalId(EID_2);
    assertEquals(request, other);
  }

  /**
   * Tests the object.
   */
  public void testObject() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    setFields(request);
    final LegalEntitySearchRequest other = new LegalEntitySearchRequest();
    setFields(other);
    assertEquals(request, request);
    assertEquals(request.toString(),
        "LegalEntitySearchRequest{uniqueIdScheme=uid, pagingRequest=PagingRequest[first=0, size=20], "
            + "versionCorrection=VLATEST.CLATEST, objectIds=[oid~o1, oid~o2], externalIdSearch=ExternalIdSearch{externalIds=[eid~e1, eid~e2], "
            + "searchType=NONE}, externalIdValue=e2, externalIdScheme=eid, attributes={k1=v1, k2=v2}, name=name, sortOrder=NAME_DESC}");
    assertEquals(request, other);
    assertEquals(request.hashCode(), other.hashCode());
    other.setAttributes(new HashMap<String, String>());
    assertNotEquals(request, other);
    setFields(other);
    other.setExternalIdScheme(EID_VALUE);
    assertNotEquals(request, other);
    setFields(other);
    other.setExternalIdSearch(ExternalIdSearch.of(EIDS));
    assertNotEquals(request, other);
    setFields(other);
    other.setExternalIdSearchType(ExternalIdSearchType.ALL);
    assertNotEquals(request, other);
    setFields(other);
    other.setName("other");
    assertNotEquals(request, other);
    setFields(other);
    other.setObjectIds(Arrays.asList(OID_1));
    assertNotEquals(request, other);
    setFields(other);
    other.setPagingRequest(PagingRequest.ALL);
    assertNotEquals(request, other);
    setFields(other);
    other.setSortOrder(LegalEntitySearchSortOrder.NAME_ASC);
    assertNotEquals(request, other);
    setFields(other);
    other.setUniqueIdScheme("uid1");
    assertNotEquals(request, other);
    setFields(other);
    other.setVersionCorrection(VersionCorrection.ofVersionAsOf(Instant.now()));
    assertNotEquals(request, other);
  }

  /**
   * Tests the bean.
   */
  public void testBean() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    setFields(request);
    assertEquals(request.propertyNames().size(), 10);
    final Meta bean = LegalEntitySearchRequest.meta();
    assertEquals(bean.attributes().get(request), ATTRIBUTES);
    assertEquals(bean.externalIdScheme().get(request), EID_SCHEME);
    assertEquals(bean.externalIdSearch().get(request), ExternalIdSearch.of(ExternalIdSearchType.NONE, EIDS));
    assertEquals(bean.externalIdValue().get(request), EID_VALUE);
    assertEquals(bean.name().get(request), NAME);
    assertEquals(bean.objectIds().get(request), Arrays.asList(OID_1, OID_2));
    assertEquals(bean.pagingRequest().get(request), PagingRequest.FIRST_PAGE);
    assertEquals(bean.sortOrder().get(request), LegalEntitySearchSortOrder.NAME_DESC);
    assertEquals(bean.uniqueIdScheme().get(request), "uid");
    assertEquals(bean.versionCorrection().get(request), VersionCorrection.LATEST);
    assertEquals(request.property("attributes").get(), ATTRIBUTES);
    assertEquals(request.property("externalIdScheme").get(), EID_SCHEME);
    assertEquals(request.property("externalIdSearch").get(), ExternalIdSearch.of(ExternalIdSearchType.NONE, EIDS));
    assertEquals(request.property("externalIdValue").get(), EID_VALUE);
    assertEquals(request.property("name").get(), NAME);
    assertEquals(request.property("objectIds").get(), Arrays.asList(OID_1, OID_2));
    assertEquals(request.property("pagingRequest").get(), PagingRequest.FIRST_PAGE);
    assertEquals(request.property("sortOrder").get(), LegalEntitySearchSortOrder.NAME_DESC);
    assertEquals(request.property("uniqueIdScheme").get(), "uid");
    assertEquals(request.property("versionCorrection").get(), VersionCorrection.LATEST);
  }

  /**
   * Tests a cycle.
   */
  public void testCycle() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    setFields(request);
    assertEncodeDecodeCycle(LegalEntitySearchRequest.class, request);
  }

  private static void setFields(final LegalEntitySearchRequest request) {
    request.setAttributes(ATTRIBUTES);
    request.setExternalIdScheme(EID_SCHEME);
    request.setExternalIdSearch(ExternalIdSearch.of(ExternalIdSearchType.NONE, EIDS));
    request.setExternalIdValue(EID_VALUE);
    request.setName(NAME);
    request.setObjectIds(Arrays.asList(OID_1, OID_2));
    request.setPagingRequest(PagingRequest.FIRST_PAGE);
    request.setSortOrder(LegalEntitySearchSortOrder.NAME_DESC);
    request.setUniqueIdScheme("uid");
    request.setVersionCorrection(VersionCorrection.LATEST);
  }
}
