/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.legalentity;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link LegalEntitySearchSortOrder}.
 */
@Test(groups = TestGroup.UNIT)
public class LegalEntitySearchSortOrderTest {
  private ManageableLegalEntity _entity1 = new ManageableLegalEntity();
  private ManageableLegalEntity _entity2 = new ManageableLegalEntity();
  private ManageableLegalEntity _entity3 = new ManageableLegalEntity();
  private ManageableLegalEntity _entity4 = new ManageableLegalEntity();
  private LegalEntityDocument _doc1 = new LegalEntityDocument(_entity1);
  private LegalEntityDocument _doc2 = new LegalEntityDocument(_entity2);
  private LegalEntityDocument _doc3 = new LegalEntityDocument(_entity3);
  private LegalEntityDocument _doc4 = new LegalEntityDocument(_entity4);

  /**
   * Resets the objects.
   */
  @AfterMethod
  public void reset() {
    _entity1 = new ManageableLegalEntity();
    _entity2 = new ManageableLegalEntity();
    _entity3 = new ManageableLegalEntity();
    _entity4 = new ManageableLegalEntity();
    _doc1 = new LegalEntityDocument(_entity1);
    _doc2 = new LegalEntityDocument(_entity2);
    _doc3 = new LegalEntityDocument(_entity3);
    _doc4 = new LegalEntityDocument(_entity4);
  }

  /**
   * Tests sorting by object id ascending with a null id.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testSortObjectIdAscendingNullId() {
    _doc1.setUniqueId(UniqueId.of("uid", "1"));
    _doc2.setUniqueId(UniqueId.of("uid", "2"));
    _doc3.setUniqueId(UniqueId.of("uid", "3"));
    final List<LegalEntityDocument> docs = Arrays.asList(_doc1, _doc2, _doc3, _doc4);
    Collections.shuffle(docs);
    Collections.sort(docs, LegalEntitySearchSortOrder.OBJECT_ID_ASC);
  }

  /**
   * Tests sorting by object id ascending.
   */
  public void testSortObjectIdAscending() {
    _doc1.setUniqueId(UniqueId.of("uid", "1"));
    _doc2.setUniqueId(UniqueId.of("uid", "2"));
    _doc3.setUniqueId(UniqueId.of("uid", "3"));
    _doc4.setUniqueId(UniqueId.of("uid", "4"));
    final List<LegalEntityDocument> docs = Arrays.asList(_doc1, _doc2, _doc3, _doc4);
    Collections.shuffle(docs);
    Collections.sort(docs, LegalEntitySearchSortOrder.OBJECT_ID_ASC);
    assertEquals(docs, Arrays.asList(_doc1, _doc2, _doc3, _doc4));
  }

  /**
   * Tests sorting by object id descending with a null id.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testSortObjectIdDescendingNullId() {
    _doc1.setUniqueId(UniqueId.of("uid", "1"));
    _doc2.setUniqueId(UniqueId.of("uid", "2"));
    _doc3.setUniqueId(UniqueId.of("uid", "3"));
    final List<LegalEntityDocument> docs = Arrays.asList(_doc1, _doc2, _doc3, _doc4);
    Collections.shuffle(docs);
    Collections.sort(docs, LegalEntitySearchSortOrder.OBJECT_ID_DESC);
  }

  /**
   * Tests sorting by object id descending.
   */
  public void testSortObjectIdDescending() {
    _doc1.setUniqueId(UniqueId.of("uid", "1"));
    _doc2.setUniqueId(UniqueId.of("uid", "2"));
    _doc3.setUniqueId(UniqueId.of("uid", "3"));
    _doc4.setUniqueId(UniqueId.of("uid", "4"));
    final List<LegalEntityDocument> docs = Arrays.asList(_doc1, _doc2, _doc3, _doc4);
    Collections.shuffle(docs);
    Collections.sort(docs, LegalEntitySearchSortOrder.OBJECT_ID_DESC);
    assertEquals(docs, Arrays.asList(_doc4, _doc3, _doc2, _doc1));
  }

