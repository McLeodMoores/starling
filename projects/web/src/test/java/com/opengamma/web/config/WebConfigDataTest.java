/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.config;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.joda.beans.Bean;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.opengamma.core.DateSet;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.id.UniqueId;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.util.test.TestGroup;
import com.opengamma.web.AbstractBeanTestCase;

/**
 * Tests for {@link WebConfigData}.
 */
@Test(groups = TestGroup.UNIT)
public class WebConfigDataTest extends AbstractBeanTestCase {
  private static final String NAME = "config";
  private static final String CONFIG_URI = "cfg";
  private static final String VERSION_URI = "version=1";
  private static final ConfigItem<DateSet> CONFIG = ConfigItem.of(DateSet.of(Collections.singleton(LocalDate.now())));
  private static final ConfigDocument DOCUMENT = new ConfigDocument(CONFIG);
  private static final ConfigDocument VERSIONED = new ConfigDocument(CONFIG);
  static {
    CONFIG.setName(NAME);
    DOCUMENT.setConfig(CONFIG);
    VERSIONED.setConfig(CONFIG);
    VERSIONED.setVersionFromInstant(Instant.now());
  }
  private static final WebConfigData DATA = new WebConfigData();
  static {
    DOCUMENT.setUniqueId(UniqueId.of("cfg", "0"));
    DATA.setConfig(DOCUMENT);
    DATA.setUriConfigId(CONFIG_URI);
    DATA.setUriVersionId(VERSION_URI);
    DATA.setType(CONFIG.getClass());
  }

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(WebConfigData.class,
        Arrays.asList("type", "uriConfigId", "uriVersionId", "config", "versioned"), Arrays.asList(DateSet.class, CONFIG_URI, VERSION_URI, DOCUMENT, VERSIONED),
        Arrays.asList(Object.class, VERSION_URI, CONFIG_URI, VERSIONED, DOCUMENT));
  }

  /**
   * Tests getting the best config if the override id is not null.
   */
  public void testBestConfigOverrideId() {
    final UniqueId uid = UniqueId.of("sec", "1");
    assertEquals(DATA.getBestConfigUriId(uid), uid.toString());
  }

  /**
   * Tests getting the best config if there is no config document.
   */
  public void testBestConfigNoConfigDocument() {
    final WebConfigData data = DATA.clone();
    data.setConfig(null);
    assertEquals(data.getBestConfigUriId(null), CONFIG_URI);
  }

  /**
   * Tests getting the best config from the document.
   */
  public void testBestConfigFromDocument() {
    assertEquals(DATA.getBestConfigUriId(null), DOCUMENT.getUniqueId().toString());
  }

  /**
   * Fudge does not deserialize the data correctly.
   */
  @Override
  @Test(dataProvider = "propertyValues")
  protected <TYPE extends Bean> void testCycle(final JodaBeanProperties<TYPE> properties) {
    final TYPE data = constructAndPopulateBeanBuilder(properties).build();
    assertEquals(data, cycleObjectJodaXml(properties.getType(), data));
  }

}
