/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.convention.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Collection;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.convention.Convention;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ConventionSearchRequest;
import com.opengamma.master.convention.ConventionSearchResult;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link MasterConventionSource}.
 */
@Test(groups = TestGroup.UNIT)
public class MasterConventionSourceTest {

  private static final ObjectId OID = ObjectId.of("A", "B");
  private static final UniqueId UID = UniqueId.of("A", "B", "V");
  private static final ExternalId ID1 = ExternalId.of("C", "D");
  private static final ExternalId ID2 = ExternalId.of("E", "F");
  private static final ExternalIdBundle BUNDLE = ExternalIdBundle.of(ID1, ID2);
  private static final Instant NOW = Instant.now();
  private static final VersionCorrection VC = VersionCorrection.of(NOW.minusSeconds(2), NOW.minusSeconds(1));

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullMaster() {
    new MasterConventionSource(null);
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  public void testGetConventionUniqueIdNoOverrideFound() {
    final ConventionMaster mock = mock(ConventionMaster.class);

    final ConventionDocument doc = new ConventionDocument(example());
    when(mock.get(UID)).thenReturn(doc);
    final MasterConventionSource test = new MasterConventionSource(mock);
    final Convention testResult = test.get(UID);
    verify(mock, times(1)).get(UID);

    assertEquals(example(), testResult);
  }

  /**
   *
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testGetConventionUniqueIdNotFound() {
    final ConventionMaster mock = mock(ConventionMaster.class);

    when(mock.get(UID)).thenThrow(new DataNotFoundException(""));
    final MasterConventionSource test = new MasterConventionSource(mock);
    try {
      test.get(UID);
    } finally {
      verify(mock, times(1)).get(UID);
    }
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  public void testGetConventionObjectIdFound() {
    final ConventionMaster mock = mock(ConventionMaster.class);

    final ConventionDocument doc = new ConventionDocument(example());
    when(mock.get(OID, VC)).thenReturn(doc);
    final MasterConventionSource test = new MasterConventionSource(mock);
    final Convention testResult = test.get(OID, VC);
    verify(mock, times(1)).get(OID, VC);

    assertEquals(example(), testResult);
  }

  /**
   *
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testGetConventionObjectIdNotFound() {
    final ConventionMaster mock = mock(ConventionMaster.class);

    when(mock.get(OID, VC)).thenThrow(new DataNotFoundException(""));
    final MasterConventionSource test = new MasterConventionSource(mock);
    try {
      test.get(OID, VC);
    } finally {
      verify(mock, times(1)).get(OID, VC);
    }
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  public void testGetExternalIdBundle() {
    final ConventionMaster mock = mock(ConventionMaster.class);
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ID1);
    request.addExternalId(ID2);
    final ManageableConvention convention = example();
    final ConventionSearchResult result = new ConventionSearchResult();
    result.getDocuments().add(new ConventionDocument(convention));

    when(mock.search(request)).thenReturn(result);
    final MasterConventionSource test = new MasterConventionSource(mock);
    final Collection<Convention> testResult = test.get(BUNDLE);
    verify(mock, times(1)).search(request);

    assertEquals(UID, testResult.iterator().next().getUniqueId());
    assertEquals("Test", testResult.iterator().next().getName());
  }

  /**
   *
   */
  public void testGetExternalIdBundleVersionCorrection() {
    final ConventionMaster mock = mock(ConventionMaster.class);
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ID1);
    request.addExternalId(ID2);
    request.setVersionCorrection(VC);
    final ManageableConvention convention = example();
    final ConventionSearchResult result = new ConventionSearchResult();
    result.getDocuments().add(new ConventionDocument(convention));

    when(mock.search(request)).thenReturn(result);
    final MasterConventionSource test = new MasterConventionSource(mock);
    final Collection<Convention> testResult = test.get(BUNDLE, VC);
    verify(mock, times(1)).search(request);

    assertEquals(UID, testResult.iterator().next().getUniqueId());
    assertEquals("Test", testResult.iterator().next().getName());
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  public void testGetSingleExternalIdBundle() {
    final ConventionMaster mock = mock(ConventionMaster.class);
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ID1);
    request.addExternalId(ID2);
    final ManageableConvention convention = example();
    final ConventionSearchResult result = new ConventionSearchResult();
    result.getDocuments().add(new ConventionDocument(convention));

    when(mock.search(request)).thenReturn(result);
    final MasterConventionSource test = new MasterConventionSource(mock);
    final Convention testResult = test.getSingle(BUNDLE);
    verify(mock, times(1)).search(request);

    assertEquals(UID, testResult.getUniqueId());
    assertEquals("Test", testResult.getName());
  }

  /**
   *
   */
  public void testGetSingleExternalIdBundleVersionCorrection() {
    final ConventionMaster mock = mock(ConventionMaster.class);
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ID1);
    request.addExternalId(ID2);
    request.setVersionCorrection(VC);
    final ManageableConvention convention = example();
    final ConventionSearchResult result = new ConventionSearchResult();
    result.getDocuments().add(new ConventionDocument(convention));

    when(mock.search(request)).thenReturn(result);
    final MasterConventionSource test = new MasterConventionSource(mock);
    final Convention testResult = test.getSingle(BUNDLE, VC);
    verify(mock, times(1)).search(request);

    assertEquals(UID, testResult.getUniqueId());
    assertEquals("Test", testResult.getName());
  }

  // -------------------------------------------------------------------------
  /**
   * @return an example convention
   */
  protected ManageableConvention example() {
    return new MockConvention(UID, "Test", ExternalIdBundle.EMPTY, Currency.GBP);
  }

}
