/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.mcleodmoores.examples.simulated.loader.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Period;

import com.opengamma.analytics.math.interpolation.factory.DoubleQuadraticInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LinearExtrapolator1dAdapter;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IborCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.curve.OvernightCurveTypeConfiguration;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.id.ExternalId;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Creates curve construction configurations, interpolated curve definitions and
 * curve node id mappers for a two-curve configurations.
 *
 * For all currencies, a two-curve configuration containing a discounting and forward IBOR interpolated curve definition is created:
 * <ul>
 *  <li> The discounting curve contains a two-day deposit node and (1m, 2m, 3m, 4m, 5m, 6m, 9m, 1y, 2y, 3y, 4y, 5y, 10y) OIS nodes.
 *  The curve is called "[CCY] Discounting"
 *  <li> The IBOR curve contains an IBOR node of the appropriate tenor and ((1y, 2y, 3y, 4y, 5y, 6y, 7y, 8y, 9y, 10y, 15y, 20y, 25y, 30y)
 *  fixed / ibor swap nodes. The curve is called "[CCY] [TENOR] Ibor"
 *  <li> The curve construction configuration is called "Default [CCY] Curves"
 * </ul>
 *
 * The interpolator used for all curves is a monotonic convex spline with linear extrapolation on both ends.
 */
public final class ExamplesFixedIncomeCurveConfigsPopulator {
  /** Zero period */
  private static final Tenor ZERO = Tenor.of(Period.ZERO);
  /** The curve node id mapper suffix */
  private static final String NODE_MAPPER_SUFFIX = " Tickers";
  /** A map of (currency string, tenor) pairs to ibor security ids */
  private static final Map<Pair<String, Tenor>, ExternalId> IBOR_SECURITY_FOR_CURRENCY = new HashMap<>();
  /** A map of currency strings to deposit convention ids */
  private static final Map<String, ExternalId> DEPOSIT_CONVENTION_FOR_CURRENCY = new HashMap<>();
  /** A map of currency strings to overnight convention ids */
  private static final Map<String, ExternalId> OVERNIGHT_CONVENTION_FOR_CURRENCY = new HashMap<>();
  /** A map of currency strings to fixed leg convention ids */
  private static final Map<String, ExternalId> FIXED_LEG_CONVENTION_FOR_CURRENCY = new HashMap<>();
  /** A map of (currency string, tenor) pairs to ibor leg convention ids */
  private static final Map<Pair<String, Tenor>, ExternalId> IBOR_LEG_CONVENTION_FOR_CURRENCY = new HashMap<>();
  /** A map of currency strings to OIS leg convention ids */
  private static final Map<String, ExternalId> FIXED_OIS_LEG_CONVENTION_FOR_CURRENCY = new HashMap<>();
  /** A map of currency strings to OIS leg convention ids */
  private static final Map<String, ExternalId> OIS_LEG_CONVENTION_FOR_CURRENCY = new HashMap<>();
  /** A map of currency strings to ibor tenors */
  private static final List<Pair<String, Tenor>> IBOR_TENOR_FOR_CURRENCY = new ArrayList<>();

  static {
    IBOR_SECURITY_FOR_CURRENCY.put(Pairs.of("USD", Tenor.THREE_MONTHS), ExternalSchemes.syntheticSecurityId("USDLIBORP3M"));
    IBOR_SECURITY_FOR_CURRENCY.put(Pairs.of("AUD", Tenor.THREE_MONTHS), ExternalSchemes.syntheticSecurityId("AUDLIBORP3M"));
    IBOR_SECURITY_FOR_CURRENCY.put(Pairs.of("AUD", Tenor.SIX_MONTHS), ExternalSchemes.syntheticSecurityId("AUDLIBORP6M"));

    DEPOSIT_CONVENTION_FOR_CURRENCY.put("USD", ExternalId.of("CONVENTION", "USD Deposit"));
    DEPOSIT_CONVENTION_FOR_CURRENCY.put("AUD", ExternalId.of("CONVENTION", "AUD Deposit"));

    OVERNIGHT_CONVENTION_FOR_CURRENCY.put("USD", ExternalSchemes.syntheticSecurityId("USDFF"));
    OVERNIGHT_CONVENTION_FOR_CURRENCY.put("AUD", ExternalSchemes.syntheticSecurityId("AUDON"));

    FIXED_LEG_CONVENTION_FOR_CURRENCY.put("USD", ExternalId.of("CONVENTION", "USD"));

    IBOR_LEG_CONVENTION_FOR_CURRENCY.put(Pairs.of("USD", Tenor.THREE_MONTHS), ExternalId.of("CONVENTION", "USD 3M"));

    FIXED_OIS_LEG_CONVENTION_FOR_CURRENCY.put("USD", ExternalId.of("CONVENTION", "USD OIS"));
    FIXED_OIS_LEG_CONVENTION_FOR_CURRENCY.put("AUD", ExternalId.of("CONVENTION", "AUD OIS"));

    OIS_LEG_CONVENTION_FOR_CURRENCY.put("USD", ExternalId.of("CONVENTION", "USD OIS"));
    OIS_LEG_CONVENTION_FOR_CURRENCY.put("AUD", ExternalId.of("CONVENTION", "AUD OIS"));

    IBOR_TENOR_FOR_CURRENCY.add(Pairs.of("USD", Tenor.THREE_MONTHS));
  }

  /**
   * Populates a config master with curve configurations, curve definitions and curve node id mappers.
   * @param configMaster  the config master, not null
   */
  public static void populateConfigMaster(final ConfigMaster configMaster) {
    ArgumentChecker.notNull(configMaster, "configMaster");
    for (final Pair<String, Tenor> p : IBOR_TENOR_FOR_CURRENCY) {
      final String currency = p.getFirst();
      final Tenor tenor = p.getSecond();
      makeTwoCurveConfiguration(currency, tenor, configMaster);
      makeDiscountingConfigsForCurrency(currency, configMaster);
      makeIborConfigsForCurrency(currency, tenor, configMaster);
    }
    makeAudThreeCurveConfigurations(configMaster);
  }

  private static void makeTwoCurveConfiguration(final String currency, final Tenor tenor, final ConfigMaster configMaster) {
    final String name = ExampleConfigUtils.generateVanillaFixedIncomeConfigName(currency);
    final String discountingCurveName = ExampleConfigUtils.generateVanillaFixedIncomeDiscountCurveName(currency);
    final String forwardIborCurveName = ExampleConfigUtils.generateVanillaFixedIncomeIborCurveName(currency, tenor);
    final DiscountingCurveTypeConfiguration discountingCurveType = new DiscountingCurveTypeConfiguration(currency);
    final OvernightCurveTypeConfiguration overnightCurveType = new OvernightCurveTypeConfiguration(OVERNIGHT_CONVENTION_FOR_CURRENCY.get(currency));
    final IborCurveTypeConfiguration forwardIborCurveType = new IborCurveTypeConfiguration(IBOR_SECURITY_FOR_CURRENCY.get(Pairs.of(currency, tenor)), tenor);
    final Map<String, List<? extends CurveTypeConfiguration>> curveTypes = new HashMap<>();
    curveTypes.put(discountingCurveName, Arrays.asList(discountingCurveType, overnightCurveType));
    curveTypes.put(forwardIborCurveName, Arrays.asList(forwardIborCurveType));
    final CurveGroupConfiguration group = new CurveGroupConfiguration(0, curveTypes);
    final List<CurveGroupConfiguration> groups = Arrays.asList(group);
    ConfigMasterUtils.storeByName(configMaster, ExampleConfigUtils.makeConfig(new CurveConstructionConfiguration(name, groups, Collections.<String>emptyList())));
  }

  private static void makeDiscountingConfigsForCurrency(final String currency, final ConfigMaster configMaster) {
    final ExternalId oisConvention = OIS_LEG_CONVENTION_FOR_CURRENCY.get(currency);
    final ExternalId fixedOisConvention = FIXED_OIS_LEG_CONVENTION_FOR_CURRENCY.get(currency);
    final String discountingCurveName = ExampleConfigUtils.generateVanillaFixedIncomeDiscountCurveName(currency);
    final String discountingCurveNodeIdMapperName = currency + NODE_MAPPER_SUFFIX;
    final Set<CurveNode> discountingCurveNodes = new LinkedHashSet<>();
    final Map<Tenor, CurveInstrumentProvider> oisNodes = new HashMap<>();
    discountingCurveNodes.add(new CashNode(ZERO, Tenor.TWO_DAYS, DEPOSIT_CONVENTION_FOR_CURRENCY.get(currency),
        discountingCurveNodeIdMapperName));
    final Map<Tenor, CurveInstrumentProvider> depositNodes = new HashMap<>();
    depositNodes.put(Tenor.TWO_DAYS, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId(currency + "CASHP2D"),
        MarketDataRequirementNames.MARKET_VALUE, DataFieldType.OUTRIGHT));
    for (final int i : new int[] {1, 2, 3, 4, 5, 6, 9 }) {
      final Tenor nodeTenor = Tenor.ofMonths(i);
      discountingCurveNodes.add(new SwapNode(ZERO, nodeTenor, fixedOisConvention, oisConvention, discountingCurveNodeIdMapperName));
      oisNodes.put(nodeTenor, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId(currency + "OIS_SWAP" + nodeTenor.toFormattedString())));
    }
    for (final int i : new int[] {1, 2, 3, 4, 5, 10 }) {
      final Tenor nodeTenor = Tenor.ofYears(i);
      discountingCurveNodes.add(new SwapNode(ZERO, Tenor.ofYears(i), fixedOisConvention,
          oisConvention, discountingCurveNodeIdMapperName));
      oisNodes.put(nodeTenor, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId(currency + "OIS_SWAP" + nodeTenor.toFormattedString())));
    }
    final CurveDefinition discountingCurveDefinition = new InterpolatedCurveDefinition(discountingCurveName, discountingCurveNodes,
        DoubleQuadraticInterpolator1dAdapter.NAME, LinearExtrapolator1dAdapter.NAME, LinearExtrapolator1dAdapter.NAME);
    final CurveNodeIdMapper discountingCurveNodeIds = CurveNodeIdMapper.builder()
        .name(discountingCurveNodeIdMapperName)
        .cashNodeIds(depositNodes)
        .swapNodeIds(oisNodes)
        .build();
    ConfigMasterUtils.storeByName(configMaster, ExampleConfigUtils.makeConfig(discountingCurveDefinition));
    ConfigMasterUtils.storeByName(configMaster, ExampleConfigUtils.makeConfig(discountingCurveNodeIds));
  }

  private static void makeIborConfigsForCurrency(final String currency, final Tenor tenor, final ConfigMaster configMaster) {
    final Pair<String, Tenor> currencyTenorPair = Pairs.of(currency, tenor);
    final ExternalId fixedLegConvention = FIXED_LEG_CONVENTION_FOR_CURRENCY.get(currency);
    final ExternalId iborLegConvention = IBOR_LEG_CONVENTION_FOR_CURRENCY.get(currencyTenorPair);
    final String iborCurveName = ExampleConfigUtils.generateVanillaFixedIncomeIborCurveName(currency, tenor);
    final String iborCurveNodeIdMapperName = currency + " " + tenor.toFormattedString().substring(1) + NODE_MAPPER_SUFFIX;
    final Map<Tenor, CurveInstrumentProvider> iborNodes = new HashMap<>();
    final Map<Tenor, CurveInstrumentProvider> swapNodes = new HashMap<>();
    final Set<CurveNode> iborCurveNodes = new LinkedHashSet<>();
    iborCurveNodes.add(new CashNode(ZERO, tenor, IBOR_SECURITY_FOR_CURRENCY.get(currencyTenorPair), iborCurveNodeIdMapperName));
    iborNodes.put(tenor, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId(currency + "LIBOR" + tenor.toFormattedString()),
        MarketDataRequirementNames.MARKET_VALUE, DataFieldType.OUTRIGHT));
    for (final int i : new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30 }) {
      final Tenor nodeTenor = Tenor.ofYears(i);
      iborCurveNodes.add(new SwapNode(ZERO, nodeTenor, fixedLegConvention, iborLegConvention, iborCurveNodeIdMapperName));
      swapNodes.put(nodeTenor, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId(currency + "SWAP" + nodeTenor.toFormattedString())));
    }
    final CurveNodeIdMapper iborCurveNodeIds = CurveNodeIdMapper.builder()
        .name(iborCurveNodeIdMapperName)
        .cashNodeIds(iborNodes)
        .swapNodeIds(swapNodes)
        .build();
    final CurveDefinition iborCurveDefinition = new InterpolatedCurveDefinition(iborCurveName, iborCurveNodes, DoubleQuadraticInterpolator1dAdapter.NAME,
        LinearExtrapolator1dAdapter.NAME, LinearExtrapolator1dAdapter.NAME);
    ConfigMasterUtils.storeByName(configMaster, ExampleConfigUtils.makeConfig(iborCurveDefinition));
    ConfigMasterUtils.storeByName(configMaster, ExampleConfigUtils.makeConfig(iborCurveNodeIds));
  }

  private static void makeAudThreeCurveConfigurations(final ConfigMaster configMaster) {
    final String currency = "AUD";
    // curve construction configuration
    final String name = "AUD Swap Curves (1)";
    final String discountingCurveName = ExampleConfigUtils.generateVanillaFixedIncomeDiscountCurveName(currency);
    final String forward3mIborCurveName = ExampleConfigUtils.generateVanillaFixedIncomeIborCurveName(currency, Tenor.THREE_MONTHS);
    final String forward6mIborCurveName = ExampleConfigUtils.generateVanillaFixedIncomeIborCurveName(currency, Tenor.SIX_MONTHS);
    final DiscountingCurveTypeConfiguration discountingCurveType = new DiscountingCurveTypeConfiguration(currency);
    final OvernightCurveTypeConfiguration overnightCurveType = new OvernightCurveTypeConfiguration(OVERNIGHT_CONVENTION_FOR_CURRENCY.get(currency));
    final IborCurveTypeConfiguration ibor3mCurveType =
        new IborCurveTypeConfiguration(IBOR_SECURITY_FOR_CURRENCY.get(Pairs.of(currency, Tenor.THREE_MONTHS)), Tenor.THREE_MONTHS);
    final IborCurveTypeConfiguration ibor6mCurveType =
        new IborCurveTypeConfiguration(IBOR_SECURITY_FOR_CURRENCY.get(Pairs.of(currency, Tenor.SIX_MONTHS)), Tenor.SIX_MONTHS);
    final Map<String, List<? extends CurveTypeConfiguration>> curveTypes = new HashMap<>();
    curveTypes.put(discountingCurveName, Arrays.asList(discountingCurveType, overnightCurveType));
    curveTypes.put(forward3mIborCurveName, Arrays.asList(ibor3mCurveType));
    curveTypes.put(forward6mIborCurveName, Arrays.asList(ibor6mCurveType));
    final CurveGroupConfiguration group = new CurveGroupConfiguration(0, curveTypes);
    final List<CurveGroupConfiguration> groups = Arrays.asList(group);
    ConfigMasterUtils.storeByName(configMaster, ExampleConfigUtils.makeConfig(new CurveConstructionConfiguration(name, groups, Collections.<String>emptyList())));
    // discounting curve
    final String discountingCurveNodeIdMapperName = currency + NODE_MAPPER_SUFFIX;
    final Set<CurveNode> discountingCurveNodes = new LinkedHashSet<>();
    final Map<Tenor, CurveInstrumentProvider> oisNodes = new HashMap<>();
    discountingCurveNodes.add(new CashNode(ZERO, Tenor.TWO_DAYS, DEPOSIT_CONVENTION_FOR_CURRENCY.get(currency),
        discountingCurveNodeIdMapperName));
    final Map<Tenor, CurveInstrumentProvider> depositNodes = new HashMap<>();
    depositNodes.put(Tenor.TWO_DAYS, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId(currency + "CASHP2D"),
        MarketDataRequirementNames.MARKET_VALUE, DataFieldType.OUTRIGHT));
    for (final int i : new int[] {1, 2, 3, 4, 5, 6, 9 }) {
      final Tenor nodeTenor = Tenor.ofMonths(i);
      discountingCurveNodes.add(new SwapNode(ZERO, nodeTenor, FIXED_OIS_LEG_CONVENTION_FOR_CURRENCY.get(currency),
          OIS_LEG_CONVENTION_FOR_CURRENCY.get(currency), discountingCurveNodeIdMapperName));
      oisNodes.put(nodeTenor, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId(currency + "OIS_SWAP" + nodeTenor.toFormattedString())));
    }
    for (final int i : new int[] {1, 2, 3, 4, 5, 10 }) {
      final Tenor nodeTenor = Tenor.ofYears(i);
      discountingCurveNodes.add(new SwapNode(ZERO, Tenor.ofYears(i), FIXED_OIS_LEG_CONVENTION_FOR_CURRENCY.get(currency),
          OIS_LEG_CONVENTION_FOR_CURRENCY.get(currency), discountingCurveNodeIdMapperName));
      oisNodes.put(nodeTenor, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId(currency + "OIS_SWAP" + nodeTenor.toFormattedString())));
    }
    final CurveDefinition discountingCurveDefinition = new InterpolatedCurveDefinition(discountingCurveName, discountingCurveNodes,
        DoubleQuadraticInterpolator1dAdapter.NAME, LinearExtrapolator1dAdapter.NAME, LinearExtrapolator1dAdapter.NAME);
    final CurveNodeIdMapper discountingCurveNodeIdMapper = CurveNodeIdMapper.builder()
        .name(discountingCurveNodeIdMapperName)
        .cashNodeIds(depositNodes)
        .swapNodeIds(oisNodes)
        .build();
    ConfigMasterUtils.storeByName(configMaster, ExampleConfigUtils.makeConfig(discountingCurveDefinition));
    ConfigMasterUtils.storeByName(configMaster, ExampleConfigUtils.makeConfig(discountingCurveNodeIdMapper));
    // 3M LIBOR curve
    final String basisSwapCurveNodeIdMapperName = "AUD 3mx6m Basis" + NODE_MAPPER_SUFFIX;
    final String iborCurveNodeIdMapperName = "AUD IBOR" + NODE_MAPPER_SUFFIX;
    final Map<Tenor, CurveInstrumentProvider> iborNodes = new HashMap<>();
    final Map<Tenor, CurveInstrumentProvider> swapNodes = new HashMap<>();
    final Map<Tenor, CurveInstrumentProvider> basisSwapNodes = new HashMap<>();
    final Set<CurveNode> ibor3mCurveNodes = new LinkedHashSet<>();
    final Set<CurveNode> ibor6mCurveNodes = new LinkedHashSet<>();
    ibor3mCurveNodes.add(
        new CashNode(ZERO, Tenor.THREE_MONTHS, IBOR_SECURITY_FOR_CURRENCY.get(Pairs.of("AUD", Tenor.THREE_MONTHS)), iborCurveNodeIdMapperName));
    ibor6mCurveNodes.add(
        new CashNode(ZERO, Tenor.SIX_MONTHS, IBOR_SECURITY_FOR_CURRENCY.get(Pairs.of("AUD", Tenor.SIX_MONTHS)), iborCurveNodeIdMapperName));
    iborNodes.put(Tenor.THREE_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("AUDLIBORP3M"),
        MarketDataRequirementNames.MARKET_VALUE, DataFieldType.OUTRIGHT));
    iborNodes.put(Tenor.SIX_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("AUDLIBORP6M"),
        MarketDataRequirementNames.MARKET_VALUE, DataFieldType.OUTRIGHT));
    final ExternalId fixed3mLegConvention = ExternalId.of("CONVENTION", "AUD 3M");
    final ExternalId fixed6mLegConvention = ExternalId.of("CONVENTION", "AUD 6M");
    final ExternalId ibor3mLegConvention = ExternalId.of("CONVENTION", "AUD 6M");
    final ExternalId ibor6mLegConvention = ExternalId.of("CONVENTION", "AUD 6M");
    for (final int i : new int[] {1, 2, 3 }) {
      final Tenor nodeTenor = Tenor.ofYears(i);
      ibor3mCurveNodes.add(new SwapNode(ZERO, nodeTenor, fixed3mLegConvention, ibor3mLegConvention, iborCurveNodeIdMapperName));
      final ExternalId swapId = ExternalSchemes.syntheticSecurityId("AUDSWAP" + nodeTenor.toFormattedString());
      swapNodes.put(nodeTenor, new StaticCurveInstrumentProvider(swapId));
    }
    for (final int i : new int[] {4, 5 }) {
      final Tenor nodeTenor = Tenor.ofYears(i);
      ibor3mCurveNodes.add(new SwapNode(ZERO, nodeTenor, ibor3mLegConvention, ibor6mLegConvention, basisSwapCurveNodeIdMapperName));
      ibor6mCurveNodes.add(new SwapNode(ZERO, nodeTenor, ibor3mLegConvention, ibor6mLegConvention, basisSwapCurveNodeIdMapperName));
      final ExternalId basisSwapId = ExternalSchemes.syntheticSecurityId("AUDBASIS_SWAP_BBSWP3MBBSWP6M_" + nodeTenor.toFormattedString());
      basisSwapNodes.put(nodeTenor, new StaticCurveInstrumentProvider(basisSwapId));
    }
    for (final int i : new int[] {4, 5, 10}) {
      final Tenor nodeTenor = Tenor.ofYears(i);
      ibor6mCurveNodes.add(new SwapNode(ZERO, nodeTenor, fixed6mLegConvention, ibor6mLegConvention, iborCurveNodeIdMapperName));
      final ExternalId swapId = ExternalSchemes.syntheticSecurityId("AUDSWAP" + nodeTenor.toFormattedString());
      swapNodes.put(nodeTenor, new StaticCurveInstrumentProvider(swapId));
    }
    final CurveNodeIdMapper iborCurveNodeIds = CurveNodeIdMapper.builder()
        .name(iborCurveNodeIdMapperName)
        .cashNodeIds(iborNodes)
        .swapNodeIds(swapNodes)
        .build();
    final CurveNodeIdMapper basisCurveNodeIds = CurveNodeIdMapper.builder()
        .name(basisSwapCurveNodeIdMapperName)
        .swapNodeIds(basisSwapNodes)
        .build();
    final CurveDefinition ibor3mCurveDefinition = new InterpolatedCurveDefinition(forward3mIborCurveName, ibor3mCurveNodes, DoubleQuadraticInterpolator1dAdapter.NAME,
        LinearExtrapolator1dAdapter.NAME, LinearExtrapolator1dAdapter.NAME);
    final CurveDefinition ibor6mCurveDefinition = new InterpolatedCurveDefinition(forward6mIborCurveName, ibor6mCurveNodes, DoubleQuadraticInterpolator1dAdapter.NAME,
        LinearExtrapolator1dAdapter.NAME, LinearExtrapolator1dAdapter.NAME);
    ConfigMasterUtils.storeByName(configMaster, ExampleConfigUtils.makeConfig(ibor3mCurveDefinition));
    ConfigMasterUtils.storeByName(configMaster, ExampleConfigUtils.makeConfig(ibor6mCurveDefinition));
    ConfigMasterUtils.storeByName(configMaster, ExampleConfigUtils.makeConfig(iborCurveNodeIds));
    ConfigMasterUtils.storeByName(configMaster, ExampleConfigUtils.makeConfig(basisCurveNodeIds));
  }

  private ExamplesFixedIncomeCurveConfigsPopulator() {
  }
}
