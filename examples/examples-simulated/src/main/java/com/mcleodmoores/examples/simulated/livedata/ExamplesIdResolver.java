/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.mcleodmoores.examples.simulated.livedata;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.resolver.IdResolver;
import com.opengamma.util.ArgumentChecker;

/**
 * Resolves identifiers to the object identifiers used in the examples simulated feed.
 */
public final class ExamplesIdResolver implements IdResolver {
  private final ExternalScheme _allowedScheme;

  /**
   * @param allowedScheme
   *          the scheme that can be resolved
   */
  public ExamplesIdResolver(final ExternalScheme allowedScheme) {
    _allowedScheme = ArgumentChecker.notNull(allowedScheme, "allowedScheme");
  }

  @Override
  public ExternalId resolve(final ExternalIdBundle ids) {
    return ids.getExternalId(_allowedScheme);
  }

  @Override
  public Map<ExternalIdBundle, ExternalId> resolve(final Collection<ExternalIdBundle> ids) {
    final Map<ExternalIdBundle, ExternalId> result = Maps.newHashMapWithExpectedSize(ids.size());
    for (final ExternalIdBundle id : ids) {
      result.put(id, resolve(id));
    }
    return result;
  }

}
