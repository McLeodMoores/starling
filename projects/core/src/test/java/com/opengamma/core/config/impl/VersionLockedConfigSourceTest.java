/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.config.impl;

import static org.testng.Assert.assertSame;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableMap;
import com.opengamma.core.DateSet;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the VersionLockedConfigSource class.
 */
@SuppressWarnings("deprecation")
@Test(groups = TestGroup.UNIT)
public class VersionLockedConfigSourceTest {

  /**
   * Tests that getting items for multiple unique ids returns the same number of items.
   */
  @Test
  public void testGetCollection() {
    final ConfigSource underlying = Mockito.mock(ConfigSource.class);
    final ConfigSource test = new VersionLockedConfigSource(underlying, VersionCorrection.LATEST);
    final Collection<UniqueId> params = Arrays.asList(UniqueId.of("Test", "Foo"), UniqueId.of("Test", "Bar"));
    final Map<UniqueId, ConfigItem<?>> result = ImmutableMap.<UniqueId, ConfigItem<?>>of(
        UniqueId.of("Test", "Foo"), Mockito.mock(ConfigItem.class),
        UniqueId.of("Test", "Bar"), Mockito.mock(ConfigItem.class));
    Mockito.when(underlying.get(params)).thenReturn(result);
    assertSame(test.get(params), result);
  }

  /**
   * Tests that getting items for multiple object ids returns the same correct number of items
   * at the correct version / correction.
   */
  @Test
  public void testGetCollectionVersionCorrection() {
    final ConfigSource underlying = Mockito.mock(ConfigSource.class);
    final Instant t1 = Instant.ofEpochMilli(1L);
    final Instant t2 = Instant.ofEpochMilli(2L);
    final Instant t3 = Instant.ofEpochMilli(3L);
    final Instant t4 = Instant.ofEpochMilli(4L);
    // sets the version / correction on the source
    final ConfigSource test = new VersionLockedConfigSource(underlying, VersionCorrection.of(t1, t2));
    final Collection<ObjectId> ids = Arrays.asList(ObjectId.of("Test", "Foo"), ObjectId.of("Test", "Bar"));
    // the version and correction are the same as the locked v/c
    Map<ObjectId, ConfigItem<?>> result = ImmutableMap.<ObjectId, ConfigItem<?>>of(
        ObjectId.of("Test", "Foo"), Mockito.mock(ConfigItem.class),
        ObjectId.of("Test", "Bar"), Mockito.mock(ConfigItem.class));
    Mockito.when(underlying.get(ids, VersionCorrection.of(t1, t2))).thenReturn(result);
    assertSame(test.get(ids, VersionCorrection.LATEST), result);
    assertSame(test.get(ids, VersionCorrection.ofVersionAsOf(t1)), result);
    assertSame(test.get(ids, VersionCorrection.ofCorrectedTo(t2)), result);
    // locks the version to t3
    result = ImmutableMap.<ObjectId, ConfigItem<?>>of(
        ObjectId.of("Test", "Foo"), Mockito.mock(ConfigItem.class),
        ObjectId.of("Test", "Bar"), Mockito.mock(ConfigItem.class));
    Mockito.when(underlying.get(ids, VersionCorrection.of(t3, t2))).thenReturn(result);
    assertSame(test.get(ids, VersionCorrection.ofVersionAsOf(t3)), result);
    // locks the correction to t3
    result = ImmutableMap.<ObjectId, ConfigItem<?>>of(
        ObjectId.of("Test", "Foo"), Mockito.mock(ConfigItem.class),
        ObjectId.of("Test", "Bar"), Mockito.mock(ConfigItem.class));
    Mockito.when(underlying.get(ids, VersionCorrection.of(t1, t3))).thenReturn(result);
    assertSame(test.get(ids, VersionCorrection.ofCorrectedTo(t3)), result);
    // locks the v / c to t3 / t4
    result = ImmutableMap.<ObjectId, ConfigItem<?>>of(
        ObjectId.of("Test", "Foo"), Mockito.mock(ConfigItem.class),
        ObjectId.of("Test", "Bar"), Mockito.mock(ConfigItem.class));
    Mockito.when(underlying.get(ids, VersionCorrection.of(t3, t4))).thenReturn(result);
    assertSame(test.get(ids, VersionCorrection.of(t3, t4)), result);
  }

