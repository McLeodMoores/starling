/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.security;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link SecuritySearchSortOrder}.
 */
@Test(groups = TestGroup.UNIT)
public class SecuritySearchSortOrderTest {
  private static final SecurityDocument DOC_1 = new SecurityDocument();
  private static final SecurityDocument DOC_2 = new SecurityDocument();
  private static final SecurityDocument DOC_3 = new SecurityDocument();
  static {
    DOC_1.setUniqueId(UniqueId.of(ObjectId.of("oid", "val3"), "v3"));
    DOC_2.setUniqueId(UniqueId.of(ObjectId.of("oid", "val1"), "v1"));
    DOC_3.setUniqueId(UniqueId.of(ObjectId.of("oid", "val2"), "v2"));

    DOC_1.setVersionFromInstant(Instant.ofEpochSecond(100000L));
    DOC_2.setVersionFromInstant(Instant.ofEpochSecond(300000L));
    DOC_3.setVersionFromInstant(Instant.ofEpochSecond(200000L));

    final RawSecurity security1 = new RawSecurity("FUTURE 2");
    security1.setName("NAME 3");
    final RawSecurity security2 = new RawSecurity("FUTURE 1");
    security2.setName("NAME 2");
    final RawSecurity security3 = new RawSecurity("FUTURE 3");
    DOC_1.setSecurity(security1);
    DOC_2.setSecurity(security2);
    DOC_3.setSecurity(security3);
  }

  /**
   * Tests sort by ascending object id.
   */
  @Test
  public void testAscendingObjectId() {
    final List<SecurityDocument> docs = Arrays.asList(DOC_1, DOC_2, DOC_3);
    Collections.sort(docs, SecuritySearchSortOrder.OBJECT_ID_ASC);
    assertEquals(docs, Arrays.asList(DOC_2, DOC_3, DOC_1));
  }

  /**
   * Tests sort by descending object id.
   */
  @Test
  public void testDescendingObjectId() {
    final List<SecurityDocument> docs = Arrays.asList(DOC_1, DOC_2, DOC_3);
    Collections.sort(docs, SecuritySearchSortOrder.OBJECT_ID_DESC);
    assertEquals(docs, Arrays.asList(DOC_1, DOC_3, DOC_2));
  }

  /**
   * Tests sort by ascending version from.
   */
  @Test
  public void testAscendingVersionFrom() {
    final List<SecurityDocument> docs = Arrays.asList(DOC_1, DOC_2, DOC_3);
    Collections.sort(docs, SecuritySearchSortOrder.VERSION_FROM_INSTANT_ASC);
    assertEquals(docs, Arrays.asList(DOC_1, DOC_3, DOC_2));
  }

  /**
   * Tests sort by descending version from.
   */
  @Test
  public void testDescendingVersionFrom() {
    final List<SecurityDocument> docs = Arrays.asList(DOC_1, DOC_2, DOC_3);
    Collections.sort(docs, SecuritySearchSortOrder.VERSION_FROM_INSTANT_DESC);
    assertEquals(docs, Arrays.asList(DOC_2, DOC_3, DOC_1));
  }

  /**
   * Tests sort by ascending name.
   */
  @Test
  public void testAscendingName() {
    final List<SecurityDocument> docs = Arrays.asList(DOC_1, DOC_2, DOC_3);
    Collections.sort(docs, SecuritySearchSortOrder.NAME_ASC);
    assertEquals(docs, Arrays.asList(DOC_3, DOC_2, DOC_1));
  }

  /**
   * Tests sort by descending name.
   */
  @Test
  public void testDescendingName() {
    final List<SecurityDocument> docs = Arrays.asList(DOC_1, DOC_2, DOC_3);
    Collections.sort(docs, SecuritySearchSortOrder.NAME_DESC);
    assertEquals(docs, Arrays.asList(DOC_1, DOC_2, DOC_3));
  }

  /**
   * Tests sort by security type.
   */
  @Test
  public void testAscendingSecurityType() {
    final List<SecurityDocument> docs = Arrays.asList(DOC_1, DOC_2, DOC_3);
    Collections.sort(docs, SecuritySearchSortOrder.SECURITY_TYPE_ASC);
    assertEquals(docs, Arrays.asList(DOC_2, DOC_1, DOC_3));
  }

  /**
   * Tests sort by descending name.
   */
  @Test
  public void testDescendingSecurityType() {
    final List<SecurityDocument> docs = Arrays.asList(DOC_1, DOC_2, DOC_3);
    Collections.sort(docs, SecuritySearchSortOrder.SECURITY_TYPE_DESC);
    assertEquals(docs, Arrays.asList(DOC_3, DOC_1, DOC_2));
  }
}
