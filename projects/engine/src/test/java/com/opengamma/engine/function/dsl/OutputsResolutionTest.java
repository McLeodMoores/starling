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
 * Tests for {@link OutputsResolution}.
 */
@Test(groups = TestGroup.UNIT)
public class OutputsResolutionTest extends AbstractBeanTestCase {

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    final ResolvedOutput input1 = new ResolvedOutput();
    input1.setName("name1");
    input1.setProperties(Collections.singletonMap("a", "b"));
    final ResolvedOutput input2 = new ResolvedOutput();
    input2.setName("name2");
    input2.setProperties(Collections.singletonMap("c", "d"));
    return new JodaBeanProperties<>(OutputsResolution.class,
        Arrays.asList("outputs"),
        Arrays.asList(Collections.singleton(input1)),
        Arrays.asList(Collections.singleton(input2)));
  }

  @Override
  protected <TYPE extends Bean> void testCycle(final JodaBeanProperties<TYPE> properties) {
  }

}
