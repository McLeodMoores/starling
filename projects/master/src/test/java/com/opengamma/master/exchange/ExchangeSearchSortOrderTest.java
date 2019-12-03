/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.exchange;

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
 * Tests for {@link ExchangeSearchSortOrder}.
 */
@Test(groups = TestGroup.UNIT)
public class ExchangeSearchSortOrderTest {
  private ManageableExchange _exchange1 = new ManageableExchange();
  private ManageableExchange _exchange2 = new ManageableExchange();
  private ManageableExchange _exchange3 = new ManageableExchange();
  private ManageableExchange _exchange4 = new ManageableExchange();
  private ManageableExchange _exchange5 = new ManageableExchange();
  private final ExchangeDocument _doc1 = new ExchangeDocument(_exchange1);
  private final ExchangeDocument _doc2 = new ExchangeDocument(_exchange2);
  private final ExchangeDocument _doc3 = new ExchangeDocument(_exchange3);
  private final ExchangeDocument _doc4 = new ExchangeDocument(_exchange4);
  private final ExchangeDocument _doc5 = new ExchangeDocument(_exchange5);

  /**
   * Resets state of all documents after each method.
   */
  @AfterMethod
  public void resetState() {
    _doc1.setUniqueId(null);
    _doc2.setUniqueId(null);
    _doc3.setUniqueId(null);
    _doc4.setUniqueId(null);
    _doc5.setUniqueId(null);
    _doc1.setVersionFromInstant(null);
    _doc2.setVersionFromInstant(null);
    _doc3.setVersionFromInstant(null);
    _doc4.setVersionFromInstant(null);
    _doc5.setVersionFromInstant(null);
    _exchange1 = new ManageableExchange();
    _exchange2 = new ManageableExchange();
    _exchange3 = new ManageableExchange();
    _exchange4 = new ManageableExchange();
    _exchange5 = new ManageableExchange();
    _doc1.setExchange(_exchange1);
    _doc2.setExchange(_exchange2);
    _doc3.setExchange(_exchange3);
    _doc4.setExchange(_exchange4);
    _doc5.setExchange(_exchange5);
  }

  /**
   * Tests sort by object id ascending with a null id.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testSortObjectIdAscendingNullId() {
    _doc1.setUniqueId(UniqueId.of("uid", "1"));
    _doc2.setUniqueId(UniqueId.of("uid", "2"));
    _doc3.setUniqueId(UniqueId.of("uid", "3"));
    _doc4.setUniqueId(UniqueId.of("uid", "4"));
    final List<ExchangeDocument> docs = Arrays.asList(_doc1, _doc2, _doc3, _doc4, _doc5);
    Collections.shuffle(docs);
    Collections.sort(docs, ExchangeSearchSortOrder.OBJECT_ID_ASC);
  }

  /**
   * Tests sorting by object id ascending.
   */
  public void testSortObjectIdAscending() {
    _doc1.setUniqueId(UniqueId.of("uid", "1"));
    _doc2.setUniqueId(UniqueId.of("uid", "2"));
    _doc3.setUniqueId(UniqueId.of("uid", "3"));
    _doc4.setUniqueId(UniqueId.of("uid", "4"));
    _doc5.setUniqueId(UniqueId.of("uid", "5"));
    final List<ExchangeDocument> docs = Arrays.asList(_doc1, _doc2, _doc3, _doc4, _doc5);
    Collections.shuffle(docs);
    Collections.sort(docs, ExchangeSearchSortOrder.OBJECT_ID_ASC);
    assertEquals(docs, Arrays.asList(_doc1, _doc2, _doc3, _doc4, _doc5));
  }

  /**
   * Tests sort by object id ascending with a null id.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testSortObjectIdDescendingNullId() {
    _doc1.setUniqueId(UniqueId.of("uid", "1"));
    _doc2.setUniqueId(UniqueId.of("uid", "2"));
    _doc3.setUniqueId(UniqueId.of("uid", "3"));
    _doc4.setUniqueId(UniqueId.of("uid", "4"));
    final List<ExchangeDocument> docs = Arrays.asList(_doc1, _doc2, _doc3, _doc4, _doc5);
    Collections.shuffle(docs);
    Collections.sort(docs, ExchangeSearchSortOrder.OBJECT_ID_DESC);
  }

  /**
   * Tests sorting by object id descending.
   */
  public void testSortObjectIdDescending() {
    _doc1.setUniqueId(UniqueId.of("uid", "1"));
    _doc2.setUniqueId(UniqueId.of("uid", "2"));
    _doc3.setUniqueId(UniqueId.of("uid", "3"));
    _doc4.setUniqueId(UniqueId.of("uid", "4"));
    _doc5.setUniqueId(UniqueId.of("uid", "5"));
    final List<ExchangeDocument> docs = Arrays.asList(_doc1, _doc2, _doc3, _doc4, _doc5);
    Collections.shuffle(docs);
    Collections.sort(docs, ExchangeSearchSortOrder.OBJECT_ID_DESC);
    assertEquals(docs, Arrays.asList(_doc5, _doc4, _doc3, _doc2, _doc1));
  }

