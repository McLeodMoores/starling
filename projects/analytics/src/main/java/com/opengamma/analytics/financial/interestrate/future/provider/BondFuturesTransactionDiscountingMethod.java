/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesTransaction;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Method to compute the bond futures transaction results with the price computed as the cheapest forward.
 */
public final class BondFuturesTransactionDiscountingMethod extends FuturesTransactionIssuerMethod {

  /**
   * Creates the method unique instance.
   */
  private static final BondFuturesTransactionDiscountingMethod INSTANCE = new BondFuturesTransactionDiscountingMethod();

  /**
   * Return the method unique instance.
   *
   * @return The instance.
   */
  public static BondFuturesTransactionDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private BondFuturesTransactionDiscountingMethod() {
  }

  /**
   * The method to compute bond security figures.
   */
  private static final BondFuturesSecurityDiscountingMethod METHOD_FUTURES_SEC = BondFuturesSecurityDiscountingMethod.getInstance();

  /**
   * Computes the present value of future from the curves using the cheapest-to-deliver and computing the value as a forward.
   *
   * @param futures
   *          The future.
   * @param issuerMulticurves
   *          The issuer and multi-curves provider.
   * @param netBasis
   *          The net basis associated to the future.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromNetBasis(final BondFuturesTransaction futures,
      final IssuerProviderInterface issuerMulticurves,
      final double netBasis) {
    return presentValueFromPrice(futures,
        METHOD_FUTURES_SEC.priceFromNetBasis(futures.getUnderlyingSecurity(), issuerMulticurves, netBasis));
  }

  /**
   * Computes the future price from the curves used to price the underlying bonds.
   *
   * @param future
   *          The future security.
   * @param issuerMulticurves
   *          The issuer and multi-curves provider.
   * @return The future price.
   */
  public double price(final BondFuturesTransaction future, final IssuerProviderInterface issuerMulticurves) {
    return priceFromNetBasis(future, issuerMulticurves, 0.0);
  }

  /**
   * Compute the present value of a future transaction from a quoted price.
   *
   * @param future
   *          The future.
   * @param price
   *          The quoted price.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromPrice(final BondFuturesTransaction future, final double price) {
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
   * @return The curve sensitivity.
   */
  public MulticurveSensitivity priceCurveSensitivity(final BondFuturesTransaction future, final IssuerProviderInterface issuerMulticurves) {
    ArgumentChecker.notNull(future, "future");
    return METHOD_FUTURES_SEC.priceCurveSensitivity(future.getUnderlyingSecurity(), issuerMulticurves);
  }

  /**
   * Compute the present value sensitivity to rates of a bond future by discounting.
   *
   * @param future
   *          The future.
   * @param issuerMulticurves
   *          The issuer and multi-curves provider.
   * @return The present value rate sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final BondFuturesTransaction future,
      final IssuerProviderInterface issuerMulticurves) {
    ArgumentChecker.notNull(future, "future");
    return METHOD_FUTURES_SEC.presentValueCurveSensitivity(future.getUnderlyingSecurity(), issuerMulticurves);
  }

  /**
   * Computes the futures price from the curves used to price the underlying bonds and the net basis.
   *
   * @param future
   *          The future security.
   * @param issuerMulticurves
   *          The issuer and multi-curves provider.
   * @param netBasis
   *          The net basis associated to the future.
   * @return The future price.
   */
  public double priceFromNetBasis(final BondFuturesTransaction future, final IssuerProviderInterface issuerMulticurves,
      final double netBasis) {
    ArgumentChecker.notNull(future, "Future");
    return METHOD_FUTURES_SEC.priceFromNetBasis(future.getUnderlyingSecurity(), issuerMulticurves, netBasis);
  }

  /**
   * Computes the gross basis of the bonds in the underlying basket from their clean prices.
   *
   * @param future
   *          The future security.
   * @param cleanPrices
   *          The clean prices (at standard bond market spot date) of the bond in the basket.
   * @param futurePrice
   *          The future price.
   * @return The gross basis for each bond in the basket.
   */
  public double[] grossBasisFromPrices(final BondFuturesTransaction future, final double[] cleanPrices, final double futurePrice) {
    ArgumentChecker.notNull(future, "futures");
    return METHOD_FUTURES_SEC.grossBasisFromPrices(future.getUnderlyingSecurity(), cleanPrices, futurePrice);
  }

  /**
   * Computes the gross basis of the bonds in the underlying basket from the curves.
   *
   * @param future
   *          The future security.
   * @param issuerMulticurves
   *          The issuer and multi-curves provider.
   * @param futurePrice
   *          The future price.
   * @return The gross basis for each bond in the basket.
   */
  public double[] grossBasisFromCurves(final BondFuturesTransaction future, final IssuerProviderInterface issuerMulticurves,
      final double futurePrice) {
    ArgumentChecker.notNull(future, "future");
    return METHOD_FUTURES_SEC.grossBasisFromCurves(future.getUnderlyingSecurity(), issuerMulticurves, futurePrice);
  }

  /**
   * Computes the net basis of all the bonds in the underlying basket from the curves and the future price.
   *
   * @param future
   *          The future security.
   * @param issuerMulticurves
   *          The issuer and multi-curves provider.
   * @param futurePrice
   *          The future price.
   * @return The net basis for each bond in the basket.
   */
  public double[] netBasisAllBonds(final BondFuturesTransaction future, final IssuerProviderInterface issuerMulticurves,
      final double futurePrice) {
    ArgumentChecker.notNull(future, "Future");
    return METHOD_FUTURES_SEC.netBasisAllBonds(future.getUnderlyingSecurity(), issuerMulticurves, futurePrice);
  }

  /**
   * Computes the net basis of associated to the cheapest to deliver bonds in the underlying basket from the curves and the future price.
   *
   * @param future
   *          The future security.
   * @param issuerMulticurves
   *          The issuer and multi-curves provider.
   * @param futurePrice
   *          The future price.
   * @return The net basis.
   */
  public double netBasisCheapest(final BondFuturesTransaction future, final IssuerProviderInterface issuerMulticurves,
      final double futurePrice) {
    ArgumentChecker.notNull(future, "Future");
    return METHOD_FUTURES_SEC.netBasisCheapest(future.getUnderlyingSecurity(), issuerMulticurves, futurePrice);
  }

}
