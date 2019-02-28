/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.function;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;
import com.opengamma.web.AbstractBeanTestCase;

/**
 * Tests for {@link WebFunctionData}.
 */
@Test(groups = TestGroup.UNIT)
public class WebFunctionDataTest extends AbstractBeanTestCase {

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(WebFunctionData.class, Arrays.asList("uriFunctionName"), Arrays.asList("fnc"), Arrays.asList("otherUri"));
  }

}
