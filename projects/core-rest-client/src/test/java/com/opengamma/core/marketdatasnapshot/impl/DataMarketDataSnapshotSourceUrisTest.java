/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.marketdatasnapshot.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.net.URI;
import java.net.URISyntaxException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link DataMarketDataSnapshotSourceUris}.
 */
@Test(groups = TestGroup.UNIT)
public class DataMarketDataSnapshotSourceUrisTest {
  private static final VersionCorrection VC = VersionCorrection.of(Instant.ofEpochSecond(10000), Instant.ofEpochSecond(20000));
  private static final UniqueId UID = UniqueId.of("snp", "1");
  private static final ObjectId OID = UID.getObjectId();
  private static final String NAME = "snapshot name";
  private URI _baseUri;

  /**
   * Sets up the URI.
   *
   * @throws URISyntaxException
   *           if the path is wrong
   */
  @BeforeMethod
  public void createUri() throws URISyntaxException {
    _baseUri = new URI("path/to/");
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetNullBaseUri1() {
    DataMarketDataSnapshotSourceUris.uriGet(null, UID);
  }

  /**
   * Tests that the unique id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetNullUid() {
    DataMarketDataSnapshotSourceUris.uriGet(_baseUri, (UniqueId) null);
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriGetUidNoVersion() {
    final URI uri = DataMarketDataSnapshotSourceUris.uriGet(_baseUri, UID);
    assertEquals(uri.getPath(), "path/to/snapshots/" + UID.toString());
    assertNull(uri.getQuery());
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriGetUidVersion() {
    final UniqueId uid = UID.withVersion(VC.toString());
    final URI uri = DataMarketDataSnapshotSourceUris.uriGet(_baseUri, uid);
    assertEquals(uri.getPath(), "path/to/snapshots/" + UID.toString());
    assertEquals(uri.getQuery(), "version=" + VC.toString());
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetNullBaseUri2() {
    DataMarketDataSnapshotSourceUris.uriGet(null, OID);
  }

  /**
   * Tests that the object id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetNullOid1() {
    DataMarketDataSnapshotSourceUris.uriGet(_baseUri, (ObjectId) null);
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriGetOidNoVersion1() {
    final URI uri = DataMarketDataSnapshotSourceUris.uriGet(_baseUri, OID);
    assertEquals(uri.getPath(), "path/to/snapshots/" + OID.toString());
    assertNull(uri.getQuery());
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriGetOidNoVersion2() {
    final URI uri = DataMarketDataSnapshotSourceUris.uriGet(_baseUri, OID, null);
    assertEquals(uri.getPath(), "path/to/snapshots/" + OID.toString());
    assertNull(uri.getQuery());
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriGetOidVersion() {
    final URI uri = DataMarketDataSnapshotSourceUris.uriGet(_baseUri, OID, VC);
    assertEquals(uri.getPath(), "path/to/snapshots/" + OID.toString());
    assertEquals(uri.getQuery(), "versionAsOf=1970-01-01T02:46:40Z&correctedTo=1970-01-01T05:33:20Z");
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetNullBaseUri3() {
    DataMarketDataSnapshotSourceUris.uriGet(null, OID, null);
  }

  /**
   * Tests that the object id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetNullOid2() {
    DataMarketDataSnapshotSourceUris.uriGet(_baseUri, null, VC);
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriSearchSingleNullBaseUri() {
    DataMarketDataSnapshotSourceUris.uriSearchSingle(null, Object.class, NAME, VC);
  }

  /**
   * Tests that the type cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriSearchSingleNullType() {
    DataMarketDataSnapshotSourceUris.uriSearchSingle(_baseUri, null, NAME, VC);
  }

  /**
   * Tests that the name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriSearchSingleNullName() {
    DataMarketDataSnapshotSourceUris.uriSearchSingle(_baseUri, Object.class, null, VC);
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriSearchSingleNoVersion() {
    final URI uri = DataMarketDataSnapshotSourceUris.uriSearchSingle(_baseUri, Object.class, NAME, null);
    assertEquals(uri.getPath(), "path/to/snapshotSearches/single");
    assertEquals(uri.getQuery(), "name=snapshot+name&type=java.lang.Object&versionCorrection=VLATEST.CLATEST");
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriSearchSingleVersion() {
    final URI uri = DataMarketDataSnapshotSourceUris.uriSearchSingle(_baseUri, Object.class, NAME, VC);
    assertEquals(uri.getPath(), "path/to/snapshotSearches/single");
    assertEquals(uri.getQuery(), "name=snapshot+name&type=java.lang.Object&versionCorrection=V1970-01-01T02:46:40Z.C1970-01-01T05:33:20Z");
  }
}
