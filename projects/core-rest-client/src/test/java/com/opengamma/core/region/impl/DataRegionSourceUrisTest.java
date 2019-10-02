/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.region.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.net.URI;
import java.net.URISyntaxException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link DataRegionSourceUris}.
 */
@Test(groups = TestGroup.UNIT)
public class DataRegionSourceUrisTest {
  private static final VersionCorrection VC = VersionCorrection.of(Instant.ofEpochSecond(10000), Instant.ofEpochSecond(20000));
  private static final UniqueId UID_1 = UniqueId.of("reg", "1");
  private static final ObjectId OID = UID_1.getObjectId();
  private static final ExternalIdBundle EIDS = ExternalIdBundle.of(ExternalId.of("eid", "1"), ExternalId.of("eid", "2"));
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
  public void testUriGetNullUri1() {
    DataRegionSourceUris.uriGet(null, OID, VC);
  }

  /**
   * Tests that the object id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetNullObjectId() {
    DataRegionSourceUris.uriGet(_baseUri, null, VC);
  }

  /**
   * Tests the URI that is built when the version correction is null (i.e. LATEST is required).
   */
  public void testBuildUriOidLatest() {
    final URI uri = DataRegionSourceUris.uriGet(_baseUri, OID, null);
    assertEquals(uri.getPath(), "path/to/regions/reg~1");
    assertNull(uri.getQuery());
  }

  /**
   * Tests the URI that is built.
   */
  public void testBuildUriOidVersion() {
    final URI uri = DataRegionSourceUris.uriGet(_baseUri, OID, VC);
    assertEquals(uri.getPath(), "path/to/regions/reg~1");
    assertEquals(uri.getQuery(), "versionAsOf=1970-01-01T02:46:40Z&correctedTo=1970-01-01T05:33:20Z");
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetNullUri2() {
    DataRegionSourceUris.uriGet(null, UID_1);
  }

  /**
   * Tests that the unique id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetNullUid() {
    DataRegionSourceUris.uriGet(_baseUri, null);
  }

  /**
   * Tests the URI that is built.
   */
  public void testBuildUriUid() {
    final URI uri = DataRegionSourceUris.uriGet(_baseUri, UID_1);
    assertEquals(uri.getPath(), "path/to/regions/reg~1");
    assertNull(uri.getQuery());
  }

  /**
   * Tests the URI that is built.
   */
  public void testBuildUriUidWithVersion() {
    final UniqueId uid = UID_1.withVersion(Instant.ofEpochMilli(10).toString());
    final URI uri = DataRegionSourceUris.uriGet(_baseUri, uid);
    assertEquals(uri.getPath(), "path/to/regions/reg~1");
    assertEquals(uri.getQuery(), "version=1970-01-01T00:00:00.010Z");
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriSearchNullUri() {
    DataRegionSourceUris.uriSearch(null, VC, EIDS);
  }

  /**
   * Tests that the bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriSearchNullBundle() {
    DataRegionSourceUris.uriSearch(_baseUri, VC, null);
  }

  /**
   * Tests the URI that is built when the version correction is null (i.e. LATEST is required).
   */
  public void testSearchLatest() {
    final URI uri = DataRegionSourceUris.uriSearch(_baseUri, null, EIDS);
    assertEquals(uri.getPath(), "path/to/regions");
    assertEquals(uri.getQuery(), "id=eid~1&id=eid~2");
  }

  /**
   * Tests the URI that is built.
   */
  public void testSearch() {
    final URI uri = DataRegionSourceUris.uriSearch(_baseUri, VC, EIDS);
    assertEquals(uri.getPath(), "path/to/regions");
    assertEquals(uri.getQuery(), "versionAsOf=1970-01-01T02:46:40Z&correctedTo=1970-01-01T05:33:20Z&id=eid~1&id=eid~2");
  }

  /**
   * Tests that the bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSearchHighestNullBundle() {
    DataRegionSourceUris.uriSearchHighest(_baseUri, null);
  }

  /**
   * Tests the URI that is built.
   */
  public void testSearchHighest() {
    final URI uri = DataRegionSourceUris.uriSearchHighest(_baseUri, EIDS);
    assertEquals(uri.getPath(), "path/to/regionSearches/highest");
    assertEquals(uri.getQuery(), "id=eid~1&id=eid~2");
  }
}
