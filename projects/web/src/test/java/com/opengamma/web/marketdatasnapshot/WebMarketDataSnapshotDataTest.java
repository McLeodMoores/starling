/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.marketdatasnapshot;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.id.UniqueId;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.util.test.TestGroup;
import com.opengamma.web.AbstractBeanTestCase;

/**
 * Tests for {@link WebMarketDataSnapshotData}.
 */
@Test(groups = TestGroup.UNIT)
public class WebMarketDataSnapshotDataTest extends AbstractBeanTestCase {
  private static final String NAME = "snapshot";
  private static final String SNAPSHOT_URI = "snp";
  private static final String VERSION_URI = "version=1";
  private static final ManageableMarketDataSnapshot SNAPSHOT = new ManageableMarketDataSnapshot();
  private static final MarketDataSnapshotDocument DOCUMENT = new MarketDataSnapshotDocument();
  private static final MarketDataSnapshotDocument VERSIONED = new MarketDataSnapshotDocument();
  static {
    SNAPSHOT.setName(NAME);
    DOCUMENT.setNamedSnapshot(SNAPSHOT);
    VERSIONED.setNamedSnapshot(SNAPSHOT);
    VERSIONED.setVersionFromInstant(Instant.now());
  }
  private static final WebMarketDataSnapshotData DATA = new WebMarketDataSnapshotData();
  static {
    DOCUMENT.setUniqueId(UniqueId.of("snp", "0"));
    DATA.setSnapshot(DOCUMENT);
    DATA.setUriSnapshotId(SNAPSHOT_URI);
    DATA.setUriVersionId(VERSION_URI);
  }

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(WebMarketDataSnapshotData.class, Arrays.asList("uriSnapshotId", "uriVersionId", "snapshot", "versioned"),
        Arrays.asList(SNAPSHOT_URI, VERSION_URI, DOCUMENT, VERSIONED), Arrays.asList(VERSION_URI, SNAPSHOT_URI, VERSIONED, DOCUMENT));
  }

  /**
   * Tests getting the best snapshot if the override id is not null.
   */
  public void testBestSnapshotOverrideId() {
    final UniqueId uid = UniqueId.of("snp", "1");
    assertEquals(DATA.getBestSnapshotUriId(uid), uid.toString());
  }

  /**
   * Tests getting the best snapshot if there is no snapshot document.
   */
  public void testBestSnapshotNoSnapshotDocument() {
    final WebMarketDataSnapshotData data = DATA.clone();
    data.setSnapshot(null);
    assertEquals(data.getBestSnapshotUriId(null), SNAPSHOT_URI);
  }

  /**
   * Tests getting the best snapshot from the document.
   */
  public void testBestSnapshotFromDocument() {
    assertEquals(DATA.getBestSnapshotUriId(null), DOCUMENT.getUniqueId().toString());
  }

}
