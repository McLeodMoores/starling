/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class GeneratorSwapFixedCompoundedONCompoundedTest {

  private static final WorkingDayCalendar NYC = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;
  private static final IndexON INDEX_CDI = IndexONMaster.getInstance().getIndex("CDI");
  private static final String BRL_NAME = "BRLCDI";
  private static final DayCount BRL_DAYCOUNT_FIXED = INDEX_CDI.getDayCount();
  private static final BusinessDayConvention BRL_BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean BRL_IS_EOM = true;
  private static final int BRL_SPOT_LAG = 2;

  private static final GeneratorSwapFixedCompoundedONCompounded USD_GENERATOR_OIS = new GeneratorSwapFixedCompoundedONCompounded(BRL_NAME,
      INDEX_CDI, BRL_DAYCOUNT_FIXED, BRL_BUSINESS_DAY, BRL_IS_EOM, BRL_SPOT_LAG, NYC);

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency() {
    new GeneratorSwapFixedCompoundedONCompounded(null, INDEX_CDI, BRL_DAYCOUNT_FIXED, BRL_BUSINESS_DAY, BRL_IS_EOM, BRL_SPOT_LAG, NYC);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullDayCount() {
    new GeneratorSwapFixedCompoundedONCompounded(BRL_NAME, INDEX_CDI, null, BRL_BUSINESS_DAY, BRL_IS_EOM, BRL_SPOT_LAG, NYC);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullBusinessDay() {
    new GeneratorSwapFixedCompoundedONCompounded(BRL_NAME, INDEX_CDI, BRL_DAYCOUNT_FIXED, null, BRL_IS_EOM, BRL_SPOT_LAG, NYC);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIndex() {
    new GeneratorSwapFixedCompoundedONCompounded(BRL_NAME, null, BRL_DAYCOUNT_FIXED, BRL_BUSINESS_DAY, BRL_IS_EOM, BRL_SPOT_LAG, NYC);
  }

  /**
   * Tests the getters.
   */
  @Test
  public void getter() {
    assertEquals("Generator ON Compounded: getter", BRL_NAME, USD_GENERATOR_OIS.getName());
    assertEquals("Generator ON Compounded: getter", BRL_DAYCOUNT_FIXED, USD_GENERATOR_OIS.getFixedLegDayCount());
    assertEquals("Generator ON Compounded: getter", BRL_BUSINESS_DAY, USD_GENERATOR_OIS.getBusinessDayConvention());
    assertEquals("Generator ON Compounded: getter", BRL_IS_EOM, USD_GENERATOR_OIS.isEndOfMonth());
    assertEquals("Generator ON Compounded: getter", INDEX_CDI, USD_GENERATOR_OIS.getIndex());
    assertEquals("Generator ON Compounded: getter", BRL_SPOT_LAG, USD_GENERATOR_OIS.getSpotLag());
  }

  /**
   * Tests the standard USD OIS builders.
   */
  @Test
  public void usdStandard() {
    final GeneratorSwapFixedCompoundedONCompounded brlStandard = GeneratorSwapFixedCompoundedONCompoundedMaster.getInstance()
        .getGenerator("BRLCDI", NYC);
    assertEquals("Generator ON Compounded: standard", BRL_NAME, brlStandard.getName());
    assertEquals("Generator ON Compounded: standard", BRL_BUSINESS_DAY, brlStandard.getBusinessDayConvention());
    assertEquals("Generator ON Compounded: standard", BRL_IS_EOM, brlStandard.isEndOfMonth());
    assertEquals("Generator ON Compounded: standard", INDEX_CDI, brlStandard.getIndex());

  }

}
