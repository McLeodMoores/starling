/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.provider.calculator.blackforex;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.date.EmptyWorkingDayCalendar;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexNonDeliverableForwardDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexNonDeliverableOptionDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionDigitalDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionSingleBarrierDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.ImmutableFxMatrix;
import com.opengamma.analytics.financial.model.UncheckedMutableFxMatrix;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.Barrier;
import com.opengamma.analytics.financial.model.option.definition.Barrier.BarrierType;
import com.opengamma.analytics.financial.model.option.definition.Barrier.KnockType;
import com.opengamma.analytics.financial.model.option.definition.Barrier.ObservationType;
import com.opengamma.analytics.financial.model.volatility.curve.BlackForexTermStructureParameters;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexFlatProvider;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.factory.LinearInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.NamedInterpolator1dFactory;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pairs;

/**
 * Utilities for tests in this package.
 */
public final class FxCalculatorsTestUtils {
  /** The domestic currency. */
  public static final Currency DOMESTIC = Currency.USD;
  /** The foreign currency. */
  public static final Currency FOREIGN = Currency.JPY;
  /** JPY/USD. */
  public static final double JPYUSD = 100;
  /** The valuation date. */
  public static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2016, 8, 1);
  /** The notional. */
  public static final double NOTIONAL = 1234;
  /** A FX forward. */
  public static final ForexDefinition FX_FORWARD = new ForexDefinition(FOREIGN, DOMESTIC, VALUATION_DATE.plusMonths(3), NOTIONAL, JPYUSD);
  /** A non-deliverable FX forward. */
  public static final ForexNonDeliverableForwardDefinition NDF = new ForexNonDeliverableForwardDefinition(FOREIGN, DOMESTIC, NOTIONAL, JPYUSD, VALUATION_DATE.plusMonths(3), VALUATION_DATE.plusMonths(3));
  /** A long vanilla call option. */
  public static final ForexOptionVanillaDefinition LONG_VANILLA_CALL_OPTION = new ForexOptionVanillaDefinition(FX_FORWARD, VALUATION_DATE.plusMonths(3), true, true);
  /** A long vanilla put option. */
  public static final ForexOptionVanillaDefinition LONG_VANILLA_PUT_OPTION = new ForexOptionVanillaDefinition(FX_FORWARD, VALUATION_DATE.plusMonths(3), false, true);
  /** A short vanilla call option. */
  public static final ForexOptionVanillaDefinition SHORT_VANILLA_CALL_OPTION = new ForexOptionVanillaDefinition(FX_FORWARD, VALUATION_DATE.plusMonths(3), true, false);
  /** A short vanilla put option. */
  public static final ForexOptionVanillaDefinition SHORT_VANILLA_PUT_OPTION = new ForexOptionVanillaDefinition(FX_FORWARD, VALUATION_DATE.plusMonths(3), false, false);
  /** A long non-deliverable call option. */
  public static final ForexNonDeliverableOptionDefinition LONG_ND_CALL_OPTION = new ForexNonDeliverableOptionDefinition(NDF, true, true);
  /** A long non-deliverable put option. */
  public static final ForexNonDeliverableOptionDefinition LONG_ND_PUT_OPTION = new ForexNonDeliverableOptionDefinition(NDF, false, true);
  /** A short non-deliverable call option. */
  public static final ForexNonDeliverableOptionDefinition SHORT_ND_CALL_OPTION = new ForexNonDeliverableOptionDefinition(NDF, true, false);
  /** A short non-deliverable put option. */
  public static final ForexNonDeliverableOptionDefinition SHORT_ND_PUT_OPTION = new ForexNonDeliverableOptionDefinition(NDF, false, false);
  /** A long digital call option. */
  public static final ForexOptionDigitalDefinition LONG_DIGITAL_CALL_OPTION = new ForexOptionDigitalDefinition(FX_FORWARD, VALUATION_DATE.plusMonths(3), true, true);
  /** A long digital put option. */
  public static final ForexOptionDigitalDefinition LONG_DIGITAL_PUT_OPTION = new ForexOptionDigitalDefinition(FX_FORWARD, VALUATION_DATE.plusMonths(3), false, true);
  /** A short digital call option. */
  public static final ForexOptionDigitalDefinition SHORT_DIGITAL_CALL_OPTION = new ForexOptionDigitalDefinition(FX_FORWARD, VALUATION_DATE.plusMonths(3), true, false);
  /** A short digital put option. */
  public static final ForexOptionDigitalDefinition SHORT_DIGITAL_PUT_OPTION = new ForexOptionDigitalDefinition(FX_FORWARD, VALUATION_DATE.plusMonths(3), false, false);
  /** A long up and in call option. */
  public static final ForexOptionSingleBarrierDefinition LONG_UP_KNOCK_IN_CALL =
      new ForexOptionSingleBarrierDefinition(LONG_VANILLA_CALL_OPTION, new Barrier(KnockType.IN, BarrierType.UP, ObservationType.CONTINUOUS, JPYUSD + 0.5));
  /** A long down and in call option. */
  public static final ForexOptionSingleBarrierDefinition LONG_DOWN_KNOCK_IN_CALL =
      new ForexOptionSingleBarrierDefinition(LONG_VANILLA_CALL_OPTION, new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CONTINUOUS, JPYUSD - 0.5));
  /** A long up and out call option. */
  public static final ForexOptionSingleBarrierDefinition LONG_UP_KNOCK_OUT_CALL =
      new ForexOptionSingleBarrierDefinition(LONG_VANILLA_CALL_OPTION, new Barrier(KnockType.OUT, BarrierType.UP, ObservationType.CONTINUOUS, JPYUSD + 0.5));
  /** A long down and out call option. */
  public static final ForexOptionSingleBarrierDefinition LONG_DOWN_KNOCK_OUT_CALL =
      new ForexOptionSingleBarrierDefinition(LONG_VANILLA_CALL_OPTION, new Barrier(KnockType.OUT, BarrierType.DOWN, ObservationType.CONTINUOUS, JPYUSD - 0.5));
  /** A short up and in call option. */
  public static final ForexOptionSingleBarrierDefinition SHORT_UP_KNOCK_IN_CALL =
      new ForexOptionSingleBarrierDefinition(SHORT_VANILLA_CALL_OPTION, new Barrier(KnockType.IN, BarrierType.UP, ObservationType.CONTINUOUS, JPYUSD + 0.5));
  /** A short down and in call option. */
  public static final ForexOptionSingleBarrierDefinition SHORT_DOWN_KNOCK_IN_CALL =
      new ForexOptionSingleBarrierDefinition(SHORT_VANILLA_CALL_OPTION, new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CONTINUOUS, JPYUSD - 0.5));
  /** A short up and out call option. */
  public static final ForexOptionSingleBarrierDefinition SHORT_UP_KNOCK_OUT_CALL =
      new ForexOptionSingleBarrierDefinition(SHORT_VANILLA_CALL_OPTION, new Barrier(KnockType.OUT, BarrierType.UP, ObservationType.CONTINUOUS, JPYUSD + 0.5));
  /** A short down and out call option. */
  public static final ForexOptionSingleBarrierDefinition SHORT_DOWN_KNOCK_OUT_CALL =
      new ForexOptionSingleBarrierDefinition(SHORT_VANILLA_CALL_OPTION, new Barrier(KnockType.OUT, BarrierType.DOWN, ObservationType.CONTINUOUS, JPYUSD - 0.5));
  /** A long up and in put option. */
  public static final ForexOptionSingleBarrierDefinition LONG_UP_KNOCK_IN_PUT =
      new ForexOptionSingleBarrierDefinition(LONG_VANILLA_PUT_OPTION, new Barrier(KnockType.IN, BarrierType.UP, ObservationType.CONTINUOUS, JPYUSD + 0.5));
  /** A long down and in put option. */
  public static final ForexOptionSingleBarrierDefinition LONG_DOWN_KNOCK_IN_PUT =
      new ForexOptionSingleBarrierDefinition(LONG_VANILLA_PUT_OPTION, new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CONTINUOUS, JPYUSD - 0.5));
  /** A long up and out put option. */
  public static final ForexOptionSingleBarrierDefinition LONG_UP_KNOCK_OUT_PUT =
      new ForexOptionSingleBarrierDefinition(LONG_VANILLA_PUT_OPTION, new Barrier(KnockType.OUT, BarrierType.UP, ObservationType.CONTINUOUS, JPYUSD + 0.5));
  /** A long down and out put option. */
  public static final ForexOptionSingleBarrierDefinition LONG_DOWN_KNOCK_OUT_PUT =
      new ForexOptionSingleBarrierDefinition(LONG_VANILLA_PUT_OPTION, new Barrier(KnockType.OUT, BarrierType.DOWN, ObservationType.CONTINUOUS, JPYUSD - 0.5));
  /** A short up and in put option. */
  public static final ForexOptionSingleBarrierDefinition SHORT_UP_KNOCK_IN_PUT =
      new ForexOptionSingleBarrierDefinition(SHORT_VANILLA_PUT_OPTION, new Barrier(KnockType.IN, BarrierType.UP, ObservationType.CONTINUOUS, JPYUSD + 0.5));
  /** A short down and in put option. */
  public static final ForexOptionSingleBarrierDefinition SHORT_DOWN_KNOCK_IN_PUT =
      new ForexOptionSingleBarrierDefinition(SHORT_VANILLA_PUT_OPTION, new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CONTINUOUS, JPYUSD - 0.5));
  /** A short up and out put option. */
  public static final ForexOptionSingleBarrierDefinition SHORT_UP_KNOCK_OUT_PUT =
      new ForexOptionSingleBarrierDefinition(SHORT_VANILLA_PUT_OPTION, new Barrier(KnockType.OUT, BarrierType.UP, ObservationType.CONTINUOUS, JPYUSD + 0.5));
  /** A short down and out put option. */
  public static final ForexOptionSingleBarrierDefinition SHORT_DOWN_KNOCK_OUT_PUT =
      new ForexOptionSingleBarrierDefinition(SHORT_VANILLA_PUT_OPTION, new Barrier(KnockType.OUT, BarrierType.DOWN, ObservationType.CONTINUOUS, JPYUSD - 0.5));

  /** Market data with smile information */
  public static final BlackForexSmileProviderDiscount MARKET_DATA_WITH_SMILE;
  /** Market data without smile information */
  public static final BlackForexFlatProvider MARKET_DATA_WITHOUT_SMILE;
  static {
    final Interpolator1D interpolator = NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME);
    final double domesticRate = 0.01;
    final YieldAndDiscountCurve domesticCurve = new YieldCurve("USD Curve",
        InterpolatedDoublesCurve.from(new double[] {0, 1, 2, 3}, new double[] {domesticRate, domesticRate, domesticRate, domesticRate}, interpolator));
    final double foreignRate = 0.02;
    final YieldAndDiscountCurve foreignCurve = new YieldCurve("JPY Curve",
        InterpolatedDoublesCurve.from(new double[] {0, 1, 2, 3}, new double[] {foreignRate, foreignRate, foreignRate, foreignRate}, interpolator));
    final Map<Currency, YieldAndDiscountCurve> discountingCurves = new HashMap<>();
    discountingCurves.put(DOMESTIC, domesticCurve);
    discountingCurves.put(FOREIGN, foreignCurve);
    final UncheckedMutableFxMatrix fx = UncheckedMutableFxMatrix.of();
    fx.addCurrency(FOREIGN, DOMESTIC, JPYUSD);
    final ImmutableFxMatrix fxMatrix = ImmutableFxMatrix.of(fx);
    final MulticurveProviderDiscount curves = new MulticurveProviderDiscount(discountingCurves, Collections.<IborIndex, YieldAndDiscountCurve>emptyMap(), Collections.<IndexON, YieldAndDiscountCurve>emptyMap(), fxMatrix);
    final ZonedDateTime[] expiryDate = new ZonedDateTime[5];
    final double[] expiryTime = new double[5];
    for (int i = 0; i < 5; i++) {
      expiryDate[i] = ScheduleCalculator.getAdjustedDate(VALUATION_DATE, Period.ofMonths((i + 1) * 3), BusinessDayConventions.NONE, EmptyWorkingDayCalendar.INSTANCE, true);
      expiryTime[i] = TimeCalculator.getTimeBetween(VALUATION_DATE, expiryDate[i]);
    }
    final SmileDeltaTermStructureParametersStrikeInterpolation flatVolSurface = new SmileDeltaTermStructureParametersStrikeInterpolation(expiryTime,
        new double[] {0.1, 0.25}, new double[] {0.2, 0.2, 0.2, 0.2, 0.2}, new double[][] {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}}, new double[][] {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}});
    final BlackForexTermStructureParameters flatVolSurfaceNoSmile = new BlackForexTermStructureParameters(InterpolatedDoublesCurve.from(expiryTime, new double[] {0.2, 0.2, 0.2, 0.2, 0.2}, interpolator));
    MARKET_DATA_WITH_SMILE = new BlackForexSmileProviderDiscount(curves, flatVolSurface, Pairs.of(FOREIGN, DOMESTIC));
    MARKET_DATA_WITHOUT_SMILE = new BlackForexFlatProvider(curves, flatVolSurfaceNoSmile, Pairs.of(FOREIGN, DOMESTIC));
  }

}
