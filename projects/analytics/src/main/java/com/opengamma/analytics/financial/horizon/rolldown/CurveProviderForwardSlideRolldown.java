/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon.rolldown;

import java.util.LinkedHashMap;
import java.util.Map;

import com.opengamma.analytics.financial.horizon.RolldownFunction;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Produces a {@link MulticurveProviderDiscount} or
 * {@link com.opengamma.analytics.financial.provider.description.interestrate.IssuerProvider} where all yield curves have been shifted
 * forward in time with slide.
 * <p>
 * This class does not handle all types of {@link ParameterProviderInterface} because the type hierarchy has not been refactored for easier
 * use.
 */
public final class CurveProviderForwardSlideRolldown implements RolldownFunction<ParameterProviderInterface> {
  /**
   * A static instance.
   */
  public static final CurveProviderForwardSlideRolldown INSTANCE = new CurveProviderForwardSlideRolldown();

  /**
   * Private constructor.
   */
  private CurveProviderForwardSlideRolldown() {
  }

  @Override
  public ParameterProviderInterface rollDown(final ParameterProviderInterface data, final double time) {
    ArgumentChecker.notNull(data, "data");
    if (data instanceof MulticurveProviderDiscount) {
      return rollDown((MulticurveProviderDiscount) data, time);
    }
    if (data instanceof IssuerProviderDiscount) {
      return rollDown((IssuerProviderDiscount) data, time);
    }
    throw new IllegalArgumentException("Cannot handle data of type " + data.getClass());
  }

  /**
   * Rolls down all of the curves in a {@link MulticurveProviderDiscount}
   *
   * @param data
   *          The data
   * @param time
   *          The time in years
   * @return A provider with all of the curves shifted by the time
   */
  private MulticurveProviderDiscount rollDown(final MulticurveProviderDiscount data, final double time) {
    final Map<Currency, YieldAndDiscountCurve> discountingCurves = new LinkedHashMap<>();
    for (final Map.Entry<Currency, YieldAndDiscountCurve> entry : data.getDiscountingCurves().entrySet()) {
      discountingCurves.put(entry.getKey(), YieldCurveForwardSlideRolldown.INSTANCE.rollDown(entry.getValue(), time));
    }
    final Map<IborIndex, YieldAndDiscountCurve> forwardIborCurves = new LinkedHashMap<>();
    for (final Map.Entry<IborIndex, YieldAndDiscountCurve> entry : data.getForwardIborCurves().entrySet()) {
      forwardIborCurves.put(entry.getKey(), YieldCurveForwardSlideRolldown.INSTANCE.rollDown(entry.getValue(), time));
    }
    final Map<IndexON, YieldAndDiscountCurve> forwardONCurves = new LinkedHashMap<>();
    for (final Map.Entry<IndexON, YieldAndDiscountCurve> entry : data.getForwardONCurves().entrySet()) {
      forwardONCurves.put(entry.getKey(), YieldCurveForwardSlideRolldown.INSTANCE.rollDown(entry.getValue(), time));
    }
    return new MulticurveProviderDiscount(discountingCurves, forwardIborCurves, forwardONCurves, data.getFxRates());
  }

  /**
   * Rolls down all of the curves in an {@link IssuerProviderDiscount}
   *
   * @param data
   *          The data
   * @param time
   *          The time in years
   * @return A provider with all of the curves shifted by the time
   */
  private IssuerProviderDiscount rollDown(final IssuerProviderDiscount data, final double time) {
    final MulticurveProviderDiscount underlying = data.getMulticurveProvider();
    final Map<Currency, YieldAndDiscountCurve> discountingCurves = new LinkedHashMap<>();
    for (final Map.Entry<Currency, YieldAndDiscountCurve> entry : underlying.getDiscountingCurves().entrySet()) {
      discountingCurves.put(entry.getKey(), YieldCurveForwardSlideRolldown.INSTANCE.rollDown(entry.getValue(), time));
    }
    final Map<IborIndex, YieldAndDiscountCurve> forwardIborCurves = new LinkedHashMap<>();
    for (final Map.Entry<IborIndex, YieldAndDiscountCurve> entry : underlying.getForwardIborCurves().entrySet()) {
      forwardIborCurves.put(entry.getKey(), YieldCurveForwardSlideRolldown.INSTANCE.rollDown(entry.getValue(), time));
    }
    final Map<IndexON, YieldAndDiscountCurve> forwardONCurves = new LinkedHashMap<>();
    for (final Map.Entry<IndexON, YieldAndDiscountCurve> entry : underlying.getForwardONCurves().entrySet()) {
      forwardONCurves.put(entry.getKey(), YieldCurveForwardSlideRolldown.INSTANCE.rollDown(entry.getValue(), time));
    }
    final Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> issuerCurves = new LinkedHashMap<>();
    for (final Map.Entry<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> entry : data.getIssuerCurves().entrySet()) {
      issuerCurves.put(entry.getKey(), YieldCurveForwardSlideRolldown.INSTANCE.rollDown(entry.getValue(), time));
    }
    return new IssuerProviderDiscount(discountingCurves, forwardIborCurves, forwardONCurves, issuerCurves, underlying.getFxRates());
  }
}
