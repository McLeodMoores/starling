/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.convention;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import com.opengamma.core.convention.Convention;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * A container for information about objects that reference {@link Convention}s that have been validated:
 * <ul>
 *  <li> The type of the convention.</li>
 *  <li> A collection of objects that have been validated given a particular criterion, e.g. if all underlying conventions
 *  were present in the convention source.</li>
 *  <li> The ids of any conventions that were not found in the convention source.</li>
 *  <li> The ids of any conventions that were duplicated in the convention source.</li>
 *  <li> The ids of conventions that were found but that were not of the expected type.</li>
 * </ul>
 * @param <T> The type of the convention that has been validated.
 */
public class ConventionValidationInfo<T> {
  /** The type of the convention that has been validated */
  private final Class<T> _type;
  /** The objects that were validated */
  private final Collection<? extends T> _validated;
  /** The ids of missing underlying conventions */
  private final Collection<ExternalId> _missingConventions;
  /** The ids of duplicated underlying conventions */
  private final Collection<ExternalId> _duplicatedConventions;
  /** Unsupported conventions */
  private final Collection<? extends Convention> _unsupportedConventions;

  /**
   * Creates an instance without any entries for unsupported conventions.
   * @param type  the type of the conventions being tested, not null
   * @param validated  the objects that were validated, not null
   * @param missingConventions  the ids of missing underlying conventions, not null
   * @param duplicatedConventions  the ids of duplicated underlying conventions, not null
   */
  public ConventionValidationInfo(final Class<T> type, final Collection<? extends T> validated, final Collection<ExternalId> missingConventions,
      final Collection<ExternalId> duplicatedConventions) {
    this(type, validated, missingConventions, duplicatedConventions, Collections.<Convention>emptySet());
  }
  /**
   * Creates an instance.
   * @param type  the type of the conventions being tested, not null
   * @param validated  the objects that were validated, not null
   * @param missingConventions  the ids of missing underlying conventions, not null
   * @param duplicatedConventions  the ids of duplicated underlying conventions, not null
   * @param unsupportedConventions  any underlying conventions that were found but were not of the expected type, not null
   */
  public ConventionValidationInfo(final Class<T> type, final Collection<? extends T> validated, final Collection<ExternalId> missingConventions,
      final Collection<ExternalId> duplicatedConventions, final Collection<? extends Convention> unsupportedConventions) {
    _type = ArgumentChecker.notNull(type, "type");
    _validated = ArgumentChecker.notNull(validated, "validated");
    _missingConventions = ArgumentChecker.notNull(missingConventions, "missingConventions");
    _duplicatedConventions = ArgumentChecker.notNull(duplicatedConventions, "duplicatedConventions");
    _unsupportedConventions = ArgumentChecker.notNull(unsupportedConventions, "unsupportedConventions");
  }

  /**
   * Gets the type of the convention that is being validated.
   * @return  the type of the convention
   */
  public Class<T> getType() {
    return _type;
  }

  /**
   * Gets an unmodifiable collection of the objects that have been validated.
   * @return  the validated objects
   */
  public Collection<T> getValidatedObjects() {
    return Collections.unmodifiableCollection(_validated);
  }

  /**
   * Gets an unmodifiable collection of ids of missing conventions.
   * @return  the missing convention ids
   */
  public Collection<ExternalId> getMissingConventionIds() {
    return Collections.unmodifiableCollection(_missingConventions);
  }

  /**
   * Gets an unmodifiable collection of ids of duplicated conventions.
   * @return  the duplicated convention ids
   */
  public Collection<ExternalId> getDuplicatedConventionIds() {
    return Collections.unmodifiableCollection(_duplicatedConventions);
  }

  /**
   * Gets an unmodifiable collection of unsupported conventions.
   * @return  the unsupported conventions
   */
  public Collection<Convention> getUnsupportedConventions() {
    return Collections.unmodifiableCollection(_unsupportedConventions);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Objects.hashCode(_type);
    result = prime * result + Objects.hashCode(_validated);
    result = prime * result + Objects.hashCode(_duplicatedConventions);
    result = prime * result + Objects.hashCode(_missingConventions);
    result = prime * result * Objects.hashCode(_unsupportedConventions);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ConventionValidationInfo)) {
      return false;
    }
    final ConventionValidationInfo<?> other = (ConventionValidationInfo<?>) obj;
    if (!Objects.equals(_type, other._type)) {
      return false;
    }
    if (!Objects.equals(_missingConventions, other._missingConventions)) {
      return false;
    }
    if (!Objects.equals(_duplicatedConventions, other._duplicatedConventions)) {
      return false;
    }
    if (!Objects.equals(_unsupportedConventions, other._unsupportedConventions)) {
      return false;
    }
    if (!Objects.equals(_validated, other._validated)) {
      return false;
    }
    return true;
  }

}