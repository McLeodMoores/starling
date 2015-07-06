/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve;

import static com.opengamma.financial.analytics.curve.CurveNodeCurrencyVisitorTest.SCHEME;
import static org.testng.Assert.assertEquals;

import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.InMemoryConventionSource;
import com.opengamma.engine.InMemorySecuritySource;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.financial.security.index.PriceIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Tests the retrieval of a currency from cash nodes.
 */
public class CashNodeCurrencyVisitorTest {
  /** US region. */
  private static final ExternalId US = ExternalSchemes.financialRegionId("US");
  /** An empty security source. */
  private static final SecuritySource EMPTY_SECURITY_SOURCE = new InMemorySecuritySource();
  /** An empty convention source. */
  private static final ConventionSource EMPTY_CONVENTION_SOURCE = new InMemoryConventionSource();
  /** The curve node id mapper name */
  private static final String CNIM_NAME = "CNIM";
  /** The id for a 1M USD deposit convention */
  private static final ExternalId DEPOSIT_1M_ID = ExternalId.of(SCHEME, "USD 1m Deposit");
  /** A 1M USD deposit convention */
  private static final DepositConvention DEPOSIT_1M = new DepositConvention("USD 1m Deposit", DEPOSIT_1M_ID.toBundle(),
      DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, Currency.USD, US);
  /** The name of the USD LIBOR convention */
  private static final String LIBOR_CONVENTION_NAME = "ICE LIBOR USD";
  /** The id for a USD LIBOR index convention */
  private static final ExternalId LIBOR_CONVENTION_ID = ExternalId.of(SCHEME, LIBOR_CONVENTION_NAME);
  /** A USD LIBOR index convention */
  private static final IborIndexConvention LIBOR_CONVENTION = new IborIndexConvention(LIBOR_CONVENTION_NAME, LIBOR_CONVENTION_ID.toBundle(),
      DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, Currency.USD, LocalTime.of(11, 0), "US", US, US, "Page");
  /** The name of a 1M USD LIBOR index security */
  private static final String LIBOR_SECURITY_NAME = "USDLIBOR1M";
  /** A 1M USD LIBOR index security */
  private static final IborIndex LIBOR_SECURITY = new IborIndex(LIBOR_SECURITY_NAME, "ICE LIBOR 1M - USD", Tenor.ONE_MONTH, LIBOR_CONVENTION_ID);
  /** A 1M USD LIBOR ticker */
  private static final ExternalId LIBOR_SECURITY_ID = ExternalSchemes.bloombergTickerSecurityId("US0001M Index");
  static {
    LIBOR_SECURITY.setExternalIdBundle(LIBOR_SECURITY_ID.toBundle());
  }
  /** The overnight convention name */
  private static final String OVERNIGHT_CONVENTION_NAME = "USD Overnight";
  /** The id for a USD overnight index convention */
  private static final ExternalId OVERNIGHT_CONVENTION_ID = ExternalId.of(SCHEME, OVERNIGHT_CONVENTION_NAME);
  /** An overnight index convention */
  private static final OvernightIndexConvention OVERNIGHT_CONVENTION = new OvernightIndexConvention(OVERNIGHT_CONVENTION_NAME,
      OVERNIGHT_CONVENTION_ID.toBundle(), DayCounts.ACT_360, 2, Currency.USD, US);
  /** The name of the USD overnight index */
  private static final String OVERNIGHT_SECURITY_NAME = "Fed Funds Effective Rate";
  /** A USD overnight index security */
  private static final OvernightIndex OVERNIGHT_SECURITY = new OvernightIndex(OVERNIGHT_SECURITY_NAME, OVERNIGHT_CONVENTION_ID);
  /** The id for the USD overnight rate */
  private static final ExternalId OVERNIGHT_SECURITY_ID = ExternalSchemes.bloombergTickerSecurityId("FEDL1 Index");
  static {
    OVERNIGHT_SECURITY.setExternalIdBundle(OVERNIGHT_SECURITY_ID.toBundle());
  }