  /**
   * Tests the change manager.
   */
  @Test
  public void testChangeManager() {
    final ConfigSource underlying = Mockito.mock(ConfigSource.class);
    final ConfigSource test = new VersionLockedConfigSource(underlying, VersionCorrection.LATEST);
    final ChangeManager result = Mockito.mock(ChangeManager.class);
    Mockito.when(underlying.changeManager()).thenReturn(result);
    assertSame(test.changeManager(), result);
  }

  /**
   * Tests retrieving the latest version of an item by unique id.
   */
  @SuppressWarnings({"unchecked", "rawtypes" })
  public void testGetUniqueId() {
    final ConfigSource underlying = Mockito.mock(ConfigSource.class);
    final ConfigSource test = new VersionLockedConfigSource(underlying, VersionCorrection.LATEST);
    final ConfigItem result = Mockito.mock(ConfigItem.class);
    Mockito.when(underlying.get(UniqueId.of("Test", "Foo"))).thenReturn(result);
    assertSame(test.get(UniqueId.of("Test", "Foo")), result);
  }

  /**
   * Tests that getting an item for an object id returns the same correct number of items
   * at the correct version / correction.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Test
  public void testGetObjectIdVersionCorrection() {
    final ConfigSource underlying = Mockito.mock(ConfigSource.class);
    final Instant t1 = Instant.ofEpochMilli(1L);
    final Instant t2 = Instant.ofEpochMilli(2L);
    final Instant t3 = Instant.ofEpochMilli(3L);
    final Instant t4 = Instant.ofEpochMilli(4L);
    // sets the version / correction on the source
    final ConfigSource test = new VersionLockedConfigSource(underlying, VersionCorrection.of(t1, t2));
    ConfigItem result = Mockito.mock(ConfigItem.class);
    // the version and correction are the same as the locked v/c
    Mockito.when(underlying.get(ObjectId.of("Test", "Foo"), VersionCorrection.of(t1, t2))).thenReturn(result);
    assertSame(test.get(ObjectId.of("Test", "Foo"), VersionCorrection.LATEST), result);
    assertSame(test.get(ObjectId.of("Test", "Foo"), VersionCorrection.ofVersionAsOf(t1)), result);
    assertSame(test.get(ObjectId.of("Test", "Foo"), VersionCorrection.ofCorrectedTo(t2)), result);
    // locks the version to t3
    result = Mockito.mock(ConfigItem.class);
    Mockito.when(underlying.get(ObjectId.of("Test", "Foo"), VersionCorrection.of(t3, t2))).thenReturn(result);
    assertSame(test.get(ObjectId.of("Test", "Foo"), VersionCorrection.ofVersionAsOf(t3)), result);
    // locks the correction to t3
    result = Mockito.mock(ConfigItem.class);
    Mockito.when(underlying.get(ObjectId.of("Test", "Foo"), VersionCorrection.of(t1, t3))).thenReturn(result);
    assertSame(test.get(ObjectId.of("Test", "Foo"), VersionCorrection.ofCorrectedTo(t3)), result);
    // locks the v / c to t3 / t4
    result = Mockito.mock(ConfigItem.class);
    Mockito.when(underlying.get(ObjectId.of("Test", "Foo"), VersionCorrection.of(t3, t4))).thenReturn(result);
    assertSame(test.get(ObjectId.of("Test", "Foo"), VersionCorrection.of(t3, t4)), result);
  }

  /**
   * Tests getting an item via a class, name and version.
   */
  @SuppressWarnings({"unchecked", "rawtypes" })
  @Test
  public void testClassStringVersionCorrection() {
    final ConfigSource underlying = Mockito.mock(ConfigSource.class);
    final Instant t1 = Instant.ofEpochMilli(1L);
    final Instant t2 = Instant.ofEpochMilli(2L);
    final Instant t3 = Instant.ofEpochMilli(3L);
    final Instant t4 = Instant.ofEpochMilli(4L);
    final ConfigSource test = new VersionLockedConfigSource(underlying, VersionCorrection.of(t1, t2));
    Collection result = Collections.singleton(Mockito.mock(ConfigItem.class));
    Mockito.when(underlying.get(DateSet.class, "Foo", VersionCorrection.of(t1, t2))).thenReturn(result);
    assertSame(test.get(DateSet.class, "Foo", VersionCorrection.LATEST), result);
    assertSame(test.get(DateSet.class, "Foo", VersionCorrection.ofVersionAsOf(t1)), result);
    assertSame(test.get(DateSet.class, "Foo", VersionCorrection.ofCorrectedTo(t2)), result);
    result = Collections.singleton(Mockito.mock(ConfigItem.class));
    Mockito.when(underlying.get(DateSet.class, "Foo", VersionCorrection.of(t3, t2))).thenReturn(result);
    assertSame(test.get(DateSet.class, "Foo", VersionCorrection.ofVersionAsOf(t3)), result);
    result = Collections.singleton(Mockito.mock(ConfigItem.class));
    Mockito.when(underlying.get(DateSet.class, "Foo", VersionCorrection.of(t1, t3))).thenReturn(result);
    assertSame(test.get(DateSet.class, "Foo", VersionCorrection.ofCorrectedTo(t3)), result);
    result = Collections.singleton(Mockito.mock(ConfigItem.class));
    Mockito.when(underlying.get(DateSet.class, "Foo", VersionCorrection.of(t3, t4))).thenReturn(result);
    assertSame(test.get(DateSet.class, "Foo", VersionCorrection.of(t3, t4)), result);
  }

