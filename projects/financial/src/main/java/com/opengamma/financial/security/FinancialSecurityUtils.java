/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.Collection;
import java.util.function.Function;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.ComputationTargetTypeMap;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;

/**
 * General utility method applying to Financial Securities.
 */
public class FinancialSecurityUtils {

  private static ComputationTargetTypeMap<Function<ComputationTarget, ValueProperties>> s_getCurrencyConstraint = getCurrencyConstraint();

  private static ComputationTargetTypeMap<Function<ComputationTarget, ValueProperties>> getCurrencyConstraint() {
    final ComputationTargetTypeMap<Function<ComputationTarget, ValueProperties>> map = new ComputationTargetTypeMap<>();
    map.put(ComputationTargetType.POSITION, target -> {
      final Security security = target.getPosition().getSecurity();
      final Currency ccy = CurrencyVisitor.getCurrency(security);
      if (ccy != null) {
        return ValueProperties.with(ValuePropertyNames.CURRENCY, ccy.getCode()).get();
      }
      return ValueProperties.none();
    });
    map.put(ComputationTargetType.SECURITY, target -> {
      final Security security = target.getSecurity();
      final Currency ccy = CurrencyVisitor.getCurrency(security);
      if (ccy != null) {
        return ValueProperties.with(ValuePropertyNames.CURRENCY, ccy.getCode()).get();
      }
      return ValueProperties.none();
    });
    map.put(ComputationTargetType.TRADE, target -> {
      final Security security = target.getTrade().getSecurity();
      final Currency ccy = CurrencyVisitor.getCurrency(security);
      if (ccy != null) {
        return ValueProperties.with(ValuePropertyNames.CURRENCY, ccy.getCode()).get();
      }
      return ValueProperties.none();
    });
    map.put(ComputationTargetType.CURRENCY, target -> ValueProperties.with(ValuePropertyNames.CURRENCY, target.getUniqueId().getValue()).get());
    return map;
  }

  /**
   * @param target
   *          the computation target being examined.
   * @return ValueProperties containing a constraint of the CurrencyUnit or empty if not possible
   */
  public static ValueProperties getCurrencyConstraint(final ComputationTarget target) {
    final Function<ComputationTarget, ValueProperties> operation = s_getCurrencyConstraint.get(target.getType());
    if (operation != null) {
      return operation.apply(target);
    }
    return ValueProperties.none();
  }

  /**
   * @param security
   *          the security to be examined.
   * @return an ExternalId for a Region, where it is possible to determine, null otherwise.
   */
  public static ExternalId getRegion(final Security security) {
    if (security instanceof FinancialSecurity) {
      final FinancialSecurity finSec = (FinancialSecurity) security;
      return finSec.accept(new RegionVisitor());
    }
    return null;
  }

  /**
   * @param security
   *          the security to be examined.
   * @return an ExternalId for an Exchange, where it is possible to determine, null otherwise.
   */
  public static ExternalId getExchange(final Security security) {
    if (security instanceof FinancialSecurity) {
      final FinancialSecurity finSec = (FinancialSecurity) security;
      return finSec.accept(new ExchangeVisitor());
    }
    return null;
  }

  /**
   * @param security
   *          the security to be examined.
   * @return a Currency, where it is possible to determine a single Currency association, null otherwise.
   */
  public static Currency getCurrency(final Security security) {
    return CurrencyVisitor.getCurrency(security);
  }

  /**
   * @param security
   *          the security to be examined.
   * @param securitySource
   *          a security source
   * @return a Currency, where it is possible to determine a Currency association, null otherwise.
   */
  public static Collection<Currency> getCurrencies(final Security security, final SecuritySource securitySource) {
    return CurrenciesVisitor.getCurrencies(security, securitySource);
  }

  /**
   * Check if a security is exchange traded.
   *
   * @param security
   *          the security to be examined.
   * @return true if exchange traded or false otherwise.
   */
  public static boolean isExchangeTraded(final Security security) {
    boolean result = false;
    if (security instanceof FinancialSecurity) {
      final FinancialSecurity finSec = (FinancialSecurity) security;
      final Boolean isExchangeTraded = finSec.accept(new ExchangeTradedVisitor());
      result = isExchangeTraded == null ? false : isExchangeTraded;
    }
    return result;
  }

  /**
   * Returns the underlying id of a security (e.g. the id of the equity underlying an equity future).
   *
   * @param security
   *          The security, not null
   * @return The id of the underlying of a security, where it is possible to identify this, or null
   */
  public static ExternalId getUnderlyingId(final Security security) {
    return UnderlyingVisitor.getUnderlyingId(security);
  }

  /**
   * Returns the notional for a security.
   *
   * @param security
   *          the security
   * @param currencyPairs
   *          the available currency pairs, FX instruments will return the amount in the base currency
   * @param securitySource
   *          the security source
   * @return the amount in the base currency (FX), null if not applicable
   */
  public static CurrencyAmount getNotional(final Security security, final CurrencyPairs currencyPairs, final SecuritySource securitySource) {
    if (security instanceof FinancialSecurity) {
      final NotionalVisitor visitor = new NotionalVisitor(currencyPairs, securitySource);
      return ((FinancialSecurity) security).accept(visitor);
    }
    return null;
  }
}
