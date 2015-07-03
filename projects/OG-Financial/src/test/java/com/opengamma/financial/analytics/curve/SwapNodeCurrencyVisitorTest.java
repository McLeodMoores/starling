/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve;

import static com.opengamma.financial.analytics.curve.CurveNodeCurrencyVisitorTest.EMPTY_SECURITY_SOURCE;
import static com.opengamma.financial.analytics.curve.CurveNodeCurrencyVisitorTest.SCHEME;
import static com.opengamma.financial.analytics.curve.CurveNodeCurrencyVisitorTest.US;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalTime;

import com.google.common.collect.Sets;
import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.CompoundingType;
import com.opengamma.engine.InMemoryConventionSource;
import com.opengamma.engine.InMemorySecuritySource;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.convention.CMSLegConvention;
import com.opengamma.financial.convention.CompoundingIborLegConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.ONArithmeticAverageLegConvention;
import com.opengamma.financial.convention.ONCompoundedLegRollDateConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.financial.security.swap.InterpolationMethod;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Tests the retrieval of a currency from swap nodes.
 */
public class SwapNodeCurrencyVisitorTest {
  /** The curve node id mapper name */
  private static final String CNIM_NAME = "CNIM";
  /** The id of an underlying swap index convention */
  private static final ExternalId SWAP_INDEX_CONVENTION_ID = ExternalId.of(SCHEME, "USD Swap Index");
  /** The id of an underlying LIBOR convention */
  private static final ExternalId LIBOR_CONVENTION_ID = ExternalId.of(SCHEME, "USD LIBOR");
  /** The id of an underlying LIBOR security */
  private static final ExternalId LIBOR_SECURITY_ID = ExternalId.of(SCHEME, "USDLIBOR3M");
  /** The id of the underlying overnight convention */
  private static final ExternalId OVERNIGHT_CONVENTION_ID = ExternalId.of(SCHEME, "USD Overnight");
  /** The id of the underlying overnight security */
  private static final ExternalId OVERNIGHT_SECURITY_ID = ExternalId.of(SCHEME, "USDFEDFUNDS");
  /** The id of a fixed leg convention */
  private static final ExternalId FIXED_LEG_CONVENTION_ID = ExternalId.of(SCHEME, "EUR Fixed Swap Leg");
  /** The id of a vanilla LIBOR leg convention */
  private static final ExternalId VANILLA_IBOR_LEG_CONVENTION_ID = ExternalId.of(SCHEME, "USD Float Swap Leg");
  /** The id of a CMS leg convention */
  private static final ExternalId CMS_LEG_CONVENTION_ID = ExternalId.of(SCHEME, "USD CMS Swap Leg");
  /** The id of a compounding LIBOR leg convention */
  private static final ExternalId COMPOUNDING_LIBOR_LEG_CONVENTION_ID = ExternalId.of(SCHEME, "USD Compounding LIBOR Swap Leg");
  /** The id of an OIS leg convention */
  private static final ExternalId OIS_LEG_CONVENTION_ID = ExternalId.of(SCHEME, "USD OIS Leg");
  /** The id of an overnight compounded leg convention */
  private static final ExternalId ON_COMPOUNDED_LEG_CONVENTION_ID = ExternalId.of(SCHEME, "USD Overnight Compounded Swap Leg");
  /** The id of an overnight average leg convention */
  private static final ExternalId ON_AVERAGE_LEG_CONVENTION_ID = ExternalId.of(SCHEME, "USD Overnight Average Swap Leg");
  /** A LIBOR convention */
  private static final IborIndexConvention LIBOR_CONVENTION = new IborIndexConvention("USD LIBOR", LIBOR_CONVENTION_ID.toBundle(),
      DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, Currency.USD, LocalTime.of(11, 0), "", US, US, "");
  /** The overnight convention */
  private static final OvernightIndexConvention OVERNIGHT_CONVENTION = new OvernightIndexConvention("USD Overnight",
      OVERNIGHT_CONVENTION_ID.toBundle(), DayCounts.ACT_360, 0, Currency.USD, US);
  /** A fixed swap leg convention */
  private static final SwapFixedLegConvention FIXED_LEG_CONVENTION = new SwapFixedLegConvention("EUR Fixed Swap Leg",
      FIXED_LEG_CONVENTION_ID.toBundle(), Tenor.SIX_MONTHS, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, Currency.EUR,
      US, 2, false, StubType.NONE, false, 0);
  /** A vanilla LIBOR swap leg convention */
  private static final VanillaIborLegConvention VANILLA_IBOR_LEG_CONVENTION = new VanillaIborLegConvention("USD Float Swap Leg",
      VANILLA_IBOR_LEG_CONVENTION_ID.toBundle(), LIBOR_CONVENTION_ID, false, InterpolationMethod.NONE.name(), Tenor.THREE_MONTHS, 2, false,
      StubType.NONE, false, 0);
  /** A CMS swap leg convention */
  private static final CMSLegConvention CMS_LEG_CONVENTION = new CMSLegConvention("USD CMS Swap Leg", CMS_LEG_CONVENTION_ID.toBundle(),
      SWAP_INDEX_CONVENTION_ID, Tenor.ONE_YEAR, false);
  /** A compounding ibor swap leg convention */
  private static final CompoundingIborLegConvention COMPOUNDING_LIBOR_LEG_CONVENTION = new CompoundingIborLegConvention("USD Compounding LIBOR Swap Leg",
      COMPOUNDING_LIBOR_LEG_CONVENTION_ID.toBundle(), LIBOR_CONVENTION_ID, Tenor.ONE_YEAR, CompoundingType.COMPOUNDING, Tenor.SIX_MONTHS, StubType.NONE,
      2, false, StubType.NONE, false, 0);
  /** An OIS swap leg convention */
  private static final OISLegConvention OIS_LEG_CONVENTION = new OISLegConvention("USD OIS Leg", OIS_LEG_CONVENTION_ID.toBundle(),
      OVERNIGHT_CONVENTION_ID, Tenor.ONE_YEAR, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, StubType.NONE, false, 0);
  /** An overnight compounded swap leg convention */
  private static final ONCompoundedLegRollDateConvention ON_COMPOUNDED_LEG_CONVENTION =
      new ONCompoundedLegRollDateConvention("USD Overnight Compounded Swap Leg", ON_COMPOUNDED_LEG_CONVENTION_ID.toBundle(), OVERNIGHT_CONVENTION_ID,
          Tenor.ONE_YEAR, StubType.NONE, false, 0);
  /** An OIS swap leg convention */
  private static final ONArithmeticAverageLegConvention ON_AVERAGE_LEG_CONVENTION = new ONArithmeticAverageLegConvention("USD Overnight Average Swap Leg",
      ON_AVERAGE_LEG_CONVENTION_ID.toBundle(), OVERNIGHT_CONVENTION_ID, Tenor.ONE_YEAR, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, StubType.NONE,
      false, 0);
  /** A LIBOR security */
  private static final IborIndex LIBOR_SECURITY = new IborIndex("USD 3M LIBOR", Tenor.THREE_MONTHS, LIBOR_CONVENTION_ID);
  static {
    LIBOR_SECURITY.addExternalId(LIBOR_SECURITY_ID);
  }
  /** The overnight security */
  private static final OvernightIndex OVERNIGHT_SECURITY = new OvernightIndex("USD FED FUNDS", OVERNIGHT_CONVENTION_ID);
  static {
    OVERNIGHT_SECURITY.addExternalId(OVERNIGHT_SECURITY_ID);
  }

