/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.historicaltimeseries;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoaderResult.Meta;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link HistoricalTimeSeriesLoaderResult}.
 */
@Test(groups = TestGroup.UNIT)
public class HistoricalTimeSeriesLoaderResultTest extends AbstractFudgeBuilderTestCase {
  private static final Map<ExternalId, UniqueId> RESULTS = new HashMap<>();
  static {
    for (int i = 0; i < 6; i++) {
      RESULTS.put(ExternalId.of("eid", Integer.toString(i)), UniqueId.of("uid", Integer.toBinaryString(i)));
    }
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final HistoricalTimeSeriesLoaderResult result = new HistoricalTimeSeriesLoaderResult();
    assertTrue(result.getResultMap().isEmpty());
    result.setResultMap(RESULTS);
    assertEquals(result, result);
    assertEquals(result.toString(),
        "HistoricalTimeSeriesLoaderResult{resultMap={eid~1=uid~1, eid~0=uid~0, eid~3=uid~11, eid~2=uid~10, eid~5=uid~101, eid~4=uid~100}}");
    final HistoricalTimeSeriesLoaderResult other = new HistoricalTimeSeriesLoaderResult();
    other.setResultMap(RESULTS);
    assertEquals(result, other);
    assertEquals(result.hashCode(), other.hashCode());
    other.setResultMap(Collections.singletonMap(RESULTS.keySet().iterator().next(), RESULTS.values().iterator().next()));
    assertNotEquals(result, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final HistoricalTimeSeriesLoaderResult result = new HistoricalTimeSeriesLoaderResult();
    result.setResultMap(RESULTS);
    assertEquals(result.propertyNames().size(), 1);
    final Meta bean = result.metaBean();
    assertEquals(bean.resultMap().get(result), RESULTS);
    assertEquals(result.property("resultMap").get(), RESULTS);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final HistoricalTimeSeriesLoaderResult result = new HistoricalTimeSeriesLoaderResult();
    assertEncodeDecodeCycle(HistoricalTimeSeriesLoaderResult.class, result);
    result.setResultMap(RESULTS);
    assertEncodeDecodeCycle(HistoricalTimeSeriesLoaderResult.class, result);
  }
}
