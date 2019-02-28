/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.position;

import static org.testng.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.joda.beans.Bean;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.UniqueId;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.util.test.TestGroup;
import com.opengamma.web.AbstractBeanTestCase;

/**
 * Tests for {@link WebPositionsData}.
 */
@Test(groups = TestGroup.UNIT)
public class WebPositionsDataTest extends AbstractBeanTestCase {
  private static final String NAME = "position";
  private static final String POSITION_URI = "pos";
  private static final String VERSION_URI = "version=1";
  private static final ManageablePosition POSITION = new ManageablePosition(BigDecimal.TEN, ExternalId.of("sec", "1"));
  private static final PositionDocument DOCUMENT = new PositionDocument();
  private static final PositionDocument VERSIONED = new PositionDocument();
  private static final Map<ExternalScheme, String> EXTERNAL_SCHEMES = new HashMap<>();
  static {
    DOCUMENT.setPosition(POSITION);
    VERSIONED.setPosition(POSITION);
    VERSIONED.setVersionFromInstant(Instant.now());
    EXTERNAL_SCHEMES.put(ExternalSchemes.OG_SYNTHETIC_TICKER, "pos");
  }
  private static final WebPositionsData DATA = new WebPositionsData();
  static {
    DOCUMENT.setUniqueId(UniqueId.of("pos", "0"));
    DATA.setPosition(DOCUMENT);
    DATA.setUriPositionId(POSITION_URI);
    DATA.setUriVersionId(VERSION_URI);
    DATA.setExternalSchemes(EXTERNAL_SCHEMES);
  }

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(WebPositionsData.class, Arrays.asList("uriPositionId", "uriVersionId", "position", "versioned", "externalSchemes"),
        Arrays.asList(POSITION_URI, VERSION_URI, DOCUMENT, VERSIONED, EXTERNAL_SCHEMES),
        Arrays.asList(VERSION_URI, POSITION_URI, VERSIONED, DOCUMENT, new HashMap<>()));
  }

  /**
   * Tests getting the best position if the override id is not null.
   */
  public void testBestPositionOverrideId() {
    final UniqueId uid = UniqueId.of("pos", "1");
    assertEquals(DATA.getBestPositionUriId(uid), uid.toString());
  }

  /**
   * Tests getting the best position if there is no position document.
   */
  public void testBestPositionNoPositionDocument() {
    final WebPositionsData data = DATA.clone();
    data.setPosition(null);
    assertEquals(data.getBestPositionUriId(null), POSITION_URI);
  }

  /**
   * Tests getting the best position from the document.
   */
  public void testBestPositionFromDocument() {
    assertEquals(DATA.getBestPositionUriId(null), DOCUMENT.getUniqueId().toString());
  }

}