  /**
   * Tests getting multiple items via a class, name and version.
   */
  @SuppressWarnings({"unchecked", "rawtypes" })
  public void testGetAllClassVersionCorrection() {
    final ConfigSource underlying = Mockito.mock(ConfigSource.class);
    final Instant t1 = Instant.ofEpochMilli(1L);
    final Instant t2 = Instant.ofEpochMilli(2L);
    final Instant t3 = Instant.ofEpochMilli(3L);
    final Instant t4 = Instant.ofEpochMilli(4L);
    final ConfigSource test = new VersionLockedConfigSource(underlying, VersionCorrection.of(t1, t2));
    Collection result = Collections.singleton(Mockito.mock(ConfigItem.class));
    Mockito.when(underlying.getAll(DateSet.class, VersionCorrection.of(t1, t2))).thenReturn(result);
    assertSame(test.getAll(DateSet.class, VersionCorrection.LATEST), result);
    assertSame(test.getAll(DateSet.class, VersionCorrection.ofVersionAsOf(t1)), result);
    assertSame(test.getAll(DateSet.class, VersionCorrection.ofCorrectedTo(t2)), result);
    result = Collections.singleton(Mockito.mock(ConfigItem.class));
    Mockito.when(underlying.getAll(DateSet.class, VersionCorrection.of(t3, t2))).thenReturn(result);
    assertSame(test.getAll(DateSet.class, VersionCorrection.ofVersionAsOf(t3)), result);
    result = Collections.singleton(Mockito.mock(ConfigItem.class));
    Mockito.when(underlying.getAll(DateSet.class, VersionCorrection.of(t1, t3))).thenReturn(result);
    assertSame(test.getAll(DateSet.class, VersionCorrection.ofCorrectedTo(t3)), result);
    result = Collections.singleton(Mockito.mock(ConfigItem.class));
    Mockito.when(underlying.getAll(DateSet.class, VersionCorrection.of(t3, t4))).thenReturn(result);
    assertSame(test.getAll(DateSet.class, VersionCorrection.of(t3, t4)), result);
  }

  /**
   * Tests getting an item by a class type and unique identifier.
   */
  @Test
  public void testGetConfigClassUniqueId() {
    final ConfigSource underlying = Mockito.mock(ConfigSource.class);
    final ConfigSource test = new VersionLockedConfigSource(underlying, VersionCorrection.LATEST);
    final DateSet result = Mockito.mock(DateSet.class);
    Mockito.when(underlying.getConfig(DateSet.class, UniqueId.of("Test", "Foo"))).thenReturn(result);
    assertSame(test.getConfig(DateSet.class, UniqueId.of("Test", "Foo")), result);
  }

