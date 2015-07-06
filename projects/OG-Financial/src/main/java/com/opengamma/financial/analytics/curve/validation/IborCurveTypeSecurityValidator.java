/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.curve.validation;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Iterables;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IborCurveTypeConfiguration;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;

/**
 *
 */
//TODO exogenous configs - should check that there's no clash with discounting, ibor, etc.
public class IborCurveTypeSecurityValidator extends SecurityValidator<CurveGroupConfiguration, IborIndex> {

  @Override
  protected SecurityValidationInfo<IborIndex> validate(final CurveGroupConfiguration configuration, final VersionCorrection versionCorrection, final SecuritySource securitySource) {
    final Map<String, List<? extends CurveTypeConfiguration>> namesToConfigurations = configuration.getTypesForCurves();
    final Collection<IborIndex> configurations = new HashSet<>();
    final Collection<ExternalId> missingConventions = new HashSet<>();
    final Collection<ExternalId> duplicatedConventions = new HashSet<>();
    final Collection<Security> unsupportedConventions = new HashSet<>();
    for (final Map.Entry<String, List<? extends CurveTypeConfiguration>> entry : namesToConfigurations.entrySet()) {
      if (entry.getValue() instanceof IborCurveTypeConfiguration) {
        final IborCurveTypeConfiguration type = (IborCurveTypeConfiguration) entry.getValue();
        final ExternalId conventionId = type.getConvention();
        try {
          final Collection<Security> conventions = securitySource.get(conventionId.toBundle(), versionCorrection);
          if (conventions == null || conventions.size() == 0) {
            missingConventions.add(conventionId);
          } else if (conventions.size() > 1) {
            duplicatedConventions.add(conventionId);
          } else {
            final Security convention = Iterables.getOnlyElement(conventions);
            if (convention instanceof IborIndex) {
              configurations.add((IborIndex) convention);
            } else {
              unsupportedConventions.add(convention);
            }
          }
        } catch (final DataNotFoundException e) {
          missingConventions.add(conventionId);
        }
      }
    }
    return new IborCurveTypeSecurityValidationInfo(configurations, missingConventions, duplicatedConventions, unsupportedConventions);
  }
}
