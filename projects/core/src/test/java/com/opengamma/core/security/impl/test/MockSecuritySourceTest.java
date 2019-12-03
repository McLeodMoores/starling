/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.security.impl.test;

import org.testng.annotations.Test;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link MockSecuritySource}.
 */
@Test(groups = TestGroup.UNIT)
public class MockSecuritySourceTest {
  private static final SecuritySource SOURCE = new MockSecuritySource();

  /**
   * Tests the changeManager method.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testChangeManager() {
    SOURCE.changeManager();
  }

  /**
   * Tests the get method.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetUniqueId() {
    SOURCE.get(UniqueId.of("uid", "1"));
  }

  /**
   * Tests the get method.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetObjectIdVersionCorrection() {
    SOURCE.get(ObjectId.of("oid", "1"), VersionCorrection.LATEST);
  }

  /**
   * Tests the get method.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetExternalIdBundleVersionCorrection() {
    SOURCE.get(ExternalIdBundle.of("eids", "1"), VersionCorrection.LATEST);
  }

  /**
   * Tests the get method.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetExternalIdBundle() {
    SOURCE.get(ExternalIdBundle.of("eids", "1"));
  }

  /**
   * Tests the getSingle method.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetSingleExternalIdBundle() {
    SOURCE.getSingle(ExternalIdBundle.of("eids", "1"));
  }

  /**
   * Tests the getSingle method.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetSingleExternalIdBundleVersionCorrection() {
    SOURCE.getSingle(ExternalIdBundle.of("eids", "1"), VersionCorrection.LATEST);
  }

}
