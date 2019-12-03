/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.summary;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.core.security.Security;

/**
 * Implements a builder pattern for a {@link Summary}.
 */
public class SummaryBuilder {

  private final Map<SummaryField, Object> _fieldMap = new HashMap<>();

  public static SummaryBuilder create(final Security security) {
    return new SummaryBuilder().with(SummaryField.TYPE, security.getSecurityType());
  }

  public static SummaryBuilder error(final String message) {
    return new SummaryBuilder();
  }

  public SummaryBuilder with(final SummaryField field, final Object value) {
    _fieldMap.put(field, value);
    return this;
  }

  public Summary build() {
    return new Summary(_fieldMap);
  }

}
