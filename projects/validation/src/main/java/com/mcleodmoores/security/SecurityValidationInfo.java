/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import com.opengamma.core.security.Security;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * A container for information about objects that reference {@link Security}s that have been validated:
 * <ul>
 *  <li> The type of the security.</li>
 *  <li> A collection of objects that have been validated given a particular criterion, e.g. if all underlying securities
 *  were present in the security source.</li>
 *  <li> The ids of any securities that were not found in the security source.</li>
 *  <li> The ids of any securities that were duplicated in the security source.</li>
 *  <li> The ids of securities that were found but that were not of the expected type.</li>
 * </ul>
 * @param <T> The type of the convention that has been validated.
 */
public class SecurityValidationInfo<T> {
  /** The type of the security that has been validated */
  private final Class<T> _type;
  /** The objects that were validated */
  private final Collection<? extends T> _validated;
  /** The ids of missing underlying securities */
  private final Collection<ExternalId> _missingSecurityIds;
  /** The ids of duplicated underlying securities */
  private final Collection<ExternalId> _duplicatedSecurityIds;
  /** Unsupported securities */
  private final Collection<? extends Security> _unsupportedSecurities;

  /**
   * Creates an instance without any entries for unsupported securities.
   * @param type  the type of the securities being tested, not null
   * @param validated  the objects that were found, not null
   * @param missingSecurities  the ids of missing securities, not null
   * @param duplicatedSecurities  the ids of duplicated securities, not null
   */
  public SecurityValidationInfo(final Class<T> type, final Collection<? extends T> validated, final Collection<ExternalId> missingSecurities,
      final Collection<ExternalId> duplicatedSecurities) {
    this(type, validated, missingSecurities, duplicatedSecurities, Collections.<Security>emptySet());
  }

  /**
   * Creates an instance.
   * @param type  the type of the securities being tested, not null
   * @param validated  the objects that were found, not null
   * @param missingSecurities  the ids of missing securities, not null
   * @param duplicatedSecurities  the ids of duplicated securities, not null
   * @param unsupportedSecurities  any underlying securities that were found but were not of the expected type, not null
   */
  public SecurityValidationInfo(final Class<T> type, final Collection<? extends T> validated, final Collection<ExternalId> missingSecurities,
      final Collection<ExternalId> duplicatedSecurities, final Collection<? extends Security> unsupportedSecurities) {
    _type = ArgumentChecker.notNull(type, "type");
    _validated = ArgumentChecker.notNull(validated, "validated");
    _missingSecurityIds = ArgumentChecker.notNull(missingSecurities, "missingSecurities");
    _duplicatedSecurityIds = ArgumentChecker.notNull(duplicatedSecurities, "duplicatedSecurities");
    _unsupportedSecurities = ArgumentChecker.notNull(unsupportedSecurities, "unsupportedSecurities");
  }

  /**
   * Gets the type of the security that is being validated.
   * @return  the type of the security
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
   * Gets an unmodifiable collection of ids of missing securities.
   * @return  the missing security ids
   */
  public Collection<ExternalId> getMissingSecurityIds() {
    return Collections.unmodifiableCollection(_missingSecurityIds);
  }

  /**
   * Gets an unmodifiable collection of ids of duplicated securities.
   * @return  the duplicated security ids
   */
  public Collection<ExternalId> getDuplicatedSecurityIds() {
    return Collections.unmodifiableCollection(_duplicatedSecurityIds);
  }

  /**
   * Gets an unmodifiable collection of unsupported securities.
   * @return  the unsupported securities
   */
  public Collection<Security> getUnsupportedSecurities() {
    return Collections.unmodifiableCollection(_unsupportedSecurities);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Objects.hashCode(_type);
    result = prime * result + Objects.hashCode(_validated);
    result = prime * result + Objects.hashCode(_duplicatedSecurityIds);
    result = prime * result + Objects.hashCode(_missingSecurityIds);
    result = prime * result * Objects.hashCode(_unsupportedSecurities);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SecurityValidationInfo)) {
      return false;
    }
    final SecurityValidationInfo<?> other = (SecurityValidationInfo<?>) obj;
    if (!Objects.equals(_type, other._type)) {
      return false;
    }
    if (!Objects.equals(_missingSecurityIds, other._missingSecurityIds)) {
      return false;
    }
    if (!Objects.equals(_duplicatedSecurityIds, other._duplicatedSecurityIds)) {
      return false;
    }
    if (!Objects.equals(_unsupportedSecurities, other._unsupportedSecurities)) {
      return false;
    }
    if (!Objects.equals(_validated, other._validated)) {
      return false;
    }
    return true;
  }

}