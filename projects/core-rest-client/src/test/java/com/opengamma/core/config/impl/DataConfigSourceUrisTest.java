/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.config.impl;

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
 * Tests for {@link DataConfigSourceUris}.
 */
@Test(groups = TestGroup.UNIT)
public class DataConfigSourceUrisTest {
  private static final String NAME = "config name";
  private static final VersionCorrection VC = VersionCorrection.of(Instant.ofEpochMilli(10000), Instant.ofEpochMilli(20000));
  private static final Class<?> TYPE = Object.class;
  private static final UniqueId UID = UniqueId.of("cfg", "1");
  private static final ObjectId OID = UID.getObjectId();
  private URI _baseUri;

  /**
   * Sets up the URI
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
    DataConfigSourceUris.uriGet(null, NAME, VC, TYPE);
  }

  /**
   * Tests that the name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetNullName() {
    DataConfigSourceUris.uriGet(_baseUri, null, VC, TYPE);
  }

  /**
   * Tests that the type cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetNullType() {
    DataConfigSourceUris.uriGet(_baseUri, NAME, VC, null);
  }

  /**
   * Tests the URI that is built when the version correction is null (i.e.
   * LATEST is required).
   */
  public void testBuildUriNameTypeLatest() {
    final URI uri = DataConfigSourceUris.uriGet(_baseUri, NAME, null, TYPE);
    assertEquals(uri.getPath(), "path/to/configs");
    assertEquals(uri.getQuery(), "name=config+name&type=java.lang.Object&versionCorrection=VLATEST.CLATEST");
  }

  /**
   * Tests the URI that is built.
   */
  public void testBuildUriNameType() {
    final URI uri = DataConfigSourceUris.uriGet(_baseUri, NAME, VC, TYPE);
    assertEquals(uri.getPath(), "path/to/configs");
    assertEquals(uri.getQuery(), "name=config+name&type=java.lang.Object&versionCorrection=V1970-01-01T00:00:10Z.C1970-01-01T00:00:20Z");
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetNullUri2() {
    DataConfigSourceUris.uriGet(null, UID);
  }

  /**
   * Tests that the unique id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetNullUid() {
    DataConfigSourceUris.uriGet(_baseUri, null);
  }

  /**
   * Tests the URI that is built.
   */
  public void testBuildUriUid() {
    final URI uri = DataConfigSourceUris.uriGet(_baseUri, UID);
    assertEquals(uri.getPath(), "path/to/configs/cfg~1");
    assertNull(uri.getQuery());
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetNullUri3() {
    DataConfigSourceUris.uriGet(null, OID, null);
  }

  /**
   * Tests that the unique id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetNullOid() {
    DataConfigSourceUris.uriGet(_baseUri, null, null);
  }

  /**
   * Tests the URI that is built.
   */
  public void testBuildUriOidLatest() {
    final URI uri = DataConfigSourceUris.uriGet(_baseUri, OID, null);
    assertEquals(uri.getPath(), "path/to/configs/cfg~1/VLATEST.CLATEST");
    assertNull(uri.getQuery());
  }

  /**
   * Tests the URI that is built.
   */
  public void testBuildUriOid() {
    final URI uri = DataConfigSourceUris.uriGet(_baseUri, OID, VC);
    assertEquals(uri.getPath(), "path/to/configs/cfg~1/V1970-01-01T00:00:10Z.C1970-01-01T00:00:20Z");
    assertNull(uri.getQuery());
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriSearchSingleNullUri() {
    DataConfigSourceUris.uriSearchSingle(null, NAME, VC, TYPE);
  }

  /**
   * Tests that the name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriSearchSingleNullName() {
    DataConfigSourceUris.uriSearchSingle(_baseUri, null, VC, TYPE);
  }

  /**
   * Tests that the type cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriSearchSingleNullType() {
    DataConfigSourceUris.uriSearchSingle(_baseUri, NAME, VC, null);
  }

  /**
   * Tests the URI that is built when the version correction is null (i.e.
   * LATEST is required).
   */
  public void testSearchSingleNameTypeLatest() {
    final URI uri = DataConfigSourceUris.uriSearchSingle(_baseUri, NAME, null, TYPE);
    assertEquals(uri.getPath(), "path/to/configSearches/single");
    assertEquals(uri.getQuery(), "name=config+name&type=java.lang.Object&versionCorrection=VLATEST.CLATEST");
  }

  /**
   * Tests the URI that is built.
   */
  public void testSearchSingleUriNameType() {
    final URI uri = DataConfigSourceUris.uriSearchSingle(_baseUri, NAME, VC, TYPE);
    assertEquals(uri.getPath(), "path/to/configSearches/single");
    assertEquals(uri.getQuery(), "name=config+name&type=java.lang.Object&versionCorrection=V1970-01-01T00:00:10Z.C1970-01-01T00:00:20Z");
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriSearchNullUri() {
    DataConfigSourceUris.uriSearch(null, TYPE, VC);
  }

  /**
   * Tests that the type cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriSearchNullType() {
    DataConfigSourceUris.uriSearch(_baseUri, null, null);
  }

  /**
   * Tests the URI that is built when the version correction is null (i.e.
   * LATEST is required).
   */
  public void testSearchNameTypeLatest() {
    final URI uri = DataConfigSourceUris.uriSearch(_baseUri, TYPE, null);
    assertEquals(uri.getPath(), "path/to/configSearches");
    assertEquals(uri.getQuery(), "type=java.lang.Object&versionCorrection=VLATEST.CLATEST");
  }

  /**
   * Tests the URI that is built.
   */
  public void testSearchUriNameType() {
    final URI uri = DataConfigSourceUris.uriSearch(_baseUri, TYPE, VC);
    assertEquals(uri.getPath(), "path/to/configSearches");
    assertEquals(uri.getQuery(), "type=java.lang.Object&versionCorrection=V1970-01-01T00:00:10Z.C1970-01-01T00:00:20Z");
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriPutNullUri() {
    DataConfigSourceUris.uriPut(null);
  }

  /**
   * Tests the URI that is built.
   */
  public void testUriPut() {
    final URI uri = DataConfigSourceUris.uriPut(_baseUri);
    assertEquals(uri.getPath(), "path/to/put");
  }
}
