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
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.InMemoryConventionSource;
import com.opengamma.engine.InMemorySecuritySource;
import com.opengamma.financial.analytics.ircurve.strips.ThreeLegBasisSwapNode;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.financial.security.swap.InterpolationMethod;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Tests the retrieval of a currency from three leg basis swap nodes.
 */
@Test(groups = TestGroup.UNIT)
public class ThreeLegBasisSwapCurrencyVisitorTest {
  /** EU region. */
  private static final ExternalId EU = ExternalSchemes.financialRegionId("EU");
  /** An empty security source. */
  private static final SecuritySource EMPTY_SECURITY_SOURCE = new InMemorySecuritySource();
  /** The curve node id mapper name */
  private static final String CNIM_NAME = "CNIM";
  /** The id of the pay leg convention */
  private static final ExternalId PAY_LEG_CONVENTION_ID = ExternalId.of(SCHEME, "EUR Fixed Swap Leg");
  /** The id of the receive leg convention */
  private static final ExternalId RECEIVE_LEG_CONVENTION_ID = ExternalId.of(SCHEME, "EUR Float Swap Leg");
  /** The id of the spread leg convention */
  private static final ExternalId SPREAD_LEG_CONVENTION_ID = ExternalId.of(SCHEME, "EUR Spread Swap Leg");
  /** The id of the underlying 3M EURIBOR convention */
  private static final ExternalId EURIBOR_3M_CONVENTION_ID = ExternalId.of(SCHEME, "EURIBOR3M");
  /** The id of the underlying 6M EURIBOR convention */
  private static final ExternalId EURIBOR_6M_CONVENTION_ID = ExternalId.of(SCHEME, "EURIBOR6M");
  /** The id of the underlying 3M EURIBOR security */
  private static final ExternalId EURIBOR_3M_SECURITY_ID = ExternalId.of(SCHEME, "EURIBOR3M");
  /** The id of the underlying 6M EURIBOR security */
  private static final ExternalId EURIBOR_6M_SECURITY_ID = ExternalId.of(SCHEME, "EURIBOR6M");
  /** The pay leg convention */
  private static final SwapFixedLegConvention PAY_LEG_CONVENTION = new SwapFixedLegConvention("EUR Fixed Swap Leg",
      PAY_LEG_CONVENTION_ID.toBundle(), Tenor.SIX_MONTHS, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, Currency.EUR,
      EU, 2, false, StubType.NONE, false, 0);
  /** The receive leg convention */
  private static final VanillaIborLegConvention RECEIVE_LEG_CONVENTION = new VanillaIborLegConvention("EUR Float Swap Leg",
      RECEIVE_LEG_CONVENTION_ID.toBundle(), EURIBOR_3M_CONVENTION_ID, false, InterpolationMethod.NONE.name(), Tenor.THREE_MONTHS, 2, false,
      StubType.NONE, false, 0);
  /** The spread leg convention */
  private static final VanillaIborLegConvention SPREAD_LEG_CONVENTION = new VanillaIborLegConvention("EUR Spread Swap Leg",
      SPREAD_LEG_CONVENTION_ID.toBundle(), EURIBOR_6M_CONVENTION_ID, false, InterpolationMethod.NONE.name(), Tenor.SIX_MONTHS, 2, false,
      StubType.NONE, false, 0);
  // note that there is no need for separate EURIBOR conventions if all other fields are the same as the bundle should be used
  // by the source
  /** The 3M EURIBOR convention with a dummy currency */
  private static final IborIndexConvention EURIBOR_3M_CONVENTION = new IborIndexConvention("EUR 3M EURIBOR",
      EURIBOR_3M_CONVENTION_ID.toBundle(), DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING,
      2, false, Currency.of("EUX"), LocalTime.of(11, 0), "", EU, EU, "");
  /** The 6M EURIBOR convention with a dummy currency */
  private static final IborIndexConvention EURIBOR_6M_CONVENTION = new IborIndexConvention("EUR 6M EURIBOR",
      EURIBOR_6M_CONVENTION_ID.toBundle(), DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING,
      2, false, Currency.of("EUY"), LocalTime.of(11, 0), "", EU, EU, "");
  /** The 3M EURIBOR security */
  private static final IborIndex EURIBOR_3M_SECURITY = new IborIndex("EUR 3M EURIBOR", Tenor.THREE_MONTHS, EURIBOR_3M_CONVENTION_ID);
  static {
    EURIBOR_3M_SECURITY.addExternalId(EURIBOR_3M_SECURITY_ID);
  }
  /** The 6M EURIBOR security */
  private static final IborIndex EURIBOR_6M_SECURITY = new IborIndex("EUR 6M EURIBOR", Tenor.SIX_MONTHS, EURIBOR_6M_CONVENTION_ID);
  static {
    EURIBOR_6M_SECURITY.addExternalId(EURIBOR_6M_SECURITY_ID);
  }

