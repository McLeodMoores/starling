/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.legalentity.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Collection;

import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.internal.matchers.Any;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.legalentity.LegalEntity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.legalentity.LegalEntitySearchRequest;
import com.opengamma.master.legalentity.LegalEntitySearchResult;
import com.opengamma.master.legalentity.ManageableLegalEntity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link com.opengamma.master.legalentity.impl.MasterLegalEntitySource}.
 */
@Test(groups = TestGroup.UNIT)
public class MasterLegalEntitySourceTest {

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
  public void testConstructor1argNullMaster() {
    new MasterLegalEntitySource(null);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructor2argNullMaster() {
    new MasterLegalEntitySource(null);
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  public void testGetLegalEntityUniqueIdNoOverrideFound() {
    final LegalEntityMaster mock = mock(LegalEntityMaster.class);

    final LegalEntityDocument doc = new LegalEntityDocument(example());
    when(mock.get(UID)).thenReturn(doc);
    final MasterLegalEntitySource test = new MasterLegalEntitySource(mock);
    final LegalEntity testResult = test.get(UID);
    verify(mock, times(1)).get(UID);

    assertEquals(example(), testResult);
  }

  /**
   *
   */
  @Test(expectedExceptions = com.opengamma.DataNotFoundException.class)
  public void testGetLegalEntityNotFound() {
    final LegalEntityMaster mock = mock(LegalEntityMaster.class);

    final ArgumentCaptor<LegalEntitySearchRequest> searchRequest = ArgumentCaptor.forClass(LegalEntitySearchRequest.class);
    final LegalEntitySearchResult searchResult = mock(LegalEntitySearchResult.class);
    when(mock.search(searchRequest.capture())).thenReturn(searchResult);
    final MasterLegalEntitySource test = new MasterLegalEntitySource(mock);
    final LegalEntity testResult = test.getSingle(ExternalId.of("b", "a"));
    assertEquals(example(), testResult);
  }

  /**
   *
   */
  public void testGetLegalEntityUniqueIdFound() {
    final LegalEntityMaster mock = mock(LegalEntityMaster.class);

    final LegalEntityDocument doc = new LegalEntityDocument(example());

    final LegalEntitySearchResult result = new LegalEntitySearchResult();
    result.getDocuments().add(doc);

    when(mock.get(UID)).thenReturn(doc);
    final MasterLegalEntitySource test = new MasterLegalEntitySource(mock);
    final LegalEntity testResult = test.get(UID);
    verify(mock, times(1)).get(UID);
    assertEquals(example(), testResult);
  }

  /**
   *
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testGetLegalEntityUniqueIdNotFound() {
    final LegalEntityMaster mock = mock(LegalEntityMaster.class);

    when(mock.get(UID)).thenThrow(new DataNotFoundException(""));
    final MasterLegalEntitySource test = new MasterLegalEntitySource(mock);
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
  public void testGetLegalEntityObjectIdFound() {
    final LegalEntityMaster mock = mock(LegalEntityMaster.class);

    final LegalEntityDocument doc = new LegalEntityDocument(example());
    when(mock.get(OID, VC)).thenReturn(doc);
    final MasterLegalEntitySource test = new MasterLegalEntitySource(mock);
    final LegalEntity testResult = test.get(OID, VC);
    verify(mock, times(1)).get(OID, VC);

    assertEquals(example(), testResult);
  }

  /**
   *
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testGetLegalEntityObjectIdNotFound() {
    final LegalEntityMaster mock = mock(LegalEntityMaster.class);

    when(mock.get(OID, VC)).thenThrow(new DataNotFoundException(""));
    final MasterLegalEntitySource test = new MasterLegalEntitySource(mock);
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
  @SuppressWarnings("unchecked")
  public void testGetLegalEntitiesByExternalIdBundle() {
    final LegalEntityMaster mock = mock(LegalEntityMaster.class);
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(ID1);
    request.addExternalId(ID2);
    request.setVersionCorrection(VC);
    final ManageableLegalEntity legalentity = example();
    final LegalEntitySearchResult result = new LegalEntitySearchResult();
    result.getDocuments().add(new LegalEntityDocument(legalentity));

    when(mock.search(Matchers.<LegalEntitySearchRequest> argThat(Any.ANY))).thenReturn(result);

    final ArgumentCaptor<LegalEntitySearchRequest> legalEntitySearchRequest = ArgumentCaptor.forClass(LegalEntitySearchRequest.class);

    final MasterLegalEntitySource test = new MasterLegalEntitySource(mock);
    final Collection<LegalEntity> testResult = test.get(BUNDLE);
    verify(mock, times(1)).search(legalEntitySearchRequest.capture());
    assertEquals(request.getAttributes(), legalEntitySearchRequest.getValue().getAttributes());
    assertEquals(request.getExternalIdSearch(), legalEntitySearchRequest.getValue().getExternalIdSearch());
    assertEquals(VersionCorrection.LATEST, legalEntitySearchRequest.getValue().getVersionCorrection());
    assertEquals(UID, testResult.iterator().next().getUniqueId());
    assertEquals("Test", testResult.iterator().next().getName());
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  @SuppressWarnings("unchecked")
  public void testGetLegalEntityExternalId() {
    final LegalEntityMaster mock = mock(LegalEntityMaster.class);
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(ID1);
    request.addExternalId(ID2);
    request.setVersionCorrection(VC);
    final ManageableLegalEntity legalentity = example();
    final LegalEntitySearchResult result = new LegalEntitySearchResult();
    result.getDocuments().add(new LegalEntityDocument(legalentity));

    final ArgumentCaptor<LegalEntitySearchRequest> legalEntitySearchRequest = ArgumentCaptor.forClass(LegalEntitySearchRequest.class);
    when(mock.search(Matchers.<LegalEntitySearchRequest> argThat(Any.ANY))).thenReturn(result);
    final MasterLegalEntitySource test = new MasterLegalEntitySource(mock);
    final LegalEntity testResult = test.getSingle(BUNDLE);
    verify(mock, times(1)).search(legalEntitySearchRequest.capture());

    assertEquals(UID, testResult.getUniqueId());
    assertEquals("Test", testResult.getName());
  }

  // -------------------------------------------------------------------------
  /**
   * @return an example legal entity
   */
  protected ManageableLegalEntity example() {
    return new MockLegalEntity(UID, "Test", ExternalIdBundle.EMPTY, Currency.GBP);
  }

}
