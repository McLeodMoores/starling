/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IndexDeposit;

public interface DepositIndexCouponDefinition<I extends IndexDeposit> {

  ZonedDateTime getAccrualStartDate();

  ZonedDateTime getAccrualEndDate();

  double getPaymentYearFraction();

  double getNotional();
}