  /**
   * Tests sorting by version ascending with a null version.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testSortVersionAscendingNullVersion() {
    _doc1.setVersionFromInstant(Instant.ofEpochSecond(10));
    _doc2.setVersionFromInstant(Instant.ofEpochSecond(20));
    _doc3.setVersionFromInstant(Instant.ofEpochSecond(30));
    final List<LegalEntityDocument> docs = Arrays.asList(_doc1, _doc2, _doc3, _doc4);
    Collections.shuffle(docs);
    Collections.sort(docs, LegalEntitySearchSortOrder.VERSION_FROM_INSTANT_ASC);
  }

  /**
   * Tests sorting by version ascending.
   */
  public void testSortVersionAscending() {
    _doc1.setVersionFromInstant(Instant.ofEpochSecond(10));
    _doc2.setVersionFromInstant(Instant.ofEpochSecond(20));
    _doc3.setVersionFromInstant(Instant.ofEpochSecond(30));
    _doc4.setVersionFromInstant(Instant.ofEpochSecond(40));
    final List<LegalEntityDocument> docs = Arrays.asList(_doc1, _doc2, _doc3, _doc4);
    Collections.shuffle(docs);
    Collections.sort(docs, LegalEntitySearchSortOrder.VERSION_FROM_INSTANT_ASC);
    assertEquals(docs, Arrays.asList(_doc1, _doc2, _doc3, _doc4));
  }

  /**
   * Tests sorting by version ascending with a null version.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testSortVersionDescendingNullVersion() {
    _doc1.setVersionFromInstant(Instant.ofEpochSecond(10));
    _doc2.setVersionFromInstant(Instant.ofEpochSecond(20));
    _doc3.setVersionFromInstant(Instant.ofEpochSecond(30));
    final List<LegalEntityDocument> docs = Arrays.asList(_doc1, _doc2, _doc3, _doc4);
    Collections.shuffle(docs);
    Collections.sort(docs, LegalEntitySearchSortOrder.VERSION_FROM_INSTANT_DESC);
  }

  /**
   * Tests sorting by version descending.
   */
  public void testSortVersionDescending() {
    _doc1.setVersionFromInstant(Instant.ofEpochSecond(10));
    _doc2.setVersionFromInstant(Instant.ofEpochSecond(20));
    _doc3.setVersionFromInstant(Instant.ofEpochSecond(30));
    _doc4.setVersionFromInstant(Instant.ofEpochSecond(40));
    final List<LegalEntityDocument> docs = Arrays.asList(_doc1, _doc2, _doc3, _doc4);
    Collections.shuffle(docs);
    Collections.sort(docs, LegalEntitySearchSortOrder.VERSION_FROM_INSTANT_DESC);
    assertEquals(docs, Arrays.asList(_doc4, _doc3, _doc2, _doc1));
  }

  /**
   * Tests sorting by name ascending.
   */
  public void testSortNameAscending() {
    _entity1.setName("n1");
    _entity2.setName("n2");
    _entity3.setName("n3");
    _entity4.setName("n4");
    final List<LegalEntityDocument> docs = Arrays.asList(_doc1, _doc2, _doc3, _doc4);
    Collections.shuffle(docs);
    Collections.sort(docs, LegalEntitySearchSortOrder.NAME_ASC);
    assertEquals(docs, Arrays.asList(_doc1, _doc2, _doc3, _doc4));
  }

  /**
   * Tests sorting by name descending.
   */
  public void testSortNameDescending() {
    _entity1.setName("n1");
    _entity2.setName("n2");
    _entity3.setName("n3");
    _entity4.setName("n4");
    final List<LegalEntityDocument> docs = Arrays.asList(_doc1, _doc2, _doc3, _doc4);
    Collections.shuffle(docs);
    Collections.sort(docs, LegalEntitySearchSortOrder.NAME_DESC);
    assertEquals(docs, Arrays.asList(_doc4, _doc3, _doc2, _doc1));
  }
}
