/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.valuerequirementname;

import static org.testng.Assert.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Arrays;
import java.util.Map;

import org.eclipse.jetty.util.ajax.JSON;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link WebValueRequirementNamesResource}.
 */
@Test(groups = TestGroup.UNIT)
public class WebValueRequirementNamesResourceTest {
  private static final WebValueRequirementNamesResource RESOURCE = new WebValueRequirementNamesResource(
      new String[] { "com.opengamma.web.valuerequirementname.ValueRequirementNamesTestHelper" });

  /**
   * Tests that the classes cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullClasses() {
    new WebValueRequirementNamesResource(null);
  }

  /**
   * Tests that the classes cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyClasses() {
    new WebValueRequirementNamesResource(new String[0]);
  }

  /**
   * Tests that the list of names is sorted.
   *
   * @throws JSONException
   *           if there is a problem with the JSON
   */
  public void testSorted() throws JSONException {
    final String json = RESOURCE.getJSON();
    @SuppressWarnings("unchecked")
    final Map<String, Object[]> map = (Map<String, Object[]>) JSON.parse(json);
    assertEquals(map.size(), 1);
    final Object[] names = map.get("types");
    assertEquals(names.length, 4);
    final Object[] copy = new Object[4];
    System.arraycopy(names, 0, copy, 0, names.length);
    Arrays.sort(copy);
    assertArrayEquals(names, copy);
  }

}
