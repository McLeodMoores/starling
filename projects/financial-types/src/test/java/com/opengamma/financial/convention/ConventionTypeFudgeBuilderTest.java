/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.convention;

import org.testng.annotations.Test;

import com.opengamma.core.convention.ConventionType;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ConventionType}.
 */
@Test(groups = TestGroup.UNIT)
public class ConventionTypeFudgeBuilderTest extends AbstractFudgeBuilderTestCase {

  /**
   * Tests a cycle.
   */
  public void testCycle() {
    final ConventionType conventionType = ConventionType.of("name");
    assertEncodeDecodeCycle(ConventionType.class, conventionType);
  }
}
