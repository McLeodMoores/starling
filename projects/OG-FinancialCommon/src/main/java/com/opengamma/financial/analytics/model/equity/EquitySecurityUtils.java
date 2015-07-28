/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitorSameValueAdapter;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * Utility class for equity index options, equity barrier options, equity options and equity variance swaps.
 */
public final class EquitySecurityUtils {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(EquitySecurityUtils.class);
  /** mapping between surface names and data schemes */
  private static final Map<String, ExternalScheme> SCHEME_MAPPING = new HashMap<>();
  /** mapping between source external schemes and destination schemes i.e. BBG_TICKER_WEAK -&gt; BBG_TICKER */
  private static final HashMap<ExternalScheme, ExternalScheme> SCHEME_REMAPPING = new HashMap<>();
  static {
    SCHEME_MAPPING.put("ACTIV", ExternalSchemes.ACTIVFEED_TICKER);
    SCHEME_MAPPING.put("BBG", ExternalSchemes.BLOOMBERG_TICKER_WEAK);
    SCHEME_MAPPING.put("ICAP", ExternalSchemes.ICAP);
    SCHEME_MAPPING.put("TULLET", ExternalSchemes.SURF);
    SCHEME_REMAPPING.put(ExternalSchemes.BLOOMBERG_TICKER_WEAK, ExternalSchemes.BLOOMBERG_TICKER);
    SCHEME_MAPPING.put("DEFAULT", ExternalSchemes.OG_SYNTHETIC_TICKER);
  }

  /**
   * Restricted constructor.
   */
  private EquitySecurityUtils() {
  }

  /**
   * Gets the underlying index or equity name from a security. At the moment, only securities with a Bloomberg ticker or
   * BUID (if the security is an equity index option) are handled. For a Bloomberg ticker, the suffix is stripped
   * (SPX Index -&gt; SPX). For a BUID, the last three letters are assumed to be the name.
   *
   * @param security The security, not null
   * @return The equity or index name, null if the underlying id is not a BUID or Bloomberg ticker
   */
  public static String getIndexOrEquityNameFromUnderlying(final Security security) {
    return getIndexOrEquityNameFromUnderlying(security, false);
  }

  /**
   * Gets the underlying index or equity name from a security. At the moment, only securities with a Bloomberg ticker
   * or BUID (if the security is an equity index option) are handled. For a Bloomberg ticker, the suffix is stripped
   * (SPX Index -&gt; SPX). For a BUID, the last three letters are assumed to be the name.
   *
   * @param security The security, not null
   * @param stopAtFirstSpace true if one wishes result to include id up to first space (eg true : "IBM US Equity" =&gt; "IBM", false : =&gt; "IBM US").
   * @return The equity or index name, null if the underlying id is not a BUID or Bloomberg ticker
   */
  public static String getIndexOrEquityNameFromUnderlying(final Security security, final boolean stopAtFirstSpace) {
    ArgumentChecker.notNull(security, "security");
    final ExternalId underlyingId = FinancialSecurityUtils.getUnderlyingId(security);
    if (underlyingId == null) {
      LOGGER.error("Underlying id for security {} was null", security);
      return null;
    }
    final String value = underlyingId.getValue();
    final ExternalScheme scheme = underlyingId.getScheme();
    if (scheme.equals(ExternalSchemes.BLOOMBERG_BUID) || scheme.equals(ExternalSchemes.BLOOMBERG_BUID_WEAK)) {
      if (security instanceof EquityIndexOptionSecurity) {
        final int length = value.length();
        return value.substring(length - 3, length).toUpperCase(); //TODO fix this
      }
      LOGGER.error("Can only use BUIDs for equity index options; have {}", security);
      return null;
    } else if (scheme.equals(ExternalSchemes.BLOOMBERG_TICKER) || scheme.equals(ExternalSchemes.BLOOMBERG_TICKER_WEAK)) {
      if (stopAtFirstSpace) {
        final int firstSpace = value.indexOf(" ");
        return value.substring(0, firstSpace);
      }
      final int lastSpace = value.lastIndexOf(" ");
      return value.substring(0, lastSpace);
    } else if (scheme.equals(ExternalSchemes.OG_SYNTHETIC_TICKER)) {
      return value;
    }
    LOGGER.error("Cannot handle scheme of type {}", scheme);
    return null;
  }

  /**
   * Gets the underlying index or equity name from an external id. At the moment, only ids with a Bloomberg ticker are handled.
   * For a Bloomberg ticker, the suffix is stripped (SPX Index -&gt; SPX).
   *
   * @param underlyingId The security, not null
   * @return The equity or index name, null if the underlying id is not a Bloomberg ticker
   */
  public static String getIndexOrEquityNameFromUnderlying(final ExternalId underlyingId) {
    ArgumentChecker.notNull(underlyingId, "underlying id");
    final String value = underlyingId.getValue();
    final ExternalScheme scheme = underlyingId.getScheme();
    if (scheme.equals(ExternalSchemes.BLOOMBERG_TICKER) || scheme.equals(ExternalSchemes.BLOOMBERG_TICKER_WEAK)) {
      final int lastSpace = value.lastIndexOf(" ");
      return value.substring(0, lastSpace);
    }
    LOGGER.error("Cannot handle scheme of type {}", scheme);
    return null;
  }

