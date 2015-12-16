/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.financial.curve;

import static org.testng.Assert.assertEquals;

import java.util.Collections;

import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.Period;

import com.mcleodmoores.quandl.QuandlConstants;
import com.mcleodmoores.quandl.convention.ConventionTestInstances;
import com.mcleodmoores.quandl.convention.QuandlFedFundsFutureConvention;
import com.mcleodmoores.quandl.convention.QuandlStirFutureConvention;
import com.mcleodmoores.quandl.financial.curve.QuandlCurveNodeCurrencyVisitor;
import com.mcleodmoores.quandl.testutils.MockConfigSource;
import com.mcleodmoores.quandl.testutils.MockConventionSource;
import com.mcleodmoores.quandl.testutils.MockSecuritySource;
import com.mcleodmoores.quandl.util.Quandl4OpenGammaRuntimeException;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link QuandlCurveNodeCurrencyVisitor}.
 */
public class QuandlCurveNodeCurrencyVisitorTest {
  /** A mock convention source */
  private static final MockConventionSource CONVENTION_SOURCE = new MockConventionSource();
  /** A mock security source */
  private static final MockSecuritySource SECURITY_SOURCE = new MockSecuritySource();
  /** A mock config source */
  private static final MockConfigSource CONFIG_SOURCE = new MockConfigSource();
  /** The visitor */
  private static final QuandlCurveNodeCurrencyVisitor VISITOR;
  /** The name of the mapper in the nodes */
  private static final String MAPPER_NAME = "Mapper";
  /** A Quandl STIR future node */
  private static final RateFutureNode QUANDL_STIR_FUTURE_NODE = new RateFutureNode(1, Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, Tenor.THREE_MONTHS,
      ConventionTestInstances.QUANDL_USD_3M_3M_STIR_FUTURE.getExternalIdBundle().getExternalIds().iterator().next(), MAPPER_NAME);
  /** An OpenGamma STIR future node */
  private static final RateFutureNode OG_STIR_FUTURE_NODE = new RateFutureNode(1, Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, Tenor.THREE_MONTHS,
      ConventionTestInstances.STIR_FUTURE.getExternalIdBundle().getExternalIds().iterator().next(), MAPPER_NAME);
  /** A Quandl Fed funds future node */
  private static final RateFutureNode QUANDL_FED_FUNDS_FUTURE_NODE = new RateFutureNode(1, Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, Tenor.ONE_MONTH,
      ConventionTestInstances.QUANDL_FED_FUNDS_FUTURE.getExternalIdBundle().getExternalIds().iterator().next(), MAPPER_NAME);

  static {
    CONVENTION_SOURCE.addConvention(ConventionTestInstances.QUANDL_USD_3M_3M_STIR_FUTURE.getExternalIdBundle().getExternalIds().iterator().next(),
        ConventionTestInstances.QUANDL_USD_3M_3M_STIR_FUTURE);
    CONVENTION_SOURCE.addConvention(ConventionTestInstances.QUANDL_FED_FUNDS_FUTURE.getExternalIdBundle().getExternalIds().iterator().next(),
        ConventionTestInstances.QUANDL_FED_FUNDS_FUTURE);
    CONVENTION_SOURCE.addConvention(ConventionTestInstances.STIR_FUTURE.getExternalIdBundle().getExternalIds().iterator().next(),
        ConventionTestInstances.STIR_FUTURE);
    CONVENTION_SOURCE.addConvention(ConventionTestInstances.IBOR_INDEX.getExternalIdBundle().getExternalIds().iterator().next(),
        ConventionTestInstances.IBOR_INDEX);
    // the underlying future and index security must be present in the security source to use OpenGamma rate future nodes
    final IborIndex iborIndex = new IborIndex("USD IBOR INDEX", Tenor.THREE_MONTHS,
        ConventionTestInstances.IBOR_INDEX.getExternalIdBundle().getExternalIds().iterator().next());
    // OpenGamma rate future node converter uses the convention id stored in the rate future convention to look up a security
    iborIndex.setExternalIdBundle(ConventionTestInstances.STIR_FUTURE.getIndexConvention().toBundle());
    SECURITY_SOURCE.addSecurity(ConventionTestInstances.STIR_FUTURE.getIndexConvention(), iborIndex);
    final InterestRateFutureSecurity stirFuture = new InterestRateFutureSecurity(new Expiry(DateUtils.getUTCDate(2015, 3, 18)), "ABC", "ABC",
        Currency.USD, 2500, ExternalId.of("DATA_PROVIDER", "USD 3M LIBOR"), "Interest rate");
    stirFuture.setExternalIdBundle(ExternalIdBundle.of("DATA PROVIDER", "EDH5"));
    SECURITY_SOURCE.addSecurity(ExternalId.of("DATA PROVIDER", "EDH5"), stirFuture);
    VISITOR = new QuandlCurveNodeCurrencyVisitor(CONVENTION_SOURCE, SECURITY_SOURCE, CONFIG_SOURCE);
  }

