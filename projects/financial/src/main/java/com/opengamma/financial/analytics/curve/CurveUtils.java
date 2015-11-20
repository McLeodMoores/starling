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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.curve.credit.CurveDefinitionSource;
import com.opengamma.financial.analytics.curve.credit.CurveSpecificationBuilder;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Contains helper methods for curve construction functions.
 */
public final class CurveUtils {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(CurveUtils.class);

  /**
   * Restricted constructor.
   */
  private CurveUtils() {
  }

  /**
   * Builds a {@link CurveSpecification} from a curve definition that is valid at a particular time.
   * This method handles only {@link CurveDefinition} and {@link InterpolatedCurveDefinition}.
   *
   * @param valuationTime  the valuation time, not null
   * @param curveDefinitionSource  the curve definition source, not null
   * @param curveSpecificationBuilder  the curve specification builder, not null
   * @param curveDate  the curve date, not null
   * @param curveName  the curve name, not null
   * @return  the curve specification
   * @deprecated  this method does not handle definition types other than {@link CurveDefinition} and {@link InterpolatedCurveDefinition}. Use
   * {@link #getSpecification(Instant, CurveDefinitionSource, CurveSpecificationBuilder, LocalDate, String)}.
   */
  @Deprecated
  public static CurveSpecification getCurveSpecification(final Instant valuationTime, final CurveDefinitionSource curveDefinitionSource,
      final CurveSpecificationBuilder curveSpecificationBuilder, final LocalDate curveDate, final String curveName) {
    ArgumentChecker.notNull(valuationTime, "valuationTime");
    ArgumentChecker.notNull(curveDefinitionSource, "curveDefinitionSource");
    ArgumentChecker.notNull(curveSpecificationBuilder, "curveSpecificationBuilder");
    ArgumentChecker.notNull(curveDate, "curveDate");
    ArgumentChecker.notNull(curveName, "curveName");
    final CurveDefinition curveDefinition = curveDefinitionSource.getCurveDefinition(curveName);
    if (curveDefinition == null) {
      throw new OpenGammaRuntimeException("Could not get curve definition called " + curveName);
    }
    return curveSpecificationBuilder.buildCurve(valuationTime, curveDate, curveDefinition);
  }

  /**
   * Builds a {@link CurveSpecification} from a curve definition that is valid at a particular time.
   *
   * @param valuationTime  the valuation time, not null
   * @param curveDefinitionSource  the curve definition source, not null
   * @param curveSpecificationBuilder  the curve specification builder, not null
   * @param curveDate  the curve date, not null
   * @param curveName  the curve name, not null
   * @return  the curve specification
   */
  public static AbstractCurveSpecification getSpecification(final Instant valuationTime, final CurveDefinitionSource curveDefinitionSource,
      final CurveSpecificationBuilder curveSpecificationBuilder, final LocalDate curveDate, final String curveName) {
    ArgumentChecker.notNull(valuationTime, "valuationTime");
    ArgumentChecker.notNull(curveDefinitionSource, "curveDefinitionSource");
    ArgumentChecker.notNull(curveSpecificationBuilder, "curveSpecificationBuilder");
    ArgumentChecker.notNull(curveDate, "curveDate");
    ArgumentChecker.notNull(curveName, "curveName");
    final AbstractCurveDefinition curveDefinition = curveDefinitionSource.getDefinition(curveName);
    if (curveDefinition == null) {
      throw new OpenGammaRuntimeException("Could not get curve definition called " + curveName);
    }
    return curveSpecificationBuilder.buildSpecification(valuationTime, curveDate, curveDefinition);
  }

  /**
   * Gets the names of all the curves that are to be constructed in this configuration.
   *
   * @param configuration  the curve construction configuration, not null
   * @return  the names of all of the curves to be constructed
   */
  public static String[] getCurveNamesForConstructionConfiguration(final CurveConstructionConfiguration configuration) {
    ArgumentChecker.notNull(configuration, "configuration");
    final List<String> names = new ArrayList<>();
    for (final CurveGroupConfiguration group : configuration.getCurveGroups()) {
      for (final Map.Entry<String, List<? extends CurveTypeConfiguration>> entry : group.getTypesForCurves().entrySet()) {
        names.add(entry.getKey());
      }
    }
    return names.toArray(new String[names.size()]);
  }

