/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.config;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.core.DateSet;
import com.opengamma.master.config.ConfigMetaDataResult.Meta;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRating;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ConfigMetaDataResult}.
 */
@Test(groups = TestGroup.UNIT)
public class ConfigMetaDataResultTest extends AbstractFudgeBuilderTestCase {
  private static final List<Class<?>> CONFIG_TYPES = Arrays.<Class<?>> asList(HistoricalTimeSeriesRating.class, DateSet.class, DateSet.class);

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final ConfigMetaDataResult result = new ConfigMetaDataResult();
    assertTrue(result.getConfigTypes().isEmpty());
    result.setConfigTypes(CONFIG_TYPES);
    final ConfigMetaDataResult other = new ConfigMetaDataResult();
    other.setConfigTypes(CONFIG_TYPES);
    assertEquals(result, result);
    assertEquals(result.toString(), "ConfigMetaDataResult{configTypes=["
        + "class com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRating, class com.opengamma.core.DateSet, class com.opengamma.core.DateSet]}");
    assertEquals(result, other);
    assertEquals(result.hashCode(), other.hashCode());
    result.setConfigTypes(CONFIG_TYPES.subList(0, 1));
    assertNotEquals(result, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final ConfigMetaDataResult result = new ConfigMetaDataResult();
    result.setConfigTypes(CONFIG_TYPES);
    assertEquals(result.propertyNames().size(), 1);
    final Meta bean = result.metaBean();
    assertEquals(bean.configTypes().get(result), CONFIG_TYPES);
    assertEquals(result.property("configTypes").get(), CONFIG_TYPES);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final ConfigMetaDataResult result = new ConfigMetaDataResult();
    result.setConfigTypes(CONFIG_TYPES);
    assertEncodeDecodeCycle(ConfigMetaDataResult.class, result);
  }
}
