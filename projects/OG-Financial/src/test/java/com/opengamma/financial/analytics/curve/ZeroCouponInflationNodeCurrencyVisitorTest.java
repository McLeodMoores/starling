/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve;

import static com.opengamma.financial.analytics.curve.CurveNodeCurrencyVisitorTest.EMPTY_SECURITY_SOURCE;
import static com.opengamma.financial.analytics.curve.CurveNodeCurrencyVisitorTest.SCHEME;
import static com.opengamma.financial.analytics.curve.CurveNodeCurrencyVisitorTest.US;
import static org.testng.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.InMemoryConventionSource;
import com.opengamma.financial.analytics.curve.CurveNodeCurrencyVisitorTest.MySecuritySource;
import com.opengamma.financial.analytics.ircurve.strips.InflationNodeType;
import com.opengamma.financial.analytics.ircurve.strips.ZeroCouponInflationNode;
import com.opengamma.financial.convention.InflationLegConvention;
import com.opengamma.financial.convention.PriceIndexConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.financial.security.index.PriceIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Tests the retrieval of a currency from zero coupon inflation nodes.
 */
public class ZeroCouponInflationNodeCurrencyVisitorTest {
  /** The curve node id mapper name */
  private static final String CNIM_NAME = "CNIM";
  /** The id of the fixed leg convention */
  private static final ExternalId FIXED_LEG_CONVENTION_ID = ExternalId.of(SCHEME, "EUR Fixed Swap Leg");
  /** The id of the inflation leg convention */
  private static final ExternalId INFLATION_LEG_CONVENTION_ID = ExternalId.of(SCHEME, "USD Zero Coupon Swap Leg");
  /** The id of the underlying price index convention */
  private static final ExternalId PRICE_INDEX_CONVENTION_ID = ExternalId.of(SCHEME, "USD Price Index");
  /** The id of the price index */
  private static final ExternalId PRICE_INDEX_ID = ExternalId.of(SCHEME, "USDCPI");
  /** The fixed leg convention */
  private static final SwapFixedLegConvention FIXED_LEG_CONVENTION = new SwapFixedLegConvention("EUR Fixed Swap Leg",
      FIXED_LEG_CONVENTION_ID.toBundle(), Tenor.ONE_YEAR, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING,
      Currency.EUR, US, 2, false, StubType.NONE, false, 0);
  /** The inflation leg convention */
  private static final InflationLegConvention INFLATION_LEG_CONVENTION = new InflationLegConvention("USD Zero Coupon Swap Leg",
      INFLATION_LEG_CONVENTION_ID.toBundle(), BusinessDayConventions.MODIFIED_FOLLOWING, DayCounts.ACT_360, false, 1, 0, PRICE_INDEX_CONVENTION_ID);
  /** The price index convention */
  private static final PriceIndexConvention PRICE_INDEX_CONVENTION = new PriceIndexConvention("USD Price Index", PRICE_INDEX_CONVENTION_ID.toBundle(),
      Currency.USD, US, PRICE_INDEX_ID);
  /** The price index security */
  private static final PriceIndex PRICE_INDEX = new PriceIndex("USD CPI", PRICE_INDEX_CONVENTION_ID);

  static {
    PRICE_INDEX.addExternalId(PRICE_INDEX_ID);
  }