  /**
   * Gets the currencies for all curve nodes in the interpolated curve definitions in a curve construction configuration
   * including exogenous configurations. If the curve definitions are of a different type, it is assumed that there are
   * no relevant currencies.
   *
   * @param configuration  the curve construction configuration, not null
   * @param curveDefinitionSource  the curve definition source, not null
   * @param curveConstructionConfigurationSource  the config source that contains information about any exogenous curve configurations, not null
   * @param curveNodeCurrencyVisitor  the curve node currency visitor, not null
   * @return  an ordered set of currencies for these curves
   */
  public static Set<Currency> getCurrencies(final CurveConstructionConfiguration configuration, final CurveDefinitionSource curveDefinitionSource,
      final CurveConstructionConfigurationSource curveConstructionConfigurationSource, final CurveNodeVisitor<Set<Currency>> curveNodeCurrencyVisitor) {
    ArgumentChecker.notNull(configuration, "configuration");
    ArgumentChecker.notNull(curveConstructionConfigurationSource, "curveConstructionConfigurationSource");
    ArgumentChecker.notNull(curveNodeCurrencyVisitor, "curveNodeCurrencyVisitor");
    final Set<Currency> currencies = new TreeSet<>();
    for (final CurveGroupConfiguration group : configuration.getCurveGroups()) {
      for (final Map.Entry<String, List<? extends CurveTypeConfiguration>> entry : group.getTypesForCurves().entrySet()) {
        final String curveName = entry.getKey();
        final AbstractCurveDefinition curveDefinition = curveDefinitionSource.getDefinition(curveName);
        if (curveDefinition == null) {
          throw new OpenGammaRuntimeException("Could not get curve definition called " + curveName);
        }
        if (curveDefinition instanceof InterpolatedCurveDefinition) {
          for (final CurveNode node : ((InterpolatedCurveDefinition) curveDefinition).getNodes()) {
            currencies.addAll(node.accept(curveNodeCurrencyVisitor));
          }
        }
      }
    }
    final List<String> exogenousConfigurations = configuration.getExogenousConfigurations();
    if (exogenousConfigurations != null && !exogenousConfigurations.isEmpty()) {
      for (final String name : exogenousConfigurations) {
        final CurveConstructionConfiguration exogenousConfiguration = curveConstructionConfigurationSource.getCurveConstructionConfiguration(name);
        if (exogenousConfiguration == null) {
          throw new OpenGammaRuntimeException("Exogenous configuration called " + name + " in " + configuration.getName()
              + " was not present in the config master");
        }
        currencies.addAll(getCurrencies(exogenousConfiguration, curveDefinitionSource, curveConstructionConfigurationSource, curveNodeCurrencyVisitor));
      }
    }
    return currencies;
  }

  /**
   * Extracts the currency from a discounting curve type configuration or throws an exception if the reference cannot be
   * parsed as a currency.
   * @param configuration  the configuration, not null
   * @return  the currency
   */
  public static Currency getCurrencyFromConfiguration(final DiscountingCurveTypeConfiguration configuration) {
    ArgumentChecker.notNull(configuration, "configuration");
    final String reference = configuration.getReference();
    try {
      return Currency.of(reference);
    } catch (final IllegalArgumentException e) {
      throw new OpenGammaRuntimeException("Cannot handle reference type " + reference + " for discounting curves");
    }
  }

