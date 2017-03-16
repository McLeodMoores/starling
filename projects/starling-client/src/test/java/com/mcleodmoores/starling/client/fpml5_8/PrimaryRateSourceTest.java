/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.fpml5_8;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.mcleodmoores.starling.client.marketdata.DataSource;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.PrimaryRateSource;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link PrimaryRateSource}.
 */
@Test(groups = TestGroup.UNIT)
public class PrimaryRateSourceTest {
  /** The data source */
  private static final DataSource SOURCE = DataSource.of("SRC");
  /** The rate page */
  private static final String PAGE = "PAGE";

  /**
   * Tests that the data source must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDataSourceNotNull() {
    PrimaryRateSource.builder().rateSourcePage(PAGE).build();
  }

  /**
   * Tests that the page must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPageNotNull() {
    PrimaryRateSource.builder().dataSource(SOURCE).build();
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final PrimaryRateSource prs = PrimaryRateSource.builder().dataSource(SOURCE).rateSourcePage(PAGE).build();
    PrimaryRateSource other = PrimaryRateSource.builder().dataSource(SOURCE).rateSourcePage(PAGE).build();
    assertEquals(prs, prs);
    assertEquals(prs, other);
    assertEquals(prs.hashCode(), other.hashCode());
    assertNotEquals(new Object(), prs);
    other = PrimaryRateSource.builder().dataSource(DataSource.DEFAULT).rateSourcePage(PAGE).build();
    assertNotEquals(prs, other);
    other = PrimaryRateSource.builder().dataSource(SOURCE).rateSourcePage("OTHER").build();
    assertNotEquals(prs, other);
  }
}
