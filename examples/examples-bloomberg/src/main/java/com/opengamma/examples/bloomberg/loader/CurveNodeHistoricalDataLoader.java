/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.bloomberg.loader;

import static com.google.common.collect.Sets.newHashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.LocalDate;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleMaster;
import com.opengamma.financial.convention.DefaultConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.util.money.Currency;

/**
 * Finds the historical time series that need to be loaded so that they can be used as instruments in the defined curves.
 */
public class CurveNodeHistoricalDataLoader {

  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(CurveNodeHistoricalDataLoader.class);

  private Set<ExternalId> _curveNodesExternalIds;

  private Set<ExternalId> _initialRateExternalIds;

  private Set<ExternalIdBundle> _futuresExternalIds;

  public Set<ExternalId> getCurveNodesExternalIds() {
    return _curveNodesExternalIds;
  }

  public Set<ExternalId> getInitialRateExternalIds() {
    return _initialRateExternalIds;
  }

  public Set<ExternalIdBundle> getFuturesExternalIds() {
    return _futuresExternalIds;
  }

  public void run(final ToolContext tools) {
    final ConfigSource configSource = tools.getConfigSource();
    final ConfigMaster configMaster = tools.getConfigMaster();

    final Set<Currency> currencies = newHashSet();
  }

  private Set<ExternalId> getInitialRateExternalIds(final Set<Currency> currencies) {
    final ConventionBundleMaster cbm = new InMemoryConventionBundleMaster();
    final DefaultConventionBundleSource cbs = new DefaultConventionBundleSource(cbm);
    final Set<ExternalId> externalInitialRateId = newHashSet();
    for (final Currency currency : currencies) {
      for (final String swapType : new String[] { "SWAP", "3M_SWAP", "6M_SWAP" }) {
        final String product = currency.getCode() + "_" + swapType;
        final ConventionBundle convention = cbs.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, product));
        if (convention != null) {
          final ExternalId initialRate = convention.getSwapFloatingLegInitialRate();
          final ConventionBundle realIdConvention = cbs.getConventionBundle(initialRate);
          externalInitialRateId.add(realIdConvention.getIdentifiers().getExternalId(ExternalSchemes.BLOOMBERG_TICKER));
        } else {
          LOGGER.info("No convention for {} product", product);
        }
      }
    }
    return externalInitialRateId;
  }

  /**
   * Generate quarterly dates +/- 2 years around today to cover futures from past and near future
   *
   * @return list of dates
   */
  private List<LocalDate> buildDates() {
    final Clock clock = Clock.systemDefaultZone();
    final List<LocalDate> dates = new ArrayList<>();
    final LocalDate twoYearsAgo = LocalDate.now(clock).minusYears(2);
    final LocalDate twoYearsTime = LocalDate.now(clock).plusYears(2);
    for (LocalDate next = twoYearsAgo; next.isBefore(twoYearsTime); next = next.plusMonths(3)) {
      dates.add(next);
    }
    return dates;
  }

}
