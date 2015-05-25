/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.LocalTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.CompoundingType;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.ircurve.strips.ContinuouslyCompoundedRateNode;
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.DiscountFactorNode;
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.financial.analytics.ircurve.strips.FXForwardNode;
import com.opengamma.financial.analytics.ircurve.strips.InflationNodeType;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.RollDateFRANode;
import com.opengamma.financial.analytics.ircurve.strips.RollDateSwapNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.analytics.ircurve.strips.ZeroCouponInflationNode;
import com.opengamma.financial.convention.CMSLegConvention;
import com.opengamma.financial.convention.CompoundingIborLegConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InflationLegConvention;
import com.opengamma.financial.convention.InterestRateFutureConvention;
import com.opengamma.financial.convention.MockConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.PriceIndexConvention;
import com.opengamma.financial.convention.RollDateFRAConvention;
import com.opengamma.financial.convention.RollDateSwapConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.SwapIndexConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.rolldate.RollDateAdjusterFactory;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.financial.security.index.PriceIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link CurveNodeCurrencyVisitor}.
 */
@Test(groups = TestGroup.UNIT)
public class CurveNodeCurrencyVisitorTest {
  /** Test scheme. */
  protected static final String SCHEME = "Test";
  /** US region. */
  protected static final ExternalId US = ExternalSchemes.financialRegionId("US");
  /** EU region. */
  protected static final ExternalId EU = ExternalSchemes.financialRegionId("EU");
  /** NY+LON holidays. */
  protected static final ExternalId NYLON = ExternalSchemes.financialRegionId("US+GB");
  /** An empty security source. */
  protected static final SecuritySource EMPTY_SECURITY_SOURCE = new MySecuritySource(Collections.<ExternalIdBundle, Security>emptyMap());
  /** An empty convention source. */
  protected static final ConventionSource EMPTY_CONVENTION_SOURCE = new TestConventionSource(Collections.<ExternalId, Convention>emptyMap());
  /** An empty config source. */
  protected static final ConfigSource EMPTY_CONFIG_SOURCE = new MyConfigSource(Collections.<String, Object>emptyMap());

