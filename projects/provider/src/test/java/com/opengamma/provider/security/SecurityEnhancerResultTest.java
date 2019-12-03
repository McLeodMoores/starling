/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.provider.security;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.impl.SimpleSecurity;
import com.opengamma.provider.AbstractBeanTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link SecurityEnhancerResult}.
 */
@Test(groups = TestGroup.UNIT)
public class SecurityEnhancerResultTest extends AbstractBeanTestCase {
  private static final List<Security> SECURITIES = Arrays.<Security> asList(new SimpleSecurity("name1"), new SimpleSecurity("name2"));

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    final ArrayList<Security> reversed = new ArrayList<>(SECURITIES);
    Collections.reverse(reversed);
    return new JodaBeanProperties<>(SecurityEnhancerResult.class,
        Arrays.asList("resultList"), Arrays.asList(SECURITIES), Arrays.asList(reversed));
  }

  /**
   * Tests that the map cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullMap() {
    new SecurityEnhancerResult().insertIntoMapValues(null);
  }

  /**
   * Tests that the map size must be equal to the securities size.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNotEqualSizes() {
    final Map<Object, Security> map = new HashMap<>();
    map.put("sec", SECURITIES.get(0));
    new SecurityEnhancerResult(SECURITIES).insertIntoMapValues(map);
  }

  /**
   * Tests that the values are inserted as entries into the provided map.
   */
  public void testInsertIntoMapValues() {
    final SecurityEnhancerResult result = new SecurityEnhancerResult(SECURITIES);
    final Map<Object, Security> map = new LinkedHashMap<>();
    final Map<Object, Security> expected = new LinkedHashMap<>();
    map.put("sec1", null);
    expected.put("sec1", SECURITIES.get(0));
    map.put("sec2", null);
    expected.put("sec2", SECURITIES.get(1));
    result.insertIntoMapValues(map);
    assertEquals(map, expected);
  }

}
