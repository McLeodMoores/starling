/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.config;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

import org.joda.beans.JodaBeanUtils;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.id.UniqueId;
import com.opengamma.master.config.ConfigDocument.Meta;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRating;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingRule;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ConfigDocument}.
 */
@Test(groups = TestGroup.UNIT)
public class ConfigDocumentTest extends AbstractFudgeBuilderTestCase {
  private static final List<HistoricalTimeSeriesRatingRule> RULES = Arrays.asList(
      HistoricalTimeSeriesRatingRule.of(HistoricalTimeSeriesRatingFieldNames.DATA_PROVIDER_NAME, "value1", 0),
      HistoricalTimeSeriesRatingRule.of(HistoricalTimeSeriesRatingFieldNames.DATA_SOURCE_NAME, "value2", 1));
  private static final HistoricalTimeSeriesRating RATING = HistoricalTimeSeriesRating.of(RULES);
  private static final ConfigItem<HistoricalTimeSeriesRating> ITEM = ConfigItem.of(RATING);
  private static final UniqueId UID = UniqueId.of("conf", "1");
  private static final String NAME = "CONFIG";
  private static final Instant VERSION_FROM = Instant.ofEpochSecond(100);
  private static final Instant VERSION_TO = Instant.ofEpochSecond(200);
  private static final Instant CORRECTION_FROM = Instant.ofEpochSecond(150);
  private static final Instant CORRECTION_TO = Instant.ofEpochSecond(250);
  static {
    ITEM.setName(NAME);
  }

  /**
   * Tests that the name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName() {
    new ConfigDocument(RATING, HistoricalTimeSeriesRating.class, null, UID, VERSION_FROM, VERSION_TO, CORRECTION_FROM, CORRECTION_TO);
  }

  /**
   * Tests constructor equivalence.
   */
  @Test
  public void testConstructors() {
    ConfigItem<?> item = ITEM.clone();
    item.setUniqueId(UID);
    final ConfigDocument doc = new ConfigDocument(item);
    doc.setVersionFromInstant(VERSION_FROM);
    doc.setVersionToInstant(VERSION_TO);
    doc.setCorrectionFromInstant(CORRECTION_FROM);
    doc.setCorrectionToInstant(CORRECTION_TO);
    final ConfigDocument other = new ConfigDocument(RATING, HistoricalTimeSeriesRating.class, NAME, UID, VERSION_FROM, VERSION_TO, CORRECTION_FROM,
        CORRECTION_TO);
    // config doesn't have a uid
    assertNotEquals(doc.getUniqueId(), other.getUniqueId());
    JodaBeanUtils.equalIgnoring(doc, other, ConfigDocument.meta().metaProperty("uniqueId"));
    final NamelessConfig config = NamelessConfig.of(53);
    config.setUniqueId(UID);
    item = ConfigItem.of(config);
    doc.setConfig(item);
    other.setConfig(item);
    assertEquals(doc, other);
  }

  /**
   * Tests the unique id getter.
   */
  @Test
  public void testGetUniqueId() {
    final ConfigDocument doc = new ConfigDocument(ITEM);
    assertNull(doc.getUniqueId());
    doc.setConfig(null);
    assertNull(doc.getUniqueId());
    final ConfigItem<HistoricalTimeSeriesRating> item = ITEM.clone();
    item.setName(NAME);
    item.setUniqueId(UID);
    doc.setConfig(item);
    assertEquals(doc.getUniqueId(), UID);
    doc.setUniqueId(null);
    item.setUniqueId(UID);
    assertEquals(doc.getUniqueId(), UID);
  }

  /**
   * Tests the unique id setter.
   */
  @Test
  public void testSetUniqueId() {
    final ConfigItem<HistoricalTimeSeriesRating> item = ITEM.clone();
    item.setUniqueId(UID);
    item.setName(NAME);
    final ConfigDocument doc = new ConfigDocument(item);
    final UniqueId uid1 = UniqueId.of("uid", "2");
    final UniqueId uid2 = UniqueId.of("uid", "3");
    assertEquals(doc.getUniqueId(), UID);
    assertEquals(item.getUniqueId(), UID);
    assertEquals(doc.getObjectId(), item.getUniqueId().getObjectId());
    doc.setUniqueId(uid1);
    assertEquals(doc.getUniqueId(), uid1);
    assertEquals(item.getUniqueId(), uid1);
    assertEquals(doc.getObjectId(), item.getUniqueId().getObjectId());
    doc.setConfig(null);
    doc.setUniqueId(uid2);
    assertEquals(doc.getUniqueId(), uid2);
    assertEquals(item.getUniqueId(), uid1);
  }