  /** The id for a USD fixed swap leg convention */
  private static final ExternalId FIXED_LEG_ID = ExternalId.of(SCHEME, "USD Swap Fixed Leg");
  /** The name of the USD LIBOR convention */
  private static final String USDLIBOR_CONVENTION_NAME = "ICE LIBOR USD";
  /** The id for a quarterly IMM future convention */
  private static final ExternalId RATE_FUTURE_3M_ID = ExternalId.of(SCHEME, "USD 3m Rate Future");
  /** The id for a quarterly USD floating swap leg convention */
  private static final ExternalId SWAP_3M_IBOR_ID = ExternalId.of(SCHEME, "USD 3m Floating Leg");
  /** The id for a semi-annual USD floating swap leg convention */
  private static final ExternalId SWAP_6M_EURIBOR_ID = ExternalId.of(SCHEME, "EUR 6m Floating Leg");
  /** The id for a USD overnight index convention */
  private static final ExternalId OVERNIGHT_CONVENTION_ID = ExternalId.of(SCHEME, "USD Overnight");
  /** The id for a OIS USD swap leg convention */
  private static final ExternalId OIS_ID = ExternalId.of(SCHEME, "USD OIS Leg");
  /** The id for a FX forward convention */
  private static final ExternalId FX_FORWARD_ID = ExternalId.of(SCHEME, "FX Forward");
  /** The id for a 3M swap index convention */
  private static final ExternalId SWAP_INDEX_ID = ExternalId.of(SCHEME, "3M Swap Index");
  /** The id for a USD CMS swap leg convention */
  private static final ExternalId CMS_SWAP_ID = ExternalId.of(SCHEME, "USD CMS");
  /** The id for a USD compounded LIBOR leg convention */
  private static final ExternalId COMPOUNDING_IBOR_ID = ExternalId.of(SCHEME, "USD Compounding Libor");
  /** The id for an IMM quarterly expiry convention */
  private static final ExternalId IMM_3M_EXPIRY_CONVENTION = ExternalId.of(SCHEME, RollDateAdjusterFactory.QUARTERLY_IMM_ROLL_STRING);
  /** The id for a USD price index convention */
  private static final ExternalId PRICE_INDEX_US_CONVENTION_ID = ExternalId.of(SCHEME, "USD CPI");
  /** The id for a USD zero-coupon inflation swap leg convention */
  private static final ExternalId ZERO_COUPON_INFLATION_ID = ExternalId.of(SCHEME, "ZCI");
  /** The id for a USD IMM swap leg convention */
  private static final ExternalId IMM_SWAP_ID = ExternalId.of(SCHEME, "USD IMM Swap");
  /** The id for a USD IMM FRA leg convention */
  private static final ExternalId IMM_FRA_ID = ExternalId.of(SCHEME, "USD IMM FRA");
  /** The name of the USD overnight index */
  private static final String USD_OVERNIGHT_NAME = "Fed Funds Effective Rate";
  /** A USD overnight index security */
  private static final OvernightIndex USD_OVERNIGHT = new OvernightIndex(USD_OVERNIGHT_NAME, OVERNIGHT_CONVENTION_ID);
  /** The id for the USD overnight rate */
  private static final ExternalId USD_OVERNIGHT_ID = ExternalSchemes.bloombergTickerSecurityId("FEDL1 Index");
  /** The id for a USD LIBOR index convention */
  private static final ExternalId USDLIBOR_CONVENTION_ID = ExternalId.of(SCHEME, USDLIBOR_CONVENTION_NAME);
  /** A USD LIBOR index convention */
  private static final IborIndexConvention USDLIBOR_CONVENTION = new IborIndexConvention(USDLIBOR_CONVENTION_NAME, ExternalIdBundle.of(USDLIBOR_CONVENTION_ID),
      DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, Currency.USD, LocalTime.of(11, 0), "US", US, US, "Page");
  /** The name of a 1M USD LIBOR index security */
  private static final String USDLIBOR1M_NAME = "USDLIBOR1M";
  /** A 1M USD LIBOR index security */
  private static final IborIndex USDLIBOR1M = new IborIndex(USDLIBOR1M_NAME, "ICE LIBOR 1M - USD", Tenor.ONE_MONTH, USDLIBOR_CONVENTION_ID);
  /** A 1M USD LIBOR ticker */
  private static final ExternalId USDLIBOR1M_ID = ExternalSchemes.bloombergTickerSecurityId("US0001M Index");
  /** The name of a 3M USD LIBOR index security */
  private static final String USDLIBOR3M_NAME = "USDLIBOR3M";
  /** A 3M USD LIBOR index security */
  private static final IborIndex USDLIBOR3M = new IborIndex(USDLIBOR3M_NAME, "ICE LIBOR 3M - USD", Tenor.THREE_MONTHS, USDLIBOR_CONVENTION_ID);
  /** A 3M USD LIBOR ticker */
  private static final ExternalId USDLIBOR3M_ID = ExternalSchemes.bloombergTickerSecurityId("US0003M Index");
  /** The name of a 6M USD LIBOR index security */
  private static final String USDLIBOR6M_NAME = "USDLIBOR6M";
  /** A 6M USD LIBOR index security */
  private static final IborIndex USDLIBOR6M = new IborIndex(USDLIBOR6M_NAME, "ICE LIBOR 6M - USD", Tenor.SIX_MONTHS, USDLIBOR_CONVENTION_ID);
  /** A 6M USD LIBOR ticker */
  private static final ExternalId USDLIBOR6M_ID = ExternalSchemes.bloombergTickerSecurityId("US0006M Index");
  /** The name of a EURIBOR convention */
  private static final String EURIBOR_CONVENTION_NAME = "EUR Euribor";
  /** The id of a EURIBOR convention */
  private static final ExternalId EURIBOR_CONVENTION_ID = ExternalId.of(SCHEME, EURIBOR_CONVENTION_NAME);
  /** A EURIBOR convention */
  private static final IborIndexConvention EURIBOR_CONVENTION = new IborIndexConvention(EURIBOR_CONVENTION_NAME, ExternalIdBundle.of(EURIBOR_CONVENTION_ID),
      DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, Currency.EUR, LocalTime.of(11, 0), "EU", EU, EU, "Page");
  /** The name of a 1M EURIBOR index security */
  private static final String EURIBOR1M_NAME = "EURIBOR1M";
  /** A 1M EURIBOR index security */
  private static final IborIndex EURIBOR1M = new IborIndex(EURIBOR1M_NAME, "EURIBOR 1M ACT/360", Tenor.ONE_MONTH, EURIBOR_CONVENTION_ID);
  /** A 1M EURIBOR ticker */
  private static final ExternalId EURIBOR1M_ID = ExternalSchemes.bloombergTickerSecurityId("EUR001M Index");
  /** The name of a 3M EURIBOR index security */
  private static final String EURIBOR3M_NAME = "EURIBOR3M";
  /** A 3M EURIBOR index security */
  private static final IborIndex EURIBOR3M = new IborIndex(EURIBOR3M_NAME, "EURIBOR 3M ACT/360", Tenor.THREE_MONTHS, EURIBOR_CONVENTION_ID);
  /** A 3M EURIBOR ticker */
  private static final ExternalId EURIBOR3M_ID = ExternalSchemes.bloombergTickerSecurityId("EUR003M Index");
  /** The name of a 6M EURIBOR index security */
  private static final String EURIBOR6M_NAME = "EURIBOR6M";
  /** A 6M EURIBOR index security */
  private static final IborIndex EURIBOR6M = new IborIndex(EURIBOR6M_NAME, "EURIBOR 6M ACT/360", Tenor.SIX_MONTHS, EURIBOR_CONVENTION_ID);
  /** A 6M EURIBOR ticker */
  private static final ExternalId EURIBOR6M_ID = ExternalSchemes.bloombergTickerSecurityId("EUR006M Index");

