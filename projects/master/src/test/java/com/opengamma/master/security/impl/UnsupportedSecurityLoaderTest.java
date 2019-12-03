/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.security.impl;

import java.util.Arrays;
import java.util.Collections;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.security.SecurityLoaderRequest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link UnsupportedSecurityLoader}.
 */
@Test(groups = TestGroup.UNIT)
public class UnsupportedSecurityLoaderTest {

  /**
   * Test the result of trying to load a security.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testLoadId() {
    new UnsupportedSecurityLoader().loadSecurity(ExternalIdBundle.of("eid", "1"));
  }

  /**
   * Test the result of trying to load securities.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testLoadIterable() {
    new UnsupportedSecurityLoader().loadSecurities(Arrays.asList(ExternalIdBundle.of("eid", "1")));
  }

  /**
   * Test the result of trying to load securities.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testLoadEmptyIterable() {
    new UnsupportedSecurityLoader().loadSecurities(Collections.<ExternalIdBundle> emptyList());
  }

  /**
   * Test the result of trying to load securities.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testLoadRequest() {
    new UnsupportedSecurityLoader().loadSecurities(SecurityLoaderRequest.create(ExternalIdBundle.of("eid", "1")));
  }

  /**
   * Test the result of trying to load securities.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testLoadEmptyRequest() {
    new UnsupportedSecurityLoader().loadSecurities(SecurityLoaderRequest.create(Collections.<ExternalIdBundle> emptyList()));
  }

  /**
   * Test the result of trying to load securities.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testBulkLoad() {
    new UnsupportedSecurityLoader().doBulkLoad(SecurityLoaderRequest.create(ExternalIdBundle.of("eid", "1")));
  }
}
