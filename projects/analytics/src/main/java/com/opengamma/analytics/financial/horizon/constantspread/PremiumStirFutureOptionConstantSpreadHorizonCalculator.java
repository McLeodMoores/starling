/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.horizon.constantspread;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.horizon.HorizonCalculator;
import com.opengamma.analytics.financial.horizon.rolldown.StirFutureOptionBlackDataConstantSpreadRolldown;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionPremiumTransactionDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.calculator.blackstirfutures.PresentValueBlackSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesProviderInterface;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the theta for a short-term interest rate future option with upfront premium without rate slide.
 */
public class PremiumStirFutureOptionConstantSpreadHorizonCalculator
    extends HorizonCalculator<InterestRateFutureOptionPremiumTransactionDefinition, BlackSTIRFuturesProviderInterface, Void> {

  @Override
  public MultipleCurrencyAmount getTheta(final InterestRateFutureOptionPremiumTransactionDefinition definition, final ZonedDateTime date,
      final BlackSTIRFuturesProviderInterface data, final int daysForward, final Calendar calendar) {
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(data, "yield curve data");
    ArgumentChecker.isTrue(daysForward == 1 || daysForward == -1, "daysForward must be either 1 or -1");
    final InstrumentDerivative instrumentToday = definition.toDerivative(date);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final InstrumentDerivative instrumentTomorrow = definition.toDerivative(horizonDate);
    final BlackSTIRFuturesProviderInterface tomorrowData = StirFutureOptionBlackDataConstantSpreadRolldown.INSTANCE
        .rollDown(data, shiftTime);
    return subtract(instrumentTomorrow.accept(PresentValueBlackSTIRFutureOptionCalculator.getInstance(), tomorrowData),
        instrumentToday.accept(PresentValueBlackSTIRFutureOptionCalculator.getInstance(), data));
  }

}
