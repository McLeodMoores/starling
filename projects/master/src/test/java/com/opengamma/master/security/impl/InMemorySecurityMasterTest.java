/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;

import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Supplier;
import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdSupplier;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link InMemorySecurityMaster}.
 */
@Test(groups = TestGroup.UNIT)
public class InMemorySecurityMasterTest {

  // TODO Move the logical tests from here to the generic SecurityMasterTestCase then we can just extend from that

  private static final UniqueId OTHER_UID = UniqueId.of("U", "1");
  private static final ExternalId ID1 = ExternalId.of("A", "B");
  private static final ExternalId ID2 = ExternalId.of("A", "C");
  private static final ExternalIdBundle BUNDLE1 = ExternalIdBundle.of(ID1);
  private static final ExternalIdBundle BUNDLE2 = ExternalIdBundle.of(ID2);
  private static final ManageableSecurity SEC1 = new ManageableSecurity(UniqueId.of("Test", "sec1"), "Test 1", "TYPE1", BUNDLE1);
  private static final ManageableSecurity SEC2 = new ManageableSecurity(UniqueId.of("Test", "sec2"), "Test 2", "TYPE2", BUNDLE2);

  private InMemorySecurityMaster testEmpty;
  private InMemorySecurityMaster testPopulated;
  private SecurityDocument doc1;
  private SecurityDocument doc2;

  /**
   * Sets up the master before each method.
   */
  @BeforeMethod
  public void setUp() {
    testEmpty = new InMemorySecurityMaster(new ObjectIdSupplier("Test"));
    testPopulated = new InMemorySecurityMaster(new ObjectIdSupplier("Test"));
    doc1 = new SecurityDocument();
    doc1.setSecurity(SEC1);
    doc1 = testPopulated.add(doc1);
    doc2 = new SecurityDocument();
    doc2.setSecurity(SEC2);
    doc2 = testPopulated.add(doc2);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that the id supplier cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullSupplier() {
    new InMemorySecurityMaster((Supplier<ObjectId>) null);
  }

  /**
   * Tests the default id supplier.
   */
  public void testDefaultSupplier() {
    final InMemorySecurityMaster master = new InMemorySecurityMaster();
    final SecurityDocument doc = new SecurityDocument();
    doc.setSecurity(SEC1);
    final SecurityDocument added = master.add(doc);
    assertEquals("MemSec", added.getUniqueId().getScheme());
  }

  /**
   * Tests that an alternate supplier is used if available.
   */
  public void testAlternateSupplier() {
    final InMemorySecurityMaster master = new InMemorySecurityMaster(new ObjectIdSupplier("Hello"));
    final SecurityDocument doc = new SecurityDocument();
    doc.setSecurity(SEC1);
    final SecurityDocument added = master.add(doc);
    assertEquals("Hello", added.getUniqueId().getScheme());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the result of a search on an empty master.
   */
  public void testSearchEmptyMaster() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    final SecuritySearchResult result = testEmpty.search(request);
    assertEquals(0, result.getPaging().getTotalItems());
    assertEquals(0, result.getDocuments().size());
  }

  /**
   * Tests the result of a search on a populated master.
   */
  public void testSearchPopulatedMasterAll() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    final SecuritySearchResult result = testPopulated.search(request);
    assertEquals(2, result.getPaging().getTotalItems());
    final List<SecurityDocument> docs = result.getDocuments();
    assertEquals(2, docs.size());
    assertEquals(true, docs.contains(doc1));
    assertEquals(true, docs.contains(doc2));
  }

  /**
   * Tests the result of a search that is filtered by an external id bundle.
   */
  public void testSearchPopulatedMasterFilterByBundle() {
    final SecuritySearchRequest request = new SecuritySearchRequest(BUNDLE1);
    final SecuritySearchResult result = testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    assertEquals(1, result.getDocuments().size());
    assertEquals(true, result.getDocuments().contains(doc1));
  }

