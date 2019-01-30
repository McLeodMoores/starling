/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.Assert.assertEquals;

import org.fudgemsg.UnmodifiableFudgeField;
import org.fudgemsg.wire.types.FudgeWireType;
import org.testng.annotations.Test;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DayCountFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  private static final DayCount REF = DayCounts.ACT_360;

  @Test
  public void testCycle() {
    assertEquals(REF, cycleObject(DayCount.class, REF));
  }

  @Test
  public void testFromString() {
    assertEquals(REF, getFudgeContext().getFieldValue(DayCount.class,
        UnmodifiableFudgeField.of(FudgeWireType.STRING, REF.getName())));
  }

}
