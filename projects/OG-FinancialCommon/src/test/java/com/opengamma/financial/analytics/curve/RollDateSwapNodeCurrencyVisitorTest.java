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
import com.opengamma.financial.analytics.ircurve.strips.RollDateSwapNode;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.RollDateSwapConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.VanillaIborLegRollDateConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Tests the retrieval of a currency from roll date swap nodes.
 */
public class RollDateSwapNodeCurrencyVisitorTest {
  /** US region. */
  private static final ExternalId US = ExternalSchemes.financialRegionId("US");
  /** An empty security source. */
  private static final SecuritySource EMPTY_SECURITY_SOURCE = new InMemorySecuritySource();
  /** An empty convention source. */
  private static final ConventionSource EMPTY_CONVENTION_SOURCE = new InMemoryConventionSource();
  /** The curve node id mapper name */
  private static final String CNIM_NAME = "CNIM";
  /** The id of the pay leg convention */
  private static final ExternalId PAY_LEG_CONVENTION_ID = ExternalId.of(SCHEME, "USD Fixed Swap Leg");
  /** The id of the receive leg convention */
  private static final ExternalId RECEIVE_LEG_CONVENTION_ID = ExternalId.of(SCHEME, "USD Float Swap Leg");
  /** The id of the underlying LIBOR convention */
  private static final ExternalId LIBOR_CONVENTION_ID = ExternalId.of(SCHEME, "USD LIBOR");
  /** The id of the swap convention */
  private static final ExternalId SWAP_CONVENTION_ID = ExternalId.of(SCHEME, "USD IMM Swap");
  /** The id of the underlying LIBOR security */
  private static final ExternalId LIBOR_SECURITY_ID = ExternalId.of(SCHEME, "USDLIBOR3M");
  /** The pay leg convention */
  private static final SwapFixedLegConvention PAY_LEG_CONVENTION = new SwapFixedLegConvention("EUR Fixed Swap Leg",
      PAY_LEG_CONVENTION_ID.toBundle(), Tenor.SIX_MONTHS, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, Currency.EUR,
      US, 2, false, StubType.NONE, false, 0);
  /** The receive leg convention */
  private static final VanillaIborLegRollDateConvention RECEIVE_LEG_CONVENTION = new VanillaIborLegRollDateConvention("USD Float Swap Leg",
      RECEIVE_LEG_CONVENTION_ID.toBundle(), LIBOR_CONVENTION_ID, false, Tenor.THREE_MONTHS, StubType.NONE, false, 0);
  /** The LIBOR convention */
  private static final IborIndexConvention LIBOR_CONVENTION = new IborIndexConvention("USD LIBOR", LIBOR_CONVENTION_ID.toBundle(),
      DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, Currency.USD, LocalTime.of(11, 0), "", US, US, "");
  /** The swap convention */
  private static final RollDateSwapConvention SWAP_CONVENTION = new RollDateSwapConvention("USD IMM Swap", SWAP_CONVENTION_ID.toBundle(),
      PAY_LEG_CONVENTION_ID, RECEIVE_LEG_CONVENTION_ID, ExternalId.of(SCHEME, "Roll date"));
  /** The LIBOR security */
  private static final IborIndex LIBOR_SECURITY = new IborIndex("USD 3M LIBOR", Tenor.THREE_MONTHS, LIBOR_CONVENTION_ID);
  static {
    LIBOR_SECURITY.addExternalId(LIBOR_SECURITY_ID);
  }

