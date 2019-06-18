/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.provider.historicaltimeseries;

import static com.opengamma.test.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.AbstractBeanTestCase;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link HistoricalTimeSeriesProviderGetResult}.
 */
@Test(groups = TestGroup.UNIT)
public class HistoricalTimeSeriesProviderGetResultTest extends AbstractBeanTestCase {
  private static final Map<ExternalIdBundle, LocalDateDoubleTimeSeries> RESULT_MAP = new HashMap<>();
  private static final Map<ExternalIdBundle, Set<String>> PERMISSIONS_MAP = new HashMap<>();
  static {
    RESULT_MAP.put(ExternalIdBundle.of("hts", "1"), ImmutableLocalDateDoubleTimeSeries.of(LocalDate.of(2020, 1, 1), 10));
    RESULT_MAP.put(ExternalIdBundle.of("hts", "2"), ImmutableLocalDateDoubleTimeSeries.of(LocalDate.of(2020, 1, 1), 11));
    PERMISSIONS_MAP.put(ExternalIdBundle.of("hts", "1"), Collections.singleton("perm1"));
    PERMISSIONS_MAP.put(ExternalIdBundle.of("hts", "2"), Collections.singleton("perm2"));
  }

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(HistoricalTimeSeriesProviderGetResult.class, Arrays.asList("resultMap", "permissionsMap"),
        Arrays.asList(RESULT_MAP, PERMISSIONS_MAP),
        Arrays.asList(Collections.singletonMap(ExternalIdBundle.of("hts", "3"), ImmutableLocalDateDoubleTimeSeries.of(LocalDate.of(2020, 1, 1), 30)),
            Collections.singletonMap(ExternalIdBundle.of("hts", "3"), "perm3")));
  }

  // overridden because the bean does not clone the time series properly
  @Override
  @Test(dataProvider = "propertyValues")
  protected <TYPE extends Bean> void testObject(final JodaBeanProperties<TYPE> properties) {
    try {
      final BeanBuilder<TYPE> builder = constructAndPopulateBeanBuilder(properties);
      final BeanBuilder<TYPE> otherBuilder = constructAndPopulateBeanBuilder(properties);
      final TYPE bean = builder.build();
      final TYPE other = otherBuilder.build();
      // test Object methods
      assertEquals(bean, bean);
      assertNotEquals(null, bean);
      assertNotEquals(builder, bean);
      assertEquals(bean, other);
      assertEquals(bean.hashCode(), other.hashCode());
      // test getters and bean
      for (int i = 0; i < properties.size(); i++) {
        final String propertyName = properties.getPropertyName(i);
        final Object propertyValue = properties.getPropertyValue(i);
        if (propertyValue != null) {
          assertNotNull(builder.get(propertyName));
        }
        assertEquals(bean.property(propertyName).get(), propertyValue);
        assertEquals(builder.get(propertyName), propertyValue);
      }
    } catch (final Exception e) {
      fail(e.getMessage());
    }
  }

  // overridden because the bean does not cycle the time series properly
  @Override
  @Test(dataProvider = "propertyValues")
  protected <TYPE extends Bean> void testCycle(final JodaBeanProperties<TYPE> properties) {
    final TYPE bean = constructAndPopulateBeanBuilder(properties).build();
    final Class<TYPE> clazz = properties.getType();
    assertEquals(cycleObjectProxy(clazz, bean), bean);
    assertEquals(cycleObjectBytes(clazz, bean), bean);
    assertEquals(cycleObjectXml(clazz, bean), bean);
  }

  /**
   * Tests that the results are empty by default.
   */
  public void testEmptyResults() {
    HistoricalTimeSeriesProviderGetResult result = new HistoricalTimeSeriesProviderGetResult();
    assertTrue(result.getResultMap().isEmpty());
    assertTrue(result.getPermissionsMap().isEmpty());
    result = new HistoricalTimeSeriesProviderGetResult(RESULT_MAP);
    assertEqualsNoOrder(result.getResultMap(), RESULT_MAP);
    assertTrue(result.getPermissionsMap().isEmpty());
    result = new HistoricalTimeSeriesProviderGetResult(RESULT_MAP, PERMISSIONS_MAP);
    assertEqualsNoOrder(result.getResultMap(), RESULT_MAP);
    assertEqualsNoOrder(result.getPermissionsMap(), PERMISSIONS_MAP);
  }
}
