/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve;

import static com.opengamma.financial.analytics.curve.CurveNodeCurrencyVisitorTest.EMPTY_SECURITY_SOURCE;
import static com.opengamma.financial.analytics.curve.CurveNodeCurrencyVisitorTest.SCHEME;
import static com.opengamma.financial.analytics.curve.CurveNodeCurrencyVisitorTest.US;
import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.LocalTime;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.financial.analytics.ircurve.strips.DeliverableSwapFutureNode;
import com.opengamma.financial.convention.DeliverablePriceQuotedSwapFutureConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapConvention;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.security.swap.InterpolationMethod;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Tests the retrieval of a currency from deliverable swap future nodes.
 */
public class DeliverableSwapFututureNodeCurrencyVisitorTest {
  /** The curve node id mapper name */
  private static final String CNIM_NAME = "CNIM";
  /** The id of the deliverable swap future convention */
  private static final ExternalId DSF_CONVENTION_ID = ExternalId.of(SCHEME, "USD DSF");
  /** The id of the swap convention */
  private static final ExternalId SWAP_CONVENTION_ID = ExternalId.of(SCHEME, "USD Calendar Swap");
  /** The id of the pay leg convention */
  private static final ExternalId PAY_LEG_CONVENTION_ID = ExternalId.of(SCHEME, "USD Fixed Swap Leg");
  /** The id of the receive leg convention */
  private static final ExternalId RECEIVE_LEG_CONVENTION_ID = ExternalId.of(SCHEME, "USD Float Swap Leg");
  /** The id of the underlying LIBOR convention */
  private static final ExternalId LIBOR_CONVENTION_ID = ExternalId.of(SCHEME, "USD LIBOR");
  /** The deliverable swap future convention */
  private static final DeliverablePriceQuotedSwapFutureConvention DSF_CONVENTION =
      new DeliverablePriceQuotedSwapFutureConvention("USD DSF", DSF_CONVENTION_ID.toBundle(), ExternalId.of(SCHEME, "IMM"),
          US, SWAP_CONVENTION_ID, 100000);
  /** The swap convention */
  private static final SwapConvention SWAP_CONVENTION = new SwapConvention("USD Calendar Swap", SWAP_CONVENTION_ID.toBundle(),
      PAY_LEG_CONVENTION_ID, RECEIVE_LEG_CONVENTION_ID);
  /** The pay leg convention */
  private static final SwapFixedLegConvention PAY_LEG_CONVENTION = new SwapFixedLegConvention("USD Fixed Swap Leg",
      PAY_LEG_CONVENTION_ID.toBundle(), Tenor.SIX_MONTHS, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, Currency.USD,
      US, 2, false, StubType.NONE, false, 0);
  /** The receive leg convention */
  private static final VanillaIborLegConvention RECEIVE_LEG_CONVENTION = new VanillaIborLegConvention("USD Float Swap Leg",
      RECEIVE_LEG_CONVENTION_ID.toBundle(), LIBOR_CONVENTION_ID, false, InterpolationMethod.NONE.name(), Tenor.THREE_MONTHS, 2, false,
      StubType.NONE, false, 0);
  /** The LIBOR convention */
  private static final IborIndexConvention LIBOR_CONVENTION = new IborIndexConvention("USD LIBOR", LIBOR_CONVENTION_ID.toBundle(),
      DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, Currency.USD, LocalTime.of(11, 0), "", US, US, "");

  /**
   * Tests the behaviour when the deliverable swap future convention is not found.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testNoPayConvention() {
    final Map<ExternalId, Convention> conventions = new HashMap<>();
    conventions.put(LIBOR_CONVENTION_ID, LIBOR_CONVENTION);
    conventions.put(PAY_LEG_CONVENTION_ID, PAY_LEG_CONVENTION);
    conventions.put(RECEIVE_LEG_CONVENTION_ID, RECEIVE_LEG_CONVENTION);
    conventions.put(SWAP_CONVENTION_ID, SWAP_CONVENTION);
    final ConventionSource conventionSource = new TestConventionSource(conventions);
    final DeliverableSwapFutureNode node = new DeliverableSwapFutureNode(1, Tenor.THREE_MONTHS, Tenor.THREE_MONTHS,
        Tenor.TEN_YEARS, DSF_CONVENTION_ID, SWAP_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests that the correct currency is returned.
   */
  @Test
  public void test() {
    final Map<ExternalId, Convention> conventions = new HashMap<>();
    conventions.put(LIBOR_CONVENTION_ID, LIBOR_CONVENTION);
    conventions.put(PAY_LEG_CONVENTION_ID, PAY_LEG_CONVENTION);
    conventions.put(RECEIVE_LEG_CONVENTION_ID, RECEIVE_LEG_CONVENTION);
    conventions.put(SWAP_CONVENTION_ID, SWAP_CONVENTION);
    conventions.put(DSF_CONVENTION_ID, DSF_CONVENTION);
    final ConventionSource conventionSource = new TestConventionSource(conventions);
    final DeliverableSwapFutureNode node = new DeliverableSwapFutureNode(1, Tenor.THREE_MONTHS, Tenor.THREE_MONTHS,
        Tenor.TEN_YEARS, DSF_CONVENTION_ID, SWAP_CONVENTION_ID, CNIM_NAME);
    assertEquals(node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE)), Collections.singleton(Currency.USD));
  }
}