  /**
   * Tests the behaviour if the inflation leg convention is not available.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testNoInflationLegConvention() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(FIXED_LEG_CONVENTION);
    final ZeroCouponInflationNode node = new ZeroCouponInflationNode(Tenor.TEN_YEARS, INFLATION_LEG_CONVENTION_ID, FIXED_LEG_CONVENTION_ID,
        InflationNodeType.INTERPOLATED, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests the behaviour if the fixed leg convention is not available.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testNoFixedLegConvention() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(INFLATION_LEG_CONVENTION);
    final ZeroCouponInflationNode node = new ZeroCouponInflationNode(Tenor.TEN_YEARS, INFLATION_LEG_CONVENTION_ID, FIXED_LEG_CONVENTION_ID,
        InflationNodeType.INTERPOLATED, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests the behaviour if the underlying convention is not a price index convention.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongUnderlyingConventionType() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    final SwapFixedLegConvention priceIndexConvention = FIXED_LEG_CONVENTION.clone();
    priceIndexConvention.setExternalIdBundle(PRICE_INDEX_CONVENTION_ID.toBundle());
    conventionSource.addConvention(FIXED_LEG_CONVENTION);
    conventionSource.addConvention(INFLATION_LEG_CONVENTION);
    conventionSource.addConvention(priceIndexConvention);
    final ZeroCouponInflationNode node = new ZeroCouponInflationNode(Tenor.TEN_YEARS, INFLATION_LEG_CONVENTION_ID, FIXED_LEG_CONVENTION_ID,
        InflationNodeType.INTERPOLATED, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests the behaviour if the underlying security is not available from the source.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoUnderlyingSecurity() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(FIXED_LEG_CONVENTION);
    conventionSource.addConvention(INFLATION_LEG_CONVENTION);
    final ZeroCouponInflationNode node = new ZeroCouponInflationNode(Tenor.TEN_YEARS, INFLATION_LEG_CONVENTION_ID, FIXED_LEG_CONVENTION_ID,
        InflationNodeType.INTERPOLATED, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests the behaviour if the underlying security is not a price index security.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongUnderlyingSecurityType() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    // note that the convention references the price index security id
    final InflationLegConvention inflationLegConvention = new InflationLegConvention("USD Zero Coupon Swap Leg",
        INFLATION_LEG_CONVENTION_ID.toBundle(), BusinessDayConventions.MODIFIED_FOLLOWING, DayCounts.ACT_360, false, 1, 0, PRICE_INDEX_ID);
    conventionSource.addConvention(FIXED_LEG_CONVENTION);
    conventionSource.addConvention(inflationLegConvention);
    final OvernightIndex overnightIndex = new OvernightIndex("USD Overnight", PRICE_INDEX_ID);
    final Map<ExternalIdBundle, Security> securities = new HashMap<>();
    securities.put(PRICE_INDEX_ID.toBundle(), overnightIndex);
    final SecuritySource securitySource = new MySecuritySource(securities);
    final ZeroCouponInflationNode node = new ZeroCouponInflationNode(Tenor.TEN_YEARS, INFLATION_LEG_CONVENTION_ID, FIXED_LEG_CONVENTION_ID,
        InflationNodeType.INTERPOLATED, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, securitySource));
  }

  /**
   * Tests the behaviour if the underlying convention from the security is not available.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoUnderlyingConventionFromSecurity() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    // note that the convention references the price index security id
    final InflationLegConvention inflationLegConvention = new InflationLegConvention("USD Zero Coupon Swap Leg",
        INFLATION_LEG_CONVENTION_ID.toBundle(), BusinessDayConventions.MODIFIED_FOLLOWING, DayCounts.ACT_360, false, 1, 0, PRICE_INDEX_ID);
    conventionSource.addConvention(FIXED_LEG_CONVENTION);
    conventionSource.addConvention(inflationLegConvention);
    final Map<ExternalIdBundle, Security> securities = new HashMap<>();
    securities.put(PRICE_INDEX_ID.toBundle(), PRICE_INDEX);
    final SecuritySource securitySource = new MySecuritySource(securities);
    final ZeroCouponInflationNode node = new ZeroCouponInflationNode(Tenor.TEN_YEARS, INFLATION_LEG_CONVENTION_ID, FIXED_LEG_CONVENTION_ID,
        InflationNodeType.INTERPOLATED, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, securitySource));
  }

  /**
   * Tests the behaviour if the underlying convention from the security is available.
   */
  @Test
  public void testUnderlyingConventionFromSecurity() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    // note that the convention references the price index security id
    final InflationLegConvention inflationLegConvention = new InflationLegConvention("USD Zero Coupon Swap Leg",
        INFLATION_LEG_CONVENTION_ID.toBundle(), BusinessDayConventions.MODIFIED_FOLLOWING, DayCounts.ACT_360, false, 1, 0, PRICE_INDEX_ID);
    conventionSource.addConvention(FIXED_LEG_CONVENTION);
    conventionSource.addConvention(inflationLegConvention);
    conventionSource.addConvention(PRICE_INDEX_CONVENTION);
    final Map<ExternalIdBundle, Security> securities = new HashMap<>();
    securities.put(PRICE_INDEX_ID.toBundle(), PRICE_INDEX);
    final SecuritySource securitySource = new MySecuritySource(securities);
    final ZeroCouponInflationNode node = new ZeroCouponInflationNode(Tenor.TEN_YEARS, INFLATION_LEG_CONVENTION_ID, FIXED_LEG_CONVENTION_ID,
        InflationNodeType.INTERPOLATED, CNIM_NAME);
    assertEquals(node.accept(new CurveNodeCurrencyVisitor(conventionSource, securitySource)), Sets.newHashSet(Currency.EUR, Currency.USD));
  }

  /**
   * Tests that all currencies are found if all conventions are available from the source.
   */
  @Test
  public void test() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(FIXED_LEG_CONVENTION);
    conventionSource.addConvention(INFLATION_LEG_CONVENTION);
    conventionSource.addConvention(PRICE_INDEX_CONVENTION);
    final ZeroCouponInflationNode node = new ZeroCouponInflationNode(Tenor.TEN_YEARS, INFLATION_LEG_CONVENTION_ID, FIXED_LEG_CONVENTION_ID,
        InflationNodeType.INTERPOLATED, CNIM_NAME);
    assertEquals(node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE)), Sets.newHashSet(Currency.EUR, Currency.USD));
  }
}
