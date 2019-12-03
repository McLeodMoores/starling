/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.region.impl;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionClassification;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link RegionComparator}.
 */
@Test(groups = TestGroup.UNIT)
public class RegionComparatorTest {
  private static final SimpleRegion REGION_1 = new SimpleRegion();
  private static final SimpleRegion REGION_2 = new SimpleRegion();
  private static final SimpleRegion REGION_3 = new SimpleRegion();
  private static final SimpleRegion REGION_4 = new SimpleRegion();
  private static final SimpleRegion REGION_5 = new SimpleRegion();
  private static final SimpleRegion REGION_6 = new SimpleRegion();
  private static final SimpleRegion REGION_7 = new SimpleRegion();
  static {
    REGION_1.setClassification(RegionClassification.INDEPENDENT_STATE);
    REGION_2.setClassification(RegionClassification.SUPER_NATIONAL);
    REGION_3.setClassification(RegionClassification.ANTARCTIC_TERRITORY);
    REGION_4.setClassification(RegionClassification.SUPER_NATIONAL);
    REGION_5.setClassification(RegionClassification.INDEPENDENT_STATE);
    REGION_6.setClassification(RegionClassification.MUNICIPALITY);
    REGION_7.setClassification(RegionClassification.MUNICIPALITY);
    REGION_1.setName("us");
    REGION_2.setName("us");
    REGION_3.setName("us");
    REGION_4.setName("fr");
    REGION_5.setName("uk");
    REGION_6.setName("au");
  }

  /**
   * Tests sort ascending.
   */
  @Test
  public void testAscending() {
    final List<Region> actual = Arrays.<Region>asList(REGION_1, REGION_7, REGION_6, REGION_5, REGION_2, REGION_4, REGION_3);
    Collections.sort(actual, RegionComparator.ASC);
    final List<Region> expected = Arrays.<Region>asList(REGION_4, REGION_2, REGION_5, REGION_1, REGION_3, REGION_7, REGION_6);
    assertEquals(actual, expected);
  }

  /**
   * Tests sort descending.
   */
  @Test
  public void testDescending() {
    final List<Region> actual = Arrays.<Region>asList(REGION_1, REGION_7, REGION_6, REGION_5, REGION_2, REGION_4, REGION_3);
    Collections.sort(actual, RegionComparator.DESC);
    final List<Region> expected = Arrays.<Region>asList(REGION_6, REGION_7, REGION_3, REGION_1, REGION_5, REGION_2, REGION_4);
    assertEquals(actual, expected);
  }
}