  /**
   * Tests sort by version from ascending with a null id.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testSortVersionFromAscendingNullId() {
    _doc1.setVersionFromInstant(Instant.ofEpochSecond(100000));
    _doc2.setVersionFromInstant(Instant.ofEpochSecond(200000));
    _doc3.setVersionFromInstant(Instant.ofEpochSecond(300000));
    _doc4.setVersionFromInstant(Instant.ofEpochSecond(400000));
    final List<ExchangeDocument> docs = Arrays.asList(_doc1, _doc2, _doc3, _doc4, _doc5);
    Collections.shuffle(docs);
    Collections.sort(docs, ExchangeSearchSortOrder.VERSION_FROM_INSTANT_ASC);
  }

  /**
   * Tests sorting by version from ascending.
   */
  public void testSortVersionFromAscending() {
    _doc1.setVersionFromInstant(Instant.ofEpochSecond(100000));
    _doc2.setVersionFromInstant(Instant.ofEpochSecond(200000));
    _doc3.setVersionFromInstant(Instant.ofEpochSecond(300000));
    _doc4.setVersionFromInstant(Instant.ofEpochSecond(400000));
    _doc5.setVersionFromInstant(Instant.ofEpochSecond(500000));
    final List<ExchangeDocument> docs = Arrays.asList(_doc1, _doc2, _doc3, _doc4, _doc5);
    Collections.shuffle(docs);
    Collections.sort(docs, ExchangeSearchSortOrder.VERSION_FROM_INSTANT_ASC);
    assertEquals(docs, Arrays.asList(_doc1, _doc2, _doc3, _doc4, _doc5));
  }

  /**
   * Tests sort by version from ascending with a null id.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testSortVersionFromDescendingNullId() {
    _doc1.setVersionFromInstant(Instant.ofEpochSecond(100000));
    _doc2.setVersionFromInstant(Instant.ofEpochSecond(200000));
    _doc3.setVersionFromInstant(Instant.ofEpochSecond(300000));
    _doc4.setVersionFromInstant(Instant.ofEpochSecond(400000));
    final List<ExchangeDocument> docs = Arrays.asList(_doc1, _doc2, _doc3, _doc4, _doc5);
    Collections.shuffle(docs);
    Collections.sort(docs, ExchangeSearchSortOrder.VERSION_FROM_INSTANT_DESC);
  }

  /**
   * Tests sorting by version from descending.
   */
  public void testSortVersionFromDescending() {
    _doc1.setVersionFromInstant(Instant.ofEpochSecond(100000));
    _doc2.setVersionFromInstant(Instant.ofEpochSecond(200000));
    _doc3.setVersionFromInstant(Instant.ofEpochSecond(300000));
    _doc4.setVersionFromInstant(Instant.ofEpochSecond(400000));
    _doc5.setVersionFromInstant(Instant.ofEpochSecond(500000));
    final List<ExchangeDocument> docs = Arrays.asList(_doc1, _doc2, _doc3, _doc4, _doc5);
    Collections.shuffle(docs);
    Collections.sort(docs, ExchangeSearchSortOrder.VERSION_FROM_INSTANT_DESC);
    assertEquals(docs, Arrays.asList(_doc5, _doc4, _doc3, _doc2, _doc1));
  }

  /**
   * Tests sort by version name with a null id.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testSortNameAscendingNullId() {
    _exchange1.setName("name1");
    _exchange2.setName("name2");
    _exchange3.setName("name3");
    _exchange4.setName("name4");
    final List<ExchangeDocument> docs = Arrays.asList(_doc1, _doc2, _doc3, _doc4, _doc5);
    Collections.shuffle(docs);
    Collections.sort(docs, ExchangeSearchSortOrder.NAME_ASC);
  }

  /**
   * Tests sorting by name ascending.
   */
  public void testSortNameAscending() {
    _exchange1.setName("name1");
    _exchange2.setName("name2");
    _exchange3.setName("name3");
    _exchange4.setName("name4");
    _exchange5.setName("name5");
    final List<ExchangeDocument> docs = Arrays.asList(_doc1, _doc2, _doc3, _doc4, _doc5);
    Collections.shuffle(docs);
    Collections.sort(docs, ExchangeSearchSortOrder.NAME_ASC);
    assertEquals(docs, Arrays.asList(_doc1, _doc2, _doc3, _doc4, _doc5));
  }

  /**
   * Tests sort by name ascending with a null id.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testSortNameDescendingNullId() {
    _exchange1.setName("name1");
    _exchange2.setName("name2");
    _exchange3.setName("name3");
    _exchange4.setName("name4");
    final List<ExchangeDocument> docs = Arrays.asList(_doc1, _doc2, _doc3, _doc4, _doc5);
    Collections.shuffle(docs);
    Collections.sort(docs, ExchangeSearchSortOrder.NAME_DESC);
  }

  /**
   * Tests sorting by name descending.
   */
  public void testSortNameDescending() {
    _exchange1.setName("name1");
    _exchange2.setName("name2");
    _exchange3.setName("name3");
    _exchange4.setName("name4");
    _exchange5.setName("name5");
    final List<ExchangeDocument> docs = Arrays.asList(_doc1, _doc2, _doc3, _doc4, _doc5);
    Collections.shuffle(docs);
    Collections.sort(docs, ExchangeSearchSortOrder.NAME_DESC);
    assertEquals(docs, Arrays.asList(_doc5, _doc4, _doc3, _doc2, _doc1));
  }
}
