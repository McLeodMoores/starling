/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.provider.livedata;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalScheme;
import com.opengamma.provider.AbstractBeanTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link LiveDataMetaDataProviderResult}.
 */
@Test(groups = TestGroup.UNIT)
public class LiveDataMetaDataProviderResultTest extends AbstractBeanTestCase {

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(LiveDataMetaDataProviderResult.class, Arrays.asList("metaData"),
        Arrays.asList(new LiveDataMetaData(Arrays.asList(ExternalScheme.of("scheme1")), LiveDataServerTypes.COGDA, "desc1")),
        Arrays.asList(new LiveDataMetaData(Arrays.asList(ExternalScheme.of("scheme2")), LiveDataServerTypes.STANDARD, "desc2")));
  }

}
