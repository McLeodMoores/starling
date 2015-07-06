/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.serialization;

import com.mcleodmoores.integration.serialization.CurveBean;



/**
 * Base class for unit tests of classes that extend {@link CurveBean}.
 */
public abstract class CurveBeanTest extends AbstractCurveBeanTest {

  /**
   * Tests curve object construction.
   */
  public abstract void testBuildCurve();

}
