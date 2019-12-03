/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon.constantspread;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.horizon.HorizonCalculator;
import com.opengamma.analytics.financial.horizon.rolldown.SwaptionBlackDataConstantSpreadRolldown;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.provider.calculator.blackswaption.PresentValueBlackSwaptionCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSwaptionFlatProviderInterface;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *
 */
public class PhysicalSwaptionBlackConstantSpreadHorizonCalculator
    extends HorizonCalculator<SwaptionPhysicalFixedIborDefinition, BlackSwaptionFlatProviderInterface, Void> {

  @Override
  public MultipleCurrencyAmount getTheta(final SwaptionPhysicalFixedIborDefinition definition, final ZonedDateTime date,
      final BlackSwaptionFlatProviderInterface data, final int daysForward, final Calendar calendar) {
    return getTheta(definition, date, data, daysForward, calendar, null);
  }

  @Override
  public MultipleCurrencyAmount getTheta(final SwaptionPhysicalFixedIborDefinition definition, final ZonedDateTime date,
      final BlackSwaptionFlatProviderInterface data, final int daysForward,
      final Calendar calendar, final Void additionalData) {
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(data, "yield curve data");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.isTrue(daysForward == 1 || daysForward == -1, "daysForward must be either 1 or -1");
    final SwaptionPhysicalFixedIbor swaptionToday = definition.toDerivative(date);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final SwaptionPhysicalFixedIbor swaptionTomorrow = definition.toDerivative(horizonDate);
    final Currency currency = definition.getCurrency();
    final BlackSwaptionFlatProviderInterface tomorrowData = SwaptionBlackDataConstantSpreadRolldown.INSTANCE.rollDown(data, shiftTime);
    return subtract(swaptionTomorrow.accept(PresentValueBlackSwaptionCalculator.getInstance(), tomorrowData),
        swaptionToday.accept(PresentValueBlackSwaptionCalculator.getInstance(), data));
  }

}
