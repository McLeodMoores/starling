/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve;

import static com.opengamma.financial.analytics.curve.CurveNodeCurrencyVisitorTest.SCHEME;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalTime;

import com.google.common.collect.Sets;
import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.InMemoryConventionSource;
import com.opengamma.engine.InMemorySecuritySource;
import com.opengamma.financial.analytics.ircurve.strips.CalendarSwapNode;
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
 * Tests the retrieval of a currency from calendar swap nodes.
 */
public class CalendarSwapNodeCurrencyVisitorTest {
  /** US region. */
  private static final ExternalId US = ExternalSchemes.financialRegionId("US");
  /** An empty security source. */
  private static final SecuritySource EMPTY_SECURITY_SOURCE = new InMemorySecuritySource();
  /** An empty convention source. */
  private static final ConventionSource EMPTY_CONVENTION_SOURCE = new InMemoryConventionSource();
  /** The curve node id mapper name */
  private static final String CNIM_NAME = "CNIM";
  /** The id of the swap convention */
  private static final ExternalId SWAP_CONVENTION_ID = ExternalId.of(SCHEME, "USD Calendar Swap");
  /** The id of the pay leg convention */
  private static final ExternalId PAY_LEG_CONVENTION_ID = ExternalId.of(SCHEME, "USD Fixed Swap Leg");
  /** The id of the receive leg convention */
  private static final ExternalId RECEIVE_LEG_CONVENTION_ID = ExternalId.of(SCHEME, "USD Float Swap Leg");
  /** The id of the underlying LIBOR convention */
  private static final ExternalId LIBOR_CONVENTION_ID = ExternalId.of(SCHEME, "USD LIBOR");
  /** The swap convention */
  private static final SwapConvention SWAP_CONVENTION = new SwapConvention("USD Calendar Swap", SWAP_CONVENTION_ID.toBundle(),
      PAY_LEG_CONVENTION_ID, RECEIVE_LEG_CONVENTION_ID);
  /** The pay leg convention */
  private static final SwapFixedLegConvention PAY_LEG_CONVENTION = new SwapFixedLegConvention("EUR Fixed Swap Leg",
      PAY_LEG_CONVENTION_ID.toBundle(), Tenor.SIX_MONTHS, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, Currency.EUR,
      US, 2, false, StubType.NONE, false, 0);
  /** The receive leg convention */
  private static final VanillaIborLegConvention RECEIVE_LEG_CONVENTION = new VanillaIborLegConvention("USD Float Swap Leg",
      RECEIVE_LEG_CONVENTION_ID.toBundle(), LIBOR_CONVENTION_ID, false, InterpolationMethod.NONE.name(), Tenor.THREE_MONTHS, 2, false,
      StubType.NONE, false, 0);
  /** The LIBOR convention */
  private static final IborIndexConvention LIBOR_CONVENTION = new IborIndexConvention("USD LIBOR", LIBOR_CONVENTION_ID.toBundle(),
      DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, Currency.USD, LocalTime.of(11, 0), "", US, US, "");

  /**
   * Tests the behaviour when the swap convention is not found.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testNoConvention() {
    final CalendarSwapNode node = new CalendarSwapNode("DATES", Tenor.ONE_YEAR, 1, 10, SWAP_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(EMPTY_CONVENTION_SOURCE, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests the behaviour when the pay leg convention is not found.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testNoPayConvention() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource(); //new TestConventionSource(conventions);
    conventionSource.addConvention(LIBOR_CONVENTION.clone());
    conventionSource.addConvention(RECEIVE_LEG_CONVENTION.clone());
    conventionSource.addConvention(SWAP_CONVENTION.clone());
    final CalendarSwapNode node = new CalendarSwapNode("DATES", Tenor.ONE_YEAR, 1, 10, SWAP_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests the behaviour when the receive leg convention is not found.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testNoReceiveConvention() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(LIBOR_CONVENTION.clone());
    conventionSource.addConvention(PAY_LEG_CONVENTION.clone());
    conventionSource.addConvention(SWAP_CONVENTION.clone());
    final CalendarSwapNode node = new CalendarSwapNode("DATES", Tenor.ONE_YEAR, 1, 10, SWAP_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests the behaviour when the underlying index convention is not found and there is no underlying ibor index security
   * in the security source.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoIndexConvention() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(RECEIVE_LEG_CONVENTION.clone());
    conventionSource.addConvention(PAY_LEG_CONVENTION.clone());
    conventionSource.addConvention(SWAP_CONVENTION.clone());
    final CalendarSwapNode node = new CalendarSwapNode("DATES", Tenor.ONE_YEAR, 1, 10, SWAP_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests that the correct currency is returned.
   */
  @Test
  public void test() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(LIBOR_CONVENTION.clone());
    conventionSource.addConvention(RECEIVE_LEG_CONVENTION.clone());
    conventionSource.addConvention(PAY_LEG_CONVENTION.clone());
    conventionSource.addConvention(SWAP_CONVENTION.clone());
    final CalendarSwapNode node = new CalendarSwapNode("DATES", Tenor.ONE_YEAR, 1, 10, SWAP_CONVENTION_ID, CNIM_NAME);
    assertEquals(node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE)), Sets.newHashSet(Currency.EUR, Currency.USD));
  }
}