  /**
   * Tests the behaviour if the pay leg convention is not available.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testNoPayLegConvention() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(VANILLA_IBOR_LEG_CONVENTION.clone());
    final SwapNode node = new SwapNode(Tenor.ONE_YEAR, Tenor.TEN_YEARS, FIXED_LEG_CONVENTION_ID, VANILLA_IBOR_LEG_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests the behaviour if the receive leg convention is not available.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testNoReceiveLegConvention() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(FIXED_LEG_CONVENTION.clone());
    final SwapNode node = new SwapNode(Tenor.ONE_YEAR, Tenor.TEN_YEARS, FIXED_LEG_CONVENTION_ID, VANILLA_IBOR_LEG_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests the behaviour if the swap index convention is not available for a CMS swap leg convention.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoSwapIndexConvention() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(FIXED_LEG_CONVENTION.clone());
    conventionSource.addConvention(CMS_LEG_CONVENTION.clone());
    final SwapNode node = new SwapNode(Tenor.ONE_YEAR, Tenor.TEN_YEARS, FIXED_LEG_CONVENTION_ID, CMS_LEG_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests the behaviour if the ibor index convention and security are not available for a compounding ibor leg convention.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoIborIndexConventionOrSecurityForCompoundingLeg() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(FIXED_LEG_CONVENTION.clone());
    conventionSource.addConvention(COMPOUNDING_LIBOR_LEG_CONVENTION.clone());
    final SwapNode node = new SwapNode(Tenor.ONE_YEAR, Tenor.TEN_YEARS, FIXED_LEG_CONVENTION_ID, COMPOUNDING_LIBOR_LEG_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests the behaviour if the convention does not reference an ibor index in a vanilla ibor leg convention.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongUnderlyingSecurityTypeForVanillaLeg() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    // note that the convention refers to the security
    final VanillaIborLegConvention compoundingLiborLegConvention = new VanillaIborLegConvention("USD LIBOR Swap Leg",
        VANILLA_IBOR_LEG_CONVENTION_ID.toBundle(), LIBOR_SECURITY_ID, false, InterpolationMethod.NONE.name(), Tenor.THREE_MONTHS,
        2, false, StubType.NONE, false, 2);
    conventionSource.addConvention(FIXED_LEG_CONVENTION.clone());
    conventionSource.addConvention(compoundingLiborLegConvention);
    final OvernightIndex overnightSecurity = OVERNIGHT_SECURITY.clone();
    overnightSecurity.setExternalIdBundle(LIBOR_SECURITY_ID.toBundle());
    final InMemorySecuritySource securitySource = new InMemorySecuritySource();
    securitySource.addSecurity(overnightSecurity);
    final SwapNode node = new SwapNode(Tenor.ONE_YEAR, Tenor.TEN_YEARS, FIXED_LEG_CONVENTION_ID, VANILLA_IBOR_LEG_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, securitySource));
  }

  /**
   * Tests the behaviour if the convention does not reference an ibor index in a compounding ibor leg convention.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongUnderlyingSecurityTypeForCompoundingLeg() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    // note that the convention refers to the security
    final CompoundingIborLegConvention compoundingLiborLegConvention = new CompoundingIborLegConvention("USD Compounding LIBOR Swap Leg",
        COMPOUNDING_LIBOR_LEG_CONVENTION_ID.toBundle(), LIBOR_SECURITY_ID, Tenor.ONE_YEAR, CompoundingType.COMPOUNDING, Tenor.SIX_MONTHS, StubType.NONE,
        2, false, StubType.NONE, false, 0);
    conventionSource.addConvention(FIXED_LEG_CONVENTION.clone());
    conventionSource.addConvention(compoundingLiborLegConvention);
    final OvernightIndex overnightSecurity = OVERNIGHT_SECURITY.clone();
    overnightSecurity.setExternalIdBundle(LIBOR_SECURITY_ID.toBundle());
    final InMemorySecuritySource securitySource = new InMemorySecuritySource();
    securitySource.addSecurity(overnightSecurity);
    final SwapNode node = new SwapNode(Tenor.ONE_YEAR, Tenor.TEN_YEARS, FIXED_LEG_CONVENTION_ID, COMPOUNDING_LIBOR_LEG_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, securitySource));
  }

  /**
   * Tests that all currencies are returned if the convention from the underlying security is available.
   */
  @Test
  public void testConventionFromUnderlyingSecurityForCompoundingLeg() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    // note that the convention refers to the security
    final CompoundingIborLegConvention compoundingLiborLegConvention = new CompoundingIborLegConvention("USD Compounding LIBOR Swap Leg",
        COMPOUNDING_LIBOR_LEG_CONVENTION_ID.toBundle(), LIBOR_SECURITY_ID, Tenor.ONE_YEAR, CompoundingType.COMPOUNDING, Tenor.SIX_MONTHS, StubType.NONE,
        2, false, StubType.NONE, false, 0);
    conventionSource.addConvention(FIXED_LEG_CONVENTION.clone());
    conventionSource.addConvention(compoundingLiborLegConvention);
    conventionSource.addConvention(LIBOR_CONVENTION.clone());
    final InMemorySecuritySource securitySource = new InMemorySecuritySource();
    securitySource.addSecurity(LIBOR_SECURITY.clone());
    final SwapNode node = new SwapNode(Tenor.ONE_YEAR, Tenor.TEN_YEARS, FIXED_LEG_CONVENTION_ID, COMPOUNDING_LIBOR_LEG_CONVENTION_ID, CNIM_NAME);
    assertEquals(node.accept(new CurveNodeCurrencyVisitor(conventionSource, securitySource)), Sets.newHashSet(Currency.USD, Currency.EUR));
  }

  /**
   * Tests that all currencies are returned if the underlying convention is available.
   */
  @Test
  public void testUnderlyingConventionForCompoundingLeg() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(FIXED_LEG_CONVENTION.clone());
    conventionSource.addConvention(COMPOUNDING_LIBOR_LEG_CONVENTION.clone());
    conventionSource.addConvention(LIBOR_CONVENTION.clone());
    final InMemorySecuritySource securitySource = new InMemorySecuritySource();
    securitySource.addSecurity(LIBOR_SECURITY.clone());
    final SwapNode node = new SwapNode(Tenor.ONE_YEAR, Tenor.TEN_YEARS, FIXED_LEG_CONVENTION_ID, COMPOUNDING_LIBOR_LEG_CONVENTION_ID, CNIM_NAME);
    assertEquals(node.accept(new CurveNodeCurrencyVisitor(conventionSource, securitySource)), Sets.newHashSet(Currency.USD, Currency.EUR));
  }

  /**
   * Tests the behaviour if the overnight index convention and security are not available for an OIS leg convention.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoOvernightIndexConventionOrSecurityForOisConvention() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(FIXED_LEG_CONVENTION.clone());
    conventionSource.addConvention(OIS_LEG_CONVENTION.clone());
    final SwapNode node = new SwapNode(Tenor.ONE_YEAR, Tenor.TEN_YEARS, FIXED_LEG_CONVENTION_ID, OIS_LEG_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests the behaviour if the convention does not reference an overnight index in a OIS ibor leg convention.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongUnderlyingSecurityTypeForOisLeg() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    // note that the convention refers to the security
    final OISLegConvention oisLegConvention = new OISLegConvention("USD OIS Leg", OIS_LEG_CONVENTION_ID.toBundle(),
        OVERNIGHT_SECURITY_ID, Tenor.ONE_YEAR, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, StubType.NONE, false, 0);
    conventionSource.addConvention(FIXED_LEG_CONVENTION.clone());
    conventionSource.addConvention(oisLegConvention);
    final IborIndex indexSecurity = LIBOR_SECURITY.clone();
    indexSecurity.setExternalIdBundle(OVERNIGHT_SECURITY_ID.toBundle());
    final InMemorySecuritySource securitySource = new InMemorySecuritySource();
    securitySource.addSecurity(indexSecurity);
    final SwapNode node = new SwapNode(Tenor.ONE_YEAR, Tenor.TEN_YEARS, FIXED_LEG_CONVENTION_ID, OIS_LEG_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, securitySource));
  }

  /**
   * Tests that all currencies are returned if the convention from the underlying security is available.
   */
  @Test
  public void testConventionFromUnderlyingSecurityForOisLeg() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    // note that the convention refers to the security
    final OISLegConvention oisLegConvention = new OISLegConvention("USD OIS Leg", OIS_LEG_CONVENTION_ID.toBundle(),
        OVERNIGHT_SECURITY_ID, Tenor.ONE_YEAR, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, StubType.NONE, false, 0);
    conventionSource.addConvention(FIXED_LEG_CONVENTION.clone());
    conventionSource.addConvention(oisLegConvention);
    conventionSource.addConvention(OVERNIGHT_CONVENTION.clone());
    final InMemorySecuritySource securitySource = new InMemorySecuritySource();
    securitySource.addSecurity(OVERNIGHT_SECURITY.clone());
    final SwapNode node = new SwapNode(Tenor.ONE_YEAR, Tenor.TEN_YEARS, FIXED_LEG_CONVENTION_ID, OIS_LEG_CONVENTION_ID, CNIM_NAME);
    assertEquals(node.accept(new CurveNodeCurrencyVisitor(conventionSource, securitySource)), Sets.newHashSet(Currency.USD, Currency.EUR));
  }

  /**
   * Tests that all currencies are returned if the underlying convention is available.
   */
  @Test
  public void testUnderlyingConventionForOisLeg() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(FIXED_LEG_CONVENTION.clone());
    conventionSource.addConvention(OIS_LEG_CONVENTION.clone());
    conventionSource.addConvention(OVERNIGHT_CONVENTION.clone());
    final InMemorySecuritySource securitySource = new InMemorySecuritySource();
    securitySource.addSecurity(OVERNIGHT_SECURITY.clone());
    final SwapNode node = new SwapNode(Tenor.ONE_YEAR, Tenor.TEN_YEARS, FIXED_LEG_CONVENTION_ID, OIS_LEG_CONVENTION_ID, CNIM_NAME);
    assertEquals(node.accept(new CurveNodeCurrencyVisitor(conventionSource, securitySource)), Sets.newHashSet(Currency.USD, Currency.EUR));
  }

  /**
   * Tests the behaviour if the overnight index convention and security are not available for an overnight compounded leg convention.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoOvernightIndexConventionOrSecurityForOnCompoundedRollDateLeg() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(FIXED_LEG_CONVENTION.clone());
    conventionSource.addConvention(ON_COMPOUNDED_LEG_CONVENTION.clone());
    final SwapNode node = new SwapNode(Tenor.ONE_YEAR, Tenor.TEN_YEARS, FIXED_LEG_CONVENTION_ID, ON_COMPOUNDED_LEG_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests the behaviour if the convention does not reference an overnight index in an overnight compounded leg convention.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongUnderlyingSecurityTypeForOnCompoundedRollDateLeg() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    // note that the convention refers to the security
    final ONCompoundedLegRollDateConvention onCompoundedLegConvention =
        new ONCompoundedLegRollDateConvention("USD Overnight Compounded Swap Leg", ON_COMPOUNDED_LEG_CONVENTION_ID.toBundle(), OVERNIGHT_SECURITY_ID,
            Tenor.ONE_YEAR, StubType.NONE, false, 0);
    conventionSource.addConvention(FIXED_LEG_CONVENTION.clone());
    conventionSource.addConvention(onCompoundedLegConvention);
    final IborIndex indexSecurity = LIBOR_SECURITY.clone();
    indexSecurity.setExternalIdBundle(OVERNIGHT_SECURITY_ID.toBundle());
    final InMemorySecuritySource securitySource = new InMemorySecuritySource();
    securitySource.addSecurity(indexSecurity);
    final SwapNode node = new SwapNode(Tenor.ONE_YEAR, Tenor.TEN_YEARS, FIXED_LEG_CONVENTION_ID, ON_COMPOUNDED_LEG_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, securitySource));
  }

  /**
   * Tests that all currencies are returned if the convention from the underlying security is available.
   */
  @Test
  public void testConventionFromUnderlyingSecurityForOnCompoundedRollDateLeg() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    // note that the convention refers to the security
    final ONCompoundedLegRollDateConvention onCompoundedLegConvention =
        new ONCompoundedLegRollDateConvention("USD Overnight Compounded Swap Leg", ON_COMPOUNDED_LEG_CONVENTION_ID.toBundle(), OVERNIGHT_SECURITY_ID,
            Tenor.ONE_YEAR, StubType.NONE, false, 0);
    conventionSource.addConvention(FIXED_LEG_CONVENTION.clone());
    conventionSource.addConvention(onCompoundedLegConvention);
    conventionSource.addConvention(OVERNIGHT_CONVENTION.clone());
    final InMemorySecuritySource securitySource = new InMemorySecuritySource();
    securitySource.addSecurity(OVERNIGHT_SECURITY.clone());
    final SwapNode node = new SwapNode(Tenor.ONE_YEAR, Tenor.TEN_YEARS, FIXED_LEG_CONVENTION_ID, ON_COMPOUNDED_LEG_CONVENTION_ID, CNIM_NAME);
    assertEquals(node.accept(new CurveNodeCurrencyVisitor(conventionSource, securitySource)), Sets.newHashSet(Currency.USD, Currency.EUR));
  }

  /**
   * Tests that all currencies are returned if the underlying convention is available.
   */
  @Test
  public void testUnderlyingConventionForOnCompoundedRollDateLeg() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(FIXED_LEG_CONVENTION.clone());
    conventionSource.addConvention(ON_COMPOUNDED_LEG_CONVENTION.clone());
    conventionSource.addConvention(OVERNIGHT_CONVENTION.clone());
    final InMemorySecuritySource securitySource = new InMemorySecuritySource();
    securitySource.addSecurity(OVERNIGHT_SECURITY.clone());
    final SwapNode node = new SwapNode(Tenor.ONE_YEAR, Tenor.TEN_YEARS, FIXED_LEG_CONVENTION_ID, ON_COMPOUNDED_LEG_CONVENTION_ID, CNIM_NAME);
    assertEquals(node.accept(new CurveNodeCurrencyVisitor(conventionSource, securitySource)), Sets.newHashSet(Currency.USD, Currency.EUR));
  }

  /**
   * Tests the behaviour if the overnight index convention and security are not available for an overnight compounded leg convention.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoOvernightIndexConventionOrSecurityForOnAverageLeg() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(FIXED_LEG_CONVENTION.clone());
    conventionSource.addConvention(ON_AVERAGE_LEG_CONVENTION.clone());
    final SwapNode node = new SwapNode(Tenor.ONE_YEAR, Tenor.TEN_YEARS, FIXED_LEG_CONVENTION_ID, ON_AVERAGE_LEG_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests the behaviour if the convention does not reference an overnight index in an overnight average leg convention.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongUnderlyingSecurityTypeForOnAverageLeg() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    // note that the convention refers to the security
    final ONArithmeticAverageLegConvention onAverageLegConvention = new ONArithmeticAverageLegConvention("USD Overnight Average Swap Leg",
        ON_AVERAGE_LEG_CONVENTION_ID.toBundle(), OVERNIGHT_SECURITY_ID, Tenor.ONE_YEAR, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, StubType.NONE,
        false, 0);
    conventionSource.addConvention(FIXED_LEG_CONVENTION.clone());
    conventionSource.addConvention(onAverageLegConvention);
    final IborIndex indexSecurity = LIBOR_SECURITY.clone();
    indexSecurity.setExternalIdBundle(OVERNIGHT_SECURITY_ID.toBundle());
    final InMemorySecuritySource securitySource = new InMemorySecuritySource();
    securitySource.addSecurity(indexSecurity);
    final SwapNode node = new SwapNode(Tenor.ONE_YEAR, Tenor.TEN_YEARS, FIXED_LEG_CONVENTION_ID, ON_AVERAGE_LEG_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, securitySource));
  }

  /**
   * Tests that all currencies are returned if the convention from the underlying security is available.
   */
  @Test
  public void testConventionFromUnderlyingSecurityForOnAverageLeg() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    // note that the convention refers to the security
    final ONArithmeticAverageLegConvention onAverageLegConvention = new ONArithmeticAverageLegConvention("USD Overnight Average Swap Leg",
        ON_AVERAGE_LEG_CONVENTION_ID.toBundle(), OVERNIGHT_SECURITY_ID, Tenor.ONE_YEAR, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, StubType.NONE,
        false, 0);
    conventionSource.addConvention(FIXED_LEG_CONVENTION.clone());
    conventionSource.addConvention(onAverageLegConvention);
    conventionSource.addConvention(OVERNIGHT_CONVENTION.clone());
    final InMemorySecuritySource securitySource = new InMemorySecuritySource();
    securitySource.addSecurity(OVERNIGHT_SECURITY);
    final SwapNode node = new SwapNode(Tenor.ONE_YEAR, Tenor.TEN_YEARS, FIXED_LEG_CONVENTION_ID, ON_AVERAGE_LEG_CONVENTION_ID, CNIM_NAME);
    assertEquals(node.accept(new CurveNodeCurrencyVisitor(conventionSource, securitySource)), Sets.newHashSet(Currency.USD, Currency.EUR));
  }

  /**
   * Tests that all currencies are returned if the underlying convention is available.
   */
  @Test
  public void testUnderlyingConventionForOnAverageLeg() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(FIXED_LEG_CONVENTION.clone());
    conventionSource.addConvention(ON_AVERAGE_LEG_CONVENTION.clone());
    conventionSource.addConvention(OVERNIGHT_CONVENTION.clone());
    final InMemorySecuritySource securitySource = new InMemorySecuritySource();
    securitySource.addSecurity(OVERNIGHT_SECURITY);
    final SwapNode node = new SwapNode(Tenor.ONE_YEAR, Tenor.TEN_YEARS, FIXED_LEG_CONVENTION_ID, ON_AVERAGE_LEG_CONVENTION_ID, CNIM_NAME);
    assertEquals(node.accept(new CurveNodeCurrencyVisitor(conventionSource, securitySource)), Sets.newHashSet(Currency.USD, Currency.EUR));
  }
}