  /**
   * Tests the behaviour when there is no convention or security available from the sources.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoConventionOrSecurity() {
    final CashNode node = new CashNode(Tenor.ONE_DAY, Tenor.ONE_WEEK, DEPOSIT_1M_ID, CNIM_NAME);
    node.accept(new CurveNodeCurrencyVisitor(EMPTY_CONVENTION_SOURCE, EMPTY_SECURITY_SOURCE));
  }

  /**
   * Tests the behaviour when the underlying index security is not an ibor or overnight security.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongSecurityType() {
    final ExternalId priceIndexId = ExternalId.of(SCHEME, "USD CPI");
    final PriceIndex priceIndex = new PriceIndex("USD CPI", priceIndexId);
    priceIndex.setExternalIdBundle(priceIndexId.toBundle());
    final CashNode node = new CashNode(Tenor.ONE_DAY, Tenor.ONE_WEEK, priceIndexId, CNIM_NAME);
    final InMemorySecuritySource securitySource = new InMemorySecuritySource();
    securitySource.addSecurity(priceIndex);
    node.accept(new CurveNodeCurrencyVisitor(EMPTY_CONVENTION_SOURCE, securitySource));
  }

  /**
   * Tests that the convention referenced in the ibor index security is used if the convention in the node
   * is not present in the convention source and that the correct currency is returned.
   */
  @Test
  public void testLiborFromSecurityAndConvention() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(LIBOR_CONVENTION.clone());
    final InMemorySecuritySource securitySource = new InMemorySecuritySource();
    securitySource.addSecurity(LIBOR_SECURITY.clone());
    final CashNode node = new CashNode(Tenor.of(Period.ZERO), Tenor.ONE_MONTH, LIBOR_SECURITY_ID, CNIM_NAME);
    final Set<Currency> currencies = node.accept(new CurveNodeCurrencyVisitor(conventionSource, securitySource));
    assertEquals(currencies.size(), 1);
    assertEquals(currencies.iterator().next(), Currency.USD);
  }

  /**
   * Tests that the correct currency is returned if the ibor index convention is present in the convention source
   * and that the security is not required.
   */
  @Test
  public void testLiborFromConventionOnly() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(LIBOR_CONVENTION.clone());
    final CashNode node = new CashNode(Tenor.of(Period.ZERO), Tenor.ONE_MONTH, LIBOR_CONVENTION_ID, CNIM_NAME);
    final Set<Currency> currencies = node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE));
    assertEquals(currencies.size(), 1);
    assertEquals(currencies.iterator().next(), Currency.USD);
  }

  /**
   * Tests that the convention referenced in the overnight index security is used if the convention in the node
   * is not present in the convention source and that the correct currency is returned.
   */
  @Test
  public void testOvernightFromSecurityAndConvention() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(OVERNIGHT_CONVENTION.clone());
    final InMemorySecuritySource securitySource = new InMemorySecuritySource();
    securitySource.addSecurity(OVERNIGHT_SECURITY.clone());
    final CashNode node = new CashNode(Tenor.of(Period.ZERO), Tenor.ON, OVERNIGHT_SECURITY_ID, CNIM_NAME);
    final Set<Currency> currencies = node.accept(new CurveNodeCurrencyVisitor(conventionSource, securitySource));
    assertEquals(currencies.size(), 1);
    assertEquals(currencies.iterator().next(), Currency.USD);
  }

  /**
   * Tests that the correct currency is returned if the overnight index convention is present in the convention source
   * and that the security is not required.
   */
  @Test
  public void testOvernightFromConventionOnly() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(OVERNIGHT_CONVENTION.clone());
    final CashNode node = new CashNode(Tenor.of(Period.ZERO), Tenor.ON, OVERNIGHT_CONVENTION_ID, CNIM_NAME);
    final Set<Currency> currencies = node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE));
    assertEquals(currencies.size(), 1);
    assertEquals(currencies.iterator().next(), Currency.USD);
  }

  /**
   * Tests that the deposit convention is retrieved and that the correct currency is returned.
   */
  @Test
  public void testDeposit() {
    final InMemoryConventionSource conventionSource = new InMemoryConventionSource();
    conventionSource.addConvention(DEPOSIT_1M.clone());
    final CashNode node = new CashNode(Tenor.ONE_DAY, Tenor.ONE_WEEK, DEPOSIT_1M_ID, CNIM_NAME);
    final Set<Currency> currencies = node.accept(new CurveNodeCurrencyVisitor(conventionSource, EMPTY_SECURITY_SOURCE));
    assertEquals(currencies.size(), 1);
    assertEquals(currencies.iterator().next(), Currency.USD);
  }

}
