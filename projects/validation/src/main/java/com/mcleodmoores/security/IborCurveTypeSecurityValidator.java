/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.security;

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
import com.opengamma.util.ArgumentChecker;

/**
 * This class checks that there is a single, unique {@link IborIndex} available from the source for each ibor
 * curve type in a group.
 */
public final class IborCurveTypeSecurityValidator extends SecurityValidator<CurveGroupConfiguration, IborIndex> {
  /** A static instance */
  private static final SecurityValidator<CurveGroupConfiguration, IborIndex> INSTANCE = new IborCurveTypeSecurityValidator();

  /**
   * Gets an instance.
   * @return  an instance
   */
  public static SecurityValidator<CurveGroupConfiguration, IborIndex> getInstance() {
    return INSTANCE;
  }

  /**
   * Restricted constructor.
   */
  private IborCurveTypeSecurityValidator() {
  }

  @Override
  public SecurityValidationInfo<IborIndex> validate(final CurveGroupConfiguration configuration, final VersionCorrection versionCorrection,
      final SecuritySource securitySource) {
    ArgumentChecker.notNull(configuration, "configuration");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(securitySource, "securitySource");
    final Map<String, List<? extends CurveTypeConfiguration>> namesToConfigurations = configuration.getTypesForCurves();
    final Collection<IborIndex> validated = new HashSet<>();
    final Collection<ExternalId> missingSecurities = new HashSet<>();
    final Collection<ExternalId> duplicatedSecurities = new HashSet<>();
    final Collection<Security> unsupportedSecurities = new HashSet<>();
    for (final Map.Entry<String, List<? extends CurveTypeConfiguration>> entry : namesToConfigurations.entrySet()) {
      for (final CurveTypeConfiguration type : entry.getValue()) {
        if (type instanceof IborCurveTypeConfiguration) {
          final IborCurveTypeConfiguration iborType = (IborCurveTypeConfiguration) type;
          final ExternalId securityId = iborType.getConvention();
          try {
            final Collection<Security> securities = securitySource.get(securityId.toBundle(), versionCorrection);
            if (securities == null || securities.size() == 0) {
              missingSecurities.add(securityId);
            } else if (securities.size() > 1) {
              duplicatedSecurities.add(securityId);
            } else {
              final Security security = Iterables.getOnlyElement(securities);
              if (security instanceof IborIndex) {
                validated.add((IborIndex) security);
              } else {
                unsupportedSecurities.add(security);
              }
            }
          } catch (final DataNotFoundException e) {
            missingSecurities.add(securityId);
          }
        }
      }
    }
    return new SecurityValidationInfo<>(IborIndex.class, validated, missingSecurities, duplicatedSecurities, unsupportedSecurities);
  }
}
