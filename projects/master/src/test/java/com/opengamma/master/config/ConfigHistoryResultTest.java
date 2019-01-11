/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.config;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.fudgemsg.FudgeRuntimeContextException;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.DateSet;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.master.config.ConfigHistoryResult.Meta;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ConfigHistoryResult}.
 */
@Test(groups = TestGroup.UNIT)
public class ConfigHistoryResultTest extends AbstractFudgeBuilderTestCase {
  private static final ConfigItem<DateSet> DS_1 = ConfigItem.of(DateSet.of(new HashSet<>(Arrays.asList(LocalDate.of(2011, 1, 1), LocalDate.of(2012, 1, 1)))));
  private static final ConfigItem<DateSet> DS_2 = ConfigItem.of(DateSet.of(new HashSet<>(Arrays.asList(LocalDate.of(2013, 1, 1), LocalDate.of(2014, 1, 1)))));
  private static final ConfigItem<DateSet> DS_3 = ConfigItem.of(DateSet.of(new HashSet<>(Arrays.asList(LocalDate.of(2015, 1, 1), LocalDate.of(2016, 1, 1)))));
  private static final ConfigItem<Object> RANDOM = ConfigItem.of(new Object());
  static {
    DS_1.setName("name1");
    DS_1.setType(ConfigItem.class);
    DS_2.setName("name2");
    DS_3.setName("name3");
    DS_3.setType(ConfigItem.class);
    RANDOM.setName("random");
    RANDOM.setType(Object.class);
  }
  private static final List<ConfigDocument> CONFIGS = Arrays.asList(new ConfigDocument(DS_1), new ConfigDocument(DS_2), new ConfigDocument(DS_3));
  private static final Paging PAGING = Paging.of(PagingRequest.FIRST_PAGE, 3);

  /**
   * Tests the default constructor.
   */
  @Test
  public void testDefaultConstructor() {
    final ConfigHistoryResult<?> result = new ConfigHistoryResult<>();
    assertEquals(result.getDocuments().size(), 0);
    assertNull(result.getPaging());
  }

  /**
   * Tests the constructor.
   */
  @Test
  public void testConstructor() {
    final ConfigHistoryResult<DateSet> result = new ConfigHistoryResult<>(CONFIGS);
    assertEquals(result.getDocuments(), CONFIGS);
    assertEquals(result.getPaging(), Paging.ofAll(CONFIGS));
  }

  /**
   * Tests the value getter.
   */
  @Test
  public void testValueGetter() {
    final List<ConfigDocument> docs = Arrays.asList(new ConfigDocument(DS_1), new ConfigDocument(DS_2), new ConfigDocument(DS_3), new ConfigDocument(RANDOM));
    final ConfigHistoryResult<?> result = new ConfigHistoryResult<>(docs);
    // DS_2 doesn't have the type set and RANDOM has the wrong type
    assertEquals(result.getValues(), Arrays.asList(DS_1, DS_3));
  }

  /**
   * Tests getting the first value.
   */
  @Test
  public void testGetFirstNoValue() {
    final ConfigHistoryResult<?> result = new ConfigHistoryResult<>();
    assertNull(result.getFirstValue());
  }

  /**
   * Tests getting the first value.
   */
  @Test
  public void testGetFirstValue() {
    final ConfigHistoryResult<?> result = new ConfigHistoryResult<>(CONFIGS);
    assertEquals(result.getFirstValue(), DS_1);
  }

  /**
   * Tests getting a single value.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testGetSingleValueMultipleValues() {
    new ConfigHistoryResult<>(CONFIGS).getSingleValue();
  }

  /**
   * Tests getting a single value.
   */
  @Test
  public void testGetSingleValue() {
    final ConfigHistoryResult<?> result = new ConfigHistoryResult<>(Collections.singletonList(new ConfigDocument(DS_3)));
    assertEquals(result.getFirstValue(), DS_3);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final ConfigHistoryResult<DateSet> result = new ConfigHistoryResult<>(CONFIGS);
    result.setPaging(PAGING);
    final ConfigHistoryResult<DateSet> other = new ConfigHistoryResult<>(CONFIGS);
    other.setPaging(PAGING);
    assertEquals(result, result);
    assertEquals(result.toString(),
        "ConfigHistoryResult{paging=Paging[first=0, size=20, totalItems=3], "
            + "documents=[ConfigDocument{versionFromInstant=null, versionToInstant=null, correctionFromInstant=null, "
            + "correctionToInstant=null, config=ConfigItem{value=DateSet{dates=[2011-01-01, 2012-01-01]}, uniqueId=null, "
            + "name=name1, type=class com.opengamma.core.config.impl.ConfigItem}, uniqueId=null}, ConfigDocument{versionFromInstant=null, "
            + "versionToInstant=null, correctionFromInstant=null, correctionToInstant=null, config=ConfigItem{value=DateSet{dates=[2013-01-01, 2014-01-01]}, "
            + "uniqueId=null, name=name2, type=class com.opengamma.core.DateSet}, uniqueId=null}, ConfigDocument{versionFromInstant=null, "
            + "versionToInstant=null, correctionFromInstant=null, correctionToInstant=null, config=ConfigItem{value=DateSet{dates=[2015-01-01, 2016-01-01]}, "
            + "uniqueId=null, name=name3, type=class com.opengamma.core.config.impl.ConfigItem}, uniqueId=null}]}");
    assertEquals(result, other);
    assertEquals(result.hashCode(), other.hashCode());
    other.setDocuments(CONFIGS.subList(0, 1));
    assertNotEquals(result, other);
    other.setDocuments(CONFIGS);
    other.setPaging(Paging.of(PagingRequest.ALL, 3));
    assertNotEquals(result, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final ConfigHistoryResult<?> result = new ConfigHistoryResult<>(CONFIGS);
    assertEquals(result.propertyNames().size(), 2);
    final Meta<?> bean = result.metaBean();
    assertEquals(bean.documents().get(result), CONFIGS);
    assertEquals(bean.paging().get(result), Paging.ofAll(CONFIGS));
    assertEquals(result.property("documents").get(), CONFIGS);
    assertEquals(result.property("paging").get(), Paging.ofAll(CONFIGS));
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final ConfigHistoryResult<?> result = new ConfigHistoryResult<>(CONFIGS);
    assertEquals(result, cycleObjectJodaXml(ConfigHistoryResult.class, result));
  }

  /**
   * Fudge cannot cycle this object.
   */
  @Test(expectedExceptions = FudgeRuntimeContextException.class)
  public void testObjectProxyCycle() {
    final ConfigHistoryResult<?> result = new ConfigHistoryResult<>(CONFIGS);
    cycleObjectProxy(ConfigHistoryResult.class, result);
  }

  /**
   * Fudge cannot cycle this object.
   */
  @Test(expectedExceptions = FudgeRuntimeContextException.class)
  public void testObjectBytesCycle() {
    final ConfigHistoryResult<?> result = new ConfigHistoryResult<>(CONFIGS);
    cycleObjectBytes(ConfigHistoryResult.class, result);
  }

  /**
   * Fudge cannot cycle this object.
   */
  @Test(expectedExceptions = FudgeRuntimeContextException.class)
  public void testObjectXmlCycle() {
    final ConfigHistoryResult<?> result = new ConfigHistoryResult<>(CONFIGS);
    cycleObjectXml(ConfigHistoryResult.class, result);
  }
}
