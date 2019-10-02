/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.testng.annotations.Test;

import com.opengamma.engine.test.TestViewDefinitionProvider;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests ViewDefinitionBuilder.
 */
@Test(groups = TestGroup.UNIT)
public class ViewDefinitionBuilderTest extends AbstractFudgeBuilderTestCase {

  @Test
  public void testViewDefinitionNoUniqueId() {
    final ViewDefinition testViewDefinition = TestViewDefinitionProvider.getTestViewDefinition();
    testViewDefinition.setUniqueId(null);
    assertEncodeDecodeCycle(ViewDefinition.class, TestViewDefinitionProvider.getTestViewDefinition());
  }

  @Test
  public void testViewDefinitionUniqueId() {
    final ViewDefinition testViewDefinition = TestViewDefinitionProvider.getTestViewDefinition();
    assertEncodeDecodeCycle(ViewDefinition.class, testViewDefinition);
  }

}
