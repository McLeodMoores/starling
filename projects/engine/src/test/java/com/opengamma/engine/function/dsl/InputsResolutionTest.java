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
 * Tests for {@link InputsResolution}.
 */
@Test(groups = TestGroup.UNIT)
public class InputsResolutionTest extends AbstractBeanTestCase {

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    final ResolvedInput input1 = new ResolvedInput();
    input1.setName("name1");
    input1.setProperties(Collections.singletonMap("a", "b"));
    final ResolvedInput input2 = new ResolvedInput();
    input2.setName("name2");
    input2.setProperties(Collections.singletonMap("c", "d"));
    return new JodaBeanProperties<>(InputsResolution.class,
        Arrays.asList("inputs"),
        Arrays.asList(Collections.singleton(input1)),
        Arrays.asList(Collections.singleton(input2)));
  }

  @Override
  protected <TYPE extends Bean> void testCycle(final JodaBeanProperties<TYPE> properties) {
  }

}
