package com.mcleodmoores.examples.simulated.loader.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityRegion;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.curve.IssuerCurveTypeConfiguration;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.strips.BillNode;
import com.opengamma.financial.analytics.ircurve.strips.BondNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.id.ExternalId;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Creates a curve construction configurations, interpolated curve definition and curve node id mapper
 * for US bonds. The ISINs used for the bill nodes follow the form "USB000000NNN" where "NNN" is equal
 * to the number of months until bill maturity. Bond nodes follow the form "UST000000NNN" where "NNN"
 * is equal to the number of months until bond maturity.
 * <p>
 * The bond curve contains bill nodes from 6 months to 18 months in six month increments and bond nodes
 * from 7 bonds with tenors {2y, 3y, 5y, 7y, 10y, 20y, 30y}, and uses the yield quote to construct the curve.
 */
public class ExamplesUsBondCurveConfigPopulator {
  /** The bond curve construction configuration name */
  private static final String BOND_CURVE_CONSTRUCTION_CONFIG_NAME = "US Government Bond Configuration";
  /** The OIS curve construction configuration name */
  private static final String OIS_CURVE_CONSTRUCTION_CONFIG_NAME = "US Government Bond Configuration (OIS)";
  /** The curve name */
  private static final String CURVE_NAME = "US Government Bond";
  /** The curve node id mapper name */
  private static final String CURVE_NODE_ID_MAPPER_NAME = "US Government Bond ISIN";

  /**
   * Populates a config master with curve configurations, curve definitions
   * and curve node id mappers.
   * @param configMaster The config master, not null
   */
  public static void populateConfigAndConventionMaster(final ConfigMaster configMaster) {
    ArgumentChecker.notNull(configMaster, "configMaster");
    final Collection<CurveConstructionConfiguration> curveConstructionConfigs = makeCurveConstructionConfiguration();
    for (final CurveConstructionConfiguration config : curveConstructionConfigs) {
      ConfigMasterUtils.storeByName(configMaster, makeConfig(config));
    }
    ConfigMasterUtils.storeByName(configMaster, makeConfig(makeCurveNodeIdMapper()));
    ConfigMasterUtils.storeByName(configMaster, makeConfig(makeCurveDefinition()));
  }

  /**
   * Creates two curve construction configuration for US bonds; one consisting of a single government bond curve
   * which matches against USD and uses the two-curve configuration as an exogenous discounting curve, and another
   * that will discount all cash-flows using the OIS curve.
   * @return The configuration
   */
  private static Collection<CurveConstructionConfiguration> makeCurveConstructionConfiguration() {
    final Set<Object> keys = Sets.<Object>newHashSet(Currency.USD);
    final LegalEntityRegion regionFilter = new LegalEntityRegion(false, false, Collections.<Country>emptySet(), true, Collections.singleton(Currency.USD));
    final Set<LegalEntityFilter<LegalEntity>> filters = new HashSet<>();
    filters.add(regionFilter);
    final IssuerCurveTypeConfiguration issuerCurveType = new IssuerCurveTypeConfiguration(keys, filters);
    final Map<String, List<? extends CurveTypeConfiguration>> bondCurveTypes = new HashMap<>();
    bondCurveTypes.put(CURVE_NAME, Arrays.asList(issuerCurveType));
    final CurveGroupConfiguration bondGroup = new CurveGroupConfiguration(0, bondCurveTypes);
    final List<CurveGroupConfiguration> bondGroups = Arrays.asList(bondGroup);
    final Map<String, List<? extends CurveTypeConfiguration>> issuerCurveTypes = new HashMap<>();
    issuerCurveTypes.put("USD Discounting", Arrays.asList(issuerCurveType));
    final CurveGroupConfiguration oisGroup = new CurveGroupConfiguration(0, issuerCurveTypes);
    final List<CurveGroupConfiguration> oisGroups = Arrays.asList(oisGroup);
    final List<String> exogenousConfigs = Collections.singletonList("Default USD Curves");
    final CurveConstructionConfiguration bondCurveConfig = new CurveConstructionConfiguration(BOND_CURVE_CONSTRUCTION_CONFIG_NAME, bondGroups, exogenousConfigs);
    final CurveConstructionConfiguration oisCurveConfig = new CurveConstructionConfiguration(OIS_CURVE_CONSTRUCTION_CONFIG_NAME, oisGroups, exogenousConfigs);
    return Arrays.asList(bondCurveConfig, oisCurveConfig);
  }

