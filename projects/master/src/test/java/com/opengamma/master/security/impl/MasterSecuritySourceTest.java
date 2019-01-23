/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Collection;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.security.Security;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link MasterSecuritySource}.
 */
@Test(groups = TestGroup.UNIT)
public class MasterSecuritySourceTest {

  private static final ObjectId OID = ObjectId.of("A", "B");
  private static final UniqueId UID = UniqueId.of("A", "B", "V");
  private static final ExternalId ID1 = ExternalId.of("C", "D");
  private static final ExternalId ID2 = ExternalId.of("E", "F");
  private static final ExternalIdBundle BUNDLE = ExternalIdBundle.of(ID1, ID2);
  private static final Instant NOW = Instant.now();
  private static final VersionCorrection VC = VersionCorrection.of(NOW.minusSeconds(2), NOW.minusSeconds(1));

  /**
   * Tests that the underlying master cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullMaster() {
    new MasterSecuritySource(null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests getting a security from the source by unique id.
   */
  public void testGetSecurityUniqueIdFound() {
    final SecurityMaster mock = mock(SecurityMaster.class);

    final SecurityDocument doc = new SecurityDocument(example());
    when(mock.get(UID)).thenReturn(doc);
    final MasterSecuritySource test = new MasterSecuritySource(mock);
    final Security testResult = test.get(UID);
    verify(mock, times(1)).get(UID);

    assertEquals(example(), testResult);
  }

  /**
   * Tests the exception when no security is found for a unique id.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testGetSecurityUniqueIdNotFound() {
    final SecurityMaster mock = mock(SecurityMaster.class);

    when(mock.get(UID)).thenThrow(new DataNotFoundException(""));
    final MasterSecuritySource test = new MasterSecuritySource(mock);
    try {
      test.get(UID);
    } finally {
      verify(mock, times(1)).get(UID);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Tests getting a security from the source by object id.
   */
  public void testGetSecurityObjectIdFound() {
    final SecurityMaster mock = mock(SecurityMaster.class);

    final SecurityDocument doc = new SecurityDocument(example());
    when(mock.get(OID, VC)).thenReturn(doc);
    final MasterSecuritySource test = new MasterSecuritySource(mock);
    final Security testResult = test.get(OID, VC);
    verify(mock, times(1)).get(OID, VC);

    assertEquals(example(), testResult);
  }

  /**
   * Tests the exception when no security is found for an object id.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testGetSecurityObjectIdNotFound() {
    final SecurityMaster mock = mock(SecurityMaster.class);

    when(mock.get(OID, VC)).thenThrow(new DataNotFoundException(""));
    final MasterSecuritySource test = new MasterSecuritySource(mock);
    try {
      test.get(OID, VC);
    } finally {
      verify(mock, times(1)).get(OID, VC);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Tests getting a security from the source by external id bundle.
   */
  public void testGetExternalIdBundle() {
    final SecurityMaster mock = mock(SecurityMaster.class);
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(ID1);
    request.addExternalId(ID2);
    final ManageableSecurity security = example();
    final SecuritySearchResult result = new SecuritySearchResult();
    result.getDocuments().add(new SecurityDocument(security));

    when(mock.search(request)).thenReturn(result);
    final MasterSecuritySource test = new MasterSecuritySource(mock);
    final Collection<Security> testResult = test.get(BUNDLE);
    verify(mock, times(1)).search(request);

    assertEquals(UID, testResult.iterator().next().getUniqueId());
    assertEquals("Test", testResult.iterator().next().getName());
  }

  /**
   * Tests the exception when no security is found for an external id bundle.
   */
  public void testGetExternalIdBundleVersionCorrection() {
    final SecurityMaster mock = mock(SecurityMaster.class);
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(ID1);
    request.addExternalId(ID2);
    request.setVersionCorrection(VC);
    final ManageableSecurity security = example();
    final SecuritySearchResult result = new SecuritySearchResult();
    result.getDocuments().add(new SecurityDocument(security));

    when(mock.search(request)).thenReturn(result);
    final MasterSecuritySource test = new MasterSecuritySource(mock);
    final Collection<Security> testResult = test.get(BUNDLE, VC);
    verify(mock, times(1)).search(request);

    assertEquals(UID, testResult.iterator().next().getUniqueId());
    assertEquals("Test", testResult.iterator().next().getName());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests getting a single security by external id bundle.
   */
  public void testGetSingleExternalIdBundle() {
    final SecurityMaster mock = mock(SecurityMaster.class);
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(ID1);
    request.addExternalId(ID2);
    final ManageableSecurity security = example();
    final SecuritySearchResult result = new SecuritySearchResult();
    result.getDocuments().add(new SecurityDocument(security));

    when(mock.search(request)).thenReturn(result);
    final MasterSecuritySource test = new MasterSecuritySource(mock);
    final Security testResult = test.getSingle(BUNDLE);
    verify(mock, times(1)).search(request);

    assertEquals(UID, testResult.getUniqueId());
    assertEquals("Test", testResult.getName());
  }

  /**
   * Tests getting a single security by external id bundle.
   */
  public void testGetSingleExternalIdBundleVersionCorrection() {
    final SecurityMaster mock = mock(SecurityMaster.class);
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(ID1);
    request.addExternalId(ID2);
    request.setVersionCorrection(VC);
    final ManageableSecurity security = example();
    final SecuritySearchResult result = new SecuritySearchResult();
    result.getDocuments().add(new SecurityDocument(security));

    when(mock.search(request)).thenReturn(result);
    final MasterSecuritySource test = new MasterSecuritySource(mock);
    final Security testResult = test.getSingle(BUNDLE, VC);
    verify(mock, times(1)).search(request);

    assertEquals(UID, testResult.getUniqueId());
    assertEquals("Test", testResult.getName());
  }

  //-------------------------------------------------------------------------
  private static ManageableSecurity example() {
    return new ManageableSecurity(UID, "Test", "EQUITY", ExternalIdBundle.EMPTY);
  }

}
