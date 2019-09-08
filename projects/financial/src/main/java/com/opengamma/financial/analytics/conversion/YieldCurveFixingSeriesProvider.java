/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.core.convention.ConventionSource;
import com.opengamma.financial.security.FinancialSecurityVisitorSameValueAdapter;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class YieldCurveFixingSeriesProvider extends FinancialSecurityVisitorSameValueAdapter<Set<ExternalIdBundle>> {
  private final ConventionSource _conventionSource;

  public YieldCurveFixingSeriesProvider(final ConventionSource conventionSource) {
    super(Collections.<ExternalIdBundle> emptySet());
    _conventionSource = ArgumentChecker.notNull(conventionSource, "conventionSource");
  }

  @Override
  public Set<ExternalIdBundle> visitFRASecurity(final FRASecurity security) {
    return Collections.singleton(_conventionSource.getSingle(security.getUnderlyingId()).getExternalIdBundle());
  }

  @Override
  public Set<ExternalIdBundle> visitSwapSecurity(final SwapSecurity security) {
    final SwapLeg payLeg = security.getPayLeg();
    final SwapLeg receiveLeg = security.getReceiveLeg();
    final Set<ExternalIdBundle> idBundles = new HashSet<>();
    if (payLeg instanceof FloatingInterestRateLeg) {
      final FloatingInterestRateLeg floatLeg = (FloatingInterestRateLeg) payLeg;
      idBundles.add(_conventionSource.getSingle(floatLeg.getFloatingReferenceRateId()).getExternalIdBundle());
    }
    if (receiveLeg instanceof FloatingInterestRateLeg) {
      final FloatingInterestRateLeg floatLeg = (FloatingInterestRateLeg) receiveLeg;
      idBundles.add(_conventionSource.getSingle(floatLeg.getFloatingReferenceRateId()).getExternalIdBundle());
    }
    return idBundles;
  }

  @Override
  public Set<ExternalIdBundle> visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
    return Collections.singleton(security.getExternalIdBundle());
  }

}