  private static final SwapFixedLegConvention FIXED_LEG = new SwapFixedLegConvention("USD Swap Fixed Leg", ExternalId.of(SCHEME, "USD Swap Fixed Leg").toBundle(),
      Tenor.SIX_MONTHS, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, Currency.USD, NYLON, 2, false, StubType.NONE, false, 2);
  private static final VanillaIborLegConvention SWAP_3M_LIBOR = new VanillaIborLegConvention("USD 3m Floating Leg", ExternalId.of(SCHEME, "USD 3m Floating Leg").toBundle(),
      USDLIBOR3M_ID, false, SCHEME, Tenor.THREE_MONTHS, 2, false, StubType.NONE, false, 2);
  private static final VanillaIborLegConvention SWAP_6M_EURIBOR = new VanillaIborLegConvention("EUR 6m Floating Leg", ExternalId.of(SCHEME, "EUR 6m Floating Leg").toBundle(),
      EURIBOR3M_ID, false, SCHEME, Tenor.SIX_MONTHS, 2, false, StubType.NONE, false,2 );
  private static final OISLegConvention OIS = new OISLegConvention("USD OIS Leg", ExternalId.of(SCHEME, "USD OIS Leg").toBundle(), USD_OVERNIGHT_ID,
      Tenor.ONE_YEAR, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, StubType.NONE, false, 1);
  private static final InterestRateFutureConvention RATE_FUTURE_3M = new InterestRateFutureConvention("USD 3m Rate Future", RATE_FUTURE_3M_ID.toBundle(),
      IMM_3M_EXPIRY_CONVENTION, NYLON, USDLIBOR3M_ID);
  private static final OvernightIndexConvention OVERNIGHT = new OvernightIndexConvention("USD Overnight", ExternalId.of(SCHEME, "USD Overnight").toBundle(),
      DayCounts.ACT_360, 1, Currency.USD, NYLON);
  private static final SwapIndexConvention SWAP_INDEX = new SwapIndexConvention("3M Swap Index", ExternalId.of(SCHEME, "3M Swap Index").toBundle(), LocalTime.of(11, 0),
      SWAP_3M_IBOR_ID);
  private static final CompoundingIborLegConvention COMPOUNDING_IBOR = new CompoundingIborLegConvention("USD Compounding Libor", ExternalId.of(SCHEME, "USD Compounding Libor").toBundle(),
      USDLIBOR3M_ID, Tenor.THREE_MONTHS, CompoundingType.COMPOUNDING, Tenor.ONE_MONTH, StubType.SHORT_START, 2, false, StubType.LONG_START, true, 1);
  private static final PriceIndexConvention PRICE_INDEX_CONVENTION = new PriceIndexConvention("USD CPI", ExternalId.of(SCHEME, "USD CPI").toBundle(), Currency.USD, US,
      ExternalId.of("TS", "CPI"));
  private static final String PRICE_INDEX_US_NAME = "US CPI Urban Consumers NSA";
  private static final ExternalId PRICE_INDEX_US_ID = ExternalSchemes.bloombergTickerSecurityId("CPURNSA Index");
  private static final PriceIndex PRICE_INDEX_US = new PriceIndex(PRICE_INDEX_US_NAME, "US CPI Urban Consumers NSA - Nice Description", PRICE_INDEX_US_CONVENTION_ID);
  private static final InflationLegConvention INFLATION_LEG = new InflationLegConvention("ZCI", ExternalId.of(SCHEME, "ZCI").toBundle(), BusinessDayConventions.MODIFIED_FOLLOWING, DayCounts.ACT_360, false,
      3, 2, PRICE_INDEX_US_ID);
  private static final CMSLegConvention CMS = new CMSLegConvention("USD CMS", ExternalId.of(SCHEME, "USD CMS").toBundle(), SWAP_INDEX_ID, Tenor.SIX_MONTHS, false);
  private static final RollDateSwapConvention IMM_SWAP = new RollDateSwapConvention("USD IMM Swap", ExternalId.of(SCHEME, "USD IMM Swap").toBundle(), FIXED_LEG_ID, SWAP_3M_IBOR_ID, IMM_3M_EXPIRY_CONVENTION);
  private static final RollDateFRAConvention IMM_FRA = new RollDateFRAConvention("USD IMM FRA", ExternalId.of(SCHEME, "USD IMM FRA").toBundle(), USDLIBOR3M_ID, IMM_3M_EXPIRY_CONVENTION);
  private static final Map<ExternalId, Convention> CONVENTIONS = new HashMap<>();
  private static final ConventionSource CONVENTION_SOURCE;
  private static final CurveNodeCurrencyVisitor VISITOR;


