/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.transport.jaxrs;

import static org.testng.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link RemoteEndPointDescriptionProvider}.
 */
@Test(groups = TestGroup.UNIT)
public class RemoteEndPointDescriptionProviderTest {
  private static final URI IDENTIFIER;
  static {
    try {
      IDENTIFIER = new URI("http://www.example.com");
    } catch (final URISyntaxException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Creates a end point description.
   */
  @Test
  public void test() {
    final RemoteEndPointDescriptionProvider id = new RemoteEndPointDescriptionProvider();
    id.setUri(IDENTIFIER);
    assertEquals(id.getUri(), IDENTIFIER);
    assertEquals(new RemoteEndPointDescriptionProvider(IDENTIFIER).getUri(), IDENTIFIER);
  }

}
