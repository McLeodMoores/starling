/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.position.impl;

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
 * Tests for {@link DataPositionSourceUris}.
 */
@Test(groups = TestGroup.UNIT)
public class DataPositionSourceUrisTest {
  private static final VersionCorrection VC = VersionCorrection.of(Instant.ofEpochSecond(10000), Instant.ofEpochSecond(20000));
  private static final UniqueId UID = UniqueId.of("ptfl", "1");
  private static final ObjectId OID = UID.getObjectId();
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
  public void testUriGetPortfolioNullBase1() {
    DataPositionSourceUris.uriGetPortfolio(null, UID);
  }

  /**
   * Tests that the unique id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetPortfolioNullUid() {
    DataPositionSourceUris.uriGetPortfolio(_baseUri, null);
  }

  /**
   * Tests the URI.
   */
  public void testUriGetPortfolioUidNoVersion() {
    final URI uri = DataPositionSourceUris.uriGetPortfolio(_baseUri, UID);
    assertEquals(uri.getPath(), "path/to/portfolios/" + UID.toString());
    assertNull(uri.getQuery());
  }

  /**
   * Tests the URI.
   */
  public void testUriGetPortfolioUidVersion() {
    final UniqueId uid = UID.withVersion(VersionCorrection.LATEST.toString());
    final URI uri = DataPositionSourceUris.uriGetPortfolio(_baseUri, uid);
    assertEquals(uri.getPath(), "path/to/portfolios/" + UID.toString());
    assertEquals(uri.getQuery(), "version=" + VersionCorrection.LATEST.toString());
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetPortfolioNullBase2() {
    DataPositionSourceUris.uriGetPortfolio(null, OID, VC);
  }

  /**
   * Tests that the object id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetPortfolioNullOid() {
    DataPositionSourceUris.uriGetPortfolio(_baseUri, null, VC);
  }

  /**
   * Tests the URI.
   */
  public void testUriGetPortfolioOidNoVersion() {
    final URI uri = DataPositionSourceUris.uriGetPortfolio(_baseUri, OID, null);
    assertEquals(uri.getPath(), "path/to/portfolios/" + OID.toString());
    assertNull(uri.getQuery());
  }

  /**
   * Tests the URI.
   */
  public void testUriGetPortfolioOidVersion() {
    final URI uri = DataPositionSourceUris.uriGetPortfolio(_baseUri, OID, VC);
    assertEquals(uri.getPath(), "path/to/portfolios/" + UID.toString());
    assertEquals(uri.getQuery(), "versionAsOf=1970-01-01T02:46:40Z&correctedTo=1970-01-01T05:33:20Z");
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetNodeNullBase() {
    DataPositionSourceUris.uriGetNode(null, UID);
  }

  /**
   * Tests that the unique id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetNodeNullUid() {
    DataPositionSourceUris.uriGetNode(_baseUri, null);
  }

  /**
   * Tests the URI.
   */
  public void testUriGetNodeNoVersion() {
    final URI uri = DataPositionSourceUris.uriGetNode(_baseUri, UID);
    assertEquals(uri.getPath(), "path/to/nodes/" + UID.toString());
    assertNull(uri.getQuery());
  }

  /**
   * Tests the URI.
   */
  public void testUriGetNodeVersion() {
    final UniqueId uid = UID.withVersion(VersionCorrection.LATEST.toString());
    final URI uri = DataPositionSourceUris.uriGetNode(_baseUri, uid);
    assertEquals(uri.getPath(), "path/to/nodes/" + UID.toString());
    assertEquals(uri.getQuery(), "version=VLATEST.CLATEST");
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetPositionNullBase1() {
    DataPositionSourceUris.uriGetPosition(null, UID);
  }

  /**
   * Tests that the unique id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetPositionNullUid() {
    DataPositionSourceUris.uriGetPosition(_baseUri, null);
  }

  /**
   * Tests the URI.
   */
  public void testUriGetPositionNoVersion() {
    final URI uri = DataPositionSourceUris.uriGetPosition(_baseUri, UID);
    assertEquals(uri.getPath(), "path/to/positions/" + UID.toString());
    assertNull(uri.getQuery());
  }

  /**
   * Tests the URI.
   */
  public void testUriGetPositionVersion() {
    final UniqueId uid = UID.withVersion(VersionCorrection.LATEST.toString());
    final URI uri = DataPositionSourceUris.uriGetPosition(_baseUri, uid);
    assertEquals(uri.getPath(), "path/to/positions/" + UID.toString());
    assertEquals(uri.getQuery(), "version=VLATEST.CLATEST");
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetPositionNullBase2() {
    DataPositionSourceUris.uriGetPosition(null, OID, VC);
  }

  /**
   * Tests that the object id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetPositionNullOid() {
    DataPositionSourceUris.uriGetPosition(_baseUri, null, VC);
  }

  /**
   * Tests the URI.
   */
  public void testUriGetPositionOidNoVersion() {
    final URI uri = DataPositionSourceUris.uriGetPosition(_baseUri, OID, null);
    assertEquals(uri.getPath(), "path/to/positions/" + OID.toString());
    assertNull(uri.getQuery());
  }

  /**
   * Tests the URI.
   */
  public void testUriGetPositionOidVersion() {
    final URI uri = DataPositionSourceUris.uriGetPosition(_baseUri, OID, VC);
    assertEquals(uri.getPath(), "path/to/positions/" + UID.toString());
    assertEquals(uri.getQuery(), "versionAsOf=1970-01-01T02:46:40Z&correctedTo=1970-01-01T05:33:20Z");
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetTradeNullBase() {
    DataPositionSourceUris.uriGetPosition(null, UID);
  }

  /**
   * Tests that the unique id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUriGetTradesNullUid() {
    DataPositionSourceUris.uriGetTrade(_baseUri, null);
  }

  /**
   * Tests the URI.
   */
  public void testUriGetTradesNoVersion() {
    final URI uri = DataPositionSourceUris.uriGetTrade(_baseUri, UID);
    assertEquals(uri.getPath(), "path/to/trades/" + UID.toString());
    assertNull(uri.getQuery());
  }

  /**
   * Tests the URI.
   */
  public void testUriGetTradeVersion() {
    final UniqueId uid = UID.withVersion(VersionCorrection.LATEST.toString());
    final URI uri = DataPositionSourceUris.uriGetTrade(_baseUri, uid);
    assertEquals(uri.getPath(), "path/to/trades/" + UID.toString());
    assertEquals(uri.getQuery(), "version=VLATEST.CLATEST");
  }

}
