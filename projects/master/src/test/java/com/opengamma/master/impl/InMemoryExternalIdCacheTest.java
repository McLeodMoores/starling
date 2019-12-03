/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.core.security.Security;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;

/**
 * Tests for {@link InMemoryExternalIdCache}.
 */
public class InMemoryExternalIdCacheTest {
  private static final InMemoryExternalIdCache<Security, SecurityDocument> CACHE = new InMemoryExternalIdCache<>();
  private static final ManageableSecurity SECURITY = new ManageableSecurity();
  private static final SecurityDocument DOCUMENT = new SecurityDocument(SECURITY);
  static {
    SECURITY.addExternalId(ExternalId.of("Test", "1"));
  }

  /**
   * Tests that the identifiable cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIdentifiable() {
    CACHE.add(null, DOCUMENT);
  }

  /**
   * Tests that the document cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDocument() {
    CACHE.add(SECURITY, null);
  }

  /**
   * Tests that values are added to the cache and can be retrieved.
   */
  @Test
  public void testAddElements() {
    final ExternalId id1 = ExternalId.of("test", "1");
    final ExternalId id2 = ExternalId.of("test", "2");
    final ExternalId id3 = ExternalId.of("test", "3");
    final ManageableSecurity element1 = new ManageableSecurity();
    element1.addExternalId(id1);
    element1.addExternalId(id2);
    final ManageableSecurity element2 = new ManageableSecurity();
    element2.addExternalId(id1);
    final ManageableSecurity element3 = new ManageableSecurity();
    element3.addExternalId(id3);
    final SecurityDocument document1 = new SecurityDocument(element1);
    final SecurityDocument document2 = new SecurityDocument(element2);
    final SecurityDocument document3 = new SecurityDocument(element3);
    final InMemoryExternalIdCache<Security, SecurityDocument> cache = new InMemoryExternalIdCache<>();
    assertTrue(cache.getMatches(ExternalIdSearch.of(id1)).isEmpty());
    assertTrue(cache.getMatches(ExternalIdSearch.of(id2)).isEmpty());
    assertTrue(cache.getMatches(ExternalIdSearch.of(id3)).isEmpty());
    cache.add(element1, document1);
    cache.add(element2, document2);
    cache.add(element3, document3);
    assertEquals(cache.getMatches(ExternalIdSearch.of(id1)).size(), 2);
    assertEquals(cache.getMatches(ExternalIdSearch.of(id2)).size(), 1);
    assertEquals(cache.getMatches(ExternalIdSearch.of(id3)).size(), 1);
  }

  /**
   * Tests that values are removed from the cache.
   */
  @Test
  public void testRemoveElements() {
    final ExternalId id1 = ExternalId.of("test", "1");
    final ExternalId id2 = ExternalId.of("test", "2");
    final ExternalId id3 = ExternalId.of("test", "3");
    final ManageableSecurity element1 = new ManageableSecurity();
    element1.addExternalId(id1);
    element1.addExternalId(id2);
    final ManageableSecurity element2 = new ManageableSecurity();
    element2.addExternalId(id1);
    final ManageableSecurity element3 = new ManageableSecurity();
    element3.addExternalId(id3);
    final SecurityDocument document1 = new SecurityDocument(element1);
    final SecurityDocument document2 = new SecurityDocument(element2);
    final SecurityDocument document3 = new SecurityDocument(element3);
    final InMemoryExternalIdCache<Security, SecurityDocument> cache = new InMemoryExternalIdCache<>();
    cache.add(element1, document1);
    cache.add(element2, document2);
    cache.add(element3, document3);
    cache.remove(element1);
    assertEquals(cache.getMatches(ExternalIdSearch.of(id1)).size(), 1);
    assertEquals(cache.getMatches(ExternalIdSearch.of(id2)).size(), 0);
    assertEquals(cache.getMatches(ExternalIdSearch.of(id3)).size(), 1);
    cache.remove(element2);
    assertEquals(cache.getMatches(ExternalIdSearch.of(id1)).size(), 0);
    assertEquals(cache.getMatches(ExternalIdSearch.of(id2)).size(), 0);
    assertEquals(cache.getMatches(ExternalIdSearch.of(id3)).size(), 1);
    cache.remove(element3);
    assertEquals(cache.getMatches(ExternalIdSearch.of(id1)).size(), 0);
    assertEquals(cache.getMatches(ExternalIdSearch.of(id2)).size(), 0);
    assertEquals(cache.getMatches(ExternalIdSearch.of(id3)).size(), 0);
  }

