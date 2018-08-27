/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master;

import static com.google.common.collect.Lists.newArrayList;
import static com.opengamma.lambdava.streams.Lambdava.functional;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.threeten.bp.Instant;

import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.lambdava.functions.Function1;

/**
 * Utilities for managing masters.
 * <p>
 * This is a thread-safe static utility class.
 */
public class MasterUtils {

  public static <D extends AbstractDocument> List<D> adjustVersionInstants(final Instant now, final Instant from, final Instant to, final List<D> documents) {
    for (final D document : documents) {
      final Instant fromInstant = document.getVersionFromInstant();
      if (fromInstant == null) {
        document.setVersionFromInstant(from);
      }
    }
    final List<D> copy = newArrayList(documents);
    Collections.sort(copy, new Comparator<D>() {
      @Override
      public int compare(final D a, final D b) {
        final Instant fromA = a.getVersionFromInstant();
        final Instant fromB = b.getVersionFromInstant();
        return fromA.compareTo(fromB);
      }
    });
    final Instant latestDocumentVersionTo = copy.get(copy.size() - 1).getVersionToInstant();
    D prevDocument = null;
    for (final D document : copy) {
      document.setVersionToInstant(latestDocumentVersionTo == null ? to : latestDocumentVersionTo);
      if (prevDocument != null) {
        prevDocument.setVersionToInstant(document.getVersionFromInstant());
      }
      prevDocument = document;
      document.setCorrectionFromInstant(now);
      document.setCorrectionToInstant(null);
    }
    return copy;
  }

  public static <D extends AbstractDocument> boolean checkUniqueVersionsFrom(final List<D> documents) {
    final Set<Instant> instants = new HashSet<>();
    for (final D document : documents) {
      instants.add(document.getVersionFromInstant());
    }
    return instants.size() == documents.size();
  }

  public static <D extends AbstractDocument> boolean checkVersionInstantsWithinRange(final Instant missing, final Instant from, final Instant to, final List<D> documents, final boolean equalFrom) {
    if (!documents.isEmpty()) {
      final SortedSet<Instant> instants = new TreeSet<>();
      for (final D document : documents) {
        final Instant fromInstant = document.getVersionFromInstant();
        if (fromInstant == null) {
          instants.add(missing);
        } else {
          instants.add(document.getVersionFromInstant());
        }
      }
      final Instant minFromVersion = instants.first();
      final Instant maxFromVersion = instants.last();
      return
        (equalFrom && minFromVersion.equals(from) || !equalFrom && !minFromVersion.isBefore(from))
          &&
          (to == null || !maxFromVersion.isAfter(to));
    } else {
      return true;
    }
  }

  public static <D extends UniqueIdentifiable> List<UniqueId> mapToUniqueIDs(final List<D> documents) {
    return functional(documents).map(new Function1<D, UniqueId>() {
      @Override
      public UniqueId execute(final D d) {
        return d.getUniqueId();
      }
    }).asList();
  }

}
