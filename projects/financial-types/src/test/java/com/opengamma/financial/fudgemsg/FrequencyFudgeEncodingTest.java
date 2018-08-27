/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.fudgemsg.UnmodifiableFudgeField;
import org.fudgemsg.wire.types.FudgeWireType;
import org.testng.annotations.Test;

import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class FrequencyFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  private static final Frequency REF = SimpleFrequency.BIMONTHLY;

  @Test
  public void testCycle() {
    assertEquals(REF, cycleObject(Frequency.class, REF));
  }

  @Test
  public void testFromString() {
    assertEquals(REF, getFudgeContext().getFieldValue(Frequency.class,
        UnmodifiableFudgeField.of(FudgeWireType.STRING, REF.getName())));
  }

}
