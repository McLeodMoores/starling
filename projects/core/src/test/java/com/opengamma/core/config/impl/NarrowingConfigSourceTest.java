/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.config.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link NarrowingConfigSource}.
 */
@Test(groups = TestGroup.UNIT)
public class NarrowingConfigSourceTest {
  private static final UniqueId UID_1 = UniqueId.of("TEST", "1");
  private static final UniqueId UID_2 = UniqueId.of("TEST", "2");
  private static final UniqueId UID_3 = UniqueId.of("TEST", "3");
  private static final UniqueId UID_4 = UniqueId.of("TEST", "4");
  private static final ConfigItem<String> CFG_1 = ConfigItem.of("ITEM_1");
  static {
    CFG_1.setUniqueId(UID_1);
    CFG_1.setName("NAME 1");
  }
  private static final ConfigItem<String> CFG_2 = ConfigItem.of("ITEM 2");
  static {
    CFG_2.setUniqueId(UID_2);
    CFG_2.setName("NAME 2");
  }
  private static final ConfigItem<String> CFG_3 = ConfigItem.of("ITEM 3");
  static {
    CFG_3.setUniqueId(UID_3);
    CFG_3.setName("NAME 3");
  }
  private static final ConfigItem<String> CFG_4 = ConfigItem.of("ITEM 4");
  static {
    CFG_4.setUniqueId(UID_4);
    CFG_4.setName("NAME 4");
  }

  private static final ConfigSource UNDERLYING = Mockito.mock(ConfigSource.class);
  static {
    populateSource();
  }
  private static final ConfigSource NARROWING = new NarrowingConfigSource(UNDERLYING);

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static void populateSource() {
    final Map<UniqueId, ConfigItem<?>> mUid = ImmutableMap.<UniqueId, ConfigItem<?>>of(
        UID_1, CFG_1, UID_2, CFG_2, UID_3, CFG_3, UID_4, CFG_4);
    final Map<ObjectId, ConfigItem<?>> mOid = ImmutableMap.<ObjectId, ConfigItem<?>>of(
        UID_1.getObjectId(), CFG_1, UID_2.getObjectId(), CFG_2, UID_3.getObjectId(), CFG_3, UID_4.getObjectId(), CFG_4);
    Mockito.when(UNDERLYING.get(UID_1)).thenReturn((ConfigItem) CFG_1);
    Mockito.when(UNDERLYING.get(UID_2)).thenReturn((ConfigItem) CFG_2);
    Mockito.when(UNDERLYING.get(UID_3)).thenReturn((ConfigItem) CFG_3);
    Mockito.when(UNDERLYING.get(UID_4)).thenReturn((ConfigItem) CFG_4);
    Mockito.when(UNDERLYING.get(Arrays.asList(UID_1, UID_2, UID_3, UID_4))).thenReturn(mUid);
    Mockito.when(UNDERLYING.get(UID_1.getObjectId(), VersionCorrection.LATEST)).thenReturn((ConfigItem) CFG_1);
    Mockito.when(UNDERLYING.get(UID_2.getObjectId(), VersionCorrection.LATEST)).thenReturn((ConfigItem) CFG_2);
    Mockito.when(UNDERLYING.get(UID_3.getObjectId(), VersionCorrection.LATEST)).thenReturn((ConfigItem) CFG_3);
    Mockito.when(UNDERLYING.get(UID_4.getObjectId(), VersionCorrection.LATEST)).thenReturn((ConfigItem) CFG_4);
    Mockito.when(UNDERLYING.get(Arrays.asList(UID_1.getObjectId(), UID_2.getObjectId(), UID_3.getObjectId(),
        UID_4.getObjectId()), VersionCorrection.LATEST)).thenReturn(mOid);
    Mockito.when(UNDERLYING.get(String.class, "NAME 1", VersionCorrection.LATEST)).thenReturn(
        Arrays.<ConfigItem<String>>asList(CFG_1));
    Mockito.when(UNDERLYING.get(String.class, "NAME 2", VersionCorrection.LATEST)).thenReturn(
        Arrays.<ConfigItem<String>>asList(CFG_2));
    Mockito.when(UNDERLYING.get(String.class, "NAME 3", VersionCorrection.LATEST)).thenReturn(
        Arrays.<ConfigItem<String>>asList(CFG_3));
    Mockito.when(UNDERLYING.get(String.class, "NAME 4", VersionCorrection.LATEST)).thenReturn(
        Arrays.<ConfigItem<String>>asList(CFG_4));
    // getAll
    Mockito.when(UNDERLYING.getAll(String.class, VersionCorrection.LATEST)).thenReturn(
        Arrays.<ConfigItem<String>>asList(CFG_1, CFG_2, CFG_3, CFG_4));
    Mockito.when(UNDERLYING.changeManager()).thenReturn(new BasicChangeManager());
    Mockito.when(UNDERLYING.get(String.class, "NAME_1", VersionCorrection.LATEST)).thenReturn(Collections.singleton(CFG_4));
    Mockito.when(UNDERLYING.get(String.class, "NAME_2", VersionCorrection.LATEST)).thenReturn(Collections.singleton(CFG_4));
    Mockito.when(UNDERLYING.get(String.class, "NAME_3", VersionCorrection.LATEST)).thenReturn(Collections.singleton(CFG_4));
    Mockito.when(UNDERLYING.get(String.class, "NAME_4", VersionCorrection.LATEST)).thenReturn(Collections.singleton(CFG_4));
  }

  /**
   * Tests the values returned when requesting by unique id.
   */
  @Test
  public void testGetUniqueId() {
    assertEquals(NARROWING.get(UID_1), CFG_1);
    assertEquals(NARROWING.get(UID_2), CFG_2);
    assertEquals(NARROWING.get(UID_3), CFG_3);
    assertEquals(NARROWING.get(UID_4), CFG_4);
  }