  /**
   * Tests getting an iten by class type, object id and version/correction.
   */
  @Test
  public void testGetConfigClassObjectIdVersionCorrection() {
    final ConfigSource underlying = Mockito.mock(ConfigSource.class);
    final Instant t1 = Instant.ofEpochMilli(1L);
    final Instant t2 = Instant.ofEpochMilli(2L);
    final Instant t3 = Instant.ofEpochMilli(3L);
    final Instant t4 = Instant.ofEpochMilli(4L);
    final ConfigSource test = new VersionLockedConfigSource(underlying, VersionCorrection.of(t1, t2));
    DateSet result = Mockito.mock(DateSet.class);
    Mockito.when(underlying.getConfig(DateSet.class, ObjectId.of("Test", "Foo"), VersionCorrection.of(t1, t2))).thenReturn(result);
    assertSame(test.getConfig(DateSet.class, ObjectId.of("Test", "Foo"), VersionCorrection.LATEST), result);
    assertSame(test.getConfig(DateSet.class, ObjectId.of("Test", "Foo"), VersionCorrection.ofVersionAsOf(t1)), result);
    assertSame(test.getConfig(DateSet.class, ObjectId.of("Test", "Foo"), VersionCorrection.ofCorrectedTo(t2)), result);
    result = Mockito.mock(DateSet.class);
    Mockito.when(underlying.getConfig(DateSet.class, ObjectId.of("Test", "Foo"), VersionCorrection.of(t3, t2))).thenReturn(result);
    assertSame(test.getConfig(DateSet.class, ObjectId.of("Test", "Foo"), VersionCorrection.ofVersionAsOf(t3)), result);
    result = Mockito.mock(DateSet.class);
    Mockito.when(underlying.getConfig(DateSet.class, ObjectId.of("Test", "Foo"), VersionCorrection.of(t1, t3))).thenReturn(result);
    assertSame(test.getConfig(DateSet.class, ObjectId.of("Test", "Foo"), VersionCorrection.ofCorrectedTo(t3)), result);
    result = Mockito.mock(DateSet.class);
    Mockito.when(underlying.getConfig(DateSet.class, ObjectId.of("Test", "Foo"), VersionCorrection.of(t3, t4))).thenReturn(result);
    assertSame(test.getConfig(DateSet.class, ObjectId.of("Test", "Foo"), VersionCorrection.of(t3, t4)), result);
  }

  /**
   * Tests getting an item by class, name and version/correction.
   */
  @Test
  public void testGetSingleClassStringVersionCorrection() {
    final ConfigSource underlying = Mockito.mock(ConfigSource.class);
    final Instant t1 = Instant.ofEpochMilli(1L);
    final Instant t2 = Instant.ofEpochMilli(2L);
    final Instant t3 = Instant.ofEpochMilli(3L);
    final Instant t4 = Instant.ofEpochMilli(4L);
    final ConfigSource test = new VersionLockedConfigSource(underlying, VersionCorrection.of(t1, t2));
    DateSet result = Mockito.mock(DateSet.class);
    Mockito.when(underlying.getSingle(DateSet.class, "Foo", VersionCorrection.of(t1, t2))).thenReturn(result);
    assertSame(test.getSingle(DateSet.class, "Foo", VersionCorrection.LATEST), result);
    assertSame(test.getSingle(DateSet.class, "Foo", VersionCorrection.ofVersionAsOf(t1)), result);
    assertSame(test.getSingle(DateSet.class, "Foo", VersionCorrection.ofCorrectedTo(t2)), result);
    result = Mockito.mock(DateSet.class);
    Mockito.when(underlying.getSingle(DateSet.class, "Foo", VersionCorrection.of(t3, t2))).thenReturn(result);
    assertSame(test.getSingle(DateSet.class, "Foo", VersionCorrection.ofVersionAsOf(t3)), result);
    result = Mockito.mock(DateSet.class);
    Mockito.when(underlying.getSingle(DateSet.class, "Foo", VersionCorrection.of(t1, t3))).thenReturn(result);
    assertSame(test.getSingle(DateSet.class, "Foo", VersionCorrection.ofCorrectedTo(t3)), result);
    result = Mockito.mock(DateSet.class);
    Mockito.when(underlying.getSingle(DateSet.class, "Foo", VersionCorrection.of(t3, t4))).thenReturn(result);
    assertSame(test.getSingle(DateSet.class, "Foo", VersionCorrection.of(t3, t4)), result);
  }

  /**
   * Gets the latest version of an item.
   */
  @Test
  public void testGetLatestByNameClassString() {
    final ConfigSource underlying = Mockito.mock(ConfigSource.class);
    final Instant t1 = Instant.ofEpochMilli(1L);
    final Instant t2 = Instant.ofEpochMilli(2L);
    final ConfigSource test = new VersionLockedConfigSource(underlying, VersionCorrection.of(t1, t2));
    final DateSet result = Mockito.mock(DateSet.class);
    Mockito.when(underlying.getSingle(DateSet.class, "Foo", VersionCorrection.of(t1, t2))).thenReturn(result);
    assertSame(test.getLatestByName(DateSet.class, "Foo"), result);
  }

}
