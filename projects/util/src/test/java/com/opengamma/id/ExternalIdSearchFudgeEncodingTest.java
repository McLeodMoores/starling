/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding of {@link ExternalIdSearch}.
 */
@Test(groups = TestGroup.UNIT)
public class ExternalIdSearchFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  /**
   * Tests an empty search.
   */
  @Test
  public void testEmpty() {
    final ExternalIdSearch object = ExternalIdSearch.of();
    assertEncodeDecodeCycle(ExternalIdSearch.class, object);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testId() {
    final ExternalIdSearch object = ExternalIdSearch.of(ExternalId.of("A", "B"));
    assertEncodeDecodeCycle(ExternalIdSearch.class, object);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testFull() {
    final ExternalIdSearch object = ExternalIdSearch.of(ExternalIdSearchType.EXACT, Arrays.asList(ExternalId.of("A", "B")));
    assertEncodeDecodeCycle(ExternalIdSearch.class, object);
  }

}