  /**
   * Tests the name getter.
   */
  @Test
  public void testGetName() {
    final ConfigItem<HistoricalTimeSeriesRating> item = ITEM.clone();
    ConfigDocument doc = new ConfigDocument(item);
    assertEquals(doc.getName(), NAME);
    doc.setConfig(null);
    assertEquals(doc.getName(), NAME);
    item.setName(NAME);
    doc.setConfig(item);
    assertEquals(doc.getName(), NAME);
    doc = new ConfigDocument(ConfigItem.of(NamelessConfig.of(6)));
    assertNull(doc.getName());
  }

  /**
   * Tests the name setter.
   */
  @Test
  public void testSetName() {
    final ConfigItem<HistoricalTimeSeriesRating> item = ITEM.clone();
    item.setName(NAME);
    ConfigDocument doc = new ConfigDocument(item);
    final String name1 = "CONFIG1";
    final String name2 = "CONFIG2";
    assertEquals(doc.getName(), NAME);
    assertEquals(item.getName(), NAME);
    doc.setName(name1);
    assertEquals(doc.getName(), name1);
    assertEquals(item.getName(), name1);
    doc.setConfig(null);
    doc.setName(name2);
    assertEquals(doc.getName(), name2);
    assertEquals(item.getName(), name1);
    doc = new ConfigDocument(ConfigItem.of(NamelessConfig.of(6)));
    doc.setName(name1);
    assertEquals(doc.getName(), name1);
  }

  /**
   * Tests the config setter.
   */
  @Test
  public void testConfigSetter() {
    final ConfigItem<HistoricalTimeSeriesRating> item = ITEM.clone();
    ConfigDocument doc = new ConfigDocument(item);
    doc.setConfig(null); // fine
    assertNull(doc.getConfig());
    doc.setConfig(item);
    assertEquals(doc.getConfig(), ITEM);
    assertEquals(doc.getName(), NAME);
    assertNull(doc.getUniqueId());
    item.setUniqueId(UID);
    assertEquals(doc.getConfig(), item);
    assertEquals(doc.getName(), NAME);
    assertEquals(doc.getUniqueId(), UID);
    item.setUniqueId(null);
    assertEquals(doc.getConfig(), ITEM);
    assertEquals(doc.getName(), NAME);
    assertEquals(doc.getUniqueId(), UID);
    final ConfigItem<NamelessConfig> nameless = ConfigItem.of(NamelessConfig.of(4));
    doc = new ConfigDocument(nameless);
    assertNull(doc.getName());
    assertNull(doc.getUniqueId());
    // bit artificial but it does mean the name isn't populated
    doc.setConfig(nameless);
    assertEquals(doc.getConfig(), nameless);
    assertNull(doc.getName());
    assertNull(doc.getUniqueId());
    nameless.setUniqueId(UID);
    doc.setConfig(nameless);
    assertEquals(doc.getConfig(), nameless);
    assertNull(doc.getName());
    assertEquals(doc.getUniqueId(), UID);
    nameless.setUniqueId(null);
    doc = new ConfigDocument(nameless);
    doc.setName(NAME);
    doc.setConfig(nameless);
    assertEquals(doc.getConfig(), nameless);
    assertEquals(doc.getName(), NAME);
    assertNull(doc.getUniqueId());
    doc.setUniqueId(UID);
    doc.setConfig(nameless);
    assertEquals(doc.getConfig(), nameless);
    assertEquals(doc.getName(), NAME);
    assertEquals(doc.getUniqueId(), UID);
  }

  /**
   * Test value getter.
   */
  @Test
  public void testValueGetter() {
    final ConfigDocument doc = new ConfigDocument(ITEM);
    assertEquals(doc.getValue(), ITEM);
    final ConfigItem<NamelessConfig> item = ConfigItem.of(NamelessConfig.of(2));
    doc.setConfig(item);
    assertEquals(doc.getValue(), item);
  }

