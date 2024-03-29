/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.summary;

import com.opengamma.financial.security.option.FXDigitalOptionSecurity;

/**
 * Summary factory for {@link FXDigitalOptionSecurity}.
 */
public class FXDigitalOptionSummaryFactory implements SummaryFactory<FXDigitalOptionSecurity> {

  @Override
  public String getSecurityType() {
    return FXDigitalOptionSecurity.SECURITY_TYPE;
  }

  @Override
  public Summary getSummary(final FXDigitalOptionSecurity security) {
    return SummaryBuilder.create(security)
        .with(SummaryField.DESCRIPTION, security.getPutCurrency() + "/" + security.getCallCurrency())
        .with(SummaryField.NOTIONAL, security.getPutAmount() + "/" + security.getCallAmount())
        .with(SummaryField.MATURITY, security.getExpiry()).build();
  }

}
