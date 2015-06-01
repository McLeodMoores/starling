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
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Tests the retrieval of a currency from FRA nodes.
 */
public class FraNodeCurrencyVisitorTest {
  /** The curve node id mapper name */
  private static final String CNIM_NAME = "CNIM";
  /** The id of the underlying LIBOR convention */
  private static final ExternalId LIBOR_CONVENTION_ID = ExternalId.of(SCHEME, "USD LIBOR");
  /** The id of the underlying LIBOR security */
  private static final ExternalId LIBOR_SECURITY_ID = ExternalId.of(SCHEME, "USDLIBOR3M");
  /** The LIBOR convention */
  private static final IborIndexConvention LIBOR_CONVENTION = new IborIndexConvention("USD LIBOR", LIBOR_CONVENTION_ID.toBundle(),
      DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, Currency.USD, LocalTime.of(11, 0), "", US, US, "");
  /** The LIBOR security */
  private static final IborIndex LIBOR_SECURITY = new IborIndex("USD 3M LIBOR", Tenor.THREE_MONTHS, LIBOR_CONVENTION_ID);
  static {
    LIBOR_SECURITY.addExternalId(LIBOR_SECURITY_ID);
  }

  /**
   * Tests the behaviour when neither the ibor index convention or security are available from the sources.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoConventionOrSecurity() {
    final FRANode node = new FRANode(Tenor.THREE_MONTHS, Tenor.SIX_MONTHS, LIBOR_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(EMPTY_CONVENTION_SOURCE, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests the behaviour when the underlying security is not an ibor index.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongUnderlyingSecurity() {
    final OvernightIndex overnightSecurity = new OvernightIndex("USD Overnight", LIBOR_CONVENTION_ID);
    overnightSecurity.addExternalId(LIBOR_SECURITY_ID);
    final Map<ExternalIdBundle, Security> securities = new HashMap<>();
    securities.put(LIBOR_SECURITY_ID.toBundle(), overnightSecurity);
    final SecuritySource securitySource = new MySecuritySource(securities);
    // note that the convention id is the id of the security
    final FRANode node = new FRANode(Tenor.THREE_MONTHS, Tenor.SIX_MONTHS, LIBOR_SECURITY_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(EMPTY_CONVENTION_SOURCE, securitySource));
  }

  /**
   * Tests the behaviour when the ibor index security is available from the source but the convention referenced in
   * the security is not.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testNoConventionFromSecurity() {
    final Map<ExternalIdBundle, Security> securities = new HashMap<>();
    securities.put(LIBOR_SECURITY_ID.toBundle(), LIBOR_SECURITY);
    final SecuritySource securitySource = new MySecuritySource(securities);
    // note that the convention id is the id of the security
    final FRANode node = new FRANode(Tenor.THREE_MONTHS, Tenor.SIX_MONTHS, LIBOR_SECURITY_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(EMPTY_CONVENTION_SOURCE, securitySource));
  }

  /**
   * Tests the behaviour when the underlying convention is not available but the ibor index security and its
   * referenced convention is.
   */
  @Test
  public void testConventionFromSecurity() {
    final Map<ExternalId, Convention> conventions = new HashMap<>();
    conventions.put(LIBOR_CONVENTION_ID, LIBOR_CONVENTION);
    final ConventionSource conventionSource = new TestConventionSource(conventions);
    final Map<ExternalIdBundle, Security> securities = new HashMap<>();
    securities.put(LIBOR_SECURITY_ID.toBundle(), LIBOR_SECURITY);
    final SecuritySource securitySource = new MySecuritySource(securities);
    // note that the convention id is the id of the security
    final FRANode node = new FRANode(Tenor.THREE_MONTHS, Tenor.SIX_MONTHS, LIBOR_SECURITY_ID, CNIM_NAME);
    assertEquals(node.accept(new CurveNodeCurrencyVisitor(conventionSource, securitySource)), Collections.singleton(Currency.USD));
  }

  /**
   * Tests the behaviour when the convention is available.
   */
  @Test
  public void testFromConvention() {
    final Map<ExternalId, Convention> conventions = new HashMap<>();
    conventions.put(LIBOR_CONVENTION_ID, LIBOR_CONVENTION);
    final ConventionSource conventionSource = new TestConventionSource(conventions);
    final FRANode node = new FRANode(Tenor.THREE_MONTHS, Tenor.SIX_MONTHS, LIBOR_CONVENTION_ID, CNIM_NAME);
    // don't need security to be in source
    assertEquals(node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE)), Collections.singleton(Currency.USD));
  }
}
