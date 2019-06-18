/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Iterator;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.id.ExternalId;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchSortOrder;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link ConfigSearchIterator}.
 */
@Test(groups = TestGroup.UNIT)
public class ConfigSearchIteratorTest {

  private ConfigDocument _doc1;
  private ConfigDocument _doc2;
  private ConfigDocument _doc3;
  private ConfigDocument _doc4;

  private ConfigMaster _configMaster;

  /**
   * Populates the master.
   *
   * @throws Exception
   *           if there is a problem
   */
  @BeforeMethod
  public void setUp() throws Exception {
    final ConfigItem<ExternalId> item1 = ConfigItem.of(ExternalId.of("A", "B"), "Test1");
    final ConfigItem<ExternalId> item2 = ConfigItem.of(ExternalId.of("C", "D"), "Test2");
    final ConfigItem<ExternalId> item3 = ConfigItem.of(ExternalId.of("E", "F"), "Test3");
    final ConfigItem<ExternalId> item4 = ConfigItem.of(ExternalId.of("E", "F"), "Test3");

    final InMemoryConfigMaster configMaster = new InMemoryConfigMaster();
    _doc1 = configMaster.add(new ConfigDocument(item1));
    _doc2 = configMaster.add(new ConfigDocument(item2));
    _doc3 = configMaster.add(new ConfigDocument(item3));
    _doc4 = configMaster.add(new ConfigDocument(item4));
    _configMaster = configMaster;
  }

  /**
   * Tears down the master.
   *
   * @throws Exception
   *           if there is a problem
   */
  @AfterMethod
  public void tearDown() throws Exception {
    _configMaster = null;
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that the master cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructor2ArgNullMaster() {
    new ConfigSearchIterator<>(null, new ConfigSearchRequest<>());
  }

  /**
   * Tests that the request cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructor2ArgNullRequest() {
    new ConfigSearchIterator<ExternalId>(_configMaster, null);
  }

  /**
   * Tests that the master cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryNullMaster() {
    ConfigSearchIterator.iterable(null, new ConfigSearchRequest<>());
  }

  /**
   * Tests that the request cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryNullRequest() {
    ConfigSearchIterator.iterable(_configMaster, null);
  }

  /**
   * Tests the iterator.
   */
  public void iterate() {
    final ConfigSearchRequest<ExternalId> request = new ConfigSearchRequest<>();
    request.setType(ExternalId.class);
    request.setSortOrder(ConfigSearchSortOrder.NAME_ASC);

    final ConfigSearchIterator<ExternalId> iterator1 = new ConfigSearchIterator<>(_configMaster, request);
    final Iterator<ConfigDocument> iterator2 = ConfigSearchIterator.iterable(_configMaster, request).iterator();

    assertTrue(iterator1.hasNext());
    assertTrue(iterator2.hasNext());
    assertEquals(iterator1.nextIndex(), 0);
    assertEquals(iterator1.next(), _doc1);
    assertEquals(iterator2.next(), _doc1);
    assertEquals(iterator1.nextIndex(), 1);

    assertTrue(iterator1.hasNext());
    assertTrue(iterator2.hasNext());
    assertEquals(iterator1.nextIndex(), 1);
    assertEquals(iterator1.next(), _doc2);
    assertEquals(iterator2.next(), _doc2);
    assertEquals(iterator1.nextIndex(), 2);

    assertTrue(iterator1.hasNext());
    assertTrue(iterator2.hasNext());
    assertEquals(iterator1.nextIndex(), 2);
    assertEquals(iterator1.next(), _doc3);
    assertEquals(iterator2.next(), _doc3);
    assertEquals(iterator1.nextIndex(), 3);

    assertTrue(iterator1.hasNext());
    assertTrue(iterator2.hasNext());
    assertEquals(iterator1.nextIndex(), 3);
    assertEquals(iterator1.next(), _doc4);
    assertEquals(iterator2.next(), _doc4);
    assertEquals(iterator1.nextIndex(), 4);

    assertFalse(iterator1.hasNext());
    assertFalse(iterator2.hasNext());
    assertEquals(iterator1.nextIndex(), 4);
  }

  /**
   * Tests the behaviour when the master throws an exception when searching.
   */
  @SuppressWarnings("unchecked")
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void iterateError1() {
    final ConfigSearchRequest<ExternalId> request = new ConfigSearchRequest<>();
    request.setType(ExternalId.class);
    request.setSortOrder(ConfigSearchSortOrder.NAME_ASC);

    final ConfigMaster mockMaster = mock(ConfigMaster.class);
    when(mockMaster.search(any(ConfigSearchRequest.class))).thenThrow(new IllegalStateException());

    final ConfigSearchIterator<ExternalId> iterator = new ConfigSearchIterator<>(mockMaster, request);
    iterator.hasNext();
  }

  /**
   * Tests the behaviour when the master throws an exception when searching.
   */
  @SuppressWarnings("unchecked")
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void iterateError2() {
    final ConfigSearchRequest<ExternalId> request = new ConfigSearchRequest<>();
    request.setType(ExternalId.class);
    request.setSortOrder(ConfigSearchSortOrder.NAME_ASC);

    final ConfigMaster mockMaster = mock(ConfigMaster.class);
    when(mockMaster.search(any(ConfigSearchRequest.class))).thenThrow(new IllegalStateException());

    final Iterator<ConfigDocument> iterator = ConfigSearchIterator.iterable(mockMaster, request).iterator();
    iterator.hasNext();
  }
}
