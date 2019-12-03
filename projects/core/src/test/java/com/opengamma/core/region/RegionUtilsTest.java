/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.region;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.Iterator;
import java.util.Set;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.region.impl.SimpleRegion;
import com.opengamma.util.test.TestGroup;


/**
 * Tests for {@link RegionUtils}.
 */
@Test(groups = TestGroup.UNIT)
public class RegionUtilsTest {
  private static final RegionSource SOURCE = Mockito.mock(RegionSource.class);
  private static final SimpleRegion NY = new SimpleRegion();
  private static final SimpleRegion EU = new SimpleRegion();
  private static final SimpleRegion AUS = new SimpleRegion();
  static {
    NY.setClassification(RegionClassification.MUNICIPALITY);
    NY.setFullName("New York");
    NY.setExternalIdBundle(ExternalSchemes.financialRegionId("NY").toBundle());
    AUS.setClassification(RegionClassification.INDEPENDENT_STATE);
    AUS.setFullName("Australia");
    AUS.setExternalIdBundle(ExternalSchemes.financialRegionId("AUS").toBundle());
    EU.setClassification(RegionClassification.SUPER_NATIONAL);
    EU.setFullName("EU");
    EU.setExternalIdBundle(ExternalSchemes.financialRegionId("EU").toBundle());
    Mockito.when(SOURCE.getHighestLevelRegion(ExternalSchemes.financialRegionId("NY"))).thenReturn(NY);
    Mockito.when(SOURCE.getHighestLevelRegion(ExternalSchemes.financialRegionId("AUS"))).thenReturn(AUS);
    Mockito.when(SOURCE.getHighestLevelRegion(ExternalSchemes.financialRegionId("EU"))).thenReturn(EU);
  }

  /**
   * Checks that the source cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSource() {
    RegionUtils.getRegions(null, ExternalSchemes.financialRegionId("NY"));
  }

  /**
   * Checks that the id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullId() {
    RegionUtils.getRegions(SOURCE, null);
  }

  /**
   * Tests getting a region with a simple id.
   */
  @Test
  public void testSimpleId() {
    final Set<Region> region = RegionUtils.getRegions(SOURCE, ExternalSchemes.financialRegionId("AUS"));
    assertEquals(region.size(), 1);
    assertEquals(region.iterator().next().getFullName(), "Australia");
  }

  /**
   * Tests getting a region with a compound id.
   */
  @Test
  public void testCompoundId() {
    final Set<Region> regions = RegionUtils.getRegions(SOURCE, ExternalSchemes.financialRegionId("NY+EU"));
    assertEquals(regions.size(), 2);
    final Iterator<Region> iter = regions.iterator();
    final Region region1 = iter.next();
    final Region region2 = iter.next();
    if (region1.getFullName().equals("New York")) {
      assertEquals(region2.getFullName(), "EU");
    } else if (region1.getFullName().equals("EU")) {
      assertEquals(region2.getFullName(), "New York");
    } else {
      fail();
    }
  }
}
