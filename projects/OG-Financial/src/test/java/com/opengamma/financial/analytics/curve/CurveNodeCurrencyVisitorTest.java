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


import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.InMemoryConventionSource;
import com.opengamma.engine.InMemorySecuritySource;
import com.opengamma.financial.analytics.ircurve.strips.ContinuouslyCompoundedRateNode;
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.DiscountFactorNode;
import com.opengamma.financial.analytics.ircurve.strips.FXForwardNode;
import com.opengamma.financial.analytics.ircurve.strips.PeriodicallyCompoundedRateNode;
import com.opengamma.financial.convention.BondConvention;
import com.opengamma.financial.convention.EquityConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
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
  /** An empty security source. */
  private static final SecuritySource EMPTY_SECURITY_SOURCE = new InMemorySecuritySource();
  /** An empty convention source. */
  private static final ConventionSource EMPTY_CONVENTION_SOURCE = new InMemoryConventionSource();
  /** The id for FX forward conventions */
  private static final ExternalId FX_FORWARD_ID = ExternalId.of(SCHEME, "FX Forward");
  /** Gets the currencies from a curve node */
  private static final CurveNodeCurrencyVisitor VISITOR;

  static {
    VISITOR = new CurveNodeCurrencyVisitor(EMPTY_CONVENTION_SOURCE, EMPTY_SECURITY_SOURCE);
  }

  /**
   * Tests the behaviour when the convention source is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConventionSource() {
    new CurveNodeCurrencyVisitor(null, null);
  }

  /**
   * Tests the behaviour when the security source is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecuritySource() {
    new CurveNodeCurrencyVisitor(EMPTY_CONVENTION_SOURCE, null);
  }

  /**
   * Tests that a continuously compounded rate node does not return a currency.
   */
  @Test
  public void testContinuouslyCompoundedRateNode() {
    final ContinuouslyCompoundedRateNode node = new ContinuouslyCompoundedRateNode(SCHEME, Tenor.TWELVE_MONTHS);
    assertEquals(node.accept(VISITOR), Collections.emptySet());
  }

  /**
   * Tests that a credit spread node does not return a currency.
   */
  @Test
  public void testCreditSpreadNode() {
    final CreditSpreadNode node = new CreditSpreadNode(SCHEME, Tenor.THREE_MONTHS);
    assertEquals(node.accept(VISITOR), Collections.emptySet());
  }

  /**
   * Tests that a discount factor node does not return a currency.
   */
  @Test
  public void testDiscountFactorNode() {
    final DiscountFactorNode node = new DiscountFactorNode(SCHEME, Tenor.FIVE_YEARS);
    assertEquals(node.accept(VISITOR), Collections.emptySet());
  }

  /**
   * Tests that a periodically compounded rate node does not return a currency.
   */
  @Test
  public void testPeriodicallyCompoundedRateNode() {
    final PeriodicallyCompoundedRateNode node = new PeriodicallyCompoundedRateNode(SCHEME, Tenor.TWELVE_MONTHS, 2);
    assertEquals(node.accept(VISITOR), Collections.emptySet());
  }

  /**
   * Tests that both currencies are returned for FX forward nodes.
   */
  @Test
  public void testFXForwardNode() {
    final FXForwardNode node = new FXForwardNode(Tenor.ONE_DAY, Tenor.ONE_YEAR, FX_FORWARD_ID, Currency.EUR, Currency.AUD, SCHEME);
    final Set<Currency> currencies = node.accept(VISITOR);
    assertEquals(currencies, Sets.newHashSet(Currency.EUR, Currency.AUD));
  }

  /**
   * Tests that a bond convention does not return a currency.
   */
  @Test
  public void testBondConvention() {
    final BondConvention convention = new BondConvention("Bond", ExternalIdBundle.of(SCHEME, "Bond"), 7, 2, BusinessDayConventions.FOLLOWING, true, true);
    assertEquals(convention.accept(VISITOR), Collections.emptySet());
  }

  /**
   * Tests that an equity convention does not return a currency.
   */
  @Test
  public void testEquityConvention() {
    final EquityConvention convention = new EquityConvention("Equity", ExternalIdBundle.of(SCHEME, "Equity"), 7);
    assertEquals(convention.accept(VISITOR), Collections.emptySet());
  }
}
