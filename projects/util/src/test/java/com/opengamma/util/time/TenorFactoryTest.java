/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.util.time;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor.BusinessDayTenor;

/**
 * Tests for {@link TenorFactory}.
 */
@Test(groups = TestGroup.UNIT)
public class TenorFactoryTest {

  /**
   * Tests that the name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName() {
    TenorFactory.INSTANCE.of(null);
  }

  /**
   * Tests a name that is not a tenor.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUnparsableName() {
    TenorFactory.INSTANCE.of("TENOR");
  }

  /**
   * Tests business day tenors.
   */
  @Test
  public void testBusinessDayTenors() {
    assertEquals(TenorFactory.INSTANCE.of("S/N"), Tenor.of(BusinessDayTenor.SPOT_NEXT));
    assertEquals(TenorFactory.INSTANCE.of("SN"), Tenor.of(BusinessDayTenor.SPOT_NEXT));
    assertEquals(TenorFactory.INSTANCE.of("T/N"), Tenor.of(BusinessDayTenor.TOM_NEXT));
    assertEquals(TenorFactory.INSTANCE.of("TN"), Tenor.of(BusinessDayTenor.TOM_NEXT));
    assertEquals(TenorFactory.INSTANCE.of("O/N"), Tenor.of(BusinessDayTenor.OVERNIGHT));
    assertEquals(TenorFactory.INSTANCE.of("ON"), Tenor.of(BusinessDayTenor.OVERNIGHT));
  }

  /**
   * Tests tenors with day periods.
   */
  @Test
  public void testDayTenors() {
    for (int i = 1; i <= 31; i++) {
      assertEquals(TenorFactory.INSTANCE.of("P" + i + "D"), Tenor.of(Period.ofDays(i)));
    }
  }

  /**
   * Tests tenors with week periods.
   */
  @Test
  public void testWeekTenors() {
    for (int i = 1; i <= 4; i++) {
      assertEquals(TenorFactory.INSTANCE.of("P" + i + "W"), Tenor.of(Period.ofDays(i * 7)));
    }
  }

  /**
   * Tests tenors with month periods.
   */
  @Test
  public void testMonthTenors() {
    for (int i = 1; i <= 12; i++) {
      assertEquals(TenorFactory.INSTANCE.of("P" + i + "M"), Tenor.of(Period.ofMonths(i)));
    }
  }

  /**
   * Tests tenors with year periods.
   */
  @Test
  public void testYearTenors() {
    for (int i = 1; i <= 50; i++) {
      assertEquals(TenorFactory.INSTANCE.of("P" + i + "Y"), Tenor.of(Period.ofYears(i)));
    }
  }
}