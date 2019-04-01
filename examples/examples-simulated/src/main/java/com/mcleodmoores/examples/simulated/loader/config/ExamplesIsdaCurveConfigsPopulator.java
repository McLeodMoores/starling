/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.examples.simulated.loader.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Period;

import com.opengamma.analytics.math.interpolation.factory.LinearExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.MonotonicConstrainedCubicSplineInterpolator1dAdapter;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ConventionSearchRequest;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Populates a config master with configurations for government bond curves.
 */
public class ExamplesIsdaCurveConfigsPopulator {
  private static final List<Currency> CCYS = Arrays.asList(Currency.AUD, /* Currency.CAD, */ Currency.CHF, Currency.EUR, Currency.GBP, Currency.JPY,
      Currency.NZD, Currency.USD);

  /**
   * @param configMaster
   *          a config master, not null
   * @param conventionMaster
   *          a convention master, not null
   */
  public static void populateConfigMaster(final ConfigMaster configMaster, final ConventionMaster conventionMaster) {
    ArgumentChecker.notNull(configMaster, "configMaster");
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.setConventionType(SwapFixedLegConvention.TYPE);
    final List<ManageableConvention> fixedLegConventions = conventionMaster.search(request).getConventions();
    if (fixedLegConventions.isEmpty()) {
      throw new IllegalStateException("No fixed swap leg conventions found in convention master");
    }
    request.setConventionType(VanillaIborLegConvention.TYPE);
    final List<ManageableConvention> vanillaIborLegConventions = conventionMaster.search(request).getConventions();
    if (vanillaIborLegConventions.isEmpty()) {
      throw new IllegalStateException("No vanilla *IBOR swap leg conventions found in convention master");
    }
    final Map<Currency, Tenor> iborTenors = new HashMap<>();
    final Map<Currency, ExternalId> iborConventionIds = new HashMap<>();
    final Map<Currency, ExternalId> payLegIds = new HashMap<>();
    final Map<Currency, ExternalId> receiveLegIds = new HashMap<>();
    for (final ManageableConvention mc : fixedLegConventions) {
      final SwapFixedLegConvention convention = (SwapFixedLegConvention) mc;
      payLegIds.put(convention.getCurrency(), convention.getExternalIdBundle().iterator().next());
    }
    for (final ManageableConvention mc : vanillaIborLegConventions) {
      final VanillaIborLegConvention convention = (VanillaIborLegConvention) mc;
      request.setConventionType(IborIndexConvention.TYPE);
      request.setExternalIdSearch(ExternalIdSearch.of(ExternalIdSearchType.ANY, convention.getIborIndexConvention()));
      final IborIndexConvention iborConvention = (IborIndexConvention) conventionMaster.search(request).getSingleConvention();
      final Currency ccy = iborConvention.getCurrency();
      iborTenors.put(ccy, convention.getResetTenor());
      iborConventionIds.put(ccy, convention.getIborIndexConvention());
      receiveLegIds.put(ccy, convention.getExternalIdBundle().iterator().next());
    }
    for (final Currency ccy : CCYS) {
      final String cccName = ccy.getCode() + " ISDA";
      final String curveName = ccy.getCode() + " ISDA";
      final String idMapperName = ccy.getCode() + " " + iborTenors.get(ccy).toFormattedString().substring(1) + " Tickers";
      final DiscountingCurveTypeConfiguration dctc = new DiscountingCurveTypeConfiguration(ccy.getCode());
      final Map<String, List<? extends CurveTypeConfiguration>> curveTypes = new HashMap<>();
      curveTypes.put(curveName, Arrays.asList(dctc));
      final CurveGroupConfiguration group = new CurveGroupConfiguration(0, curveTypes);
      final CurveConstructionConfiguration ccc = new CurveConstructionConfiguration(cccName, Arrays.asList(group), Collections.<String> emptyList());
      final ConfigItem<CurveConstructionConfiguration> cccItem = ConfigItem.of(ccc);
      cccItem.setName(ccc.getName());
      ConfigMasterUtils.storeByName(configMaster, cccItem);
      final Set<CurveNode> curveNodes = new LinkedHashSet<>();
      for (int i = 1; i <= 10; i++) {
        curveNodes.add(new SwapNode(Tenor.of(Period.ZERO), Tenor.ofYears(i), payLegIds.get(ccy), receiveLegIds.get(ccy), idMapperName));
      }
      final InterpolatedCurveDefinition interpolatedCurve = new InterpolatedCurveDefinition(curveName, curveNodes,
          MonotonicConstrainedCubicSplineInterpolator1dAdapter.NAME, LinearExtrapolator1dAdapter.NAME, LinearExtrapolator1dAdapter.NAME);
      final ConfigItem<InterpolatedCurveDefinition> curveItem = ConfigItem.of(interpolatedCurve);
      curveItem.setName(curveItem.getName());
      ConfigMasterUtils.storeByName(configMaster, curveItem);
    }
  }

}
