/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.provider.livedata.impl;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalScheme;
import com.opengamma.provider.livedata.LiveDataMetaData;
import com.opengamma.provider.livedata.LiveDataMetaDataProviderRequest;
import com.opengamma.provider.livedata.LiveDataMetaDataProviderResult;
import com.opengamma.provider.livedata.LiveDataServerTypes;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link SimpleLiveDataMetaDataProvider}.
 */
@Test(groups = TestGroup.UNIT)
public class SimpleLiveDataMetaDataProviderTest {
  private static final LiveDataMetaData META_DATA = new LiveDataMetaData(Arrays.asList(ExternalScheme.of("scheme")), LiveDataServerTypes.COGDA, "desc");

  /**
   * Tests that the request cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRequest() {
    new SimpleLiveDataMetaDataProvider(META_DATA).metaData(null);
  }

  /**
   * Tests the object.
   */
  public void test() {
    final SimpleLiveDataMetaDataProvider provider = new SimpleLiveDataMetaDataProvider(META_DATA);
    assertEquals(provider.metaData(), META_DATA);
    final LiveDataMetaDataProviderRequest request = new LiveDataMetaDataProviderRequest();
    assertEquals(provider.metaData(request), new LiveDataMetaDataProviderResult(META_DATA));
  }
}
