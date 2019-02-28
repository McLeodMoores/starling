/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.convention;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.joda.beans.Bean;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mcleodmoores.web.json.DepositConventionJsonBuilder;
import com.opengamma.financial.convention.BondConvention;
import com.opengamma.financial.convention.EquityConvention;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.util.test.TestGroup;
import com.opengamma.web.AbstractBeanTestCase;
import com.opengamma.web.json.JSONBuilder;

/**
 * Tests for {@link WebConventionData}.
 */
@Test(groups = TestGroup.UNIT)
public class WebConventionDataTest extends AbstractBeanTestCase {
  private static final String NAME = "convention";
  private static final ExternalIdBundle IDS = ExternalIdBundle.of("eid", "1");
  private static final String CONVENTION_URI = "cnv";
  private static final String VERSION_URI = "version=1";
  private static final ManageableConvention CONVENTION = new EquityConvention(NAME, IDS, 7);
  private static final ConventionDocument DOCUMENT = new ConventionDocument();
  private static final ConventionDocument VERSIONED = new ConventionDocument();
  private static final BiMap<String, Class<? extends ManageableConvention>> CONVENTION_TYPES = HashBiMap.create();
  private static final BiMap<String, Class<? extends ManageableConvention>> CLASS_NAMES = HashBiMap.create();
  private static final Map<Class<?>, JSONBuilder<?>> JSON_BUILDER_MAP = new HashMap<>();
  static {
    DOCUMENT.setConvention(CONVENTION);
    VERSIONED.setConvention(CONVENTION);
    VERSIONED.setVersionFromInstant(Instant.now());
    CONVENTION_TYPES.put("equity", EquityConvention.class);
    CLASS_NAMES.put("equity", EquityConvention.class);
    JSON_BUILDER_MAP.put(EquityConvention.class, DepositConventionJsonBuilder.INSTANCE);
  }
  private static final WebConventionData DATA = new WebConventionData();
  static {
    DOCUMENT.setUniqueId(UniqueId.of("conv", "0"));
    DATA.setConvention(DOCUMENT);
    DATA.setUriConventionId(CONVENTION_URI);
    DATA.setUriVersionId(VERSION_URI);
    DATA.setType(CONVENTION.getClass());
  }

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(WebConventionData.class,
        Arrays.asList("type", "uriConventionId", "uriVersionId", "convention", "versioned", "typeMap", "classNameMap", "jsonBuilderMap"),
        Arrays.asList(CONVENTION.getClass(), CONVENTION_URI, VERSION_URI, DOCUMENT, VERSIONED, CONVENTION_TYPES, CLASS_NAMES, JSON_BUILDER_MAP),
        Arrays.asList(BondConvention.class, VERSION_URI, CONVENTION_URI, VERSIONED, DOCUMENT, HashBiMap.create(), HashBiMap.create(), new HashMap<>()));
  }

  /**
   * Tests getting the best convention if the override id is not null.
   */
  public void testBestConventionOverrideId() {
    final UniqueId uid = UniqueId.of("conv", "1");
    assertEquals(DATA.getBestConventionUriId(uid), uid.toString());
  }

  /**
   * Tests getting the best convention if there is no convention document.
   */
  public void testBestConventionNoConventionDocument() {
    final WebConventionData data = DATA.clone();
    data.setConvention(null);
    assertEquals(data.getBestConventionUriId(null), CONVENTION_URI);
  }

  /**
   * Tests getting the best convention from the document.
   */
  public void testBestConventionFromDocument() {
    assertEquals(DATA.getBestConventionUriId(null), DOCUMENT.getUniqueId().toString());
  }

  /**
   * Fudge does not deserialize the maps correctly.
   */
  @Override
  @Test(dataProvider = "propertyValues")
  protected <TYPE extends Bean> void testCycle(final JodaBeanProperties<TYPE> properties) {
  }

}
