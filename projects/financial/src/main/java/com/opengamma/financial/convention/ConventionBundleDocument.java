/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;


/**
 * Document to hold the reference rate and meta-data
 *
 * @deprecated {@link ConventionBundle} is deprecated. Use a {@link com.opengamma.core.convention.Convention} instead.
 */
@Deprecated
public class ConventionBundleDocument {
  private final String _name;
  private final ConventionBundle _conventionSet;

  public ConventionBundleDocument(final ConventionBundle conventionSet) {
    _conventionSet = conventionSet;
    _name = conventionSet.getName();
  }

  public String getName() {
    return _name;
  }

  public ConventionBundle getConventionSet() {
    return _conventionSet;
  }

  public ConventionBundle getValue() {
    return getConventionSet();
  }
}