  /**
   * Creates an interpolated curve definition containing 3 bills with tenors from 6 months to 18 months
   * in six month intervals and 7 bonds with tenors {2y, 3y, 5y, 7y, 10y, 20y, 30y}.
   * The interpolator is double quadratic with linear extrapolation on both sides.
   * @return The curve definition
   */
  private static CurveDefinition makeCurveDefinition() {
    final Set<CurveNode> curveNodes = new LinkedHashSet<>();
    for (int i = 6; i <= 18; i += 6) {
      curveNodes.add(new BillNode(Tenor.ofMonths(i), CURVE_NODE_ID_MAPPER_NAME));
    }
    for (final int i : new int[] {2, 3, 5, 7, 10, 20, 30 }) {
      curveNodes.add(new BondNode(Tenor.ofYears(i), CURVE_NODE_ID_MAPPER_NAME));
    }
    final CurveDefinition curveDefinition = new InterpolatedCurveDefinition(CURVE_NAME, curveNodes,
        Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    return curveDefinition;
  }

  /**
   * Creates a curve node id mapper containing ISINs for 3 bills with tenors from 6 months to 18 months
   * in six month intervals and 7 bonds with tenors {2y, 3y, 5y, 7y, 10y, 20y, 30y}.
   * @return The curve node id mapper
   */
  private static CurveNodeIdMapper makeCurveNodeIdMapper() {
    final Map<Tenor, CurveInstrumentProvider> billNodes = new HashMap<>();
    final Map<Tenor, CurveInstrumentProvider> bondNodes = new HashMap<>();
    for (int i = 6; i < 24; i += 6) {
      final Tenor tenor = Tenor.ofMonths(i);
      String suffix;
      if (i < 10) {
        suffix = "00" + Integer.toString(i);
      } else {
        suffix = "0" + Integer.toString(i);
      }
      final ExternalId isin = ExternalSchemes.syntheticSecurityId("USB000000" + suffix);
      final CurveInstrumentProvider instrumentProvider = new StaticCurveInstrumentProvider(isin, MarketDataRequirementNames.YIELD_YIELD_TO_MATURITY_MID, DataFieldType.OUTRIGHT);
      billNodes.put(tenor, instrumentProvider);
    }
    for (final int i : new int[] {2, 3, 5, 7, 10, 20, 30 }) {
      final Tenor tenor = Tenor.ofYears(i);
      String suffix;
      if (i < 10) {
        suffix = "00" + Integer.toString(i);
      } else if (i < 100) {
        suffix = "0" + Integer.toString(i);
      } else {
        suffix = Integer.toString(i);
      }
      final ExternalId isin = ExternalSchemes.syntheticSecurityId("UST000000" + suffix);
      final CurveInstrumentProvider instrumentProvider = new StaticCurveInstrumentProvider(isin, MarketDataRequirementNames.YIELD_YIELD_TO_MATURITY_MID, DataFieldType.OUTRIGHT);
      bondNodes.put(tenor, instrumentProvider);
    }
    final CurveNodeIdMapper curveNodeIdMapper = CurveNodeIdMapper.builder()
        .name(CURVE_NODE_ID_MAPPER_NAME)
        .billNodeIds(billNodes)
        .bondNodeIds(bondNodes)
        .build();
    return curveNodeIdMapper;
  }

  /**
   * Creates a config item from a curve construction configuration object.
   * @param curveConfig The curve construction configuration
   * @return The config item
   */
  private static ConfigItem<CurveConstructionConfiguration> makeConfig(final CurveConstructionConfiguration curveConfig) {
    final ConfigItem<CurveConstructionConfiguration> config = ConfigItem.of(curveConfig);
    config.setName(curveConfig.getName());
    return config;
  }

  /**
   * Creates a config item from a curve node id mapper object.
   * @param curveNodeIdMapper The curve node id mapper
   * @return The config item
   */
  private static ConfigItem<CurveNodeIdMapper> makeConfig(final CurveNodeIdMapper curveNodeIdMapper) {
    final ConfigItem<CurveNodeIdMapper> config = ConfigItem.of(curveNodeIdMapper);
    config.setName(curveNodeIdMapper.getName());
    return config;
  }

  /**
   * Creates a config item from a curve definition object.
   * @param curveDefinition The curve definition
   * @return The config item
   */
  private static ConfigItem<CurveDefinition> makeConfig(final CurveDefinition curveDefinition) {
    final ConfigItem<CurveDefinition> config = ConfigItem.of(curveDefinition);
    config.setName(curveDefinition.getName());
    return config;
  }
}