  /**
   * Tests the values returned when requesting by object id and version/correction.
   */
  @Test
  public void testGetObjectId() {
    assertEquals(NARROWING.get(UID_1.getObjectId(), VersionCorrection.LATEST), CFG_1);
    assertEquals(NARROWING.get(UID_2.getObjectId(), VersionCorrection.LATEST), CFG_2);
    assertEquals(NARROWING.get(UID_3.getObjectId(), VersionCorrection.LATEST), CFG_3);
    assertEquals(NARROWING.get(UID_4.getObjectId(), VersionCorrection.LATEST), CFG_4);
  }

  /**
   * Tests the correct values are returned when multiple values are requested.
   */
  @Test
  public void testGetCollectionUniqueId() {
    final Map<UniqueId, ConfigItem<?>> result = NARROWING.get(Arrays.asList(UID_1, UID_2, UID_3, UID_4));
    assertEquals(result.size(), 4);
    assertEquals(result.get(UID_1), CFG_1);
    assertEquals(result.get(UID_2), CFG_2);
    assertEquals(result.get(UID_3), CFG_3);
    assertEquals(result.get(UID_4), CFG_4);
  }

  /**
   * Tests the correct values are returned when multiple values are requested.
   */
  @Test
  public void testGetCollectionObjectId() {
    final Map<ObjectId, ConfigItem<?>> result = NARROWING.get(Arrays.asList(
        UID_1.getObjectId(), UID_2.getObjectId(), UID_3.getObjectId(), UID_4.getObjectId()), VersionCorrection.LATEST);
    assertEquals(result.size(), 4);
    assertEquals(result.get(UID_1.getObjectId()), CFG_1);
    assertEquals(result.get(UID_2.getObjectId()), CFG_2);
    assertEquals(result.get(UID_3.getObjectId()), CFG_3);
    assertEquals(result.get(UID_4.getObjectId()), CFG_4);
  }

  /**
   * Tests the correct values are returned.
   */
  @Test
  public void testGetClassName() {
    assertEquals(NARROWING.get(String.class, "NAME 1", VersionCorrection.LATEST), Collections.singleton(CFG_1));
    assertEquals(NARROWING.get(String.class, "NAME 2", VersionCorrection.LATEST), Collections.singleton(CFG_2));
    assertEquals(NARROWING.get(String.class, "NAME 3", VersionCorrection.LATEST), Collections.singleton(CFG_3));
    assertEquals(NARROWING.get(String.class, "NAME 4", VersionCorrection.LATEST), Collections.singleton(CFG_4));
  }

  /**
   * Tests that this method is not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetAll() {
    NARROWING.getAll(String.class, VersionCorrection.LATEST);
  }

  /**
   * Tests that the correct value is returned.
   */
  @Test
  public void testGetConfigUniqueId() {
    assertEquals(NARROWING.getConfig(String.class, UID_1), CFG_1.getValue());
    assertEquals(NARROWING.getConfig(String.class, UID_2), CFG_2.getValue());
    assertEquals(NARROWING.getConfig(String.class, UID_3), CFG_3.getValue());
    assertEquals(NARROWING.getConfig(String.class, UID_4), CFG_4.getValue());
    assertNull(NARROWING.getConfig(ConfigItem.class, UID_1));
  }

  /**
   * Tests that the correct value is returned.
   */
  @Test
  public void testGetConfigObjectId() {
    assertEquals(NARROWING.getConfig(String.class, UID_1.getObjectId(), VersionCorrection.LATEST), CFG_1.getValue());
    assertEquals(NARROWING.getConfig(String.class, UID_2.getObjectId(), VersionCorrection.LATEST), CFG_2.getValue());
    assertEquals(NARROWING.getConfig(String.class, UID_3.getObjectId(), VersionCorrection.LATEST), CFG_3.getValue());
    assertEquals(NARROWING.getConfig(String.class, UID_4.getObjectId(), VersionCorrection.LATEST), CFG_4.getValue());
  }

  /**
   * Tests that the correct value is returned.
   */
  @Test
  public void testGetSingleClassName() {
    assertEquals(NARROWING.getSingle(String.class, "NAME 1", VersionCorrection.LATEST), CFG_1.getValue());
    assertEquals(NARROWING.getSingle(String.class, "NAME 2", VersionCorrection.LATEST), CFG_2.getValue());
    assertEquals(NARROWING.getSingle(String.class, "NAME 3", VersionCorrection.LATEST), CFG_3.getValue());
    assertEquals(NARROWING.getSingle(String.class, "NAME 4", VersionCorrection.LATEST), CFG_4.getValue());
    assertNull(NARROWING.getSingle(String.class, "NAME 5", VersionCorrection.LATEST));
  }

  /**
   * Tests that the correct value is returned.
   */
  @Test
  public void testGetLatestByName() {
    assertEquals(NARROWING.getLatestByName(String.class, "NAME 1"), CFG_1.getValue());
    assertEquals(NARROWING.getLatestByName(String.class, "NAME 2"), CFG_2.getValue());
    assertEquals(NARROWING.getLatestByName(String.class, "NAME 3"), CFG_3.getValue());
    assertEquals(NARROWING.getLatestByName(String.class, "NAME 4"), CFG_4.getValue());
    assertNull(NARROWING.getLatestByName(String.class, "NAME 5"));
  }

  /**
   * Tests that the correct change manager is returned.
   */
  @Test
  public void testGetChangeManager() {
    assertTrue(NARROWING.changeManager() instanceof BasicChangeManager);
  }
}