  private static final Map<ExternalIdBundle, Security> SECURITY_MAP = new HashMap<>();
  private static final SecuritySource SECURITY_SOURCE;


  private static final CurveNodeCurrencyVisitor EMPTY_CONVENTIONS = new CurveNodeCurrencyVisitor(new TestConventionSource(Collections.<ExternalId, Convention>emptyMap()),
      new MySecuritySource(new HashMap<ExternalIdBundle, Security>()));

  static {
    CONVENTIONS.put(FIXED_LEG_ID, FIXED_LEG);
    CONVENTIONS.put(RATE_FUTURE_3M_ID, RATE_FUTURE_3M);
    CONVENTIONS.put(SWAP_3M_IBOR_ID, SWAP_3M_LIBOR);
    CONVENTIONS.put(OIS_ID, OIS);
    CONVENTIONS.put(OVERNIGHT_CONVENTION_ID, OVERNIGHT);
    CONVENTIONS.put(SWAP_INDEX_ID, SWAP_INDEX);
    CONVENTIONS.put(CMS_SWAP_ID, CMS);
    CONVENTIONS.put(SWAP_6M_EURIBOR_ID, SWAP_6M_EURIBOR);
    CONVENTIONS.put(COMPOUNDING_IBOR_ID, COMPOUNDING_IBOR);
    CONVENTIONS.put(PRICE_INDEX_US_CONVENTION_ID, PRICE_INDEX_CONVENTION);
    CONVENTIONS.put(ZERO_COUPON_INFLATION_ID, INFLATION_LEG);
    CONVENTIONS.put(IMM_SWAP_ID, IMM_SWAP);
    CONVENTIONS.put(IMM_FRA_ID, IMM_FRA);
    CONVENTIONS.put(EURIBOR_CONVENTION_ID, EURIBOR_CONVENTION);
    CONVENTIONS.put(USDLIBOR_CONVENTION_ID, USDLIBOR_CONVENTION);
    CONVENTION_SOURCE = new TestConventionSource(CONVENTIONS);
    // Security map. Used for index.
    SECURITY_MAP.put(USD_OVERNIGHT_ID.toBundle(), USD_OVERNIGHT);
    SECURITY_MAP.put(USDLIBOR1M_ID.toBundle(), USDLIBOR1M);
    SECURITY_MAP.put(USDLIBOR3M_ID.toBundle(), USDLIBOR3M);
    SECURITY_MAP.put(USDLIBOR6M_ID.toBundle(), USDLIBOR6M);
    SECURITY_MAP.put(EURIBOR1M_ID.toBundle(), EURIBOR1M);
    SECURITY_MAP.put(EURIBOR3M_ID.toBundle(), EURIBOR3M);
    SECURITY_MAP.put(EURIBOR6M_ID.toBundle(), EURIBOR6M);
    SECURITY_MAP.put(PRICE_INDEX_US_ID.toBundle(), PRICE_INDEX_US);
    SECURITY_SOURCE = new MySecuritySource(SECURITY_MAP);
    VISITOR = new CurveNodeCurrencyVisitor(CONVENTION_SOURCE, SECURITY_SOURCE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConventionSource() {
    new CurveNodeCurrencyVisitor(null, null);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullFRAConvention() {
    final FRANode node = new FRANode(Tenor.ONE_DAY, Tenor.THREE_MONTHS, USDLIBOR3M_ID, SCHEME);
    node.accept(EMPTY_CONVENTIONS);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullRateFutureConvention() {
    final RateFutureNode node = new RateFutureNode(2, Tenor.ONE_DAY, Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, RATE_FUTURE_3M_ID, SCHEME);
    node.accept(EMPTY_CONVENTIONS);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullRateFutureUnderlyingConvention() {
    final Map<ExternalId, Convention> map = new HashMap<>();
    map.put(RATE_FUTURE_3M_ID, RATE_FUTURE_3M);
    final CurveNodeCurrencyVisitor visitor = new CurveNodeCurrencyVisitor(new TestConventionSource(map), SECURITY_SOURCE);
    final RateFutureNode node = new RateFutureNode(2, Tenor.ONE_DAY, Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, RATE_FUTURE_3M_ID, SCHEME);
    node.accept(visitor);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullSwapPayConvention() {
    final SwapNode node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, FIXED_LEG_ID, SWAP_3M_IBOR_ID, SCHEME);
    node.accept(EMPTY_CONVENTIONS);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullSwapReceiveConvention() {
    final Map<ExternalId, Convention> map = new HashMap<>();
    map.put(FIXED_LEG_ID, FIXED_LEG);
    final CurveNodeCurrencyVisitor visitor = new CurveNodeCurrencyVisitor(new TestConventionSource(map), SECURITY_SOURCE);
    final SwapNode node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, FIXED_LEG_ID, SWAP_3M_IBOR_ID, SCHEME);
    node.accept(visitor);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullIborUnderlyingConvention() {
    final Map<ExternalId, Convention> map = new HashMap<>();
    map.put(FIXED_LEG_ID, FIXED_LEG);
    map.put(SWAP_3M_IBOR_ID, SWAP_3M_LIBOR);
    final CurveNodeCurrencyVisitor visitor = new CurveNodeCurrencyVisitor(new TestConventionSource(map), SECURITY_SOURCE);
    final SwapNode node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, FIXED_LEG_ID, SWAP_3M_IBOR_ID, SCHEME);
    node.accept(visitor);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullCMSIndexUnderlyingConvention() {
    final Map<ExternalId, Convention> map = new HashMap<>();
    map.put(FIXED_LEG_ID, FIXED_LEG);
    map.put(SWAP_INDEX_ID, SWAP_INDEX);
    final CurveNodeCurrencyVisitor visitor = new CurveNodeCurrencyVisitor(new TestConventionSource(map), SECURITY_SOURCE);
    final SwapNode node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, FIXED_LEG_ID, SWAP_INDEX_ID, SCHEME);
    node.accept(visitor);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullCMSUnderlyingConvention() {
    final Map<ExternalId, Convention> map = new HashMap<>();
    map.put(FIXED_LEG_ID, FIXED_LEG);
    map.put(CMS_SWAP_ID, CMS);
    final CurveNodeCurrencyVisitor visitor = new CurveNodeCurrencyVisitor(new TestConventionSource(map), SECURITY_SOURCE);
    final SwapNode node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, FIXED_LEG_ID, CMS_SWAP_ID, SCHEME);
    node.accept(visitor);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullOISUnderlyingConvention() {
    final Map<ExternalId, Convention> map = new HashMap<>();
    map.put(FIXED_LEG_ID, FIXED_LEG);
    map.put(OIS_ID, OIS);
    final CurveNodeCurrencyVisitor visitor = new CurveNodeCurrencyVisitor(new TestConventionSource(map), SECURITY_SOURCE);
    final SwapNode node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, FIXED_LEG_ID, OIS_ID, SCHEME);
    node.accept(visitor);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullFixedLegConvention() {
    final Map<ExternalId, Convention> map = new HashMap<>();
    map.put(OIS_ID, OIS);
    final CurveNodeCurrencyVisitor visitor = new CurveNodeCurrencyVisitor(new TestConventionSource(map), SECURITY_SOURCE);
    final SwapNode node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, FIXED_LEG_ID, OIS_ID, SCHEME);
    node.accept(visitor);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullCompoundingIborLegConvention() {
    final Map<ExternalId, Convention> map = new HashMap<>();
    final CompoundingIborLegConvention compoundingIbor = new CompoundingIborLegConvention("USD Compounding Libor", ExternalId.of(SCHEME, "USD Compounding Libor").toBundle(),
        USDLIBOR3M_ID, Tenor.THREE_MONTHS, CompoundingType.COMPOUNDING, Tenor.ONE_MONTH, StubType.SHORT_START, 2, false, StubType.LONG_START, true, 1);
    map.put(FIXED_LEG_ID, FIXED_LEG);
    map.put(COMPOUNDING_IBOR_ID, compoundingIbor);
    final CurveNodeCurrencyVisitor visitor = new CurveNodeCurrencyVisitor(new TestConventionSource(map), SECURITY_SOURCE);
    final SwapNode node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, FIXED_LEG_ID, COMPOUNDING_IBOR_ID, SCHEME);
    node.accept(visitor);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongUnderlyingCompoundingIborLegConvention() {
    final Map<ExternalId, Convention> map = new HashMap<>();
    map.put(FIXED_LEG_ID, FIXED_LEG);
    map.put(COMPOUNDING_IBOR_ID, COMPOUNDING_IBOR);
    final CurveNodeCurrencyVisitor visitor = new CurveNodeCurrencyVisitor(new TestConventionSource(map), SECURITY_SOURCE);
    final SwapNode node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, FIXED_LEG_ID, COMPOUNDING_IBOR_ID, SCHEME);
    node.accept(visitor);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullZeroCouponInflationConvention() {
    final ZeroCouponInflationNode node = new ZeroCouponInflationNode(Tenor.EIGHT_MONTHS, ZERO_COUPON_INFLATION_ID, FIXED_LEG_ID, InflationNodeType.INTERPOLATED, "TEST");
    node.accept(EMPTY_CONVENTIONS);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullPriceIndexConvention() {
    final Map<ExternalId, Convention> map = new HashMap<>();
    map.put(ZERO_COUPON_INFLATION_ID, INFLATION_LEG);
    final CurveNodeCurrencyVisitor visitor = new CurveNodeCurrencyVisitor(new TestConventionSource(map), SECURITY_SOURCE);
    final ZeroCouponInflationNode node = new ZeroCouponInflationNode(Tenor.EIGHT_MONTHS, ZERO_COUPON_INFLATION_ID, FIXED_LEG_ID, InflationNodeType.MONTHLY, "TEST");
    node.accept(visitor);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongTypeZeroCouponInflationConvention() {
    final ZeroCouponInflationNode node = new ZeroCouponInflationNode(Tenor.EIGHT_MONTHS, SWAP_3M_IBOR_ID, FIXED_LEG_ID, InflationNodeType.INTERPOLATED, "TEST");
    node.accept(VISITOR);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullIMMFRAConvention() {
    final Map<ExternalId, Convention> map = new HashMap<>();
    map.put(USDLIBOR_CONVENTION_ID, USDLIBOR_CONVENTION);
    final CurveNodeCurrencyVisitor visitor = new CurveNodeCurrencyVisitor(new TestConventionSource(map), SECURITY_SOURCE);
    final RollDateFRANode node = new RollDateFRANode(Tenor.ONE_DAY, Tenor.THREE_MONTHS, 4, 40, IMM_FRA_ID, "Test");
    node.accept(visitor);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongTypeIMMFRAConvention() {
    final Map<ExternalId, Convention> map = new HashMap<>();
    map.put(IMM_FRA_ID, FIXED_LEG);
    final CurveNodeCurrencyVisitor visitor = new CurveNodeCurrencyVisitor(new TestConventionSource(map), SECURITY_SOURCE);
    final RollDateSwapNode node = new RollDateSwapNode(Tenor.ONE_DAY, 4, 40, IMM_FRA_ID, "Test");
    node.accept(visitor);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullIMMSwapConvention() {
    final Map<ExternalId, Convention> map = new HashMap<>();
    map.put(FIXED_LEG_ID, FIXED_LEG);
    map.put(SWAP_3M_IBOR_ID, SWAP_3M_LIBOR);
    final CurveNodeCurrencyVisitor visitor = new CurveNodeCurrencyVisitor(new TestConventionSource(map), SECURITY_SOURCE);
    final RollDateSwapNode node = new RollDateSwapNode(Tenor.ONE_DAY, 4, 40, IMM_SWAP_ID, "Test");
    node.accept(visitor);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongTypeIMMSwapConvention() {
    final Map<ExternalId, Convention> map = new HashMap<>();
    map.put(IMM_SWAP_ID, FIXED_LEG);
    final CurveNodeCurrencyVisitor visitor = new CurveNodeCurrencyVisitor(new TestConventionSource(map), SECURITY_SOURCE);
    final RollDateSwapNode node = new RollDateSwapNode(Tenor.ONE_DAY, 4, 40, IMM_SWAP_ID, "Test");
    node.accept(visitor);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullPayIMMSwapConvention() {
    final Map<ExternalId, Convention> map = new HashMap<>();
    map.put(IMM_SWAP_ID, IMM_SWAP);
    map.put(SWAP_3M_IBOR_ID, SWAP_3M_LIBOR);
    final CurveNodeCurrencyVisitor visitor = new CurveNodeCurrencyVisitor(new TestConventionSource(map), SECURITY_SOURCE);
    final RollDateSwapNode node = new RollDateSwapNode(Tenor.ONE_DAY, 4, 40, IMM_SWAP_ID, "Test");
    node.accept(visitor);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullReceiveIMMSwapConvention() {
    final Map<ExternalId, Convention> map = new HashMap<>();
    map.put(IMM_SWAP_ID, IMM_SWAP);
    map.put(FIXED_LEG_ID, FIXED_LEG);
    final CurveNodeCurrencyVisitor visitor = new CurveNodeCurrencyVisitor(new TestConventionSource(map), SECURITY_SOURCE);
    final RollDateSwapNode node = new RollDateSwapNode(Tenor.ONE_DAY, 4, 40, IMM_SWAP_ID, "Test");
    node.accept(visitor);
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void testUnhandledConvention() {
    final Convention convention = new MockConvention(UniqueId.of("Convention", "Test"), "Mock", ExternalIdBundle.of("A", "B"), Currency.GBP);
    final Map<ExternalId, Convention> map = new HashMap<>();
    map.put(FIXED_LEG_ID, FIXED_LEG);
    map.put(ExternalId.of("A", "B"), convention);
    final CurveNodeCurrencyVisitor visitor = new CurveNodeCurrencyVisitor(new TestConventionSource(map), SECURITY_SOURCE);
    final SwapNode node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, FIXED_LEG_ID, ExternalId.of("A", "B"), SCHEME);
    node.accept(visitor);
  }

  @Test
  public void testContinuouslyCompoundedRateNode() {
    final ContinuouslyCompoundedRateNode node = new ContinuouslyCompoundedRateNode(SCHEME, Tenor.TWELVE_MONTHS);
    assertEquals(0, node.accept(VISITOR).size());
  }

  @Test
  public void testCreditSpreadNode() {
    final CreditSpreadNode node = new CreditSpreadNode(SCHEME, Tenor.THREE_MONTHS);
    assertEquals(0, node.accept(VISITOR).size());
  }

  @Test
  public void testDiscountFactorNode() {
    final DiscountFactorNode node = new DiscountFactorNode(SCHEME, Tenor.FIVE_YEARS);
    assertEquals(0, node.accept(VISITOR).size());
  }

  @Test
  public void testFRANode() {
    final FRANode node = new FRANode(Tenor.ONE_DAY, Tenor.THREE_MONTHS, USDLIBOR3M_ID, SCHEME);
    final Set<Currency> currencies = node.accept(VISITOR);
    assertEquals(1, currencies.size());
    assertEquals(Currency.USD, currencies.iterator().next());
  }

  @Test
  public void testFXForwardNode() {
    final FXForwardNode node = new FXForwardNode(Tenor.ONE_DAY, Tenor.ONE_YEAR, FX_FORWARD_ID, Currency.EUR, Currency.AUD, SCHEME);
    final Set<Currency> currencies = node.accept(VISITOR);
    assertEquals(2, currencies.size());
    assertEquals(Sets.newHashSet(Currency.EUR, Currency.AUD), currencies);
  }

  @Test
  public void testIMMFRANode() {
    final RollDateFRANode node = new RollDateFRANode(Tenor.ONE_DAY, Tenor.THREE_MONTHS, 4, 40, IMM_FRA_ID, "Test");
    final Set<Currency> currencies = node.accept(VISITOR);
    assertEquals(1, currencies.size());
    assertEquals(Currency.USD, currencies.iterator().next());
  }

  @Test
  public void testIMMSwapNode() {
    final RollDateSwapNode node = new RollDateSwapNode(Tenor.ONE_DAY, 4, 40, IMM_SWAP_ID, "Test");
    final Set<Currency> currencies = node.accept(VISITOR);
    assertEquals(1, currencies.size());
    assertEquals(Currency.USD, currencies.iterator().next());
  }

  @Test
  public void testRateFutureNode() {
    final RateFutureNode node = new RateFutureNode(2, Tenor.ONE_DAY, Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, RATE_FUTURE_3M_ID, SCHEME);
    final Set<Currency> currencies = node.accept(VISITOR);
    assertEquals(1, currencies.size());
    assertEquals(Currency.USD, currencies.iterator().next());
  }

  @Test
  public void testSwapNode() {
    SwapNode node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, FIXED_LEG_ID, SWAP_3M_IBOR_ID, SCHEME);
    Set<Currency> currencies = node.accept(VISITOR);
    assertEquals(1, currencies.size());
    assertEquals(Currency.USD, currencies.iterator().next());
    node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, FIXED_LEG_ID, OIS_ID, SCHEME);
    currencies = node.accept(VISITOR);
    assertEquals(1, currencies.size());
    assertEquals(Currency.USD, currencies.iterator().next());
    node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, FIXED_LEG_ID, CMS_SWAP_ID, SCHEME);
    currencies = node.accept(VISITOR);
    assertEquals(1, currencies.size());
    assertEquals(Currency.USD, currencies.iterator().next());
    node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, FIXED_LEG_ID, COMPOUNDING_IBOR_ID, SCHEME);
    currencies = node.accept(VISITOR);
    assertEquals(1, currencies.size());
    assertEquals(Currency.USD, currencies.iterator().next());
    node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, SWAP_3M_IBOR_ID, SWAP_6M_EURIBOR_ID, SCHEME);
    currencies = node.accept(VISITOR);
    assertEquals(2, currencies.size());
    assertEquals(Sets.newHashSet(Currency.EUR, Currency.USD), currencies);
  }

  @Test
  public void testZeroCouponInflationNode() {
    final ZeroCouponInflationNode node = new ZeroCouponInflationNode(Tenor.EIGHT_YEARS, ZERO_COUPON_INFLATION_ID, FIXED_LEG_ID, InflationNodeType.INTERPOLATED, "TEST");
    final Set<Currency> currencies = node.accept(VISITOR);
    assertEquals(1, currencies.size());
    assertEquals(Currency.USD, currencies.iterator().next());
  }

  /**
   * A simplified local version of a SecuritySource for tests.
   */
  //TODO replace with a proper MockSecuritySource
  protected static class MySecuritySource implements SecuritySource {

    /** Security source as a map for tests **/
    private final Map<ExternalIdBundle, Security> _map;

    /**
     * @param map The map of id/Security
     */
    public MySecuritySource(final Map<ExternalIdBundle, Security> map) {
      super();
      _map = map;
    }

    @Override
    public Collection<Security> get(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
      return null;
    }

    @Override
    public Map<ExternalIdBundle, Collection<Security>> getAll(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
      return null;
    }

    @Override
    public Collection<Security> get(final ExternalIdBundle bundle) {
      return null;
    }

    @Override
    public Security getSingle(final ExternalIdBundle bundle) {
      return _map.get(bundle);
    }

    @Override
    public Security getSingle(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
      return null;
    }

    @Override
    public Map<ExternalIdBundle, Security> getSingle(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
      return null;
    }

    @Override
    public Security get(final UniqueId uniqueId) {
      return null;
    }

    @Override
    public Security get(final ObjectId objectId, final VersionCorrection versionCorrection) {
      return null;
    }

    @Override
    public Map<UniqueId, Security> get(final Collection<UniqueId> uniqueIds) {
      return null;
    }

    @Override
    public Map<ObjectId, Security> get(final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
      return null;
    }

    @Override
    public ChangeManager changeManager() {
      return null;
    }

  }


  /**
   * A simplified local version of a ConfigSource for tests.
   */
  //TODO replace with a proper MockConfigSource
  protected static class MyConfigSource implements ConfigSource {
    /** Config source as a map for tests **/
    private final Map<String, Object> _map;

    public MyConfigSource(final Map<String, Object> map) {
      _map = map;
    }

    @Override
    public Map<UniqueId, ConfigItem<?>> get(final Collection<UniqueId> uniqueIds) {
      return null;
    }

    @Override
    public Map<ObjectId, ConfigItem<?>> get(final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
      return null;
    }

    @Override
    public ChangeManager changeManager() {
      return null;
    }

    @Override
    public ConfigItem<?> get(final UniqueId uniqueId) {
      return null;
    }

    @Override
    public ConfigItem<?> get(final ObjectId objectId, final VersionCorrection versionCorrection) {
      return null;
    }

    @Override
    public <R> Collection<ConfigItem<R>> get(final Class<R> clazz, final String configName, final VersionCorrection versionCorrection) {
      return null;
    }

    @Override
    public <R> Collection<ConfigItem<R>> getAll(final Class<R> clazz, final VersionCorrection versionCorrection) {
      return null;
    }

    @Override
    public <R> R getConfig(final Class<R> clazz, final UniqueId uniqueId) {
      return null;
    }

    @Override
    public <R> R getConfig(final Class<R> clazz, final ObjectId objectId, final VersionCorrection versionCorrection) {
      return null;
    }

    @Override
    public <R> R getSingle(final Class<R> clazz, final String configName, final VersionCorrection versionCorrection) {
      final Object config = _map.get(configName);
      if (config != null && config.getClass().equals(clazz)) {
        return (R) config;
      }
      return null;
    }

    @Override
    public <R> R getLatestByName(final Class<R> clazz, final String name) {
      return null;
    }
  }
}
