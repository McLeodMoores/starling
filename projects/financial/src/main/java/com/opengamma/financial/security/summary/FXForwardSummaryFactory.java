/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.summary;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.fx.FXForwardSecurity;

/**
 * Summary factory for {@link FXForwardSecurity}.
 */
public class FXForwardSummaryFactory implements SummaryFactory<FXForwardSecurity> {

  public FXForwardSummaryFactory(final SecuritySource securitySource) {
  }

  @Override
  public String getSecurityType() {
    return FXForwardSecurity.SECURITY_TYPE;
  }

  @Override
  public Summary getSummary(final FXForwardSecurity security) {
    return SummaryBuilder.create(security)
        .with(SummaryField.NOTIONAL, security.getPayAmount() + "/" + security.getReceiveAmount())
        .with(SummaryField.UNDERLYING, security.getPayCurrency() + "/" + security.getReceiveCurrency())
        .with(SummaryField.MATURITY, security.getForwardDate()).build();
  }

}
