/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.security.impl;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.impl.SimpleSecurity;
import com.opengamma.provider.security.SecurityEnhancer;
import com.opengamma.provider.security.SecurityEnhancerRequest;
import com.opengamma.provider.security.SecurityEnhancerResult;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class NoEnhancementSecurityEnhancerTest {

  private static final SimpleSecurity SECURITY1 = new SimpleSecurity("A1");
  private static final SimpleSecurity SECURITY2 = new SimpleSecurity("A2");

  /**
   * Tests getting a single enhanced security.
   */
  public void testGetSingle() {
    final SecurityEnhancer test = new NoEnhancementSecurityEnhancer();
    final Security result = test.enhanceSecurity(SECURITY1);
    assertEquals(result, SECURITY1);
  }

  /**
   * Tests getting multiple enhanced securities.
   */
  public void testGetBulk() {
    final SecurityEnhancer test = new NoEnhancementSecurityEnhancer();
    final List<Security> result = test.enhanceSecurities(Arrays.<Security>asList(SECURITY1, SECURITY2));
    assertEquals(result, Arrays.asList(SECURITY1, SECURITY2));
  }

  /**
   * Tests getting multiple enhanced securities.
   */
  public void testGetBulkMap() {
    final SecurityEnhancer test = new NoEnhancementSecurityEnhancer();
    final Map<String, Security> map = new HashMap<>();
    map.put("A", SECURITY1);
    map.put("B", SECURITY2);
    final Map<String, Security> result = test.enhanceSecurities(map);
    assertEquals(result, map);
  }

  /**
   * Tests getting multiple enhanced securities using a get request.
   */
  public void testGetRequest() {
    final SecurityEnhancer test = new NoEnhancementSecurityEnhancer();
    final SecurityEnhancerRequest request = SecurityEnhancerRequest.create(SECURITY1, SECURITY2);
    final SecurityEnhancerResult result = test.enhanceSecurities(request);
    assertEquals(result.getResultList(), Arrays.asList(SECURITY1, SECURITY2));
  }

}
