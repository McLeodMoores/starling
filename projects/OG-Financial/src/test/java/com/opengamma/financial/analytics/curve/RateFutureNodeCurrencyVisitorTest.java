/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve;

import static com.opengamma.financial.analytics.curve.CurveNodeCurrencyVisitorTest.EMPTY_CONVENTION_SOURCE;
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
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.curve.CurveNodeCurrencyVisitorTest.MySecuritySource;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.convention.FXSpotConvention;
import com.opengamma.financial.convention.FederalFundsFutureConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InterestRateFutureConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Tests the retrieval of a currency from rate future nodes.
 */
public class RateFutureNodeCurrencyVisitorTest {
  /** The curve node id mapper name */
  private static final String CNIM_NAME = "CNIM";
  /** The id of the underlying LIBOR convention */
  private static final ExternalId LIBOR_CONVENTION_ID = ExternalId.of(SCHEME, "USD LIBOR");
  /** The id of the underlying LIBOR security */
  private static final ExternalId LIBOR_SECURITY_ID = ExternalId.of(SCHEME, "USDLIBOR3M");
  /** The id of the underlying overnight convention */
  private static final ExternalId OVERNIGHT_CONVENTION_ID = ExternalId.of(SCHEME, "USD Overnight");
  /** The id of the underlying overnight security */
  private static final ExternalId OVERNIGHT_SECURITY_ID = ExternalId.of(SCHEME, "USDFEDFUNDS");
  /** The id of the interest rate future convention */
  private static final ExternalId STIR_CONVENTION_ID = ExternalId.of(SCHEME, "USD STIR Future");
  /** The id of the Fed funds future convention */
  private static final ExternalId FED_FUNDS_CONVENTION_ID = ExternalId.of(SCHEME, "USD FED FUNDS Future");
  /** The LIBOR convention */
  private static final IborIndexConvention LIBOR_CONVENTION = new IborIndexConvention("USD LIBOR", LIBOR_CONVENTION_ID.toBundle(),
      DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, Currency.USD, LocalTime.of(11, 0), "", US, US, "");
  /** The overnight convention */
  private static final OvernightIndexConvention OVERNIGHT_CONVENTION = new OvernightIndexConvention("USD Overnight",
      OVERNIGHT_CONVENTION_ID.toBundle(), DayCounts.ACT_360, 0, Currency.USD, US);
  /** The interest rate future convention */
  private static final InterestRateFutureConvention STIR_CONVENTION = new InterestRateFutureConvention("USD STIR Future",
      STIR_CONVENTION_ID.toBundle(), ExternalId.of(SCHEME, "IMM"), US, LIBOR_CONVENTION_ID);
  /** The Fed funds future convention */
  private static final FederalFundsFutureConvention FED_FUNDS_CONVENTION = new FederalFundsFutureConvention("USD FED FUNDS Future",
      FED_FUNDS_CONVENTION_ID.toBundle(), ExternalId.of(SCHEME, "EOM"), US, OVERNIGHT_CONVENTION_ID, 1250000);
  /** The LIBOR security */
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
   * Tests the behaviour when the convention is not available from the source.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testNoConvention() {
    final RateFutureNode node = new RateFutureNode(1, Tenor.ONE_DAY, Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, STIR_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(EMPTY_CONVENTION_SOURCE, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests the behaviour when the convention is not an interest rate or Fed funds future convention.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testUnhandledConventionType() {
    final Map<ExternalId, Convention> conventions = new HashMap<>();
    conventions.put(STIR_CONVENTION_ID, new FXSpotConvention("FX", STIR_CONVENTION_ID.toBundle(), 2, US));
    final ConventionSource conventionSource = new TestConventionSource(conventions);
    final RateFutureNode node = new RateFutureNode(1, Tenor.ONE_DAY, Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, STIR_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests the behaviour when the convention is an interest rate future convention but the underlying
   * convention and security are not available.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoUnderlyingsAvailableForStirConvention() {
    final Map<ExternalId, Convention> conventions = new HashMap<>();
    conventions.put(STIR_CONVENTION_ID, STIR_CONVENTION);
    final ConventionSource conventionSource = new TestConventionSource(conventions);
    final RateFutureNode node = new RateFutureNode(1, Tenor.ONE_DAY, Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, STIR_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests the behaviour when the convention is a Fed funds future convention but the underlying convention
   * and security are not available.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoUnderlyingsAvailableForFedFundsConvention() {
    final Map<ExternalId, Convention> conventions = new HashMap<>();
    conventions.put(FED_FUNDS_CONVENTION_ID, FED_FUNDS_CONVENTION);
    final ConventionSource conventionSource = new TestConventionSource(conventions);
    final RateFutureNode node = new RateFutureNode(1, Tenor.ONE_DAY, Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, FED_FUNDS_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests the behaviour for an interest rate future convention where the underlying convention from the
   * future convention is available.
   */
  @Test
  public void testUnderlyingsConventionAvailableForStirConvention() {
    final Map<ExternalId, Convention> conventions = new HashMap<>();
    conventions.put(STIR_CONVENTION_ID, STIR_CONVENTION);
    conventions.put(LIBOR_CONVENTION_ID, LIBOR_CONVENTION);
    final ConventionSource conventionSource = new TestConventionSource(conventions);
    final RateFutureNode node = new RateFutureNode(1, Tenor.ONE_DAY, Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, STIR_CONVENTION_ID, CNIM_NAME);
    assertEquals(node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE)), Collections.singleton(Currency.USD));
  }

  /**
   * Tests the behaviour for a Fed funds future convention where the underlying convention from the future
   * convention is available.
   */
  @Test
  public void testUnderlyingsConventionAvailableForFedFundsConvention() {
    final Map<ExternalId, Convention> conventions = new HashMap<>();
    conventions.put(FED_FUNDS_CONVENTION_ID, FED_FUNDS_CONVENTION);
    conventions.put(OVERNIGHT_CONVENTION_ID, OVERNIGHT_CONVENTION);
    final ConventionSource conventionSource = new TestConventionSource(conventions);
    final RateFutureNode node = new RateFutureNode(1, Tenor.ONE_DAY, Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, FED_FUNDS_CONVENTION_ID, CNIM_NAME);
    assertEquals(node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE)), Collections.singleton(Currency.USD));
  }

  /**
   * Tests the behaviour for an interest rate future convention where the underlying convention is
   * unavailable and the underlying security is not an ibor index.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongSecurityTypeForStirConvention() {
    final Map<ExternalId, Convention> conventions = new HashMap<>();
    conventions.put(STIR_CONVENTION_ID, STIR_CONVENTION);
    final ConventionSource conventionSource = new TestConventionSource(conventions);
    final Map<ExternalIdBundle, Security> securities = new HashMap<>();
    securities.put(LIBOR_SECURITY_ID.toBundle(), OVERNIGHT_SECURITY);
    final SecuritySource securitySource = new MySecuritySource(securities);
    final RateFutureNode node = new RateFutureNode(1, Tenor.ONE_DAY, Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, STIR_CONVENTION_ID, CNIM_NAME);
    assertEquals(node.accept(new CurveNodeCurrencyVisitor(conventionSource, securitySource)), Collections.singleton(Currency.USD));
  }

  /**
   * Tests the behaviour for a Fed funds future convention where the underlying convention is
   * unavailable and the underlying security is not an overnight index.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongSecurityTypeForFedFundsConvention() {
    final Map<ExternalId, Convention> conventions = new HashMap<>();
    conventions.put(FED_FUNDS_CONVENTION_ID, FED_FUNDS_CONVENTION);
    final ConventionSource conventionSource = new TestConventionSource(conventions);
    final Map<ExternalIdBundle, Security> securities = new HashMap<>();
    securities.put(OVERNIGHT_SECURITY_ID.toBundle(), LIBOR_SECURITY);
    final SecuritySource securitySource = new MySecuritySource(securities);
    final RateFutureNode node = new RateFutureNode(1, Tenor.ONE_DAY, Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, FED_FUNDS_CONVENTION_ID, CNIM_NAME);
    assertEquals(node.accept(new CurveNodeCurrencyVisitor(conventionSource, securitySource)), Collections.singleton(Currency.USD));
  }

  /**
   * Tests the behaviour for an interest rate future convention where the underlying convention is
   * unavailable but the convention referenced in the index security is.
   */
  @Test
  public void testUnderlyingConventionAvailableFromSecurityForStirConvention() {
    // convention uses the LIBOR security id
    final InterestRateFutureConvention stirConvention = new InterestRateFutureConvention("USD STIR Future",
        STIR_CONVENTION_ID.toBundle(), ExternalId.of(SCHEME, "IMM"), US, LIBOR_SECURITY_ID);
    final Map<ExternalId, Convention> conventions = new HashMap<>();
    conventions.put(STIR_CONVENTION_ID, stirConvention);
    conventions.put(LIBOR_CONVENTION_ID, LIBOR_CONVENTION);
    final ConventionSource conventionSource = new TestConventionSource(conventions);
    final Map<ExternalIdBundle, Security> securities = new HashMap<>();
    securities.put(LIBOR_SECURITY_ID.toBundle(), LIBOR_SECURITY);
    final SecuritySource securitySource = new MySecuritySource(securities);
    final RateFutureNode node = new RateFutureNode(1, Tenor.ONE_DAY, Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, STIR_CONVENTION_ID, CNIM_NAME);
    assertEquals(node.accept(new CurveNodeCurrencyVisitor(conventionSource, securitySource)), Collections.singleton(Currency.USD));
  }

  /**
   * Tests the behaviour for a Fed funds future convention where the underlying convention is
   * unavailable but the convention referenced in the index security is.
   */
  @Test
  public void testUnderlyingConventionAvailableFromSecurityForFedFundConvention() {
    // convention uses the overnight security id
    final FederalFundsFutureConvention fedFundsConvention = new FederalFundsFutureConvention("USD FED FUNDS Future",
        FED_FUNDS_CONVENTION_ID.toBundle(), ExternalId.of(SCHEME, "EOM"), US, OVERNIGHT_SECURITY_ID, 1250000);
    final Map<ExternalId, Convention> conventions = new HashMap<>();
    conventions.put(FED_FUNDS_CONVENTION_ID, fedFundsConvention);
    conventions.put(OVERNIGHT_CONVENTION_ID, OVERNIGHT_CONVENTION);
    final ConventionSource conventionSource = new TestConventionSource(conventions);
    final Map<ExternalIdBundle, Security> securities = new HashMap<>();
    securities.put(OVERNIGHT_SECURITY_ID.toBundle(), OVERNIGHT_SECURITY);
    final SecuritySource securitySource = new MySecuritySource(securities);
    final RateFutureNode node = new RateFutureNode(1, Tenor.ONE_DAY, Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, FED_FUNDS_CONVENTION_ID, CNIM_NAME);
    assertEquals(node.accept(new CurveNodeCurrencyVisitor(conventionSource, securitySource)), Collections.singleton(Currency.USD));
  }

  /**
   * Tests the behaviour for an interest rate future convention where the underlying convention is
   * unavailable and the convention from the security is not an ibor index convention.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongConventionTypeFromSecurityForStirConvention() {
    final InterestRateFutureConvention stirConvention = new InterestRateFutureConvention("USD STIR Future",
        STIR_CONVENTION_ID.toBundle(), ExternalId.of(SCHEME, "IMM"), US, LIBOR_SECURITY_ID);
    final Map<ExternalId, Convention> conventions = new HashMap<>();
    conventions.put(STIR_CONVENTION_ID, stirConvention);
    conventions.put(OVERNIGHT_CONVENTION_ID, OVERNIGHT_CONVENTION);
    final ConventionSource conventionSource = new TestConventionSource(conventions);
    final Map<ExternalIdBundle, Security> securities = new HashMap<>();
    final IborIndex liborSecurity = new IborIndex("USD 3M LIBOR", Tenor.THREE_MONTHS, OVERNIGHT_CONVENTION_ID);
    liborSecurity.addExternalId(LIBOR_SECURITY_ID);
    securities.put(LIBOR_SECURITY_ID.toBundle(), liborSecurity);
    final SecuritySource securitySource = new MySecuritySource(securities);
    final RateFutureNode node = new RateFutureNode(1, Tenor.ONE_DAY, Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, STIR_CONVENTION_ID, CNIM_NAME);
    assertEquals(node.accept(new CurveNodeCurrencyVisitor(conventionSource, securitySource)), Collections.singleton(Currency.USD));
  }

  /**
   * Tests the behaviour for a Fed funds future convention where the underlying convention is
   * unavailable and the convention from the security is not an overnight index convention.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongConventionTypeFromSecurityForFedFundsConvention() {
    final FederalFundsFutureConvention fedFundsConvention = new FederalFundsFutureConvention("USD FED FUNDS Future",
        FED_FUNDS_CONVENTION_ID.toBundle(), ExternalId.of(SCHEME, "EOM"), US, OVERNIGHT_SECURITY_ID, 1250000);
    final Map<ExternalId, Convention> conventions = new HashMap<>();
    conventions.put(FED_FUNDS_CONVENTION_ID, fedFundsConvention);
    conventions.put(LIBOR_CONVENTION_ID, LIBOR_CONVENTION);
    final ConventionSource conventionSource = new TestConventionSource(conventions);
    final Map<ExternalIdBundle, Security> securities = new HashMap<>();
    final OvernightIndex overnightSecurity = new OvernightIndex("USD FED FUNDS", LIBOR_CONVENTION_ID);
    overnightSecurity.addExternalId(OVERNIGHT_SECURITY_ID);
    securities.put(OVERNIGHT_SECURITY_ID.toBundle(), overnightSecurity);
    final SecuritySource securitySource = new MySecuritySource(securities);
    final RateFutureNode node = new RateFutureNode(1, Tenor.ONE_DAY, Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, FED_FUNDS_CONVENTION_ID, CNIM_NAME);
    assertEquals(node.accept(new CurveNodeCurrencyVisitor(conventionSource, securitySource)), Collections.singleton(Currency.USD));
  }
}