  /**
   * Tests the behaviour when the swap convention is not available.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testNoSwapConvention() {
    final RollDateSwapNode node = new RollDateSwapNode(Tenor.THREE_MONTHS, 1, 2, SWAP_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(EMPTY_CONVENTION_SOURCE, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests the behaviour when the convention is available but is not a roll date swap convention.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongConventionType() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    final IborIndexConvention liborConvention = LIBOR_CONVENTION.clone();
    liborConvention.setExternalIdBundle(SWAP_CONVENTION_ID.toBundle());
    conventionSource.addConvention(liborConvention);
    final RollDateSwapNode node = new RollDateSwapNode(Tenor.THREE_MONTHS, 1, 2, SWAP_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests the behaviour when the pay leg convention is not available.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testNoPayConvention() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(SWAP_CONVENTION.clone());
    conventionSource.addConvention(RECEIVE_LEG_CONVENTION.clone());
    final RollDateSwapNode node = new RollDateSwapNode(Tenor.THREE_MONTHS, 1, 2, SWAP_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests the behaviour when the receive leg convention is not available.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testNoReceiveConvention() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(SWAP_CONVENTION.clone());
    conventionSource.addConvention(PAY_LEG_CONVENTION.clone());
    final RollDateSwapNode node = new RollDateSwapNode(Tenor.THREE_MONTHS, 1, 2, SWAP_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests the behaviour when neither the underlying convention of the floating leg nor the index security is available.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoIndexConventionOrSecurity() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(SWAP_CONVENTION.clone());
    conventionSource.addConvention(PAY_LEG_CONVENTION.clone());
    conventionSource.addConvention(RECEIVE_LEG_CONVENTION.clone());
    final RollDateSwapNode node = new RollDateSwapNode(Tenor.THREE_MONTHS, 1, 2, SWAP_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests the behaviour when the underlying security is not an ibor index.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongUnderlyingSecurityType() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    // note that the underlying id refers to the security
    final VanillaIborLegRollDateConvention receiveLegConvention = new VanillaIborLegRollDateConvention("USD Float Swap Leg",
        RECEIVE_LEG_CONVENTION_ID.toBundle(), LIBOR_SECURITY_ID, false, Tenor.THREE_MONTHS, StubType.NONE, false, 0);
    final RollDateSwapConvention swapConvention = new RollDateSwapConvention("USD IMM Swap", SWAP_CONVENTION_ID.toBundle(),
        PAY_LEG_CONVENTION_ID, RECEIVE_LEG_CONVENTION_ID, ExternalId.of(SCHEME, "Roll date"));
    conventionSource.addConvention(swapConvention);
    conventionSource.addConvention(PAY_LEG_CONVENTION.clone());
    conventionSource.addConvention(receiveLegConvention);
    final OvernightIndex overnightSecurity = new OvernightIndex("USD Overnight", PAY_LEG_CONVENTION_ID);
    overnightSecurity.addExternalId(LIBOR_SECURITY_ID);
    final InMemorySecuritySource securitySource = new InMemorySecuritySource();
    securitySource.addSecurity(overnightSecurity);
    final RollDateSwapNode node = new RollDateSwapNode(Tenor.THREE_MONTHS, 1, 2, SWAP_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, securitySource));
  }

  /**
   * Tests the behaviour when the ibor index security is available from the source but the convention referenced in
   * the security is not.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testNoConventionFromSecurity() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    // note that the underlying id refers to the security
    final VanillaIborLegRollDateConvention receiveLegConvention = new VanillaIborLegRollDateConvention("USD Float Swap Leg",
        RECEIVE_LEG_CONVENTION_ID.toBundle(), LIBOR_SECURITY_ID, false, Tenor.THREE_MONTHS, StubType.NONE, false, 0);
    final RollDateSwapConvention swapConvention = new RollDateSwapConvention("USD IMM Swap", SWAP_CONVENTION_ID.toBundle(),
        PAY_LEG_CONVENTION_ID, RECEIVE_LEG_CONVENTION_ID, ExternalId.of(SCHEME, "Roll date"));
    conventionSource.addConvention(swapConvention);
    conventionSource.addConvention(PAY_LEG_CONVENTION.clone());
    conventionSource.addConvention(receiveLegConvention);
    final InMemorySecuritySource securitySource = new InMemorySecuritySource();
    securitySource.addSecurity(LIBOR_SECURITY.clone());
    final RollDateSwapNode node = new RollDateSwapNode(Tenor.THREE_MONTHS, 1, 2, SWAP_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, securitySource));
  }

  /**
   * Tests the behaviour when the underlying convention is not available but the ibor index security and its
   * referenced convention is.
   */
  @Test
  public void testConventionFromSecurity() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    // note that the underlying id refers to the security
    final VanillaIborLegRollDateConvention receiveLegConvention = new VanillaIborLegRollDateConvention("USD Float Swap Leg",
        RECEIVE_LEG_CONVENTION_ID.toBundle(), LIBOR_SECURITY_ID, false, Tenor.THREE_MONTHS, StubType.NONE, false, 0);
    final RollDateSwapConvention swapConvention = new RollDateSwapConvention("USD IMM Swap", SWAP_CONVENTION_ID.toBundle(),
        PAY_LEG_CONVENTION_ID, RECEIVE_LEG_CONVENTION_ID, ExternalId.of(SCHEME, "Roll date"));
    conventionSource.addConvention(swapConvention);
    conventionSource.addConvention(PAY_LEG_CONVENTION.clone());
    conventionSource.addConvention(receiveLegConvention);
    conventionSource.addConvention(LIBOR_CONVENTION.clone());
    final InMemorySecuritySource securitySource = new InMemorySecuritySource();
    securitySource.addSecurity(LIBOR_SECURITY);
    final RollDateSwapNode node = new RollDateSwapNode(Tenor.THREE_MONTHS, 1, 2, SWAP_CONVENTION_ID, CNIM_NAME);
    assertEquals(node.accept(new CurveNodeCurrencyVisitor(conventionSource, securitySource)), Sets.newHashSet(Currency.EUR, Currency.USD));
  }

  /**
   * Tests the behaviour when the underlying convention is available.
   */
  @Test
  public void testFromConvention() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(SWAP_CONVENTION.clone());
    conventionSource.addConvention(PAY_LEG_CONVENTION.clone());
    conventionSource.addConvention(RECEIVE_LEG_CONVENTION.clone());
    conventionSource.addConvention(LIBOR_CONVENTION.clone());
    final RollDateSwapNode node = new RollDateSwapNode(Tenor.THREE_MONTHS, 1, 2, SWAP_CONVENTION_ID, CNIM_NAME);
    // don't need the security to be in the source
    assertEquals(node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE)), Sets.newHashSet(Currency.EUR, Currency.USD));
  }
}
