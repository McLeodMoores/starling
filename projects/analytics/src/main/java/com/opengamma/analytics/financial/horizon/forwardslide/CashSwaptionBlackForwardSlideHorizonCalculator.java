/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon.forwardslide;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.horizon.HorizonCalculator;
import com.opengamma.analytics.financial.horizon.rolldown.SwaptionBlackDataForwardSlideRolldown;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.provider.calculator.blackswaption.PresentValueBlackSwaptionCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSwaptionFlatProviderInterface;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * For cash-settled swaptions, calculates the difference in present value between one day and the next with volatility and rate slide. The
 * pricing model used is the Black model.
 */
public class CashSwaptionBlackForwardSlideHorizonCalculator
    extends HorizonCalculator<SwaptionCashFixedIborDefinition, BlackSwaptionFlatProviderInterface, Void> {

  @Override
  public MultipleCurrencyAmount getTheta(final SwaptionCashFixedIborDefinition definition, final ZonedDateTime date,
      final BlackSwaptionFlatProviderInterface data, final int daysForward, final Calendar calendar) {
    return getTheta(definition, date, data, daysForward, calendar, null);
  }

  @Override
  public MultipleCurrencyAmount getTheta(final SwaptionCashFixedIborDefinition definition, final ZonedDateTime date,
      final BlackSwaptionFlatProviderInterface data, final int daysForward, final Calendar calendar, final Void additionalData) {
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.isTrue(daysForward == 1 || daysForward == -1, "daysForward must be either 1 or -1");
    final SwaptionCashFixedIbor swaptionToday = definition.toDerivative(date);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final SwaptionCashFixedIbor swaptionTomorrow = definition.toDerivative(horizonDate);
    final BlackSwaptionFlatProviderInterface tomorrowData = SwaptionBlackDataForwardSlideRolldown.INSTANCE.rollDown(data, shiftTime);
    return subtract(swaptionTomorrow.accept(PresentValueBlackSwaptionCalculator.getInstance(), tomorrowData),
        swaptionToday.accept(PresentValueBlackSwaptionCalculator.getInstance(), data));
  }

}
