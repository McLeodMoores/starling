/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.horizon.forwardslide;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.horizon.HorizonCalculator;
import com.opengamma.analytics.financial.horizon.rolldown.CurveProviderForwardSlideRolldown;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *
 */
public class FxForwardSlideHorizonCalculator extends HorizonCalculator<ForexDefinition, MulticurveProviderInterface, Void> {

  /**
   * Calculates the theta for a FX spot or forward trade without rate slide.
   *
   * @param definition
   *          The FX trade definition, not null
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
  public MultipleCurrencyAmount getTheta(final ForexDefinition definition, final ZonedDateTime date,
      final MulticurveProviderInterface data, final int daysForward, final Calendar calendar) {
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(data, "yield curve data");
    ArgumentChecker.isTrue(daysForward == 1 || daysForward == -1, "daysForward must be either 1 or -1");
    final InstrumentDerivative instrumentToday = definition.toDerivative(date);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final InstrumentDerivative instrumentTomorrow = definition.toDerivative(horizonDate);
    final MulticurveProviderInterface tomorrowData = (MulticurveProviderInterface) CurveProviderForwardSlideRolldown.INSTANCE
        .rollDown(data, shiftTime);
    return subtract(instrumentTomorrow.accept(PresentValueDiscountingCalculator.getInstance(), tomorrowData),
        instrumentToday.accept(PresentValueDiscountingCalculator.getInstance(), data));
  }

}
