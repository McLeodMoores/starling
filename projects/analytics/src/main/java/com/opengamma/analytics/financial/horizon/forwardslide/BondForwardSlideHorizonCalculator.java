/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon.forwardslide;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.horizon.HorizonCalculator;
import com.opengamma.analytics.financial.horizon.rolldown.CurveProviderForwardSlideRolldown;
import com.opengamma.analytics.financial.instrument.bond.BondTransactionDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the difference in the present value of a bond between two dates with rate slide.
 */
public final class BondForwardSlideHorizonCalculator
    extends HorizonCalculator<BondTransactionDefinition<?, ?>, IssuerProviderInterface, Double> {
  /** The present value calculator */
  private static final InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, MultipleCurrencyAmount> PV_CALCULATOR = PresentValueIssuerCalculator
      .getInstance();
  /** The singleton instance */
  private static final HorizonCalculator<BondTransactionDefinition<?, ?>, IssuerProviderInterface, Double> INSTANCE = new BondForwardSlideHorizonCalculator();

  /**
   * Gets the singleton instance.
   *
   * @return The instance
   */
  public static HorizonCalculator<BondTransactionDefinition<?, ?>, IssuerProviderInterface, Double> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor
   */
  private BondForwardSlideHorizonCalculator() {
  }

  @Override
  public MultipleCurrencyAmount getTheta(final BondTransactionDefinition<?, ?> definition, final ZonedDateTime date,
      final IssuerProviderInterface data, final int daysForward, final Calendar calendar) {
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(daysForward == 1 || daysForward == -1, "daysForward must be either 1 or -1");
    final InstrumentDerivative instrumentToday = definition.toDerivative(date);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final InstrumentDerivative instrumentTomorrow = definition.toDerivative(horizonDate);
    final IssuerProviderInterface dataTomorrow = (IssuerProviderInterface) CurveProviderForwardSlideRolldown.INSTANCE.rollDown(data,
        shiftTime);
    final MultipleCurrencyAmount pvTomorrow = instrumentTomorrow.accept(PV_CALCULATOR, dataTomorrow);
    final MultipleCurrencyAmount pvToday = instrumentToday.accept(PV_CALCULATOR, data);
    return subtract(pvTomorrow, pvToday);
  }

}
