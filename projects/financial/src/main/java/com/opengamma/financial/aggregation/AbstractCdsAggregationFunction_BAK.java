/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.Collection;
import java.util.Comparator;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePositionComparator;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.cds.CreditDefaultSwapSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Abstract aggregation function for CDS reference entity data. If used with
 * non-CDS securities, all items will be classified as "N/A".
 *
 * @param <T> The type of data that this implementation will extract
 */
public abstract class AbstractCdsAggregationFunction_BAK<T> implements AggregationFunction<String> {

  /**
   * Classification indicating that this aggregation does not apply to the security.
   */
  private static final String NOT_APPLICABLE = "N/A";

  /**
   * The name to be used for this aggregation, not null.
   */
  private final String _name;

  /**
   * The security source used for resolution of the CDS security, not null.
   */
  private final SecuritySource _securitySource;

  /**
   * The extractor which will process the red code and return the required type, not null.
   */
  private final CdsRedCodeExtractor<T> _redCodeExtractor;

  /**
   * Creates the aggregation function.
   *
   * @param name the name to be used for this aggregation, not null
   * @param securitySource the security source used for resolution of the CDS security, not null
   * @param redCodeHandler the extractor which will process the red code and return the required type, not null
   */
  public AbstractCdsAggregationFunction_BAK(final String name, final SecuritySource securitySource, final RedCodeHandler<T> redCodeHandler) {

    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(securitySource, "securitySource");
    ArgumentChecker.notNull(redCodeHandler, "redCodeHandler");
    _name = name;
    _securitySource = securitySource;
    _redCodeExtractor = new CdsRedCodeExtractor<>(redCodeHandler);
  }

  @Override
  public Collection<String> getRequiredEntries() {
    return ImmutableList.of();
  }

  @Override
  public String classifyPosition(final Position position) {

    final Security security = resolveSecurity(position);

    if (security instanceof CreditDefaultSwapSecurity) {
      final CreditDefaultSwapSecurity cds = (CreditDefaultSwapSecurity) security;
      final T extracted = _redCodeExtractor.extract(cds);
      if (extracted != null) {

        return handleExtractedData(extracted);
      }
    }

    return NOT_APPLICABLE;
  }

  /**
   * Handle the data which has been returned from the {@link RedCodeHandler} instance.
   *
   * @param extracted the data extracted by the handler
   * @return the string which should be used as the classifier value
   */
  protected abstract String handleExtractedData(T extracted);

  private Security resolveSecurity(final Position position) {

    final Security security = position.getSecurityLink().getTarget();
    return security != null ? security : position.getSecurityLink().resolveQuiet(_securitySource);
  }

  @Override
  public Comparator<Position> getPositionComparator() {
    return new SimplePositionComparator();
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public int compare(final String sector1, final String sector2) {
    return sector1.compareTo(sector2);
  }
}
