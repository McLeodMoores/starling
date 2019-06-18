/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.legalentity;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.legalentity.LegalEntitySearchResult.Meta;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link LegalEntitySearchResult}.
 */
@Test(groups = TestGroup.UNIT)
public class LegalEntitySearchResultTest extends AbstractFudgeBuilderTestCase {
  private static final ManageableLegalEntity ENTITY_1 = new ManageableLegalEntity();
  private static final ManageableLegalEntity ENTITY_2 = new ManageableLegalEntity();
  private static final ManageableLegalEntity ENTITY_3 = new ManageableLegalEntity();
  private static final LegalEntityDocument DOC_1 = new LegalEntityDocument();
  private static final LegalEntityDocument DOC_2 = new LegalEntityDocument();
  private static final LegalEntityDocument DOC_3 = new LegalEntityDocument();
  static {
    ENTITY_1.setName("one");
    ENTITY_2.setName("two");
    ENTITY_3.setName("three");
    DOC_1.setLegalEntity(ENTITY_1);
    DOC_2.setLegalEntity(ENTITY_2);
    DOC_3.setLegalEntity(ENTITY_3);
    DOC_1.setUniqueId(UniqueId.of("uid", "1"));
    DOC_2.setUniqueId(UniqueId.of("uid", "2"));
    DOC_3.setUniqueId(UniqueId.of("uid", "3"));
  }
  private static final List<LegalEntityDocument> DOCS = Arrays.asList(DOC_1, DOC_2, DOC_3);
  private static final VersionCorrection VC = VersionCorrection.of(Instant.ofEpochSecond(1000), Instant.ofEpochSecond(1500));

  /**
   * Tests that the documents can be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullDocumentsConstructor() {
    new LegalEntitySearchResult((Collection<LegalEntityDocument>) null);
  }

  /**
   * Tests that the documents cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDocumentsSetter() {
    new LegalEntitySearchResult().setDocuments(null);
  }

  /**
   * Tests that the version correction cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVersionCorrectionConstructor() {
    new LegalEntitySearchResult((VersionCorrection) null);
  }

  /**
   * Tests that the version correction cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVersionCorrectionSetter() {
    new LegalEntitySearchResult().setVersionCorrection(null);
  }

  /**
   * Tests retrieval of the legal entities.
   */
  public void testGetLegalEntities() {
    final LegalEntitySearchResult result = new LegalEntitySearchResult(DOCS);
    final List<ManageableLegalEntity> entities = result.getLegalEntities();
    assertEquals(entities, Arrays.asList(ENTITY_1, ENTITY_2, ENTITY_3));
  }

  /**
   * Tests getting the first entity.
   */
  public void testGetFirstLegalEntityNoDocuments() {
    final LegalEntitySearchResult result = new LegalEntitySearchResult();
    assertNull(result.getFirstLegalEntity());
  }

  /**
   * Tests getting the first entity.
   */
  public void testGetFirstLegalEntity() {
    final LegalEntitySearchResult result = new LegalEntitySearchResult(DOCS);
    assertEquals(result.getFirstLegalEntity(), ENTITY_1);
  }

  /**
   * Tests that there must be one document available.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testGetSingleLegalEntityNoDocuments() {
    new LegalEntitySearchResult().getSingleLegalEntity();
  }

  /**
   * Tests that there must be one document available.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testGetSingleLegalEntityMultipleDocuments() {
    new LegalEntitySearchResult(DOCS).getSingleLegalEntity();
  }

  /**
   * Tests the object.
   */
  public void testObject() {
    final LegalEntitySearchResult result = new LegalEntitySearchResult(DOCS);
    result.setVersionCorrection(VC);
    final LegalEntitySearchResult other = new LegalEntitySearchResult(DOCS);
    other.setVersionCorrection(VC);
    assertEquals(result, result);
    assertEquals(result.toString(),
        "LegalEntitySearchResult{paging=Paging[first=0, size=2147483647, totalItems=3], "
            + "documents=[LegalEntityDocument{versionFromInstant=null, versionToInstant=null, correctionFromInstant=null, "
            + "correctionToInstant=null, legalEntity=ManageableLegalEntity{uniqueId=null, externalIdBundle=Bundle[], "
            + "attributes={}, details={}, name=one, ratings=[], capabilities=[], issuedSecurities=[], obligations=[], "
            + "rootPortfolio=null, accounts=[]}, uniqueId=uid~1}, LegalEntityDocument{versionFromInstant=null, versionToInstant=null, "
            + "correctionFromInstant=null, correctionToInstant=null, legalEntity=ManageableLegalEntity{uniqueId=null, "
            + "externalIdBundle=Bundle[], attributes={}, details={}, name=two, ratings=[], capabilities=[], issuedSecurities=[], "
            + "obligations=[], rootPortfolio=null, accounts=[]}, uniqueId=uid~2}, LegalEntityDocument{versionFromInstant=null, "
            + "versionToInstant=null, correctionFromInstant=null, correctionToInstant=null, legalEntity=ManageableLegalEntity{uniqueId=null, "
            + "externalIdBundle=Bundle[], attributes={}, details={}, name=three, ratings=[], capabilities=[], issuedSecurities=[], "
            + "obligations=[], rootPortfolio=null, accounts=[]}, uniqueId=uid~3}], versionCorrection=V1970-01-01T00:16:40Z.C1970-01-01T00:25:00Z}");
    assertEquals(result, other);
    assertEquals(result.hashCode(), other.hashCode());
    other.setDocuments(DOCS.subList(0, 1));
    assertNotEquals(result, other);
    other.setDocuments(DOCS);
    other.setPaging(Paging.of(PagingRequest.FIRST_PAGE, DOCS));
    assertNotEquals(result, other);
    other.setPaging(Paging.ofAll(DOCS));
    other.setVersionCorrection(VersionCorrection.LATEST);
    assertNotEquals(result, other);
  }

  /**
   * Tests the bean.
   */
  public void testBean() {
    final LegalEntitySearchResult result = new LegalEntitySearchResult(DOCS);
    assertEquals(result.propertyNames().size(), 3);
    final Meta bean = LegalEntitySearchResult.meta();
    assertEquals(bean.documents().get(result), DOCS);
    assertEquals(bean.paging().get(result), Paging.ofAll(DOCS));
    assertEquals(bean.versionCorrection().get(result), VersionCorrection.LATEST);
    assertEquals(result.property("documents").get(), DOCS);
    assertEquals(result.property("paging").get(), Paging.ofAll(DOCS));
    assertEquals(result.property("versionCorrection").get(), VersionCorrection.LATEST);
  }

  /**
   * Tests a cycle.
   */
  public void testCycle() {
    final LegalEntitySearchResult result = new LegalEntitySearchResult(DOCS);
    result.setVersionCorrection(VC);
    assertEncodeDecodeCycle(LegalEntitySearchResult.class, result);
  }
}