  /**
   * Tests the behaviour when the pay convention is not available.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testNoPayConvention() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(EURIBOR_3M_CONVENTION.clone());
    conventionSource.addConvention(EURIBOR_6M_CONVENTION.clone());
    conventionSource.addConvention(RECEIVE_LEG_CONVENTION.clone());
    conventionSource.addConvention(SPREAD_LEG_CONVENTION.clone());
    final ThreeLegBasisSwapNode node = new ThreeLegBasisSwapNode(Tenor.ONE_MONTH, Tenor.TEN_YEARS, PAY_LEG_CONVENTION_ID, RECEIVE_LEG_CONVENTION_ID,
        SPREAD_LEG_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests the behaviour when the receive convention is not available.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testNoReceiveConvention() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(EURIBOR_3M_CONVENTION.clone());
    conventionSource.addConvention(EURIBOR_6M_CONVENTION.clone());
    conventionSource.addConvention(PAY_LEG_CONVENTION.clone());
    conventionSource.addConvention(SPREAD_LEG_CONVENTION.clone());
    final ThreeLegBasisSwapNode node = new ThreeLegBasisSwapNode(Tenor.ONE_MONTH, Tenor.TEN_YEARS, PAY_LEG_CONVENTION_ID, RECEIVE_LEG_CONVENTION_ID,
        SPREAD_LEG_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests the behaviour when the pay convention is not available.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testNoSpreadConvention() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(EURIBOR_3M_CONVENTION.clone());
    conventionSource.addConvention(EURIBOR_6M_CONVENTION.clone());
    conventionSource.addConvention(PAY_LEG_CONVENTION.clone());
    conventionSource.addConvention(RECEIVE_LEG_CONVENTION.clone());
    final ThreeLegBasisSwapNode node = new ThreeLegBasisSwapNode(Tenor.ONE_MONTH, Tenor.TEN_YEARS, PAY_LEG_CONVENTION_ID, RECEIVE_LEG_CONVENTION_ID,
        SPREAD_LEG_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests that all currencies are returned.
   */
  @Test
  public void test() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(EURIBOR_3M_CONVENTION.clone());
    conventionSource.addConvention(EURIBOR_6M_CONVENTION.clone());
    conventionSource.addConvention(PAY_LEG_CONVENTION.clone());
    conventionSource.addConvention(RECEIVE_LEG_CONVENTION.clone());
    conventionSource.addConvention(SPREAD_LEG_CONVENTION.clone());
    final ThreeLegBasisSwapNode node = new ThreeLegBasisSwapNode(Tenor.ONE_MONTH, Tenor.TEN_YEARS, PAY_LEG_CONVENTION_ID, RECEIVE_LEG_CONVENTION_ID,
        SPREAD_LEG_CONVENTION_ID, CNIM_NAME);
    assertEquals(node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE)),
        Sets.newHashSet(Currency.EUR, Currency.of("EUX"), Currency.of("EUY")));
  }
}
