/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import static org.testng.AssertJUnit.assertEquals;

import org.fudgemsg.UnmodifiableFudgeField;
import org.fudgemsg.wire.types.FudgeWireType;
import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor.BusinessDayTenor;

/**
 * Test Tenor Fudge support.
 */
@Test(groups = TestGroup.UNIT)
public class TenorFudgeEncodingTest extends AbstractFudgeBuilderTestCase {
  private static final Tenor PERIOD_TENOR = Tenor.EIGHT_MONTHS;
  private static final Tenor BUSINESS_DAY_TENOR = Tenor.SN;

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    assertEncodeDecodeCycle(Tenor.class, PERIOD_TENOR);
    assertEncodeDecodeCycle(Tenor.class, BUSINESS_DAY_TENOR);
  }

  /**
   * Tests a cycle using the secondary type.
   */
  @Test
  public void testFromString() {
    assertEquals(PERIOD_TENOR, getFudgeContext().getFieldValue(Tenor.class,
        UnmodifiableFudgeField.of(FudgeWireType.STRING, PERIOD_TENOR.getPeriod().toString())));
    assertEquals(BUSINESS_DAY_TENOR, getFudgeContext().getFieldValue(Tenor.class,
        UnmodifiableFudgeField.of(FudgeWireType.STRING, BUSINESS_DAY_TENOR.getBusinessDayTenor().toString())));
    assertEquals(PERIOD_TENOR, getFudgeContext().getFieldValue(Tenor.class,
        UnmodifiableFudgeField.of(FudgeWireType.STRING, PERIOD_TENOR.toFormattedString())));
    assertEquals(BUSINESS_DAY_TENOR, getFudgeContext().getFieldValue(Tenor.class,
        UnmodifiableFudgeField.of(FudgeWireType.STRING, BUSINESS_DAY_TENOR.toFormattedString())));
  }

  /**
   * Tests the builder when the Tenor entry is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderNullEntry() {
    final TenorFudgeBuilder builder = new TenorFudgeBuilder();
    builder.buildObject(getFudgeDeserializer(), getFudgeContext().newMessage());
  }

  /**
   * Tests the object builder.
   */
  @Test
  public void testBuilder() {
    final TenorFudgeBuilder builder = new TenorFudgeBuilder();
    assertEquals(builder.buildObject(getFudgeDeserializer(), builder.buildMessage(getFudgeSerializer(), Tenor.ONE_MONTH)), Tenor.ONE_MONTH);
    assertEquals(builder.buildObject(getFudgeDeserializer(), builder.buildMessage(getFudgeSerializer(), Tenor.of(BusinessDayTenor.TOM_NEXT))),
        Tenor.of(BusinessDayTenor.TOM_NEXT));
  }
}
