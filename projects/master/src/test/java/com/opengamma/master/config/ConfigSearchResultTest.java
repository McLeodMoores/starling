/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.config;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.fudgemsg.FudgeRuntimeContextException;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.DateSet;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigSearchResult.Meta;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ConfigSearchResult}.
 */
@Test(groups = TestGroup.UNIT)
public class ConfigSearchResultTest extends AbstractFudgeBuilderTestCase {
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
  private static final VersionCorrection VC = VersionCorrection.ofCorrectedTo(Instant.ofEpochSecond(1000));
  private static final Paging PAGING = Paging.of(PagingRequest.NONE, CONFIGS);

  /**
   * Tests the default constructor.
   */
  @Test
  public void testDefaultConstructor() {
    final ConfigSearchResult<?> result = new ConfigSearchResult<>();
    assertTrue(result.getDocuments().isEmpty());
    assertNull(result.getPaging());
    assertEquals(result.getVersionCorrection(), VersionCorrection.LATEST);
  }

  /**
   * Tests the constructor.
   */
  @Test
  public void testConstructor1() {
    final ConfigSearchResult<?> result = new ConfigSearchResult<>(CONFIGS);
    assertEquals(result.getDocuments(), CONFIGS);
    assertEquals(result.getPaging(), Paging.ofAll(CONFIGS));
    assertEquals(result.getVersionCorrection(), VersionCorrection.LATEST);
  }

  /**
   * Tests the constructor.
   */
  @Test
  public void testConstructor2() {
    final ConfigSearchResult<?> result = new ConfigSearchResult<>(VC);
    assertTrue(result.getDocuments().isEmpty());
    assertNull(result.getPaging());
    assertEquals(result.getVersionCorrection(), VC);
  }

  /**
   * Tests the value getter.
   */
  @Test
  public void testValueGetter() {
    final List<ConfigDocument> docs = Arrays.asList(new ConfigDocument(DS_1), new ConfigDocument(DS_2), new ConfigDocument(DS_3), new ConfigDocument(RANDOM));
    final ConfigSearchResult<?> result = new ConfigSearchResult<>(docs);
    assertEquals(result.getValues(), Arrays.asList(DS_1, DS_2, DS_3, RANDOM));
  }

  /**
   * Tests getting the first value.
   */
  @Test
  public void testGetFirstNoValue() {
    final ConfigSearchResult<?> result = new ConfigSearchResult<>();
    assertNull(result.getFirstValue());
  }

  /**
   * Tests getting the first value.
   */
  @Test
  public void testGetFirstValue() {
    final ConfigSearchResult<?> result = new ConfigSearchResult<>(CONFIGS);
    assertEquals(result.getFirstValue(), DS_1);
  }

  /**
   * Tests getting a single value.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testGetSingleValueMultipleValues() {
    new ConfigSearchResult<>(CONFIGS).getSingleValue();
  }

  /**
   * Tests getting a single value.
   */
  @Test
  public void testGetSingleValue() {
    final ConfigSearchResult<?> result = new ConfigSearchResult<>(Collections.singletonList(new ConfigDocument(DS_3)));
    assertEquals(result.getFirstValue(), DS_3);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final ConfigSearchResult<DateSet> result = new ConfigSearchResult<>(CONFIGS);
    result.setPaging(PAGING);
    result.setVersionCorrection(VC);
    final ConfigSearchResult<DateSet> other = new ConfigSearchResult<>(CONFIGS);
    other.setPaging(PAGING);
    other.setVersionCorrection(VC);
    assertEquals(result, result);
    assertEquals(result.toString(),
        "ConfigSearchResult{paging=Paging[first=0, size=0, totalItems=3], documents=[ConfigDocument{versionFromInstant=null, "
            + "versionToInstant=null, correctionFromInstant=null, correctionToInstant=null, config=ConfigItem{value=DateSet{dates=[2011-01-01, 2012-01-01]}, "
            + "uniqueId=null, name=name1, type=class com.opengamma.core.config.impl.ConfigItem}, uniqueId=null}, ConfigDocument{versionFromInstant=null, "
            + "versionToInstant=null, correctionFromInstant=null, correctionToInstant=null, config=ConfigItem{value=DateSet{dates=[2013-01-01, 2014-01-01]}, "
            + "uniqueId=null, name=name2, type=class com.opengamma.core.DateSet}, uniqueId=null}, ConfigDocument{versionFromInstant=null, "
            + "versionToInstant=null, correctionFromInstant=null, correctionToInstant=null, config=ConfigItem{value=DateSet{dates=[2015-01-01, 2016-01-01]}, "
            + "uniqueId=null, name=name3, type=class com.opengamma.core.config.impl.ConfigItem}, uniqueId=null}], versionCorrection=VLATEST.C1970-01-01T00:16:40Z}");
    assertEquals(result, other);
    assertEquals(result.hashCode(), other.hashCode());
    other.setDocuments(CONFIGS.subList(0, 1));
    assertNotEquals(result, other);
    other.setDocuments(CONFIGS);
    other.setPaging(Paging.of(PagingRequest.ALL, 3));
    assertNotEquals(result, other);
    other.setPaging(PAGING);
    other.setVersionCorrection(VersionCorrection.LATEST);
    assertNotEquals(result, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final ConfigSearchResult<?> result = new ConfigSearchResult<>(CONFIGS);
    result.setPaging(PAGING);
    result.setVersionCorrection(VC);
    assertEquals(result.propertyNames().size(), 3);
    final Meta<?> bean = result.metaBean();
    assertEquals(bean.documents().get(result), CONFIGS);
    assertEquals(bean.paging().get(result), PAGING);
    assertEquals(bean.versionCorrection().get(result), VC);
    assertEquals(result.property("documents").get(), CONFIGS);
    assertEquals(result.property("paging").get(), PAGING);
    assertEquals(result.property("versionCorrection").get(), VC);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final ConfigSearchResult<?> result = new ConfigSearchResult<>(CONFIGS);
    assertEquals(result, cycleObjectJodaXml(ConfigSearchResult.class, result));
  }

  /**
   * Fudge cannot cycle this object.
   */
  @Test(expectedExceptions = FudgeRuntimeContextException.class)
  public void testObjectProxyCycle() {
    final ConfigSearchResult<?> result = new ConfigSearchResult<>(CONFIGS);
    cycleObjectProxy(ConfigSearchResult.class, result);
  }

  /**
   * Fudge cannot cycle this object.
   */
  @Test(expectedExceptions = FudgeRuntimeContextException.class)
  public void testObjectBytesCycle() {
    final ConfigSearchResult<?> result = new ConfigSearchResult<>(CONFIGS);
    cycleObjectBytes(ConfigSearchResult.class, result);
  }

  /**
   * Fudge cannot cycle this object.
   */
  @Test(expectedExceptions = FudgeRuntimeContextException.class)
  public void testObjectXmlCycle() {
    final ConfigSearchResult<?> result = new ConfigSearchResult<>(CONFIGS);
    cycleObjectXml(ConfigSearchResult.class, result);
  }
}
