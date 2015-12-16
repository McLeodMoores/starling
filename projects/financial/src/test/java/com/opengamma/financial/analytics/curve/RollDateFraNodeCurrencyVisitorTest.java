/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve;

import static com.opengamma.financial.analytics.curve.CurveNodeCurrencyVisitorTest.SCHEME;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;

import org.testng.annotations.Test;
import org.threeten.bp.LocalTime;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.InMemoryConventionSource;
import com.opengamma.engine.InMemorySecuritySource;
import com.opengamma.financial.analytics.ircurve.strips.RollDateFRANode;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.RollDateFRAConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Tests the retrieval of a currency from roll date FRA nodes.
 */
@Test(groups = TestGroup.UNIT)
public class RollDateFraNodeCurrencyVisitorTest {
  /** US region. */
  private static final ExternalId US = ExternalSchemes.financialRegionId("US");
  /** An empty security source. */
  private static final SecuritySource EMPTY_SECURITY_SOURCE = new InMemorySecuritySource();
  /** An empty convention source. */
  private static final ConventionSource EMPTY_CONVENTION_SOURCE = new InMemoryConventionSource();
  /** The curve node id mapper name */
  private static final String CNIM_NAME = "CNIM";
  /** The id of the underlying LIBOR convention */
  private static final ExternalId LIBOR_CONVENTION_ID = ExternalId.of(SCHEME, "USD LIBOR");
  /** The id of the FRA convention */
  private static final ExternalId FRA_CONVENTION_ID = ExternalId.of(SCHEME, "USD IMM FRA");
  /** The id of the underlying LIBOR security */
  private static final ExternalId LIBOR_SECURITY_ID = ExternalId.of(SCHEME, "USDLIBOR3M");
  /** The LIBOR convention */
  private static final IborIndexConvention LIBOR_CONVENTION = new IborIndexConvention("USD LIBOR", LIBOR_CONVENTION_ID.toBundle(),
      DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, Currency.USD, LocalTime.of(11, 0), "", US, US, "");
  /** The FRA convention */
  private static final RollDateFRAConvention FRA_CONVENTION = new RollDateFRAConvention("USD IMM FRA", FRA_CONVENTION_ID.toBundle(),
      LIBOR_CONVENTION_ID, ExternalId.of(SCHEME, "Roll date"));
  /** The LIBOR security */
  private static final IborIndex LIBOR_SECURITY = new IborIndex("USD 3M LIBOR", Tenor.THREE_MONTHS, LIBOR_CONVENTION_ID);
  static {
    LIBOR_SECURITY.addExternalId(LIBOR_SECURITY_ID);
  }

  /**
   * Tests the behaviour when the FRA convention is not available.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoFraConvention() {
    final RollDateFRANode node = new RollDateFRANode(Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, 1, 2, FRA_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(EMPTY_CONVENTION_SOURCE, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests the behaviour when neither the underlying convention nor the index security is available.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoIndexConventionOrSecurity() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(FRA_CONVENTION.clone());
    final RollDateFRANode node = new RollDateFRANode(Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, 1, 2, FRA_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests the behaviour when the convention is available but is not a roll date FRA convention.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongConventionType() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    final IborIndexConvention liborConvention = LIBOR_CONVENTION.clone();
    liborConvention.setExternalIdBundle(FRA_CONVENTION_ID.toBundle());
    conventionSource.addConvention(liborConvention);
    final RollDateFRANode node = new RollDateFRANode(Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, 1, 2, FRA_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests the behaviour when the underlying security is not an ibor index.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongUnderlyingSecurityType() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    final RollDateFRAConvention fraConvention = new RollDateFRAConvention("USD IMM FRA", FRA_CONVENTION_ID.toBundle(),
        LIBOR_SECURITY_ID, ExternalId.of(SCHEME, "Roll date"));
    conventionSource.addConvention(fraConvention);
    final OvernightIndex overnightSecurity = new OvernightIndex("USD Overnight", LIBOR_CONVENTION_ID);
    overnightSecurity.addExternalId(LIBOR_SECURITY_ID);
    final InMemorySecuritySource securitySource = new InMemorySecuritySource();
    securitySource.addSecurity(overnightSecurity);
    // note that the convention id is the id of the security
    final RollDateFRANode node = new RollDateFRANode(Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, 1, 2, FRA_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, securitySource));
  }

  /**
   * Tests the behaviour when the ibor index security is available from the source but the convention referenced in
   * the security is not.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testNoConventionFromSecurity() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    // note that the underlying convention id is the ibor index security id
    final RollDateFRAConvention fraConvention = new RollDateFRAConvention("USD IMM FRA", FRA_CONVENTION_ID.toBundle(),
        LIBOR_SECURITY_ID, ExternalId.of(SCHEME, "Roll date"));
    conventionSource.addConvention(fraConvention);
    final InMemorySecuritySource securitySource = new InMemorySecuritySource();
    securitySource.addSecurity(LIBOR_SECURITY.clone());
    final RollDateFRANode node = new RollDateFRANode(Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, 1, 2, FRA_CONVENTION_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(conventionSource, securitySource));
  }

  /**
   * Tests the behaviour when the underlying convention is not available but the ibor index security and its
   * referenced convention is.
   */
  @Test
  public void testConventionFromSecurity() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    // note that the underlying convention id is the ibor index security id
    final RollDateFRAConvention fraConvention = new RollDateFRAConvention("USD IMM FRA", FRA_CONVENTION_ID.toBundle(),
        LIBOR_SECURITY_ID, ExternalId.of(SCHEME, "Roll date"));
    conventionSource.addConvention(fraConvention);
    conventionSource.addConvention(LIBOR_CONVENTION.clone());
    final InMemorySecuritySource securitySource = new InMemorySecuritySource();
    securitySource.addSecurity(LIBOR_SECURITY.clone());
    // note that the convention id is the id of the security
    final RollDateFRANode node = new RollDateFRANode(Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, 1, 2, FRA_CONVENTION_ID, CNIM_NAME);
    assertEquals(node.accept(new CurveNodeCurrencyVisitor(conventionSource, securitySource)), Collections.singleton(Currency.USD));
  }

  /**
   * Tests the behaviour when the underlying convention is available.
   */
  @Test
  public void testFromConvention() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(FRA_CONVENTION.clone());
    conventionSource.addConvention(LIBOR_CONVENTION.clone());
    final RollDateFRANode node = new RollDateFRANode(Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, 1, 2, FRA_CONVENTION_ID, CNIM_NAME);
    // don't need security to be in source
    assertEquals(node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE)), Collections.singleton(Currency.USD));
  }
}
