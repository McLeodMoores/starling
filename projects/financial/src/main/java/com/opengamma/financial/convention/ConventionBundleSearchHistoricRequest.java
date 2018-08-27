/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import org.threeten.bp.Instant;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * A historic search request to retrieve convention set information.
 */
public class ConventionBundleSearchHistoricRequest {
  private final Instant _version;
  private final Instant _correction;
  private final ExternalIdBundle _identifiers;

  public ConventionBundleSearchHistoricRequest(final Instant version, final Instant correction, final ExternalId identifier) {
    _version = version;
    _correction = correction;
    _identifiers = ExternalIdBundle.of(identifier);
  }

  public ConventionBundleSearchHistoricRequest(final Instant version, final Instant correction, final ExternalIdBundle identifiers) {
    _version = version;
    _correction = correction;
    _identifiers = identifiers;
  }

  public Instant getVersion() {
    return _version;
  }

  public Instant getCorrection() {
    return _correction;
  }

  public ExternalIdBundle getIdentifiers() {
    return _identifiers;
  }
}
