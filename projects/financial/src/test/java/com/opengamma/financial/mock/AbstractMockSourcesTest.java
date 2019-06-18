/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.mock;


import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.util.test.TestGroup;

/**
 * Test harness which instantiates InMemory sources with sample data.
 */
@SuppressWarnings("deprecation")
@Test(groups = TestGroup.UNIT)
public abstract class AbstractMockSourcesTest {

  protected FunctionExecutionContext _executionContext;
  protected RegionSource _regionSource;
  protected HolidaySource _holidaySource;
  protected ConventionBundleSource _conventionBundleSource;

  @BeforeSuite(alwaysRun = true)
  protected void initMocks() {
    _executionContext = MockSources.isdaMocks();
    _regionSource = OpenGammaExecutionContext.getRegionSource(_executionContext);
    _holidaySource = OpenGammaExecutionContext.getHolidaySource(_executionContext);
    _conventionBundleSource = OpenGammaExecutionContext.getConventionBundleSource(_executionContext);
  }

  @Test(enabled = true)
  public void testSourcesInited() {
    Assert.assertNotNull(_regionSource);
    Assert.assertNotNull(_holidaySource);
    Assert.assertNotNull(_conventionBundleSource);
  }


}
