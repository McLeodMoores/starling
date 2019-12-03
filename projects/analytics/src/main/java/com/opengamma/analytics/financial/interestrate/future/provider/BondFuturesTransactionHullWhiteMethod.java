/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesTransaction;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Method to compute the bond futures transaction results with the price computed as the cheapest forward.
 */
public final class BondFuturesTransactionHullWhiteMethod extends FuturesTransactionHullWhiteIssuerMethod {
  private static final BondFuturesTransactionHullWhiteMethod INSTANCE = new BondFuturesTransactionHullWhiteMethod();
  private static final BondFuturesSecurityHullWhiteMethod UNDERLYING = BondFuturesSecurityHullWhiteMethod.getInstance();

  /**
   * Return the method unique instance.
   *
   * @return The instance.
   */
  public static BondFuturesTransactionHullWhiteMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private BondFuturesTransactionHullWhiteMethod() {
  }

  /**
   * Computes the future price from the curves used to price the underlying bonds and a Hull-White one factor model.
   *
   * @param future
   *          The future security.
   * @param data
   *          The curve and Hull-White parameters.
   * @param nbPoint
   *          The number of point in the numerical cross estimation.
   * @return The future price.
   */
  public double price(final BondFuturesTransaction future, final HullWhiteIssuerProviderInterface data, final int nbPoint) {
    ArgumentChecker.notNull(future, "future");
    return UNDERLYING.price(future.getUnderlyingSecurity(), data, nbPoint);
  }

  /**
   * Computes the future price from the curves used to price the underlying bonds and a Hull-White one factor model. The default number of
   * points is used for the numerical search.
   *
   * @param future
   *          The future security.
   * @param data
   *          The curve and Hull-White parameters.
   * @return The future price.
   */
  public double price(final BondFuturesTransaction future, final HullWhiteIssuerProviderInterface data) {
    ArgumentChecker.notNull(future, "future");
    return UNDERLYING.price(future.getUnderlyingSecurity(), data);
  }

  /**
   * Computes the present value of future from the curves using the cheapest-to-deliver and computing the value as a forward.
   *
   * @param future
   *          The future.
   * @param data
   *          The curve and Hull-White parameters.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final BondFuturesTransaction future, final HullWhiteIssuerProviderInterface data) {
    ArgumentChecker.notNull(future, "future");
    final double price = price(future, data);
    final double pv = future.getQuantity() * (price - future.getReferencePrice()) * future.getUnderlyingSecurity().getNotional();
    return MultipleCurrencyAmount.of(future.getCurrency(), pv);
  }

  /**
   * Computes the future price curve sensitivity.
   *
   * @param future
   *          The future security.
   * @param issuerMulticurves
   *          The issuer and multi-curves provider.
   * @param nbPoint
   *          The number of point in the numerical cross estimation.
   * @return The curve sensitivity.
   */
  public MulticurveSensitivity priceCurveSensitivity(final BondFuturesTransaction future,
      final HullWhiteIssuerProviderInterface issuerMulticurves, final int nbPoint) {
    ArgumentChecker.notNull(future, "future");
    return UNDERLYING.priceCurveSensitivity(future.getUnderlyingSecurity(), issuerMulticurves);
  }

  /**
   * Computes the future price curve sensitivity. The default number of points is used for the numerical search.
   *
   * @param future
   *          The future derivative.
   * @param data
   *          The curve and Hull-White parameters.
   * @return The curve sensitivity.
   */
  public MulticurveSensitivity priceCurveSensitivity(final BondFuturesTransaction future, final HullWhiteIssuerProviderInterface data) {
    ArgumentChecker.notNull(future, "future");
    return UNDERLYING.priceCurveSensitivity(future.getUnderlyingSecurity(), data);
  }

  /**
   * Compute the present value sensitivity to rates of a bond future by discounting.
   *
   * @param future
   *          The future.
   * @param data
   *          The curve and Hull-White parameters.
   * @return The present value rate sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final BondFuturesTransaction future,
      final HullWhiteIssuerProviderInterface data) {
    ArgumentChecker.notNull(future, "future");
    return UNDERLYING.presentValueCurveSensitivity(future.getUnderlyingSecurity(), data);
  }
}
