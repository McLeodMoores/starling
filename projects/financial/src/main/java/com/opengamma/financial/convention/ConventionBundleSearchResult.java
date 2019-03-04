/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import java.util.Collection;
import java.util.Collections;

/**
 * A collection of result documents from a search
 * 
 * @deprecated ConventionBundles should no longer be used. Use
 *             {@link com.opengamma.master.convention.ConventionDocument} and
 *             {@link com.opengamma.master.convention.ConventionSearchResult}.
 */
@Deprecated
public class ConventionBundleSearchResult {
  private final Collection<ConventionBundleDocument> _results;
  public ConventionBundleSearchResult(final ConventionBundleDocument singleResult) {
    _results = Collections.singletonList(singleResult);
  }
  public ConventionBundleSearchResult(final Collection<ConventionBundleDocument> results) {
    _results = results;
  }

  public Collection<ConventionBundleDocument> getResults() {
    return _results;
  }
}
