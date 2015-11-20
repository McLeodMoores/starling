/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.simulatedexamples.populator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;

import com.opengamma.analytics.math.interpolation.factory.DoubleQuadraticInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.FlatExtrapolator1dAdapter;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IborCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.ExternalId;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public class TestCurveConfigurationsPopulator {
  private static final ExternalId US = ExternalSchemes.financialRegionId(Country.US.getCode());
  private static final ExternalId USD_LIBOR_CONVENTION_ID = ExternalId.of("CONVENTION", "USD LIBOR");
  private static final IborIndexConvention USD_LIBOR_CONVENTION = new IborIndexConvention("USD LIBOR", USD_LIBOR_CONVENTION_ID.toBundle(),
      DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, Currency.USD, LocalTime.of(11, 0), "Europe/London",
      ExternalSchemes.financialRegionId(Country.GB.getCode()), US, "");
  private static final ExternalId USD_FIXED_SWAP_LEG_CONVENTION_ID = ExternalId.of("CONVENTION", "USD Fixed Swap Leg");
  private static final SwapFixedLegConvention USD_FIXED_SWAP_LEG_CONVENTION = new SwapFixedLegConvention("USD Fixed Swap Leg", USD_FIXED_SWAP_LEG_CONVENTION_ID.toBundle(),
      Tenor.SIX_MONTHS, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, Currency.USD, US, 2, false, StubType.NONE, false, 0);
  private static final ExternalId USD_LIBOR_SWAP_LEG_CONVENTION_ID = ExternalId.of("CONVENTION", "USD Vanilla LIBOR Swap Leg");
  private static final VanillaIborLegConvention USD_LIBOR_SWAP_LEG_CONVENTION = new VanillaIborLegConvention("USD Vanilla LIBOR Swap Leg", USD_LIBOR_SWAP_LEG_CONVENTION_ID.toBundle(),
      USD_LIBOR_CONVENTION_ID, false, "NONE", Tenor.THREE_MONTHS, 2, false, StubType.NONE, false, 0);

  public static void populateConfigAndConventionMaster(final ConfigMaster configMaster, final ConventionMaster conventionMaster) {
    final String curveNodeIdMapperName = "Synthetic USD";
    final String curveName = "USD 3M LIBOR";
    final String curveConstructionConfigurationName = "USD Curves";
    final Tenor[] liborTenors = new Tenor[] {Tenor.THREE_MONTHS};
    final Tenor[] swapTenors = new Tenor[] {Tenor.SIX_MONTHS, Tenor.ONE_YEAR, Tenor.TWO_YEARS, Tenor.THREE_YEARS, Tenor.FIVE_YEARS, Tenor.SEVEN_YEARS, Tenor.TEN_YEARS};
    ConfigMasterUtils.storeByName(configMaster, ConfigItem.of(makeIdMapper(curveNodeIdMapperName, liborTenors, swapTenors), curveNodeIdMapperName, CurveNodeIdMapper.class));
    ConfigMasterUtils.storeByName(configMaster, ConfigItem.of(makeCurveDefinition(curveName, curveNodeIdMapperName, liborTenors, swapTenors), curveName, InterpolatedCurveDefinition.class));
    ConfigMasterUtils.storeByName(configMaster, ConfigItem.of(makeCurveConstructionConfiguration(curveConstructionConfigurationName, curveName)));
    conventionMaster.add(new ConventionDocument(USD_FIXED_SWAP_LEG_CONVENTION));
    conventionMaster.add(new ConventionDocument(USD_LIBOR_CONVENTION));
    conventionMaster.add(new ConventionDocument(USD_LIBOR_SWAP_LEG_CONVENTION));
  }

  private static CurveNodeIdMapper makeIdMapper(final String name, final Tenor[] liborTenors, final Tenor[] swapTenors) {
    final Map<Tenor, CurveInstrumentProvider> cashNodeIds = new HashMap<>();
    for (final Tenor tenor : liborTenors) {
      final String ticker = "USDLIBOR" + tenor.getPeriod().toString();
      cashNodeIds.put(tenor, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId(ticker), MarketDataRequirementNames.MARKET_VALUE, DataFieldType.OUTRIGHT));
    }
    final Map<Tenor, CurveInstrumentProvider> swapNodeIds = new HashMap<>();
    for (final Tenor tenor : liborTenors) {
      final String ticker = "USDSWAP" + tenor.getPeriod().toString();
      swapNodeIds.put(tenor, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId(ticker), MarketDataRequirementNames.MARKET_VALUE, DataFieldType.OUTRIGHT));
    }
    return CurveNodeIdMapper.builder()
        .name(name)
        .cashNodeIds(cashNodeIds)
        .swapNodeIds(swapNodeIds)
        .build();
  }

  private static InterpolatedCurveDefinition makeCurveDefinition(final String name, final String curveNodeIdMapperName, final Tenor[] liborTenors, final Tenor[] swapTenors) {
    final Set<CurveNode> nodes = new HashSet<>();
    for (final Tenor tenor : liborTenors) {
      nodes.add(new CashNode(Tenor.of(Period.ZERO), tenor, USD_LIBOR_CONVENTION_ID, curveNodeIdMapperName));
    }
    for (final Tenor tenor : swapTenors) {
      nodes.add(new SwapNode(Tenor.of(Period.ZERO), tenor, USD_FIXED_SWAP_LEG_CONVENTION_ID, USD_LIBOR_SWAP_LEG_CONVENTION_ID, curveNodeIdMapperName));
    }
    return new InterpolatedCurveDefinition(name, nodes, DoubleQuadraticInterpolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME);
  }

  private static CurveConstructionConfiguration makeCurveConstructionConfiguration(final String name, final String curveName) {
    final List<CurveTypeConfiguration> curveTypes = new ArrayList<>();
    curveTypes.add(new DiscountingCurveTypeConfiguration(Currency.USD.getCode()));
    curveTypes.add(new IborCurveTypeConfiguration(USD_LIBOR_CONVENTION_ID, Tenor.THREE_MONTHS));
    final Map<String, List<? extends CurveTypeConfiguration>> typesForCurve = new HashMap<>();
    typesForCurve.put(curveName, curveTypes);
    final List<CurveGroupConfiguration> curveGroups = new ArrayList<>();
    curveGroups.add(new CurveGroupConfiguration(0, typesForCurve));
    return new CurveConstructionConfiguration(name, curveGroups, Collections.<String>emptyList());
  }
}
