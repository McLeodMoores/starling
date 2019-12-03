/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.provider.livedata;

import java.util.Collections;

import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.opengamma.provider.AbstractBeanTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link LiveDataMetaDataProviderRequest}.
 */
@Test(groups = TestGroup.UNIT)
public class LiveDataMetaDataProviderRequestTest extends AbstractBeanTestCase {

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(LiveDataMetaDataProviderRequest.class, Collections.<String> emptyList(), Collections.emptyList());
  }

}
