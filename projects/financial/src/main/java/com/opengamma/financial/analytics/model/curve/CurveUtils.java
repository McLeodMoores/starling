/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.curve.ConverterUtils;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IborCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.OvernightCurveTypeConfiguration;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Utility methods for use in {@link MultiCurveFunction} and implementing classes.
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
   * Extracts the currency from a discounting curve type configuration or throws an exception if the reference cannot be
   * parsed as a currency
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
   * from the security source (to preserve the base OpenGamma behaviour). If the security is not found, a search for an {@link IborIndexConvention} in
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
   * Creates the {@link IndexON} from an overnight curve type configuration by trying first to use a {@link com.opengamma.financial.security.index.OvernightIndex}
   * from the security source (to preserve the base OpenGamma behaviour). If the security is not found, a search for an {@link OvernightIndexConvention} in
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
