/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Sets;
import com.opengamma.id.ExternalBundleIdentifiable;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdentifiable;
import com.opengamma.util.ArgumentChecker;

/**
 * Holds instances of {@link ExternalIdentifiable} for the purpose of improving searching in
 * any of the In Memory Masters.
 *
 * @param <T> the type of element stored
 * @param <V> the containing document
 */
public class InMemoryExternalIdCache<T extends ExternalBundleIdentifiable, V> {
  private final Map<T, V> _allItems = new ConcurrentHashMap<>();
  private final ConcurrentMap<ExternalId, Map<T, V>> _store = new ConcurrentHashMap<>();

  public void add(final T element, final V document) {
    ArgumentChecker.notNull(element, "element");
    ArgumentChecker.notNull(document, "document");
    final ExternalIdBundle bundle = element.getExternalIdBundle();
    if (bundle == null) {
      return;
    }
    for (final ExternalId externalId : bundle) {
      Map<T, V> elements = _store.get(externalId);
      if (elements == null) {
        final Map<T, V> fresh = new ConcurrentHashMap<>();
        elements = _store.putIfAbsent(externalId, fresh);
        elements = elements == null ? fresh : elements;
      }
      elements.put(element, document);
    }
    _allItems.put(element, document);
  }

  public void remove(final T element) {
    ArgumentChecker.notNull(element, "element");
    final ExternalIdBundle bundle = element.getExternalIdBundle();
    if (bundle == null) {
      return;
    }
    for (final ExternalId externalId : bundle) {
      final Map<T, V> elements = _store.get(externalId);
      if (elements == null) {
        continue;
      }
      elements.remove(element);
    }
    _allItems.remove(element);
  }

  /**
   * Obtain all items that <em>might</em> match the search request.
   * By design, this will return false positives so that a subsequent
   * call to {@link ExternalIdSearch#matches(Iterable)} can be performed
   * for further analysis, typically in the context of a larger check.
   * That being said, this method will attempt to minimize false
   * positives.
   * False negatives will not be returned.
   *
   * @param search the search to evaluate
   * @return items that are likely to match the search
   */
  public Set<V> getMatches(final ExternalIdSearch search) {
    ArgumentChecker.notNull(search, "search");

    switch (search.getSearchType()) {
      case NONE:
        return getMatchesNone(search);
      case EXACT: // Intentional fall-through.
      case ALL:
        return getMatchesAll(search);
      case ANY:
        return getMatchesAny(search);
    }

    throw new IllegalStateException("All branches should have been handled in the switch statement.");
  }

  private Set<V> getMatchesAny(final ExternalIdSearch search) {
    final Set<V> result = new HashSet<>();
    for (final ExternalId id : search.getExternalIds()) {
      final Map<T, V> matches = _store.get(id);
      if (matches == null) {
        continue;
      }
      result.addAll(matches.values());
    }
    return result;
  }

  private Set<V> getMatchesAll(final ExternalIdSearch search) {
    Set<V> result = null;
    for (final ExternalId id : search.getExternalIds()) {
      final Map<T, V> matches = _store.get(id);
      if (matches == null) {
        // We can short circuit it here because by definition
        // these can't be satisfied.
        return Collections.emptySet();
      }
      if (result == null) {
        result = new HashSet<>(matches.values());
      } else {
        result = Sets.intersection(result, new HashSet<>(matches.values()));
      }
    }
    return result;
  }

  private Set<V> getMatchesNone(final ExternalIdSearch search) {
    final Set<V> result = new HashSet<>(_allItems.values());
    for (final ExternalId id : search.getExternalIds()) {
      final Map<T, V> matches = _store.get(id);
      if (matches != null) {
        result.removeAll(matches.values());
      }
    }
    return result;
  }

}