  /**
   * Tests matching on any of the ids.
   */
  @Test
  public void testMatchAny() {
    final ExternalId id1 = ExternalId.of("test", "1");
    final ExternalId id2 = ExternalId.of("test", "2");
    final ExternalId id3 = ExternalId.of("test", "3");
    final ManageableSecurity element = new ManageableSecurity();
    element.addExternalId(id1);
    element.addExternalId(id2);
    final SecurityDocument document = new SecurityDocument(element);
    final InMemoryExternalIdCache<Security, SecurityDocument> cache = new InMemoryExternalIdCache<>();
    cache.add(element, document);
    Set<SecurityDocument> matches = cache.getMatches(ExternalIdSearch.of(ExternalIdSearchType.ANY, id1));
    assertEquals(matches.size(), 1);
    assertEquals(matches.iterator().next(), document);
    matches = cache.getMatches(ExternalIdSearch.of(ExternalIdSearchType.ANY, id1, id2));
    assertEquals(matches.size(), 1);
    assertEquals(matches.iterator().next(), document);
    matches = cache.getMatches(ExternalIdSearch.of(ExternalIdSearchType.ANY, id1, id2, id3));
    assertEquals(matches.size(), 1);
    assertEquals(matches.iterator().next(), document);
  }

  /**
   * Tests matching on all of the ids.
   */
  @Test
  public void testMatchAll() {
    final ExternalId id1 = ExternalId.of("test", "1");
    final ExternalId id2 = ExternalId.of("test", "2");
    final ExternalId id3 = ExternalId.of("test", "3");
    final ManageableSecurity element = new ManageableSecurity();
    element.addExternalId(id1);
    element.addExternalId(id2);
    final SecurityDocument document = new SecurityDocument(element);
    final InMemoryExternalIdCache<Security, SecurityDocument> cache = new InMemoryExternalIdCache<>();
    cache.add(element, document);
    Set<SecurityDocument> matches = cache.getMatches(ExternalIdSearch.of(ExternalIdSearchType.ALL, id1, id2, id3));
    assertTrue(matches.isEmpty());
    matches = cache.getMatches(ExternalIdSearch.of(ExternalIdSearchType.ALL, id1, id2));
    assertEquals(matches.size(), 1);
    assertEquals(matches.iterator().next(), document);
    matches = cache.getMatches(ExternalIdSearch.of(ExternalIdSearchType.ALL, id1));
    assertEquals(matches.size(), 1);
    assertEquals(matches.iterator().next(), document);
  }

  /**
   * Tests matching on all of the ids.
   */
  @Test
  public void testMatchExact() {
    final ExternalId id1 = ExternalId.of("test", "1");
    final ExternalId id2 = ExternalId.of("test", "2");
    final ExternalId id3 = ExternalId.of("test", "3");
    final ManageableSecurity element = new ManageableSecurity();
    element.addExternalId(id1);
    element.addExternalId(id2);
    final SecurityDocument document = new SecurityDocument(element);
    final InMemoryExternalIdCache<Security, SecurityDocument> cache = new InMemoryExternalIdCache<>();
    cache.add(element, document);
    Set<SecurityDocument> matches = cache.getMatches(ExternalIdSearch.of(ExternalIdSearchType.EXACT, id1, id2, id3));
    assertTrue(matches.isEmpty());
    matches = cache.getMatches(ExternalIdSearch.of(ExternalIdSearchType.EXACT, id1, id2));
    assertEquals(matches.size(), 1);
    assertEquals(matches.iterator().next(), document);
    matches = cache.getMatches(ExternalIdSearch.of(ExternalIdSearchType.EXACT, id1));
    assertEquals(matches.size(), 1);
    assertEquals(matches.iterator().next(), document);
  }

  /**
   * Tests matching on none of the ids.
   */
  @Test
  public void testMatchNone() {
    final ExternalId id1 = ExternalId.of("test", "1");
    final ExternalId id2 = ExternalId.of("test", "2");
    final ExternalId id3 = ExternalId.of("test", "3");
    final ManageableSecurity element = new ManageableSecurity();
    element.addExternalId(id1);
    element.addExternalId(id2);
    final SecurityDocument document = new SecurityDocument(element);
    final InMemoryExternalIdCache<Security, SecurityDocument> cache = new InMemoryExternalIdCache<>();
    cache.add(element, document);
    Set<SecurityDocument> matches = cache.getMatches(ExternalIdSearch.of(ExternalIdSearchType.NONE, id3));
    assertEquals(matches.size(), 1);
    matches = cache.getMatches(ExternalIdSearch.of(ExternalIdSearchType.NONE, id1));
    assertTrue(matches.isEmpty());
    matches = cache.getMatches(ExternalIdSearch.of(ExternalIdSearchType.NONE, id1, id2));
    assertTrue(matches.isEmpty());
    matches = cache.getMatches(ExternalIdSearch.of(ExternalIdSearchType.NONE, id1, id3));
    assertTrue(matches.isEmpty());
  }
}