  /**
   * Tests the type getter.
   */
  @Test
  public void testTypeGetter() {
    final ConfigDocument doc = new ConfigDocument(ITEM);
    assertEquals(doc.getType(), HistoricalTimeSeriesRating.class);
    final ConfigItem<NamelessConfig> item = ConfigItem.of(NamelessConfig.of(2));
    doc.setConfig(item);
    assertEquals(doc.getType(), NamelessConfig.class);
    doc.setConfig(null);
    assertNull(doc.getType());
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final ConfigDocument doc = new ConfigDocument(ITEM);
    final ConfigDocument other = new ConfigDocument(ITEM.clone());
    assertEquals(doc, doc);
    assertEquals(doc.toString(),
        "ConfigDocument{versionFromInstant=null, versionToInstant=null, correctionFromInstant=null, "
            + "correctionToInstant=null, config=ConfigItem{value=HistoricalTimeSeriesRating{rules=[HistoricalTimeSeriesRatingRule{fieldName=dataProvider, "
            + "fieldValue=value1, rating=0}, HistoricalTimeSeriesRatingRule{fieldName=dataSource, fieldValue=value2, rating=1}]}, "
            + "uniqueId=null, name=CONFIG, type=class com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRating}, uniqueId=null}");
    assertEquals(doc, other);
    assertEquals(doc.hashCode(), other.hashCode());
    ConfigItem<?> item = ConfigItem.of(HistoricalTimeSeriesRating.of(Arrays.asList(RULES.get(0))));
    item.setName(NAME);
    other.setConfig(item);
    assertNotEquals(doc, other);
    item = ConfigItem.of(NamelessConfig.of(6));
    other.setConfig(item);
    assertNotEquals(doc, other);
    item = ITEM.clone();
    other.setConfig(item);
    other.setCorrectionFromInstant(CORRECTION_FROM);
    assertNotEquals(doc, other);
    other.setCorrectionFromInstant(CORRECTION_FROM);
    other.setCorrectionToInstant(CORRECTION_TO);
    assertNotEquals(doc, other);
    other.setCorrectionToInstant(CORRECTION_TO);
    other.setVersionFromInstant(VERSION_FROM);
    assertNotEquals(doc, other);
    other.setVersionFromInstant(VERSION_FROM);
    other.setVersionToInstant(VERSION_TO);
    assertNotEquals(doc, other);
    other.setVersionToInstant(VERSION_TO);
    other.setUniqueId(UniqueId.of("uid", "2"));
    assertNotEquals(doc, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final ConfigItem<HistoricalTimeSeriesRating> item = ITEM.clone();
    item.setUniqueId(UID);
    final ConfigDocument doc = new ConfigDocument(item);
    doc.setCorrectionFromInstant(CORRECTION_FROM);
    doc.setCorrectionToInstant(CORRECTION_TO);
    doc.setVersionFromInstant(VERSION_FROM);
    doc.setVersionToInstant(VERSION_TO);
    assertEquals(doc.propertyNames().size(), 6);
    final Meta bean = doc.metaBean();
    assertEquals(bean.config().get(doc), item);
    assertEquals(bean.correctionFromInstant().get(doc), CORRECTION_FROM);
    assertEquals(bean.correctionToInstant().get(doc), CORRECTION_TO);
    assertEquals(bean.uniqueId().get(doc), UID);
    assertEquals(bean.versionFromInstant().get(doc), VERSION_FROM);
    assertEquals(bean.versionToInstant().get(doc), VERSION_TO);
    assertEquals(doc.property("config").get(), item);
    assertEquals(doc.property("correctionFromInstant").get(), CORRECTION_FROM);
    assertEquals(doc.property("correctionToInstant").get(), CORRECTION_TO);
    assertEquals(doc.property("uniqueId").get(), UID);
    assertEquals(doc.property("versionFromInstant").get(), VERSION_FROM);
    assertEquals(doc.property("versionToInstant").get(), VERSION_TO);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final ConfigDocument doc = new ConfigDocument(RATING, HistoricalTimeSeriesRating.class, NAME, UID, VERSION_FROM, VERSION_TO, CORRECTION_FROM,
        CORRECTION_TO);
    assertEncodeDecodeCycle(ConfigDocument.class, doc);
  }

  private static class NamelessConfig {

    public static NamelessConfig of(final int n) {
      return new NamelessConfig(n);
    }

    private final int _n;
    private UniqueId _uid;

    private NamelessConfig(final int n) {
      _n = n;
    }

    public void setUniqueId(final UniqueId uid) {
      _uid = uid;
    }

    @Override
    public int hashCode() {
      return _n;
    }

    @Override
    public boolean equals(final Object o) {
      return o instanceof NamelessConfig && _n == ((NamelessConfig) o)._n && _uid.equals(((NamelessConfig) o)._uid);
    }
  }
}
