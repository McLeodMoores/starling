/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.security.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.HashMap;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.core.security.Security;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.security.SecurityProviderRequest;
import com.opengamma.provider.security.SecurityProviderResult;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class NoneFoundSecurityProviderTest {

  private static final ExternalIdBundle BUNDLE = ExternalIdBundle.of("A", "B");

  /**
   * Tests trying to get a single security.
   */
  public void testGetSingle() {
    final NoneFoundSecurityProvider test = new NoneFoundSecurityProvider();
    assertNull(test.getSecurity(BUNDLE));
  }

  /**
   * Tests attempting to get multiple securities.
   */
  public void testGetBulk() {
    final NoneFoundSecurityProvider test = new NoneFoundSecurityProvider();
    final HashMap<ExternalIdBundle, Security> expected = new HashMap<>();
    expected.put(BUNDLE, null);
    assertEquals(test.getSecurities(ImmutableSet.of(BUNDLE)), expected);
  }

  /**
   * Tests attempting to get securities using a get request.
   */
  public void testGetRequest() {
    final NoneFoundSecurityProvider test = new NoneFoundSecurityProvider();
    final SecurityProviderRequest request = SecurityProviderRequest.createGet(BUNDLE, "FOO");
    final SecurityProviderResult expected = new SecurityProviderResult();
    expected.getResultMap().put(BUNDLE, null);
    assertEquals(expected, test.getSecurities(request));
  }

}
