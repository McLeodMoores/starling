/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.engine.function.dsl;

import java.util.Arrays;
import java.util.Collections;

import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ResolvedInput}.
 */
@Test(groups = TestGroup.UNIT)
public class ResolvedInputTest extends AbstractBeanTestCase {

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(ResolvedInput.class,
        Arrays.asList("properties", "name"),
        Arrays.asList(Collections.singletonMap("a", "z"), "name1"),
        Arrays.asList(Collections.singletonMap("b", "y"), "name2"));
  }

}
