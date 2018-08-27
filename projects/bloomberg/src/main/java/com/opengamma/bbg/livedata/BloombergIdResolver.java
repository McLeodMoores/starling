/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDomainIdentifierResolver;
import com.opengamma.bbg.util.ReferenceDataProviderUtils;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.livedata.resolver.AbstractResolver;
import com.opengamma.livedata.resolver.IdResolver;
import com.opengamma.util.ArgumentChecker;

/**
 * Resolves Bloomberg IDs. Accepts IDs of types:
 * <ul>
 * <li>{@link ExternalSchemes#BLOOMBERG_BUID}
 * <li>{@link ExternalSchemes#BLOOMBERG_TICKER}
 * <li>{@link ExternalSchemes#BLOOMBERG_TCM}
 * <li>{@link ExternalSchemes#ISIN}
 * <li>{@link ExternalSchemes#CUSIP}
 * </ul>
 * Returns an ID collection with {@link ExternalSchemes#BLOOMBERG_BUID}.
 * All other IDs are stripped out.
 *
 */
public class BloombergIdResolver extends AbstractResolver<ExternalIdBundle, ExternalId> implements IdResolver {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(BloombergIdResolver.class);
  private final ReferenceDataProvider _referenceDataProvider;

  public BloombergIdResolver(final ReferenceDataProvider referenceDataProvider) {
    ArgumentChecker.notNull(referenceDataProvider, "Reference Data Provider");
    _referenceDataProvider = referenceDataProvider;
  }

  /**
   * @return the referenceDataProvider
   */
  public ReferenceDataProvider getReferenceDataProvider() {
    return _referenceDataProvider;
  }

  @Override
  public Map<ExternalIdBundle, ExternalId> resolve(final Collection<ExternalIdBundle> bundles) {
    final Map<ExternalIdBundle, ExternalId> result = new HashMap<>();

    final Set<String> bbgKeys = new HashSet<>();
    final Map<String, Collection<ExternalIdBundle>> bbgKey2Bundle = new HashMap<>();

    for (final ExternalIdBundle bundle : bundles) {
      String bbgUniqueId = null;
      final ExternalId preferredIdentifier = BloombergDomainIdentifierResolver.resolvePreferredIdentifier(bundle);
      if (preferredIdentifier != null) {
        if (!preferredIdentifier.getScheme().equals(ExternalSchemes.BLOOMBERG_BUID)) {

          final String bloombergKey = BloombergDomainIdentifierResolver.toBloombergKey(preferredIdentifier);
          bbgKeys.add(bloombergKey);

          Collection<ExternalIdBundle> bundlesForKey = bbgKey2Bundle.get(bloombergKey);
          if (bundlesForKey == null) {
            bundlesForKey = new ArrayList<>();
            bbgKey2Bundle.put(bloombergKey, bundlesForKey);
          }
          bundlesForKey.add(bundle);

        } else {
          bbgUniqueId = preferredIdentifier.getValue();
          result.put(bundle, ExternalSchemes.bloombergBuidSecurityId(bbgUniqueId));
        }
      } else {
        LOGGER.info("Unable to identify any Bloomberg compatible identifier for {}", bundle);
        result.put(bundle, null);
      }
    }

    final Map<String, String> bbgKey2BbgUniqueId = ReferenceDataProviderUtils.getBloombergUniqueIDs(bbgKeys, getReferenceDataProvider());

    for (final String bbgKey : bbgKey2Bundle.keySet()) {
      final String bbgUniqueId = bbgKey2BbgUniqueId.get(bbgKey);

      ExternalId identifier;
      if (bbgUniqueId == null) {
        identifier = null;
      } else {
        identifier = ExternalSchemes.bloombergBuidSecurityId(bbgUniqueId);
      }

      for (final ExternalIdBundle bundle : bbgKey2Bundle.get(bbgKey)) {
        if (identifier == null) {
          LOGGER.warn("Unable to get Bloomberg unique ID for {}", bundle);
        }

        result.put(bundle, identifier);
      }
    }

    return result;
  }

}
