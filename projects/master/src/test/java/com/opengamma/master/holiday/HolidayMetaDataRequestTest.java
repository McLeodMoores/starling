/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.holiday;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link HolidayMetaDataRequest}.
 */
@Test(groups = TestGroup.UNIT)
public class HolidayMetaDataRequestTest extends AbstractFudgeBuilderTestCase {
  private static final String UID_SCHEME = "hol";
  private static final Boolean FETCH_TYPES = false;

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final HolidayMetaDataRequest request = new HolidayMetaDataRequest();
    assertTrue(request.isHolidayTypes());
    request.setHolidayTypes(FETCH_TYPES);
    request.setUniqueIdScheme(UID_SCHEME);
    assertEquals(request.getUniqueIdScheme(), UID_SCHEME);
    assertFalse(request.isHolidayTypes());
    assertEquals(request, request);
    assertNotEquals(null, request);
    assertEquals(request.toString(), "HolidayMetaDataRequest{uniqueIdScheme=hol, holidayTypes=false}");
    final HolidayMetaDataRequest other = new HolidayMetaDataRequest();
    other.setHolidayTypes(FETCH_TYPES);
    other.setUniqueIdScheme(UID_SCHEME);
    assertEquals(request, other);
    assertEquals(request.hashCode(), other.hashCode());
    other.setHolidayTypes(!FETCH_TYPES);
    assertNotEquals(request, other);
    other.setHolidayTypes(FETCH_TYPES);
    other.setUniqueIdScheme("uid");
    assertNotEquals(request, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final HolidayMetaDataRequest request = new HolidayMetaDataRequest();
    request.setHolidayTypes(FETCH_TYPES);
    request.setUniqueIdScheme(UID_SCHEME);
    assertEquals(request.propertyNames().size(), 2);
    assertEquals(request.metaBean().holidayTypes().get(request), FETCH_TYPES);
    assertEquals(request.metaBean().uniqueIdScheme().get(request), UID_SCHEME);
    assertEquals(request.property("holidayTypes").get(), FETCH_TYPES);
    assertEquals(request.property("uniqueIdScheme").get(), UID_SCHEME);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final HolidayMetaDataRequest request = new HolidayMetaDataRequest();
    assertEncodeDecodeCycle(HolidayMetaDataRequest.class, request);
    request.setHolidayTypes(FETCH_TYPES);
    assertEncodeDecodeCycle(HolidayMetaDataRequest.class, request);
    request.setUniqueIdScheme(UID_SCHEME);
    assertEncodeDecodeCycle(HolidayMetaDataRequest.class, request);
  }
}
