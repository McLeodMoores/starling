/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.user.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.net.URI;
import java.net.URISyntaxException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link DataUserSourceUris}.
 */
@Test(groups = TestGroup.UNIT)
public class DataUserSourceUrisTest {
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
   * Tests that the name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUserNyNameNullName() {
    DataUserSourceUris.uriUserByName(_baseUri, null);
  }

  /**
   * Tests the URI.
   */
  public void testUserByName() {
    final String userName = "me";
    final URI uri = DataUserSourceUris.uriUserByName(_baseUri, userName);
    assertEquals(uri.getPath(), "path/to/users/name/me");
    assertNull(uri.getQuery());
  }
}
