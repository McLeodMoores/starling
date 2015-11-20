/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.instrument.index;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BillSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillTransactionDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.util.ArgumentChecker;

/**
 * Class use to generate Bill transactions.
 */
public class GeneratorBill extends GeneratorInstrument<GeneratorAttribute> {

  /**
   * The underlying bill security.
   */
  private final BillSecurityDefinition _security;

  /**
   * Creates an instance.
   * @param name  the generator name, not null
   * @param security  the underlying bill security, not null
   */
  public GeneratorBill(final String name, final BillSecurityDefinition security) {
    super(name);
    _security = ArgumentChecker.notNull(security, "security");
  }

  /**
   * {@inheritDoc}
   * Generate a bill transaction from the bill (market quote) yield.
   */
  @Override
  public BillTransactionDefinition generateInstrument(final ZonedDateTime date, final double marketQuote, final double notional,
      final GeneratorAttribute attribute) {
    ArgumentChecker.notNull(date, "date");
    final int quantity = (int) Math.round(notional / _security.getNotional());
    final ZonedDateTime settleDate = ScheduleCalculator.getAdjustedDate(date, _security.getSettlementDays(), _security.getCalendar());
    return BillTransactionDefinition.fromYield(_security, quantity, settleDate, marketQuote, _security.getWorkingDayCalendar());
  }

}
