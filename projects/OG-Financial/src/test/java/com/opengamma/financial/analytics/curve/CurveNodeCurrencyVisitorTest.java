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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.InMemoryConventionSource;
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
  protected static final ConventionSource EMPTY_CONVENTION_SOURCE = new InMemoryConventionSource();
  /** An empty config source. */
  protected static final ConfigSource EMPTY_CONFIG_SOURCE = new MyConfigSource(Collections.<String, Object>emptyMap());

  private static final ExternalId FX_FORWARD_ID = ExternalId.of(SCHEME, "FX Forward");
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
