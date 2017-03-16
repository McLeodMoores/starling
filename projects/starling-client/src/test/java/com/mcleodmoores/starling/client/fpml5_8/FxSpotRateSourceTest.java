/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.fpml5_8;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;

import com.mcleodmoores.starling.client.marketdata.DataSource;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.FxSpotRateSource;
import com.mcleodmoores.starling.client.portfolio.fpml5_8.PrimaryRateSource;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link FxSpotRateSource}.
 */
@Test(groups = TestGroup.UNIT)
public class FxSpotRateSourceTest {
  /** The primary rate source */
  private static final PrimaryRateSource PRIMARY_SOURCE = PrimaryRateSource.builder().dataSource(DataSource.DEFAULT).rateSourcePage("FX FIX").build();
  /** The fixing time */
  private static final LocalTime FIXING_TIME = LocalTime.of(11, 0);
  /** The business centre zone */
  private static final ZoneId ZONE = ZoneOffset.UTC;

  /**
   * Tests that the rate source must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testRateSourceNotNull() {
    FxSpotRateSource.builder().businessCenterZone(ZONE).fixingTime(FIXING_TIME).build();
  }

  /**
   * Tests that the fixing time must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFixingTimeNotNull() {
    FxSpotRateSource.builder().businessCenterZone(ZONE).primaryRateSource(PRIMARY_SOURCE).build();
  }

  /**
   * Tests that the time zone must be set.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZoneNotNull() {
    FxSpotRateSource.builder().primaryRateSource(PRIMARY_SOURCE).fixingTime(FIXING_TIME).build();
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final FxSpotRateSource source = FxSpotRateSource.builder()
        .businessCenterZone(ZONE).fixingTime(FIXING_TIME).primaryRateSource(PRIMARY_SOURCE).build();
    FxSpotRateSource other = FxSpotRateSource.builder()
        .businessCenterZone(ZONE).fixingTime(FIXING_TIME).primaryRateSource(PRIMARY_SOURCE).build();
    assertEquals(source, source);
    assertEquals(source, other);
    assertEquals(source.hashCode(), other.hashCode());
    assertNotEquals(new Object(), source);
    other = FxSpotRateSource.builder()
        .businessCenterZone(ZoneId.of("America/New_York")).fixingTime(LocalTime.of(11, 0)).primaryRateSource(PRIMARY_SOURCE).build();
    assertNotEquals(source, other);
    other = FxSpotRateSource.builder()
        .businessCenterZone(ZONE).fixingTime(FIXING_TIME.plusHours(1)).primaryRateSource(PRIMARY_SOURCE).build();
    assertNotEquals(source, other);
    other = FxSpotRateSource.builder()
        .businessCenterZone(ZONE).fixingTime(FIXING_TIME)
        .primaryRateSource(PrimaryRateSource.builder().dataSource(DataSource.DEFAULT).rateSourcePage("FX FIXINGS").build()).build();
    assertNotEquals(source, other);
  }
}