  /**
   * Gets the underlying index or equity name from a unique id. At the moment, only securities with a Bloomberg ticker or BUID
   * (if the security is an equity index option) are handled. For a Bloomberg ticker, the suffix is stripped (SPX Index -&gt; SPX).
   * For a BUID, the last three letters are assumed to be the name.
   *
   * @param externalId The unique id, not null
   * @return The equity or index name, null if the underlying id is not a BUID or Bloomberg ticker
   */
  public static String getIndexOrEquityName(final ExternalId externalId) {
    //FIXME: Modify to take ExternalId to avoid incorrect cast to UniqueId
    ArgumentChecker.notNull(externalId, "unique id");
    final String value = externalId.getValue();
    final ExternalScheme extScheme = externalId.getScheme();

    if (extScheme.equals(ExternalSchemes.BLOOMBERG_BUID) || extScheme.equals(ExternalSchemes.BLOOMBERG_BUID_WEAK)) {
      final int length = value.length();
      return value.substring(length - 3, length).toUpperCase(); //TODO fix this
    }
    if (extScheme.equals(ExternalSchemes.BLOOMBERG_TICKER) || extScheme.equals(ExternalSchemes.BLOOMBERG_TICKER_WEAK)) {
      final int lastSpace = value.lastIndexOf(" ");
      return value.substring(0, lastSpace);
    }
    if (extScheme.equals(ExternalSchemes.OG_SYNTHETIC_TICKER)) {
      return value;
    }
    if (extScheme.equals(ExternalSchemes.ACTIVFEED_TICKER)) {
      final int firstDot = value.indexOf(".", 0);
      return value.substring(0, firstDot + 1); // e.g. "IBM."
    }
    LOGGER.error("Cannot handle scheme of type {}", extScheme);
    return null;
  }

  /**
   * Removes the postfix if the uid is a Bloomberg ticker.
   *
   * @param uid The unique id, not null
   * @return The ticker without postfix
   * @deprecated holding a Bloomberg ticker in a unique identifier is normally wrong
   */
  @Deprecated
  public static String getTrimmedTarget(final UniqueId uid) {
    ArgumentChecker.notNull(uid, "unique id");
    final String value = uid.getValue();
    if (uid.getScheme().equals(ExternalSchemes.BLOOMBERG_TICKER.getName()) || uid.getScheme().equals(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName())) {
      final int lastSpace = value.lastIndexOf(" ");
      return value.substring(0, lastSpace);
    }
    return value;
  }

  /**
   * Removes the postfix if the identifier is a Bloomberg ticker.
   *
   * @param identifier The ticker identifier
   * @return The ticker without postfix
   */
  public static String getTrimmedTarget(final ExternalId identifier) {
    ArgumentChecker.notNull(identifier, "identifier");
    final String value = identifier.getValue();
    if (identifier.getScheme().equals(ExternalSchemes.BLOOMBERG_TICKER) || identifier.getScheme().equals(ExternalSchemes.BLOOMBERG_TICKER_WEAK)) {
      final int lastSpace = value.lastIndexOf(" ");
      return value.substring(0, lastSpace);
    }
    return value;
  }

  /**
   * Returns a hard-coded value for the exchange.
   *
   * @param securitySource The security source, not null
   * @param id The id of the equity, not null
   * @return The exchange or null if there is no security for this id in the source.
   * @deprecated  this method does not get the exchange from the security source and returns
   * "CBOE" in all cases
   */
  @Deprecated
  public static String getExchange(final SecuritySource securitySource, final UniqueId id) {
    ArgumentChecker.notNull(securitySource, "security source");
    ArgumentChecker.notNull(id, "unique id");
    return "CBOE";
  }

  /**
   * Returns a hard-coded value for the currency.
   *
   * @param securitySource The security source, not null
   * @param id The id of the equity, not null
   * @return The currency or null if there is no security for this id in the source.
   * @deprecated  this method does not get the security from the source and returns "USD" in all cases.
   */
  @Deprecated
  public static String getCurrency(final SecuritySource securitySource, final UniqueId id) {
    ArgumentChecker.notNull(securitySource, "security source");
    ArgumentChecker.notNull(id, "unique id");
    return "USD";
  }

  /**
   * Determine the data scheme from the surface name e.g. BBG -&gt; Bloomberg ticker
   *
   * @param surfaceName the surface or curve name
   * @return the scheme
   */
  //TODO: This should be moved somewhere non equity specific
  public static ExternalScheme getTargetType(final String surfaceName) {
    final ExternalScheme target = SCHEME_MAPPING.get(surfaceName);
    if (target == null) {
      LOGGER.warn("Can't determine data scheme from surface/curve name: " + surfaceName);
      return null;
    }
    return target;
  }

  /**
   * Get scheme which maps onto this one.
   *
   * @param scheme the destination scheme
   * @return the source scheme
   */
  public static ExternalScheme getRemappedScheme(final ExternalScheme scheme) {
    final ExternalScheme remappedScheme = SCHEME_REMAPPING.get(scheme);
    if (remappedScheme == null) {
      return scheme; //no remapping
    }
    return remappedScheme;
  }

  /**
   * Gets the point value for an equity or equity index option.
   * @param security  the security
   * @return  the point value or null if not found.
   */
  public static Double getPointValue(final Security security) {
    if (security instanceof FinancialSecurity) {
      final FinancialSecurity financialSecurity = (FinancialSecurity) security;
      final Double pointValue = financialSecurity.accept(new FinancialSecurityVisitorSameValueAdapter<Double>(null) {

        @Override
        public Double visitEquityOptionSecurity(final EquityOptionSecurity equityOption) {
          return equityOption.getPointValue();
        }

        @Override
        public Double visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity equityIndexOption) {
          return equityIndexOption.getPointValue();
        }
      });
      return pointValue;
    }
    return null;
  }
}
