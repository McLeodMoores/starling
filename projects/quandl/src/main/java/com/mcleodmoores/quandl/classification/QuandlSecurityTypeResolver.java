/**
 * Copyright (C) 2014 - present by McLeod Moores Software Limited
 * Modified from APLv2 code Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
package com.mcleodmoores.quandl.classification;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.mcleodmoores.quandl.QuandlConstants;
import com.mcleodmoores.quandl.util.ArgumentChecker;
import com.opengamma.id.ExternalIdBundle;

/**
 * Determines the security type of a set of ids.
 */
public class QuandlSecurityTypeResolver {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(QuandlSecurityTypeResolver.class);
  /** Valid types */
  private static final Map<Pattern, QuandlSecurityType> VALID_TYPES = Maps.newConcurrentMap();

  static {
    addValidTypes(VALID_TYPES, QuandlFutureTypes.VALID_RATE_FUTURE_PATTERNS, QuandlSecurityType.RATE_FUTURE);
    addValidTypes(VALID_TYPES, QuandlCashTypes.VALID_IBOR_PATTERNS, QuandlSecurityType.CASH);
    addValidTypes(VALID_TYPES, QuandlCashTypes.VALID_OVERNIGHT_PATTERNS, QuandlSecurityType.CASH);
    addValidTypes(VALID_TYPES, QuandlSwapTypes.VALID_SWAP_PATTERNS, QuandlSecurityType.SWAP);
  }

  /**
   * Gets the security type from an id bundle. If the bundles do not contain a {@link QuandlConstants#QUANDL_CODE}, or
   * the code cannot be classified, returns an empty map.
   * @param idBundles The id bundles, not null
   * @return A map from id bundle to security type.
   */
  public Map<ExternalIdBundle, QuandlSecurityType> getSecurityType(final Collection<ExternalIdBundle> idBundles) {
    ArgumentChecker.notNull(idBundles, "idBundles");
    final Map<ExternalIdBundle, QuandlSecurityType> result = Maps.newHashMap();
    for (final ExternalIdBundle idBundle : idBundles) {
      final String quandlCode = idBundle.getValue(QuandlConstants.QUANDL_CODE);
      boolean found = false;
      if (quandlCode != null) {
        for (final Map.Entry<Pattern, QuandlSecurityType> entry : VALID_TYPES.entrySet()) {
          final Matcher matcher = entry.getKey().matcher(quandlCode);
          while (matcher.find()) {
            result.put(idBundle, entry.getValue());
            found = true;
          }
        }
      }
      if (!found) {
        LOGGER.warn("Could not find security type for {}", idBundle);
      }
    }
    return result;
  }

  /**
   * Creates valid type mappings.
   * @param types The types
   * @param validNamePattern The valid name pattern
   * @param securityType The security type
   */
  private static void addValidTypes(final Map<Pattern, QuandlSecurityType> types, final Set<Pattern> validNamePattern,
      final QuandlSecurityType securityType) {
    for (final Pattern validPattern : validNamePattern) {
      types.put(validPattern, securityType);
    }
  }
}
