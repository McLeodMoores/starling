/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.security;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.VersionCorrection;

/**
 * Base class for classes that validate that particular criteria were satisfied when the configuration was created e.g.
 * that any securities that are referenced are available from the source.
 * @param <U>  the type of the object to be validated
 * @param <V>  the type of the objects that have been validated
 */
public abstract class SecurityValidator<U, V> {

  /**
   * Validates an object, returning information about successfully validated securities and duplicate, missing and / or
   * unsupported securities that were referenced by the object being tested.
   * @param object  the object, not null
   * @param versionCorrection  the version correction, not null
   * @param securitySource  the security source, not null
   * @return  the validation information
   */
  public abstract SecurityValidationInfo<V> validate(final U object, VersionCorrection versionCorrection, SecuritySource securitySource);
}