  /**
   * Creates the {@link IborIndex} from an ibor curve type configuration by trying first to use a {@link com.opengamma.financial.security.index.IborIndex}
   * from the security source (to preserve the original behaviour). If the security is not found, a search for an {@link IborIndexConvention} in
   * the convention source is made using the convention id from the configuration. If neither a security nor convention can be found, throws an
   * exception.
   * @param configuration  the configuration, not null
   * @param securitySource  the security source, not null
   * @param conventionSource  the convention source, not null
   * @return  the index
   */
  public static IborIndex getIborIndexFromConfiguration(final IborCurveTypeConfiguration configuration, final SecuritySource securitySource,
      final ConventionSource conventionSource) {
    ArgumentChecker.notNull(configuration, "configuration");
    ArgumentChecker.notNull(securitySource, "securitySource");
    ArgumentChecker.notNull(conventionSource, "conventionSource");
    try {
      final Security sec = securitySource.getSingle(configuration.getConvention().toBundle());
      if (sec == null) {
        LOGGER.info("Cannot find Ibor index security with id {}: using convention" + configuration.getConvention());
        final IborIndexConvention indexConvention = conventionSource.getSingle(configuration.getConvention(), IborIndexConvention.class);
        if (indexConvention == null) {
          throw new OpenGammaRuntimeException("Could not find a security or an index with id " + configuration.getConvention());
        }
        return ConverterUtils.indexIbor(indexConvention.getName(), indexConvention, configuration.getTenor());
      }
      final com.opengamma.financial.security.index.IborIndex indexSecurity = (com.opengamma.financial.security.index.IborIndex) sec;
      final IborIndexConvention indexConvention = conventionSource.getSingle(indexSecurity.getConventionId(), IborIndexConvention.class);
      if (indexConvention == null) {
        throw new OpenGammaRuntimeException("Could not find a convention with id " + indexSecurity.getConventionId());
      }
      return ConverterUtils.indexIbor(indexConvention.getName(), indexConvention, indexSecurity.getTenor());
    } catch (final Exception e) {
      // sources can throw exceptions if the value is not found
      throw new OpenGammaRuntimeException("Could not create ibor index from " + configuration + ": " + e.getMessage());
    }
  }

  /**
   * Creates the {@link IndexON} from an overnight curve type configuration by trying to use a {@link com.opengamma.financial.security.index.OvernightIndex}
   * from the security source (to preserve the original behaviour). If the security is not found, a search for an {@link OvernightIndexConvention} in
   * the convention source is made using the convention id from the configuration. If neither a security nor convention can be found, throws an
   * exception.
   * @param configuration  the configuration, not null
   * @param securitySource  the security source, not null
   * @param conventionSource  the convention source, not null
   * @return  the index
   */
  public static IndexON getOvernightIndexFromConfiguration(final OvernightCurveTypeConfiguration configuration, final SecuritySource securitySource,
      final ConventionSource conventionSource) {
    ArgumentChecker.notNull(configuration, "configuration");
    ArgumentChecker.notNull(securitySource, "securitySource");
    ArgumentChecker.notNull(conventionSource, "conventionSource");
    try {
      final OvernightIndex overnightIndex = (OvernightIndex) securitySource.getSingle(configuration.getConvention().toBundle());
      if (overnightIndex == null) {
        LOGGER.info("Cannot find overnight index security with id {}: using convention" + configuration.getConvention());
        final OvernightIndexConvention indexConvention = conventionSource.getSingle(configuration.getConvention(), OvernightIndexConvention.class);
        if (indexConvention == null) {
          throw new OpenGammaRuntimeException("Could not find a security or an index with id " + configuration.getConvention());
        }
        return ConverterUtils.indexON(indexConvention.getName(), indexConvention);
      }
      final OvernightIndexConvention indexConvention = conventionSource.getSingle(overnightIndex.getConventionId(), OvernightIndexConvention.class);
      if (indexConvention == null) {
        throw new OpenGammaRuntimeException("Could not find a convention with id " + overnightIndex.getConventionId());
      }
      return ConverterUtils.indexON(indexConvention.getName(), indexConvention);
    } catch (final Exception e) {
      // sources can throw exceptions if the value is not found
      throw new OpenGammaRuntimeException("Could not create overnight index from " + configuration + ": " + e.getMessage());
    }
  }
}
