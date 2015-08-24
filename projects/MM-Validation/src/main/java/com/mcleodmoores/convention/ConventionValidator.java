/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.convention;

import com.opengamma.core.convention.ConventionSource;
import com.opengamma.id.VersionCorrection;

/**
 * Base class for classes that validate that particular criteria were satisfied when the configuration was created e.g. that
 * any conventions that are referenced are available from the source.
 * @param <U>  the type of the object to be validated
 * @param <V>  the type of the objects that have been validated
 */
public abstract class ConventionValidator<U, V> {

  /**
   * Validates an object, returning information about successfully validated conventions and duplicate, missing
   * and / or unsupported conventions that were referenced by the object being tested.
   * @param object  the object, not null
   * @param versionCorrection  the version correction, not null
   * @param conventionSource  the convention source, not null
   * @return  the validation information
   */
  public abstract ConventionValidationInfo<V> validate(final U object, VersionCorrection versionCorrection, ConventionSource conventionSource);
}
