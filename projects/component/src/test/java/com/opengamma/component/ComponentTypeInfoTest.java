/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.component;

import static org.testng.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ComponentTypeInfo}.
 */
@Test(groups = TestGroup.UNIT)
public class ComponentTypeInfoTest {

  /**
   * The component type cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullType() {
    new ComponentTypeInfo(null);
  }

  /**
   * The classifier string cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullClassifier() {
    new ComponentTypeInfo().getInfo(null);
  }

  /**
   * The component must be available.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testComponentNotAvailable() {
    new ComponentTypeInfo().getInfo("htsSource");
  }

  /**
   * Tests that the expected information about the component is returned.
   */
  @Test
  public void testGetInfo() {
    final ComponentTypeInfo info = new ComponentTypeInfo(HistoricalTimeSeriesSource.class);
    final Map<String, ComponentInfo> infoMap = new HashMap<>();
    infoMap.put("htsSource1", new ComponentInfo(HistoricalTimeSeriesSource.class, "hts1"));
    infoMap.put("htsSource2", new ComponentInfo(HistoricalTimeSeriesSource.class, "hts2"));
    info.setInfoMap(infoMap);
    assertEquals(info.getInfo("htsSource1"), new ComponentInfo(HistoricalTimeSeriesSource.class, "hts1"));
    assertEquals(info.getInfo("htsSource2"), new ComponentInfo(HistoricalTimeSeriesSource.class, "hts2"));
  }
}
