/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.target;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Map;

import org.eclipse.jetty.util.ajax.JSON;
import org.testng.annotations.Test;

import com.opengamma.engine.target.DefaultComputationTargetTypeProvider;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link WebComputationTargetTypeResource}.
 */
@Test(groups = TestGroup.UNIT)
public class WebComputationTargetTypeResourceTest {
  private static final DefaultComputationTargetTypeProvider TYPES = new DefaultComputationTargetTypeProvider();
  private static final WebComputationTargetTypeResource RESOURCE = new WebComputationTargetTypeResource(TYPES);

  /**
   * Tests that the target type provider cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullProvider() {
    new WebComputationTargetTypeResource(null);
  }

  /**
   * Tests conversion of types to a JSON array.
   */
  @SuppressWarnings("unchecked")
  public void testConvertToJson() {
    final String json = RESOURCE.typesJSONResponse(TYPES.getAllTypes());
    final Map<String, Object[]> map = (Map<String, Object[]>) JSON.parse(json);
    assertEquals(map.size(), 1);
    final Object[] types = map.get("types");
    for (final Object type : types) {
      assertTrue(type instanceof Map);
      final Map<String, String> t = (Map<String, String>) type;
      assertEquals(t.size(), 2);
      assertNotNull(t.get("label"));
      assertNotNull(t.get("value"));
    }
  }

  /**
   * Tests getting the simple target types.
   */
  public void testGetSimpleTypes() {
    final String json = RESOURCE.getSimpleTypes();
    @SuppressWarnings("unchecked")
    final Map<String, Object[]> map = (Map<String, Object[]>) JSON.parse(json);
    assertEquals(map.size(), 1);
    final Object[] types = map.get("types");
    assertEquals(types.length, TYPES.getSimpleTypes().size());
  }

  /**
   * Tests getting the additional target types.
   */
  public void testGetAdditionalTypes() {
    final String json = RESOURCE.getAdditionalTypes();
    @SuppressWarnings("unchecked")
    final Map<String, Object[]> map = (Map<String, Object[]>) JSON.parse(json);
    assertEquals(map.size(), 1);
    final Object[] types = map.get("types");
    assertEquals(types.length, TYPES.getAdditionalTypes().size());
  }

  /**
   * Tests getting the all target types.
   */
  public void testGetAllTypes() {
    final String json = RESOURCE.getAllTypes();
    @SuppressWarnings("unchecked")
    final Map<String, Object[]> map = (Map<String, Object[]>) JSON.parse(json);
    assertEquals(map.size(), 1);
    final Object[] types = map.get("types");
    assertEquals(types.length, TYPES.getAllTypes().size());
  }

}