  /**
   * Tests the behaviour when the convention source is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConventionSource1() {
    new QuandlCurveNodeCurrencyVisitor(null, SECURITY_SOURCE);
  }

  /**
   * Tests the behaviour when the security source is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecuritySource1() {
    new QuandlCurveNodeCurrencyVisitor(CONVENTION_SOURCE, null);
  }

  /**
   * Tests the behaviour when the convention source is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConventionSource2() {
    new QuandlCurveNodeCurrencyVisitor(null, SECURITY_SOURCE, CONFIG_SOURCE);
  }

  /**
   * Tests the behaviour when the security source is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecuritySource2() {
    new QuandlCurveNodeCurrencyVisitor(CONVENTION_SOURCE, null, CONFIG_SOURCE);
  }

  /**
   * Tests the behaviour when the config source is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConfigSource() {
    new QuandlCurveNodeCurrencyVisitor(CONVENTION_SOURCE, SECURITY_SOURCE, null);
  }

  /**
   * Tests the behaviour when the convention for a Quandl STIR future is not present in the convention source.
   */
  @Test(expectedExceptions = Quandl4OpenGammaRuntimeException.class)
  public void testNoQuandlStirFutureConvention() {
    final QuandlCurveNodeCurrencyVisitor visitor = new QuandlCurveNodeCurrencyVisitor(new MockConventionSource(), SECURITY_SOURCE, CONFIG_SOURCE);
    QUANDL_STIR_FUTURE_NODE.accept(visitor);
  }

  /**
   * Tests the behaviour when the convention for a Quandl Fed funds future is not present in the convention source.
   */
  @Test(expectedExceptions = Quandl4OpenGammaRuntimeException.class)
  public void testNoQuandlFedFundsFutureConvention() {
    final QuandlCurveNodeCurrencyVisitor visitor = new QuandlCurveNodeCurrencyVisitor(new MockConventionSource(), SECURITY_SOURCE, CONFIG_SOURCE);
    QUANDL_FED_FUNDS_FUTURE_NODE.accept(visitor);
  }

  /**
   * Tests that expected currency is returned for rate future nodes.
   */
  @Test
  public void testRateFutureNodes() {
    assertEquals(QUANDL_STIR_FUTURE_NODE.accept(VISITOR), Collections.singleton(ConventionTestInstances.QUANDL_USD_3M_3M_STIR_FUTURE.getCurrency()));
    assertEquals(QUANDL_FED_FUNDS_FUTURE_NODE.accept(VISITOR), Collections.singleton(ConventionTestInstances.QUANDL_FED_FUNDS_FUTURE.getCurrency()));
    assertEquals(OG_STIR_FUTURE_NODE.accept(VISITOR), Collections.singleton(Currency.USD));
  }

  /**
   * Tests that the expected currency is returned for {@link com.mcleodmoores.quandl.convention.QuandlFutureConvention}s.
   */
  @Test
  public void testQuandlRateFutureConvention() {
    final QuandlFedFundsFutureConvention fedFunds = new QuandlFedFundsFutureConvention("QUANDL FED FUNDS",
        ExternalIdBundle.of("Test", "QUANDL FED FUNDS"), "16:00", "America/Chicago", 2500, QuandlConstants.ofCode("QUANDL_FED_FUNDS"),
        "ABC", "ABC");
    assertEquals(fedFunds.accept(VISITOR), Collections.singleton(Currency.USD));
    final QuandlStirFutureConvention stir = new QuandlStirFutureConvention("GBP 3M/3M STIR",
        ExternalIdBundle.of("Test", "GBP 3M/3M STIR"), Currency.GBP, Tenor.THREE_MONTHS, Tenor.THREE_MONTHS,
        "16:00", "London", 2500, QuandlConstants.ofCode("GBP_3M_LIBOR"), 3, DayOfWeek.MONDAY.name(),
        "ABC", "ABC");
    assertEquals(stir.accept(VISITOR), Collections.singleton(Currency.GBP));
  }
}
