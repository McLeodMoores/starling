/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding for {@link ExternalScheme}.
 */
@Test(groups = TestGroup.UNIT)
public class ExternalSchemeFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  /**
   * Tests the encoding/decoding cycle.
   */
  public void test() {
    final ExternalScheme object = ExternalScheme.of("A");
    assertEncodeDecodeCycle(ExternalScheme.class, object);
  }

}
