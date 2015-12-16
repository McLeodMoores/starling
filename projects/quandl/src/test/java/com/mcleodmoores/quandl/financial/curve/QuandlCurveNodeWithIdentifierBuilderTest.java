/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.financial.curve;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.mcleodmoores.quandl.QuandlConstants;
import com.mcleodmoores.quandl.future.QuandlFedFundsFutureCurveInstrumentProvider;
import com.mcleodmoores.quandl.future.QuandlFutureCurveInstrumentProvider;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.curve.CurveNodeWithIdentifierBuilder;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link QuandlCurveNodeWithIdentifierBuilder}.
 */
@Test(groups = TestGroup.UNIT)
public class QuandlCurveNodeWithIdentifierBuilderTest {
  /** The curve date */
  private static final LocalDate CURVE_DATE = LocalDate.of(2015, 4, 15);
  /** The node id mapper containing a P0D instrument provider that contains underlying information and a P3M
   * provider that does not */
  private static final CurveNodeIdMapper MAPPER;
  /** A Fed funds future rate node */
  private static final RateFutureNode NODE1 = new RateFutureNode(1, Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, Tenor.ON,
      ExternalId.of("Test", "Test1"), "Name");
  /** A Fed funds future rate node */
  private static final RateFutureNode NODE2 = new RateFutureNode(1, Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, Tenor.ON, ExternalId.of("Test", "Test2"), "Name");
  /** The builder */
  private static final CurveNodeWithIdentifierBuilder BUILDER;

  static {
    final CurveInstrumentProvider provider1 = new QuandlFedFundsFutureCurveInstrumentProvider("FF", MarketDataRequirementNames.MARKET_VALUE,
        DataFieldType.OUTRIGHT, QuandlConstants.ofCode("ON"), MarketDataRequirementNames.LAST);
    final CurveInstrumentProvider provider2 = new QuandlFutureCurveInstrumentProvider("FF", MarketDataRequirementNames.MARKET_VALUE, DataFieldType.OUTRIGHT);
    final Map<Tenor, CurveInstrumentProvider> rateFutureNodeIds = new HashMap<>();
    rateFutureNodeIds.put(Tenor.of(Period.ZERO), provider1);
    rateFutureNodeIds.put(Tenor.THREE_MONTHS, provider2);
    MAPPER = CurveNodeIdMapper.builder()
        .rateFutureNodeIds(rateFutureNodeIds)
        .build();
    BUILDER = new QuandlCurveNodeWithIdentifierBuilder(CURVE_DATE, MAPPER);
  }

  /**
   * Tests that a {@link QuandlCurveNodeWithIdentifierAndUnderlying} is created if the curve instrument provider is a
   * {@link QuandlFedFundsFutureCurveInstrumentProvider}.
   */
  @Test
  public void testNodeWithUnderlyingData() {
    final CurveNodeWithIdentifier nodeWithId = NODE1.accept(BUILDER);
    assertTrue(nodeWithId instanceof QuandlCurveNodeWithIdentifierAndUnderlying);
    final QuandlCurveNodeWithIdentifierAndUnderlying nodeWithIdAndUnderlying = (QuandlCurveNodeWithIdentifierAndUnderlying) nodeWithId;
    final QuandlCurveNodeWithIdentifierAndUnderlying expected = new QuandlCurveNodeWithIdentifierAndUnderlying(NODE1, QuandlConstants.ofCode("FFM2015"),
        MarketDataRequirementNames.MARKET_VALUE, DataFieldType.OUTRIGHT, QuandlConstants.ofCode("ON"), MarketDataRequirementNames.LAST);
    assertEquals(nodeWithIdAndUnderlying, expected);
  }

  /**
   * Tests that a {@link CurveNodeWithIdentifier} is created if the curve instrument provider is not a
   * {@link QuandlFedFundsFutureCurveInstrumentProvider}.
   */
  @Test
  public void testNodeWithoutUnderlyingData() {
    final CurveNodeWithIdentifier nodeWithId = NODE2.accept(BUILDER);
    assertFalse(nodeWithId instanceof QuandlCurveNodeWithIdentifierAndUnderlying);
    final CurveNodeWithIdentifier expected = new CurveNodeWithIdentifier(NODE2, QuandlConstants.ofCode("FFU2015"),
        MarketDataRequirementNames.MARKET_VALUE, DataFieldType.OUTRIGHT);
    assertEquals(nodeWithId, expected);
  }
}
