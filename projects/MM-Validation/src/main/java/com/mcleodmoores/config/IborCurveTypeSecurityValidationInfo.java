/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.config;

import java.util.Collection;

import com.mcleodmoores.security.SecurityValidationInfo;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.id.ExternalId;

/**
 *
 */
public class IborCurveTypeSecurityValidationInfo extends SecurityValidationInfo<IborIndex> {

  public IborCurveTypeSecurityValidationInfo(final Collection<IborIndex> configurations, final Collection<ExternalId> missingUnderlyings,
      final Collection<ExternalId> duplicatedUnderlyings, final Collection<?> unsupportedUnderlyings) {
    super(IborIndex.class, configurations, missingUnderlyings, duplicatedUnderlyings, unsupportedUnderlyings);
  }
}
