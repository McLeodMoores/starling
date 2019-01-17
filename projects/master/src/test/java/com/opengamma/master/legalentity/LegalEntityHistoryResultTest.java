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

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.UniqueId;
import com.opengamma.master.legalentity.LegalEntityHistoryResult.Meta;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link LegalEntityHistoryResult}.
 */
@Test(groups = TestGroup.UNIT)
public class LegalEntityHistoryResultTest extends AbstractFudgeBuilderTestCase {
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

  /**
   * Tests that the documents can be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullDocumentsConstructor() {
    new LegalEntityHistoryResult((Collection<LegalEntityDocument>) null);
  }

  /**
   * Tests that the documents cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDocumentsSetter() {
    new LegalEntityHistoryResult().setDocuments(null);
  }

  /**
   * Tests retrieval of the legal entities.
   */
  public void testGetLegalEntities() {
    final LegalEntityHistoryResult result = new LegalEntityHistoryResult(DOCS);
    final List<ManageableLegalEntity> entities = result.getLegalEntities();
    assertEquals(entities, Arrays.asList(ENTITY_1, ENTITY_2, ENTITY_3));
  }

  /**
   * Tests getting the first entity.
   */
  public void testGetFirstLegalEntityNoDocuments() {
    final LegalEntityHistoryResult result = new LegalEntityHistoryResult();
    assertNull(result.getFirstLegalEntity());
  }

  /**
   * Tests getting the first entity.
   */
  public void testGetFirstLegalEntity() {
    final LegalEntityHistoryResult result = new LegalEntityHistoryResult(DOCS);
    assertEquals(result.getFirstLegalEntity(), ENTITY_1);
  }

  /**
   * Tests that there must be one document available.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testGetSingleLegalEntityNoDocuments() {
    new LegalEntityHistoryResult().getSingleLegalEntity();
  }

  /**
   * Tests that there must be one document available.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testGetSingleLegalEntityMultipleDocuments() {
    new LegalEntityHistoryResult(DOCS).getSingleLegalEntity();
  }

  /**
   * Tests the object.
   */
  public void testObject() {
    final LegalEntityHistoryResult result = new LegalEntityHistoryResult(DOCS);
    final LegalEntityHistoryResult other = new LegalEntityHistoryResult(DOCS);
    assertEquals(result, result);
    assertEquals(result.toString(),
        "LegalEntityHistoryResult{paging=Paging[first=0, size=2147483647, totalItems=3], documents=[LegalEntityDocument{versionFromInstant=null, "
            + "versionToInstant=null, correctionFromInstant=null, correctionToInstant=null, legalEntity=ManageableLegalEntity{uniqueId=null, "
            + "externalIdBundle=Bundle[], attributes={}, details={}, name=one, ratings=[], capabilities=[], issuedSecurities=[], "
            + "obligations=[], rootPortfolio=null, accounts=[]}, uniqueId=uid~1}, LegalEntityDocument{versionFromInstant=null, "
            + "versionToInstant=null, correctionFromInstant=null, correctionToInstant=null, legalEntity=ManageableLegalEntity{uniqueId=null, "
            + "externalIdBundle=Bundle[], attributes={}, details={}, name=two, ratings=[], capabilities=[], issuedSecurities=[], obligations=[], "
            + "rootPortfolio=null, accounts=[]}, uniqueId=uid~2}, LegalEntityDocument{versionFromInstant=null, versionToInstant=null, "
            + "correctionFromInstant=null, correctionToInstant=null, legalEntity=ManageableLegalEntity{uniqueId=null, externalIdBundle=Bundle[], "
            + "attributes={}, details={}, name=three, ratings=[], capabilities=[], issuedSecurities=[], obligations=[], rootPortfolio=null, "
            + "accounts=[]}, uniqueId=uid~3}]}");
    assertEquals(result, other);
    assertEquals(result.hashCode(), other.hashCode());
    other.setDocuments(DOCS.subList(0, 1));
    assertNotEquals(result, other);
    other.setDocuments(DOCS);
    other.setPaging(Paging.of(PagingRequest.FIRST_PAGE, DOCS));
    assertNotEquals(result, other);
    other.setPaging(Paging.ofAll(DOCS));
  }

  /**
   * Tests the bean.
   */
  public void testBean() {
    final LegalEntityHistoryResult result = new LegalEntityHistoryResult(DOCS);
    assertEquals(result.propertyNames().size(), 2);
    final Meta bean = LegalEntityHistoryResult.meta();
    assertEquals(bean.documents().get(result), DOCS);
    assertEquals(bean.paging().get(result), Paging.ofAll(DOCS));
    assertEquals(result.property("documents").get(), DOCS);
    assertEquals(result.property("paging").get(), Paging.ofAll(DOCS));
  }

  /**
   * Tests a cycle.
   */
  public void testCycle() {
    final LegalEntityHistoryResult result = new LegalEntityHistoryResult(DOCS);
    assertEncodeDecodeCycle(LegalEntityHistoryResult.class, result);
  }
}