  /**
   * Tests the result of a search that is filtered by an external id bundle.
   */
  public void testSearchPopulatedMasterFilterByBundleBoth() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalIds(BUNDLE1);
    request.addExternalIds(BUNDLE2);
    final SecuritySearchResult result = testPopulated.search(request);
    assertEquals(2, result.getPaging().getTotalItems());
    final List<SecurityDocument> docs = result.getDocuments();
    assertEquals(2, docs.size());
    assertEquals(true, docs.contains(doc1));
    assertEquals(true, docs.contains(doc2));
  }

  /**
   * Tests the result of a search that is filtered by name.
   */
  public void testSearchPopulatedMasterFilterByName() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setName("*est 2");
    final SecuritySearchResult result = testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    final List<SecurityDocument> docs = result.getDocuments();
    assertEquals(1, docs.size());
    assertEquals(true, docs.contains(doc2));
  }

  /**
   * Tests the result of a search that is filtered by type.
   */
  public void testSearchPopulatedMasterFilterByType() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setSecurityType("TYPE2");
    final SecuritySearchResult result = testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    final List<SecurityDocument> docs = result.getDocuments();
    assertEquals(1, docs.size());
    assertEquals(true, docs.contains(doc2));
  }

  /**
   * Tests the result of a search that is filtered by an external id value.
   */
  public void testSearchPopluatedMasterFilterByExternalIdValue() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setExternalIdValue("B");
    final SecuritySearchResult result = testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    final List<SecurityDocument> docs = result.getDocuments();
    assertEquals(1, docs.size());
    assertEquals(true, docs.contains(doc1));
  }

  /**
   * Tests the result of a search that is filtered by an external id bundle.
   */
  public void testSearchPopluatedMasterFilterByExternalIdValueCase() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setExternalIdValue("b");
    final SecuritySearchResult result = testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    final List<SecurityDocument> docs = result.getDocuments();
    assertEquals(1, docs.size());
    assertEquals(true, docs.contains(doc1));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the exception when no securities can be found.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testGetEmptyMaster() {
    assertNull(testEmpty.get(OTHER_UID));
  }

  /**
   * Tests getting securities from a populated master.
   */
  public void testGetPopulatedMaster() {
    assertSame(doc1, testPopulated.get(doc1.getUniqueId()));
    assertSame(doc2, testPopulated.get(doc2.getUniqueId()));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests adding securities to a populated master.
   */
  public void testAddEmptyMaster() {
    final SecurityDocument doc = new SecurityDocument();
    doc.setSecurity(SEC1);
    final SecurityDocument added = testEmpty.add(doc);
    assertNotNull(added.getVersionFromInstant());
    assertNotNull(added.getCorrectionFromInstant());
    assertEquals(added.getVersionFromInstant(), added.getCorrectionFromInstant());
    assertEquals("Test", added.getUniqueId().getScheme());
    assertSame(SEC1, added.getSecurity());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the exception when trying to update a security that is not present in
   * the master.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testUpdateEmptyMaster() {
    final SecurityDocument doc = new SecurityDocument();
    doc.setSecurity(SEC1);
    doc.setUniqueId(OTHER_UID);
    testEmpty.update(doc);
  }

  /**
   * Tests updating a security.
   */
  public void testUpdatePopulatedMaster() {
    final SecurityDocument doc = new SecurityDocument();
    doc.setSecurity(SEC1);
    doc.setUniqueId(doc1.getUniqueId());
    final SecurityDocument updated = testPopulated.update(doc);
    assertEquals(doc1.getUniqueId(), updated.getUniqueId());
    assertNotNull(doc1.getVersionFromInstant());
    assertNotNull(updated.getVersionFromInstant());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the exception when trying to remove using an id that does not have a
   * corresponding security.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testRemoveEmptyMaster() {
    testEmpty.remove(OTHER_UID);
  }

  /**
   * Tests the removal of a security.
   */
  public void testRemovePopulatedMaster() {
    testPopulated.remove(doc1.getUniqueId());
    final SecuritySearchRequest request = new SecuritySearchRequest();
    final SecuritySearchResult result = testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    final List<SecurityDocument> docs = result.getDocuments();
    assertEquals(1, docs.size());
    assertEquals(true, docs.contains(doc2));
  }

}
