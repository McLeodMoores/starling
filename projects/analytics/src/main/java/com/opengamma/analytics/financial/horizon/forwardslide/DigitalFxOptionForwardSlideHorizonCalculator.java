/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.horizon.forwardslide;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.definition.ForexOptionDigitalDefinition;
import com.opengamma.analytics.financial.horizon.HorizonCalculator;
import com.opengamma.analytics.financial.horizon.rolldown.FxOptionBlackDataForwardSlideRolldown;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.calculator.blackforex.PresentValueForexBlackSmileCalculator;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *
 */
public class DigitalFxOptionForwardSlideHorizonCalculator
    extends HorizonCalculator<ForexOptionDigitalDefinition, BlackForexSmileProviderInterface, Void> {

  /**
   * Calculates the theta for a digital FX option without rate slide.
   *
   * @param definition
   *          The swap definition, not null
   * @param date
   *          The calculation date, not null
   * @param data
   *          The initial yield curve data, not null
   * @param daysForward
   *          The number of days to roll forward, must be +/-1
   * @param calendar
   *          A holiday calendar, not used
   * @return The theta
   */
  @Override
  public MultipleCurrencyAmount getTheta(final ForexOptionDigitalDefinition definition, final ZonedDateTime date,
      final BlackForexSmileProviderInterface data, final int daysForward, final Calendar calendar) {
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(data, "yield curve data");
    ArgumentChecker.isTrue(daysForward == 1 || daysForward == -1, "daysForward must be either 1 or -1");
    final InstrumentDerivative instrumentToday = definition.toDerivative(date);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final InstrumentDerivative instrumentTomorrow = definition.toDerivative(horizonDate);
    final BlackForexSmileProviderInterface tomorrowData = FxOptionBlackDataForwardSlideRolldown.INSTANCE
        .rollDown(data, shiftTime);
    return subtract(instrumentTomorrow.accept(PresentValueForexBlackSmileCalculator.getInstance(), tomorrowData),
        instrumentToday.accept(PresentValueForexBlackSmileCalculator.getInstance(), data));
  }

}
